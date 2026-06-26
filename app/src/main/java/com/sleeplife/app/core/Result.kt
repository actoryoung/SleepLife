package com.sleeplife.app.core

/**
 * 类型安全的操作结果包装器，用于统一处理所有 Repository 操作的成功/失败状态。
 *
 * Result wrapper for type-safe error handling across all repository operations.
 *
 * ## 三种状态
 *
 * - **Success<T>**: 操作成功，携带结果数据
 * - **Error**: 操作失败，携带 [AppException] 异常信息
 * - **Loading<T>**: 操作进行中（可选状态，用于 UI 加载指示）
 *
 * ## 使用示例
 *
 * ```kotlin
 * // Repository 层返回 Result
 * suspend fun insertItem(item: Item): Result<Long> {
 *     return try {
 *         val id = dao.insert(item)
 *         Result.Success(id)
 *     } catch (e: Exception) {
 *         Result.Error(AppException.DatabaseException("插入失败", e))
 *     }
 * }
 *
 * // ViewModel 层处理 Result
 * when (val result = repository.insertItem(item)) {
 *     is Result.Success -> println("ID: ${result.data}")
 *     is Result.Error -> println("错误: ${result.exception.message}")
 *     is Result.Loading -> println("加载中...")
 * }
 * ```
 *
 * @param T 成功时携带的数据类型
 * @see AppException 异常层次结构
 * @since 1.0.0
 */
sealed class Result<out T> {
    /**
     * 操作成功状态，携带结果数据。
     *
     * @param data 操作成功返回的数据
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * 操作失败状态，携带异常信息。
     *
     * @param exception 操作失败的异常对象（必须是 [AppException] 子类）
     */
    data class Error(val exception: AppException) : Result<Nothing>()

    /**
     * 操作进行中状态（可选）。
     *
     * 用于 UI 显示加载指示器，通常在耗时操作开始时设置。
     */
    class Loading<T> : Result<T>()

    /**
     * 对成功结果执行转换操作。
     *
     * 如果当前是 Success 状态，对 data 应用 transform 函数；
     * 如果是 Error 或 Loading，原样返回。
     *
     * @param transform 数据转换函数
     * @return 转换后的 Result
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(exception)
        is Loading -> Loading()
    }

    /**
     * 链式调用返回 Result 的操作（Monad flatMap）。
     *
     * 用于组合多个可能失败的操作。
     *
     * ```kotlin
     * repository.getItem(id)
     *     .flatMap { item -> repository.updateItem(item) }
     *     .flatMap { updatedId -> repository.getItem(updatedId) }
     * ```
     *
     * @param transform 返回 Result 的转换函数
     * @return 转换后的 Result
     */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> Error(exception)
        is Loading -> Loading()
    }

    /**
     * 安全获取数据，失败时返回 null。
     *
     * @return 成功时返回数据，失败或加载中时返回 null
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
        is Loading -> null
    }

    /**
     * 检查是否为成功状态。
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * 检查是否为失败状态。
     */
    fun isError(): Boolean = this is Error

    /**
     * 检查是否为加载中状态。
     */
    fun isLoading(): Boolean = this is Loading
}

/**
 * 应用程序异常层次结构，用于分类错误类型。
 *
 * 所有业务异常都应继承此类，便于统一处理和日志记录。
 *
 * ## 异常分类原则
 *
 * - **ValidationException**: 用户可纠正的错误（如输入格式错误）
 * - **DatabaseException**: 系统错误（如磁盘空间不足、数据库损坏）
 * - **UnknownException**: 未预期的错误（如 NullPointerException）
 *
 * @param message 异常描述信息（面向用户）
 * @param cause 原始异常（可选，便于调试）
 * @since 1.0.0
 */
sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /**
     * 数据库操作异常。
     *
     * 包括但不限于：
     * - 数据库连接失败
     * - 磁盘空间不足
     * - 数据损坏
     * - SQL 语法错误
     *
     * **日志级别**: ERROR  
     * **用户提示**: 通用错误提示（避免暴露技术细节）
     *
     * @param message 错误描述（如"保存失败，请稍后重试"）
     * @param cause 原始数据库异常
     */
    class DatabaseException(message: String, cause: Throwable? = null) :
        AppException(message, cause)

    /**
     * 业务规则验证失败。
     *
     * 包括但不限于：
     * - 必填字段为空
     * - 数值超出合法范围
     * - 格式不正确
     * - 业务逻辑约束不满足
     *
     * **日志级别**: WARN  
     * **用户提示**: 具体验证失败原因（可直接显示给用户）
     *
     * @param message 验证失败的具体原因（如"睡眠时长必须在 0-24 小时之间"）
     */
    class ValidationException(message: String) :
        AppException(message)

    /**
     * 未预期的异常。
     *
     * 用于包装所有未明确分类的异常（如 NullPointerException, IllegalStateException）。
     *
     * **日志级别**: ERROR  
     * **用户提示**: 通用错误提示  
     * **行动**: 应上报到 Crashlytics 等错误追踪服务
     *
     * @param message 错误描述
     * @param cause 原始异常
     */
    class UnknownException(message: String, cause: Throwable? = null) :
        AppException(message, cause)
}
