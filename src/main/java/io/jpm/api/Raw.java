package io.jpm.api;

public class Raw implements IArg, JpmAbstractQuerySegment.SelectRawResultArg {
    public String val;
    public Object origin;
    public Raw(Object v) {
        this.origin = v;
        this.val = (v instanceof String) ? (String) v : "___RAW_REF___";
    }
    @Override public String toString() { return val; }
}
