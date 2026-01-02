# E-Commerce Platform (Coupang Clone)

## 프로젝트 개요
- **목적**: 클라우드 엔지니어링 부트캠프 팀 프로젝트 (5인)
- **모델**: 쿠팡 스타일 이커머스 플랫폼
- **개발 단계**: Phase 1 (Monolithic MVP)
- **담당 도메인**: Product 

## 기술 스택
- Java 17, Spring Boot 3.x, Spring Data JPA, QueryDSL
- PostgreSQL 15 (Docker), Gradle
- 인증: JWT, Mock 인증 (test용, user 도메인과 병합 후 삭제)

## 아키텍처
DDD + Layered Architecture
├── presentation/   # Controller, DTO
├── application/    # Service (비즈니스 로직)
├── domain/         # Entity, Repository Interface, Enum
└── infrastructure/ # Repository 구현, 외부 연동


## 도메인 구조 (17 테이블, 배송 외부 API)
| 도메인 | 테이블 | 담당    |
|--------|--------|-------|
| User | p_user, p_owner, p_user_address, p_payment_method, p_claim | 팀원A   |
| **Product** | p_category, p_product, p_product_option, p_product_option_value, p_product_variant | **담당** |
| Review | p_review, p_product_rating, p_ai_review_log | 팀원B   |
| Order | p_cart, p_cart_item, p_order, p_order_item | 팀원C   |
| Payment | p_payment, p_payment_split, p_refund, p_settlement | 팀원D   |

## Product 도메인 상세

### ERD
p_category (카테고리)
├── category_id (PK), parent_id (자기참조), name, depth, sort_order, is_active
└── Audit: created_at, created_by, updated_at, updated_by, deleted_at, deleted_by
p_product (상품)
├── product_id (PK), owner_id (FK→p_owner), category_id (FK)
├── title, description, thumbnail_url, status, has_options
├── price, stock_quantity (옵션 없을 때)
├── suspend_reason, suspended_at (Manager 정지용)
└── Audit: created_at, created_by, updated_at, updated_by, deleted_at, deleted_by

p_product_option (옵션: 사이즈, 색상)
├── option_id (PK), product_id (FK), name, sort_order
└── Audit: created_at, created_by, updated_at, updated_by, deleted_at, deleted_by
p_product_option_value (옵션값: 270, 화이트)
├── option_value_id (PK), option_id (FK), value, sort_order
└── Audit: created_at, created_by, updated_at, updated_by, deleted_at, deleted_by
p_product_variant (SKU: 옵션 조합별 재고/가격)
├── variant_id (PK), product_id (FK), sku_code, option_value_ids[]
├── option_name, price, stock_quantity, status
└── Audit: created_at, created_by, updated_at, updated_by, deleted_at, deleted_by

### Enum
ProductStatus: ON_SALE, SOLD_OUT, HIDDEN, SUSPENDED, DELETED
VariantStatus: ON_SALE, SOLD_OUT, DISCONTINUED


### API (15개)
| Method | Endpoint                       | 권한 | 설명 |
|--------|--------------------------------|------|------|
| GET | /categories                    | ALL | 카테고리 목록 |
| GET | /categories/{id}               | ALL | 카테고리 상세 |
| GET | /products                      | ALL | 상품 목록 (구매자) |
| GET | /products/{id}                 | ALL | 상품 상세 |
| POST | /products                      | OWNER | 상품 등록 |
| GET | /products/owner                | OWNER | 내 상품 목록 |
| PATCH | /products/{id}                 | OWNER | 상품 수정 |
| DELETE | /products/{id}                 | OWNER | 상품 삭제 (Soft) |
| PUT | /products/{id}/options         | OWNER | 옵션 전체 수정 |
| POST | /products/{id}/variants        | OWNER | SKU 추가 |
| PATCH | /products/{id}/variants/{vid}  | OWNER | SKU 수정 |
| DELETE | /products/{id}/variants/{vid}  | OWNER | SKU 삭제 |
| GET | /manager/products              | MANAGER | 전체 상품 조회 |
| PATCH | /manager/products/{id}/suspend | MANAGER | 상품 정지 |
| PATCH | /manager/products/{id}/restore | MANAGER | 정지 해제 |

### 주요 비즈니스 규칙
- 상품 삭제: Soft Delete (status → DELETED)
- SKU 삭제: 주문 있으면 DISCONTINUED, 없으면 Hard Delete
- Manager 정지: suspend_reason, suspended_at 기록
- 옵션 수정: PUT으로 전체 교체 (기존 삭제 후 새로 생성)

## 패키지 구조
com.ecommerce/
├── global/
│   ├── common/
│   │   ├── response/ApiResponse.java, PageResponse.java
│   │   ├── exception/ErrorCode.java, BusinessException.java
│   │   ├── entity/BaseEntity.java (Audit)
│   │   └── constants/MockIds.java
│   └── infrastructure/config/
│       ├── DevSecurityConfig.java (개발용)
│       └── MockDataInitializer.java
└── product/
    ├── presentation/
    │   ├── controller/ (CategoryController, ProductController, ...)
    │   └── dto/request/, dto/response/
    ├── application/service/
    │   └── CategoryService, ProductService, ProductVariantService
    ├── domain/
    │   ├── entity/ (Category, Product, ProductOption, ...)
    │   ├── repository/ (JpaRepository interfaces)
    │   └── enums/ (ProductStatus, VariantStatus)
    └── infrastructure/repository/
        └── ProductQueryRepository.java (QueryDSL)

## 개발 컨벤션
- REST API: `/api/v1/...`
- 응답 형식: `ApiResponse<T>` (status, data, error)
- 페이징: `PageResponse<T>` (content, page, size, totalElements, totalPages)
- 예외: `BusinessException(ErrorCode.XXX)` → GlobalExceptionHandler
- Mock ID: `MockIds.OWNER`, `MockIds.MANAGER` (개발 단계)

## ErrorCode (Product)
P001: PRODUCT_NOT_FOUND
P002: CATEGORY_NOT_FOUND
P003: VARIANT_NOT_FOUND
P004: DUPLICATE_SKU_CODE
P005: PRODUCT_NOT_ON_SALE
P006: PRODUCT_ALREADY_SUSPENDED
P007: PRODUCT_NOT_SUSPENDED
P008: PRODUCT_ACCESS_DENIED
P009: STOCK_NOT_ENOUGH
P010: PRODUCT_HAS_ORDERS


## 현재 상태
- [x] ERD 설계 완료
- [x] API 명세서 완료 (노션)
- [ ] Entity 작성
- [ ] Repository 작성
- [ ] Service 작성
- [ ] Controller 작성
- [ ] 테스트

## 참고 링크
- 노션 ERD: https://www.notion.so/2d50c73d23d680eb8fbfc0fb28867f36
- 노션 API: https://www.notion.so/2d80c73d23d680dda534c60994dc80d0
