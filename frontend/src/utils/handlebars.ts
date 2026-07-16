/**
 * 自研 Handlebars 子集渲染器（T209，OQ-15）
 *
 * <p>v0.32 §10.5.3 PRM 模板渲染支持的 Markdown 元素列表（OQ-15 决策）：
 * 收缩到产品需求范围——不实现 partials / helpers / 嵌套 sub-expressions。
 *
 * <p>支持的语法（3 类）：
 * <ul>
 *   <li>{{var}} - 变量替换</li>
 *   <li>{{#each items}}...{{/each}} - 数组循环（支持嵌套 var）</li>
 *   <li>{{#if var}}...{{/if}} - 条件分支（支持比较运算符 ==/!=/>/<）</li>
 * </ul>
 *
 * <p>不实现（OQ-15 决策）：
 * <ul>
 *   <li>partials（{{> partial}}）</li>
 *   <li>helpers（{{#if_eq x y}} 等自定义）</li>
 *   <li>子表达式（{{#if (eq x y)}}）</li>
 *   <li>块参数（as |var|）</li>
 * </ul>
 */

export interface RenderContext {
  [key: string]: unknown
}

/**
 * 渲染 Handlebars 模板
 *
 * @param template 模板字符串（含 {{var}} / {{#each}} / {{#if}} 等占位符）
 * @param context 数据上下文
 * @returns 渲染后的字符串
 */
export function render(template: string, context: RenderContext = {}): string {
  let result = template
  // 多轮处理（避免嵌套顺序问题：each > if > var 重复）
  // 每轮处理一种语法，最多 100 轮防无限循环
  for (let i = 0; i < 100; i++) {
    const prev = result

    // 1. 处理 {{#each items}}...{{/each}}（最优先，处理嵌套结构）
    result = processEach(result, context)

    // 2. 处理 {{#if cond}}...{{/if}}
    result = processIf(result, context)

    // 3. 处理 {{var}} 变量替换
    result = processVar(result, context)

    if (result === prev) break  // 无变化，停止
  }
  return result
}

/**
 * 处理 {{#each items}}...{{/each}} 循环
 */
function processEach(template: string, context: RenderContext): string {
  const eachRegex = /\{\{#each\s+(\w+)\}\}([\s\S]*?)\{\{\/each\}\}/g
  return template.replace(eachRegex, (_match, varName: string, body: string) => {
    const arr = context[varName]
    if (!Array.isArray(arr) || arr.length === 0) return ''
    return arr
      .map((item) => {
        // 每次迭代用 item 替换 body 中的 {{item.x}} 引用
        return render(body, { ...context, this: item, ...(item as object) })
      })
      .join('')
  })
}

/**
 * 处理 {{#if cond}}...{{/if}} 条件
 * 支持简单比较：{{#if var}} 或 {{#if var == value}}
 */
function processIf(template: string, context: RenderContext): string {
  // 匹配 {{#if X}} 或 {{#if X op Y}} 三种形式
  const ifRegex = /\{\{#if\s+([^}]+?)\}\}([\s\S]*?)\{\{\/if\}\}/g
  return template.replace(ifRegex, (_match, cond: string, body: string) => {
    const truthy = evaluateCondition(cond.trim(), context)
    return truthy ? body : ''
  })
}

/**
 * 评估条件：var / var == value / var != value / var > value / var < value
 * F-54 修复：支持有/无空格写法（`x==y` 与 `x == y` 等价）
 * 用 regex 一次性匹配最长优先运算符（<=, >=, ==, !=, <, >），前后允许 0+ 空格
 */
function evaluateCondition(cond: string, context: RenderContext): boolean {
  // 匹配最长优先运算符（每种允许 0+ 空格包围）
  const opRegex = /(<=|>=|==|!=|<|>)/g;
  const m = opRegex.exec(cond);
  if (m) {
    const op = m[1];
    const idx = m.index;
    const left = resolveValue(cond.substring(0, idx).trim(), context);
    const right = resolveValue(cond.substring(idx + op.length).trim(), context);
    switch (op) {
      case '==': return left === right;
      case '!=': return left !== right;
      case '>': return toDouble(left) > toDouble(right);
      case '<': return toDouble(left) < toDouble(right);
      case '>=': return toDouble(left) >= toDouble(right);
      case '<=': return toDouble(left) <= toDouble(right);
    }
  }
  // 无运算符：truthy 检查
  const val = resolveValue(cond, context);
  const valStr = val == null ? '' : String(val);
  return valStr !== '' && valStr !== 'false' && valStr !== '0';
}

/**
 * 数字转换（支持 number / string / null）
 */
function toDouble(o: unknown): number {
  if (typeof o === 'number') return o;
  if (typeof o === 'string') {
    const n = Number(o);
    return isNaN(n) ? 0 : n;
  }
  return 0;
}

/**
 * 解析变量值：支持 {{this.x}} / {{user.name}} 嵌套属性 + 字符串字面量
 */
function resolveValue(expr: string, context: RenderContext): unknown {
  // 字符串字面量（"..." 或 '...'）
  if ((expr.startsWith('"') && expr.endsWith('"')) ||
      (expr.startsWith("'") && expr.endsWith("'"))) {
    return expr.substring(1, expr.length - 1)
  }
  // 数字字面量
  if (/^-?\d+(\.\d+)?$/.test(expr)) {
    return Number(expr)
  }
  // this.x 引用
  if (expr.startsWith('this.')) {
    const key = expr.substring(5)
    return (context.this as Record<string, unknown> | undefined)?.[key]
  }
  // 普通变量（可能含 . 嵌套属性）
  const parts = expr.split('.')
  let val: unknown = context[parts[0]]
  for (let i = 1; i < parts.length && val != null && typeof val === 'object'; i++) {
    val = (val as Record<string, unknown>)[parts[i]]
  }
  return val
}

/**
 * 处理 {{var}} 变量替换
 * 不替换 {{#...}} / {{/...}} 块标记 / {{> partial}}
 */
function processVar(template: string, context: RenderContext): string {
  // 匹配 {{var}} 或 {{this.x}}，排除 {{#...}} / {{/...}} / {{>...}}
  const varRegex = /\{\{(?![#/])((?!>)[\s\S]+?)\}\}/g
  return template.replace(varRegex, (_match, expr: string) => {
    const val = resolveValue(expr.trim(), context)
    return val == null ? '' : String(val)
  })
}