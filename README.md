# 数字九宫格 · 上传 GitHub & 自动构建 APK 指南

把网页版数独（4 个难度 × 每档 20 关，共 80 关）封装成安卓 App。
**本机不需要装 Android 开发环境**——推到 GitHub 后由 Actions 自动编译出 APK。

玩法：9×9 棋盘，每行、每列、每个 3×3 宫填 1-9 不重复。四档难度：简单 / 中级 / 高级 / 地狱，每档 20 关，顺序解锁并自动保存进度。

---

## 快速开始

### 第一步：在 GitHub 创建仓库

1. 打开 [github.com/new](https://github.com/new)
2. 仓库名填 `sudoku-nine`（或任意名字）
3. 选 **Private**（推荐，保护隐私；仓库里的签名文件也就不会公开）
4. **不要**勾选 "Add a README file"
5. 点击 **Create repository**

### 第二步：把代码推送上去

在这个 `sudoku-android` 文件夹里，打开终端（Windows 用 Git Bash 或 PowerShell）：

```bash
# 初始化 git
git init

# 添加所有文件
git add .

# 第一次提交
git commit -m "feat: 数字九宫格 安卓版初版"

# 关联你的 GitHub 仓库（把下面的 URL 换成你刚建的仓库地址）
git remote add origin https://github.com/你的用户名/sudoku-nine.git

# 推送
git branch -M main
git push -u origin main
```

推送成功后，GitHub Actions 会**自动开始构建**，无需任何额外操作。

> 不想用命令行？也可以在仓库页面点 `Add file → Upload files`，
> 打开解压出来的文件夹后按 `Ctrl+A` 全选里面的**所有内容**（注意别把外层文件夹本身拖进去），
> 一起拖到上传区提交。
> 提交后**务必检查**仓库里有没有 `.github/workflows/build-apk.yml`：
> 网页上传有时会漏掉点开头的隐藏目录，漏了的话点 `Add file → Create new file`，
> 文件名输入 `.github/workflows/build-apk.yml`，把本工程同路径文件的内容整段粘进去即可。

### 第三步：下载 APK

1. 打开你的 GitHub 仓库页面
2. 点击顶部的 **Actions** 标签
3. 找到最新一次 **Build APK** 任务，点进去
4. 等待构建完成（**通常 3~6 分钟**）
5. 页面底部 **Artifacts** 区域会出现：
   - `sudoku-9x9-apk-编号` → 点开下载 zip，解压后有两个 APK：
     - `sudoku-9x9-release.apk` → **正式版（推荐装这个）**
     - `sudoku-9x9-debug.apk` → 调试版（内容一样，只是带调试信息）
6. 把 APK 发到手机上安装（微信/QQ 传文件、数据线拷贝都行），
   手机上需要允许"未知来源应用"安装

**另一个更省事的下载入口**：构建成功后会自动生成 Release，
电脑或手机浏览器打开下面这个地址可直接下载，**链接长期有效**（Artifacts 30 天后会过期）：

```
https://github.com/你的用户名/sudoku-nine/releases/latest/download/sudoku-9x9-release.apk
```

> 仓库是 Private 时，这个链接需要先登录 GitHub。
> 手机上下载不方便的话，就在电脑上从 Actions 里下载后传给手机。

---

## 后续每次更新

改完游戏内容后（改法见下面"更新游戏内容"一节）：

```bash
git add .
git commit -m "feat: 调整了xxx"
git push
```

推送后 GitHub Actions 自动重新构建，几分钟后按同样的方式下载新 APK。
因为签名固定，**新版本可以直接覆盖安装，通关进度不会丢**。

---

## 工程结构

```
sudoku-android/
├── app/
│   ├── build.gradle.kts                    应用配置（包名、版本号、签名、SDK 版本）
│   └── src/main/
│       ├── AndroidManifest.xml             清单（不申请任何权限）
│       ├── assets/sudoku.html              游戏本体（单文件、离线、含 80 关题库）
│       ├── java/com/orange/sudoku/
│       │   └── MainActivity.java           WebView 外壳 + 返回键接管
│       └── res/                            图标、主题、应用名
├── keystore/sudoku.jks                     固定签名文件（务必保留）
├── .github/workflows/build-apk.yml         自动构建脚本
├── build.gradle.kts / settings.gradle.kts  工程配置
└── gradle.properties
```

App 名称、图标、包名等要改的话见文末"常用修改"。

---

## 关于签名（重要，和以前几个 App 的做法不同）

以前 `戒了么`、`AutoClicker` 等工程为了省事，`*.jks` 被 .gitignore 排除，CI 只能出**未签名**的 Release 包，
实际装的是 debug 包——而 debug 签名每次构建都会变，导致**新版本必须卸载旧版才能装，进度会清空**。

这个游戏把进度存在本机，所以我做了改进：

| 项目 | 说明 |
| --- | --- |
| 签名文件 | `keystore/sudoku.jks`，随仓库一起提交 |
| 密码 / 别名 | `sudoku123` / `sudoku` |
| 好处 | 每次构建签名一致 → 新版直接覆盖安装，**进度和星级不丢** |
| 注意 | 请勿外传；不要用它去应用商店上架（上架需要独立密钥） |

想沿用以前"不签名"的做法：删掉 `keystore/` 目录，并把 `app/build.gradle.kts` 里的
`signingConfigs { ... }` 和两处 `signingConfig = ...` 一起删掉即可。

---

## 更新游戏内容

游戏网页的源工程就是本工作区的 `sudoku-game/` 目录：

```bash
# 1. 改 sudoku-game/game.template.html（界面与逻辑）
#    或 sudoku-game/generate.js（题库难度参数，改完要重跑生成题库）
# 2. 重新生成网页
node sudoku-game/build.js
node sudoku-game/generate.js      # 仅题库参数有变动时才需要
# 3. 同步进安卓工程
cp 数字九宫格.html sudoku-android/app/src/main/assets/sudoku.html
# 4. 提交推送，GitHub 自动重新出包
```

只改网页的话，第 3 步做完直接 push 就行，不用动任何 Java 代码。

---

## 常用修改

| 想改什么 | 改哪里 |
| --- | --- |
| App 名称 | `app/src/main/res/values/strings.xml` 的 `app_name` |
| 版本号 | `app/build.gradle.kts` 的 `versionCode`（每次必须递增）/ `versionName` |
| 包名 | `app/build.gradle.kts` 的 `applicationId`（改了相当于另一个 App，可共存） |
| 图标 | `app/src/main/res/drawable/ic_launcher_background.xml`、`ic_launcher_foreground.xml`（矢量图，直接改颜色形状） |
| 最低安卓版本 | `app/build.gradle.kts` 的 `minSdk`（当前 26 = Android 8.0） |
| 允许横屏 | `AndroidManifest.xml` 里删掉 `android:screenOrientation="portrait"` |

---

## 构建配置说明

| 配置项 | 值 |
| --- | --- |
| 触发条件 | push 到 main/master 分支，或手动触发 |
| 构建环境 | Ubuntu Latest + JDK 17 |
| Gradle 版本 | 8.7 |
| Android Gradle Plugin | 8.6.0 |
| Compile / Target SDK | 34 |
| Min SDK | 26 (Android 8.0+) |
| 应用权限 | 无（不联网、不读存储） |
| Artifact 保留时间 | 30 天 |

---

## 常见问题

**Q：构建失败怎么办？**
A：点进 Actions 任务，展开失败的步骤查看日志，把错误信息发给我，我来修。

**Q：Actions 页面里一个任务都没有？**
A：`.github/workflows/build-apk.yml` 没上传成功，按第二步的说明补上即可。

**Q：构建成功了，但"发布 Release"那一步是红叉？**
A：仓库默认的令牌权限是只读。进 `Settings → Actions → General → Workflow permissions`，
选 `Read and write permissions` 保存，再点 `Re-run all jobs`。
这一步失败不影响使用，Artifacts 里的 APK 照样能下载。

**Q：手机上提示"解析软件包时出现问题"或"应用未安装"？**
A：确认手机 Android 版本 ≥ 8.0，并在设置里开启"允许安装未知来源应用"；
若装过不同签名的同包名版本，先卸载再装。

**Q：游戏进度会丢吗？**
A：正常覆盖安装不会丢（签名固定）。只有卸载重装才会清空。

**Q：想在 Android Studio 本地开发？**
A：直接用 Android Studio 打开 `sudoku-android` 文件夹，按提示 sync Gradle 后即可运行。
本工程刻意没有提交 `gradlew` 等二进制文件，本地需要的话在工程根目录执行
`gradle wrapper --gradle-version 8.7` 生成即可。
