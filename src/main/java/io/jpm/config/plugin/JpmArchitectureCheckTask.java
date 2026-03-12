package io.jpm.config.plugin;

import com.tngtech.archunit.core.domain.*;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import io.jpm.api.MField;
import io.jpm.common.exception.ErrorCode;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.file.Path;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;


public class JpmArchitectureCheckTask extends DefaultTask {

    private static final String[] INTERNAL_PACKAGES = {
            "io.jpm.core..",
            "io.jpm.api..",
            "io.jpm.common..",
            "io.jpm.config.."
    };

    private static final String[] FORBIDDEN_PREFIXES = {
            "io.jpm.core",
            "io.jpm.common",
            "io.jpm.config"
    };

    private FileCollection classesDirs;

    @InputFiles
    public FileCollection getClassesDirs() { return classesDirs; }
    public void setClassesDirs(FileCollection dirs) { this.classesDirs = dirs; }

    @TaskAction
    public void checkArchitecture() {
        System.setProperty("archunit.fail_on_empty_should_clauses", "false");
        List<Path> paths = classesDirs.getFiles().stream()
                .map(File::toPath)
                .collect(Collectors.toList());

        JavaClasses importedClasses =
                new ClassFileImporter().importPaths(paths);


        fieldEncapsulationRule(paths).check(importedClasses);
        getterNamingRule(paths).check(importedClasses);
        checkAnnotatedClassNotInDefaultPackage(importedClasses, paths);

    }

    // ──────────────────────────────────────────────
    // Rule 1: Package Access Control
    // ──────────────────────────────────────────────
    private void checkAnnotatedClassNotInDefaultPackage(JavaClasses importedClasses, List<Path> paths) {
        for (JavaClass item : importedClasses) {
            if (item.getPackageName().isEmpty() && !item.getAnnotations().isEmpty()) {
                String absolutePath = resolveAbsoluteSourcePath(item, paths);
                String description = String.format(
                        ErrorCode.ANNOTATION_DEFAULT_PACKAGE_FORBIDDEN.getMessage(),
                        item.getSimpleName()
                );
                String report = buildReport(
                        ErrorCode.ANNOTATION_DEFAULT_PACKAGE_FORBIDDEN.getCode(),
                        description, absolutePath, 1
                );
                throw new RuntimeException(report);
            }
        }
    }





    private ArchRule fieldEncapsulationRule(List<Path> classesPaths) {
        return classes()
                .should(new ArchCondition<JavaClass>
                        ("MField fields must be private") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        // ✅ JPM 내부 패키지 제외

                        item.getFields().stream()
                                .filter(field -> field.getRawType().isEquivalentTo(MField.class))
                                .forEach(field -> {

                                    if(field.getModifiers().contains(JavaModifier.PRIVATE))
                                    {
                                        return;
                                    }

                                    String absolutePath = resolveAbsoluteSourcePath(item, classesPaths);
                                    int line = findFieldLineNumber(absolutePath, field.getName());
                                    String description = String.format("Field '%s' must be private", field.getName());
                                    String report = buildReport(ErrorCode.M_FIELD_MUST_PRIVATE.getCode(), description, absolutePath, line);
                                    events.add(SimpleConditionEvent.violated(field, report));
                                });
                    }
                });
    }

    // ──────────────────────────────────────────────
// Rule 3: Getter Naming Convention
// ──────────────────────────────────────────────


    private int findFieldLineNumber(String absolutePath, String fieldName) {
        try {
            List<String> lines = java.nio.file.Files.readAllLines(java.nio.file.Paths.get(absolutePath));
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                // 필드 선언 라인 찾기
                if (line.contains(fieldName) && !line.trim().startsWith("//")) {
                    return i + 1; // 1-based
                }
            }
        } catch (Exception e) {
            // 파일 못 읽으면 0 반환
        }
        return 0;
    }

    private ArchRule getterNamingRule(List<Path> classesPaths) {
        return classes()
                .should(new ArchCondition<JavaClass>("") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        item.getFields().stream()
                                .filter(field -> field.getRawType().isEquivalentTo(MField.class))
                                .forEach(field -> checkGetterNaming(item, field, events, classesPaths));
                    }
                });
    }

    private void checkGetterNaming(JavaClass item, JavaField field, ConditionEvents events, List<Path> classesPaths) {
        String expectedGetter = "get" + capitalize(field.getName());

        Optional<JavaMethod> anyMFieldGetter = item.getMethods().stream()
                .filter(m -> m.getRawReturnType().isEquivalentTo(MField.class))
                .findFirst();

        if (!anyMFieldGetter.isPresent()) return;

        boolean hasCorrectGetter = item.getMethods().stream()
                .anyMatch(m -> m.getName().equals(expectedGetter)
                        && m.getRawReturnType().isEquivalentTo(MField.class));

        if (hasCorrectGetter) return;

        JavaMethod wrongMethod = anyMFieldGetter.get();
        int line = wrongMethod.getSourceCodeLocation().getLineNumber();

        if (line <= 0) return;

        String absolutePath = resolveAbsoluteSourcePath(item, classesPaths);
        String description = String.format(
                ErrorCode.M_FIELD_GETTER_NAMING_INVALID.getMessage(),
                field.getName(), expectedGetter
        );
        String buildReport = buildReport(ErrorCode.M_FIELD_GETTER_NAMING_INVALID.getCode(), description, absolutePath, line);

        events.add(SimpleConditionEvent.violated(wrongMethod, buildReport));
    }



    private String buildReport(String errorCode, String description, String absolutePath, int line) {
        return  String.format("================================================================================" +
                "\n[JPM ARCHITECTURE VIOLATION]" +
                "\n================================================================================" +
                "\n▶ Error: %s"+
                "\n  Desc: %s"+
                "\n  at (%s:%d)"+
                "\n---------------------------------------------------------------------------------\n", errorCode, description, absolutePath, line);
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    /**
     * 클래스 파일 경로 기준으로 소스 파일 절대 경로를 역추적합니다.
     *
     * build/classes/java/main  →  src/main/java
     * build/classes/kotlin/main →  src/main/kotlin
     *
     * 예) /project/build/classes/java/main/io/jpm/foo/Bar.class
     *  →  /project/src/main/java/io/jpm/foo/Bar.java
     */
    private String resolveAbsoluteSourcePath(JavaClass javaClass, List<Path> classesPaths) {
        String packagePath = javaClass.getPackageName().replace('.', '/');
        String simpleFileName = javaClass.getSimpleName() + ".java";

        for (Path classesDir : classesPaths) {
            Path normalized = classesDir.toAbsolutePath().normalize();

            // ← 이거 추가해서 실제 경로 확인


            Path projectRoot = findProjectRoot(normalized);


            if (projectRoot == null) continue;

            for (String lang : new String[]{"java", "kotlin"}) {
                Path candidate = projectRoot
                        .resolve("src/main/" + lang)
                        .resolve(packagePath)
                        .resolve(simpleFileName)
                        .normalize();



                if (candidate.toFile().exists()) {
                    return candidate.toString();
                }
            }
        }

        return simpleFileName; // fallback
    }

    /**
     * build/classes/java/main 구조에서 프로젝트 루트를 추정합니다.
     * "build" 디렉토리의 부모를 프로젝트 루트로 간주합니다.
     */
    private Path findProjectRoot(Path classesDir) {
        Path current = classesDir;
        while (current != null) {
            if (current.getFileName() != null && current.getFileName().toString().equals("build")) {
                return current.getParent();
            }
            current = current.getParent();
        }
        return null;
    }

    private boolean isForbiddenPackage(String packageName) {

        for (String prefix : FORBIDDEN_PREFIXES) {
            if (packageName.startsWith(prefix)) return true;
        }

        return false;
    }

    private String capitalize(String name) {
        if (name == null || name.isEmpty()) return name;
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }




    // ──────────────────────────────────────────────
// Rule 4: Annotation Default Package 금지
// ──────────────────────────────────────────────

}