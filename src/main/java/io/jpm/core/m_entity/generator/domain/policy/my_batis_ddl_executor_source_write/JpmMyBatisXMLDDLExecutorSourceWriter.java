package io.jpm.core.m_entity.generator.domain.policy.my_batis_ddl_executor_source_write;

import io.jpm.core.m_entity.processor.MEntityAstProcessor;

import javax.annotation.processing.FilerException; // 🔥 핵심: 중복 생성 에러 처리를 위해 필요
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;

public class JpmMyBatisXMLDDLExecutorSourceWriter implements MyBatisXMLDDLExecutorSourceWriter {

    private final ProcessingEnvironment processingEnv;

    public JpmMyBatisXMLDDLExecutorSourceWriter(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    @Override
    public void write(String packageName, String className, MEntityAstProcessor.GeneratorCommand cmd) {
        String fullClassName = packageName + "." + className;

        try {
            // 🔥 파일 생성 시도
            JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(fullClassName);

            // 성공 시 Writer 열고 작성
            try (PrintWriter out = new PrintWriter(fileObject.openWriter())) {

                // 1. 패키지 & 임포트
                writePackageAndImports(out, packageName);

                // 2. 클래스 시작
                out.println("public class " + className + " {");

                // 3. 필드, 내부클래스, 생성자, 메서드들
                writeFields(out);
                writeInnerClasses(out);
                writeConstructor(out, className, cmd);
                writeRunMethod(out);
                writeDynamicExecutorMethod(out);
                writeSqlSessionExecutorMethod(out);

                // 4. 클래스 끝
                out.println("}");
            }

        } catch (FilerException e) {
            // 💡 [버그 해결] 이미 파일이 존재하면 여기서 잡힙니다.
            // 에러를 던지지 않고 "이미 있으니 넘어간다"는 로그만 남기고 정상 종료합니다.
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "⚠️ [SKIP] Executor 소스 파일이 이미 존재하므로 생성을 건너뜁니다: " + fullClassName);

        } catch (IOException e) {
            // 진짜 IO 에러(디스크 용량 부족 등)는 던져야 함
            throw new RuntimeException("Executor 소스 생성 실패: " + e.getMessage(), e);
        }
    }

    private void writePackageAndImports(PrintWriter out, String packageName) {
        out.println("package " + packageName + ";");
        out.println();
        out.println("import org.apache.ibatis.datasource.pooled.PooledDataSource;");
        out.println("import org.apache.ibatis.mapping.*;");
        out.println("import org.apache.ibatis.session.*;");
        out.println("import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;");
        out.println("import org.apache.ibatis.builder.xml.XMLMapperBuilder;");
        out.println("import org.apache.ibatis.builder.StaticSqlSource;");
        out.println("import org.apache.ibatis.io.Resources;");

        // 유틸 및 IO
        out.println("import java.util.Properties;");
        out.println("import java.io.InputStream;");
        out.println("import java.io.IOException;");
        out.println();
    }

    private void writeFields(PrintWriter out) {
        out.println("    private final SqlSessionFactory factory;");
        out.println();
    }

    private void writeInnerClasses(PrintWriter out) {
        out.println("    public static class Command {");
        out.println("        public String dynamicMode;");
        out.println("        public String xmlPath;");
        out.println("        public String namespace;");
        out.println("        public String id;");
        out.println("        public SqlCommandType type;");
        out.println("        public String sql;");
        out.println("    }");
        out.println();
    }

    // 🔥 [핵심 수정] GeneratorCommand cmd 파라미터 추가
    // cmd에 있는 컴파일 시점의 DB 정보를 생성된 코드의 '기본값'으로 사용합니다.
    private void writeConstructor(PrintWriter out, String className, MEntityAstProcessor.GeneratorCommand cmd) {
        out.println("    public " + className + "() {");
        out.println("        Properties props = new Properties();");

        // 1. application.properties 로드 시도 (런타임 오버라이드용)
        out.println("        try (InputStream is = getClass().getClassLoader().getResourceAsStream(\"application.properties\")) {");
        out.println("            if (is != null) {");
        out.println("                props.load(is);");
        out.println("            } else {");
        out.println("                System.out.println(\"ℹ️ [INFO] application.properties 없음. 컴파일 시점 설정값을 사용합니다.\");");
        out.println("            }");
        out.println("        } catch (IOException e) {");
        out.println("            System.err.println(\"⚠️ [WARN] 설정 파일 로드 중 오류: \" + e.getMessage());");
        out.println("        }");
        out.println();

        out.println("        PooledDataSource ds = new PooledDataSource();");

        // 2. DB 정보 설정 (우선순위: properties 파일 > cmd(컴파일 시점 값))
        // cmd 객체의 값을 문자열 리터럴로 소스 코드에 박아넣습니다.
        String defaultDriver = cmd.dbType != null && cmd.dbType.contains("POSTGRES")
                ? "org.postgresql.Driver" : "com.mysql.cj.jdbc.Driver";

        out.println("        // application.properties 값이 있으면 사용하고, 없으면 컴파일 시점의 값(" + cmd.url + ")을 사용");
        out.println("        String driver = props.getProperty(\"spring.datasource.driver-class-name\", \"" + defaultDriver + "\");");
        out.println("        String url = props.getProperty(\"spring.datasource.url\", \"" + cmd.url + "\");");
        out.println("        String username = props.getProperty(\"spring.datasource.username\", \"" + cmd.username + "\");");
        out.println("        String password = props.getProperty(\"spring.datasource.password\", \"" + cmd.password + "\");");
        out.println();

        // 3. 유효성 검사
        out.println("        if (url == null || username == null || password == null) {");
        out.println("            throw new RuntimeException(\"❌ DB 접속 정보가 누락되었습니다.\");");
        out.println("        }");
        out.println();

        out.println("        ds.setDriver(driver);");
        out.println("        ds.setUrl(url);");
        out.println("        ds.setUsername(username);");
        out.println("        ds.setPassword(password);");

        out.println();
        out.println("        Configuration config = new Configuration(new Environment(\"jpm_env\", new JdbcTransactionFactory(), ds));");
        out.println("        this.factory = new SqlSessionFactoryBuilder().build(config);");
        out.println("    }");
    }

    private void writeRunMethod(PrintWriter out) {
        out.println("    public void run(Command runCmd) {");
        out.println("        if (\"null\".equals(runCmd.dynamicMode)) {");
        out.println("            executeBySqlSession(runCmd.xmlPath, runCmd.namespace, runCmd.id, runCmd.type);");
        out.println("        } else {");
        out.println("            executeDynamic(runCmd.namespace, runCmd.id, runCmd.type, runCmd.sql);");
        out.println("        }");
        out.println("    }");
        out.println();
    }

    private void writeDynamicExecutorMethod(PrintWriter out) {
        out.println("    private void executeDynamic(String ns, String id, SqlCommandType type, String sql) {");
        out.println("        Configuration config = factory.getConfiguration();");
        out.println("        String fullId = ns + \".\" + id;");
        out.println();
        out.println("        if (!config.hasStatement(fullId)) {");
        out.println("            StaticSqlSource sqlSource = new StaticSqlSource(config, sql);");
        out.println("            MappedStatement ms = new MappedStatement.Builder(config, fullId, sqlSource, type).build();");
        out.println("            config.addMappedStatement(ms);");
        out.println("        }");
        out.println("        try (SqlSession session = factory.openSession(true)) {");
        out.println("            session.update(fullId);");
        out.println("            System.out.println(\"✅ [DYNAMIC] Executed: \" + fullId);");
        out.println("        }");
        out.println("    }");
        out.println();
    }

    private void writeSqlSessionExecutorMethod(PrintWriter out) {
        out.println("    private void executeBySqlSession(String xmlPath, String ns, String id, SqlCommandType type) {");
        out.println("        try (SqlSession session = factory.openSession(true);");
        out.println("             InputStream is = Resources.getResourceAsStream(xmlPath)) {");
        out.println("            if (is != null) {");
        out.println("                new XMLMapperBuilder(is, session.getConfiguration(), xmlPath, session.getConfiguration().getSqlFragments()).parse();");
        out.println("            }");
        out.println("            session.update(ns + \".\" + id);");
        out.println("            System.out.println(\"✅ [SQL_SESSION] Executed: \" + ns + \".\" + id);");
        out.println("        } catch (Exception e) { e.printStackTrace(); }");
        out.println("    }");
    }
}