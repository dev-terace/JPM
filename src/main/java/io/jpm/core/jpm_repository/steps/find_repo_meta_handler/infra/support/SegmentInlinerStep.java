package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support;

import com.sun.source.tree.*;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.AstContext;
import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.config.ast.Step;
import io.jpm.core.jpm_repository.domain.cache.MapParamRegistryImpl;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context.DslCommandProcContext;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context.SegmentInlinerContext;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.context.SegmentInlinerProcContext;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.parse_method_body_step.DslCommandProcStep;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.utils.FindFqnUtil;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.utils.TreePositionUtil;


import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 세그먼트(Segment) 클래스의 특정 메서드를 현재 MethodMeta 에 인라인합니다.
 */
public class SegmentInlinerStep implements Step<SegmentInlinerContext> {

    private final ArgumentTokenExtractor tokenExtractor;
    private final DslCommandProcStep dslCommandProcStep;
    private final AstContext                   astContext;
    private final Set<String>                  dslKeywords;
    private final ErrorTracker errorTracker;

    private static final List<String> JOIN_COMMANDS      = Arrays.asList("innerJoin", "leftJoin", "rightJoin");


    public SegmentInlinerStep(ArgumentTokenExtractor tokenExtractor,
                              DslCommandProcStep dslCommandProcStep,
                              AstContext astContext,
                              Set<String> dslKeywords,
                              ErrorTracker errorTracker


                              ) {
        this.tokenExtractor   = tokenExtractor;
        this.dslCommandProcStep = dslCommandProcStep;
        this.astContext       = astContext;
        this.dslKeywords      = dslKeywords;
        this.errorTracker   = errorTracker;


    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** ParseMethodBodyStep 에서 호출 - 토큰 추출 / FQN 탐색 / 인라인을 일괄 수행 */
    public void execute(SegmentInlinerContext context) {
        try {

            MethodInvocationTree call = context.getCall();
            TypeElement repoElement =  context.getRepoElement();
            MapParamRegistryImpl mapParamRegistry = context.getMapParamRegistry();
            MethodMeta methodMeta = context.getMethodMeta();

            List<String> passedArgs  = tokenExtractor.extract(call, mapParamRegistry);
            String segmentClassName  = FindFqnUtil.findClassNameFqnOrInterface(repoElement, passedArgs, astContext);
            String segmentMethodName = passedArgs.get(1);

            if(segmentMethodName.contains("::")) {
                segmentMethodName = segmentMethodName.split("::")[1];
            }


            LogPrinter.info("[segmentInliner] segmentClassName = " + segmentClassName);

            errorTracker.setTrees(astContext.getTrees());


            List<String> segmentArgs = passedArgs.subList(2, passedArgs.size());
            int lineNumber = TreePositionUtil.getLineNumber(astContext.getTrees(), call, repoElement);
            inline(methodMeta, segmentArgs, segmentClassName, segmentMethodName, lineNumber);
            LogPrinter.info("[PARSE] methodMeta=" + methodMeta);
        } catch (Exception e) {
            for (StackTraceElement ste : e.getStackTrace()) LogPrinter.info("[STACK] " + ste);
            throw new IllegalArgumentException(e);
        }
    }




    // -------------------------------------------------------------------------
    // Inline
    // -------------------------------------------------------------------------

    private void inline(MethodMeta methodMeta,
                        List<String> passedArgs,
                        String segmentClassName,
                        String segmentMethodName,
                        int lineNumber
    ) {

        LogPrinter.info("[INLINE] segmentClassName= " + segmentClassName);
        TypeElement segmentElement = astContext.getCompilingElement(segmentClassName);
        if (segmentElement == null) return;

        ClassTree segmentTree = astContext.getTrees().getTree(segmentElement);
        if (segmentTree == null) return;

        for (Tree member : segmentTree.getMembers()) {
            if (!(member instanceof MethodTree)) continue;
            MethodTree methodTree = (MethodTree) member;
            if (!methodTree.getName().toString().equals(segmentMethodName)) continue;

            MapParamRegistryImpl argContext = buildArgContext(methodTree, passedArgs);

            LogPrinter.info("[SegmentInlinerStep]" +
                    " .body("              + methodTree.getBody()  + ")" +
                    " .argContext("        + argContext             + ")" +
                    " .methodMeta("        + methodMeta             + ")" +
                    " .segmentClassName("  + segmentClassName       + ")" +
                    " .segmentMethodName(" + segmentMethodName      + ")"
            );

            processBody(new SegmentInlinerProcContext
                    (methodTree.getBody(), argContext, methodMeta
                    , segmentClassName, segmentMethodName, lineNumber

                    ));
            break;
        }
    }


    // -------------------------------------------------------------------------
    // Argument Context
    // -------------------------------------------------------------------------

    private MapParamRegistryImpl buildArgContext(MethodTree methodTree, List<String> passedArgs) {
        MapParamRegistryImpl ctx = new MapParamRegistryImpl();
        List<? extends VariableTree> params = methodTree.getParameters();
        for (int i = 0; i < params.size() && i < passedArgs.size(); i++) {
            ctx.bind(params.get(i).getName().toString(), passedArgs.get(i));
        }
        return ctx;
    }

    // -------------------------------------------------------------------------
    // Body Processing
    // -------------------------------------------------------------------------

    private void processBody(SegmentInlinerProcContext ctx) {
        BlockTree body = ctx.getBody();


        if (body == null) return;



        for (StatementTree stmt : body.getStatements()) {
            if (!(stmt instanceof ExpressionStatementTree)) continue;
            ExpressionTree expr = ((ExpressionStatementTree) stmt).getExpression();
            processChain(expr, ctx);
        }
    }




    private void processChain(ExpressionTree expr,
                             SegmentInlinerProcContext ctx) {

        MapParamRegistryImpl argContext = ctx.getArgContext();
        MethodMeta methodMeta = ctx.getMethodMeta();


        for (MethodInvocationTree call : AstMethodTree.flattenChain(expr)) {
            String command = AstMethodTree.getMethodName(call);
            if (!dslKeywords.contains(command)) continue;

            List<String> rawArgs = tokenExtractor.extract(call, argContext);
            LogPrinter.info("[SegmentInliner] rawArgs: " + rawArgs);



            dslCommandProcStep.execute(new DslCommandProcContext(command, rawArgs, methodMeta
                    , ctx.getSegmentClassName(), ctx.getSegmentMethodName(), ctx.getLineNumber()));
        }
    }
}