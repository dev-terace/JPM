package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.composite;

import io.jpm.common.exception.CustomProcessorException;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.CompositeStep;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;

import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context.MethodParseContext;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.context.SqlMapBinderContext;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.alias_pre_scanner.AliasPreScannerSteps;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.utils.SqlAssemblerUtil;

import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.ArgResolver;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.SqlNodeParserSteps;
import io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.node.SqlNode;
import io.jpm.core.jpm_repository.utils.ColumnResolver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CompositeSqlMapperBinderStep implements CompositeStep<SqlMapBinderContext>{


    private final List<Step<SqlMapBinderContext>> steps;

    public CompositeSqlMapperBinderStep(RepoMetaRegistry repoMetaRegistry, ColumnResolver columnResolver) {

        ArgResolver argResolver  = new ArgResolver(repoMetaRegistry);

        steps = Collections.singletonList(
                new SqlNodeParserSteps(repoMetaRegistry, argResolver, columnResolver)
        );


    }


    @Override
    public List<Step<SqlMapBinderContext>> getSteps() {
        return steps;
    }




}
