# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Top-Level Rules

- To maximize efficiency, **if you need to execute multiple independent processes, invoke those tools concurrently, not sequentially**.
- **You must think exclusively in English**. However, you are required to **respond in Japanese**.

## WHAT: プロジェクト概要

newsflow-libraryは、Android・iOS向けのKotlin Multiplatform (KMP)ニュース配信ライブラリ。Clean Architecture + MVIパターンを採用。

## WHY: 設計原則

### 絶対に守るべきルール

1. **フィーチャーモジュール間の依存は絶対に作らない** - feature:homeからfeature:viewerへの依存など禁止
2. **実装クラスは`internal`、インターフェースは`public`** - Koinバインディングのため必須
3. **テストは`src/commonTest/kotlin/`のみ** - プラットフォーム固有テストは作成しない
4. **ViewModelStateの`toState()`は純粋関数** - 副作用禁止

### よくある間違い

- ❌ フィーチャーからcore:dataやcore:networkを直接参照 → ✅ core:domain経由のみ
- ❌ ViewModelでリポジトリを直接使用 → ✅ UseCaseを経由
- ❌ Stateをミュータブルにする → ✅ ViewModelStateでミュータブル管理、Stateはイミュータブル
- ❌ Dispatcherをハードコード → ✅ コンストラクタ注入で設定

## HOW: 開発方法

### よく使うコマンド

| コマンド | 説明 |
|---------|------|
| `./gradlew allTests` | 全モジュールのテスト実行 |
| `./gradlew :core:domain:allTests` | 特定モジュールのテスト |
| `./gradlew detekt` | 静的解析（コード品質チェック） |
| `./gradlew allTests detekt` | テスト＋静的解析（実装完了時に実行） |

### 重要ファイル参照

| 内容 | 参照先 |
|------|--------|
| MVI基底クラス | `core/mvi/src/commonMain/kotlin/.../StatefulBaseViewModel.kt:19` |
| エラー型定義 | `core/model/src/commonMain/kotlin/.../NewsflowError.kt:3` |
| UseCase実装例 | `core/domain/src/commonMain/kotlin/.../FetchTopHeadlineArticlesUseCaseImpl.kt:6` |
| ViewModel実装例 | `feature/home/src/commonMain/kotlin/.../HomeViewModel.kt:12` |
| テスト例 | `feature/home/src/commonTest/kotlin/.../HomeViewModelTest.kt:28` |
| バージョン情報 | `gradle/libs.versions.toml` |

### 推奨ワークフロー

**新機能実装時:**
1. **探索**: 関連コードを読んで既存パターンを理解
2. **計画**: 実装ステップを整理（必要に応じてTodoWriteを使用）
3. **テスト作成**: Mokkeryを使った失敗テストを先に書く
4. **実装**: テストを通すコードを実装
5. **検証**: `./gradlew allTests detekt`で品質確認

**デバッグ時:**
1. エラーメッセージから`NewsflowError`の型を特定
2. 関連するRepository/UseCaseのテストを確認
3. モックの設定が正しいか検証

### 開発環境

- JDK 21
- Android Studio with KMP plugin
- Xcode（iOS開発時）

## 詳細ドキュメント

特定タスクの詳細は以下を参照:

| ドキュメント | 内容 |
|-------------|------|
| `agent-docs/ARCHITECTURE.md` | モジュール構造、依存関係、Convention Plugins |
| `agent-docs/MVI_PATTERN.md` | MVI実装パターン、ファイル構成、テンプレート |
| `agent-docs/TESTING.md` | テスト戦略、Mokkery使用法、テストデータ生成 |
| `agent-docs/KOIN_DI.md` | Koin DI設定、バインディングパターン |