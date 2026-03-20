package io.jpm.api;

public class Bind implements IArg {
    public String val;
    public Object origin;
    public Bind(Object v) {
        this.origin = v;
        this.val = (v instanceof String) ? (String) v : "___BIND_REF___";
    }
    @Override public String toString() { return "#{" + val + "}"; }
}
