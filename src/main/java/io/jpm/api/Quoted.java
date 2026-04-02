package io.jpm.api;

public class Quoted  implements IArg, JpmAbstractQuerySegment.SelectRawResultArg {
    public String val;
    public Object origin;
    public Quoted(Object v) {
        this.origin = v;
        this.val = (v instanceof String) ? (String) v : "___QUOTED_REF___";
    }
    @Override public String toString() { return "'" + val + "'"; }
}
