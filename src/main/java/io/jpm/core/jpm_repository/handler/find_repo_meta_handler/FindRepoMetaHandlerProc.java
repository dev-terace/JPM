package io.jpm.core.jpm_repository.handler.find_repo_meta_handler;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.AstContext;
import io.jpm.config.ast.GlobalRegistry;
import io.jpm.core.jpm_repository.domain.cache.MapParamRegistryImpl;
import io.jpm.core.jpm_repository.domain.model.DSLKeywords;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;
import io.jpm.core.jpm_repository.domain.model.RepoMeta;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.DslCommandProcessor;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.SegmentInliner;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.ArgumentTokenExtractor;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.AstMethodTree;

import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;

public class FindRepoMetaHandlerProc {

    /*private final AstContext astContext;
    private final MapParamRegistryImpl mapParamRegistryImpl;
    private final ArgumentTokenExtractor tokenExtractor;
    private final DslCommandProcessor commandProcessor;
    private final SegmentInliner segmentInliner;
    private final ErrorTracker errorTracker;
    private final Trees trees;
    private final Set<String> DSL_KEYWORDS;



    public FindRepoMetaHandlerProc(GlobalRegistry globalRegistry, AstContext astContext) {
        this.astContext       = astContext;
        this.mapParamRegistryImpl = globalRegistry.mapParamRegistry();
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

        }
    }

    // =========================================================================
    // Method Parsing
    // =========================================================================

    private MethodMeta parseMemberMethodProc(MethodTree methodTree, TypeElement repoElement) {
        MethodMeta methodMeta = new MethodMeta(methodTree.getName().toString());
        registerParams(methodTree, methodMeta);
        parseBody(methodTree.getBody(), repoElement, methodMeta);

        return methodMeta;
    }

    private void registerParams(MethodTree methodTree, MethodMeta methodMeta) {
        for (VariableTree param : methodTree.getParameters()) {
            String paramName = param.getName().toString();
            String paramType = param.getType().toString();
            methodMeta.addParameter(paramName, paramType);
            mapParamRegistryImpl.registerParam(paramName, paramType);
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
        for (MethodInvocationTree call : AstMethodTree.flattenChain(expr)) {
            String command = AstMethodTree.getMethodName(call);
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
        List<String> rawArgs = tokenExtractor.extract(call, mapParamRegistryImpl);



        LogPrinter.info("[call] rawARgs=" + rawArgs);
        commandProcessor.process(AstMethodTree.getMethodName(call), rawArgs, methodMeta);
    }

    private void processSegmentCommand(MethodInvocationTree call, TypeElement repoElement, MethodMeta methodMeta) {
        try {
            List<String> passedArgs = tokenExtractor.extract(call, mapParamRegistryImpl);
            String segmentClassName  = getSegmentClassName(repoElement, passedArgs);
            String segmentMethodName = passedArgs.get(1);
            List<String> segmentArgs = passedArgs.subList(2, passedArgs.size());

            LogPrinter.info("[PARSE] passedArgs=" + passedArgs);

            segmentInliner.inline(astContext, methodMeta, segmentArgs, segmentClassName, segmentMethodName);


            LogPrinter.info("[PARSE] methodMeta = " + methodMeta.toString());





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
        *//*    LogPrinter.info("[SEGMENT] " + importStr);*//*
            if (importStr.endsWith("." + simpleName)) {
                return importStr;
            }
        }
        return "";
    }*/
}
