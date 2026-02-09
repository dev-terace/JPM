package config;

import auto_ddl.AutoDDLPolicy;


public class AppConfig {


    private AppConfig() {}


    public static DBTypePolicy DBTypePolicy = config.DBTypePolicy.MYSQL;;
    public static AutoDDLPolicy AUTO_DDL_POLICY;
    public static String MAPPER_NAME_SPACE = "dev.sj.jqm.mapper.";

    
    private static boolean isFirstSet = true;


    static {
        AUTO_DDL_POLICY = AutoDDLPolicy.CREATE;
    }
    // Setter


    // ==========================

    public static void setCompleted()
    {
        isFirstSet = true;
    }
    public static void setAutoDDLPolicy(AutoDDLPolicy policy) {
        if(isFirstSet) {
            AUTO_DDL_POLICY = policy;
        }
    }

    public static void setDBType(DBTypePolicy dbType) {
        if(isFirstSet) {
            DBTypePolicy = dbType;
        }
    }

    // ==========================
    // Getter / 상태 확인
    // ==========================
    public static AutoDDLPolicy getAutoDDLPolicy() {
        return AUTO_DDL_POLICY;
    }

    public static DBTypePolicy getDBType() {
        return DBTypePolicy;
    }

    public static boolean isAutoDDLEnabled() {
        return AUTO_DDL_POLICY != AutoDDLPolicy.DISABLED;
    }

    public static boolean isCreate() {
        return AUTO_DDL_POLICY == AutoDDLPolicy.CREATE;
    }

    public static boolean isDropAndCreate() {
        return AUTO_DDL_POLICY == AutoDDLPolicy.DROP_AND_CREATE;
    }
}
