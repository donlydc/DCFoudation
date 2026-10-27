# Triển khai `:data:database`

Hạ tầng Room (factory, transaction, chính sách cũ/mới) và schema tham chiếu `articles`.

## Khi nào dùng
Dữ liệu có cấu trúc, cần truy vấn hoặc offline-first. Entity/DAO dừng ở repository.

## Thêm vào dự án
```kotlin
plugins { id("dc.android.library"); id("dc.room"); id("dc.testing") }
dependencies { implementation(project(":runtime:concurrency")) }
```
`dc.room` bật KSP + `androidx.room` và xuất schema vào `<module>/schemas/` (phải commit). `:app` dùng `implementation(project(":data:database"))` và cũng áp `dc.room`.

## Nối dây (ở `:app`)
```kotlin
@Provides @Singleton
fun provideDb(@ApplicationContext c: Context, d: DispatcherProvider): DcDatabase =
    RoomDatabaseFactory.create(c, DcDatabase::class.java, name = "dc_foundation_db", dispatcherProvider = d)
@Provides @Singleton fun dao(db: DcDatabase): ArticleDao = db.articleDao()
```
Tham số tuỳ chọn: `migrations`, `allowDestructiveMigrationInDebugOrTest`. Query/transaction chạy trên `dispatcherProvider.io`.

## Cách dùng
- Transaction: `RoomTransactionRunner(db).inTransaction { … }` (hợp đồng `TransactionRunner` để fake khi test).
- Độ cũ cache: `StalenessPolicy(maxAge).isStale(lastUpdatedAtMillis, nowMillis)` (null → cũ; thời gian tương lai → chưa cũ).
- Repository map `ArticleEntity` ↔ model miền; DTO mạng map ở feature, không ở đây.

## Thêm entity/bảng
1. Thêm `@Entity` + DAO, đăng ký trong `@Database(entities = [...])`.
2. Tăng `version`, viết `Migration`, truyền vào `RoomDatabaseFactory.create(migrations = …)`.
3. Build để Room xuất schema mới vào `schemas/` và commit cùng thay đổi.

## Test
`StalenessPolicyTest`. Chạy: `./gradlew :data:database:testDebugUnitTest`.

## Lỗi hay gặp
- Quên migration/schema khi đổi cấu trúc.
- Để `Entity`/`Dao` lọt vào UI state.

Tài liệu: `architecture/10-data/persistence-room.md`, `cache-and-offline.md`, `mapping-boundaries.md`.
