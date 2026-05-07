package io.jpm.core.jpm_repository.runtime.core;



import io.jpm.common.utils.LogPrinter;
import io.jpm.core.jpm_repository.domain.cache.interfaces.EntityRelationRegistry;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.*;
import io.jpm.core.jpm_repository.runtime.config.AppConfig;
import io.jpm.core.jpm_repository.utils.ColumnResolver;

import java.util.*;


/**
 * 쿼리 결과를 Java 객체로 변환하기 위한 매핑 메타데이터
 * (스스로 MethodMeta를 읽어서 매핑 정보를 추출합니다)
 */



public class ResultMapMetaV2 {

    private final List<ResultMapFactoryV2.FieldMapping> idMappings = new ArrayList<>();
    private final List<ResultMapFactoryV2.FieldMapping> resultMappings = new ArrayList<>();
    private final List<ResultMapFactoryV2.RelationMapping> associationMappings = new ArrayList<>();
    private final List<ResultMapFactoryV2.RelationMapping> collectionMappings = new ArrayList<>();
    private static final RepoMetaRegistry repoMetaRegistry = AppConfig.getRepoMetaRegistry();
    private static final EntityRelationRegistry entityRelationRegistry = AppConfig.getEntityRelationRegistry();
    private static final ColumnResolver columResolver = new ColumnResolver(repoMetaRegistry);

    private static final String CMD_SELECT = "select";
    private static final String CMD_FROM = "from";
    private static final String CMD_JOIN = "Join";
    private static final String CMD_MAP_ID = "mapId";
    private static final String CMD_MAP_RESULT = "mapResult";
    private static final String CMD_SELECT_RAW = "selectRawResult";


    public ResultMapMetaV2() {}



    public static ResultMapMetaV2 from(
            List<DslStatementV2> statements
    ) throws ClassNotFoundException {

        try {
            ResultMapMetaV2 meta = new ResultMapMetaV2();
            ParsedStatements parsed = ParsedStatements.from(statements);


            // 명시적 mapId / mapResult / selectRaw 처리



            parsed.applyExplicitMappings(meta);
            System.out.println("[ResultMapMeta DSL Statements] : " + statements);


            if (parsed.hasJoin()) {
                resolveWithJoin(parsed, meta);
            } else {
                resolveDefault(parsed,  meta);
            }

            return meta;

        }catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }

    // ──────────────────────────────────────────────
    // 구문 파싱 결과를 담는 내부 VO
    // ──────────────────────────────────────────────

    private static class ParsedStatements {
        final List<DslStatementV2> selectStmts = new ArrayList<>();
        DslStatementV2 fromStmt = null;
        boolean hasJoin = false;
        final List<DslStatementV2> explicitMappings = new ArrayList<>();
        final List<DslStatementV2> rawResultStmts = new ArrayList<>();

        static ParsedStatements from(List<DslStatementV2> statements) {
            ResultMapMetaV2.ParsedStatements p = new ResultMapMetaV2.ParsedStatements();


            for (DslStatementV2 stmt : statements) {
                String cmd = stmt.getCommand();

                if (cmd.contains(CMD_SELECT) && p.fromStmt == null) p.selectStmts.add(stmt);
                if (cmd.contains(CMD_FROM) && p.fromStmt == null)   p.fromStmt = stmt;
                if (cmd.contains(CMD_JOIN))                          p.hasJoin = true;
                if (CMD_MAP_ID.equals(cmd) || CMD_MAP_RESULT.equals(cmd)) p.explicitMappings.add(stmt);
                if (CMD_SELECT_RAW.equals(cmd))                      p.rawResultStmts.add(stmt);
            }

            return p;
        }


        boolean hasJoin()        { return hasJoin; }
        boolean isSingleFrom()   { return fromStmt != null && fromStmt.getArgs().size() == 1; }
        String  fromEntity()     { return


                fromStmt.getArgs()
                        .get(0)
                        .toString()
                        .replace(".class", "");
        }

        String  fromAlias()      { return fromStmt.getArgs().size() >= 2 ? fromStmt.getArgs().get(1).toString() : null; }

        void applyExplicitMappings(ResultMapMetaV2 meta) {



            for (DslStatementV2 stmt : explicitMappings) {
                List<String> args = DslStatementArgResolver.resolve(stmt);


                System.out.println("[applyExplicitMappings] : " + args);
                if (args.size() < 2) continue;
                if (CMD_MAP_ID.equals(stmt.getCommand())) {
                    System.out.println("[CMD MAP ID] arg 0 : " + args.get(0) + ", arg 1: " +args.get(1));
                    meta.idMappings.add(new ResultMapFactoryV2.FieldMapping(args.get(0), args.get(1)));
                }

                if (CMD_MAP_RESULT.equals(stmt.getCommand())) {
                    System.out.println("[CMD MAP RESULT] arg 0 : " + args.get(0) + ", arg 1: " +args.get(1));
                    meta.resultMappings.add(new ResultMapFactoryV2.FieldMapping(args.get(0), args.get(1)));
                }

            }


            for (DslStatementV2 stmt : rawResultStmts) {
                List<String> resolveStmt = DslStatementArgResolver.resolve(stmt);
                String sql = resolveStmt.get(0);
                for (String part : splitSelectRawSql(sql)) {
                    if (!part.toUpperCase().contains(" AS ")) continue;
                    String asName = part.split(" AS ")[1].trim();

                    System.out.println("[CMD MAP RESULT] arg 0 : " + asName);
                    meta.resultMappings.add(new ResultMapFactoryV2.FieldMapping(snakeToCamel(asName), asName));

                }
            }


        }
    }

    // ──────────────────────────────────────────────
    // Join 없는 단순 쿼리 매핑
    // ──────────────────────────────────────────────


    private static void resolveDefault(
            ParsedStatements parsed,
            ResultMapMetaV2 meta
    )  {

        if (parsed.fromStmt == null) return;

        String entity   = parsed.fromEntity();
        String pkField  = entityRelationRegistry.getPkFieldName(entity);
        String pkFieldType = entityRelationRegistry.getPkFieldType(entity);




        for (DslStatementV2 stmt : parsed.selectStmts) {
            List<String> args = DslStatementArgResolver.resolve(stmt);


            System.out.println(" [resolveDefault] args: " + args);



            for(String arg : args)
            {
                String[] resolveColumn = columResolver.resolve(arg);
                System.out.println(" [resolveDefault] resolveColumn: " + resolveColumn[0] + ", " + resolveColumn[1]);

                String className = resolveColumn[0];
                String fieldName = resolveColumn[1];

                EntityMeta entityMeta = repoMetaRegistry.getEntityMeta(className);
                String fieldType = entityMeta.getFieldType(fieldName);

                System.out.println(" [resolveDefault] field type: "+fieldType);
                System.out.println(" [resolveDefault] field column: " + entityMeta.getFieldToColumn().get(fieldName));



            }

            for (int i = 0; i < args.size(); i++) {
                String arg = args.get(i);

                if (!arg.contains("AS")) continue;

                System.out.println("=====================================");



                for (String expr : arg.split(",")) {
                    expr = expr.trim();
                    if (!expr.contains("AS")) continue;

                    String[] asParts  = expr.split(" AS ");
                    String   alias    = asParts[1].trim();

                    if (i + 1 >= args.size() || !args.get(i + 1).contains("::")) continue;

                    String[] refParts = args.get(i + 1).trim().split("::");
                    if (!refParts[0].equals(entity)) continue;


                    String fieldName = ColumnResolver.extractColumn(ColumnResolver.convertGetterToField(refParts[1]));


                    System.out.println(" [ResolveDefault] : pkField : " + pkField + ", fieldName : " + fieldName + ", alias : " + alias );

                    meta.addMapping(pkField, fieldName, alias);

                }
                i++;
            }
        }
    }

    // ──────────────────────────────────────────────
    // Join이 있는 쿼리 매핑
    // ──────────────────────────────────────────────


    private static void resolveWithJoin(
            ParsedStatements parsed,
            ResultMapMetaV2 meta
    )
    {

        if (parsed.fromStmt == null) return;

        String     entity    = parsed.fromEntity();
        String     pkField   = entityRelationRegistry.getPkFieldName(entity);
        String     pkFieldType = entityRelationRegistry.getPkFieldType(entity);
        EntityMeta entityMeta = repoMetaRegistry.getEntityMeta(entity);
        String     tableAlias = parsed.fromAlias();



        if (tableAlias == null) {
            resolveWithJoinNoAlias(parsed.selectStmts, entity, pkField, entityMeta, meta);
        } else {
            resolveWithJoinAlias(parsed.selectStmts, entity, pkField, entityMeta, tableAlias, meta);
        }


    }





    private static void resolveWithJoinNoAlias(
            List<DslStatementV2> selectStmts,
            String entity, String pkField,
            EntityMeta entityMeta,
            ResultMapMetaV2 meta
    )  {

        for (DslStatementV2 s : selectStmts) {
            List<String> resolveS = DslStatementArgResolver.resolve(s);
            for (String arg : resolveS) {

                String[] asParts   = arg.split(" AS ");
                String   cleaned   = asParts[0].trim();
                String   alias     = asParts.length > 1 ? asParts[1].trim() : null;
                String[] colonParts = cleaned.split("::");

                boolean isTargetEntity = colonParts[0].equals(entity) || colonParts[0].contains("%s");
                if (!isTargetEntity) continue;

                String fieldName = colonParts[0].contains("%s")
                        ? colonParts[0]
                        : ColumnResolver.extractColumn(ColumnResolver.convertGetterToField(colonParts[1]));

                if (alias == null) {
                    alias = entityMeta.getTableName() + "_" + entityMeta.getColumn(fieldName);
                }

                if (fieldName.contains("%s")) {
                    meta.resultMappings.add(new ResultMapFactoryV2.FieldMapping(alias, alias));
                } else {
                    meta.addMapping(pkField, fieldName, alias);
                }


            }
        }
    }


    private static void resolveWithJoinAlias(
            List<DslStatementV2> selectStmts,
            String entity, String pkField,
            EntityMeta entityMeta, String tableAlias,
            ResultMapMetaV2 meta
    ) {

        List<AbstractMap.SimpleEntry<String, String>> entries = new ArrayList<>();

        for (DslStatementV2 s : selectStmts) {
            Deque<String> pendingAliases = new ArrayDeque<>();

            List<String> resolveS = DslStatementArgResolver.resolve(s);
            for (String arg : resolveS) {
                for (String col : arg.split(",")) {
                    col = col.trim();
                    String[] asParts  = col.split("(?i)\\s+AS\\s+");
                    String   cleaned  = asParts[0].trim();
                    String   alias    = asParts.length > 1 ? asParts[1].trim() : null;

                    int    dotIdx    = cleaned.lastIndexOf(".");
                    String key       = dotIdx >= 0 ? cleaned.substring(dotIdx + 1) : cleaned;

                    if (key.contains("%s")) {
                        pendingAliases.add(alias);
                        continue;
                    }

                    String fieldName = key.contains("::")
                            ? ColumnResolver.extractColumn(ColumnResolver.convertGetterToField(key.split("::")[1]))
                            : ColumnResolver.extractColumn(key);

                    if (pendingAliases.isEmpty()) {
                        entries.add(new AbstractMap.SimpleEntry<>(fieldName, alias != null ? alias : ""));
                    } else {
                        String pending = pendingAliases.pop();
                        entries.add(new AbstractMap.SimpleEntry<>(pending, pending));
                    }

                }
            }
        }

        for (Map.Entry<String, String> entry : entries) {
            String fieldName = entry.getKey();
            String alias     = entry.getValue().isEmpty() ? null : entry.getValue();

            if (pkField.equals(fieldName)) {
                meta.idMappings.add(new ResultMapFactoryV2.FieldMapping(fieldName,
                        alias != null ? alias : tableAlias.replace(".", "_"))
                );

            } else {
                meta.resultMappings.add(new ResultMapFactoryV2.FieldMapping(fieldName,
                        alias != null ? alias : tableAlias + "_" + entityMeta.getColumn(fieldName))
                );
            }


        }
    }

    // ──────────────────────────────────────────────
    // 공통 헬퍼: pk인지 여부에 따라 idMappings / resultMappings 분기
    // ──────────────────────────────────────────────
    private void addMapping(String pkField, String fieldName, String alias)  {




        if (pkField.equals(fieldName)) {
            idMappings.add(new ResultMapFactoryV2.FieldMapping(fieldName, alias));
        }
        else {
            resultMappings.add(new ResultMapFactoryV2.FieldMapping(fieldName, alias));
        }


    }

    // ──────────────────────────────────────────────
    // 유틸
    // ──────────────────────────────────────────────
    private static List<String> splitSelectRawSql(String sql) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();
        for (char c : sql.toCharArray()) {
            if      (c == '(') depth++;
            else if (c == ')') depth--;
            else if (c == ',' && depth == 0) { parts.add(current.toString().trim()); current = new StringBuilder(); continue; }
            current.append(c);
        }
        if (current.length() > 0) parts.add(current.toString().trim());
        return parts;
    }

    private static String snakeToCamel(String str) {
        if (str == null) return null;
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (char c : str.toCharArray()) {
            if (c == '_') { nextUpper = true; }
            else if (nextUpper) { result.append(Character.toUpperCase(c)); nextUpper = false; }
            else { result.append(c); }
        }
        return result.toString();
    }

    // ──────────────────────────────────────────────
    // Getters & inner VO
    // ──────────────────────────────────────────────
    public List<ResultMapFactoryV2.FieldMapping> getIdMappings()              { return idMappings; }
    public List<ResultMapFactoryV2.FieldMapping> getResultMappings()          { return resultMappings; }
    public List<ResultMapFactoryV2.RelationMapping> getAssociationMappings()  { return associationMappings; }
    public List<ResultMapFactoryV2.RelationMapping> getCollectionMappings()   { return collectionMappings; }

    // ==========================================
    // 내부 VO 클래스들
    // ==========================================


}
