package io.jpm.core.jpm_repository.parse.domain.policy.sql_node_parser.node;

import io.jpm.core.jpm_repository.parse.domain.cache.RepoMetaRegistry;
import io.jpm.core.jpm_repository.parse.domain.vo.BuildContext;
import io.jpm.core.jpm_repository.generator.infra.utils.ColumnResolver;

import java.util.List;
import java.util.stream.Collectors;

public class GroupByNode implements SqlNode {
    private final List<String> columns;
    private final RepoMetaRegistry repoMetaRegistry;
    private final ColumnResolver columnResolver;

    public GroupByNode(List<String> columns, RepoMetaRegistry repoMetaRegistry, ColumnResolver columnResolver) { this.columns = columns;
        this.repoMetaRegistry = repoMetaRegistry;

        this.columnResolver = columnResolver;
    }

    @Override
    public void apply(BuildContext ctx) {


        List<String> resolveColumns = columns.stream()
                .map(s -> columnResolver.resolve(s, ctx))
                .collect(Collectors.toList());

        ctx.getGroupBys().add(String.join(", ", resolveColumns));


    }
    @Override public String toSql(BuildContext ctx) { return ""; }
}
