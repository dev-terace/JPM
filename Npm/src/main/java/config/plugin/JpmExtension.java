package config.plugin;

public class JpmExtension {
    // 기존 필드
    private String dbType = "POSTGRES";
    private String auto = "NONE";

    // 🔥 [추가] DB 접속 정보 필드 추가
    private String url = "";
    private String username = "";
    private String password = "";

    // Getter & Setter (필수)
    public String getDbType() { return dbType; }
    public void setDbType(String dbType) { this.dbType = dbType; }

    public String getAuto() { return auto; }
    public void setAuto(String auto) { this.auto = auto; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}