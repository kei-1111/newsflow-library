# Create Pull Request

関連Issueに基づいてPull Requestを作成します。

## 引数
- $ARGUMENTS: Issue番号（例: `6`, `#6`）

## 実行手順

1. **引数の検証**: `$ARGUMENTS` が空の場合、ユーザーにIssue番号を質問してください

2. **現在のブランチ確認**: 現在のブランチがmainブランチ以外であることを確認
   ```bash
   git branch --show-current
   ```

3. **Issue情報の取得**: GitHub CLIでIssue詳細を取得
   ```bash
   gh issue view {issue_number}
   ```
   - Issueタイトルを取得（PRタイトルに使用）
   - Issue内容を把握（PR概要の参考に使用）
   - newsflow-android の関連Issue/PR URLを抽出（`https://github.com/kei-1111/newsflow-android/...` 形式）

4. **差分の確認**: mainブランチとの差分を取得
   ```bash
   git diff main...HEAD --stat
   git diff main...HEAD
   ```

5. **PRテンプレートの参照**: @.github/PULL_REQUEST_TEMPLATE.md を参照

6. **PRの作成**: 以下の形式でPRを作成
   - **タイトル**: Issueタイトルをそのまま使用
   - **本文**: 以下のセクションのみ含める（不要なセクションは削除）

### PR本文テンプレート

```markdown
## 概要 / Overview

{変更内容の簡潔な説明}

## 変更内容 / Changes

- {変更点1}
- {変更点2}
- {変更点3}

## 関連Issue / Related Issues

Closes #{issue_number}

{newsflow-android の関連URLがIssue内にある場合のみ以下を追記}
**Related (newsflow-android)**: - {抽出したURL}

## テスト / Testing

- [ ] ユニットテストを追加/更新 / Added/updated unit tests
- [ ] 手動テストを実施 / Manual testing performed
- [ ] Detektチェックを通過 / Detekt checks passed
- [ ] ビルドが成功 / Build succeeds

## レビュワーへのメモ / Notes for Reviewers

{レビュワーに特に見てほしい点や設計判断の理由}
```

7. **PR作成コマンドの実行**:
   ```bash
   gh pr create --title "{Issue タイトル}" --body "{PR本文}"
   ```

8. **結果の報告**: 作成されたPRのURLをユーザーに報告

## 注意事項

- テストセクションのチェックボックスは、ユーザーに確認してから適切にマークする
- レビュワーへのメモは、設計判断や特に注目してほしい点がある場合のみ記載
- 変更内容は `git diff` の結果から具体的に記述する
- 不要なセクション（チェックリスト、その他など）は含めない