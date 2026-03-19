package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.AstContext;
import io.jpm.core.jpm_repository.domain.cache.MapParamRegistryImpl;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;
import io.jpm.core.jpm_repository.valid.policy.JoinNodeValidatorPolicyV2;

import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 세그먼트(Segment) 클래스의 특정 메서드를 현재 MethodMeta 에 인라인합니다.
 */
public class SegmentInliner {

    private final ArgumentTokenExtractor tokenExtractor;
    private final DslCommandProcessor dslCommandProcessor;
    private final AstContext                   astContext;
    private final Set<String>                  dslKeywords;
    private final ErrorTracker errorTracker;
    private final JoinNodeValidatorPolicyV2 joinValidator;
    private static final List<String> JOIN_COMMANDS      = Arrays.asList("innerJoin", "leftJoin", "rightJoin");

    public SegmentInliner(ArgumentTokenExtractor tokenExtractor,
                          DslCommandProcessor dslCommandProcessor,
                          AstContext astContext,
                          Set<String> dslKeywords,
                          ErrorTracker errorTracker,
                          JoinNodeValidatorPolicyV2 joinValidator) {
        this.tokenExtractor   = tokenExtractor;
        this.dslCommandProcessor = dslCommandProcessor;
        this.astContext       = astContext;
        this.dslKeywords      = dslKeywords;
        this.errorTracker   = errorTracker;
        this.joinValidator = joinValidator;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** ParseMethodBodyStep 에서 호출 - 토큰 추출 / FQN 탐색 / 인라인을 일괄 수행 */
    public void execute(MethodInvocationTree call,
                        TypeElement repoElement,
                        MapParamRegistryImpl mapParamRegistry,
                        MethodMeta methodMeta) {
        try {


            List<String> passedArgs  = tokenExtractor.extract(call, mapParamRegistry);
            String segmentClassName  = resolveSegmentClassName(repoElement, passedArgs);
            String segmentMethodName = passedArgs.get(1);

            if(segmentMethodName.contains("::")) {
                segmentMethodName = segmentMethodName.split("::")[1];
            }

            LogPrinter.info("[segmentInliner] passedArgs = " + passedArgs);
            errorTracker.setClassName(segmentClassName);
            errorTracker.setMethodName(segmentMethodName);
            errorTracker.setTrees(astContext.getTrees());


            List<String> segmentArgs = passedArgs.subList(2, passedArgs.size());

            inline(methodMeta, segmentArgs, segmentClassName, segmentMethodName);
            LogPrinter.info("[PARSE] methodMeta=" + methodMeta);
        } catch (Exception e) {
            for (StackTraceElement ste : e.getStackTrace()) LogPrinter.info("[STACK] " + ste);
            throw new IllegalArgumentException(e);
        }
    }

    // -------------------------------------------------------------------------
    // Segment Class Resolution  (기존 ParseMethodBodyStep 에 있던 로직)
    // -------------------------------------------------------------------------

    private String resolveSegmentClassName(TypeElement repoElement, List<String> passedArgs) {

        String simpleName = passedArgs.get(0).replace(".class", "");

        String fqn = findFqnFromImports(repoElement, simpleName);

        if (!fqn.isEmpty()) return fqn;

        // 같은 패키지 fallback
        String pkg = astContext.getElements()
                .getPackageOf(repoElement)
                .getQualifiedName()
                .toString();

        return pkg + "." + simpleName;
    }

    private String findFqnFromImports(TypeElement repoElement, String simpleName) {
        TreePath path          = astContext.getTrees().getPath(repoElement);
        CompilationUnitTree cu = path.getCompilationUnit();
        for (ImportTree imp : cu.getImports()) {
            String importStr = imp.getQualifiedIdentifier().toString();
            if (importStr.endsWith("." + simpleName)) return importStr;
        }
        return "";
    }

    // -------------------------------------------------------------------------
    // Inline
    // -------------------------------------------------------------------------

    private void inline(MethodMeta methodMeta,
                        List<String> passedArgs,
                        String segmentClassName,
                        String segmentMethodName) {

        LogPrinter.info("[INLINE] segmentClassName= " + segmentClassName);
        TypeElement segmentElement = astContext.getCompilingElement(segmentClassName);
        if (segmentElement == null) return;

        ClassTree segmentTree = astContext.getTrees().getTree(segmentElement);
        if (segmentTree == null) return;

        findAndInlineMethod(segmentTree, segmentMethodName, passedArgs, methodMeta);
    }

    private void findAndInlineMethod(ClassTree segmentTree,
                                     String segmentMethodName,
                                     List<String> passedArgs,
                                     MethodMeta methodMeta) {
        for (Tree member : segmentTree.getMembers()) {
            if (!(member instanceof MethodTree)) continue;
            MethodTree methodTree = (MethodTree) member;
            if (!methodTree.getName().toString().equals(segmentMethodName)) continue;

            MapParamRegistryImpl argContext = buildArgContext(methodTree, passedArgs);
            processBody(methodTree.getBody(), argContext, methodMeta);
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

    private void processBody(BlockTree body,
                             MapParamRegistryImpl argContext,
                             MethodMeta methodMeta) {
        if (body == null) return;
        for (StatementTree stmt : body.getStatements()) {
            if (!(stmt instanceof ExpressionStatementTree)) continue;
            ExpressionTree expr = ((ExpressionStatementTree) stmt).getExpression();
            processChain(expr, argContext, methodMeta);
        }
    }

    private void processChain(ExpressionTree expr,
                              MapParamRegistryImpl argContext,
                              MethodMeta methodMeta) {
        for (MethodInvocationTree call : AstMethodTree.flattenChain(expr)) {
            String command = AstMethodTree.getMethodName(call);
            if (!dslKeywords.contains(command)) continue;

            List<String> rawArgs = tokenExtractor.extract(call, argContext);
            LogPrinter.info("[SegmentInliner] rawArgs: " + rawArgs);

            if(JOIN_COMMANDS.contains(command)) {
                joinValidator.validateJoinType(command, rawArgs.get(1), rawArgs.get(2));
            }

            dslCommandProcessor.process(command, rawArgs, methodMeta);
        }
    }
}