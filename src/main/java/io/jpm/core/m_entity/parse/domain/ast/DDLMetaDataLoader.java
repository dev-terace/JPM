package io.jpm.core.m_entity.parse.domain.ast;

import io.jpm.core.m_entity.generator.domain.vo.DDLTableMetadata;
import javax.annotation.processing.RoundEnvironment;
import java.util.List;

public interface DDLMetaDataLoader {
    List<DDLTableMetadata> load(RoundEnvironment roundEnv);
}