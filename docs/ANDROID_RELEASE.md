# リリース手順

newsflow-library を GitHub Packages にリリースする手順です。

## 前提条件

- `main` ブランチに全ての変更がマージされていること
- CI（テスト、detekt）がすべて通過していること

## 手順

### 1. バージョンを更新

`gradle/libs.versions.toml` の `newsflowLibrary` を更新:

```toml
newsflowLibrary = "X.Y.Z"
```

### 2. 変更をコミット・プッシュ

```bash
git add gradle/libs.versions.toml
git commit -m "chore: バージョンをX.Y.Zへ更新"
git push origin main
```

### 3. タグを作成・プッシュ

```bash
git tag vX.Y.Z
git push origin vX.Y.Z
```

### 4. 自動リリース

タグのプッシュにより `android-release.yml` が実行され、GitHub Packages に自動公開されます。

## 公開先

- **GitHub Packages**: `https://maven.pkg.github.com/kei-1111/newsflow-library`

## 利用方法

リリース後、以下のように依存関係を追加:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/kei-1111/newsflow-library")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.token").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

// build.gradle.kts
dependencies {
    implementation("io.github.kei-1111.newsflow.library:shared:X.Y.Z")
}
```