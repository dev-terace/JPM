package io.jpm.core.jpm_repository.processor.handler;

import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.AstContext;
import io.jpm.config.ast.AstHandler;
import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.config.ast.GlobalRegistry;
import io.jpm.core.jpm_repository.generator.infra.mapper.MybatisXmlGenerator;
import io.jpm.core.jpm_repository.generator.infra.utils.ColumnResolver;
import io.jpm.core.jpm_repository.parse.domain.cache.RepoMetaRegistry;
import io.jpm.core.jpm_repository.parse.domain.policy.sql_mapper_binder.SqlMapperBinder;
import io.jpm.core.jpm_repository.parse.domain.policy.sql_mapper_binder.SqlMapperBinderImplV2;
import io.jpm.core.jpm_repository.parse.domain.vo.EntityMeta;
import io.jpm.core.jpm_repository.parse.domain.vo.MethodMeta;
import io.jpm.core.jpm_repository.parse.domain.vo.RepoMeta;
import io.jpm.core.jpm_repository.parse.domain.vo.ResultMapMeta;
import io.jpm.core.jpm_repository.processor.handler.handlerContext.AstJpmRepoContext;

import javax.annotation.processing.RoundEnvironment;
import java.util.ArrayList;
import java.util.List;

public class WriteAndExecuteDMLHandler extends AstHandler<AstJpmRepoContext> {

    private final RepoMetaRegistry repoMetaRegistry;
    private final SqlMapperBinder binder;
    private final ErrorTracker errorTracker;

    public WriteAndExecuteDMLHandler(BuildTimeMetadataCache cache, GlobalRegistry globalRegistry, AstContext astContext) {
        super(cache, globalRegistry, astContext);
        this.repoMetaRegistry = cache.getRepoMetaRegistry();
        ColumnResolver columnResolver = new ColumnResolver(repoMetaRegistry);
        this.binder = new SqlMapperBinderImplV2(repoMetaRegistry, columnResolver);
        this.errorTracker = globalRegistry.errorTracker();
    }


    @Override
    public void handle(RoundEnvironment roundEnv) throws Exception {
        try {

            List<RepoMeta> repoMetas = handlerContext.getRepoMetas();



            for (RepoMeta repoMeta : repoMetas) {

                List<MybatisXmlGenerator.MethodData> methodDataList = new ArrayList<>();

                for (MethodMeta method : repoMeta.getMethods()) {
                    LogPrinter.info("[WriteAndExecuteDMLHandler] MethodName=" + method.getMethodName());
                    EntityMeta entityMeta = repoMetaRegistry.getEntityMeta(method.getTargetType());
                    LogPrinter.info("generateSql: " + method.getTargetType());
                    String finalSql = String.valueOf(binder.generateSql(method, entityMeta));

                    ResultMapMeta mappingMeta = ResultMapMeta.from(method);
                    // 2. XML 생성을 위해 리스트에 데이터 적재 (아직 XML 생성 안 함)
                    methodDataList.add(new MybatisXmlGenerator.MethodData(method, mappingMeta, finalSql));
                }


                String errorMessage = errorTracker.reportChain();

                if(errorMessage != null) {throw  new IllegalArgumentException(errorMessage);}

                if (!methodDataList.isEmpty()) {
                    MybatisXmlGenerator mybatisXmlGenerator = new MybatisXmlGenerator(repoMetaRegistry);

                    String resultXml = mybatisXmlGenerator.generateXml(repoMeta.getNamespace(), methodDataList);

                    LogPrinter.info("\n[완성된 MyBatis XML]");
                    LogPrinter.info(resultXml);
                }


            }



        } catch (Exception e) {
            for (StackTraceElement ste : e.getStackTrace()) {
                LogPrinter.info("[STACK] " + ste.toString());
            }
            throw new IllegalArgumentException(e);
        }

    }

    @Override
    public void setHandlerContext(AstJpmRepoContext handlerContext) {
        this.handlerContext = handlerContext;
    }
}
