package io.jpm.core.jpm_repository.steps.write_dml_handler;

import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;
import io.jpm.core.jpm_repository.domain.model.ResultMapMeta;
import io.jpm.core.jpm_repository.generator.MybatisXmlGenerator;
import io.jpm.core.jpm_repository.steps.write_dml_handler.context.SqlMapBinderContext;
import io.jpm.core.jpm_repository.steps.write_dml_handler.composite.CompositeSqlMapperBinderStep;
import io.jpm.core.jpm_repository.steps.write_dml_handler.context.BuildMethodDataContext;
import io.jpm.core.jpm_repository.utils.ColumnResolver;

import java.util.ArrayList;
import java.util.List;

public class BuildMethodDataStep implements Step<BuildMethodDataContext> {

    private List<MybatisXmlGenerator.MethodData> result = new ArrayList<>();

    @Override
    public void execute(BuildMethodDataContext ctx) throws Exception {
        RepoMetaRegistry repoMetaRegistry = ctx.getJpmRepoContext().getCache().getRepoMetaRegistry();
        Step<SqlMapBinderContext> binder           = new CompositeSqlMapperBinderStep(
                repoMetaRegistry, new ColumnResolver(repoMetaRegistry)
        );

        result = new ArrayList<>();
        for (MethodMeta method : ctx.getRepoMeta().getMethods()) {
            LogPrinter.info("[BuildMethodDataStep] MethodName=" + method.getMethodName());

            EntityMeta entityMeta = repoMetaRegistry.getEntityMeta(method.getTargetType());

            SqlMapBinderContext sqlMapBinderContext = new SqlMapBinderContext(method, entityMeta);
            binder.execute(sqlMapBinderContext);

            String finalSql = sqlMapBinderContext.getFinalSql();

            result.add(new MybatisXmlGenerator.MethodData(method, ResultMapMeta.from(method), finalSql));
        }
    }

    public List<MybatisXmlGenerator.MethodData> getResult() {
        return result;
    }
}