package io.jpm.core.m_entity.generator.domain.policy.writer;

import java.io.IOException;

public interface DDLWriter {
    void write(String content) throws IOException;
}
