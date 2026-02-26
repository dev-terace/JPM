package jpm_repository.parse.infra.ast;

import com.sun.source.tree.*;
import jpm_repository.parse.infra.MapParamRegistryImpl;
import jpm_repository.parse.domain.vo.EntityMeta;
import jpm_repository.parse.infra.utils.MethodRefUtil;
import jpm_repository.parse.domain.cache.RepoMetaRegistry;
import utils.LogPrinter;

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

    public String resolve(ExpressionTree expr, MapParamRegistryImpl mapParamRegistryImpl,
                          boolean quoteString, boolean resolveToColumn) {
        if (expr == null) return "";
        try {
            if (expr instanceof MethodInvocationTree) {
                return resolveMethodInvocation((MethodInvocationTree) expr, mapParamRegistryImpl, resolveToColumn);
            }
            if (expr instanceof LiteralTree) {
                return resolveLiteral((LiteralTree) expr, quoteString);
            }
            if (expr instanceof MemberReferenceTree) {
                return resolveMemberReference((MemberReferenceTree) expr, resolveToColumn);
            }
            if (expr instanceof IdentifierTree) {
                return resolveIdentifier((IdentifierTree) expr, mapParamRegistryImpl);
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
                                           MapParamRegistryImpl mapParamRegistryImpl, boolean resolveToColumn) {
        String name = AstMethodTreeUtil.getMethodName(mCall);
        List<? extends ExpressionTree> args = mCall.getArguments();

        switch (name) {
            case "raw":
                return args.isEmpty() ? "" : resolve(args.get(0), mapParamRegistryImpl, false, resolveToColumn);

            case "quoted": {
                String inner = resolveInner(args, mapParamRegistryImpl, resolveToColumn);
                return "'" + inner + "'";
            }
            case "bind": {
                String inner = resolveInner(args, mapParamRegistryImpl, resolveToColumn);
                return "#{" + inner + "}";
            }
            case "as": {
                ExpressionTree methodSel = mCall.getMethodSelect();
                if (!(methodSel instanceof MemberSelectTree)) return "";
                ExpressionTree scope = ((MemberSelectTree) methodSel).getExpression();
                return resolve(scope, mapParamRegistryImpl, false, true)
                        + " AS "
                        + resolve(args.get(0), mapParamRegistryImpl, false, true);
            }
            case "col": {
                if (args.size() < 2) return "";
                String alias = resolve(args.get(0), mapParamRegistryImpl, false, true);
                String field = resolve(args.get(1), mapParamRegistryImpl, false, resolveToColumn);
                LogPrinter.info("[joinNode] alias=" + alias + " field=" + field);
                return alias + "." + field;
            }
            default:
                return "";
        }
    }

    private String resolveInner(java.util.List<? extends ExpressionTree> args,
                                MapParamRegistryImpl mapParamRegistryImpl, boolean resolveToColumn) {
        if (args.isEmpty()) return "";
        String inner = resolve(args.get(0), mapParamRegistryImpl, false, true);
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

    private String resolveIdentifier(IdentifierTree idTree, MapParamRegistryImpl mapParamRegistryImpl) {
        String name = idTree.getName().toString();
        LogPrinter.info("[3] IdentifierTree name=" + name + " argContext keys=" + mapParamRegistryImpl);

        if (mapParamRegistryImpl.has(name))     return mapParamRegistryImpl.get(name);
        if (mapParamRegistryImpl.isParam(name)) return "#{" + name + "}";
        if (mapParamRegistryImpl.hasAlias(name)) return mapParamRegistryImpl.getAlias(name);
        return name;
    }
}