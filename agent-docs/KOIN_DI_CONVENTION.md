# Koin DI設定

## 参照ファイル

- DI初期化: `shared/src/commonMain/kotlin/.../Koin.kt:3`
- Home Module例: `feature/home/src/commonMain/kotlin/.../di/HomeModule.kt`

## モジュール構成

```kotlin
// 各レイヤーのモジュール
networkModule: HttpClient, NewsApiService (singleton)
dataModule: NewsRepository (singleton)
domainModule: ユースケース (singleton)
homeModule: HomeViewModel (viewModel scope)
searchModule: SearchViewModel (viewModel scope)
viewerModule: ViewerViewModel (viewModel scope)
```

## バインディングパターン

### 基本パターン

```kotlin
val myModule = module {
    // インターフェースバインディング
    singleOf(::MyRepositoryImpl) bind MyRepository::class
    singleOf(::MyUseCaseImpl) bind MyUseCase::class

    // ViewModelスコープ
    viewModelOf(::MyViewModel)
}
```

### 可視性ルール

| 種類 | 可視性 | 理由 |
|------|--------|------|
| インターフェース | `public` | 他モジュールから参照 |
| 実装クラス | `internal` | Koinバインディング経由のみ |

### UseCase バインディング例

```kotlin
// core/domain のモジュール
val domainModule = module {
    singleOf(::FetchTopHeadlineArticlesUseCaseImpl) bind FetchTopHeadlineArticlesUseCase::class
    singleOf(::GetArticleByIdUseCaseImpl) bind GetArticleByIdUseCase::class
    singleOf(::SearchArticlesUseCaseImpl) bind SearchArticlesUseCase::class
}
```

### ViewModel バインディング例

```kotlin
// feature/home のモジュール
val homeModule = module {
    viewModelOf(::HomeViewModel)
}
```

## 初期化

```kotlin
// Android
initKoin(newsApiKey = "YOUR_API_KEY", appContext = applicationContext)

// iOS
initKoin(newsApiKey = "YOUR_API_KEY")
```