package io.jpm.core.jpm_repository.runtime.core.node;


import io.jpm.core.jpm_repository.domain.cache.interfaces.EntityRelationRegistry;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.runtime.cache.ResultMappingMeta;
import io.jpm.core.jpm_repository.runtime.config.AppConfig;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.node.SqlNode;
import io.jpm.core.jpm_repository.utils.ColumnResolver;

import io.jpm.core.m_entity.parse.domain.enums.ResolveType;



import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class SelectNodeV2 implements SqlNode {
    private final List<String> columns;

    private final ColumnResolver columnResolver = AppConfig.getColumnResolver();

    private final RepoMetaRegistry repoMetaRegistry = AppConfig.getRepoMetaRegistry();

    private final EntityRelationRegistry entityRelationRegistry = AppConfig.getEntityRelationRegistry();

    private final List<ResultMappingMeta> resultMappingMetas;
    private final String entityName;
    private final EntityMeta entityMeta;
    private final Map<String, String> tableAliases;

    public SelectNodeV2(List<String> columns, EntityMeta entityMeta, List<ResultMappingMeta> resultMappingMetas, Map<String, String> tableAliases, String entityName) {
        this.columns = columns;
        this.resultMappingMetas = resultMappingMetas;
        this.entityMeta = entityMeta;
        this.entityName = entityName;
        this.tableAliases = tableAliases;
        System.out.println("[SelectNodeV2] tableAliases: " + tableAliases);
    }


    public void parseColumnMappings(List<String> columns)
    {

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

            String tableName = entityMeta.getTableName();

            if(key.contains("."))
            {
                String[] resolveKey = key.split("\\.");

                String entityName = resolveKey[0];
                String columnName = resolveKey[1];

                //alias o1, o2 proc 추가
                boolean isContainAlias = tableAliases.containsKey(entityName);
                EntityMeta entityMeta = repoMetaRegistry.getEntityMeta(entityName);


                if(isContainAlias && entityMeta == null)
                {
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
                            .filter(e-> !aliasValue.equals(e.getKey()))
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElse(null);

                    String pkFieldName = entityRelationRegistry.getPkFieldName(aliasEntityName);

                    System.out.println("[SelectNodeV2] pkFieldName: " + aliasEntityName + ", " +pkFieldName);

                    if( pkFieldName == null || !pkFieldName.equals(fieldName)) {continue;}

                    resultMappingMetas.add(new ResultMappingMeta(fieldName, value, javaType, true, entityName));

                }

                if(!tableName.equals(entityName) || entityMeta == null) {continue;}



                String fieldName = entityMeta.getFieldName(columnName);
                String fieldType = entityMeta.getFieldType(fieldName);

                ResolveType resolveType = ResolveType.valueOf(fieldType);

                Class<?> javaType = resolveType.getJavaType();

                String pkFieldName = entityRelationRegistry.getPkFieldName(this.entityName);

                if( pkFieldName == null || !pkFieldName.equals(fieldName)) {continue;}

                resultMappingMetas.add(new ResultMappingMeta(fieldName, value, javaType, true, entityName));




            }

        }

        System.out.println("[parseColumnMappings] resultMappingMetas " + resultMappingMetas);

        System.out.println("[parseColumnMappings] resolveColumns: "+ resolveColumns);


    }

    @Override
    public void apply(BuildContext ctx) {
        List<String> resolved = columns;

/*        for (String col : columns) {
            resolved.add(columnResolver.resolve(col, ctx));
        }*/

        resolved = columnResolver.normalizeColumnName(resolved);

        System.out.println("[SelectNode] resolved: " + resolved);

        parseColumnMappings(resolved);


        ctx.setAction("SELECT");
        // 덮어쓰기 대신 누적
        if (ctx.getColumns().isEmpty()) {
            ctx.setColumns(String.join(", ", resolved));
        } else {
            ctx.setColumns(ctx.getColumns() + ", " + String.join(", ", resolved));
        }

    }





    @Override public String toSql(BuildContext ctx) { return ""; }
}