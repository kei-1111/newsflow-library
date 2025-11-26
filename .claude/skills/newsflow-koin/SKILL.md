---
name: newsflow-koin
description: newsflow-libraryのKoin DI設定規約を適用します。*Module.ktファイル作成、Koin.kt編集、依存性注入設定時に使用してください。
---

# Newsflow Koin Rules

## モジュール定義パターン

### Repository/UseCase（singleOf + bind）

```kotlin
val dataModule = module {
    singleOf(::NewsRepositoryImpl) bind NewsRepository::class
}

val domainModule = module {
    singleOf(::FetchTopHeadlineArticlesUseCaseImpl) bind FetchTopHeadlineArticlesUseCase::class
}
```

### ViewModel（viewModelOf）

```kotlin
val homeModule = module {
    viewModelOf(::HomeViewModel)
}
```

## スコープ選択

| スコープ | 用途 |
|---------|------|
| `singleOf` | Repository, UseCase, HttpClient |
| `viewModelOf` | ViewModel |

## 新規モジュール追加手順

1. **di/{Name}Module.kt 作成**
```kotlin
package io.github.kei_1111.newsflow.library.feature.{name}.di

val {name}Module = module {
    viewModelOf(::{Name}ViewModel)
}
```

2. **shared/Koin.kt に追加**（Android/iOS両方）

```kotlin
// shared/src/androidMain/.../Koin.kt
// shared/src/iosMain/.../Koin.kt

import io.github.kei_1111.newsflow.library.feature.{name}.di.{name}Module

fun initKoin(...) {
    startKoin {
        modules(
            networkModule,
            dataModule,
            domainModule,
            homeModule,
            viewerModule,
            {name}Module,  // 追加
        )
    }
}
```

## トラブルシューティング

| エラー | 原因 | 対処 |
|-------|------|------|
| No definition found | モジュール未登録 | shared/Koin.ktに追加 |
| Cycle detected | 循環依存 | アーキテクチャ見直し |
| Multiple definitions | 重複バインド | 既存定義を確認 |