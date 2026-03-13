package io.jpm.core.jpm_repository.steps.write_dml_handler.build_method_data.core.sql_node_parser.build_sql_nodes.support.node;


import io.jpm.core.jpm_repository.domain.model.BuildContext;

// 1. 모든 SQL 처리 단위의 기본 인터페이스
public interface SqlNode {
    // Context를 받아 자신의 로직을 수행하고 Context를 업데이트함
    void apply(BuildContext ctx);
    String toSql(BuildContext ctx);



}