package io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support;

import com.sun.source.tree.MethodInvocationTree;
import io.jpm.api.MqAssociation;
import io.jpm.api.MqCollection;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.BuildTimeMetadataCache;
import io.jpm.core.jpm_repository.domain.cache.MapParamRegistryImpl;
import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.domain.model.EntityMeta;
import io.jpm.core.jpm_repository.domain.model.MapJoinMeta;
import io.jpm.core.jpm_repository.domain.model.MethodMeta;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.utils.MethodRefUtil;
import io.jpm.core.jpm_repository.utils.ColumnResolver;
import io.jpm.core.jpm_repository.valid.policy.JoinNodeValidatorPolicyV2;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * DSL 커맨드(select, from, where, innerJoin, mapJoin …)를 받아
 * MethodMeta 에 DslStatement / MapJoinMeta 를 추가합니다.
 */
public class DslCommandProcessor {

    private static final List<String> JOIN_COMMANDS = Arrays.asList(
            "innerJoin", "leftJoin", "hashJoin", "mergeJoin", "loopJoin"
    );

    private static final List<String> TARGET_COMMANDS = Arrays.asList(
            "from", "insertInto", "update", "deleteFrom"
    );

    private final RepoMetaRegistry          repoMetaRegistry;
    private final ArgumentTokenExtractor tokenExtractor;
    private final JoinNodeValidatorPolicyV2 joinNodeValidator;

    public DslCommandProcessor(BuildTimeMetadataCache cache,
                               ErrorTracker errorTracker,
                               ArgumentTokenExtractor tokenExtractor) {
        this.repoMetaRegistry  = cache.getRepoMetaRegistry();
        this.tokenExtractor    = tokenExtractor;
        this.joinNodeValidator = new JoinNodeValidatorPolicyV2(
                cache, errorTracker, new ColumnResolver(cache.getRepoMetaRegistry())
        );
    }

    /** ParseMethodBodyStep 에서 호출 - 토큰 추출부터 처리까지 일괄 수행 */
    public void execute(MethodInvocationTree call,
                        MapParamRegistryImpl mapParamRegistry,
                        MethodMeta methodMeta) {
        List<String> rawArgs = tokenExtractor.extract(call, mapParamRegistry);
        LogPrinter.info("[call] rawArgs=" + rawArgs);
        process(AstMethodTree.getMethodName(call), rawArgs, methodMeta);
    }

    /** SegmentInlinerStep 에서 이미 추출된 args 로 직접 호출 */
    public void process(String command, List<String> rawArgs, MethodMeta methodMeta) {
        if ("mapJoin".equals(command)) {
            processMapJoin(rawArgs, methodMeta);
        } else if (JOIN_COMMANDS.contains(command)) {
            processJoin(command, rawArgs, methodMeta);
        } else {
            processDefault(command, rawArgs, methodMeta);
        }
    }

    // -------------------------------------------------------------------------
    // Private handlers
    // -------------------------------------------------------------------------

    private void processMapJoin(List<String> rawArgs, MethodMeta methodMeta) {
        if (rawArgs.isEmpty()) return;
        String raw       = rawArgs.get(0);
        String fieldName = MethodRefUtil.extractFieldName(raw);
        String alias     = rawArgs.size() > 1 ? rawArgs.get(1) : null;

        methodMeta.addMapJoin(new MapJoinMeta(fieldName, alias, resolveMappingType(raw, fieldName)));
        methodMeta.addStatement(new DslStatement("mapJoin", rawArgs));
    }

    private void processJoin(String command, List<String> rawArgs, MethodMeta methodMeta) {
        String arg0  = rawArgs.size() > 0 ? rawArgs.get(0) : "";
        String arg1  = rawArgs.size() > 1 ? rawArgs.get(1) : "";
        String arg2  = rawArgs.size() > 2 ? rawArgs.get(2) : "";
        String alias = arg2.contains("|") ? arg2.split("\\|")[0] : "";

        methodMeta.addStatement(new DslStatement(command, Arrays.asList(arg0, arg1, arg2, "", alias)));
    }

    private void processDefault(String command, List<String> rawArgs, MethodMeta methodMeta) {
        methodMeta.addStatement(new DslStatement(command, rawArgs));
        LogPrinter.info("[DslCommandProcStep] command=" + command + " rawArgs=" + rawArgs);

        if (TARGET_COMMANDS.contains(command) && !rawArgs.isEmpty()) {
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
            EntityMeta meta = repoMetaRegistry.getEntityMeta(classNamePart);
            if (meta == null) return MapJoinMeta.MappingType.AUTO;

            Class<?> entityClass = repoMetaRegistry.getEntityClass(classNamePart);
            Field field = entityClass.getDeclaredField(fieldName);

            if (field.isAnnotationPresent(MqCollection.class))  return MapJoinMeta.MappingType.COLLECTION;
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