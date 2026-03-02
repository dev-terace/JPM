package io.jpm.core.jpm_repository.parse.domain.vo;

import java.util.*;
/**
 * SQL 생성 한 사이클 동안 유지되는 빌드 컨텍스트.
 *
 * <p>기존의 모든 public 필드를 캡슐화하고,
 * 의미 있는 변이 메서드를 제공합니다.
 */
public class BuildContext {

    // ── 액션 & 컬럼 ────────────────────────────────────────────────
    private String action  = "";
    private String columns = "";

    // ── FROM / JOIN / WHERE ─────────────────────────────────────────
    private final Set<String>  tables     = new LinkedHashSet<>();
    private final List<String> joins      = new ArrayList<>();
    private final List<String> wheres     = new ArrayList<>();
    private final List<String> sets       = new ArrayList<>();
    private final List<String> insertCols = new ArrayList<>();
    private final List<String> insertVals = new ArrayList<>();

    // ── 정렬 / 페이징 ───────────────────────────────────────────────
    private final List<String> groupBys = new ArrayList<>();
    private final List<String> orderBys = new ArrayList<>();
    private String limit  = "";
    private String offset = "";

    // ── 테이블 별칭 ─────────────────────────────────────────────────
    private String tablePrefix = "";
    private final Map<String, String> tableAliases = new HashMap<>();

    /** JOIN 문이 하나라도 존재하면 컬럼에 별칭 접두어를 강제합니다. */
    private boolean requiresPrefix = false;

    // ── 생성자 ──────────────────────────────────────────────────────

    public BuildContext() {}

    public BuildContext(EntityMeta mainMeta) {
        this.tablePrefix = mainMeta.getTableName();
        registerAlias(mainMeta.getTableName(), mainMeta.getTableName());
    }

    // ── 별칭 관리 ────────────────────────────────────────────────────

    /** 테이블명(또는 클래스명) ↔ 별칭 양방향 등록 */
    public void registerAlias(String key, String alias) {
        tableAliases.put(key, alias);
    }

    public String resolveAlias(String tableOrClass) {
        return tableAliases.getOrDefault(tableOrClass, tableOrClass);
    }

    public boolean hasAlias(String key) {
        return tableAliases.containsKey(key);
    }

    // ── 뮤테이션 메서드 ──────────────────────────────────────────────

    public void setAction(String action)   { this.action  = action; }
    public void setColumns(String columns) { this.columns = columns; }
    public void setTablePrefix(String p)   { this.tablePrefix = p; }
    public void setLimit(String limit)     { this.limit  = limit; }
    public void setOffset(String offset)   { this.offset = offset; }
    public void markRequiresPrefix()       { this.requiresPrefix = true; }

    public void addTable(String table)     { tables.add(table); }
    public void addJoin(String join)       { joins.add(join); }
    public void addWhere(String cond)      { wheres.add(cond); }
    public void addSet(String expr)        { sets.add(expr); }
    public void addInsertCol(String col)   { insertCols.add(col); }
    public void addInsertVal(String val)   { insertVals.add(val); }
    public void addGroupBy(String expr)    { groupBys.add(expr); }
    public void addOrderBy(String expr)    { orderBys.add(expr); }

    // ── 읽기 전용 뷰 ─────────────────────────────────────────────────

    public String getAction()        { return action; }
    public String getColumns()       { return columns; }
    public String getTablePrefix()   { return tablePrefix; }
    public String getLimit()         { return limit; }
    public String getOffset()        { return offset; }
    public boolean isRequiresPrefix(){ return requiresPrefix; }

    public Set<String>  getTables()     { return tables; }
    public List<String> getJoins()      { return joins; }
    public List<String> getWheres()     { return wheres; }
    public List<String> getSets()       { return sets; }
    public List<String> getInsertCols() { return insertCols; }
    public List<String> getInsertVals() { return insertVals; }
    public List<String> getGroupBys()   { return groupBys; }
    public List<String> getOrderBys()   { return orderBys; }
    /**
     * 기존 SqlNode 구현체들의 직접 put() 호출과의 호환성을 위해 뮤터블 맵을 반환합니다.
     * 신규 코드에서는 {@link #registerAlias(String, String)}를 사용하세요.
     */
    public Map<String, String> getTableAliases() { return tableAliases; }
}