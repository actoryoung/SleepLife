# 命令行构建指南

## 前置要求

1. **安装 JDK 17**
   - 下载: https://adoptium.net/ 或 https://www.oracle.com/java/technologies/downloads/
   - 安装后设置环境变量 `JAVA_HOME`

2. **安装 Android SDK**
   - 下载 Android SDK Command-line Tools
   - 或安装 Android Studio（只取 SDK）
   - 设置环境变量 `ANDROID_HOME` 或 `ANDROID_SDK_ROOT`

3. **设置环境变量**

在 Windows 系统属性中添加：
```
JAVA_HOME=C:\Program Files\Java\jdk-17
ANDROID_HOME=C:\Users\YourName\AppData\Local\Android\Sdk
Path=%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\tools;...
```

## 构建步骤

### 1. 首次构建（下载依赖）

```bash
gradlew.bat
```

查看可用任务：
```bash
gradlew.bat tasks
```

### 2. 构建 Debug APK

```bash
gradlew.bat assembleDebug
```

输出文件：`app\build\outputs\apk\debug\app-debug.apk`

### 3. 构建 Release APK

```bash
gradlew.bat assembleRelease
```

输出文件：`app\build\outputs\apk\release\app-release-unsigned.apk`

### 4. 安装到连接的设备

```bash
# 先检查设备连接
adb devices

# 安装 Debug 版本
gradlew.bat installDebug

# 或手动安装
adb install app\build\outputs\apk\debug\app-debug.apk
```

### 5. 清理构建

```bash
gradlew.bat clean
```

## 常用 Gradle 命令

| 命令 | 说明 |
|------|------|
| `gradlew.bat build` | 构建所有版本 |
| `gradlew.bat assembleDebug` | 构建 Debug APK |
| `gradlew.bat assembleRelease` | 构建 Release APK |
| `gradlew.bat installDebug` | 安装 Debug 版本到设备 |
| `gradlew.bat uninstallDebug` | 从设备卸载 |
| `gradlew.bat clean` | 清理构建缓存 |
| `gradlew.bat tasks` | 列出所有可用任务 |
| `gradlew.bat dependencies` | 查看依赖树 |

## 仅安装 SDK 不使用 Android Studio

### 方法 1: 使用 Command-line Tools

1. 下载 Command-line Tools:
   https://developer.android.com/studio#command-tools

2. 解压到指定目录（如 `C:\android-sdk`）

3. 运行以下命令安装必要组件：
```bash
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

4. 设置环境变量 `ANDROID_HOME=C:\android-sdk`

### 方法 2: 使用 scoop（推荐 Windows 用户）

```bash
# 安装 scoop
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
Invoke-RestMethod -Uri https://get.scoop.sh | Invoke-Expression

# 安装 Android SDK
scoop bucket add extras
scoop install android-sdk

# 安装必要的 SDK 组件
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

## 验证环境

运行以下命令验证环境是否正确：

```bash
# 检查 Java
java -version

# 检查 SDK
adb version

# 检查构建
gradlew.bat tasks --all
```

## 故障排除

**问题 1: JAVA_HOME 未设置**
```
ERROR: JAVA_HOME is not set
```
解决：安装 JDK 17 并设置环境变量

**问题 2: 找不到 Android SDK**
``<arg_value>SDK location not found
```
解决：在 `local.properties` 文件中添加：
```
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
```

**问题 3: 许可证未接受**
```
You have not accepted the license agreements
```
解决：
```bash
sdkmanager --licenses
```
然后按提示接受所有许可证

**问题 4: 构建失败**
```bash
gradlew.bat clean
gradlew.bat --stacktrace assembleDebug
```
使用 `--stacktrace` 查看详细错误信息
