package io.jpm.core.jpm_repository.generator;

import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.domain.model.MapJoinMeta;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.ResultMapMeta;
import io.jpm.common.utils.LogPrinter;

import java.util.List;
import java.util.Map;

public class MybatisXmlGenerator {

    /**
     * 하나의 메서드를 생성하는 데 필요한 데이터 묶음
     */


    private final RepoMetaRegistry repoMetaRegistry;

    public MybatisXmlGenerator(RepoMetaRegistry repoMetaRegistry) {
        this.repoMetaRegistry = repoMetaRegistry;
    }

    public static class MethodData {
        private final MethodMeta methodMeta;
        private final ResultMapMeta meta;
        private final String sql;

        public MethodData(MethodMeta methodMeta, ResultMapMeta meta, String sql) {
            this.methodMeta = methodMeta;
            this.meta = meta;
            this.sql = sql;
        }

        public MethodMeta getMethodMeta() { return methodMeta; }
        public ResultMapMeta getMeta() { return meta; }
        public String getSql() { return sql; }
    }

    // ========================================================
    // 🚀 [추가됨] 문자열 정제 유틸리티 메서드
    // ========================================================

    /**
     * "MEntity3::getId" -> "id"
     * "MEntity3::getDescription" -> "description" 으로 변환
     */


    private String toPropertyName(String rawArg) {
        if (rawArg == null || rawArg.trim().isEmpty()) return rawArg;

        if (rawArg.contains("::")) {
            String methodName = rawArg.split("::")[1].trim();



            if (methodName.startsWith("get") && methodName.length() > 3) {
                return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            }
            return methodName;
        }

        if (rawArg.startsWith("#{") && rawArg.endsWith("}")) {
            return rawArg.substring(2, rawArg.length() - 1);
        }

        return rawArg;
    }

    /**
     * "MEntity2::getOrder" -> DB 컬럼명 또는 "get_order" 로 변환
     */
    private String toColumnName(String rawArg) {
        if (rawArg == null || rawArg.trim().isEmpty()) return rawArg;

        if (rawArg.contains("::")) {
            String[] parts = rawArg.split("::");
            String className = parts[0].trim();
            String propertyName = toPropertyName(rawArg);

            // 1. EntityMetaRegistry에서 컬럼명을 가져옴
            EntityMeta targetMeta = repoMetaRegistry.getEntityMeta(className);
            if (targetMeta != null) {
                String dbCol = targetMeta.getColumn(propertyName);

                // 🚀 [수정] Registry에서 가져온 값이 "orderName"처럼 카멜 케이스일 수 있으므로
                // 무조건 안전하게 스네이크 케이스로 변환해서 내보냅니다.
                if (dbCol != null) {
                    return toSafeSnakeCase(dbCol);
                }
            }

            // 2. 메타가 없어도 기본 필드명을 안전하게 변환
            return toSafeSnakeCase(propertyName);
        }
        return rawArg;
    }

    // 🚀 [추가] 완벽한 스네이크 케이스 변환 & 앞글자 언더바 방지 유틸
    private String toSafeSnakeCase(String str) {
        if (str == null || str.trim().isEmpty()) return str;

        // 1. 소문자 뒤에 대문자가 올 때만 언더바 추가 (사용자님이 쓰시던 훌륭한 정규식!)
        String snake = str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();

        // 2. 🚨 만약 레지스트리에 이미 "_order_name" 처럼 언더바가 붙어있었거나,
        // 알 수 없는 이유로 맨 앞에 '_'가 생겼다면 강제로 제거! (무한 루프로 싹 다 제거)
        while (snake.startsWith("_")) {
            snake = snake.substring(1);
        }

        return snake;
    }

    /**
     * 메타데이터와 SQL 리스트를 조합하여 MyBatis XML Mapper 문자열을 생성합니다.
     *
     * @param namespace 매퍼의 네임스페이스 (예: "mq_repository.UserRepository")
     * @param methods   생성할 메서드들의 데이터 리스트
     * @return 완성된 MyBatis XML 포맷 문자열
     */
    public String generateXml(String namespace, List<MethodData> methods) {
        StringBuilder xml = new StringBuilder();

        // ========================================================
        // 1. XML 헤더 및 매퍼 여는 태그 (한 번만 생성)
        // ========================================================



        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        xml.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" ")
                .append("\"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n\n");

        xml.append("<mapper namespace=\"").append(namespace).append("\">\n\n");

        // ========================================================
        // 2. 전달받은 메서드 목록을 순회하며 ResultMap과 Query 생성
        // ========================================================

        for (MethodData method : methods) {
            MethodMeta methodMeta = method.getMethodMeta();





            ResultMapMeta meta = method.getMeta();
            String sql = method.getSql() != null ? method.getSql() : "";

            String methodName = methodMeta.getMethodName();


            LogPrinter.info("[XML] methodName=" + methodName);
            LogPrinter.info("[XML] targetType=" + methodMeta.getTargetType()); // null or "orders" 이면 문제
            LogPrinter.info("[XML] statements=" + methodMeta.getStatements());

            String resultMapId = methodName + "ResultMap";

            // 생성된 SQL의 첫 단어를 확인하여 태그 종류(select/insert/update/delete) 결정
            String sqlTrimmed = sql.trim().toUpperCase();
            String tagName = "select"; // 기본값

            if (sqlTrimmed.startsWith("INSERT")) {
                tagName = "insert";
            } else if (sqlTrimmed.startsWith("UPDATE")) {
                tagName = "update";
            } else if (sqlTrimmed.startsWith("DELETE")) {
                tagName = "delete";
            }

            // ========================================================
            // --- <resultMap> 태그 생성 (SELECT 일 때만 생성) ---
            // ========================================================
            if ("select".equals(tagName)) {
                xml.append("    <resultMap id=\"").append(resultMapId).append("\" ")
                        .append("type=\"").append(methodMeta.getTargetType()).append("\" autoMapping=\"true\">\n");

                // 1-1. <id> 매핑 (PK)
                for (ResultMapMeta.FieldMapping idMap : meta.getIdMappings()) {
                    // 🚀 [적용] property와 column 정제
                    String prop = toPropertyName(idMap.getFieldName());
                    String col = toColumnName(idMap.getColumnName());
                    xml.append("        <id property=\"").append(prop).append("\" column=\"").append(col).append("\"/>\n");
                }

                // 1-2. <result> 매핑 (일반 필드)
                for (ResultMapMeta.FieldMapping resultMap : meta.getResultMappings()) {
                    // 🚀 [적용] property와 column 정제
                    String prop = toPropertyName(resultMap.getFieldName());
                    String col = toColumnName(resultMap.getColumnName());
                    xml.append("        <result property=\"").append(prop).append("\" column=\"").append(col).append("\"/>\n");
                }



                List<MapJoinMeta> mapJoins = methodMeta.getMapJoins();











                for (MapJoinMeta mj : mapJoins) {
                    String propName = toPropertyName(mj.getParentField());
                    String alias = mj.getAlias();
                    String targetJavaType = mj.getJavaType();
                    String pkFieldName = mj.getPkFieldName();
                    String pkColName = mj.getPkColName();

                    if (mj.isList()) {
                        xml.append("        <collection property=\"").append(propName)
                                .append("\" ofType=\"").append(targetJavaType)
                                .append("\" autoMapping=\"true\" columnPrefix=\"").append(alias).append("_\">\n")
                                .append("            <id property=\"").append(pkFieldName)
                                .append("\" column=\"").append(pkColName).append("\"/>\n")
                                .append("        </collection>\n");


                    } else {
                        xml.append("        <association property=\"").append(propName)
                                .append("\" javaType=\"").append(targetJavaType)
                                .append("\" autoMapping=\"true\" columnPrefix=\"").append(alias).append("_\">\n")
                                .append("            <id property=\"").append(pkFieldName)
                                .append("\" column=\"").append(pkColName).append("\"/>\n")
                                .append("        </association>\n");
                    }
                }


                xml.append("    </resultMap>\n\n");
            }

            // ========================================================
            // --- 쿼리 태그 (<select>, <insert>, <update>, <delete>) 생성 ---
            // ========================================================
            Map<String, String> params = methodMeta.getParameters();
            String parameterType = "";

            if (params.size() == 1) {
                parameterType = params.values().iterator().next();
            } else if (params.size() > 1) {
                parameterType = "map"; // 파라미터가 여러 개일 경우 MyBatis의 기본 처리에 맞게 map 사용
            }

            xml.append("    <").append(tagName).append(" id=\"").append(methodName).append("\"");

            if (!parameterType.isEmpty()) {
                xml.append(" parameterType=\"").append(parameterType).append("\"");
            }

            if ("select".equals(tagName)) {
                xml.append(" resultMap=\"").append(resultMapId).append("\"");
            }

            xml.append(">\n");

            xml.append("        <![CDATA[\n");
            String formattedSql = "            " + sql.replace("\n", "\n            ");
            xml.append(formattedSql).append("\n");
            xml.append("        ]]>\n");

            xml.append("    </").append(tagName).append(">\n\n");
        }

        // ========================================================
        // 3. 매퍼 닫는 태그 (한 번만 생성)
        // ========================================================
        xml.append("</mapper>");




        return xml.toString();
    }


    private String resolveTargetJavaType(String parentField) {
        String field = toPropertyName(parentField);
        return Character.toUpperCase(field.charAt(0)) + field.substring(1);
    }
}