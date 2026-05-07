package io.jpm.api;

import io.jpm.core.jpm_repository.domain.cache.interfaces.RepoMetaRegistry;
import io.jpm.core.jpm_repository.domain.model.DslStatementV2;


import io.jpm.core.jpm_repository.runtime.config.AppConfig;

import io.jpm.core.jpm_repository.runtime.context.SqlMapBinderContextV2;

import io.jpm.core.jpm_repository.runtime.core.MurmurHash3;
import io.jpm.core.jpm_repository.runtime.core.ResultMapMetaV2;
import io.jpm.core.jpm_repository.runtime.core.SqlNodeParserStepsV2;


import io.jpm.core.jpm_repository.utils.ColumnResolver;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


// ResultMapping 추가 개발

public class TerraceQueryExecutor {

    private final SqlSessionFactory sqlSessionFactory;
    private static final RepoMetaRegistry repoMetaRegistry = AppConfig.getRepoMetaRegistry();

    public TerraceQueryExecutor(SqlSessionFactory sqlSessionFactory) throws IOException {
        this.sqlSessionFactory = sqlSessionFactory;



    }





    // =========================
    // SELECT ONE
    // =========================



    public <P, R, S extends TerraceQuery> R  selectOne(
            String id,
            Consumer<TerraceQuery> dsl,
            Class<P> parameter
    ) throws Exception {

        TerraceQuery terraceQuery = new TerraceQuery();

        dsl.accept(terraceQuery);

        String statementId = buildStatementId(terraceQuery, id);

        SqlNodeParserStepsV2 steps = new SqlNodeParserStepsV2(repoMetaRegistry);
      /*  ResultMapMetaV2.from(terraceQuery.getStatements());*/

        SqlMapBinderContextV2 binderContextV2 = new SqlMapBinderContextV2(terraceQuery.getStatements());

        steps.execute(binderContextV2);


       /* Configuration configuration = sqlSessionFactory.getConfiguration();

        SqlSource sqlSource = new SqlSourceBuilder(configuration)
                .parse(binderContextV2.getFinalSql(), parameter, new HashMap<>());

        List<ResultMapping> resultMappings = new ArrayList<>();
*/

      /*  MappedStatement ms = new MappedStatement.Builder(
                configuration,
                statementId,
                sqlSource,
                SqlCommandType.SELECT
        )
                .resultMaps(Collections.singletonList(resultMap))
                .build();


        if (!configuration.hasStatement(statementId)) {
            configuration.addMappedStatement(ms);
        }*/


        System.out.println("statementId: " + statementId);







        return null;
     /*   try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectOne(statementId, parameter);
        }*/


    }

    // =========================
    // SELECT LIST
    // =========================
    public <P, R, S extends TerraceQuery> List<R> selectList(
            String id,
            Consumer<TerraceQuery> dsl,
            P parameter
    ) {
        TerraceQuery terraceQuery = new TerraceQuery();

        dsl.accept(terraceQuery);

        String statementId = buildStatementId(terraceQuery, id);

        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList(statementId, parameter);
        }

    }


    // =========================
    // UPDATE
    // =========================

    public <P, S extends TerraceQuery> int update(
            String id,
            Consumer<TerraceQuery> dsl,
            P parameter
    ) {
        TerraceQuery terraceQuery = new TerraceQuery();
        dsl.accept(terraceQuery);

        String statementId = buildStatementId(terraceQuery, id);

        try (SqlSession session = sqlSessionFactory.openSession()) {
            int result = session.update(statementId, parameter);
            session.commit();
            return result;
        }
    }

    // =========================
    // Statement ID 생성
    // =========================

    private void addMappedStatement(String statementId,  String sql) {

        Configuration config = sqlSessionFactory.getConfiguration();

        if (config.hasStatement(statementId)) {
            return;
        }

        SqlSource sqlSource = new RawSqlSource(config, sql, Object.class);

        MappedStatement ms = new MappedStatement.Builder(config, statementId, sqlSource, SqlCommandType.SELECT)
                .resultMaps(new ArrayList<>()) // 나중에 자동매핑 붙이면 확장
                .build();

        config.addMappedStatement(ms);

    }



    private String buildStatementId(TerraceQuery query, String id) {



        List<DslStatementV2> statements = query.getStatements();


        System.out.println("statements: " + statements);
        String hash = MurmurHash3.hash64Hex(  statements.stream()
                        .map(Objects::toString)
                        .collect(Collectors.joining()));



        return id + "_" + hash;
    }
}