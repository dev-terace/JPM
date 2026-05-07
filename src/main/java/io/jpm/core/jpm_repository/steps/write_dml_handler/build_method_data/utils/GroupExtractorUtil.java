package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.utils;

import io.jpm.core.jpm_repository.domain.model.DslStatement;
import io.jpm.core.jpm_repository.domain.model.DslStatementV2;

import java.util.ArrayList;
import java.util.List;

/**
 * andGroup / orGroup / whereExistsGroup 등으로 시작해 endGroup 으로 닫히는
 * 서브 DSL 구문 목록을 추출하는 유틸리티입니다.
 */
public class GroupExtractorUtil {

    private GroupExtractorUtil() {}

    /**
     * {@code currentIndex} 위치의 GroupOpen 명령 이후부터 대응하는
     * {@code endGroup} 직전까지의 구문을 반환합니다.
     */
    public static List<DslStatement> extract(List<DslStatement> statements, int currentIndex) {
        List<DslStatement> group = new ArrayList<>();
        int depth = 1;
        for (int j = currentIndex + 1; j < statements.size(); j++) {
            DslStatement s = statements.get(j);
            if (isGroupOpen(s.getCommand()))          depth++;
            else if ("endGroup".equals(s.getCommand())) depth--;

            if (depth == 0) break;
            group.add(s);
        }
        return group;
    }


    public static List<DslStatementV2> extractV2(List<DslStatementV2> statements, int currentIndex) {
        List<DslStatementV2> group = new ArrayList<>();
        int depth = 1;
        for (int j = currentIndex + 1; j < statements.size(); j++) {
            DslStatementV2 s = statements.get(j);
            if (isGroupOpen(s.getCommand()))          depth++;
            else if ("endGroup".equals(s.getCommand())) depth--;

            if (depth == 0) break;
            group.add(s);
        }
        return group;
    }

    public static boolean isGroupOpen(String cmd) {
        return cmd.endsWith("Group") && !"endGroup".equals(cmd);
    }
}