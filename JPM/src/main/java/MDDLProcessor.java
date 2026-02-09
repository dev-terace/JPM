

import com.google.auto.service.AutoService;

import m_ddl_generator.generator.AutoDDLGenerator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.Set;


@AutoService(Processor.class)
@SupportedOptions({"ddl.db", "ddl.auto"})
@SupportedAnnotationTypes("annotation.MEntity")
public class MDDLProcessor extends AbstractProcessor {


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add("annotation.MEntity");
        types.add("annotation.MColumn"); // ✅ MColumn 추가
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 처리할 어노테이션이 없으면 그냥 통과
        if (annotations.isEmpty()) {
            return false;
        }

        // 옵션 읽기
        String dbType = processingEnv.getOptions().getOrDefault("ddl.db", "MYSQL");

        try {
            // 우리가 만든 Generator 실행
            AutoDDLGenerator generator = AutoDDLGenerator.createDefault(processingEnv, roundEnv, dbType);
            generator.generate();
        } catch (Exception e) {
            // 컴파일 에러 메시지로 출력
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "DDL 생성 실패: " + e.getMessage());
        }

        return true;
    }
}