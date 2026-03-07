package io.jpm.core.jpm_repository.parse.infra.ast.ast_segment_inliner;

import com.sun.source.tree.*;
import io.jpm.config.ast.AstContext;
import io.jpm.core.jpm_repository.parse.infra.MapParamRegistry;
import io.jpm.core.jpm_repository.parse.domain.vo.MethodMeta;
import io.jpm.core.jpm_repository.parse.domain.cache.RepoMetaRegistry;
import io.jpm.core.jpm_repository.parse.infra.ast.ast_dsl_command_proc.AstDslCommandProc;
import io.jpm.core.jpm_repository.parse.infra.ast.AstMethodTreeUtil;
import io.jpm.common.utils.LogPrinter;
import io.jpm.core.jpm_repository.parse.infra.ast.argument_token_extractor.AstArgumentTokenExtractorV2;
import io.jpm.core.jpm_repository.parse.infra.ast.ast_dsl_command_proc.AstDslCommandProcV2;

import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 세그먼트(Segment) 클래스의 특정 메서드를 현재 MethodMeta 에 인라인합니다.
 * 기존 inlineSegmentMethodTree() 의 단일 책임 분리 버전입니다.
 */
public class AstSegmentInlinerV2 {

    private final RepoMetaRegistry repoMetaRegistry;
    private final AstArgumentTokenExtractorV2 tokenExtractor;
    private final AstDslCommandProcV2 commandProcessor;
    private final Set<String> dslKeywords;

    public AstSegmentInlinerV2(RepoMetaRegistry repoMetaRegistry,
                             AstArgumentTokenExtractorV2 tokenExtractor,
                             AstDslCommandProcV2 commandProcessor,
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

            if(!Arrays.asList("r", "b", "q").contains(paramName))
            {
                continue;
            }

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
                if (dslKeywords.contains(command)) {
                    List<String> rawArgs = tokenExtractor.extract(call, mapParamRegistry);
                    commandProcessor.process(command, rawArgs, methodMeta);
                }
            }
        }
    }
}