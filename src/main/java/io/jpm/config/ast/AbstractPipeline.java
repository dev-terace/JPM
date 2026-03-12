package io.jpm.config.ast;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPipeline {

    protected ProcessingEnvironment processingEnv;
    protected RoundEnvironment roundEnv;
    protected BuildTimeMetadataCache cache;
    protected AstContext context;
    protected GlobalRegistry globalRegistry;

    // 핸들러 + 컨텍스트를 함께 묶어 저장
    private final List<HandlerEntry<?>> handlerEntries = new ArrayList<>();

    public void init(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv,
                     BuildTimeMetadataCache buildTimeMetadataCache, GlobalRegistry globalRegistry) {
        this.processingEnv = processingEnv;
        this.roundEnv      = roundEnv;
        this.cache         = buildTimeMetadataCache;
        this.context       = new AstContext(processingEnv);
        this.globalRegistry = globalRegistry;
        onInit();
    }

    protected abstract void onInit();

    protected <C> void addHandler(AbstractHandler<C> handler, C handlerContext) {
        handlerEntries.add(new HandlerEntry<>(handler, handlerContext));
    }

    @SuppressWarnings("unchecked")
    public void execute() throws Exception {
        for (HandlerEntry<?> entry : handlerEntries) {
            entry.execute();
        }
    }

    // 핸들러 + 컨텍스트 묶음
    private static class HandlerEntry<C> {
        private final AbstractHandler<C> handler;
        private final C context;

        HandlerEntry(AbstractHandler<C> handler, C context) {
            this.handler = handler;
            this.context = context;
        }

        void execute() throws Exception {
            handler.handle(context);
        }
    }
}