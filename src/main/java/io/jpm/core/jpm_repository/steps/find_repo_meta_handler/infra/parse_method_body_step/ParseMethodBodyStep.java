package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.parse_method_body_step;

import com.sun.source.tree.*;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.AstContext;
import io.jpm.config.ast.GlobalRegistry;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.cache.MapParamRegistryImpl;
import io.jpm.core.jpm_repository.domain.model.DSLKeywords;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context.MethodParseContext;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.DslCommandProcessor;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.SegmentInliner;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.AstMethodTree;

import javax.lang.model.element.TypeElement;
import java.util.Set;

public class ParseMethodBodyStep implements Step<MethodParseContext> {

    private final DslCommandProcessor dslCommandProcessor;
    private final SegmentInliner segmentInliner;
    private final MapParamRegistryImpl mapParamRegistry;
    private final ErrorTracker         errorTracker;
    private final Set<String>          DSL_KEYWORDS;
    private final GlobalRegistry      globalRegistry;

    public ParseMethodBodyStep(GlobalRegistry globalRegistry, AstContext astContext) {
        this.dslCommandProcessor = globalRegistry.commandProcessor();
        this.segmentInliner = globalRegistry.segmentInliner();
        this.mapParamRegistry   = globalRegistry.mapParamRegistry();
        this.errorTracker       = globalRegistry.errorTracker();
        this.DSL_KEYWORDS       = DSLKeywords.getDSLKeywords();
        this.globalRegistry = globalRegistry;
    }

    @Override
    public void execute(MethodParseContext context) {
        BlockTree body = context.getMethodTree().getBody();
        if (body == null) return;

        globalRegistry.localVariableCollector().collect(body, mapParamRegistry);


        for (StatementTree stmt : body.getStatements()) {
            if (stmt instanceof ExpressionStatementTree) {
                ExpressionTree expr = ((ExpressionStatementTree) stmt).getExpression();
                parseChain(expr, context);
            }
        }
    }

    private void parseChain(ExpressionTree expr, MethodParseContext context) {
        MethodMeta  methodMeta  = context.getMethodMeta();
        TypeElement repoElement = context.getRepoElement();

        for (MethodInvocationTree call : AstMethodTree.flattenChain(expr)) {
            String command = AstMethodTree.getMethodName(call);


            errorTracker.setMethodName(methodMeta.getMethodName());


            boolean isSegmentCommand = !command.equals("super");
            if (isDslCommand(command)) {

                errorTracker.setClassName(context.getRepoElement().getQualifiedName().toString());
                dslCommandProcessor.execute(call, mapParamRegistry, methodMeta);
            } else if (isSegmentCommand) {
                segmentInliner.execute(call, repoElement, mapParamRegistry, methodMeta);
            }
        }
    }

    private boolean isDslCommand(String command) {
        return DSL_KEYWORDS.contains(command) && !command.equals("segment");
    }
}