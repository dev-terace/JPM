import annotation.MColumn;
import annotation.MEntity;

import dsl_variable.v2.ColumnType;
import dsl_variable.v2.MVariable;


@MEntity(name = "orders") // DB 테이블명: orders
public class OrderEntity {

    // 1. PK
    @MColumn
    private MVariable id = MVariable.builder()
            .type(ColumnType.LONG)
            .primaryKey(true)
            .autoIncrement(true)
            .build();

    // 2. FK (외래키 설정) 🔥 여기가 바뀐 부분!


    @MColumn
    private MVariable productName = MVariable.builder()
            .type(ColumnType.STRING)
            .length(100)
            .build();
}


