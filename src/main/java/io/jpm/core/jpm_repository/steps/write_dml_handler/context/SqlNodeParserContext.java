package io.jpm.core.jpm_repository.steps.write_dml_handler.context;

import io.jpm.core.jpm_repository.domain.model.BuildContext;
import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.steps.write_dml_handler.core.sql_node_parser.node.SqlNode;

import java.util.ArrayList;
import java.util.List;

public class SqlNodeParserContext {

    private final List<DslStatement> statements;
    private final BuildContext       buildContext;
    private final EntityMeta         entityMeta;
    private final List<SqlNode>      nodes = new ArrayList<>();

    public SqlNodeParserContext(List<DslStatement> statements,
                                BuildContext buildContext,
                                EntityMeta entityMeta) {
        this.statements   = statements;
        this.buildContext = buildContext;
        this.entityMeta   = entityMeta;
    }

    public List<DslStatement> getStatements()  { return statements; }
    public BuildContext        getBuildContext() { return buildContext; }
    public EntityMeta          getEntityMeta()  { return entityMeta; }
    public List<SqlNode>       getNodes()       { return nodes; }
    public void                addNode(SqlNode node) { nodes.add(node); }
}