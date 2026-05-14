package io.jpm.api.terrace_query;

import io.jpm.api.AliasedField;
import io.jpm.core.jpm_repository.domain.model.DslStatementV2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RawSelectBuilder {

    private final TerraceQuery query;
    private final List<Object> selectRawArgs = new ArrayList<>();
    private final List<FieldAsType> selectRawTypeArgs = new ArrayList<>();


    public RawSelectBuilder(TerraceQuery query, String rawSql) {
        this.query = query;
        selectRawArgs.add(rawSql);
    }

    public <E, R> RawSelectBuilder field(TerraceQuery.MFieldRef<E, R> ref) {
        selectRawArgs.add(ref);
        return this;
    }

    public RawSelectBuilder field(AliasedField<?, ?> ref) {
        selectRawArgs.add(ref);
        return this;
    }

    public RawSelectBuilder fieldAsType(String rawArg, Class<?> fieldType) {

        selectRawTypeArgs.add(new FieldAsType(rawArg, fieldType));
        return this;
    }

    public TerraceQuery done() {

        if(!selectRawArgs.isEmpty()) {
            query.statements.add(
                    new DslStatementV2("selectRaw", selectRawArgs)
            );
        }

        if(!selectRawTypeArgs.isEmpty()) {
            query.statements.add(new DslStatementV2("selectRawResult", Collections.singletonList(selectRawTypeArgs)));
        }

        return query;
    }
}
