# Terrace

JPA의 `auto-ddl` 기능에서 영감을 받아 만든 mybatis dsl입니다. 
현재는 `auto ddl`만 지원하며 쿼리 실행은 개발중에 있습니다.

JPA는 `@Entity`, `@Column` 등의 **어노테이션을 기반으로 엔티티와 컬럼을 정의**하는 반면,  
Terrace는 `MField`를 중심으로 **빌더 패턴을 사용하여 테이블의 필드와 속성을 정의**할 수 있도록 설계했습니다.

이를 통해 애플리케이션에서 정의한 필드 정보를 기반으로 데이터베이스의 **테이블 생성 및 변경 작업을 자동화**할 수 있도록 구현했습니다.

## 주요 특징

### MField 기반 필드 정의

Terrace는 `MField`를 통해 테이블의 필드와 속성을 빌더 패턴으로 정의할 수 있습니다.

지원하는 주요 타입은 다음과 같습니다.

- `INTEGER`
- `LONG`
- `STRING`
- `BOOLEAN`
- `LOCAL_DATE`
- `LOCAL_DATE_TIME`
- `FK`
- `FLOAT`
- `DOUBLE`
- `UUID_V_7`
- `JSON`
- `TEXT`

### Auto DDL Policy

데이터베이스의 DDL 처리 방식을 `AutoDDLPolicy`를 통해 제어할 수 있습니다.

지원하는 정책은 다음과 같습니다.

- `DISABLED` — 자동 DDL 수행 안 함
- `CREATE` — 테이블 생성
- `DROP` — 테이블 삭제
- `ALTER` — 테이블 변경
- `ALTER_N_EXE` — ALTER 후 실행
- `CREATE_N_EXE` — CREATE 후 실행
- `DROP_N_CREATE_EXE` — 기존 테이블 삭제 후 재생성

## 작동 화면

<img width="1882" height="951" alt="Terrace 작동 화면" src="https://github.com/user-attachments/assets/7cdb0b79-ab31-4840-8d1f-e2b64a00a14b" />

## 메인 기술

| 기술 | 활용 |
|---|---|
| **Gradle Plugin** | 프로젝트 빌드 및 플러그인 구성 |
| **ArchUnit** | 아키텍처 규칙 및 계층 구조 검증 |
| **MyBatis** | SQL 기반 데이터 접근 및 DB 연동 |
| **AutoService Annotation** | Annotation Processor를 활용한 구현체 자동 등록 |
