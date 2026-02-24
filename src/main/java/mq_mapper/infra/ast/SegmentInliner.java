package mq_mapper.infra.ast;

import com.sun.source.tree.*;
import com.sun.source.util.Trees;
import mq_mapper.domain.vo.MethodMeta;
import mq_mapper.infra.ast.utils.MethodTreeUtil;
import mq_mapper.infra.repo.EntityMetaRegistry;
import utils.LogPrinter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;

/**
 * 세그먼트(Segment) 클래스의 특정 메서드를 현재 MethodMeta 에 인라인합니다.
 * 기존 inlineSegmentMethodTree() 의 단일 책임 분리 버전입니다.
 */
public class SegmentInliner {

    private final EntityMetaRegistry entityMetaRegistry;
    private final ArgumentTokenExtractor tokenExtractor;
    private final DslCommandProcessor commandProcessor;
    private final Set<String> dslKeywords;

    public SegmentInliner(EntityMetaRegistry entityMetaRegistry,
                          ArgumentTokenExtractor tokenExtractor,
                          DslCommandProcessor commandProcessor,
                          Set<String> dslKeywords) {
        this.entityMetaRegistry = entityMetaRegistry;
        this.tokenExtractor     = tokenExtractor;
        this.commandProcessor   = commandProcessor;
        this.dslKeywords        = dslKeywords;
    }

    public void inline(ProcessingEnvironment env, Trees trees,
                       String repoClassName, String fieldVarName, String segmentMethodName,
                       MethodMeta methodMeta, List<String> passedArgs) {

        String segmentTypeName = entityMetaRegistry.getSegmentPath(repoClassName, fieldVarName);
        if (segmentTypeName == null) return;

        TypeElement segmentElement = env.getElementUtils().getTypeElement(segmentTypeName);
        if (segmentElement == null) return;

        ClassTree segmentTree = trees.getTree(segmentElement);
        if (segmentTree == null) return;

        for (Tree member : segmentTree.getMembers()) {
            if (!(member instanceof MethodTree)) continue;
            MethodTree methodTree = (MethodTree) member;
            if (!methodTree.getName().toString().equals(segmentMethodName)) continue;

            ArgContext argContext = buildArgContext(methodTree, passedArgs);
            processBody(methodTree.getBody(), argContext, methodMeta);
            break;
        }
    }

    // -------------------------------------------------------------------------

    private ArgContext buildArgContext(MethodTree methodTree, List<String> passedArgs) {
        ArgContext ctx = new ArgContext();
        List<? extends VariableTree> params = methodTree.getParameters();
        for (int i = 0; i < params.size() && i < passedArgs.size(); i++) {
            String paramName = params.get(i).getName().toString();
            ctx.bind(paramName, passedArgs.get(i));
            LogPrinter.info("[SegmentInliner] argContext 매핑: " + paramName + " → " + passedArgs.get(i));
        }
        return ctx;
    }

    private void processBody(BlockTree body, ArgContext argContext, MethodMeta methodMeta) {
        if (body == null) return;
        for (StatementTree stmt : body.getStatements()) {
            if (!(stmt instanceof ExpressionStatementTree)) continue;
            ExpressionTree expr = ((ExpressionStatementTree) stmt).getExpression();
            for (MethodInvocationTree call : MethodTreeUtil.flattenChain(expr)) {
                String command = MethodTreeUtil.getMethodName(call);
                if (dslKeywords.contains(command)) {
                    List<String> rawArgs = tokenExtractor.extract(call, argContext, methodMeta);
                    commandProcessor.process(command, rawArgs, methodMeta, argContext);
                }
            }
        }
    }
}