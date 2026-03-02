package io.jpm.core.m_entity.processor.handler.handlerContext;

import io.jpm.core.m_entity.generator.domain.vo.DDLTableMetadata;

import java.util.ArrayList;
import java.util.List;

public class DDLHandlerContext {
    private List<DDLTableMetadata> tables = new ArrayList<>();
    private String finalSql;
    public List<DDLTableMetadata> getTables() {
        return tables;
    }

    public void setTables(List<DDLTableMetadata> tables) {
        this.tables = tables;
    }

    public String getFinalSql() {
        return finalSql;
    }

    public void setFinalSql(String finalSql) {
        this.finalSql = finalSql;
    }
}
