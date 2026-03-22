# Sealog 프로젝트 코딩 컨벤션 (Coding Convention)

본 문서는 Sealog 백엔드 프로젝트의 일관된 코드 스타일과 구조를 유지하기 위한 가이드라인입니다. 개발자 및 AI 어시스턴트는 코드를 작성하거나 리팩토링할 때 반드시 이 규칙을 준수해야 합니다.

---

## 1. 주석(Comment) 컨벤션

### 기본 원칙
- 모든 주석은 JavaDoc 스타일(`/** ... */`)을 권장하며, 가독성을 위해 한글 작성을 원칙으로 합니다.

### 레이어별 주석 가이드
- **Controller:** 각 API 메소드 상단에 기능 설명과 비즈니스적 의도를 상세히 작성합니다. (Swagger 어노테이션과 병행)
- **Service Interface:** 서비스의 모든 공개 메소드(Public)에 대해 다음 항목을 포함한 **가장 상세한 명세**를 작성합니다.
    - `@param`: 파라미터의 의미와 제약 조건
    - `@return`: 반환되는 데이터의 의미
    - **`@throws`**: 비즈니스 로직 위반으로 인해 발생할 수 있는 예외 상황 (예: `CustomException.notFound`)
- **Service Implementation:** 
    - `@Override` 메소드에는 별도의 주석을 달지 않습니다. (인터페이스 주석 참조)
    - **`private` 메소드:** 내부적으로 사용되는 로직이므로, 복잡도가 낮더라도 메소드의 역할과 로직의 흐름을 설명하는 상세 주석을 반드시 작성합니다.
- **DTO:** 모든 내부 정적 클래스(Inner Class) 상단에 해당 DTO의 목적을 명시하는 주석을 작성합니다.
- **Repository:** 각 쿼리 메소드 상단에 조회의 목적과 특이사항을 작성합니다.

---

## 2. Controller 컨벤션

### 기본 원칙
- **역할:** 요청 값 검증, 서비스 호출, 응답 반환에만 집중합니다.
- **공통 응답 (자동화):** `ResponseWrapperAdvice`가 반환값을 가로채서 `CustomResponse<T>`로 자동 포장합니다.
- **명시적 응답:** 특정 성공 메시지가 필요한 경우에만 `CustomResponse`를 반환합니다.

---

## 3. Service 컨벤션

### 규약(Contract) 기반 설계
- **인터페이스:** 모든 상세 명세(JavaDoc) 및 **발생 가능 예외(`@throws`)**를 인터페이스에 집중시킵니다.
- **구현체:** 규약 이행에만 집중하며, 내부 보조 메소드(`private`)에 대해서는 상세히 주석을 작성합니다.
- **변환 책임:** Entity ↔ DTO 변환은 서비스 레이어에서 수행합니다.

### 예시 코드
```java
/**
 * 게시글 비즈니스 규약 인터페이스
 */
public interface PostService {

    /**
     * 새로운 게시글을 생성합니다.
     *
     * @param request 생성 요청 데이터
     * @return 생성된 게시글의 정보
     * @throws CustomException.badRequest 제목이 중복될 경우 발생
     * @throws CustomException.notFound 시리즈 ID가 유효하지 않을 경우 발생
     */
    PostMeResponse.Detail create(PostMeRequest.Create request);
}

@Service
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    @Override
    @Transactional
    public PostMeResponse.Detail create(PostMeRequest.Create request) {
        // ... 로직 (인터페이스 규약 이행)
        return buildResponse(post);
    }

    /**
     * 엔티티를 DTO로 변환하는 보조 메소드입니다.
     * 서비스 내부에서만 사용되며, 정적 팩토리 메소드 'of'를 호출합니다.
     */
    private PostMeResponse.Detail buildResponse(Post post) {
        return PostMeResponse.Detail.of(post.getId(), post.getTitle());
    }
}
```

---

## 4. DTO 컨벤션

### 기본 원칙
- **엔티티 의존성 제거:** DTO는 엔티티 클래스를 참조하지 않습니다. (Import 금지)
- **팩토리 메소드:** `from(Entity)` 대신 `of(...)`를 사용하여 필요한 값만 인자로 받습니다.
- **문서화:** 모든 이너 클래스에 주석을 달아 용도를 명확히 합니다.

---

## 5. Repository 및 Query 컨벤션

### 기본 원칙
- 모든 메소드에 조회 목적을 설명하는 주석을 작성합니다.
- **N+1 방지:** `JOIN FETCH`를 적극 활용합니다.

---

## 6. 기타 공통 규칙

- **Entity:** `BaseTimeEntity` 상속 필수, 도메인 메소드(`publish()`, `softDelete()`)를 통한 상태 관리.
- **Exception:** `CustomException`의 정적 팩토리 메소드 사용.
- **Naming:** DB(snake_case), Package(소문자 연속), Class(PascalCase), Method/Variable(camelCase).
