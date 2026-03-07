package io.jpm.core.jpm_repository.processor.handler.find_repo_meta_handler;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.AstContext;
import io.jpm.config.ast.GlobalRegistry;
import io.jpm.core.jpm_repository.parse.domain.vo.DSLKeywords;
import io.jpm.core.jpm_repository.parse.domain.vo.MethodMeta;
import io.jpm.core.jpm_repository.parse.domain.vo.RepoMeta;
import io.jpm.core.jpm_repository.parse.infra.MapParamRegistry;
import io.jpm.core.jpm_repository.parse.infra.ast.AstMethodTreeUtil;
import io.jpm.core.jpm_repository.parse.infra.ast.argument_token_extractor.AstArgumentTokenExtractorV2;
import io.jpm.core.jpm_repository.parse.infra.ast.ast_dsl_command_proc.AstDslCommandProcV2;
import io.jpm.core.jpm_repository.parse.infra.ast.ast_segment_inliner.AstSegmentInlinerV3;

import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;

public class FindRepoMetaHandlerProc {

    private final AstContext astContext;
    private final MapParamRegistry mapParamRegistry;
    private final AstArgumentTokenExtractorV2 tokenExtractor;
    private final AstDslCommandProcV2 commandProcessor;
    private final AstSegmentInlinerV3 segmentInliner;
    private final ErrorTracker errorTracker;
    private final Trees trees;
    private final Set<String> DSL_KEYWORDS;

    public FindRepoMetaHandlerProc(GlobalRegistry globalRegistry, AstContext astContext) {
        this.astContext       = astContext;
        this.mapParamRegistry = globalRegistry.mapParamRegistry();
        this.tokenExtractor   = globalRegistry.tokenExtractor();
        this.commandProcessor = globalRegistry.commandProcessor();
        this.segmentInliner   = globalRegistry.segmentInliner();
        this.errorTracker     = globalRegistry.errorTracker();
        this.trees            = astContext.getTrees();
        this.DSL_KEYWORDS     = DSLKeywords.getDSLKeywords();
    }

    // =========================================================================
    // Public API
    // =========================================================================

    public void parseMemberMethod(MethodTree methodTree, TypeElement repoElement, RepoMeta repoMeta) {
        MethodMeta methodMeta = parseMemberMethodProc(methodTree, repoElement);
        if (!methodMeta.getStatements().isEmpty()) {
            repoMeta.addMethod(methodMeta);
            LogPrinter.info("[FindRepoMetaHandler]" + repoMeta);
        }
    }

    // =========================================================================
    // Method Parsing
    // =========================================================================

    private MethodMeta parseMemberMethodProc(MethodTree methodTree, TypeElement repoElement) {
        MethodMeta methodMeta = new MethodMeta(methodTree.getName().toString());
        registerParams(methodTree, methodMeta);
        parseBody(methodTree.getBody(), repoElement, methodMeta);
        LogPrinter.info("[METHOD] name=" + methodTree.getName()
                + " statements=" + methodMeta.getStatements().size()
                + " params=" + methodMeta.getParameters().size());
        return methodMeta;
    }

    private void registerParams(MethodTree methodTree, MethodMeta methodMeta) {
        for (VariableTree param : methodTree.getParameters()) {
            String paramName = param.getName().toString();
            String paramType = param.getType().toString();
            methodMeta.addParameter(paramName, paramType);
            mapParamRegistry.registerParam(paramName, paramType);
        }
    }

    private void parseBody(BlockTree body, TypeElement repoElement, MethodMeta methodMeta) {
        if (body == null) return;
        for (StatementTree stmt : body.getStatements()) {
            if (stmt instanceof ExpressionStatementTree) {
                ExpressionTree expr = ((ExpressionStatementTree) stmt).getExpression();
                parseChain(expr, repoElement, methodMeta);
            }
        }
    }

    // =========================================================================
    // Chain Parsing
    // =========================================================================

    private void parseChain(ExpressionTree expr, TypeElement repoElement, MethodMeta methodMeta) {
        for (MethodInvocationTree call : AstMethodTreeUtil.flattenChain(expr)) {
            String command = AstMethodTreeUtil.getMethodName(call);
            errorTracker.setMethodName(methodMeta.getMethodName());

            if (isDslCommand(command)) {
                processDslCommand(call, methodMeta);
            } else if (!command.equals("super")) {
                processSegmentCommand(call, repoElement, methodMeta);
            }
        }
    }

    private boolean isDslCommand(String command) {
        return DSL_KEYWORDS.contains(command) && !command.equals("segment");
    }

    private void processDslCommand(MethodInvocationTree call, MethodMeta methodMeta) {
        List<String> rawArgs = tokenExtractor.extract(call, mapParamRegistry);
        commandProcessor.process(AstMethodTreeUtil.getMethodName(call), rawArgs, methodMeta);
    }

    private void processSegmentCommand(MethodInvocationTree call, TypeElement repoElement, MethodMeta methodMeta) {
        try {
            List<String> passedArgs = tokenExtractor.extract(call, mapParamRegistry);
            String segmentClassName  = getSegmentClassName(repoElement, passedArgs);
            String segmentMethodName = passedArgs.get(1);
            List<String> segmentArgs = passedArgs.subList(2, passedArgs.size());

            LogPrinter.info("[PARSE] passedArgs=" + passedArgs);
            segmentInliner.inline(astContext, methodMeta, segmentArgs, segmentClassName, segmentMethodName);
        } catch (Exception e) {
            for (StackTraceElement ste : e.getStackTrace()) {
                LogPrinter.info("[STACK] " + ste.toString());
            }
            throw new IllegalArgumentException(e);
        }
    }

    // =========================================================================
    // Segment Class Resolution
    // =========================================================================

    private String getSegmentClassName(TypeElement repoElement, List<String> passedArgs) {
        String simpleName = passedArgs.get(0).replace(".class", "");
        String segmentFqn = findFqnFromImports(repoElement, simpleName);
        return !segmentFqn.isEmpty() ? segmentFqn : passedArgs.get(0);
    }

    private String findFqnFromImports(TypeElement repoElement, String simpleName) {
        TreePath path = trees.getPath(repoElement);
        CompilationUnitTree cu = path.getCompilationUnit();

        for (ImportTree imp : cu.getImports()) {
            String importStr = imp.getQualifiedIdentifier().toString();
            LogPrinter.info("[SEGMENT] " + importStr);
            if (importStr.endsWith("." + simpleName)) {
                return importStr;
            }
        }
        return "";
    }
}
