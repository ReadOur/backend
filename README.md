# 현재 src/main/resources/application.properties가 3307 포트를 쓰도록 설정해두었음.
# 포트 쓸 때 3307에 올려서 쓰거나, gitignore에 해당 부분 추가해서 로컬 세팅으로 써야 개발할 때 확인 용이함

## 패키지 분리 구조
src/main/java/com/readour/
├─ common/
│  ├─ config/
│  ├─ exception/
│  ├─ dto/
│  │  ├─ ApiResponse.java        # 공용 응답 래퍼
│  │  └─ PageResponse.java       # 공용 페이징 응답
│  ├─ enums/
│  └─ util/
├─ chat/
│  ├─ api/
│  │  ├─ controller/
│  │  └─ dto/                    # Chat 전용 DTO들
│  ├─ service/
│  ├─ repository/                # Spring Data JPA 바로 사용
│  └─ entity/
└─ community/
   ├─ api/
   │  ├─ controller/
   │  └─ dto/                    # Community 전용 DTO들
   ├─ service/
   ├─ repository/                # Spring Data JPA
   └─ entity/
