package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support;

import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.common.utils.LogPrinter;

import java.util.List;

import java.util.stream.Collectors;

/**
 * DSL 인자 문자열(Java 필드 참조 등)을 DB 컬럼명으로 변환합니다.
 *
 * <p>기존 resolveArgs / resolveArg / toSnakeCase 메서드를 단일 책임으로 분리했습니다.
 */
public class ArgResolver {

    private final RepoMetaRegistry repoMetaRegistry;

    public ArgResolver(RepoMetaRegistry repoMetaRegistry) {
        this.repoMetaRegistry = repoMetaRegistry;
    }

    public List<String> resolveAll(List<String> rawArgs, EntityMeta mainMeta, BuildContext ctx) {

        LogPrinter.info("[ArgResolver] resolveAll rawArgs = "+rawArgs );
        return rawArgs.stream()
                .map(arg -> arg != null ? resolve(arg, mainMeta, ctx) : "")
                .collect(Collectors.toList());
    }

    public String resolve(String arg, EntityMeta mainMeta, BuildContext ctx) {
        if (arg == null || arg.trim().isEmpty()) return arg;
        try {
            AliasAndArg parsed = splitAsAlias(arg);
            String resolved    = resolveCore(parsed.arg, mainMeta, ctx);
            return resolved + parsed.asClause;
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }

    // -------------------------------------------------------------------------
    // private – 핵심 변환 로직
    // -------------------------------------------------------------------------

    private String resolveCore(String arg, EntityMeta mainMeta, BuildContext ctx) {


        if (arg.contains("::")) {
            return resolveMethodRef(arg, mainMeta, ctx);
        }
        if (arg.contains(".") && !arg.contains("(")) {
            return resolveDotNotation(arg, mainMeta);
        }
        return arg;
    }

    /** "ClassName::getField" 또는 "alias|ClassName::getField" 형태 처리 */
    private String resolveMethodRef(String arg, EntityMeta mainMeta, BuildContext ctx) {
        String[] parts        = arg.split("::");
        String refObj         = parts[0].trim();
        String fieldName      = extractFieldName(parts[1].trim());

        String classNameForMeta   = refObj;
        String explicitTableAlias = null;



        if (refObj.contains(".")) {
            LogPrinter.info("[ArgResolver] resolveMethodRef refObj = "+refObj);
            String[] refParts     = refObj.split("\\.");
            explicitTableAlias    = refParts[0];
            classNameForMeta      = refParts[1];
        }



        EntityMeta targetMeta = "target".equals(classNameForMeta)
                ? mainMeta
                : repoMetaRegistry.getEntityMeta(classNameForMeta);



        if (targetMeta != null) {
            String columnName = targetMeta.getColumn(fieldName);
            String finalCol   = (columnName != null && !columnName.isEmpty())
                    ? columnName
                    : toSnakeCase(fieldName);

            String tableName = targetMeta.getTableName();
            String alias     = explicitTableAlias != null
                    ? explicitTableAlias
                    : ctx.resolveAlias(tableName);

            LogPrinter.info("[ArgResolver] resolveMethodRef alias = "+explicitTableAlias);

            // 메인 테이블이고 별칭 접두어 불필요한 경우 컬럼명만 반환
            if (!ctx.isRequiresPrefix()
                    && tableName.equals(mainMeta.getTableName())
                    && alias.equals(tableName)) {

                return finalCol;
            }

            LogPrinter.info("[resolveMethodRef] : " + alias + "." + finalCol);
            return alias + "." + finalCol;
        }

        // 메타 없는 경우 fallback
        if (explicitTableAlias != null) {
            return explicitTableAlias + "." + toSnakeCase(fieldName);
        }
        return toSnakeCase(fieldName);
    }

    /** "alias.field" 형태 처리 */
    private String resolveDotNotation(String arg, EntityMeta mainMeta) {
        String[] parts    = arg.split("\\.");
        if (parts.length != 2) return arg;

        String alias     = parts[0];
        String fieldName = parts[1];

        String dbCol = mainMeta.getColumn(fieldName);
        if (dbCol != null) {
            return mainMeta.getTableName() + "." + dbCol;
        }
        return alias + "." + fieldName;
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    /**
     * 인자 문자열에서 AS 별칭 부분을 분리합니다.
     * "Entity::getField|alias_name" → arg="Entity::getField", asClause=" AS alias_name"
     */
    private AliasAndArg splitAsAlias(String raw) {
        int lastPipeIdx     = raw.lastIndexOf('|');
        int doubleColonIdx  = raw.indexOf("::");

        if (lastPipeIdx > 0 && lastPipeIdx > doubleColonIdx) {
            return new AliasAndArg(
                    raw.substring(0, lastPipeIdx),
                    " AS " + raw.substring(lastPipeIdx + 1)
            );
        }
        return new AliasAndArg(raw, "");
    }

    private String extractFieldName(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        }
        return methodName;
    }

    private String toSnakeCase(String camel) {
        if (camel == null) return null;
        return camel.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    // -------------------------------------------------------------------------
    // 내부 값 객체
    // -------------------------------------------------------------------------

    private static class AliasAndArg {
        final String arg;
        final String asClause;
        AliasAndArg(String arg, String asClause) {
            this.arg      = arg;
            this.asClause = asClause;
        }
    }
}