package io.jpm.api;

import io.jpm.api.terrace_query.TerraceQuery;

public class Quoted  implements IArg, TerraceQuery.SelectRawResultArg {
    public String val;
    public Object origin;
    public Quoted(Object v) {
        this.origin = v;
        this.val = (v instanceof String) ? (String) v : "___QUOTED_REF___";
    }
    @Override public String toString() { return "'" + val + "'"; }
}
