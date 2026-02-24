package mq_mapper.infra.ast;

import annotation.MqAssociation;
import annotation.MqCollection;
import mq_mapper.domain.vo.*;
import mq_mapper.infra.ast.utils.MethodRefUtil;
import mq_mapper.infra.repo.EntityMetaRegistry;
import utils.LogPrinter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DSL 커맨드(select, from, where, innerJoin, mapJoin …)를 받아
 * MethodMeta 에 DslStatement / MapJoinMeta 를 추가합니다.
 *
 * 기존 processDslCommand() 의 단일 책임 분리 버전입니다.
 */
public class DslCommandProcessor {

    private static final List<String> JOIN_COMMANDS =
            Arrays.asList("innerJoin", "leftJoin", "hashJoin", "mergeJoin", "loopJoin");

    private static final List<String> TARGET_COMMANDS =
            Arrays.asList("from", "insertInto", "update", "deleteFrom");

    private final EntityMetaRegistry entityMetaRegistry;

    public DslCommandProcessor(EntityMetaRegistry entityMetaRegistry) {
        this.entityMetaRegistry = entityMetaRegistry;
    }

    public void process(String command, List<String> rawArgs,
                        MethodMeta methodMeta, ArgContext argContext) {
        if ("mapJoin".equals(command)) {
            processMapJoin(rawArgs, methodMeta);
        } else if (JOIN_COMMANDS.contains(command)) {
            processJoin(command, rawArgs, methodMeta);
        } else {
            processDefault(command, rawArgs, methodMeta);
        }
    }

    // -------------------------------------------------------------------------

    private void processMapJoin(List<String> rawArgs, MethodMeta methodMeta) {
        if (rawArgs.isEmpty()) return;
        String raw       = rawArgs.get(0);
        String fieldName = MethodRefUtil.extractFieldName(raw);
        String alias     = rawArgs.size() > 1 ? rawArgs.get(1) : null;

        MapJoinMeta.MappingType mappingType = resolveMappingType(raw, fieldName);
        methodMeta.addMapJoin(new MapJoinMeta(fieldName, alias, mappingType));
        methodMeta.addStatement(new DslStatement("mapJoin", rawArgs));
    }

    private void processJoin(String command, List<String> rawArgs, MethodMeta methodMeta) {
        List<String> joinArgs = new ArrayList<>();
        for (int i = 0; i < 3 && i < rawArgs.size(); i++) joinArgs.add(rawArgs.get(i));

        String extractedAlias = "";
        if (rawArgs.size() > 2 && rawArgs.get(2).contains("|")) {
            extractedAlias = rawArgs.get(2).split("\\|")[0];
        }
        while (joinArgs.size() < 4) joinArgs.add("");
        joinArgs.add(extractedAlias);

        methodMeta.addStatement(new DslStatement(command, joinArgs));
    }

    private void processDefault(String command, List<String> rawArgs, MethodMeta methodMeta) {
        methodMeta.addStatement(new DslStatement(command, rawArgs));

        if (TARGET_COMMANDS.contains(command) && !rawArgs.isEmpty()) {
            LogPrinter.info("[DslCommandProcessor] command=" + command + " rawArgs=" + rawArgs);
            methodMeta.setTargetType(rawArgs.get(0).replace(".class", ""));
        } else if ("mapTarget".equals(command) && !rawArgs.isEmpty()) {
            methodMeta.setTargetType(rawArgs.get(0).replace(".class", ""));
        }
    }

    private MapJoinMeta.MappingType resolveMappingType(String raw, String fieldName) {
        String classNamePart = raw.contains("|")
                ? raw.split("\\|")[1].split("::")[0].trim()
                : raw.contains("::") ? raw.split("::")[0].trim() : null;

        if (classNamePart == null) return MapJoinMeta.MappingType.AUTO;

        try {
            EntityMeta meta = entityMetaRegistry.getEntityMeta(classNamePart);
            if (meta == null) return MapJoinMeta.MappingType.AUTO;

            Class<?> entityClass = entityMetaRegistry.getEntityClass(classNamePart);
            Field field = entityClass.getDeclaredField(fieldName);

            if (field.isAnnotationPresent(MqCollection.class))   return MapJoinMeta.MappingType.COLLECTION;
            if (field.isAnnotationPresent(MqAssociation.class)) return MapJoinMeta.MappingType.ASSOCIATION;

            return List.class.isAssignableFrom(field.getType())
                    ? MapJoinMeta.MappingType.COLLECTION
                    : MapJoinMeta.MappingType.ASSOCIATION;
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            return MapJoinMeta.MappingType.AUTO;
        }
    }
}