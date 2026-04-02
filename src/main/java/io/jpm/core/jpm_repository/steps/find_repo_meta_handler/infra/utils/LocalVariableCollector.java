package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.utils;

import com.sun.source.tree.*;
import io.jpm.common.utils.LogPrinter;
import io.jpm.core.jpm_repository.domain.cache.MapParamRegistryImpl;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.ArgumentTokenExtractor;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.ArgumentTokenExtractorValueResolver;

import java.util.List;

// LocalVariableCollector.java
public class LocalVariableCollector {


    private final ArgumentTokenExtractorValueResolver valueResolver; // 추가

    public LocalVariableCollector(
                                  ArgumentTokenExtractorValueResolver valueResolver) {

        this.valueResolver  = valueResolver;
    }

    public void collect(BlockTree body, MapParamRegistryImpl argContext) {
        for (StatementTree stmt : body.getStatements()) {
            if (!(stmt instanceof VariableTree)) continue;
            VariableTree varTree = (VariableTree) stmt;

            String varName = varTree.getName().toString();
            ExpressionTree initializer = varTree.getInitializer();
            if (initializer == null) continue;

            if (initializer instanceof MethodInvocationTree) {
                // col(), r(), q() 등은 valueResolver 로 resolve
                String resolved = valueResolver.resolve(
                        initializer, argContext, false, true
                );
                if (resolved != null && !resolved.isEmpty()) {
                    argContext.bind(varName, resolved);
                    LogPrinter.info("[LocalVar] " + varName + " = " + resolved);
                }
            } else if (initializer instanceof LiteralTree){
                Object value = ((LiteralTree) initializer).getValue();

                argContext.bind(varName, value.toString());
                LogPrinter.info("[LocalVar] " + varName + " = " + initializer);
            }
        }
    }
}
