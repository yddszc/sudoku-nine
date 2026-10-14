# 数字九宫格 · 安卓版

把网页版数独（4 个难度 × 每档 20 关，共 80 关）封装成安卓 App。
**不需要在本机装 Android 开发环境**——推到 GitHub 后由 Actions 自动编译出 APK。

- 游戏本体：`app/src/main/assets/sudoku.html`（单文件、离线、自带 80 关题库）
- 外壳：`app/src/main/java/com/orange/sudoku/MainActivity.java`（WebView 承载 + 返回键接管）
- 无任何权限申请：不联网、不读存储，纯单机

---

## 一、三步拿到 APK

### 第 1 步：在 GitHub 建一个空仓库

打开 <https://github.com/new>，仓库名填 `sudoku-nine`（随意），
**不要**勾选 "Add a README file"，然后点 `Create repository`。

### 第 2 步：把本工程传上去（两种方式任选）

**方式 A：网页拖拽（不用装任何工具，推荐）**

1. 解压本工程压缩包，进入解压后的文件夹
2. 在仓库页面点 `uploading an existing file`（或 `Add file → Upload files`）
3. 把文件夹里的**所有内容**（`app`、`keystore`、`.github`、`build.gradle` 等）拖进上传区
4. 点 `Commit changes`

> 如果 `.github` 文件夹没被上传（网页上传偶尔会忽略以点开头的目录）：
> 在仓库里点 `Add file → Create new file`，文件名输入
> `.github/workflows/android.yml`（输入 `/` 会自动建目录），
> 然后把本工程里同路径文件的内容整段粘贴进去，提交即可。

**方式 B：命令行（本机已装 git）**

```bash
cd 解压后的目录
git init
git add .
git commit -m "数字九宫格 安卓版"
git branch -M main
git remote add origin https://github.com/你的用户名/sudoku-nine.git
git push -u origin main
```

首次推送会弹出 GitHub 登录窗口，登录一次即可。

### 第 3 步：等 3-5 分钟，下载 APK

推送成功后 GitHub 会**自动开始编译**：

- 看进度：仓库页顶部 `Actions` 标签 → 点最新一次 `构建 APK`
- 下载方式一（推荐，手机直接装）：
  编译成功后会自动发布到 Releases，手机浏览器打开
  `https://github.com/你的用户名/sudoku-nine/releases/latest/download/sudoku-9x9.apk`
  即可直接下载安装（仓库设为 Public 时无需登录）
- 下载方式二：在 Actions 那次运行的页面底部 `Artifacts` 里下载 `sudoku-9x9-apk`（需登录 GitHub）

**安装时**手机可能提示"未知来源应用"，在弹窗里选择允许安装即可。

---

## 二、常用修改

| 想改什么 | 改哪里 |
| --- | --- |
| 游戏内容/题库/界面 | 替换 `sudoku-game/game.template.html` 后重新生成并替换 `app/src/main/assets/sudoku.html`（见下） |
| App 名称 | `app/src/main/res/values/strings.xml` 里的 `app_name` |
| 版本号 | `app/build.gradle` 里的 `versionCode`（每次必须递增）/ `versionName` |
| 包名 | `app/build.gradle` 的 `applicationId`（改了就是另一个 App，能共存） |
| 图标 | `app/src/main/res/drawable/ic_launcher_background.xml` 与 `ic_launcher_foreground.xml`（矢量图，直接改颜色/形状） |
| 最低安卓版本 | `app/build.gradle` 的 `minSdk`（当前 26 = Android 8.0） |
| 允许横屏 | `AndroidManifest.xml` 中删掉 `android:screenOrientation="portrait"` |

### 更新游戏内容的标准流程

游戏网页的源工程就是本工作区的 `sudoku-game/` 目录：

```bash
# 1. 改 game.template.html（界面/逻辑）或 generate.js（题库难度参数）
# 2. 重新生成网页 + 重新生成题库
node sudoku-game/build.js        # 生成 数字九宫格.html
node sudoku-game/generate.js     # 只有在改题库参数时才需要
# 3. 同步进安卓工程
cp 数字九宫格.html sudoku-android/app/src/main/assets/sudoku.html
# 4. 提交推送，GitHub 自动重新出包
```

---

## 三、关于签名（重要）

`keystore/sudoku.jks` 是**随仓库一起提交**的固定签名文件：

- 密码：`sudoku123`，别名：`sudoku`
- 作用是让每次编译出的 APK 签名一致，这样**新版本能直接覆盖安装旧版本**，
  玩家的关卡进度不会丢
- 如果换了签名文件，手机上必须先卸载旧版才能装新版，**通关进度会清空**
- 请不要把这个签名文件用于正式上架（应用商店要求独立密钥，且泄露有风险）
- 仓库如果是 Public，任何人都能看到这个密钥。介意的话把仓库设为 Private

---

## 四、构建失败怎么排查

| 现象 | 原因与处理 |
| --- | --- |
| Actions 页面提示工作流语法错误 | `.github/workflows/android.yml` 粘贴时缩进被破坏，重新整段粘贴一次 |
| `Could not find or load main class` / `gradle: command not found` | 编译步骤少了 `setup-gradle`，确认工作流里 `gradle/actions/setup-gradle@v4` 那一步在编译之前 |
| `Failed to find Platform SDK with path: platforms;android-34` | 工作流里"补齐 Android SDK 组件"那步没执行成功，重新 Run workflow 一次 |
| `Keystore file not found` | `keystore/sudoku.jks` 没上传成功（网页上传请确认该文件包含在内） |
| 手机提示"应用未安装"/"签名冲突" | 手机上已装有不同签名的同包名版本，先卸载再装 |
| 混淆相关报错 | 本工程 `minifyEnabled false`，若你手动开启需自行补 keep 规则 |

---

## 五、本机编译（可选，装了 Android Studio 之后）

本工程刻意没有包含 `gradlew` 二进制文件。本机想构建时（需 JDK 17 + Android Studio）：

```bash
cd sudoku-android
gradle wrapper --gradle-version 8.7     # 生成 gradlew
./gradlew assembleRelease
# 产物：app/build/outputs/apk/release/app-release.apk
```

或用 Android Studio 直接 `Open` 本目录，它会自动补齐 Gradle 配置。

---

## 六、工作流做了什么

`.github/workflows/android.yml` 每次推送（或在 Actions 页面手动触发）会：

1. 检出代码
2. 装 JDK 17（Android Gradle Plugin 8.5 要求）
3. 装 Gradle 8.7
4. 补齐 `platforms;android-34`、`build-tools;34.0.0`
5. 执行 `gradle assembleRelease`
6. 上传 APK 到 Actions Artifacts
7. 把 APK 发布到 Releases 的 `latest` 标签，形成一个固定下载链接
