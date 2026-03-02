package io.jpm.core.jpm_repository.parse.infra.ast;

import com.sun.source.tree.*;
import io.jpm.core.jpm_repository.parse.infra.MapParamRegistry;
import io.jpm.core.jpm_repository.parse.domain.vo.EntityMeta;
import io.jpm.core.jpm_repository.parse.infra.utils.MethodRefUtil;
import io.jpm.core.jpm_repository.parse.domain.cache.RepoMetaRegistry;
import io.jpm.common.utils.LogPrinter;

import java.util.List;

/**
 * ExpressionTree 노드를 SQL 토큰(문자열)으로 변환합니다.
 * 기존 resolveValueTree() 의 단일 책임 분리 버전입니다.
 */
public class AstExpressionTreeValueResolver {

    private final RepoMetaRegistry repoMetaRegistry;

    public AstExpressionTreeValueResolver(RepoMetaRegistry repoMetaRegistry) {
        this.repoMetaRegistry = repoMetaRegistry;
    }

    public String resolve(ExpressionTree expr, MapParamRegistry mapParamRegistry,
                          boolean quoteString, boolean resolveToColumn) {
        if (expr == null) return "";
        try {
            if (expr instanceof MethodInvocationTree) {
                return resolveMethodInvocation((MethodInvocationTree) expr, mapParamRegistry, resolveToColumn);
            }
            if (expr instanceof LiteralTree) {
                return resolveLiteral((LiteralTree) expr, quoteString);
            }
            if (expr instanceof MemberReferenceTree) {
                return resolveMemberReference((MemberReferenceTree) expr, resolveToColumn);
            }
            if (expr instanceof IdentifierTree) {
                return resolveIdentifier((IdentifierTree) expr, mapParamRegistry);
            }
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
        }
        return expr.toString().replace("\"", "");
    }



    // -------------------------------------------------------------------------
    // private helpers
    // -------------------------------------------------------------------------

    private String resolveMethodInvocation(MethodInvocationTree mCall,
                                           MapParamRegistry mapParamRegistry, boolean resolveToColumn) {
        String name = AstMethodTreeUtil.getMethodName(mCall);
        List<? extends ExpressionTree> args = mCall.getArguments();

        switch (name) {
            case "raw":
                return args.isEmpty() ? "" : resolve(args.get(0), mapParamRegistry, false, resolveToColumn);

            case "quoted": {
                String inner = resolveInner(args, mapParamRegistry, resolveToColumn);
                return "'" + inner + "'";
            }
            case "bind": {
                String inner = resolveInner(args, mapParamRegistry, resolveToColumn);
                return "#{" + inner + "}";
            }
            case "as": {
                ExpressionTree methodSel = mCall.getMethodSelect();
                if (!(methodSel instanceof MemberSelectTree)) return "";
                ExpressionTree scope = ((MemberSelectTree) methodSel).getExpression();
                return resolve(scope, mapParamRegistry, false, true)
                        + " AS "
                        + resolve(args.get(0), mapParamRegistry, false, true);
            }
            case "col": {
                if (args.size() < 2) return "";
                String alias = resolve(args.get(0), mapParamRegistry, false, true);
                String field = resolve(args.get(1), mapParamRegistry, false, resolveToColumn);
                LogPrinter.info("[joinNode] alias=" + alias + " field=" + field);
                return alias + "." + field;
            }
            default:
                return "";
        }
    }

    private String resolveInner(java.util.List<? extends ExpressionTree> args,
                                MapParamRegistry mapParamRegistry, boolean resolveToColumn) {
        if (args.isEmpty()) return "";
        String inner = resolve(args.get(0), mapParamRegistry, false, true);
        if (inner.contains("::")) inner = MethodRefUtil.extractFieldName(inner);
        return inner;
    }

    private String resolveLiteral(LiteralTree lit, boolean quoteString) {
        Object val = lit.getValue();
        if (val instanceof Boolean) return ((Boolean) val) ? "TRUE" : "FALSE";
        if (val instanceof String)  return quoteString ? "'" + val + "'" : val.toString();
        return val != null ? val.toString() : "";
    }

    private String resolveMemberReference(MemberReferenceTree mRef, boolean resolveToColumn) {
        String className  = mRef.getQualifierExpression().toString();
        String methodName = mRef.getName().toString();
        String fieldName  = MethodRefUtil.convertGetterToField(methodName);

        if (!resolveToColumn) {
            return className + "::" + methodName;
        }

        EntityMeta entityMeta = repoMetaRegistry.getEntityMeta(className);
        if (entityMeta != null) {
            String columnName = entityMeta.getColumn(fieldName);
            return columnName != null ? columnName : fieldName;
        }
        return className + "::" + methodName;
    }

    private String resolveIdentifier(IdentifierTree idTree, MapParamRegistry mapParamRegistry) {
        String name = idTree.getName().toString();
        LogPrinter.info("[3] IdentifierTree name=" + name + " argContext keys=" + mapParamRegistry);

        if (mapParamRegistry.has(name))     return mapParamRegistry.get(name);
        if (mapParamRegistry.isParam(name)) return "#{" + name + "}";
        if (mapParamRegistry.hasAlias(name)) return mapParamRegistry.getAlias(name);
        return name;
    }
}