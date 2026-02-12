# MTR アドオンテンプレート

[Minecraft Transit Railway](https://github.com/jonafanho/Minecraft-Transit-Railway) 用アドオンをコンパイルするためのテンプレートです。

一般的なクロスローダーやクロスバージョン設定にも使用可能です。その場合は MTR 依存関係を削除してください。



## 注意事項

すべてのファイル内で「MtrAddonTemplate」に関連するパッケージ名と参照を必ず検索・置換してください！そうしないと、他のMODと競合する可能性があります。

`mtraddontemplate`、`mtr-addon-template`、`MtrAddonTemplate` を検索してください。



## セットアップ

1. このリポジトリをクローンする
2. Gradleプロジェクトを同期する
3. Minecraftバージョンを切り替える場合、または初回実行時：
   1. Gradleプロジェクトを同期する
   2. ルートプロジェクトでGradleの`setupFiles`タスクを実行する
   3. Gradleプロジェクトを再度同期する

*原文をDeepL.com（無料版）で翻訳しましたが、一部変更点があります。


# MTR Addon Template

An template for compiling addons for [Minecraft Transit Railway](https://github.com/jonafanho/Minecraft-Transit-Railway).

Also can be used for general cross-loader and cross-version setups, just delete the MTR dependencies.



## Note

Make sure you search and replace all package names and references of "MtrAddonTemplate" in all files! Otherwise, your mod might conflict with others.

Look for `mtraddontemplate`, `mtr-addon-template` and `MtrAddonTemplate`.



## Setup

1. Clone this repository
2. Sync the Gradle project
3. To switch Minecraft versions or on first run:
   1. Sync the Gradle Project
   2. In the root project, run the Gradle `setupFiles` task
   3. Sync the Gradle Project again

