package io.jpm.api;


import io.jpm.api.terrace_query.TerraceQuery;


public class AliasedField<E, R extends MField<?>> implements TerraceQuery.SelectRawResultArg {

    public final String tableAlias;   // u1
    public final TerraceQuery.MFieldRef<E, R> fieldRef;


    public AliasedField(String tableAlias,
                        TerraceQuery.MFieldRef<E, R> fieldRef) {
        this.tableAlias = tableAlias;
        this.fieldRef = fieldRef;

    }


    public String describe() {
        return tableAlias + "." + fieldRef.describe();
    }

    @Override
    public String toString() {
        return describe();
    }

}
