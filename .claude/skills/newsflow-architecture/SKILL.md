---
name: newsflow-architecture
description: newsflow-libraryのモジュール依存関係とファイル配置規約を適用します。新規モジュール作成、build.gradle.kts編集、settings.gradle.kts編集、パッケージ構造変更時に使用してください。
---

# Newsflow Architecture Rules

## モジュール依存フロー（厳守）

```
基盤層（依存なし）:
├── core:model
└── core:network

データ層:
└── core:data → core:model, core:network

ドメイン層:
└── core:domain → core:data, core:model

プレゼンテーション層:
└── feature:* → core:domain, core:model, core:mvi, core:logger

独立:
├── core:mvi, core:logger（どこからでも利用可）
├── core:test（testImplementationのみ）
└── shared（全モジュール集約）
```

## 禁止事項チェックリスト

新規コード追加時、以下を確認:

- [ ] feature間の依存を作っていないか（feature:home → feature:viewer は禁止）
- [ ] レイヤー逆転していないか（core:model → core:data は禁止）
- [ ] ViewModelがRepositoryを直接使っていないか（UseCase経由必須）

## Convention Plugin選択

| モジュール種別 | プラグイン |
|--------------|-----------|
| core:* | `alias(libs.plugins.newsflow.library.kmp.library)` |
| feature:* | `alias(libs.plugins.newsflow.library.kmp.feature)` |

feature用プラグインは core:mvi, core:model, core:domain, core:logger, Koin, テスト依存を自動インクルード。

## 新規モジュール追加手順

1. `settings.gradle.kts` に `include(":core:xxx")` または `include(":feature:xxx")` 追加
2. 適切なConvention Plugin適用
3. namespace設定: `io.github.kei_1111.newsflow.library.{layer}.{name}`

## 可視性規則

```kotlin
interface NewsRepository { ... }           // public
internal class NewsRepositoryImpl { ... }  // internal
val dataModule = module { ... }            // public（sharedから参照）
```