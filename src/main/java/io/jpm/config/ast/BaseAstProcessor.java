package io.jpm.config.ast;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseAstProcessor {

    protected ProcessingEnvironment processingEnv;
    protected RoundEnvironment roundEnv;
    protected BuildTimeMetadataCache cache;
    protected AstContext context;
    protected GlobalRegistry globalRegistry;

    // 핸들러 리스트
    private final List<AstHandler<?>> handlers = new ArrayList<>();

    /**
     * 초기화
     */
    public void init(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv, BuildTimeMetadataCache buildTimeMetadataCache, GlobalRegistry globalRegistry) {
        this.processingEnv = processingEnv;
        this.roundEnv = roundEnv;
        this.cache = buildTimeMetadataCache;
        this.context = new AstContext(processingEnv);

        this.globalRegistry = globalRegistry;
        onInit();
    }

    /**
     * 서브 클래스에서 필요한 초기화 작업을 구현
     */
    protected abstract void onInit();

    /**
     * 핸들러 등록
     */
    protected <T> void addHandler(AstHandler<T> handler, T handlerContext) {
        handlers.add(handler);
        handler.setHandlerContext(handlerContext);
    }

    /**
     * 등록된 핸들러 순차 실행
     */
    public void execute() throws Exception {
        for (AstHandler<?> handler : handlers) {
            handler.handle(roundEnv);
        }
    }
}