package jpm_repository.parse.domain.policy.deprecated;

/**
 * DSL Statement 목록을 순회하여 SQL 문자열을 생성하는 바인더.
 *
 * 처리 흐름:
 * 1. Pre-scan: JOIN/FROM 선언을 미리 읽어 테이블 별칭 맵(tableAliases) 구성
 * 2. Main loop: Statement를 순회하며 각 SQL 절(SELECT/WHERE/JOIN …)을 빌드
 * 3. Assemble: 수집된 절을 하나의 SQL 문자열로 조립
 */

@Deprecated
public class SqlMapperBinderImpl  {

    // -------------------------------------------------------------------------
    // 내부 타입
    // -------------------------------------------------------------------------

   /* private static final EntityMetaRegistry entityMetaRegistry = AppConfig.getEntityMetaRegistry();

    *//** generateSql 한 번의 호출 동안 유지되는 빌드 컨텍스트. *//*
    public static class BuildContext {
        public String action  = "";
        public String columns = "";




        public final Set<String> tables = new LinkedHashSet<>();
        public final List<String> joins       = new ArrayList<>();
        public final List<String> wheres      = new ArrayList<>();
        public final List<String> sets        = new ArrayList<>();
        public final List<String> insertCols  = new ArrayList<>();
        public final List<String> insertVals  = new ArrayList<>();

        // 🚀 [신규 Node 방식 지원] 나중에 Node로 완전히 넘어가면 사용할 리스트들
        public final List<String> groupBys    = new ArrayList<>();
        public final List<String> orderBys    = new ArrayList<>();
        public String limit = "";
        public String offset = "";

        // 🔙 [기존 방식 지원] 빨간 줄(에러)을 없애기 위해 예전 필드들을 다시 살렸습니다!
        public String groupBy = "";
        public String orderBy = "";
        public String limitOffset = "";

        *//** 현재 유효한 테이블 별칭(또는 테이블명). WHERE/ORDER BY 접두어로 사용. *//*
        public String tablePrefix;
        *//** 테이블명/클래스명 ↔ 별칭 양방향 맵. *//*
        public final Map<String, String> tableAliases = new HashMap<>();
        *//** JOIN 문이 하나라도 있으면 컬럼에 별칭 접두어를 강제. *//*
        public boolean requiresPrefix = false;

        public BuildContext() {
            this.tablePrefix = "";
        }

        public BuildContext(EntityMeta mainMeta) {
            this.tablePrefix = mainMeta.getTableName();
            this.tableAliases.put(mainMeta.getTableName(), mainMeta.getTableName());
            // 메인 테이블을 FROM 절에 바로 추가
            //this.tables.add(mainMeta.getTableName());
        }
    }





    public String generateSqlFromStatements(List<DslStatement> statements, EntityMeta entityMeta) {
        // 1. 새로운 컨텍스트 생성 (서브쿼리용 독립 공간)
        BuildContext ctx = new BuildContext(entityMeta);

        // 2. 별칭 사전 스캔 (서브쿼리 내의 JOIN 별칭 등 파악)
        preScanAliases(statements, ctx);


        // 3. Statement -> Node 트리 변환
        List<SqlNode> nodes = parseToNodes(statements, ctx, entityMeta);

        // 4. 노드 실행 (BuildContext에 데이터 적재)
        for (SqlNode node : nodes) {
           *//* node.apply(ctx);*//*
        }

        // 5. 최종 조립
        return assembleSql(ctx);
    }




    public String generateSql(MethodMeta method, EntityMeta entityMeta) {
        try {
            List<DslStatement> statements = method.getStatements();
            BuildContext ctx = new BuildContext(entityMeta);

            // 1단계: 별칭 사전 스캔 (기존 유지)
            preScanAliases(statements, ctx);

            // 2단계: [수정] Statement를 Node 객체들로 변환 (Parsing)
            List<SqlNode> nodes = parseToNodes(statements, ctx, entityMeta);

            // 3단계: [수정] 생성된 노드들을 Context에 적용 (Execution)
            for (SqlNode node : nodes) {

                LogPrinter.info("[node 탐색: ]: " + node.getClass().getName());
                node.apply(ctx);
            }

            // 4단계: 최종 조립 (기존 유지)
            return assembleSql(ctx);
        }catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }






    // -------------------------------------------------------------------------
    // 1단계 – Pre-scan: 별칭 맵 구성
    // -------------------------------------------------------------------------

    *//** FROM / innerJoin / leftJoin 선언을 미리 읽어 tableAliases를 채운다. *//*
    private void preScanAliases(List<DslStatement> statements, BuildContext ctx) {
        try {
            int groupDepth = 0;
            for (int i = 0; i < statements.size(); i++) {
                DslStatement stmt = statements.get(i);
                String cmd = stmt.getCommand();

                if (groupDepth == 0) {
                    switch (cmd) {
                        case "from":
                            preScanFrom(stmt, ctx);
                            break;
                        case "innerJoin":
                        case "leftJoin":
                            preScanJoin(stmt, ctx);
                            break;
                        case "innerJoinGroup":
                        case "leftJoinGroup":
                            preScanJoinGroup(stmt, ctx);
                            break;
                    }

                    // 🚀 그룹 내부도 재귀 스캔 (whereExistsGroup, whereInGroup, fromGroup 등)
                    if (isGroupOpen(cmd)) {
                        List<DslStatement> subStmts = extractGroupStatements(statements, i);
                        preScanAliases(subStmts, ctx); // 내부 from/join 별칭 등록
                    }
                }

                if (isGroupOpen(cmd)) groupDepth++;
                else if ("endGroup".equals(cmd)) groupDepth--;
            }
        }catch (Exception e) {
            LogPrinter.exceptionInfo(e);
        }
    }

    // 🚀 새로 추가하는 메서드
    private void preScanJoinGroup(DslStatement stmt, BuildContext ctx) {
        try {
            if (stmt.getArgs().size() < 3) return;

            String rawClass = cleanClassName(stmt.getArgs().get(0));

            LogPrinter.info("[preScanJoinGroup] stmt args" + stmt);
            EntityMeta meta = entityMetaRegistry.getEntityMeta(rawClass);
            String actualTable = (meta != null) ? meta.getTableName() : rawClass;

            String explicitAlias = null;

            // 🚀 1. col() 방식 등으로 들어온 명시적 별칭 탐색
            // 보통 3번째 인자(Index 2)가 rightCol(서브쿼리 쪽 조건)입니다.
            String rightColArg = stmt.getArgs().get(2);

            if (rightColArg.contains("|")) {
                // col() 내부 구현이 "alias|MEntity::field" 형태의 문자열을 반환한다고 가정
                explicitAlias = rightColArg.split("\\|")[0];
            } else if (rightColArg.contains(".") && !rightColArg.contains("::")) {
                // alias.field 형태일 경우
                explicitAlias = rightColArg.split("\\.")[0];
            } else {
                // 파라미터 중에 별도의 String으로 별칭을 넘겼을 경우 탐색
                for (int i = 1; i < stmt.getArgs().size(); i++) {
                    String argStr = stmt.getArgs().get(i);
                    // 메서드 레퍼런스(::), 클래스(.class), 람다 등은 제외하고 순수 문자열 찾기
                    if (!argStr.contains("::") && !argStr.endsWith(".class") && !argStr.contains("->")) {
                        explicitAlias = argStr;
                        break;
                    }
                }
            }

            // 🚀 2. 별칭 확정: 명시된 별칭이 있으면 사용, 없으면 "테이블명_sub" 기본값 적용
            String finalAlias = (explicitAlias != null && !explicitAlias.trim().isEmpty())
                    ? explicitAlias
                    : actualTable + "_sub";

            // 3. SELECT 절 등에서 쓸 수 있도록 맵에 등록
            ctx.tableAliases.put(actualTable, finalAlias);
            ctx.tableAliases.put(rawClass, finalAlias);
            ctx.tableAliases.put(finalAlias, actualTable);
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }

    private void preScanFrom(DslStatement stmt, BuildContext ctx) {
        try {
            if (stmt.getArgs().isEmpty()) return;
            String rawTable = cleanClassName(stmt.getArgs().get(0));


            EntityMeta meta = entityMetaRegistry.getEntityMeta(rawTable);
            String actualTable = (meta != null) ? meta.getTableName() : rawTable;
            String alias = stmt.getArgs().size() >= 2 ? stmt.getArgs().get(1) : actualTable;

            System.out.println("[preScanFrom] rawTable=" + rawTable + " actualTable=" + actualTable + " alias=" + alias);

            ctx.tableAliases.put(actualTable, alias);   // order_items -> order_items
            ctx.tableAliases.put(rawTable, alias);       // OrderItemEntity -> order_items
            ctx.tableAliases.put(alias, actualTable);
        }catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }

    private void preScanJoin(DslStatement stmt, BuildContext ctx) {
        try {
            if (stmt.getArgs().size() < 3) return;

            String rawClass = cleanClassName(stmt.getArgs().get(0));
            LogPrinter.info("[preScanJoin] rawClass=" + rawClass);
            EntityMeta meta = entityMetaRegistry.getEntityMeta(rawClass);
            String actualTable = (meta != null) ? entityMetaRegistry.getTable(meta.getTableName()) : rawClass;

            String explicitAlias = null;
            String rightColArg = stmt.getArgs().get(2);

            // 🚀 1. col() 방식이나 명시적 별칭 탐색 ("alias|...", "alias.field")
            if (rightColArg.contains("|")) {
                explicitAlias = rightColArg.split("\\|")[0];
            } else if (rightColArg.contains(".") && !rightColArg.contains("::")) {
                explicitAlias = rightColArg.split("\\.")[0];
            }

            // 🚀 2. 일반 조인은 별칭이 없으면 '테이블명'을 그대로 기본값으로 사용! (핵심)
            String finalAlias = (explicitAlias != null && !explicitAlias.trim().isEmpty())
                    ? explicitAlias
                    : actualTable;

            // 3. SELECT 절에서 쓸 수 있게 맵에 등록
            ctx.tableAliases.put(actualTable, finalAlias);
            ctx.tableAliases.put(rawClass, finalAlias);
            ctx.tableAliases.put(finalAlias, actualTable);
        }catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }








    private List<SqlNode> parseToNodes(List<DslStatement> statements, BuildContext ctx, EntityMeta entityMeta) {
        try {
            List<SqlNode> nodes = new ArrayList<>();


            for (DslStatement stmt : statements) {
                String cmd = stmt.getCommand();
                if (cmd.contains("Join") || cmd.equals("whereExistsGroup") || cmd.equals("whereNotExistsGroup")) {
                    ctx.requiresPrefix = true;
                    break;
                }
            }


            // WHERE 절은 여러 조건이 묶여야 하므로 별도의 컨테이너 노드를 사용합니다.
            WhereClauseNode whereClause = new WhereClauseNode();

            for (int i = 0; i < statements.size(); i++) {
                DslStatement stmt = statements.get(i);
                String cmd = stmt.getCommand();

                // 🚀 [특수 처리] mapJoin은 기존처럼 Context의 별칭 맵을 먼저 채워야 하므로 유지합니다.
                if ("mapJoin".equals(cmd)) {
                    handleMapJoin(stmt, ctx, entityMeta);
                    continue;
                }

                // 인자 해석 (Java 필드 -> DB 컬럼 변환 준비)
                List<Object> resolvedArgs = resolveArgs(stmt.getArgs(), entityMeta, ctx);

                LogPrinter.info("[parseToNodes] resolvedArgs=" + resolvedArgs);
                List<String> args = toStringList(resolvedArgs);
                LogPrinter.info("[parseToNodes] args=" + args);

                switch (cmd) {
                    // ── SELECT ──────────────────────────────────────────────────
                    case "select":
                    case "selectRaw":
                        nodes.add(new SelectNode(stmt.getArgs()));
                        break;

                    // ── FROM ────────────────────────────────────────────────────
                    case "from":
                        // 🚀 테이블명 변환 적용 (Index 0이 테이블명 자리)
                        args.set(0, resolveTableName(cleanClassName(args.get(0))));
                        LogPrinter.info("[parseToNodes] from args=" + args);
                        nodes.add(new TableNode(args));
                        break;

                    case "fromGroup": {
                        List<DslStatement> sub = extractGroupStatements(statements, i);
                        i += sub.size() + 1;
                        nodes.add(new FromSubQueryNode(stmt, sub, entityMeta));
                        break;
                    }

                    // ── JOIN ─────────────────────────────────────────────────────
                    case "innerJoin":
                    case "leftJoin":
                        // 🚀 테이블명 변환 적용 (Index 0이 조인할 테이블명 자리)
                        args.set(0, args.get(0));
                        nodes.add(new JoinNode(cmd, stmt.getArgs()));
                        break;

                    case "innerJoinGroup":
                    case "leftJoinGroup": {
                        List<DslStatement> sub = extractGroupStatements(statements, i);
                        i += sub.size() + 1;
                        nodes.add(new JoinGroupNode(cmd, args, sub, entityMeta));
                        break;
                    }

                    // ── WHERE ──────────────────────────────────────────────────
                    case "where":
                    case "and":
                        // stmt.getArgs().get(0)은 "OrderEntity::getIsDeleted" 같은 원본 문자열입니다.
                        // args.get(2)는 이미 컬럼명으로 변환된 값일 수 있으니, 원본 값(stmt.getArgs().get(2))을 사용하는 것이 안전합니다.
                        String rawValue = stmt.getArgs().size() > 2 ? stmt.getArgs().get(2) : args.get(2);

                        whereClause.addCondition(new ConditionNode(
                                "AND",
                                stmt.getArgs().get(0),
                                args.get(1),
                                resolveSqlValue(stmt.getArgs().get(0), rawValue, ctx) // 👈 좌항의 메타데이터를 참조하여 우항 포맷팅
                        ));
                        break;


                    case "or":
                        whereClause.addCondition(new ConditionNode(
                                "OR", stmt.getArgs().get(0), args.get(1),
                                // 🚀 [수정]
                                resolveSqlValue(stmt.getArgs().get(0), args.get(2), ctx)
                        ));
                        break;

                    case "andGroup":
                    case "orGroup":
                    case "group": {
                        List<DslStatement> sub = extractGroupStatements(statements, i);
                        i += sub.size() + 1;
                        GroupType type = cmd.startsWith("or") ? GroupType.OR : GroupType.AND;
                        whereClause.addGroup(parseToGroupNode(sub, type, ctx, entityMeta));
                        break;
                    }

                    case "whereExistsGroup":
                    case "whereNotExistsGroup": {
                        List<DslStatement> sub = extractGroupStatements(statements, i);
                        i += sub.size() + 1;
                        ctx.requiresPrefix = true; // 🚀 EXISTS가 있으면 접두어 강제
                        whereClause.addCondition(new ExistsNode(cmd, sub, entityMeta));
                        break;
                    }

                    // ── DML ─────────────────────────────────────────────────────
                    case "update":
                        nodes.add(new ActionNode("UPDATE"));
                        break;
                    case "deleteFrom":
                        nodes.add(new ActionNode("DELETE"));
                        break;
                    case "insertInto":
                        // 🚀 INSERT 문 테이블명 변환 적용
                        *//* args.set(0, resolveTableName(cleanClassName(args.get(0))));*//*
                        nodes.add(new InsertNode(args));
                        break;

                    case "set":
                    case "setRaw":
                        // 🚀 [수정] UPDATE의 SET 구문도 동일하게 처리
                        nodes.add(new SetNode(stmt.getArgs().get(0), resolveSqlValue(stmt.getArgs().get(0), args.get(1), ctx)));
                        break;

                    case "value":
                        // 🚀 [수정] INSERT 문 등
                        nodes.add(new ValueNode(stmt.getArgs().get(0), resolveSqlValue(stmt.getArgs().get(0), args.get(1), ctx)));
                        break;

                    // ── 기타 (Sort, Limit 등) ──────────────────────────────────
                    case "groupBy":
                        nodes.add(new GroupByNode(stmt.getArgs()));
                        break;
                    case "orderBy":
                        nodes.add(new OrderByNode(stmt.getArgs()));
                        break;
                    case "limit":
                        nodes.add(new LimitOffsetNode("LIMIT", args.get(0)));
                        break;
                    case "offset":
                        nodes.add(new LimitOffsetNode("OFFSET", args.get(0)));
                        break;
                }
            }

            // 모든 구문 해석 후, WHERE 절에 조건이 있다면 전체 노드 리스트에 추가합니다.
            if (!whereClause.isEmpty()) {
                nodes.add(whereClause);
            }

            return nodes;
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }

    *//**
     * 헬퍼 메서드: 서브 그룹(GroupNode) 내부를 재귀적으로 파싱합니다.
     *//*
    private GroupNode parseToGroupNode(List<DslStatement> subStatements, GroupType type, BuildContext ctx, EntityMeta entityMeta) {
        try {
            GroupNode group = new GroupNode(type);
            for (int j = 0; j < subStatements.size(); j++) {
                DslStatement s = subStatements.get(j);
                List<String> args = s.getArgs().stream().map(arg -> ColumnResolver.resolve(arg, ctx)).collect(Collectors.toList());

                *//*toStringList(resolveArgs(s.getArgs(), entityMeta, ctx));*//*

                if (isGroupOpen(s.getCommand())) {
                    List<DslStatement> nested = extractGroupStatements(subStatements, j);
                    j += nested.size() + 1;
                    GroupType nestedType = s.getCommand().startsWith("or") ? GroupType.OR : GroupType.AND;
                    group.add(parseToGroupNode(nested, nestedType, ctx, entityMeta));
                } else {
                    // 🚀 [수정] 명령어가 "or"로 시작하면 OR, 아니면 AND로 처리
                    String logic = s.getCommand().equalsIgnoreCase("or") ? "OR" : "AND";

                    // 🚀 [수정] 4개의 인자를 전달 (logic, column, operator, value)
                    if (args.size() >= 3) {
                        group.add(new ConditionNode(logic, args.get(0), args.get(1), args.get(2)));
                    }
                    // 참고: isNull 같은 인자가 적은 명령어를 쓰신다면 별도 처리가 필요할 수 있습니다.
                }
            }
            return group;
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }


    // -------------------------------------------------------------------------
    // 핸들러 메서드
    // -------------------------------------------------------------------------




    private void handleMapJoin(DslStatement stmt, BuildContext ctx, EntityMeta entityMeta) {
        try {
            String rawArg = stmt.getArgs().get(0);
            String fieldName = extractFieldName(rawArg.contains("::") ? rawArg.split("::")[1] : rawArg);
            String rightAlias = stmt.getArgs().size() > 1 ? stmt.getArgs().get(1)
                    : "mj" + ctx.joins.size();

            EntityMeta targetMeta = entityMeta.getRelationTargetMeta(fieldName);
            if (targetMeta == null) {
                System.err.println("🚨 [WARNING] mapJoin 타겟 엔티티 메타를 찾을 수 없습니다: field=" + fieldName);
                return;
            }

            String targetTable = targetMeta.getTableName();
            ctx.tableAliases.put(targetTable, rightAlias);

            if (rawArg.contains("::")) {
                LogPrinter.info("[handleMapJoin] : " + rawArg.split("::")[0]);
                EntityMeta parentMeta = entityMetaRegistry.getEntityMeta(rawArg.split("::")[0]);
                if (parentMeta != null && ctx.tableAliases.containsKey(parentMeta.getTableName())) {
                    ctx.tablePrefix = ctx.tableAliases.get(parentMeta.getTableName());
                }
            }
        }catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }













    private String assembleSql(BuildContext ctx) {
        StringBuilder sql = new StringBuilder();
        String actionType = ctx.action.isEmpty() ? "SELECT" : ctx.action;

        switch (actionType) {
            case "SELECT":
                // 1. SELECT & FROM
                String fromClause = ctx.tables.isEmpty()
                        ? ctx.tablePrefix
                        : String.join(", ", ctx.tables);

                sql.append("SELECT ").append(ctx.columns.isEmpty() ? "*" : ctx.columns)
                        .append("\nFROM ").append(fromClause);

                // 2. JOIN
                for (String join : ctx.joins) {
                    sql.append("\n").append(join);
                }

                // 3. WHERE (WhereClauseNode가 생성한 쿼리들을 AND로 묶어줌)
                if (!ctx.wheres.isEmpty()) {
                    sql.append("\nWHERE ").append(String.join(" AND ", ctx.wheres));
                }

                // 4. GROUP BY (신규 List 방식 적용 + 하위 호환성 유지)
                if (!ctx.groupBys.isEmpty()) {
                    sql.append("\nGROUP BY ").append(String.join(", ", ctx.groupBys));
                } else if (ctx.groupBy != null && !ctx.groupBy.isEmpty()) {
                    sql.append(ctx.groupBy);
                }

                // 5. ORDER BY (신규 List 방식 적용 + 하위 호환성 유지)
                if (!ctx.orderBys.isEmpty()) {
                    sql.append("\nORDER BY ").append(String.join(", ", ctx.orderBys));
                } else if (ctx.orderBy != null && !ctx.orderBy.isEmpty()) {
                    sql.append(ctx.orderBy);
                }

                // 6. LIMIT & OFFSET (신규 분리 방식 적용 + 하위 호환성 유지)
                if (ctx.limit != null && !ctx.limit.isEmpty()) {
                    sql.append("\nLIMIT ").append(ctx.limit);
                }
                if (ctx.offset != null && !ctx.offset.isEmpty()) {
                    sql.append("\nOFFSET ").append(ctx.offset);
                }
                if (ctx.limitOffset != null && !ctx.limitOffset.isEmpty()) {
                    sql.append(ctx.limitOffset);
                }
                break;

            case "UPDATE":
                sql.append("UPDATE ").append(ctx.tablePrefix).append("\nSET ")
                        .append(String.join(", ", ctx.sets));

                if (!ctx.wheres.isEmpty()) {
                    sql.append("\nWHERE ").append(String.join(" AND ", ctx.wheres));
                }
                break;

            case "DELETE":
                sql.append("DELETE FROM ").append(ctx.tablePrefix);

                if (!ctx.wheres.isEmpty()) {
                    sql.append("\nWHERE ").append(String.join(" AND ", ctx.wheres));
                }
                break;

            case "INSERT":


                String cols = ctx.insertCols.isEmpty() ? (ctx.columns == null ? "" : ctx.columns)
                        : "(" + String.join(", ", ctx.insertCols) + ")";
                String vals = "(" + String.join(", ", ctx.insertVals) + ")";

                sql.append("INSERT INTO ").append(ctx.tablePrefix).append(" ")
                        .append(cols).append("\nVALUES ").append(vals);
                break;
        }

        return sql.toString();
    }

    // -------------------------------------------------------------------------
    // 인자 해석 유틸리티
    // -------------------------------------------------------------------------

    private List<Object> resolveArgs(List<String> rawArgs, EntityMeta mainMeta, BuildContext ctx) {
        return rawArgs.stream().map(arg -> {
            if (arg != null) {
                return resolveArg(arg, mainMeta, ctx.tableAliases, ctx.requiresPrefix);
            }
            return arg;
        }).collect(Collectors.toList());
    }

    private String resolveArg(String arg, EntityMeta mainMeta,
                              Map<String, String> tableAliases, boolean requiresPrefix) {
        if (arg == null || arg.trim().isEmpty()) return arg;

        try {
            // 🚀 [버그 수정 1] SELECT 별칭(AS) 분리
            // 예: "o1::getId|main_order_id" 또는 "o1.id|main_order_id"
            String asAlias = "";
            int lastPipeIdx = arg.lastIndexOf('|');
            int doubleColonIdx = arg.indexOf("::");


            System.out.println("[resolveArg] arg=" + arg + " tableAliases=" + tableAliases);
            // '::' 이후에 '|'가 있거나, '::'가 없어도 마지막에 '|'가 있는 경우 별칭(AS)으로 판단
            if (lastPipeIdx > 0 && lastPipeIdx > doubleColonIdx) {
                asAlias = " AS " + arg.substring(lastPipeIdx + 1);
                arg = arg.substring(0, lastPipeIdx); // 별칭을 제외한 순수 인자만 남김
            }

            if (arg.contains("::")) {
                String[] parts = arg.split("::");
                String refObj = parts[0].trim();
                String fieldName = extractFieldName(parts[1].trim());

                // "alias|Entity" 형태인 경우 분리 (JOIN 시 주로 발생)
                String classNameForMeta = refObj;
                String explicitTableAlias = null;
                if (refObj.contains("|")) {
                    String[] refParts = refObj.split("\\|");
                    explicitTableAlias = refParts[0];
                    classNameForMeta = refParts[1];
                }

                EntityMeta targetMeta = "target".equals(classNameForMeta) ? mainMeta
                        : entityMetaRegistry.getEntityMeta(classNameForMeta);

                if (targetMeta != null) {
                    String columnName = targetMeta.getColumn(fieldName);
                    String finalCol = (columnName != null && !columnName.isEmpty())
                            ? columnName
                            : toSnakeCase(fieldName);

                    String tableName = targetMeta.getTableName();
                    // 명시적 테이블 별칭이 있으면 우선, 없으면 맵에서 가져오기
                    String alias = explicitTableAlias != null ? explicitTableAlias : tableAliases.getOrDefault(tableName, tableName);

                    if (!requiresPrefix && tableName.equals(mainMeta.getTableName()) && alias.equals(tableName)) {
                        return finalCol + asAlias;
                    }
                    return alias + "." + finalCol + asAlias;
                }

                // 메타가 없는 경우 Fallback
                if (explicitTableAlias != null) {
                    return explicitTableAlias + "." + toSnakeCase(fieldName) + asAlias;
                }
                return toSnakeCase(fieldName) + asAlias;
            }

            if (arg.contains(".") && !arg.contains("(")) { // 함수 호출 제외
                String[] parts = arg.split("\\.");
                if (parts.length == 2) {
                    String alias = parts[0];
                    String fieldName = parts[1];

*//*                    String actualTable = tableAliases.get(alias);
                    if (alias.matches("\\d+") || parts[1].matches("\\d+")) {
                        return arg + asAlias; // 상수/숫자 포함 시 그대로 반환
                    }*//*


                    if (alias != null) {


                  *//*  EntityMeta meta = entityMetaRegistry.getEntityMeta(actualTable);
                    if (meta != null) {*//*
                       *//* String dbCol = meta.getColumn(fieldName);
                        String finalCol = (dbCol != null) ? dbCol : toSnakeCase(fieldName);*//*
                        return alias + "." + fieldName;*//*alias + "." + finalCol + asAlias;*//*
                        *//*}*//*
                    }

                    String dbCol = mainMeta.getColumn(fieldName);
                    if (dbCol != null) {
                        return mainMeta.getTableName() + "." + dbCol + asAlias;
                    }

                    // 테이블 메타를 못 찾았을 때의 기본 스네이크 케이스 처리
                    return alias + "." + toSnakeCase(fieldName) + asAlias;
                }
            }

            return arg + asAlias;
        } catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException(e);
        }
    }


    private String toSnakeCase(String camel) {
        if (camel == null) return null;
        return camel.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }


    // -------------------------------------------------------------------------
    // 그룹(서브쿼리) 추출
    // -------------------------------------------------------------------------

    private List<DslStatement> extractGroupStatements(List<DslStatement> statements, int currentIndex) {
        List<DslStatement> group = new ArrayList<>();
        int depth = 1;
        for (int j = currentIndex + 1; j < statements.size(); j++) {
            DslStatement s = statements.get(j);
            if (isGroupOpen(s.getCommand()))       depth++;
            else if ("endGroup".equals(s.getCommand())) depth--;

            if (depth == 0) break;
            group.add(s);
        }
        return group;
    }

    // -------------------------------------------------------------------------
    // 소형 헬퍼
    // -------------------------------------------------------------------------

    *//** JOIN 문이 하나라도 있는지 확인 *//*


    *//** "endGroup"을 제외하고 Group으로 끝나는 명령인지 *//*
    private boolean isGroupOpen(String cmd) {
        return cmd.endsWith("Group") && !"endGroup".equals(cmd);
    }

    *//** ".class" 및 "class " 접두어 제거 *//*
    private String cleanClassName(String raw) {
        if (raw.startsWith("class ")) raw = raw.substring(raw.lastIndexOf('.') + 1);
        return raw.replace(".class", "");
    }

    *//** getter 이름에서 필드명 추출 (getXxx → xxx) *//*
    private String extractFieldName(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        }
        return methodName;
    }



    *//** Object 리스트 → String 리스트 변환 *//*
    private List<String> toStringList(List<Object> list) {
        return list.stream().map(Object::toString).collect(Collectors.toList());
    }



    // -------------------------------------------------------------------------
    // 값(Value) 포맷팅 유틸리티
    // -------------------------------------------------------------------------
// 🚀 기존 formatSqlValue를 대체하는 지능형 값 포맷터
    private String resolveSqlValue(String rawLeftArg, String resolvedVal, BuildContext ctx) {
        if (resolvedVal == null || resolvedVal.trim().isEmpty()) return "NULL";
        String val = resolvedVal.trim();

        if (val.contains("#{") || (val.startsWith("'") && val.endsWith("'"))) return val;


        System.out.println("rawLeftArg=" + rawLeftArg + " resolvedVal=" + resolvedVal);

        if (val.matches("-?\\d+(\\.\\d+)?")) return val;

        return val;
    }


    private String resolveTableName(String entityOrTableName) {
        if (entityOrTableName == null) return null;


        System.out.println("entityOrTableName=" + entityOrTableName);
        // 1. 엔티티 메타 관리자에서 클래스명으로 메타 정보 조회 (프로젝트 상황에 맞게 수정!)
        EntityMeta meta = entityMetaRegistry.getEntityMeta(entityOrTableName);

        // 2. 메타 정보가 존재하면 해당 테이블명 반환, 없으면 입력된 문자열 그대로 반환
        if (meta != null && meta.getTableName() != null) {
            System.out.println("meta get TAble Name=" + entityMetaRegistry.getTable(entityOrTableName));
            return entityMetaRegistry.getTable(entityOrTableName);
        }


        return entityOrTableName;
    }
*/





}