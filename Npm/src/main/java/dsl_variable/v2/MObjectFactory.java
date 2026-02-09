package dsl_variable.v2;

import java.util.List;

public class MObjectFactory {

    public static MVariable createMVariable(List<MParserUtils.Pair> pairs) {
        MVariable.Builder builder = MVariable.builder();

        for (MParserUtils.Pair pair : pairs) {
            String key = pair.key;
            String val = pair.value;

            switch (key) {
                case "fieldName": // 파서에서 넣어준 변수명
                    // .name() 설정이 없으면 변수명을 사용하도록 로직 처리 필요
                    builder.name(val);
                    break;
                case "name": // 명시적 컬럼명
                    builder.name(val);
                    break;
                case "type":
                    // String으로 된 Enum 이름을 실제 Enum으로 변환
                    builder.type(ColumnType.valueOf(val));
                    break;
                case "primaryKey":
                    builder.primaryKey(Boolean.parseBoolean(val));
                    break;
                case "autoIncrement":
                    builder.autoIncrement(Boolean.parseBoolean(val));
                    break;
                case "nullable":
                    builder.nullable(Boolean.parseBoolean(val));
                    break;
                case "defaultValue":
                    builder.defaultValue(val);
                    break;
                case "length":
                    builder.length(Integer.parseInt(val));
                    break;
                case "target": // FK 타겟 클래스
                    builder.target(val); // 클래스명 문자열 저장
                    break;
                case "onDelete":
                    builder.onDelete(val);
                    break;
            }
        }

        return builder.build();
    }
}