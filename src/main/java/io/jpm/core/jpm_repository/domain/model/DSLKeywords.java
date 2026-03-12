package io.jpm.core.jpm_repository.domain.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DSLKeywords {
    private static final Set<String> DSL_KEYWORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "select", "from", "where", "and", "or", "andGroup", "orGroup", "endGroup",
            "innerJoin", "leftJoin", "hashJoin", "mergeJoin", "loopJoin",
            "insertInto", "update", "deleteFrom", "value", "set", "setRaw",
            "orderBy", "groupBy", "limit", "offset", "sql", "selectRaw", "orderByRaw", "groupByRaw",
            "whereInGroup", "group", "fromGroup", "selectCase",
            "mapTarget", "mapId", "mapResult", "mapJoin", "innerJoinGroup", "leftJoinGroup",
            "whereExistsGroup", "whereNotExistsGroup"
            )));

    public static Set<String> getDSLKeywords()
    {
        return DSL_KEYWORDS;
    }
}
