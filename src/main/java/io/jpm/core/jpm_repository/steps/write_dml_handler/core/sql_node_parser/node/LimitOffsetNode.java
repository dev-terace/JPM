package io.jpm.core.jpm_repository.steps.write_dml_handler.core.sql_node_parser.node;


import io.jpm.core.jpm_repository.domain.model.BuildContext;

public class LimitOffsetNode implements SqlNode {
    private final String type;
    private final String value;

    public LimitOffsetNode(String type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public void apply(BuildContext ctx) {
        // 키워드 없이 값만 컨텍스트에 저장
        if ("LIMIT".equals(type)) ctx.setLimit(value);
        if ("OFFSET".equals(type)) ctx.setOffset(value);
    }

    @Override public String toSql(BuildContext ctx) { return ""; }
}
