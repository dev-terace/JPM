package io.jpm.config.ast;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

public abstract class AstHandler <T>{
    protected final BuildTimeMetadataCache cache;
    protected final GlobalRegistry globalRegistry;
    protected final AstContext context;
    protected T handlerContext;

    public AstHandler(BuildTimeMetadataCache cache,
                      GlobalRegistry globalRegistry, AstContext astContext) {
        this.context = astContext;
        this.cache = cache;
        this.globalRegistry = globalRegistry;


    }

    public abstract void handle(RoundEnvironment roundEnv) throws Exception;

    public abstract void setHandlerContext(T handlerContext);

}