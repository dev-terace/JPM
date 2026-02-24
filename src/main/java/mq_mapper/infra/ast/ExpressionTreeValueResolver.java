package mq_mapper.infra.ast;

import com.sun.source.tree.*;
import mq_mapper.domain.vo.EntityMeta;
import mq_mapper.infra.ast.utils.MethodRefUtil;
import mq_mapper.infra.ast.utils.MethodTreeUtil;
import mq_mapper.infra.repo.EntityMetaRegistry;
import utils.LogPrinter;

import java.util.List;

/**
 * ExpressionTree 노드를 SQL 토큰(문자열)으로 변환합니다.
 * 기존 resolveValueTree() 의 단일 책임 분리 버전입니다.
 */
public class ExpressionTreeValueResolver {

    private final EntityMetaRegistry entityMetaRegistry;

    public ExpressionTreeValueResolver(EntityMetaRegistry entityMetaRegistry) {
        this.entityMetaRegistry = entityMetaRegistry;
    }

    public String resolve(ExpressionTree expr, ArgContext argContext,
                          boolean quoteString, boolean resolveToColumn) {
        if (expr == null) return "";
        try {
            if (expr instanceof MethodInvocationTree) {
                return resolveMethodInvocation((MethodInvocationTree) expr, argContext, resolveToColumn);
            }
            if (expr instanceof LiteralTree) {
                return resolveLiteral((LiteralTree) expr, quoteString);
            }
            if (expr instanceof MemberReferenceTree) {
                return resolveMemberReference((MemberReferenceTree) expr, resolveToColumn);
            }
            if (expr instanceof IdentifierTree) {
                return resolveIdentifier((IdentifierTree) expr, argContext);
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
                                           ArgContext argContext, boolean resolveToColumn) {
        String name = MethodTreeUtil.getMethodName(mCall);
        List<? extends ExpressionTree> args = mCall.getArguments();

        switch (name) {
            case "raw":
                return args.isEmpty() ? "" : resolve(args.get(0), argContext, false, resolveToColumn);

            case "quoted": {
                String inner = resolveInner(args, argContext, resolveToColumn);
                return "'" + inner + "'";
            }
            case "bind": {
                String inner = resolveInner(args, argContext, resolveToColumn);
                return "#{" + inner + "}";
            }
            case "as": {
                ExpressionTree methodSel = mCall.getMethodSelect();
                if (!(methodSel instanceof MemberSelectTree)) return "";
                ExpressionTree scope = ((MemberSelectTree) methodSel).getExpression();
                return resolve(scope, argContext, false, true)
                        + " AS "
                        + resolve(args.get(0), argContext, false, true);
            }
            case "col": {
                if (args.size() < 2) return "";
                String alias = resolve(args.get(0), argContext, false, true);
                String field = resolve(args.get(1), argContext, false, resolveToColumn);
                LogPrinter.info("[joinNode] alias=" + alias + " field=" + field);
                return alias + "." + field;
            }
            default:
                return "";
        }
    }

    private String resolveInner(java.util.List<? extends ExpressionTree> args,
                                ArgContext argContext, boolean resolveToColumn) {
        if (args.isEmpty()) return "";
        String inner = resolve(args.get(0), argContext, false, true);
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

        EntityMeta entityMeta = entityMetaRegistry.getEntityMeta(className);
        if (entityMeta != null) {
            String columnName = entityMeta.getColumn(fieldName);
            return columnName != null ? columnName : fieldName;
        }
        return className + "::" + methodName;
    }

    private String resolveIdentifier(IdentifierTree idTree, ArgContext argContext) {
        String name = idTree.getName().toString();
        LogPrinter.info("[3] IdentifierTree name=" + name + " argContext keys=" + argContext);

        if (argContext.has(name))     return argContext.get(name);
        if (argContext.isParam(name)) return "#{" + name + "}";
        if (argContext.hasAlias(name)) return argContext.getAlias(name);
        return name;
    }
}