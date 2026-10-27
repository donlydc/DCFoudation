# Triển khai `:data:preferences`

Ranh giới DataStore Preferences generic. Từ vựng khoá và mapping thuộc repository của feature.

## Khi nào dùng
Lưu cấu hình/cờ nhỏ của người dùng (không phải dữ liệu quan hệ → dùng `:data:database`; không phải bí mật → dùng `:security`).

## Thêm vào dự án
```kotlin
dependencies { implementation(project(":data:preferences")) }
```
`:app` còn cần `libs.androidx.datastore.preferences` để tự tạo `DataStore` (module không phơi nó ra `api`).

## Nối dây (ở `:app`)
```kotlin
@Provides @Singleton
fun provideDataStore(@ApplicationContext c: Context): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(produceFile = { c.preferencesDataStoreFile("dc_foundation_prefs") })

@Provides @Singleton
fun providePreferencesDataSource(ds: DataStore<Preferences>): PreferencesDataSource =
    DataStorePreferencesDataSource(ds)
```

## Cách dùng
```kotlin
// Khoá khai báo trong feature repository
private val LastName = PreferenceKey.IntKey("greeting_last_name_index", default = 0)

class GreetingPreferencesRepository @Inject constructor(private val prefs: PreferencesDataSource) {
    fun lastNameIndex(): Flow<Int> = prefs.observe(LastName)
    suspend fun save(i: Int) = prefs.set(LastName, i)
}
```
- Loại khoá: `IntKey`, `LongKey`, `FloatKey`, `BooleanKey`, `StringKey`, `StringSetKey`, `EnumKey` (giá trị lạ → default), `JsonKey` (lỗi decode → default).
- Ghi nhiều khoá nguyên tử: `prefs.edit { set(a, 1); remove(b) }`.
- Migration: triển khai `PreferencesMigration(toVersion, migrate(editor))`.

## Test
Dùng `InMemoryPreferencesDataSource` từ `:testing`. Chạy: `./gradlew :data:preferences:testDebugUnitTest`.

## Lỗi hay gặp
- Đặt khoá/từ vựng nghiệp vụ vào module này.
- Để lộ `DataStore`/`Preferences` ra UI state.

Tài liệu: `architecture/10-data/preferences-datastore.md`.
