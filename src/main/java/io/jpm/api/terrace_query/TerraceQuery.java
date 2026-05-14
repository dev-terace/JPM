package io.jpm.api.terrace_query;


import io.jpm.api.*;
import io.jpm.core.jpm_repository.domain.model.DslStatementV2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class TerraceQuery {

    protected final List<DslStatementV2> statements = new ArrayList<>();


    public  List<DslStatementV2> getStatements()
    {
        return Collections.unmodifiableList(statements);
    }

    public Raw r(Object obj) { return new Raw(obj); }
    public  <E, R> Raw r(MFieldRef<E, R> fieldRef) { return new Raw(fieldRef); }

    public Quoted q(String val) { return new Quoted(val); }

    public Bind b(String val) { return new Bind(val); }
    public  <E, R> Bind b(MFieldRef<E, R> fieldRef) { return new Bind(fieldRef); }


    // 2. 사용자가 호출할 col 메서드 (정적 메서드로 선언하여 어디서든 사용)
    public  <E, R extends MField<?>> AliasedField<E, R> prefix(String prefix, MFieldRef<E, R> fieldRef) {
        return new AliasedField<>(prefix, fieldRef);
    }

    // ★ 1. 메서드 참조(::)를 받기 위한 함수형 인터페이스 정의
    @FunctionalInterface
    public interface MFieldRef<E, R> extends Function<E, R>, SelectRawResultArg, Serializable {


        @Override
        R apply(E entity);

        default R getField() {
            return apply(null);
        }


        default String describe() {
            try {
                java.lang.reflect.Method writeReplace =
                        this.getClass().getDeclaredMethod("writeReplace");
                writeReplace.setAccessible(true);
                java.lang.invoke.SerializedLambda sl =
                        (java.lang.invoke.SerializedLambda) writeReplace.invoke(this);

                String className = sl.getImplClass()
                        .replaceAll(".*/", ""); // 패키지 제거

                return className + "::" + sl.getImplMethodName();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }






    }


    public interface SelectRawResultArg {}

    public static class SelectRawResultObject implements SelectRawResultArg {

    }

    public  SelectRawResultObject sr(String obj, Class<?> classType){return new SelectRawResultObject();}
    public  <E, R extends MField<?>> MFieldRef<E, R> sr(MFieldRef<E, R> ref) {
        return ref;
    }
    public  SelectRawResultObject sr(IArg obj){return new SelectRawResultObject();}



    // =======================================================
    // --- Selectable 구현 ---
    // =======================================================


    // [수정] 조인된 다른 엔티티의 컬럼도 select 할 수 있도록 <?> 로 변경

    @SafeVarargs
    public final <E, R> void select(MFieldRef<E, R>... fieldRefs) {
        statements.add(new DslStatementV2("select", Arrays.asList(fieldRefs)));
    }

    public final void select(AliasedField<?, ?>... aliasedFields) {
        statements.add(new DslStatementV2("select", Arrays.asList(aliasedFields)));
    }

    public final void select(Object... obj) {
        statements.add(new DslStatementV2("select", Arrays.asList(obj)));
    }

    public  RawSelectBuilder selectRaw(String rawSqls)
    {
        return  new RawSelectBuilder(this, rawSqls);
    }
/*
    public void selectRaw(String rawSqls) {
        statements.add(new DslStatementV2("selectRaw", Collections.singletonList(rawSqls)));
    }

    @SafeVarargs
    public final <E, R> void selectRaw(String rawSqls, MFieldRef<E, R>... fieldRefs) {
        List<Object> args = new ArrayList<>();
        args.add(rawSqls);
        args.addAll(Arrays.asList(fieldRefs));
        statements.add(new DslStatementV2("selectRaw", args));
    }



    public final <E, R> void selectRaw(String rawSqls, AliasedField<?, ?>... aliasedFields) {
        List<Object> args = new ArrayList<>();
        args.add(rawSqls);
        args.addAll(Arrays.asList(aliasedFields));
        statements.add(new DslStatementV2("selectRaw", args));
    }

    public final void selectRawResult(String rawSqls, SelectRawResultArg... field) {
        List<Object> args = new ArrayList<>();
        args.add(rawSqls);
        args.addAll(Arrays.asList(field));
        statements.add(new DslStatementV2("selectRawResult", args));
    }
*/



    public void from(Class<?> entityClass) {
        statements.add(new DslStatementV2("from", Collections.singletonList(entityClass)));
    }

    public void from(String table) {
        statements.add(new DslStatementV2("from", Collections.singletonList(table)));
    }

    public void from(Class<?> entityClass, String alias) {
        statements.add(new DslStatementV2("from", Arrays.asList(entityClass, alias)));
    }

    public void from(Object table) {
        statements.add(new DslStatementV2("from", Collections.singletonList(table)));
    }

    // =======================================================
    // --- Conditional 구현 ---
    // =======================================================


    public void whereExistsGroup() {
        statements.add(new DslStatementV2("whereExistsGroup", null));

    }
    public void whereNotExistsGroup() {
        statements.add(new DslStatementV2("whereNotExistsGroup", null));
    }



    public WhereClause where(String cond) {
        statements.add(new DslStatementV2("where", Collections.singletonList(cond)));
        return new WhereClause();
    }

    public <E, R> WhereClause where(MFieldRef<E, R> fieldRef, String op, Object value) {
        statements.add(new DslStatementV2("where", Arrays.asList(fieldRef, op, value)));
        return new WhereClause();
    }

    public <E, R> WhereClause where(MFieldRef<E, R> fieldRef, String op, String value) {
        statements.add(new DslStatementV2("where", Arrays.asList(fieldRef, op, value)));
        return new WhereClause();
    }

    public WhereClause where(Object left, String op, Object right) {
        statements.add(new DslStatementV2("where", Arrays.asList(left, op, right)));
        return new WhereClause();
    }

    public WhereClause where(AliasedField<?, ?> col, String op, Object value) {
        statements.add(new DslStatementV2("where", Arrays.asList(col, op, value)));
        return new WhereClause();
    }


// ====== WhereClause 내부 클래스 - and/or만 존재 ======

    public class WhereClause {

        // MFieldRef + Object
        public <E, R> WhereClause and(MFieldRef<E, R> fieldRef, String op, Object value) {
            statements.add(new DslStatementV2("whereAnd", Arrays.asList(fieldRef, op, value)));
            return this;
        }
        public <E, R> WhereClause or(MFieldRef<E, R> fieldRef, String op, Object value) {
            statements.add(new DslStatementV2("whereOr", Arrays.asList(fieldRef, op, value)));
            return this;
        }

        // MFieldRef + String
        public <E, R> WhereClause and(MFieldRef<E, R> fieldRef, String op, String value) {
            statements.add(new DslStatementV2("whereAnd", Arrays.asList(fieldRef, op, value)));
            return this;
        }
        public <E, R> WhereClause or(MFieldRef<E, R> fieldRef, String op, String value) {
            statements.add(new DslStatementV2("whereOr", Arrays.asList(fieldRef, op, value)));
            return this;
        }

        // Object + Object
        public WhereClause and(Object left, String op, Object right) {
            statements.add(new DslStatementV2("whereAnd", Arrays.asList(left, op, right)));
            return this;
        }
        public WhereClause or(Object left, String op, Object right) {
            statements.add(new DslStatementV2("whereOr", Arrays.asList(left, op, right)));
            return this;
        }

        // AliasedField
        public WhereClause and(AliasedField<?, ?> col, String op, Object value) {
            statements.add(new DslStatementV2("whereAnd", Arrays.asList(col, op, value)));
            return this;
        }
        public WhereClause or(AliasedField<?, ?> col, String op, Object value) {
            statements.add(new DslStatementV2("whereOr", Arrays.asList(col, op, value)));
            return this;
        }
    }


    public void andGroup() {
        statements.add(new DslStatementV2("andGroup", null));
    }
    public void orGroup() {
        statements.add(new DslStatementV2("orGroup", null));
    }
    public void endGroup() {
        statements.add(new DslStatementV2("endGroup", null));
    }
    public void group() {
        statements.add(new DslStatementV2("group", null));
    }



    public <E, R> void whereInGroup(MFieldRef<E, R> fieldRef) {
        statements.add(new DslStatementV2("whereInGroup", Collections.singletonList(fieldRef)));
    }





    // [수정] 양쪽 모두 메서드 참조 지원 (N중 조인을 위해 <L, R> 분리)
    public <R,  LFR, RF, RFR> void innerJoin(Class<R> targetTable, MFieldRef<R, LFR> left, MFieldRef<RF, RFR> right) {
        statements.add(new DslStatementV2("innerJoin", Arrays.asList(targetTable, left, right)));
    }

    public <R, LFR, RF, RFR> void leftJoin(Class<R> targetTable, MFieldRef<R, LFR> left, MFieldRef<RF, RFR> right) {

        statements.add(new DslStatementV2("leftJoin", Arrays.asList(targetTable, left, right)));
    }


    public <R> void innerJoin(Class<R> targetTable, AliasedField<R, ?> left, AliasedField<?, ?> right) {
        statements.add(new DslStatementV2("innerJoin", Arrays.asList(targetTable, left, right)));
    }

    public <R> void leftJoin(Class<R> targetTable, AliasedField<R, ?> left, AliasedField<?, ?> right) {
        statements.add(new DslStatementV2("leftJoin", Arrays.asList(targetTable, left, right)));
    }


    public <R,  LFR, RF, RFR> void innerJoinGroup(Class<R> targetTable, MFieldRef<R, LFR> left, MFieldRef<RF, RFR> right) {
        statements.add(new DslStatementV2("innerJoinGroup", Arrays.asList(targetTable, left, right)));
    }

    public <R,  LFR, RF, RFR> void leftJoinGroup(Class<R> targetTable, MFieldRef<R, LFR> left, MFieldRef<RF, RFR> right) {
        statements.add(new DslStatementV2("leftJoinGroup", Arrays.asList(targetTable, left, right)));
    }

    public <R> void innerJoinGroup(Class<R> targetTable, AliasedField<R, ?> left, AliasedField<?, ?> right) {
        statements.add(new DslStatementV2("innerJoinGroup", Arrays.asList(targetTable, left, right)));
    }
    public <R> void leftJoinGroup(Class<R> targetTable, AliasedField<R, ?> left, AliasedField<?, ?> right) {
        statements.add(new DslStatementV2("leftJoinGroup", Arrays.asList(targetTable, left, right)));
    }



    public void innerJoin(Object targetTable, Object left, Object right) {
        statements.add(new DslStatementV2("innerJoin", Arrays.asList(targetTable, left, right)));
    }
    public void leftJoin(Object targetTable, Object left, Object right) {
        statements.add(new DslStatementV2("leftJoin", Arrays.asList(targetTable, left, right)));
    }

    public void innerJoinGroup(Object targetTable, Object left, Object right) {
        statements.add(new DslStatementV2("innerJoinGroup", Arrays.asList(targetTable, left, right)));
    }
    public void leftJoinGroup(Object targetTable, Object left, Object right) {
        statements.add(new DslStatementV2("leftJoinGroup", Arrays.asList(targetTable, left, right)));
    }





    // =======================================================
    // --- Modifiable 구현 ---
    // =======================================================
    public void insertInto(Class<?> entityClass) {
        statements.add(new DslStatementV2("insertInto", Collections.singletonList(entityClass)));
    }
    public void update(Class<?> entityClass) {
        statements.add(new DslStatementV2("update", Collections.singletonList(entityClass)));
    }
    public void deleteFrom(Class<?> entityClass) {
        statements.add(new DslStatementV2("deleteFrom", Collections.singletonList(entityClass)));
    }





    // [수정] UPDATE, INSERT는 대상 테이블(T)에 종속적이지만 확장성을 고려해 <E>로 엽니다.
    public <E, R> void value(MFieldRef<E, R> fieldRef, Object value) {
        statements.add(new DslStatementV2("value", Arrays.asList(fieldRef, value)));
    }
    public <E, R> void value(MFieldRef<E, R> fieldRef, String value) {
        statements.add(new DslStatementV2("value", Arrays.asList(fieldRef, value)));
    }


    public <E, R> void set(MFieldRef<E, R> fieldRef, Object value) {
        statements.add(new DslStatementV2("set", Arrays.asList(fieldRef, value)));
    }

    public void set(AliasedField<?, ?> col, Object value) {
        statements.add(new DslStatementV2("set", Arrays.asList(col, value)));
    }


    public <E, R> void set(MFieldRef<E, R> fieldRef, String value) {
        statements.add(new DslStatementV2("set", Arrays.asList(fieldRef, value)));
    }

    public void set(AliasedField<?, ?> col, String value) {
        statements.add(new DslStatementV2("set", Arrays.asList(col, value)));
    }

    public <E, R> void setRaw(MFieldRef<E, R> fieldRef, String rawSql) {
        statements.add(new DslStatementV2("setRaw", Arrays.asList(fieldRef, rawSql)));
    }


    // 1. GroupBy 세트
    @SafeVarargs
    public final <E, R> void groupBy(MFieldRef<E, R>... fieldRefs) {
        statements.add(new DslStatementV2("groupBy", Arrays.asList(fieldRefs)));
    }

    protected void groupByRaw(String... rawSqls) {
        statements.add(new DslStatementV2("groupByRaw", Arrays.asList(rawSqls)));
    }

    // 2. OrderBy 세트
    protected <E, R> void orderBy(MFieldRef<E, R> fieldRef, String direction) {
        statements.add(new DslStatementV2("orderBy", Arrays.asList(fieldRef, direction)));
    }


    public final void groupBy(AliasedField<?, ?>... aliasedFields) {
        statements.add(new DslStatementV2("groupBy", Arrays.asList(aliasedFields)));
    }

    public void orderBy(AliasedField<?, ?> col, String direction) {
        statements.add(new DslStatementV2("orderBy", Arrays.asList(col, direction)));
    }


    protected void orderByRaw(String... rawSqls) {
        statements.add(new DslStatementV2("orderByRaw", Arrays.asList(rawSqls)));
    }

    protected void limit(int count) {
        statements.add(new DslStatementV2("limit", Collections.singletonList(count)));
    }
    protected void offset(int start) {
        statements.add(new DslStatementV2("offset", Collections.singletonList(start)));
    }
    protected void sql(String rawSql) {
        statements.add(new DslStatementV2("sql", Collections.singletonList(rawSql)));
    }




    public void mapTarget(Class<?> targetClass) {
        statements.add(new DslStatementV2("mapTarget", Collections.singletonList(targetClass)));
    }


    public <E, R> void mapId(MFieldRef<E, R> fieldRef, String dbColumn) {
        statements.add(new DslStatementV2("mapId", Arrays.asList(fieldRef, dbColumn)));
    }

    /**
     * [result] 일반 필드 매핑 (컬럼명과 필드명이 다를 때 명시적으로 사용)
     * 예: mapResult(MEntity3::getName, "user_name")
     */
    public <E, R> void mapResult(MFieldRef<E, R> fieldRef, String dbColumn) {
        statements.add(new DslStatementV2("mapResult", Arrays.asList(fieldRef, dbColumn)));
    }



    public void collectionJoin(Class<?> clazz, String property, String alias) {
        statements.add(new DslStatementV2("collectionJoin", Arrays.asList(clazz, property, alias)));
    }
    public void associationJoin(Class<?> clazz, String property, String alias) {
        statements.add(new DslStatementV2("associationJoin", Arrays.asList(clazz, property, alias)));
    }


    // 문자열 기반 간단한 CASE 문
    public void selectCase(String condition, Object thenValue, Object elseValue, String alias) {
        statements.add(new DslStatementV2("selectCase", Arrays.asList(condition, thenValue, elseValue, alias)));
    }

    // 메서드 참조(::)를 활용한 타입 세이프 CASE 문
    public <E, R> void selectCase(MFieldRef<E, R> fieldRef, String op, Object condValue, Object thenValue, Object elseValue, String alias) {
        statements.add(new DslStatementV2("selectCase", Arrays.asList(fieldRef, op, condValue, thenValue, elseValue, alias)));
    }




    public <E, R> HavingClause having(MFieldRef<E, R> fieldRef, String op, Object value) {
        statements.add(new DslStatementV2("having", Arrays.asList(fieldRef, op, value)));
        return new HavingClause();
    }

    public HavingClause havingRaw(String rawSql) {
        statements.add(new DslStatementV2("havingRaw", Collections.singletonList(rawSql)));
        return new HavingClause();
    }


    public class HavingClause {

        public <E, R> HavingClause havingAnd(MFieldRef<E, R> fieldRef, String op, Object value) {
            statements.add(new DslStatementV2("havingAnd", Arrays.asList(fieldRef, op, value)));
            return this;
        }

        public <E, R> HavingClause havingOr(MFieldRef<E, R> fieldRef, String op, Object value) {
            statements.add(new DslStatementV2("havingOr", Arrays.asList(fieldRef, op, value)));
            return this;
        }
    }





    public void apply(Consumer<TerraceQuery> consumer) {
        consumer.accept(this);
    }

    @SafeVarargs
    public final void apply(Consumer<TerraceQuery>... consumers) {
        for (Consumer<TerraceQuery> c : consumers) {
            c.accept(this);
        }
    }

    public <E> void segment(
            Class<? extends E> type,
            Consumer<E> consumer
    ) {

        E instance = createInstance(type, this);
        consumer.accept(instance);
    }

    protected <E> E createInstance(Class<E> type, TerraceQuery q) {
        try {
            return type.getDeclaredConstructor(TerraceQuery.class).newInstance(q);
        } catch (NoSuchMethodException e) {
            // fallback (기존 방식)
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




}