package io.jpm.config.ast;

public interface Step<C extends  Context> {
    void execute(C context) throws Exception;
}

