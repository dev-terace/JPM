package io.jpm.core.m_entity.generator.domain.policy.ddl_script_builder;

import io.jpm.core.m_entity.dialect.SqlDialect;
import io.jpm.core.m_entity.generator.domain.vo.DDLColumnMetadata;
import io.jpm.core.m_entity.generator.domain.vo.DDLTableMetadata;
import io.jpm.common.utils.LogPrinter;

import java.util.*;
import java.util.stream.Collectors;


public class DDLForeignKeyGenerator {

    private final SqlDialect dialect;
    private final Map<String, DDLTableMetadata> tableLookup;

    public DDLForeignKeyGenerator(SqlDialect dialect, List<DDLTableMetadata> tables) {
        this.dialect = dialect;
        // 성능 최적화: 테이블 리스트를 Map으로 한 번만 변환 (Key: TableName)
        this.tableLookup = tables.stream()
                .collect(Collectors.toMap(
                        DDLTableMetadata::getTableName, // Key
                        t -> t,
                        (oldVal, newVal) -> oldVal,
                        HashMap::new
                ));
    }

    public void generate(StringBuilder sb) {
        sb.append("\t/* --- 3. FOREIGN KEYS --- */\n");
        for (DDLTableMetadata table : tableLookup.values()) {
            for (DDLColumnMetadata col : table.getColumns()) {

                // FK인 경우에만 로직 수행

                if (col.isForeignKey()) {

                    generateFkSql(sb, table, col);
                }
            }
        }
        sb.append("\n");
    }

    private void generateFkSql(StringBuilder sb, DDLTableMetadata currentTable, DDLColumnMetadata col) {
        // 1. 부모 테이블 이름 가져오기
        String parentName = col.getFkTargetTable(); // 혹은 getParentTableName() 상황에 맞춰 사용
        if (parentName == null) return;

        // 2. 부모 테이블의 PK 타입 조회
        List<String> parentPkTypes = findParentPkTypes(parentName);

        // 3. Dialect에 넘기기 위해 Map 형태로 포장 (기존 로직 유지)
        HashMap<String, List<String>> parentFieldTypes = new HashMap<>();
        parentFieldTypes.put(parentName, parentPkTypes);

        // 4. SQL 생성 및 추가
        List<String> sqls = dialect.createAlterTableSql(currentTable, col, parentFieldTypes);

        for (String sql : sqls) {
            appendStatement(sb, sql);
        }
    }

    private List<String> findParentPkTypes(String parentTableName) {
        DDLTableMetadata parentTable = tableLookup.get(parentTableName);

        // 방어 코드: 부모 테이블이 없는 경우 빈 리스트 반환
        if (parentTable == null) {
            LogPrinter.info("Parent table not found: " + parentTableName);
            return Collections.emptyList();
        }

        List<String> pkTypes = new ArrayList<>();
        for (DDLColumnMetadata parentCol : parentTable.getColumns()) {
            if (parentCol.isContainPrimaryKey()) {
                pkTypes.add(parentCol.getType());
            }
        }


        return pkTypes;
    }

    private void appendStatement(StringBuilder sb, String sql) {
        sb.append("\t").append(sql).append(";\n");
    }
}
