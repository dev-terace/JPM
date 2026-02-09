package config.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.testing.Test; // 이거 임포트 필수

public class JpmPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        // ... (앞부분 동일) ...
        JpmExtension extension = project.getExtensions().create("myDdl", JpmExtension.class);

        // ... (의존성 설정 등 동일) ...

        // 🔥 [핵심 추가] 테스트 실행 시(Run Test), 설정값을 시스템 프로퍼티로 주입!
        project.afterEvaluate(p -> {
            p.getTasks().withType(Test.class).configureEach(testTask -> {

                // 사용자가 입력한 값 가져오기
                String url = extension.getUrl();
                String user = extension.getUsername();
                String pass = extension.getPassword();

                // 값이 있을 때만 주입 (Null 체크)
                if (url != null) testTask.systemProperty("jpm.url", url);
                if (user != null) testTask.systemProperty("jpm.user", user);
                if (pass != null) testTask.systemProperty("jpm.password", pass);

                System.out.println("💉 [JPM Plugin] Injected DB config into Test Environment.");
            });
        });

        // ... (컴파일러 옵션 설정 부분 동일) ...
    }
}