package io.jpm.api.m_field_type;

public interface MFieldType {
    class INTEGER implements MFieldType {}
    class LONG implements MFieldType {}
    class STRING implements MFieldType {}
    class BOOLEAN implements MFieldType {}
    class LOCAL_DATE implements MFieldType {}
    class LOCAL_DATE_TIME implements MFieldType {}
    class FK implements MFieldType {}
    class FLOAT implements MFieldType {}
    class DOUBLE implements MFieldType {}
    class UUID_V_7 implements MFieldType {}
    class JSON implements MFieldType {}
    class TEXT implements MFieldType {}
}
