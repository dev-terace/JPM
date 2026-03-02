package io.jpm.core.m_entity.processor;

import io.jpm.config.ast.BaseAstProcessor;
import io.jpm.core.m_entity.processor.handler.CreateDDLMetadataHandler;
import io.jpm.core.m_entity.processor.handler.ExecuteDDLXMLHandler;
import io.jpm.core.m_entity.processor.handler.WriteDDLXMLHandler;
import io.jpm.core.m_entity.processor.handler.handlerContext.DDLHandlerContext;

public class MEntityAstProcessorV2 extends BaseAstProcessor {

    @Override
    protected void onInit() {
        DDLHandlerContext handlerContext = new DDLHandlerContext();
        addHandler(new CreateDDLMetadataHandler(cache, globalRegistry, context), handlerContext);
        addHandler(new WriteDDLXMLHandler(cache, globalRegistry, context), handlerContext);
        addHandler(new ExecuteDDLXMLHandler(cache, globalRegistry, context), handlerContext);

    }


}
