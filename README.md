# PROJECT_ANT
![Ant Logo](./src/main/resources/images/ANT.png)

Spring Boot 기반의 OAuth2 소셜 로그인 + JWT 인증 시스템

## 기술 스택

| 구분 | 기술 |
|------|------|
| Framework | Spring Boot 3.5.5 |
| Language | Java 17 |
| Build Tool | Gradle |
| Database | MariaDB |
| Security | Spring Security + OAuth2 + JWT |
| Documentation | SpringDoc OpenAPI (Swagger) |

## 프로젝트 구조

```
src/main/java/com/project_ant/
├── ProjectAntApplication.java       # 메인 애플리케이션
├── config/
│   └── SwaggerConfig.java           # Swagger/OpenAPI 설정
├── controller/
│   ├── AuthController.java          # 인증 API (토큰 갱신, 로그아웃)
│   └── UserController.java          # 사용자 API (프로필 조회/수정/탈퇴)
├── domain/
│   └── User.java                    # 사용자 엔티티
├── dto/
│   ├── RefreshTokenRequest.java     # 토큰 갱신 요청 DTO
│   ├── TokenResponse.java           # 토큰 응답 DTO
│   ├── UserResponse.java            # 사용자 정보 응답 DTO
│   └── UserUpdateRequest.java       # 프로필 수정 요청 DTO
├── repository/
│   └── UserRepository.java          # 사용자 Repository
└── security/
    ├── config/
    │   └── SecurityConfig.java      # Spring Security 설정
    ├── jwt/
    │   ├── JwtAuthenticationFilter.java  # JWT 인증 필터
    │   └── JwtTokenProvider.java         # JWT 토큰 생성/검증
    └── oauth/
        ├── CustomOAuth2User.java         # OAuth2 사용자 객체
        ├── CustomOAuth2UserService.java  # OAuth2 사용자 서비스
        └── OAuth2SuccessHandler.java     # 로그인 성공 핸들러
```

## 주요 기능

### 1. 소셜 로그인 (OAuth2)
- Google, Kakao, Naver 소셜 로그인 지원
- 로그인 성공 시 Access Token + Refresh Token 발급

### 2. JWT 인증
- Access Token: 30분 유효
- Refresh Token: 7일 유효 (Rotation 적용)
- Stateless 인증 방식

### 3. 사용자 관리
- 프로필 조회/수정
- 회원 탈퇴
- 로그아웃 (Refresh Token 무효화)

## API 명세

### 인증 API

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|:----:|
| GET | `/oauth2/authorization/google` | Google 로그인 | X |
| GET | `/oauth2/authorization/kakao` | Kakao 로그인 | X |
| GET | `/oauth2/authorization/naver` | Naver 로그인 | X |
| POST | `/api/auth/refresh` | 토큰 갱신 | X |
| POST | `/api/auth/logout` | 로그아웃 | X |

### 사용자 API

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|:----:|
| GET | `/api/users/me` | 내 정보 조회 | O |
| PUT | `/api/users/me` | 프로필 수정 | O |
| DELETE | `/api/users/me` | 회원 탈퇴 | O |

### API 문서
- Swagger UI: `http://localhost:8080/api-docs`

## 인증 플로우

```
1. 소셜 로그인 요청
   GET /oauth2/authorization/{provider}

2. OAuth2 인증 후 콜백
   → Access Token + Refresh Token 발급
   → 프론트엔드로 리다이렉트 (?accessToken=...&refreshToken=...)

3. API 요청
   Authorization: Bearer {accessToken}

4. Access Token 만료 시
   POST /api/auth/refresh
   Body: { "refreshToken": "..." }
   → 새 Access Token + Refresh Token 발급

5. 로그아웃
   POST /api/auth/logout
   Body: { "refreshToken": "..." }
```

## 환경 설정

### 1. 환경 변수 설정

`.env.example`을 복사하여 `.env` 파일 생성 후 실제 값 입력:

```bash
cp .env.example .env
```

```properties
# Database
DB_URL=jdbc:mariadb://localhost:3306/project_ant
DB_USERNAME=your_username
DB_PASSWORD=your_password

# JWT
JWT_SECRET_KEY=your-secret-key-at-least-256-bits

# OAuth2
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
NAVER_CLIENT_ID=your_naver_client_id
NAVER_CLIENT_SECRET=your_naver_client_secret

# Frontend
FRONTEND_REDIRECT_URI=http://localhost:3000/login/success
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

### 2. 데이터베이스 생성

```sql
CREATE DATABASE project_ant CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

## 프론트엔드 연동 예시

### 로그인 후 토큰 처리 (React)

```javascript
// /login/success 페이지
useEffect(() => {
  const params = new URLSearchParams(window.location.search);
  const accessToken = params.get('accessToken');
  const refreshToken = params.get('refreshToken');

  if (accessToken && refreshToken) {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    navigate('/');
  }
}, []);
```

### API 요청

```javascript
// 인증이 필요한 API 요청
const response = await fetch('/api/users/me', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});
```

### 토큰 갱신

```javascript
const refreshToken = async () => {
  const response = await fetch('/api/auth/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      refreshToken: localStorage.getItem('refreshToken')
    })
  });

  if (response.ok) {
    const data = await response.json();
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
  }
};
```
