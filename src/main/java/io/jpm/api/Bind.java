package io.jpm.api;

import io.jpm.api.terrace_query.TerraceQuery;

public class Bind  implements IArg, TerraceQuery.SelectRawResultArg {
    public String val;
    public Object origin;
    public Bind(Object v) {
        this.origin = v;
        this.val = (v instanceof String) ? (String) v : "___BIND_REF___";
    }
    @Override public String toString() { return "#{" + val + "}"; }
}
