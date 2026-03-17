# 🌊 Sealog Backend `v2.0.0`

> **Sealog 블로그 백엔드 시스템**  
> 안정적인 성능을 목표로 설계하는 sealog 블로그 백엔드입니다.

---

## 🛠 Tech Stack & Architecture

### **1. Backend Core**
- **Framework:** Spring Boot 3.3.x (Java 17)
- **Database:** MariaDB (Primary), Redis (Cache & Session)
- **Persistence:** Spring Data JPA
- **Security:** Spring Security & JWT (Stateless Authentication)

### **2. Infrastructure & Storage**
- **File Strategy:** `Local File System` vs `AWS S3 + CloudFront` (동적 전환 가능)
- **Environment:** Docker Compose (Containerized Infrastructure)
- **Documentation:** Springdoc OpenAPI (Swagger UI)

---

## 🏗 Domain Models & Key Features

프로젝트는 도메인 중심의 패키지 구조(`domain.feature`)를 따르며, 각 도메인은 독립적인 비즈니스 로직을 수행합니다.

### **1. Post (게시글)**
- **HTML Parsing & Sanitizing:** 게시글 본문의 HTML을 파싱하여 미디어(Image/Video) 태그를 안전하게 처리하고 메타데이터를 추출합니다.
- **Slug Generation:** 제목 기반의 SEO 최적화된 URL 슬러그 자동 생성.
- **Support Media:** 게시글 내 비디오 스트리밍 지원 및 `ClientAbortException` 최적화 처리.

### **2. File (파일 관리)**
- **Metadata Management:** 업로드된 모든 파일의 메타데이터를 DB에서 관리하며, 미사용 파일을 주기적으로 정리(Scheduler)합니다.
- **Storage Abstraction:** `FileStorageService` 인터페이스를 통해 로컬과 S3 저장소 간의 기술적 결합도를 낮췄습니다.

### **3. Auth & User (인증 및 사용자)**
- **JWT Auth:** Access Token(Memory)과 Refresh Token(Redis/Cookie)을 활용한 이중 인증 체계.
- **Tech Stack Profile:** 사용자의 기술 스택을 그룹화(`StackGroup`)하여 관리하고 프로필에 노출합니다.

### **4. Series & Stack (시리즈 및 스택)**
- 연관된 게시글을 묶어 관리하는 시리즈 기능과 다대다(`N:M`) 관계의 스택 시스템.

---

## ⚙️ Configuration Strategy (Environment Isolation)

### **1. Profile-based Config**
- **`application-dev.yml`:** 로컬 및 개발 서버용. SQL 포맷팅 및 로그 상세 노출, 테스트 데이터 초기화 활성화.
- **`application-prod.yml`:** 운영 서버용. 보안 강화 및 인프라 최적화 설정.

### **2. Feature-based Bean Switching**
`@Profile` 대신 `@ConditionalOnProperty`를 사용하여 특정 기능의 활성화 여부를 환경 변수로 제어합니다.
- **Storage Switching:** `.env`의 `STORAGE_TYPE` 값(`local` 또는 `s3`)에 따라 관련 빈(`S3FileStorageService` 등)이 동적으로 로드됩니다.

---

## 🚀 Getting Started (For Developers)

### **1. Running Infrastructure**
`Makefile`을 통해 로컬 개발 환경에 필요한 DB와 Redis를 컨테이너로 즉시 실행할 수 있습니다.

```bash
# 인프라 실행 (MariaDB, Redis)
make dev-up

# 테스트 서버 실행 (격리된 환경)
make perf-up
```

### **2. API Documentation**
서버 실행 후 Swagger UI를 통해 모든 API의 요청/응답 스펙과 제약 조건을 확인할 수 있습니다.
- **URL:** `http://localhost:8080/swagger-ui/index.html`

---

## 📈 Release Notes (v2.0.0)

- **Config Refactoring:** 기능 중심의 조건부 빈 설정(`@ConditionalOnProperty`) 도입.
- **Media Support:** 게시글 내 비디오 태그 정식 지원 및 스트리밍 안정화.
- **Architecture:** `domain.base` 패키지를 통한 공통 유틸리티 및 검증 로직 모듈화.
- **Reliability:** 글로벌 예외 처리기 보강 및 스트리밍 중단 케이스 최적화.

---

