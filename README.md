# Galaxy V1.0

Galaxy 是一个本地离线习惯打卡日历。

## V1.0
- 每日打卡
- 连续天数
- 最长连续
- 累计正常打卡
- 补签卡
- 每累计正常打卡 30 天获得 1 张补签卡
- 补签卡最多保存 3 张
- 补签只能用于过去漏签日期
- 数据保存在 Android 本机 SharedPreferences

## 构建
需要 Android Studio + Android SDK。
打开本目录后等待 Gradle Sync，然后运行 app 或 Build APK。


## GitHub 一键云端构建

项目已经附带 `.github/workflows/build-apk.yml`。
上传到 GitHub 后，在仓库的 Actions 页面手动运行 `Build Galaxy APK`，
构建完成后即可从该次运行的 Artifacts 下载 `Galaxy-V1.0-APK`。

GitHub Actions 使用 Ubuntu runner、JDK 17 和 Android SDK 构建调试版 APK。
