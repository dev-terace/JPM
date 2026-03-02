package io.jpm.core;

import com.google.auto.service.AutoService;

import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.config.ast.GlobalRegistry;

import io.jpm.common.utils.LogPrinter;
import io.jpm.core.jpm_data_source_registry.JpmDataSourceRegistry;
import io.jpm.core.jpm_repository.processor.AstJpmRepositoryProcessorV2;
import io.jpm.core.m_entity.processor.MEntityAstProcessorV2;


import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;


import java.io.FileInputStream;
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

        GlobalRegistry globalRegistry = new GlobalRegistry(processingEnv);
        BuildTimeMetadataCache buildTimeMetadataCache = new BuildTimeMetadataCache();
        MEntityAstProcessorV2 mEntityProc = new MEntityAstProcessorV2();
        mEntityProc.init(processingEnv, roundEnv, buildTimeMetadataCache, globalRegistry);
        AstJpmRepositoryProcessorV2 aRepoProc = new AstJpmRepositoryProcessorV2();
        aRepoProc.init(processingEnv, roundEnv, buildTimeMetadataCache, globalRegistry);


        try {
            mEntityProc.execute();
            aRepoProc.execute();
        } catch (IOException e) {
            LogPrinter.exceptionInfo(e);
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }



  /*      try {
            // 1. Gradle이 주입한 옵션 가져오기 (파일 읽기 X, 오직 주입된 값만 신뢰)
            Map<String, String> options = processingEnv.getOptions();

            LogPrinter.init(processingEnv); //콘솔 찍기용

            // 2. Policy 파싱 (대소문자 무시 처리)
            String autoStr = options.getOrDefault("auto", "DISABLED").toUpperCase();
            AutoDDLPolicy policy;
            try {
                policy = AutoDDLPolicy.valueOf(autoStr);
            } catch (IllegalArgumentException e) {
                // 오타가 있거나 값이 이상하면 DISABLED 처리
                policy = AutoDDLPolicy.DISABLED;
                LogPrinter.warn("⚠️ [JPM] 알 수 없는 auto 모드입니다 ('" + autoStr + "'). DISABLED로 설정합니다.");
            }


            // 3. DISABLED 상태면 즉시 종료 (로그만 남김)
            if (policy == AutoDDLPolicy.DISABLED) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                        "💤 [JPM] DDL Generator is DISABLED. (Skipping execution)");
                return true;
            }


            // 4. 실행 정보 로그 출력
            String dbType = options.getOrDefault("dbType", "MYSQL").toUpperCase();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "🚀 [JPM] Start DDL Generation!222 (Policy: " + policy + ", DB: " + dbType + ")");


            LogPrinter.info("==============================");
            // 5. 컴포넌트 준비


            // DB 타입에 따른 방언 설정
            AppConfig.sqlDialectInit(options);



            // 6. Generator 생성 및 실행
            new MEntityAstProcessor(

                    processingEnv,
                    roundEnv,
                    options // 전체 옵션 전달 (url, username, password 포함됨)
            ).generate();


            new AstJpmRepositoryProcessor(roundEnv, processingEnv).generate();





        } catch (Exception e) {

            throw e;
        }
*/
        return true;
    }


}