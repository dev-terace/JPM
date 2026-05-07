package io.jpm.core.jpm_repository.domain.model.result_map_meta;

import io.jpm.common.utils.LogPrinter;
import io.jpm.core.jpm_repository.domain.cache.interfaces.EntityRelationRegistry;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;
import io.jpm.core.jpm_repository.domain.model.RepoMeta;
import io.jpm.core.jpm_repository.utils.ColumnResolver;

import javax.annotation.processing.Filer;
import java.util.*;


/**
 * 쿼리 결과를 Java 객체로 변환하기 위한 매핑 메타데이터
 * (스스로 MethodMeta를 읽어서 매핑 정보를 추출합니다)
 */
public class ResultMapMeta {

    private final List<ResultMapFactory.FieldMapping> idMappings = new ArrayList<>();
    private final List<ResultMapFactory.FieldMapping> resultMappings = new ArrayList<>();
    private final List<ResultMapFactory.RelationMapping> associationMappings = new ArrayList<>();
    private final List<ResultMapFactory.RelationMapping> collectionMappings = new ArrayList<>();




    private static final String CMD_SELECT = "select";
    private static final String CMD_FROM = "from";
    private static final String CMD_JOIN = "Join";
    private static final String CMD_MAP_ID = "mapId";
    private static final String CMD_MAP_RESULT = "mapResult";
    private static final String CMD_SELECT_RAW = "selectRawResult";


    public ResultMapMeta() {}


    public static ResultMapMeta from(
            MethodMeta methodMeta,
            RepoMetaRegistry repoMetaRegistry,
            EntityRelationRegistry entityRelationRegistry,
            RepoMeta repoMeta,
            Filer filer
            ) throws ClassNotFoundException {

        try {
            ResultMapMeta meta = new ResultMapMeta();
            ParsedStatements parsed = ParsedStatements.from(methodMeta.getStatements());

            // 명시적 mapId / mapResult / selectRaw 처리
            parsed.applyExplicitMappings(meta);


            // 자동 alias 매핑
            ResultMapFactory.ProjectionSpec projectionSpec = new ResultMapFactory.ProjectionSpec(methodMeta.getMethodName(),
                    repoMeta.getQualifiedClassName()
                    , new HashSet<>()
                    , new ArrayList<>());


            if (parsed.hasJoin()) {
                resolveWithJoin(parsed, repoMetaRegistry, entityRelationRegistry, meta, projectionSpec, filer);
            } else {
                resolveDefault(parsed, entityRelationRegistry, meta, projectionSpec, filer);
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
        final List<DslStatement> selectStmts = new ArrayList<>();
        DslStatement fromStmt = null;
        boolean hasJoin = false;
        final List<DslStatement> explicitMappings = new ArrayList<>();
        final List<DslStatement> rawResultStmts = new ArrayList<>();

        static ParsedStatements from(List<DslStatement> statements) {
            ParsedStatements p = new ParsedStatements();
            for (DslStatement stmt : statements) {
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
        String  fromEntity()     { return fromStmt.getArgs().get(0).replace(".class", ""); }
        String  fromAlias()      { return fromStmt.getArgs().size() >= 2 ? fromStmt.getArgs().get(1) : null; }

        void applyExplicitMappings(ResultMapMeta meta) {





            for (DslStatement stmt : explicitMappings) {
                List<String> args = stmt.getArgs();
                if (args.size() < 2) continue;
                if (CMD_MAP_ID.equals(stmt.getCommand()))     meta.idMappings.add(new ResultMapFactory.FieldMapping(args.get(0), args.get(1)));

                if (CMD_MAP_RESULT.equals(stmt.getCommand())) meta.resultMappings.add(new ResultMapFactory.FieldMapping(args.get(0), args.get(1)));
            }


            for (DslStatement stmt : rawResultStmts) {
                String sql = stmt.getArgs().get(0);
                for (String part : splitSelectRawSql(sql)) {
                    if (!part.toUpperCase().contains(" AS ")) continue;
                    String asName = part.split(" AS ")[1].trim();
                    meta.resultMappings.add(new ResultMapFactory.FieldMapping(snakeToCamel(asName), asName));
                }
            }


        }
    }

    // ──────────────────────────────────────────────
    // Join 없는 단순 쿼리 매핑
    // ──────────────────────────────────────────────
    private static void resolveDefault(
            ParsedStatements parsed,
            EntityRelationRegistry entityRelationRegistry,
            ResultMapMeta meta,
            ResultMapFactory.ProjectionSpec projectionSpec,
            Filer filer
            ) throws ClassNotFoundException {

        if (parsed.fromStmt == null) return;

        String entity   = parsed.fromEntity();
        String pkField  = entityRelationRegistry.getPkFieldName(entity);

        String pkFieldType = entityRelationRegistry.getPkFieldType(entity);

        for (DslStatement stmt : parsed.selectStmts) {
            List<String> args = stmt.getArgs();
            for (int i = 0; i < args.size(); i++) {
                String arg = args.get(i);
                if (!arg.contains("AS")) continue;

                for (String expr : arg.split(",")) {
                    expr = expr.trim();
                    if (!expr.contains("AS")) continue;

                    String[] asParts  = expr.split(" AS ");
                    String   alias    = asParts[1].trim();

                    if (i + 1 >= args.size() || !args.get(i + 1).contains("::")) continue;

                    String[] refParts = args.get(i + 1).trim().split("::");
                    if (!refParts[0].equals(entity)) continue;

                    String fieldName = ColumnResolver.extractColumn(ColumnResolver.convertGetterToField(refParts[1]));

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
            RepoMetaRegistry repoMetaRegistry,
            EntityRelationRegistry entityRelationRegistry,
            ResultMapMeta meta,
            ResultMapFactory.ProjectionSpec projectionSpec,
            Filer filer
            ) throws ClassNotFoundException {

        if (parsed.fromStmt == null) return;

        String     entity    = parsed.fromEntity();
        String     pkField   = entityRelationRegistry.getPkFieldName(entity);
        String     pkFieldType = entityRelationRegistry.getPkFieldType(entity);
        EntityMeta entityMeta = repoMetaRegistry.getEntityMeta(entity);
        String     tableAlias = parsed.fromAlias();


        if (tableAlias == null) {
            resolveWithJoinNoAlias(parsed.selectStmts, entity, pkField, entityMeta, meta, projectionSpec, filer, pkFieldType);
        } else {
            resolveWithJoinAlias(parsed.selectStmts, entity, pkField, entityMeta, tableAlias, meta, projectionSpec, filer, pkFieldType);
        }


    }


    private static void resolveWithJoinNoAlias(
            List<DslStatement> selectStmts,
            String entity, String pkField,
            EntityMeta entityMeta, ResultMapMeta meta,
            ResultMapFactory.ProjectionSpec projectionSpec,
            Filer filer,
            String pkFieldType
            ) throws ClassNotFoundException {

        for (DslStatement s : selectStmts) {
            for (String arg : s.getArgs()) {

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
                    meta.resultMappings.add(new ResultMapFactory.FieldMapping(alias, alias));
                } else {
                    meta.addMapping(pkField, fieldName, alias);
                }

                projectionSpec.getFields().add(new ResultMapFactory.ResultMapProjectionSpec(fieldName, pkFieldType, filer));

            }
        }
    }

    private static void resolveWithJoinAlias(
            List<DslStatement> selectStmts,
            String entity, String pkField,
            EntityMeta entityMeta, String tableAlias,
            ResultMapMeta meta,
            ResultMapFactory.ProjectionSpec projectionSpec,
            Filer filer,
            String pkFieldType
            ) throws ClassNotFoundException {

        List<AbstractMap.SimpleEntry<String, String>> entries = new ArrayList<>();

        for (DslStatement s : selectStmts) {
            Deque<String> pendingAliases = new ArrayDeque<>();

            for (String arg : s.getArgs()) {
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
                meta.idMappings.add(new ResultMapFactory.FieldMapping(fieldName,
                        alias != null ? alias : tableAlias.replace(".", "_"))
                );

            } else {
                meta.resultMappings.add(new ResultMapFactory.FieldMapping(fieldName,
                        alias != null ? alias : tableAlias + "_" + entityMeta.getColumn(fieldName))
                );
            }

         /*   projectionSpec.getFields().add(new ResultMapFactoryV2.ResultMapProjectionSpec(fieldName, pkFieldType, filer));*/

        }
    }

    // ──────────────────────────────────────────────
    // 공통 헬퍼: pk인지 여부에 따라 idMappings / resultMappings 분기
    // ──────────────────────────────────────────────
    private void addMapping(String pkField, String fieldName, String alias)  {




        if (pkField.equals(fieldName)) {
            idMappings.add(new ResultMapFactory.FieldMapping(fieldName, alias));
        }
        else {
            resultMappings.add(new ResultMapFactory.FieldMapping(fieldName, alias));
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
    public List<ResultMapFactory.FieldMapping> getIdMappings()              { return idMappings; }
    public List<ResultMapFactory.FieldMapping> getResultMappings()          { return resultMappings; }
    public List<ResultMapFactory.RelationMapping> getAssociationMappings()  { return associationMappings; }
    public List<ResultMapFactory.RelationMapping> getCollectionMappings()   { return collectionMappings; }

    // ==========================================
    // 내부 VO 클래스들
    // ==========================================


}