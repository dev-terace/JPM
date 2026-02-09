package auto_ddl;

public enum AutoDDLPolicy {
    DISABLED,        // 자동 DDL 수행 안 함
    CREATE,          // 테이블 생성
    DROP_AND_CREATE  // 기존 테이블 삭제 후 생성
}
