# SleepLife

> 个人生活管理 Android 应用

## 简介

SleepLife 是一个功能丰富的个人生活管理应用，帮助你追踪睡眠、培养习惯、记录笔记和提高专注力。

## 功能

### 核心功能

- **睡眠追踪** 🌙
  - 一键开始/结束睡眠记录
  - 记录睡眠质量和备注
  - 查看最近7天睡眠历史

- **习惯打卡** ✅
  - 创建自定义习惯
  - 每日打卡追踪
  - 可视化进度条
  - 目标天数设定

- **笔记日记** 📝
  - 快速记录想法和日记
  - 心情标记
  - 标签分类
  - 收藏功能

- **番茄钟专注** ⏱️
  - 自定义专注时长
  - 任务名称记录
  - 今日专注统计
  - 完成历史查看

### 即将推出

- 睡眠统计和趋势分析
- 习惯报告生成
- 智能起床闹钟
- 白噪音播放
- 数据云端备份
- 更多主题定制

## 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构**: MVVM + Clean Architecture
- **依赖注入**: Hilt
- **数据库**: Room
- **异步处理**: Kotlin Coroutines + Flow
- **导航**: Navigation Compose
- **最低支持**: Android 8.0 (API 26)

## 项目结构

```
app/src/main/java/com/sleeplife/app/
├── data/                      # 数据层
│   ├── entities/             # 数据库实体
│   ├── dao/                  # 数据访问对象
│   ├── repository/           # 仓库层
│   └── SleepLifeDatabase.kt  # 数据库配置
├── di/                       # 依赖注入模块
├── ui/                       # UI层
│   ├── screens/              # 各功能屏幕
│   │   ├── sleep/           # 睡眠模块
│   │   ├── habits/          # 习惯模块
│   │   ├── notes/           # 笔记模块
│   │   ├── pomodoro/        # 专注模块
│   │   └── more/            # 更多页面
│   ├── viewmodels/          # ViewModel
│   ├── navigation/          # 导航配置
│   ├── theme/               # 主题配置
│   └── components/          # 通用组件
├── MainActivity.kt          # 主Activity
└── SleepLifeApplication.kt  # Application类
```

## 构建项目

### 前置要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34
- Gradle 8.2

### 构建步骤

1. 克隆或下载项目
2. 使用 Android Studio 打开项目
3. 等待 Gradle 同步完成
4. 连接 Android 设备或启动模拟器
5. 点击 Run 按钮构建并安装

### 命令行构建

```bash
# Windows
gradlew.bat assembleDebug

# Linux/macOS
./gradlew assembleDebug

# 安装到设备
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 数据库

应用使用 Room 数据库存储所有数据：

- `sleep_records` - 睡眠记录
- `habits` - 习惯列表
- `habit_checkins` - 打卡记录
- `notes` - 笔记
- `pomodoro_sessions` - 番茄钟会话

## 贡献

欢迎贡献！请遵循以下步骤：

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 许可证

MIT License

## 作者

Created with Claude Code

## 更新日志

### v1.0.0 (2024)
- 初始版本发布
- 实现睡眠追踪功能
- 实现习惯打卡功能
- 实现笔记日记功能
- 实现番茄钟专注功能
