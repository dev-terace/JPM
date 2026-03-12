package io.jpm.config.ast;

public interface Step<C> {
    void execute(C context) throws Exception;
}

