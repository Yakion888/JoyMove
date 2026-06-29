# Bug 修复报告：打卡提交显示"网络错误"

**报告日期**：2026-06-29
**报告人**：开发排查
**严重程度**：高
**状态**：已修复

---

## 1. 问题描述

**现象**：  
在打卡页面填写表单后点击「完成打卡」，提示"网络错误"，打卡失败。

**影响范围**：  
所有用户的打卡功能。

---

## 2. 复现步骤

1. 登录系统，进入打卡页面 `/checkin`
2. 填写运动项目、孩子、时长等必填项
3. 点击「✅ 完成打卡」
4. 看到红色错误提示「网络错误」

**预期结果**：打卡成功，弹出「打卡成功！🎉」
**实际结果**：打卡失败，显示「网络错误」

---

## 3. 根因分析

经过排查，**不是单一 bug，而是"前端错误提示吞没 + 后端异常处理不足"的组合问题**。

### 3.1 前端：`error` 回调丢弃服务器错误信息

**问题代码**：[checkin.html:290-291](src/main/resources/templates/checkin.html#L290)

```javascript
error: function() {
    $('#errorBox').removeClass('d-none').text('网络错误');
}
```

无论服务器返回 400、401、500 还是其他错误，用户一律只看到"网络错误"。真正的错误信息在 `xhr.responseText` 中，但被完全丢弃。

### 3.2 后端：`getCurrentUser()` 返回 null 导致 NPE

**问题代码**：[FamilyMomentController.java:93-94](src/main/java/com/sportsblog/controller/FamilyMomentController.java#L93)

```java
User user = getCurrentUser();    // 可能返回 null
moment.setUserId(user.getId());  // NPE → 500 → "网络错误"
```

触发场景：会话未过期但数据库中用户记录被删除 / 用户名变更。

### 3.3 后端：缺少常见异常处理器

`GlobalExceptionHandler` 只处理了 `MethodArgumentNotValidException`、`BusinessException` 和兜底 `Exception`。

**缺失的处理器**：

| 异常 | 触发条件 | 当前表现 |
|------|----------|----------|
| `MethodArgumentTypeMismatchException` | 未选项目/孩子就提交 | 500 → "网络错误" |
| `MissingServletRequestParameterException` | 请求缺少必填参数 | 500 → "网络错误" |
| `MaxUploadSizeExceededException` | 上传图片 > 2MB | 500 → "网络错误" |

这些异常全被兜底 `@ExceptionHandler(Exception.class)` 捕获，返回 HTTP 500 + "服务器内部错误"，前端再吞掉这个消息，用户只看到"网络错误"。

---

## 4. 修复方案

**修改文件**：

| 文件 | 修改类型 | 说明 |
|------|----------|------|
| [checkin.html](src/main/resources/templates/checkin.html) | 修改 | `error` 回调解析 `xhr.responseText.msg` 展示真实错误 |
| [FamilyMomentController.java](src/main/java/com/sportsblog/controller/FamilyMomentController.java) | 修改 | `create()` 增加 `user == null` 检查，返回 401 |
| [GlobalExceptionHandler.java](src/main/java/com/sportsblog/common/GlobalExceptionHandler.java) | 新增 3 个方法 | 处理类型不匹配、缺参数、文件过大异常 |

**修复前/后对比**：

```diff
# checkin.html
- error: function() {
-     $('#errorBox').removeClass('d-none').text('网络错误');
+ error: function(xhr) {
+     var msg = '网络错误，请稍后重试';
+     try {
+         var res = JSON.parse(xhr.responseText);
+         if (res && res.msg) msg = res.msg;
+     } catch(e) {}
+     $('#errorBox').removeClass('d-none').text(msg);

# FamilyMomentController.java
  User user = getCurrentUser();
+ if (user == null) return Result.error(ResultCode.UNAUTHORIZED);

# GlobalExceptionHandler.java — 新增
+ @ExceptionHandler(MethodArgumentTypeMismatchException.class)  → "参数格式不正确"
+ @ExceptionHandler(MissingServletRequestParameterException.class) → "缺少必填参数"
+ @ExceptionHandler(MaxUploadSizeExceededException.class) → "文件过大"
```

---

## 5. 验证

**验证步骤**：
1. 正常提交打卡 → 应成功，显示「打卡成功！🎉」
2. 不选运动项目直接提交 → 显示「参数错误：projectId 格式不正确」（而非"网络错误"）
3. 不选孩子直接提交 → 显示「参数错误：childId 格式不正确」（而非"网络错误"）
4. 上传超过 2MB 的图片 → 显示「文件过大，单张图片不超过 2MB」
5. 未登录状态调用 API → 显示「未授权，请先登录」

**回归检查**：
- [x] 正常打卡流程不受影响
- [x] 日历刷新正常
- [x] 看板数据不受影响

---

## 6. 经验教训

1. **前端错误回调必须解析服务器响应**：`error` 回调的第一个参数 `xhr` 包含了服务器返回的所有信息，直接丢弃等于蒙着眼睛调试
2. **Controller 入口做空检查**：`getCurrentUser()` 等可能返回 null 的方法，调用后立即判空
3. **异常处理器要覆盖常见场景**：参数类型不匹配、文件过大、缺参数是 Web 应用的常见异常，应有专门处理器给出友好提示
4. **"网络错误"不是万能兜底文案**：它掩盖了真实问题，延长了排查时间
