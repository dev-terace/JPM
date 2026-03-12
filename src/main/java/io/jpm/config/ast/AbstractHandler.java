package io.jpm.config.ast;

public interface AbstractHandler<C>{

    void handle(C context) throws Exception;
}
