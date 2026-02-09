import annotation.MColumn;
import annotation.MEntity;
import dsl_variable.v2.ColumnType;
import dsl_variable.v2.MVariable;


@MEntity(name = "users") // DB 테이블명: users
public class UserEntity {

    // 1. [LONG + PK] 기본 키 테스트
    @MColumn
    private MVariable id = MVariable.builder()
            .type(ColumnType.LONG)
            .primaryKey(true)
            .autoIncrement(true)
            .build();

    // 2. [STRING] 필수 입력 (NOT NULL) 테스트
    @MColumn
    private MVariable username = MVariable.builder()
            .type(ColumnType.STRING)
            .length(50)
            .nullable(false)
            .build();

    // 3. [STRING] 기본값 (DEFAULT) + 긴 길이 테스트
    @MColumn
    private MVariable bio = MVariable.builder()
            .type(ColumnType.STRING)
            .length(500)
            .defaultValue("'Hello World'")
            .build();

    // 4. [INTEGER] 숫자형 + Null 허용 테스트
    @MColumn
    private MVariable age = MVariable.builder()
            .type(ColumnType.INTEGER)
            .nullable(true)
            .build();

    // 5. [BOOLEAN] 불리언 타입 테스트
    @MColumn
    private MVariable isActive = MVariable.builder()
            .type(ColumnType.BOOLEAN)
            .defaultValue("true")
            .build();

    // 6. [LOCAL_DATE] 날짜 타입 테스트
    @MColumn
    private MVariable birthDate = MVariable.builder()
            .type(ColumnType.LOCAL_DATE)
            .build();

    // 7. [LOCAL_DATE_TIME] 타임스탬프 테스트
    @MColumn
    private MVariable createdAt = MVariable.builder()
            .type(ColumnType.LOCAL_DATE_TIME)
            .defaultValue("CURRENT_TIMESTAMP")
            .build();

    // 8. [FK] 외래키 테스트 (OrderEntity 참조)
    // 주의: 테스트를 위해 User가 Order를 가리키게 함 (1:1 or N:1)
    @MColumn
    private MVariable lastOrder = MVariable.builder()
            .type(ColumnType.FK)
            .target(OrderEntity.class) // OrderEntity.class 참조
            .onDelete("SET NULL")      // 삭제 시 NULL 처리
            .nullable(true)
            .build();
}