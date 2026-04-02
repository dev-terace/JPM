package io.jpm.core.m_entity.dialect;

import io.jpm.api.m_field_type.MFieldType;
import io.jpm.api.m_field_type.MFieldTypeEnum;
import io.jpm.core.m_entity.generator.domain.vo.DDLColumnMetadata;
import io.jpm.core.m_entity.generator.domain.vo.DDLTableMetadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface SqlDialect {
    String getField(MFieldTypeEnum fieldType);

    List<String> createDropTableSql(DDLTableMetadata table);
    String createTableDefinitionSql(DDLTableMetadata table);
    List<String> createAlterTableSql(DDLTableMetadata table, DDLColumnMetadata column, HashMap<String, List<String>> parentFieldTypes); // FK 등 추가용
    String createAddColumnIfNotExistsSql(DDLTableMetadata table, DDLColumnMetadata col); //update policy
    default List<String> createIndexSql(DDLTableMetadata table) {
        List<String> sqls = new ArrayList<>();

        for (DDLColumnMetadata col : table.getColumns()) {
            // 인덱스 or 유니크 설정이 있으면 생성
            if (col.isContainIndexed() || col.isContainUnique()) {
                String indexName = "idx_" + table.getTableName() + "_" + col.getName();

                // UNIQUE 키워드 처리 (뒤에 공백 포함)
                String uniquePart = col.isContainUnique() ? "UNIQUE " : "";

                // [핵심 수정] INDEX 뒤에 'IF NOT EXISTS' 추가
                // 문법: CREATE [UNIQUE] INDEX IF NOT EXISTS [인덱스명] ON [테이블명] ([컬럼명])
                String sql = String.format("CREATE %sINDEX IF NOT EXISTS %s ON %s (%s)",
                        uniquePart, indexName, table.getTableName(), col.getName());

                sqls.add(sql);
            }
        }
        return sqls;
    }
}