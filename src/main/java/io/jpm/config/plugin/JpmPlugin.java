package io.jpm.config.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class JpmPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        // 1. Extension 생성
        JpmDdlExtension extension = project.getExtensions().create("jpm", JpmDdlExtension.class);

        project.afterEvaluate(p -> {
            p.getTasks().withType(JavaCompile.class).all(task -> {
                task.getOptions().setDebug(true);
                task.getOptions().getDebugOptions().setDebugLevel("source,lines,vars");
                // Java 8 컴파일러에서 라인 정보 유실 방지를 위한 포크 설정
                task.getOptions().setFork(true);
            });
        });

        // 2. 아키텍처 체크 태스크 등록
        project.getTasks().register("validateJpmStructure", JpmArchitectureCheckTask.class, task -> {
            task.setGroup("verification");

            JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
            SourceSet mainSourceSet = javaExtension.getSourceSets().getByName("main");

            task.setClassesDirs(
                    project.getLayout().getBuildDirectory()
                            .dir("classes/java/main")
                            .get()
                            .getAsFileTree());


                    // compileJava가 끝난 "직후"에 실행되도록 명시
            task.mustRunAfter(project.getTasks().named("compileJava"));
        });

        // [수정 2] 'classes' 태스크가 실행될 때 우리 검증 태스크를 반드시 포함시키도록 함
        project.getTasks().named("classes").configure(t -> {
            t.dependsOn("validateJpmStructure");
        });

        // 4. JavaCompile 설정 (디버그 옵션 및 옵션 주입)
        project.getTasks().withType(JavaCompile.class).configureEach(task -> {

            // [위치 0 문제 해결] 컴파일러에게 라인 넘버 테이블 생성을 강제함 (Java 8 필수 설정)
            task.getOptions().setDebug(true);
            task.getOptions().getDebugOptions().setDebugLevel("source,lines,vars");

            // --- 기존 로직 (Properties 읽기) ---
            File propsFile = project.file("src/main/resources/application.properties");
            if (propsFile.exists()) {
                task.getInputs().file(propsFile);
            }

            Properties props = new Properties();
            if (propsFile.exists()) {
                try (InputStream input = new FileInputStream(propsFile)) {
                    props.load(input);
                } catch (IOException e) {
                    project.getLogger().warn("⚠️ [JPM Plugin] Failed to read properties: " + e.getMessage());
                }
            }

            String url = getVal(extension.getUrl().getOrNull(), props.getProperty("spring.datasource.url"), "");
            String username = getVal(extension.getUsername().getOrNull(), props.getProperty("spring.datasource.username"), "");
            String password = getVal(extension.getPassword().getOrNull(), props.getProperty("spring.datasource.password"), "");
            String dbType = getVal(extension.getDbType().getOrNull(), props.getProperty("jpm.ddl.db-type"), "MYSQL");
            String auto = getVal(extension.getAuto().getOrNull(), props.getProperty("jpm.ddl.auto"), "NONE");
            String projectDir = project.getProjectDir().getAbsolutePath();

            // --- 컴파일러 옵션 주입 ---
            List<String> compilerArgs = new ArrayList<>();
            compilerArgs.add("-AprojectDir=" + projectDir);
            compilerArgs.add("-Aurl=" + url);
            compilerArgs.add("-Ausername=" + username);
            compilerArgs.add("-Apassword=" + password);
            compilerArgs.add("-AdbType=" + dbType);
            compilerArgs.add("-Aauto=" + auto);
            task.getOptions().getCompilerArgs().addAll(compilerArgs);

            // [로그 위치 교정] 설정 단계가 아닌 '실행' 단계에서 로그 출력
            task.doFirst(t -> {
                project.getLogger().lifecycle("\n========== 🛠️ [JPM Plugin Config] ==========");
                project.getLogger().lifecycle("   👉 URL       : " + url);
                project.getLogger().lifecycle("   👉 Username  : " + username);
                project.getLogger().lifecycle("   👉 Password  : " + (password.isEmpty() ? "(empty)" : "****"));
                project.getLogger().lifecycle("   👉 DB Type   : " + dbType);
                project.getLogger().lifecycle("   👉 Auto Mode : " + auto);
                project.getLogger().lifecycle("===========================================\n");
            });
        });
    }

    private String getVal(String extVal, String propVal, String defVal) {
        if (extVal != null && !extVal.isEmpty()) return extVal;
        if (propVal != null && !propVal.isEmpty()) return propVal;
        return defVal;
    }
}