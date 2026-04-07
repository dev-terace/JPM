package io.jpm.core.jpm_repository.domain.model;

import io.jpm.common.utils.LogPrinter;
import io.jpm.core.jpm_repository.domain.cache.interfaces.EntityRelationRegistry;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.utils.ColumnResolver;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 쿼리 결과를 Java 객체로 변환하기 위한 매핑 메타데이터
 * (스스로 MethodMeta를 읽어서 매핑 정보를 추출합니다)
 */
public class ResultMapMeta {

    private final List<FieldMapping> idMappings = new ArrayList<>();
    private final List<FieldMapping> resultMappings = new ArrayList<>();
    private final List<RelationMapping> associationMappings = new ArrayList<>();
    private final List<RelationMapping> collectionMappings = new ArrayList<>();

    // 기본 생성자는 private으로 막고 정적 팩토리 메서드 사용
    private ResultMapMeta() {}

    // ==============================================================
    // ★ [핵심] MethodMeta를 통째로 받아서 매핑 정보만 쏙쏙 뽑아내는 팩토리 메서드
    // ==============================================================


    public static ResultMapMeta from(MethodMeta methodMeta, RepoMetaRegistry repoMetaRegistry, EntityRelationRegistry entityRelationRegistry) {
        ResultMapMeta meta = new ResultMapMeta();




        List<DslStatement> selectStmts = new ArrayList<>();

        DslStatement fromStmt = null;

        for (DslStatement stmt : methodMeta.getStatements()) {


            String command = stmt.getCommand();

            List<String> args = stmt.getArgs();

            LogPrinter.info("ResultMapMeta Args : "  + args.toString());

            // 기존 mapId, mapResult 처리 로직 (필요시 활성화)

            if(fromStmt == null && command.contains("select"))
            {
               selectStmts.add(stmt);

            }

            if(fromStmt == null && command.contains("from"))
            {
                fromStmt = stmt;
                LogPrinter.info("ResultMapMeta fromStmts : "  + stmt);
            }



            if ("mapId".equals(command) && args.size() >= 2) {
                meta.idMappings.add(new FieldMapping(args.get(0), args.get(1)));
            } else if ("mapResult".equals(command) && args.size() >= 2) {
                meta.resultMappings.add(new FieldMapping(args.get(0), args.get(1)));
            }
            else if("selectRawResult".equals(command)) {
                LogPrinter.info("Select Raw Result : " + args);

                for (String part : splitSelectRawSql(args.get(0))) {
                    if (part.toUpperCase().contains(" AS ")) {
                        // 특정 프로세스

                        String asName = part.split(" AS ")[1];
                        meta.resultMappings.add(new FieldMapping(snakeToCamel(asName), asName));
                    }
                }


            }


        }





        resolveIdMappings(fromStmt, selectStmts, repoMetaRegistry, entityRelationRegistry, meta);


        return meta;
    }



    private static void resolveIdMappings(DslStatement fromStmt, List<DslStatement> selectStmts,
                                   RepoMetaRegistry repoMetaRegistry, EntityRelationRegistry entityRelationRegistry,  ResultMapMeta meta) {
        if (fromStmt == null) return;

        String fromTargetEntity = fromStmt.getArgs().get(0).replace(".class", "");
        String fromTargetPkFieldName = entityRelationRegistry.getPkFieldName(fromTargetEntity);
        EntityMeta selectMeta = repoMetaRegistry.getEntityMeta(fromTargetEntity);


        String tableAlias;
        if(fromStmt.getArgs().size() >= 2) {
            tableAlias = fromStmt.getArgs().get(1);
        } else {
            tableAlias = null;
        }


        if(tableAlias == null) { //alias 없는 형태
            selectStmts.stream()
                    .flatMap(s -> s.getArgs().stream())

                    .filter(arg -> arg.split("::")[0].equals(fromTargetEntity))
                    .map(arg -> arg.split("::")[1])
                    .map(arg -> ColumnResolver.extractColumn(ColumnResolver.convertGetterToField(arg)))
                    .filter(fromTargetPkFieldName::equals)
                    .forEach(fieldName -> {
                        String columnName = selectMeta.getColumn(fieldName);
                        String aliasName = selectMeta.getTableName() + "_" + columnName;
                        LogPrinter.info("[ResultMapMeta] aliasName : " + aliasName);
                        meta.idMappings.add(new FieldMapping(fieldName, aliasName));
                    });
        }else{
            String rootPkFiledName = tableAlias + "." + selectMeta.getColumn(fromTargetPkFieldName);
            LogPrinter.info("[ResultMapMeta] tableAlias : " + rootPkFiledName);



            selectStmts.stream()
                    .flatMap(s -> s.getArgs().stream())
                    .map(arg -> {
                        String[] asParts = arg.split(" AS ");
                        String cleaned = asParts[0].trim();
                        String alias = asParts.length > 1 ? asParts[1].trim() : null;

                        int dotIdx = cleaned.lastIndexOf(".");
                        String key = dotIdx >= 0 ? cleaned.substring(dotIdx + 1) : cleaned;

                        return new AbstractMap.SimpleEntry<>(key, alias);  // ("OrderEntity::getTotalPrice", "good2")
                    })
                    .filter(entry -> entry.getKey().split("::")[0].equals(fromTargetEntity))
                    .map(entry -> new AbstractMap.SimpleEntry<>(
                            ColumnResolver.extractColumn(ColumnResolver.convertGetterToField(entry.getKey().split("::")[1])),
                            entry.getValue()  // alias 유지
                    ))
                    .peek(entry -> LogPrinter.info("[ResultMapMeta] fieldName : " + entry.getKey() + ", alias : " + entry.getValue()))
                    .filter(entry -> fromTargetPkFieldName.equals(entry.getKey()))
                    .forEach(entry -> {
                        String fieldName = entry.getKey();
                        String alias = entry.getValue();
                        String columnName = selectMeta.getColumn(fieldName);
                        String aliasName = selectMeta.getTableName() + "_" + columnName;
                        LogPrinter.info("[ResultMapMeta] alias : " + alias);

                        if(alias == null) {
                            alias = rootPkFiledName.replace(".", "_");
                        }
                        meta.idMappings.add(new FieldMapping(fieldName, alias));
                    });
        }
    }


    // ==============================================================
    // 유틸리티 2: 카멜케이스(orderId) -> 스네이크케이스(order_id) 변환기
    // ==============================================================
    private static String camelToSnake(String str) {
        if (str == null) return null;
        return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }


    private static  List<String> splitSelectRawSql(String sql) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();

        for (char c : sql.toCharArray()) {
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (c == ',' && depth == 0) {
                parts.add(current.toString().trim());
                current = new StringBuilder();
                continue;
            }
            current.append(c);
        }

        if (current.length() > 0) {
            parts.add(current.toString().trim());
        }

        return parts;
    }



    private static String snakeToCamel(String str) {
        if (str == null) return null;
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (char c : str.toCharArray()) {
            if (c == '_') {
                nextUpper = true; // 다음 글자는 대문자로
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }

    // ==========================================
    // Getters
    // ==========================================
    public List<FieldMapping> getIdMappings() { return idMappings; }
    public List<FieldMapping> getResultMappings() { return resultMappings; }
    public List<RelationMapping> getAssociationMappings() { return associationMappings; }
    public List<RelationMapping> getCollectionMappings() { return collectionMappings; }

    // ==========================================
    // 내부 VO 클래스들
    // ==========================================
    public static class FieldMapping {
        private final String fieldName;   // 예: "id"
        private final String columnName;  // 예: "user_id"

        public FieldMapping(String fieldName, String columnName) {
            this.fieldName = fieldName;
            this.columnName = columnName;
        }
        public String getFieldName() { return fieldName; }
        public String getColumnName() { return columnName; }
    }

    public static class RelationMapping {
        private final String fieldName;      // 예: "orders"
        private final String targetClass;    // 예: "OrderEntity"

        // MyBatis <id> 태그 용도
        private final String childIdProperty; // 예: "orderId"
        private final String childIdColumn;   // 예: "order_id"

        // 유연한 처리를 위해 4개를 모두 받는 생성자
        public RelationMapping(String fieldName, String targetClass, String childIdProperty, String childIdColumn) {
            this.fieldName = fieldName;
            this.targetClass = targetClass.replace(".class", ""); // .class 자동 제거
            this.childIdProperty = childIdProperty;
            this.childIdColumn = childIdColumn;
        }

        public String getFieldName() { return fieldName; }
        public String getTargetClass() { return targetClass; }
        public String getChildIdProperty() { return childIdProperty; }
        public String getChildIdColumn() { return childIdColumn; }
    }
}