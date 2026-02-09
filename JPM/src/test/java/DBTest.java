import executor.AutoDDLExecutor;
import executor.DbConfig;

public class DBTest {

    public static void main(String[] args) {

        // 1. 하드코딩 삭제! -> 시스템 프로퍼티에서 읽어옴
        // (플러그인이 값을 넣어줬으므로 여기서 읽힘)
        String targetUrl = System.getProperty("jpm.url");
        String user = System.getProperty("jpm.user");
        String password = System.getProperty("jpm.password");

        // 방어 로직 (혹시 설정 안 했을까봐)
        if (targetUrl == null) {
            throw new RuntimeException("❌ build.gradle에 myDdl { url = ... } 설정을 확인해주세요!");
        }

        System.out.println("running test with: " + targetUrl);

        // 2. 설정 객체 생성
        // 드라이버는 보통 URL 보고 자동 추론하거나, 필요하면 이것도 프로퍼티로 받음
        DbConfig config = new DbConfig(targetUrl, user, password, "org.postgresql.Driver");

        // 3. 실행
        AutoDDLExecutor executor = new AutoDDLExecutor(config);
        executor.run();
    }
}