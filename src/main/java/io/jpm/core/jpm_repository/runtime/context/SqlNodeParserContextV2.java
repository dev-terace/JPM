package io.jpm.core.jpm_repository.runtime.context;

import io.jpm.config.ast.Context;
import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.domain.model.DslStatementV2;

import io.jpm.core.jpm_repository.runtime.cache.ResultMappingMeta;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.node.SqlNode;

import java.util.ArrayList;
import java.util.List;

public class SqlNodeParserContextV2 implements Context {

    private final List<DslStatementV2> statements;
    private final BuildContext       buildContext;
    private final List<SqlNode>      nodes = new ArrayList<>();
    private List<ResultMappingMeta> resultMappingMeta = new ArrayList<>();




    public SqlNodeParserContextV2(List<DslStatementV2> statements,
                                BuildContext buildContext) {
        this.statements   = statements;
        this.buildContext = buildContext;
    }

    public List<DslStatementV2> getStatements()  { return statements; }
    public BuildContext        getBuildContext() { return buildContext; }
    public List<SqlNode>       getNodes()       { return nodes; }
    public void                addNode(SqlNode node) { nodes.add(node); }
    public void addResultMappingMeta(ResultMappingMeta resultMappingMeta) { this.resultMappingMeta.add(resultMappingMeta); }
    public List<ResultMappingMeta> getResultMappingMeta() { return resultMappingMeta; }

}