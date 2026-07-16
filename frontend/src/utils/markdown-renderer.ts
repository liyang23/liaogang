/**
 * 自研 Markdown 渲染器（T209，OQ-15 收缩到 PRD §10.5.3 列表）
 *
 * <p>v0.32 §10.5.3 PRM 模板渲染支持的 Markdown 元素列表（9 类 + 变量高亮）：
 * <ol>
 *   <li>H2 / H3 / H4 标题（# / ## / ###）</li>
 *   <li>**粗体** / *斜体* / `行内代码`</li>
 *   <li>```代码块```</li>
 *   <li>表格（| col | col | + |---|---|）</li>
 *   <li>列表（- item / 1. item）</li>
 *   <li>> 引用</li>
 *   <li>[text](url) 链接</li>
 *   <li>--- 分隔线</li>
 *   <li>{{var}} 变量高亮（OQ-15）</li>
 * </ol>
 *
 * <p>不实现：图片、代码块语言标记、删除线、任务列表、嵌套引用
 */

import { render as renderHandlebars } from './handlebars'  // 当前未使用（markdown 独立渲染）

/**
 * 渲染 Markdown 文本（含 {{var}} 行内高亮）
 *
 * 注意：本函数**不调用 handlebars**（那是独立工具）。
 * Handlebars 替换 var 后值变为普通文本，{{var}} 字面量丢失 → 高亮不生效。
 * 因此本函数保留 {{var}} 字面量，inline 高亮为 span（用于编辑器预览）。
 *
 * @param source 原始 Markdown（含 {{var}} 等占位符，{{#each}}/{{#if}} 由 handlebars 处理）
 * @returns 渲染后的 HTML 字符串
 */
export function render(source: string): string {
  return renderMarkdown(source)
}

/**
 * 渲染 Markdown 为 HTML（不处理 {{}} 占位符，由 render() 统一处理）
 */
function renderMarkdown(text: string): string {
  const lines = text.split('\n')
  const out: string[] = []
  let i = 0

  while (i < lines.length) {
    const line = lines[i]

    // 代码块（```lang...```）
    if (line.trim().startsWith('```')) {
      const lang = line.trim().substring(3).trim()
      const codeLines: string[] = []
      i++
      while (i < lines.length && !lines[i].trim().startsWith('```')) {
        codeLines.push(escapeHtml(lines[i]))
        i++
      }
      i++  // 跳过结束的 ```
      const langClass = lang ? ` class="language-${lang}"` : ''
      out.push(`<pre><code${langClass}>${codeLines.join('\n')}</code></pre>`)
      continue
    }

    // 标题（H2-H4，# H1 不支持，OQ-15 收缩）
    const hMatch = /^(#{2,4})\s+(.+)$/.exec(line)
    if (hMatch) {
      const level = hMatch[1].length  // 2/3/4
      const content = inlineMd(hMatch[2])
      out.push(`<h${level}>${content}</h${level}>`)
      i++
      continue
    }

    // 分隔线（--- 独占一行）
    if (/^-{3,}\s*$/.test(line)) {
      out.push('<hr>')
      i++
      continue
    }

    // 表格（| col | col | + 下一行 |---|---|）
    if (/^\|.+\|/.test(line) && i + 1 < lines.length && /^\|[\s\-:|]+\|/.test(lines[i + 1])) {
      const headers = parseRow(line)
      i += 2  // 跳过分隔行
      const rows: string[][] = []
      while (i < lines.length && /^\|.+\|/.test(lines[i])) {
        rows.push(parseRow(lines[i]))
        i++
      }
      out.push(renderTable(headers, rows))
      continue
    }

    // 无序列表
    if (/^[-*]\s+/.test(line)) {
      const items: string[] = []
      while (i < lines.length && /^[-*]\s+/.test(lines[i])) {
        items.push(inlineMd(lines[i].replace(/^[-*]\s+/, '')))
        i++
      }
      out.push('<ul>' + items.map(it => `<li>${it}</li>`).join('') + '</ul>')
      continue
    }

    // 有序列表
    if (/^\d+\.\s+/.test(line)) {
      const items: string[] = []
      while (i < lines.length && /^\d+\.\s+/.test(lines[i])) {
        items.push(inlineMd(lines[i].replace(/^\d+\.\s+/, '')))
        i++
      }
      out.push('<ol>' + items.map(it => `<li>${it}</li>`).join('') + '</ol>')
      continue
    }

    // 引用（> ...）
    if (/^>\s+/.test(line)) {
      const quoteLines: string[] = []
      while (i < lines.length && /^>\s+/.test(lines[i])) {
        quoteLines.push(lines[i].replace(/^>\s+/, ''))
        i++
      }
      // 简化：单行纯文本引用不包 <p>，多行或含 markdown 标记时递归渲染
      const joined = quoteLines.join(' ')
      if (quoteLines.length === 1 && !joined.match(/[*_`#-]/)) {
        out.push(`<blockquote>${escapeHtml(joined)}</blockquote>`)
      } else {
        out.push(`<blockquote>${renderMarkdown(quoteLines.join('\n'))}</blockquote>`)
      }
      continue
    }

    // 空行
    if (line.trim() === '') {
      i++
      continue
    }

    // 图片（![alt](url)）—— OQ-15 决策：不实现图片，按原样作为段落处理
    if (/^!\[[^\]]*\]\([^)]+\)\s*$/.test(line)) {
      out.push(`<p>${escapeHtml(line.trim())}</p>`)
      i++
      continue
    }

    // 段落（累积连续非空行）
    const paraLines: string[] = [line]
    i++
    while (i < lines.length && lines[i].trim() !== '' && !isBlockStart(lines[i])) {
      paraLines.push(lines[i])
      i++
    }
    out.push(`<p>${inlineMd(paraLines.join(' '))}</p>`)
  }

  return out.join('\n')
}

function isBlockStart(line: string): boolean {
  return /^(#{2,4}\s|```|`{3}|---|\|[\s\-:|]+\||^[-*]\s|^\d+\.\s|^>\s)/.test(line)
}

/**
 * 行内 Markdown 解析
 * 顺序：行内代码 → 粗体 → 斜体 → 链接 → {{var}} 高亮
 */
function inlineMd(text: string): string {
  let result = escapeHtml(text)
  // 行内代码 `code`
  result = result.replace(/`([^`]+)`/g, '<code>$1</code>')
  // 粗体 **text**
  result = result.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  // 斜体 *text*
  result = result.replace(/(?<!\*)\*([^*]+)\*(?!\*)/g, '<em>$1</em>')
  // 链接 [text](url)
  result = result.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2">$1</a>')
  // {{var}} 变量高亮（OQ-15）
  result = result.replace(/\{\{([^{}]+?)\}\}/g, '<span class="var-highlight">{{$1}}</span>')
  return result
}

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
}

function parseRow(line: string): string[] {
  // | col | col | → [col, col]
  const trimmed = line.trim()
  const inner = trimmed.startsWith('|') ? trimmed.substring(1) : trimmed
  const finalInner = inner.endsWith('|') ? inner.substring(0, inner.length - 1) : inner
  return finalInner.split('|').map(c => c.trim())
}

function renderTable(headers: string[], rows: string[][]): string {
  const thead = '<thead><tr>' + headers.map(h => `<th>${inlineMd(h)}</th>`).join('') + '</tr></thead>'
  const tbody = '<tbody>' + rows.map(row =>
    '<tr>' + headers.map((_, i) => `<td>${inlineMd(row[i] || '')}</td>`).join('') + '</tr>'
  ).join('') + '</tbody>'
  return `<table>${thead}${tbody}</table>`
}
