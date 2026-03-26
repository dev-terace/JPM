package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.parse_method_body_step;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;

import com.sun.source.util.Trees;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.config.ast.AstContext;
import io.jpm.config.ast.GlobalRegistry;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.cache.MapParamRegistryImpl;
import io.jpm.core.jpm_repository.domain.model.DSLKeywords;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context.DslCommandProcContext;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context.MethodParseContext;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context.SegmentInlinerContext;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.ArgumentTokenExtractor;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.SegmentInlinerStep;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.AstMethodTree;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.utils.TreePositionUtil;

import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;

public class ParseMethodBodySteps implements Step<MethodParseContext> {

    private final DslCommandProcStep dslCommandProcStep;
    private final SegmentInlinerStep segmentInlinerStep;
    private final MapParamRegistryImpl mapParamRegistry;
    private final ErrorTracker         errorTracker;
    private final Set<String>          DSL_KEYWORDS;
    private final GlobalRegistry      globalRegistry;
    private final ArgumentTokenExtractor argumentTokenExtractor;
    private final AstContext astContext;
    public ParseMethodBodySteps(GlobalRegistry globalRegistry, AstContext astContext) {
        this.dslCommandProcStep = globalRegistry.commandProcessor();
        this.segmentInlinerStep = globalRegistry.segmentInliner();
        this.mapParamRegistry   = globalRegistry.mapParamRegistry();
        this.errorTracker       = globalRegistry.errorTracker();
        this.DSL_KEYWORDS       = DSLKeywords.getDSLKeywords();
        this.globalRegistry = globalRegistry;
        this.argumentTokenExtractor = globalRegistry.tokenExtractor();
        this.astContext       = astContext;

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


            boolean isSegmentCommand = !command.equals("super");

            if (isDslCommand(command)) {


                String className = repoElement.getQualifiedName().toString();
                String methodName = methodMeta.getMethodName();
                int lineNumber = TreePositionUtil.getLineNumber(astContext.getTrees(), call, context.getRepoElement());


                List<String> rawArgs = argumentTokenExtractor.extract(call, mapParamRegistry);
                dslCommandProcStep.execute(getContext
                        (command, rawArgs, methodMeta,
                        className, methodName, lineNumber)
                );



            } else if (isSegmentCommand) {
                segmentInlinerStep.execute(new SegmentInlinerContext(call, repoElement, mapParamRegistry, methodMeta));
            }
        }
    }



    private DslCommandProcContext getContext(String command, List<String> rawArgs, MethodMeta methodMeta, String className, String methodName, int lineNumber) {
        return new DslCommandProcContext(command, rawArgs, methodMeta, className, methodName, lineNumber);
    }

    private boolean isDslCommand(String command) {
        return DSL_KEYWORDS.contains(command) && !command.equals("segment");
    }
}