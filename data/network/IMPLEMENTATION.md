# Triển khai `:data:network`

Dựng OkHttp/Retrofit và chuyển lỗi truyền tải thành `NetworkResult`. DTO và service API thuộc feature.

## Khi nào dùng
Feature gọi HTTP. Repository bọc mọi lời gọi bằng `safeApiCall`; không để exception Retrofit/OkHttp lọt lên UI.

## Thêm vào dự án
```kotlin
dependencies {
    implementation(project(":data:network"))
    // :app tự khai báo vì module không phơi ra api:
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.kotlinx.serialization.json)
}
```
Cần plugin `kotlin.serialization` ở module chứa DTO.

## Nối dây (ở `:app`)
```kotlin
@Provides @Singleton fun okHttp(): OkHttpClient =
    NetworkClientFactory.createOkHttpClient(isDebug = AppEnvironmentProvider.current == AppEnvironment.DEBUG)
@Provides @Singleton fun retrofit(c: OkHttpClient): Retrofit =
    NetworkClientFactory.createRetrofit(baseUrl = "https://…/", okHttpClient = c)
@Provides @Singleton fun api(r: Retrofit): ArticlesApiService = r.create(ArticlesApiService::class.java)
```
Tuỳ biến: `NetworkClientConfig(isDebug, connect/read/writeTimeout, headers, requestIdHeader, requestIdProvider)`. Log chỉ ở mức `BASIC` và chỉ khi debug.

## Cách dùng
```kotlin
suspend fun load(): NetworkResult<List<Article>> =
    safeApiCall { api.articles().map(ArticleDto::toDomain) }

when (val r = load()) {
    is NetworkResult.Success -> r.data
    is NetworkResult.Failure -> r.error   // AppError.Connectivity / Http / Serialization
}
```
`safeApiCall(errorBodyParser = …)` nhận `HttpErrorBodyParser` để đọc `serverMessage` từ body lỗi.

## Test
`NetworkClientFactoryTest`, `NetworkResultTest`. Chạy: `./gradlew :data:network:testDebugUnitTest`.

## Lỗi hay gặp
- Log header `Authorization`, token, body phản hồi hay PII (vi phạm SEC-01).
- Đặt DTO/service dùng riêng một feature vào module này.
- Gọi service trực tiếp từ ViewModel/Composable.

Tài liệu: `architecture/10-data/network.md`, `architecture/11-error-and-result-model.md`.
