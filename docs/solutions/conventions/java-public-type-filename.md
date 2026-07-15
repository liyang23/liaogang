---
title: "Java 规范：public type 必须与文件名匹配"
date: "2026-07-15"
category: "conventions"
module: "backend"
problem_type: "convention"
component: "tooling"
severity: "medium"
applies_when:
  - "Java 公共类型（class / interface / enum / record / @interface）跨包引用时"
  - "IDE / 编译期 / 测试运行时报'公共类型应在名为 X.java 的文件中声明'"
symptoms:
  - "javac 报 '接口 AuditLog 是公共的, 应在名为 AuditLog.java 的文件中声明' —— public @interface AuditLog 在 AuditLogAnnotation.java 里"
  - "修复后 javac 报 '类重复: com.liaogang.famou.km.audit.AuditLog' —— 改名后同包两个 public type 同名"
  - "IDE 自动 import 时找不到类（应该 import X 但实际编译单元叫 XImplementation）"
domain: "java-language"
pattern: "public-type-filename-convention"
rejected_alternatives:
  - "把 @interface 改成 non-public — 拒绝：注解必须 public 才能被其他包引用（如 @Around 切面）"
  - "保留文件名 AuditLogAnnotation.java 改类名为 AuditLogAnnotation — 接受：可行但所有 @AuditLog 引用要改成 @AuditLogAnnotation 改名波及面广"
  - "实体类改名为 AuditLogRecord — 拒绝：AuditLogRecord 在 Java 14+ 是 record 关键字提示，混淆概念"
applicable_versions:
  - "Java 8+"
  - "Java 17"
invalidation_condition: "JEP 463 / 447 等未来 Java 版本可能放宽此规则（不太可能，但留意）"
source_refs:
  - "backend/src/main/java/com/liaogang/famou/km/audit/AuditLogAnnotation.java"
  - "backend/src/main/java/com/liaogang/famou/km/audit/AuditLog.java"
  - "backend/src/main/java/com/liaogang/famou/km/audit/AuditLogEntity.java"
  - "docs/solutions/build-errors/2026-07-15-001-sprint-1-compile-gate-missing.md"
tags:
  - "java"
  - "naming-convention"
  - "public-type"
  - "file-name"
  - "lombok"
  - "sprint-1"
---

# Java 规范：public type 必须与文件名匹配

## Convention

Java 语言规范（JLS §7.6）要求：**如果一个 .java 文件中有一个 public 类型（class / interface / enum / record / @interface），文件名必须与该 public 类型同名**。非 public 类型可以与文件名不同。

```
// 合法：文件名 = 类名
@interface AuditLog { ... }       // 文件: AuditLog.java ✅

// 非法：文件名 ≠ public 类型名
@interface AuditLog { ... }       // 文件: AuditLogAnnotation.java ❌
// javac 报: "接口 AuditLog 是公共的, 应在名为 AuditLog.java 的文件中声明"
```

**例外**：一个 .java 文件可以有多个非 public 类型，或 1 个 public + 多个 package-private 类型。

## Context

Sprint 1 落地时，`AuditLog` 同时存在两种含义：
- `AuditLogAnnotation.java`：`@interface AuditLog`（注解，被 `@Around("@annotation(... AuditLog)")` 引用）
- `AuditLog.java`（实体）：`class AuditLog`（@Data @Builder 实体，被 `AuditAspect` 用 `AuditLog.builder()` 构造）

两者同名 + 同包 → 编译报"类重复"。前者是注解必须 public → 改名实体为 `AuditLogEntity.java` + `class AuditLogEntity`，前者保留 `AuditLog.java`（含 `@interface AuditLog`）。

## Guidance

### 命名规则

1. **public 类型决定文件名**（不能反过来）
2. **同包不能有同名 public 类型**
3. **跨包同名 public 类型合法**（`com.x.AuditLog` vs `com.y.AuditLog`），但 import 时必须全限定名避免歧义

### Spring Boot 项目实践

- **注解**（`@interface Xxx`）：文件名 = `Xxx.java`（`@interface` 本身是 public 的）
- **实体**（`class XxxEntity` 或 `class Xxx`）：按 DDD 风格，业务实体直接用业务名（如 `User` / `Order`），DTO 加后缀（如 `UserDTO`）
- **注解和实体不要同名**：用 `AuditLog` 注解 + `AuditLogEntity` 实体；或 `Auditable` 注解 + `AuditLog` 实体

### Lombok 注解不豁免规则

`@Data` / `@Builder` / `@RequiredArgsConstructor` 等 Lombok 注解是 class-level 注解，**不影响 public type 与文件名匹配规则**。Lombok 生成的代码是 class 文件的一部分，文件名仍要跟 class 名一致。

## Why This Matters

**JLS 强制**：违反此规则编译失败，没有绕过方法。**跨包引用时 IDE 自动 import 依赖文件名匹配**。

**业务影响**：
- 编译失败 → 阻塞 commit / 部署
- 改不对 → 跨包 import 混乱（如 `import com.x.AuditLog` 不知道是注解还是实体）
- 重构波及面广（要改文件 + 改所有 import + 改所有引用点）

## When to Apply

- 新建任何 public Java 类型（class / interface / enum / record / @interface）
- 重构时改 public 类型名
- 跨包引用 public 类型时
- 用 Lombok `@Data` / `@Builder` / `@Service` / `@RestController` 等（这些都在 class / interface 上，遵循规则）

## Examples

### ✅ 正确

```java
// 文件: AuditLog.java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {  // public @interface，文件名 = AuditLog
    String action();
}

// 文件: AuditLogEntity.java
@Data @Builder
public class AuditLogEntity {  // public class，文件名 = AuditLogEntity
    private String id;
    private String action;
}
```

### ❌ 错误

```java
// 文件: AuditLogAnnotation.java  ← 文件名错
public @interface AuditLog {  // public @interface，但文件名不匹配
    String action();
}
// javac 报: "接口 AuditLog 是公共的, 应在名为 AuditLog.java 的文件中声明"
```

```java
// 文件: AuditLog.java  ← 类名 = 文件名 OK
public class AuditLog { }
// 文件: AuditLog.java  ← 同包另一个 public type 同名（不可能编译过，Java 不允许）
public class AuditLog { }
// javac 报: "类重复: com.example.AuditLog"
```

### 修复

```java
// 把实体改名为 AuditLogEntity
@Data @Builder
public class AuditLogEntity {  // 改名后文件名 = AuditLogEntity.java
    private String id;
}
```

## Related

- **JLS §7.6** Top Level Type Declarations：原文规定
- **`docs/solutions/build-errors/2026-07-15-001-sprint-1-compile-gate-missing.md`**：Sprint 1 总览
- **`docs/solutions/build-errors/lombok-required-args-constructor-conflict.md`**：Lombok 相关约定
