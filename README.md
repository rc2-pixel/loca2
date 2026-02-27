# 現在地表示アプリ

GPSで現在地を取得し、日本語の住所を表示するAndroidアプリです。

## 機能
- GPSで現在地（緯度・経度）を取得
- Geocoderで住所（都道府県・市区町村・番地）に変換
- 「更新」ボタンで再取得

## ファイル構成
```
LocationApp/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/locationapp/
│       │   └── MainActivity.kt          ← メインロジック
│       └── res/
│           ├── layout/activity_main.xml ← UI レイアウト
│           └── values/
│               ├── strings.xml
│               └── themes.xml
├── build.gradle
└── settings.gradle
```

## セットアップ方法

1. Android Studio を開く
2. "Open an existing project" でこのフォルダを選択
3. Gradle sync を待つ
4. 実機またはエミュレータで実行（Run ▶️）

## 必要な環境
- Android Studio (最新版推奨)
- Android SDK 34
- 実機またはエミュレータ (Android 7.0以上)
- インターネット接続（住所変換に必要）

## 注意事項
- 初回起動時に位置情報の許可が求められます
- エミュレータの場合、Extended Controls > Location から位置を設定してください
