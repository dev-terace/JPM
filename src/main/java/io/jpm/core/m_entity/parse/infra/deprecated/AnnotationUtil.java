package io.jpm.core.m_entity.parse.infra.deprecated;


import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.io.File;


@Deprecated
public class AnnotationUtil {





    public static String getRelativePath(VariableElement ve, boolean isTest) {
        // 1. 변수가 포함된 클래스(TypeElement) 찾기
        Element enclosing = ve.getEnclosingElement();
        while (enclosing != null && !(enclosing instanceof TypeElement)) {
            enclosing = enclosing.getEnclosingElement();
        }

        if (enclosing != null) {
            TypeElement typeElement = (TypeElement) enclosing;

            // 2. 패키지를 포함한 전체 이름 가져오기 (예: com.example.MyEntity)
            // 패키지 경로로 변환 (com.example -> com/example)
            String qualifiedName = typeElement.getQualifiedName().toString();
            String pathFromPackage = qualifiedName.replace(".", "/") + ".java";

            // 3. 베이스 경로 결정
            String basePath = isTest ? "src/test/java/" : "src/main/java/";

            // 4. 최종 경로 조합
            String finalPath = basePath + pathFromPackage;

            return new File(finalPath).getAbsolutePath();
        }

        throw new IllegalArgumentException("element not found");
    }

    // 단순 Pair 클래스 (없으면 Map.Entry로 대체 가능)
    public static class Pair<K, V> {
        public final K first;
        public final V second;

        public Pair(K first, V second) {
            this.first = first;
            this.second = second;
        }
    }



}
