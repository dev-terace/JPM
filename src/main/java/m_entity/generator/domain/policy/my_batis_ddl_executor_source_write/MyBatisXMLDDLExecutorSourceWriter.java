package m_entity.generator.domain.policy.my_batis_ddl_executor_source_write;

import m_entity.processor.MEntityAstProcessor;

public interface MyBatisXMLDDLExecutorSourceWriter {
    /**
     * Executor 자바 소스 파일을 생성합니다.
     * @param packageName 패키지명
     * @param className 클래스명
     */
    void write(String packageName, String className, MEntityAstProcessor.GeneratorCommand cmd);
}
