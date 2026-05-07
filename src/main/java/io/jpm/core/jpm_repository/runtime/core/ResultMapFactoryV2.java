package io.jpm.core.jpm_repository.runtime.core;

import io.jpm.common.utils.LogPrinter;
import io.jpm.core.jpm_repository.domain.model.result_map_meta.ResultMapFactory;
import io.jpm.core.jpm_repository.domain.model.t_repo_java_type.TRepoJavaTypeMeta;

import javax.annotation.processing.Filer;
import java.util.List;
import java.util.Set;

public class ResultMapFactoryV2 {


    public static class ResultMapProjectionSpec  {
        private final String fieldName;   // DTO 필드명  (ex: "userId")
        private final Class<?> fieldType; // 타입 변환용 (ex: Long.class)

        public ResultMapProjectionSpec(String fieldName,  String pkFieldType, Filer filer) throws ClassNotFoundException {



            TRepoJavaTypeMeta repoJavaTypeMeta = new TRepoJavaTypeMeta(filer);



            repoJavaTypeMeta.addTypeInfo(fieldName, pkFieldType);


            LogPrinter.info("[ResultMapFactoryV2] Line 31: " + fieldName + ", " + pkFieldType);

            Class<?> clazz = Class.forName(repoJavaTypeMeta.getJavaTypeFqcn());



            this.fieldName = fieldName;
            this.fieldType = clazz;
        }


        public String getFieldName() { return fieldName; }
        public Class<?> getFieldType() { return fieldType; }
    }




    public static class FieldMapping {
        private final String fieldName;   // 예: "id"
        private final String columnName;  // 예: "user_id"

        public FieldMapping(String fieldName, String columnName) {
            this.fieldName = fieldName;
            this.columnName = columnName;
        }

        public String getFieldName() { return fieldName; }
        public String getColumnName() { return columnName; }
    }

    public static class RelationMapping {
        private final String fieldName;      // 예: "orders"
        private final String targetClass;    // 예: "OrderEntity"

        // MyBatis <id> 태그 용도
        private final String childIdProperty; // 예: "orderId"
        private final String childIdColumn;   // 예: "order_id"

        // 유연한 처리를 위해 4개를 모두 받는 생성자
        public RelationMapping(String fieldName, String targetClass, String childIdProperty, String childIdColumn) {
            this.fieldName = fieldName;
            this.targetClass = targetClass.replace(".class", ""); // .class 자동 제거
            this.childIdProperty = childIdProperty;
            this.childIdColumn = childIdColumn;
        }

        public String getFieldName() { return fieldName; }
        public String getTargetClass() { return targetClass; }
        public String getChildIdProperty() { return childIdProperty; }
        public String getChildIdColumn() { return childIdColumn; }
    }



}
