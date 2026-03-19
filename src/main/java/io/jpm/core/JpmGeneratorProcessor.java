package io.jpm.core;

import com.google.auto.service.AutoService;

import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.exception.ErrorTrackerImpl;
import io.jpm.config.AppConfig;
import io.jpm.config.ast.AstContext;
import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.config.ast.GlobalRegistry;

import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.ImmutableGlobalRegistry;
import io.jpm.core.jpm_data_source_registry.JpmDataSourceRegistry;
import io.jpm.core.jpm_repository.domain.cache.MapParamRegistryImpl;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.DSLKeywords;
import io.jpm.core.jpm_repository.handler.find_repo_meta_handler.FindRepoMetaValidProc;
import io.jpm.core.jpm_repository.pipeline.JpmRepositoryPipeline;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.ArgumentTokenExtractor;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.ArgumentTokenExtractorValueResolver;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.DslCommandProcessor;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.SegmentInliner;
import io.jpm.core.jpm_repository.utils.ColumnResolver;
import io.jpm.core.jpm_repository.valid.policy.JoinNodeValidatorPolicyV2;
import io.jpm.core.m_entity.processor.MEntityPipelineV2;


import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@AutoService(Processor.class)
@SupportedOptions({ "url", "username", "password", "dbType", "auto", "projectDir" })
@SupportedAnnotationTypes({ "io.jpm.api.*" })
public class JpmGeneratorProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        // 1. LogPrinter에 Messager 전달 (이제부터 LogPrinter 사용 가능)
        LogPrinter.init(processingEnv);

        LogPrinter.info("🚀 [JPM] Annotation Processor Initializing...");

        // 2. build.gradle에서 넘겨준 projectDir 옵션 읽기
        Map<String, String> options = processingEnv.getOptions();
        String projectDir = options.get("projectDir");

        if (projectDir == null) {
            LogPrinter.error("❌ [JPM] 'projectDir' option is missing. Please add it to build.gradle.");
            return;
        }

        // 3. 실제 프로젝트 경로에서 설정 파일 로드
        Path configPath = Paths.get(projectDir, "src", "main", "resources", "application.properties");
        LogPrinter.info("📂 [JPM] Configuration Path: " + configPath.toAbsolutePath());

        if (Files.exists(configPath)) {
            Properties props = new Properties();
            try (InputStream is = Files.newInputStream(configPath)) {
                props.load(is);
                // 4. 레지스트리에 데이터 로드
                JpmDataSourceRegistry.load(props);
            } catch (IOException e) {
                LogPrinter.error("❌ [JPM] Failed to load properties: " + e.getMessage());
            }
        } else {
            LogPrinter.error("⚠️ [JPM] application.properties not found at " + configPath);
        }
    }


/*    private Path findApplicationProperties(ProcessingEnvironment env) {
        // Gradle 환경에서는 프로젝트 루트를 직접 지정하는 옵션을 build.gradle에서 넘겨주는게 가장 깔끔하지만,
        // 우선은 추측 로직을 사용합니다.
        String userDir = System.getProperty("user.dir"); // 현재는 .gradle/workers

        // 보통 프로젝트 루트는 .gradle 폴더의 부모의 부모... 어딘가에 있습니다.
        // 하지만 가장 확실한 건 build.gradle에 아래 설정을 추가하는 것입니다.
        return Paths.get("src/main/resources/application.properties");
    }*/




    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 어노테이션이 없으면 처리 안 함
        if (annotations.isEmpty()) {
            return false;
        }


        BuildTimeMetadataCache buildTimeMetadataCache = new BuildTimeMetadataCache();
        GlobalRegistry globalRegistry = null;
        try {
            globalRegistry = getGlobalRegistry(buildTimeMetadataCache);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        AppConfig.sqlDialectInit(processingEnv.getOptions());

        MEntityPipelineV2 mEntityProc = new MEntityPipelineV2();
        mEntityProc.init(processingEnv, roundEnv, buildTimeMetadataCache, globalRegistry);
        JpmRepositoryPipeline jRepoProc = new JpmRepositoryPipeline();
        jRepoProc.init(processingEnv, roundEnv, buildTimeMetadataCache, globalRegistry);






        try {
            mEntityProc.execute();
            jRepoProc.execute();

        } catch (Exception e) {

            throw new RuntimeException(e);
        }

        return true;
    }


    private GlobalRegistry getGlobalRegistry(BuildTimeMetadataCache cache) throws Exception {


        try {
            RepoMetaRegistry repoMetaRegistry = cache.getRepoMetaRegistry();

            ErrorTracker errorTracker = new ErrorTrackerImpl(cache.getSourceLocationCache());
            ArgumentTokenExtractor argumentTokenExtractor = new ArgumentTokenExtractor(new ArgumentTokenExtractorValueResolver(repoMetaRegistry), cache, errorTracker);
            DslCommandProcessor commandProcV2 = new DslCommandProcessor(cache, errorTracker, argumentTokenExtractor);


            Map<String, String> safeOptions = processingEnv.getOptions().entrySet().stream()
                    .filter(e -> e.getValue() != null)
                    .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


            return ImmutableGlobalRegistry.builder()
                    .options(safeOptions)
                    .tokenExtractor(argumentTokenExtractor)
                    .commandProcessor(commandProcV2)
                    .mapParamRegistry(new MapParamRegistryImpl())
                    .findRepoMetaValidProc(new FindRepoMetaValidProc(cache, errorTracker, new ColumnResolver(cache.getRepoMetaRegistry())))
                    .segmentInliner(new SegmentInliner(argumentTokenExtractor, commandProcV2
                                    ,new AstContext(processingEnv), DSLKeywords.getDSLKeywords(), errorTracker,
                                    new JoinNodeValidatorPolicyV2(cache, errorTracker, new ColumnResolver(repoMetaRegistry))
                    ))
                    .errorTracker(errorTracker)
                    .build();

        } catch (Exception e) {
            System.err.println("=== ERROR: " + e.getClass().getName() + ": " + e.getMessage());
            for (StackTraceElement ste : e.getStackTrace()) {
                System.err.println("  at " + ste);
            }
            throw new RuntimeException(e);

        }
    }


}