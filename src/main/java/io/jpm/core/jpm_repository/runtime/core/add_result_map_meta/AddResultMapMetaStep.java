package io.jpm.core.jpm_repository.runtime.core.add_result_map_meta;

import io.jpm.common.utils.CustomLogger;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.cache.interfaces.EntityRelationRegistry;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.domain.model.DslStatementV2;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.runtime.cache.ResultMappingMeta;
import io.jpm.core.jpm_repository.runtime.config.AppConfig;
import io.jpm.core.jpm_repository.runtime.context.AddResultMapMetaContext;
import io.jpm.core.jpm_repository.runtime.core.DslStatementArgResolver;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.ArgResolver;
import io.jpm.core.jpm_repository.utils.ColumnResolver;
import io.jpm.core.m_entity.parse.domain.enums.ResolveType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AddResultMapMetaStep implements Step<AddResultMapMetaContext> {
    private static final CustomLogger log = CustomLogger.getLogger(AddResultMapMetaStep.class);
    private static final RepoMetaRegistry repoMetaRegistry = AppConfig.getRepoMetaRegistry();
    private static final ArgResolver argResolver = new ArgResolver(repoMetaRegistry);
    private static final EntityRelationRegistry entityRelationRegistry = AppConfig.getEntityRelationRegistry();
    private final ColumnResolver columnResolver =  AppConfig.getColumnResolver();

    @Override
    public void execute(AddResultMapMetaContext context) throws Exception {

        List<DslStatementV2> statements = context.getStatements();
        BuildContext buildCtx = context.getBuildContext();


        String fromEntityName = "";
        String joinEntityName = "";

        //resultMapping에 사용 될 Select DSL 들을 추출
        List<DslStatementV2> extractSelects = new ArrayList<>();


        for (DslStatementV2 stmt : statements) {
            String cmd = stmt.getCommand();

            if(cmd.contains("select")){
                extractSelects.add(stmt);
            }

            if(cmd.equals("from"))
            {
                fromEntityName = stmt.getArgs().get(0).toString()
                        .replace("class ", "")
                        .trim()
                        .split("\\.")[1];


                break;
            }

        }

        for(DslStatementV2 stmt : statements){

            String cmd = stmt.getCommand();
            log.debug(cmd);
            if(cmd.contains("Join"))
            {
                joinEntityName = stmt.getArgs().get(0).toString()
                        .replace("class ", "")
                        .trim()
                        .split("\\.")[1];

                break;
            }
        }

        EntityMeta fromEntityMeta = repoMetaRegistry.getEntityMeta(fromEntityName);
        EntityMeta joinEntityMeta = repoMetaRegistry.getEntityMeta(joinEntityName);



        for(DslStatementV2 stmt : extractSelects)
        {


            List<String> resolveStmt = DslStatementArgResolver.resolve(stmt);
            List<String> args = argResolver.resolveAll(resolveStmt, fromEntityMeta, buildCtx);

            args = columnResolver.normalizeColumnName(args);

            parseColumnMappings(args, fromEntityMeta, fromEntityName, context);

            if(joinEntityMeta != null) {
                parseColumnMappings(args, joinEntityMeta, joinEntityName, context);
            }

            log.debug("resultMappings : {}", context.getResultMappings());
        }

    }


    /*private void parseColumnMappings(List<String> columns, EntityMeta targetEntityMeta, String targetEntityName, AddResultMapMetaContext context ) {
        Map<String, String> tableAliases = context.getTableAliases();
        List<ResultMappingMeta> resultMappingMetas = context.getResultMappings();

        Map<String, String> resolveColumns = columns.stream()
                .filter(c -> c.toLowerCase().contains("as"))
                .collect(Collectors.toMap(
                                c -> c.toLowerCase().split("as")[0].trim(), // key
                                c -> c.toLowerCase().split("as")[1].trim()  // value
                        )
                );


        for (Map.Entry<String, String> entry : resolveColumns.entrySet()) {
            String key = entry.getKey(); //ex: orders.order_id
            String value = entry.getValue(); //ex: AS orders_order_id

            String tableName = targetEntityMeta.getTableName();



            if (key.contains(".")) {
                String[] resolveKey = key.split("\\.");

                String entityName = resolveKey[0];
                String columnName = resolveKey[1];

                //alias o1, o2 proc 추가
                boolean isContainAlias = tableAliases.containsKey(entityName);
                EntityMeta entityMeta = repoMetaRegistry.getEntityMeta(entityName);


                if (isContainAlias && entityMeta == null) {
                    //entity name ex: o1
                    String aliasValue = tableAliases.get(entityName); // ex: key: o1, value: order_items
                    EntityMeta aliasesEntityMeta = repoMetaRegistry.getEntityMeta(aliasValue);
                    String fieldName = aliasesEntityMeta.getFieldName(columnName);
                    String fieldType = aliasesEntityMeta.getFieldType(fieldName);

                    ResolveType resolveType = ResolveType.valueOf(fieldType);
                    Class<?> javaType = resolveType.getJavaType();

                    String aliasEntityName = tableAliases.entrySet()
                            .stream()
                            .filter(e -> entityName.equals(e.getValue()))
                            .filter(e -> !aliasValue.equals(e.getKey()))
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElse(null);

                    String pkFieldName = entityRelationRegistry.getPkFieldName(aliasEntityName);

                    log.debug("[SelectNodeV2] aliasEntityName: {}, pkFieldName: {}", aliasEntityName, pkFieldName);

                    if (pkFieldName == null || !pkFieldName.equals(fieldName)) {
                        continue;
                    }

                    resultMappingMetas.add(new ResultMappingMeta(fieldName, value, javaType, true, entityName));

                }

                if (!tableName.equals(entityName) || entityMeta == null) {
                    continue;
                }


                String fieldName = entityMeta.getFieldName(columnName);
                String fieldType = entityMeta.getFieldType(fieldName);

                ResolveType resolveType = ResolveType.valueOf(fieldType);

                Class<?> javaType = resolveType.getJavaType();

                String pkFieldName = entityRelationRegistry.getPkFieldName(targetEntityName);


                if (pkFieldName == null || !pkFieldName.equals(fieldName)) {
                    continue;
                }

                resultMappingMetas.add(new ResultMappingMeta(fieldName, value, javaType, true, entityName));
            }

        }

        if(resolveColumns.isEmpty()){

            for(String columnName : columns){
                String pkFieldName = entityRelationRegistry.getPkFieldName(targetEntityName);

                String pkColumnName = targetEntityMeta.getColumn(pkFieldName);
                String pkFieldType = entityRelationRegistry.getPkFieldType(targetEntityName);

                ResolveType resolveType = ResolveType.valueOf(pkFieldType);
                Class<?> javaType = resolveType.getJavaType();

                if(columnName.equals(pkColumnName)){
                    resultMappingMetas.add(new ResultMappingMeta(pkFieldName, pkColumnName, javaType, true, null));

                }

            }

        }
    }*/



    private void parseColumnMappings(List<String> columns, EntityMeta targetEntityMeta,
                                     String targetEntityName, AddResultMapMetaContext context) {
        Map<String, String> tableAliases = context.getTableAliases();
        List<ResultMappingMeta> resultMappingMetas = context.getResultMappings();

        Map<String, String> resolvedColumns = extractAliasedColumns(columns);

        if (resolvedColumns.isEmpty()) {
            //기본 pk 처리
            parsePlainPkColumns(columns, targetEntityMeta, targetEntityName, resultMappingMetas);


        } else {
            //AS 처리
            parseAliasedColumns(resolvedColumns, targetEntityMeta, targetEntityName, tableAliases, resultMappingMetas);
        }
    }

    private Map<String, String> extractAliasedColumns(List<String> columns) {
        return columns.stream()
                .filter(c -> c.toLowerCase().contains(" as "))
                .collect(Collectors.toMap(
                        c -> c.toLowerCase().split("\\s+as\\s+")[0].trim(),
                        c -> c.toLowerCase().split("\\s+as\\s+")[1].trim()
                ));
    }


    private void parsePlainPkColumns(List<String> columns, EntityMeta targetEntityMeta,
                                     String targetEntityName, List<ResultMappingMeta> resultMappingMetas) {
        String pkFieldName  = entityRelationRegistry.getPkFieldName(targetEntityName);
        String pkColumnName = targetEntityMeta.getColumn(pkFieldName);
        Class<?> javaType   = resolvePkJavaType(targetEntityName);

        columns.stream()
                .filter(col -> col.equals(pkColumnName))
                .findFirst()
                .ifPresent(col ->
                        resultMappingMetas.add(new ResultMappingMeta(pkFieldName, pkColumnName, javaType, true, null))
                );
    }

    private void parseAliasedColumns(Map<String, String> resolvedColumns, EntityMeta targetEntityMeta,
                                     String targetEntityName, Map<String, String> tableAliases,
                                     List<ResultMappingMeta> resultMappingMetas) {

        for (Map.Entry<String, String> entry : resolvedColumns.entrySet()) {
            String rawKey    = entry.getKey();   // ex: "orders.order_id"  또는  "o1.order_id"
            String aliasName = entry.getValue(); // ex: "orders_order_id"

            if (!rawKey.contains(".")) continue;

            String[] parts      = rawKey.split("\\.");
            String entityToken  = parts[0]; // 테이블명 또는 별칭
            String columnName   = parts[1];

            ResultMappingMeta mapping = resolveMapping(
                    entityToken, columnName, aliasName,
                    targetEntityMeta, targetEntityName, tableAliases
            );

            if (mapping != null) {
                resultMappingMetas.add(mapping);
            }
        }
    }

    /**
     * entityToken 이 실제 테이블명인지 별칭인지 판단해 ResultMappingMeta 생성.
     * PK 가 아닌 경우 null 반환 → 호출부에서 skip.
     */


    private ResultMappingMeta resolveMapping(String entityToken, String columnName, String aliasName,
                                             EntityMeta targetEntityMeta, String targetEntityName,
                                             Map<String, String> tableAliases) {

        boolean isAlias    = tableAliases.containsKey(entityToken);
        EntityMeta entityMeta = repoMetaRegistry.getEntityMeta(entityToken);

        // ── 케이스 1: 별칭(o1, o2 등)으로 참조된 컬럼 ──
        if (isAlias && entityMeta == null) {
            return resolveMappingByAlias(entityToken, columnName, aliasName, tableAliases);
        }

        // ── 케이스 2: 실제 테이블명으로 참조된 컬럼 ──
        boolean isSameTable = targetEntityMeta.getTableName().equals(entityToken);
        if (!isSameTable || entityMeta == null) return null;

        String fieldName = entityMeta.getFieldName(columnName);
        String pkFieldName = entityRelationRegistry.getPkFieldName(targetEntityName);

        if (!fieldName.equals(pkFieldName)) return null;

        Class<?> javaType = resolveJavaType(entityMeta, fieldName);
        return new ResultMappingMeta(fieldName, aliasName, javaType, true, entityToken);
    }


    private ResultMappingMeta resolveMappingByAlias(String aliasToken, String columnName,
                                                    String aliasName, Map<String, String> tableAliases) {

        String realTableName      = tableAliases.get(aliasToken);
        EntityMeta aliasEntityMeta = repoMetaRegistry.getEntityMeta(realTableName);
        if (aliasEntityMeta == null) return null;

        String fieldName = aliasEntityMeta.getFieldName(columnName);

        // 별칭에 대응하는 실제 엔티티명(역방향 조회)
        String aliasEntityName = tableAliases.entrySet().stream()
                .filter(e -> aliasToken.equals(e.getValue()) && !realTableName.equals(e.getKey()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        String pkFieldName = entityRelationRegistry.getPkFieldName(aliasEntityName);
        log.debug("[SelectNodeV2] aliasEntityName: {}, pkFieldName: {}", aliasEntityName, pkFieldName);

        if (pkFieldName == null || !pkFieldName.equals(fieldName)) return null;

        Class<?> javaType = resolveJavaType(aliasEntityMeta, fieldName);
        return new ResultMappingMeta(fieldName, aliasName, javaType, true, aliasToken);
    }


    private Class<?> resolveJavaType(EntityMeta entityMeta, String fieldName) {
        String fieldType = entityMeta.getFieldType(fieldName);
        return ResolveType.valueOf(fieldType).getJavaType();
    }

    private Class<?> resolvePkJavaType(String entityName) {
        String pkFieldType = entityRelationRegistry.getPkFieldType(entityName);
        return ResolveType.valueOf(pkFieldType).getJavaType();
    }



}
