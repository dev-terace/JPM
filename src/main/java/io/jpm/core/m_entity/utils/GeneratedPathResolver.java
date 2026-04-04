package io.jpm.core.m_entity.utils;

import io.jpm.common.utils.LogPrinter;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

// ── GeneratedPathResolver 수정 ───────────────────────────────
public class GeneratedPathResolver {


    private static final String DEFAULT_RESULT_PATH = "terrace.result";

    public static String resolveResult(String fqcn)
    {
        int lastDot = fqcn.lastIndexOf('.');
        String packageName        = fqcn.substring(0, lastDot);
        String className          = fqcn.substring(lastDot + 1);
        String generatedClassName = "R" + className;
        String packagePath        = packageName.replace('.', '/');

        return DEFAULT_RESULT_PATH + "." + packagePath + "." + generatedClassName;
    }
    public static Path resolveResult(String fqcn, Filer filer)  {

        int lastDot = fqcn.lastIndexOf('.');
        String packageName        = fqcn.substring(0, lastDot);
        String className          = fqcn.substring(lastDot + 1);
        String generatedClassName = "R" + className;
        String packagePath        = packageName.replace('.', '/');

        Path projectRoot = getProjectRoot(filer);

        return projectRoot
                .resolve("build")
                .resolve("generated")
                .resolve("sources")
                .resolve("annotationProcessor")
                .resolve("java")
                .resolve("main")
                .resolve(DEFAULT_RESULT_PATH)
                .resolve(packagePath)
                .resolve(generatedClassName + ".java");
    }

    private static Path getProjectRoot(Filer filer) {
        try {
            // CLASS_OUTPUT = build/classes/java/main
            // 더미 리소스 URI로 클래스 출력 경로 추출
            FileObject dummy = filer
                    .getResource(StandardLocation.CLASS_OUTPUT, "", "dummy");

            // build/classes/java/main/dummy → 상위 4개 올라가면 프로젝트 루트
            Path classOutput = Paths.get(dummy.toUri()).getParent();
            // build/classes/java/main
            //   ↑ .getParent() × 4
            // projectRoot
            return classOutput
                    .getParent() // java
                    .getParent() // classes
                    .getParent() // build
                    .getParent(); // projectRoot

        } catch (IOException e) {
            // fallback

            LogPrinter.exceptionInfo(e);
            return Paths.get(System.getProperty("user.dir"));
        }
    }
}
