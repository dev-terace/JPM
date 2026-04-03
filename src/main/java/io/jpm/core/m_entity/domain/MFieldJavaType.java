package io.jpm.core.m_entity.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MFieldJavaType {

    private static final Map<String, String> MFIELD_TYPE_MAP = new LinkedHashMap<>();


    static {
        MFIELD_TYPE_MAP.put("INTEGER",        "java.lang.Integer");
        MFIELD_TYPE_MAP.put("LONG",           "java.lang.Long");
        MFIELD_TYPE_MAP.put("STRING",         "java.lang.String");
        MFIELD_TYPE_MAP.put("BOOLEAN",        "java.lang.Boolean");
        MFIELD_TYPE_MAP.put("LOCAL_DATE",     "java.time.LocalDate");
        MFIELD_TYPE_MAP.put("LOCAL_DATE_TIME","java.time.LocalDateTime");
        MFIELD_TYPE_MAP.put("FLOAT",          "java.lang.Float");
        MFIELD_TYPE_MAP.put("DOUBLE",         "java.lang.Double");
        MFIELD_TYPE_MAP.put("UUID_V_7",       "java.util.UUID");
        MFIELD_TYPE_MAP.put("JSON",           "java.lang.String");   // JSON → String (필요시 Object로 변경)
        MFIELD_TYPE_MAP.put("TEXT",           "java.lang.String");
        // FK 는 런타임에 부모 엔티티 PK 타입을 추적해서 결정
    }



    public static String getMFieldTypeMap(String mFieldType)
    {
        return  MFIELD_TYPE_MAP.get(mFieldType);
    }

}
