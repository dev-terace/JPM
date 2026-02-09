package dsl_variable.v2;

public enum ColumnType {
    INTEGER, LONG, STRING, BOOLEAN,
    LOCAL_DATE, LOCAL_DATE_TIME,
    FK; // 외래키
}