package io.jpm.api;

public class AliasedField<E, R extends MField<?>> implements TerraceQuery.SelectRawResultArg {

    public final String tableAlias;   // u1
    public final TerraceQuery.MFieldRef<E, R> fieldRef;


    public AliasedField(String tableAlias,
                        TerraceQuery.MFieldRef<E, R> fieldRef) {
        this.tableAlias = tableAlias;
        this.fieldRef = fieldRef;

    }

/*    public AliasedField<E, R> as(String selectAlias) {
        return new AliasedField<>(this.tableAlias, this.fieldRef, selectAlias);
    }*/
    public String describe() {
        return tableAlias + "." + fieldRef.describe();
    }

    @Override
    public String toString() {
        return describe();
    }

}
