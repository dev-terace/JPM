package jpm_repository.parse.infra.ast;

import com.sun.source.tree.*;
import com.sun.source.util.Trees;
import jpm_repository.parse.infra.MapParamRegistryImpl;
import jpm_repository.parse.domain.vo.MethodMeta;
import jpm_repository.parse.domain.cache.RepoMetaRegistry;
import jpm_repository.parse.infra.ast.argument_token_extractor.AstArgumentTokenExtractor;
import utils.LogPrinter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;

/**
 * 세그먼트(Segment) 클래스의 특정 메서드를 현재 MethodMeta 에 인라인합니다.
 * 기존 inlineSegmentMethodTree() 의 단일 책임 분리 버전입니다.
 */
public class AstSegmentInliner {

    private final RepoMetaRegistry repoMetaRegistry;
    private final AstArgumentTokenExtractor tokenExtractor;
    private final AstDslCommandProc commandProcessor;
    private final Set<String> dslKeywords;

    public AstSegmentInliner(RepoMetaRegistry repoMetaRegistry,
                             AstArgumentTokenExtractor tokenExtractor,
                             AstDslCommandProc commandProcessor,
                             Set<String> dslKeywords) {
        this.repoMetaRegistry = repoMetaRegistry;
        this.tokenExtractor     = tokenExtractor;
        this.commandProcessor   = commandProcessor;
        this.dslKeywords        = dslKeywords;
    }

    public void inline(ProcessingEnvironment env, Trees trees,
                       String repoClassName, String fieldVarName, String segmentMethodName,
                       MethodMeta methodMeta, List<String> passedArgs) {

        String segmentTypeName = repoMetaRegistry.getSegmentPath(repoClassName, fieldVarName);
        if (segmentTypeName == null) return;

        TypeElement segmentElement = env.getElementUtils().getTypeElement(segmentTypeName);
        if (segmentElement == null) return;

        ClassTree segmentTree = trees.getTree(segmentElement);
        if (segmentTree == null) return;

        for (Tree member : segmentTree.getMembers()) {
            if (!(member instanceof MethodTree)) continue;
            MethodTree methodTree = (MethodTree) member;
            if (!methodTree.getName().toString().equals(segmentMethodName)) continue;

            MapParamRegistryImpl mapParamRegistryImpl = buildArgContext(methodTree, passedArgs);
            processBody(methodTree.getBody(), mapParamRegistryImpl, methodMeta);
            break;
        }
    }

    // -------------------------------------------------------------------------

    private MapParamRegistryImpl buildArgContext(MethodTree methodTree, List<String> passedArgs) {
        MapParamRegistryImpl ctx = new MapParamRegistryImpl();
        List<? extends VariableTree> params = methodTree.getParameters();
        for (int i = 0; i < params.size() && i < passedArgs.size(); i++) {
            String paramName = params.get(i).getName().toString();
            ctx.bind(paramName, passedArgs.get(i));
            LogPrinter.info("[SegmentInliner] argContext 매핑: " + paramName + " → " + passedArgs.get(i));
        }
        return ctx;
    }

    private void processBody(BlockTree body, MapParamRegistryImpl mapParamRegistryImpl, MethodMeta methodMeta) {
        if (body == null) return;
        for (StatementTree stmt : body.getStatements()) {
            if (!(stmt instanceof ExpressionStatementTree)) continue;
            ExpressionTree expr = ((ExpressionStatementTree) stmt).getExpression();
            for (MethodInvocationTree call : AstMethodTreeUtil.flattenChain(expr)) {
                String command = AstMethodTreeUtil.getMethodName(call);
                if (dslKeywords.contains(command)) {
                    List<String> rawArgs = tokenExtractor.extract(call, mapParamRegistryImpl, methodMeta);
                    commandProcessor.process(command, rawArgs, methodMeta, mapParamRegistryImpl);
                }
            }
        }
    }
}