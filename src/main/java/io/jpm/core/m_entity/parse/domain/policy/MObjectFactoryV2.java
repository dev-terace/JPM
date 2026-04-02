package io.jpm.core.m_entity.parse.domain.policy;




import io.jpm.api.MField;
import io.jpm.api.m_field_type.MFieldTypeEnum;
import io.jpm.api.OnDeleteType;
import io.jpm.common.exception.ErrorTracker;
import io.jpm.core.m_entity.valid.MFieldValidator;
import io.jpm.core.m_entity.parse.infra.ast.AstMFieldParserV2; // 🚀 V2: Tree API 유틸 사용

import java.util.List;

public class MObjectFactoryV2 {

    /**
     * MTreeUtils에서 추출한 Pair 리스트를 바탕으로 MField 객체를 생성합니다.
     */
    private final MFieldValidator validator;
    public MObjectFactoryV2() {
        this.validator = new MFieldValidator();
    }

    public MField<?> createMVariableV2(List<AstMFieldParserV2.Pair> pairs, ErrorTracker errorTracker) {
        MField.Builder builder = MField.builder();

        // 1. 변수명(fieldName)을 기본 컬럼명으로 먼저 설정
        // 만약 체이닝에서 .name("...")이 나오면 뒤에서 덮어쓰게 됩니다.
        for (AstMFieldParserV2.Pair pair : pairs) {
            if ("fieldName".equals(pair.key)) {
                builder.name(pair.value);
                break;
            }
        }

        String typeTemp = "";

        for (AstMFieldParserV2.Pair pair : pairs) {
            String key = pair.key;
            String val = pair.value;

            if (val == null || val.isEmpty()) continue;

            switch (key) {
                case "name": // 명시적 컬럼명 (.name("col_name"))
                    builder.name(val);
                    break;
                case "type":
                    typeTemp = String.valueOf((MFieldTypeEnum.valueOf(val.toUpperCase())));
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
                    try {
                        builder.length(Integer.parseInt(val));
                    } catch (NumberFormatException e) {
                        // 숫자가 아닌 값이 들어올 경우 기본값 처리 혹은 무시
                    }
                    break;
                case "parent": // FK 대상 클래스
                    builder.parent(val);
                    break;
                case "onDelete":
                    builder.onDelete(OnDeleteType.valueOf(val.toUpperCase()));
                    break;
                case "index":
                    builder.index(Boolean.parseBoolean(val));
                    break;
                case "unique":
                    builder.unique(Boolean.parseBoolean(val));
                    break;
            }
        }

        // 3. 객체 생성 및 검증
        MField<?> var = builder.build();
        if (typeTemp != null) {
            var.setType(typeTemp);
        }

        // 기존에 분리해두신 Validator를 호출하여 논리 오류 체크
        validator.validate(var, errorTracker);

        return var;
    }
}