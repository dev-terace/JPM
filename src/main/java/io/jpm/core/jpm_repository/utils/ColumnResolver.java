package io.jpm.core.jpm_repository.utils;




import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.BuildContext;

import java.util.List;
import java.util.stream.Collectors;

public class ColumnResolver {

    public ColumnResolver(RepoMetaRegistry repoMetaRegistry) {
        this.repoMetaRegistry = repoMetaRegistry;
    }

    private final RepoMetaRegistry repoMetaRegistry;


    public String resolve(String colStr, BuildContext ctx) {
        if (colStr.contains("::")) {
            String alias = null;
            String processingStr = colStr;

            // 1. 점(.)이 있다면 Alias와 실제 클래스명 분리
            // 예: "o1.OrderItem::getOrderId" -> alias: "o1", processingStr: "OrderItem::getOrderId"
            if (colStr.contains(".")) {
                int dotIndex = colStr.indexOf(".");
                alias = colStr.substring(0, dotIndex).trim();
                processingStr = colStr.substring(dotIndex + 1).trim();
            }

            String[] parts = processingStr.split("::");
            String className = parts[0].trim();
            String methodName = parts[1].trim();

            // 2. 메서드명을 필드명으로 변환
            String fieldName = convertGetterToField(methodName);

            // 3. 메타데이터에서 실제 컬럼명 조회
            EntityMeta entityMeta = repoMetaRegistry.getEntityMeta(className);
            if (entityMeta == null) {
                // 메타데이터가 없으면 최소한 필드명이라도 반환
                return (alias != null ? alias + "." : "") + fieldName;
            }

            String colName = entityMeta.getColumn(fieldName);
            if (colName == null) colName = fieldName;

            // 4. 최종 문자열 조립
            // 사용자가 alias(o1)를 줬다면 그걸 우선적으로 사용하고, 없으면 테이블명을 prefix로 쓸지 결정
            if (alias != null) {
                return alias + "." + colName;
            } else {
                boolean needsPrefix = ctx.isRequiresPrefix() || !ctx.getJoins().isEmpty();
                String tableName = entityMeta.getTableName(); // 혹은 repoMetaRegistry.getTable(...)
                return needsPrefix ? tableName + "." + colName : colName;
            }
        }



        return colStr;
    }

    public  String[] resolve(String colStr) {

        if (!colStr.contains("::")) {
            throw new RuntimeException("MField(::) 방식의 인자 값이 아닙니다.");
        }

        colStr = colStr.contains(".") ? colStr.split("\\.")[1] : colStr;



        String[] parts = colStr.split("::");
        String className = parts[0].trim();
        String methodName = parts[1].trim();
        String filedName = convertGetterToField(methodName);


        return new String[]{className, filedName};

    }


    //as 없이 o.id 와 같은 select 컬럼 값이 있으면 AS를 자동으로 붙여줌
    public List<String> normalizeColumnName(List<String> columns) {
        return
                columns.stream()
                        .map(col -> {
                            // 이미 AS가 붙어있으면 그대로 반환
                            if (col.toLowerCase().contains(" as ")) {
                                return col;
                            }
                            // o.id -> o_id
                            if (col.contains(".")) {
                                String alias = col.replace(".", "_");
                                return col + " AS " + alias;  // AS alias 추가
                            }
                            // 루트 컬럼은 그대로
                            return col;
                        })
                        .collect(Collectors.toList());
    }



    public static String convertGetterToField(String methodName) {

        if (methodName.startsWith("get") && methodName.length() > 3) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
            return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        } else {
            throw new RuntimeException("[convertGetterToField] 알 수 없는 메서드명: " + methodName);
        }
    }


    public static String extractColumn(String arg) {
        // 1. AS 제거
        String expr = arg.split("(?i)\\s+AS\\s+")[0].trim();

        // 2. 함수 제거 (예: MAX(...))
        if (expr.contains("(")) {
            expr = expr.replaceAll(".*\\((.*)\\)", "$1");
        }

        // 3. alias 제거 (o1.order_id → order_id)
        if (expr.contains(".")) {
            expr = expr.substring(expr.lastIndexOf('.') + 1);
        }

        return expr.trim();
    }



}