package jpm_repository.parse.domain.cache;

public interface ParamRegistry {

    /**
     * 특정 키와 값을 저장소에 바인딩합니다.
     */
    void bind(String key, String value);

    /**
     * 해당 이름이 파라미터(isParam_)로 등록되어 있는지 확인합니다.
     */
    boolean isParam(String name);

    /**
     * 해당 이름의 에일리어스(alias_)가 존재하는지 확인합니다.
     */
    boolean hasAlias(String name);

    /**
     * 등록된 에일리어스 값을 가져옵니다.
     */
    String getAlias(String name);

    /**
     * 일반 키가 저장소에 존재하는지 확인합니다.
     */
    boolean has(String key);

    /**
     * 일반 키에 해당하는 값을 가져옵니다.
     */
    String get(String key);
}
