package io.jpm.core.jpm_repository.parse.infra.ast.ast_segment_inliner;

import com.sun.source.tree.*;
import io.jpm.config.ast.AstContext;
import io.jpm.core.jpm_repository.parse.infra.MapParamRegistry;
import io.jpm.core.jpm_repository.parse.domain.vo.MethodMeta;
import io.jpm.core.jpm_repository.parse.domain.cache.RepoMetaRegistry;
import io.jpm.core.jpm_repository.parse.infra.ast.ast_dsl_command_proc.AstDslCommandProc;
import io.jpm.core.jpm_repository.parse.infra.ast.AstMethodTreeUtil;
import io.jpm.core.jpm_repository.parse.infra.ast.argument_token_extractor.AstArgumentTokenExtractor;
import io.jpm.common.utils.LogPrinter;

import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;

/**
 * 세그먼트(Segment) 클래스의 특정 메서드를 현재 MethodMeta 에 인라인합니다.
 * 기존 inlineSegmentMethodTree() 의 단일 책임 분리 버전입니다.
 */
@Deprecated
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

    public void inline(AstContext astContext,
                       String repoClassName, String fieldVarName, String segmentMethodName,
                       MethodMeta methodMeta, List<String> passedArgs) {

        String segmentTypeName = repoMetaRegistry.getSegmentPath(repoClassName, fieldVarName);
        if (segmentTypeName == null) return;

        TypeElement segmentElement = astContext.getElements().getTypeElement(segmentTypeName);
        if (segmentElement == null) return;

        ClassTree segmentTree = astContext.getTrees().getTree(segmentElement);
        if (segmentTree == null) return;

        for (Tree member : segmentTree.getMembers()) {
            if (!(member instanceof MethodTree)) continue;
            MethodTree methodTree = (MethodTree) member;
            if (!methodTree.getName().toString().equals(segmentMethodName)) continue;

            MapParamRegistry mapParamRegistry = buildArgContext(methodTree, passedArgs);
            processBody(methodTree.getBody(), mapParamRegistry, methodMeta);
            break;
        }
    }

    // -------------------------------------------------------------------------

    private MapParamRegistry buildArgContext(MethodTree methodTree, List<String> passedArgs) {
        MapParamRegistry ctx = new MapParamRegistry();
        List<? extends VariableTree> params = methodTree.getParameters();
        for (int i = 0; i < params.size() && i < passedArgs.size(); i++) {
            String paramName = params.get(i).getName().toString();
            ctx.bind(paramName, passedArgs.get(i));
            LogPrinter.info("[SegmentInliner] argContext 매핑: " + paramName + " → " + passedArgs.get(i));
        }
        return ctx;
    }

    private void processBody(BlockTree body, MapParamRegistry mapParamRegistry, MethodMeta methodMeta) {
        if (body == null) return;
        for (StatementTree stmt : body.getStatements()) {
            if (!(stmt instanceof ExpressionStatementTree)) continue;
            ExpressionTree expr = ((ExpressionStatementTree) stmt).getExpression();
            for (MethodInvocationTree call : AstMethodTreeUtil.flattenChain(expr)) {
                String command = AstMethodTreeUtil.getMethodName(call);
                LogPrinter.info("[SegmentInliner] command: " + command);
                if (dslKeywords.contains(command)) {
                    List<String> rawArgs = tokenExtractor.extract(call, mapParamRegistry, methodMeta);


                    commandProcessor.process(command, rawArgs, methodMeta, mapParamRegistry);
                }
            }
        }
    }
}