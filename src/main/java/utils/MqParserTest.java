package utils;

import mq_mapper.domain.vo.EntityMeta;

public class MqParserTest {

    public static void main(String[] args) {
        System.out.println("🚀 MQ DSL Parser Test 시작 (Spring-free mode)\n");

        try {


        } catch (Exception e) {
            System.err.println("❌ 테스트 도중 에러 발생!");
            e.printStackTrace();
        }
    }

    private static void printColumnInfo(EntityMeta meta, String javaFieldName) {
        String dbColumn = meta.getColumn(javaFieldName);
        System.out.printf("👉 Java Field: [%-12s]  --->  DB Column: [%s]\n", javaFieldName, dbColumn);
    }
}
