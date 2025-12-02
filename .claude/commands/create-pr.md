# Create Pull Request

関連Issueに基づいてPull Requestを作成します。

## 引数
- $ARGUMENTS: Issue番号（例: `6`, `#6`）

## 実行手順

1. **引数の検証**: `$ARGUMENTS` が空の場合、ユーザーにIssue番号を質問

2. **現在のブランチ確認**: mainブランチ以外であることを確認

3. **Issue情報の取得**: `gh issue view {issue_number}`
    - Issueタイトル → PRタイトルに使用
    - newsflow-library の関連URL抽出

4. **差分の確認**: `git diff main...HEAD`

5. **PRの作成**: 以下の形式で作成

## PR本文テンプレート

```markdown
## 概要

{変更内容の簡潔な説明}

## 変更内容

- {変更点1}
- {変更点2}
- {変更点3}

## 関連Issue

Closes #{issue_number}

**Related (newsflow-library)**: {抽出したURL}

## スクリーンショット

Before | After
:--: | :--:
<img src="" width="300" /> | <img src="" width="300" /> 

## チェックリスト

- [ ] ビルド・Detektが成功
- [ ] 動作確認済み
```

6. **PR作成コマンドの実行**: `gh pr create --title "{Issueタイトル}" --body "{PR本文}"`

7. **結果の報告**: 作成されたPRのURLを報告