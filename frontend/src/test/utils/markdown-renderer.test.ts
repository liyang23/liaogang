import { describe, it, expect } from 'vitest'
import { render } from '@/utils/markdown-renderer'

describe('markdown 渲染器（T209 OQ-15 9 类元素）', () => {
  it('H2 标题（##）', () => {
    expect(render('## 标题')).toBe('<h2>标题</h2>')
  })

  it('H3 标题（###）', () => {
    expect(render('### 三级标题')).toBe('<h3>三级标题</h3>')
  })

  it('H4 标题（####）', () => {
    expect(render('#### 四级标题')).toBe('<h4>四级标题</h4>')
  })

  it('不支持 H1（# 开头按段落处理）', () => {
    expect(render('# H1')).toBe('<p># H1</p>')
  })

  it('**粗体**', () => {
    expect(render('**重要**')).toBe('<p><strong>重要</strong></p>')
  })

  it('*斜体*', () => {
    expect(render('*强调*')).toBe('<p><em>强调</em></p>')
  })

  it('`行内代码`', () => {
    expect(render('`code`')).toBe('<p><code>code</code></p>')
  })

  it('```代码块```', () => {
    expect(render('```\nSELECT *\nFROM ko\n```'))
      .toBe('<pre><code>SELECT *\nFROM ko</code></pre>')
  })

  it('```python 代码块带语言```', () => {
    expect(render('```python\nprint("hello")\n```'))
      .toBe('<pre><code class="language-python">print("hello")</code></pre>')
  })

  it('| 表格 |（3 列）', () => {
    const md = `| A | B | C |
|---|---|---|
| 1 | 2 | 3 |
| 4 | 5 | 6 |`
    const expected = '<table>' +
      '<thead><tr><th>A</th><th>B</th><th>C</th></tr></thead>' +
      '<tbody>' +
      '<tr><td>1</td><td>2</td><td>3</td></tr>' +
      '<tr><td>4</td><td>5</td><td>6</td></tr>' +
      '</tbody>' +
      '</table>'
    expect(render(md)).toBe(expected)
  })

  it('- 无序列表', () => {
    const md = `- 第一项
- 第二项
- 第三项`
    expect(render(md)).toBe('<ul><li>第一项</li><li>第二项</li><li>第三项</li></ul>')
  })

  it('1. 有序列表', () => {
    const md = `1. 第一步
2. 第二步
3. 第三步`
    expect(render(md)).toBe('<ol><li>第一步</li><li>第二步</li><li>第三步</li></ol>')
  })

  it('> 引用', () => {
    expect(render('> 这是一段引用'))
      .toBe('<blockquote>这是一段引用</blockquote>')
  })

  it('--- 分隔线', () => {
    expect(render('---')).toBe('<hr>')
  })

  it('[text](url) 链接', () => {
    expect(render('[百度](https://baidu.com)'))
      .toBe('<p><a href="https://baidu.com">百度</a></p>')
  })

  it('{{var}} 变量高亮（OQ-15 收缩：未替换 + 高亮）', () => {
    // render() 不处理 {{}}（由 handlebars.render 处理）；markdown 渲染时给变量 span 高亮
    expect(render('{{username}}')).toBe('<p><span class="var-highlight">{{username}}</span></p>')
  })

  it('HTML 字符转义（防 XSS）', () => {
    expect(render('<script>alert(1)</script>'))
      .toContain('&lt;script&gt;')
    expect(render('<script>alert(1)</script>'))
      .not.toContain('<script>')
  })

  it('多行段落累积', () => {
    expect(render('第一行\n第二行\n第三行'))
      .toBe('<p>第一行 第二行 第三行</p>')
  })

  it('不实现图片（OQ-15 决策）', () => {
    expect(render('![alt](url)')).toBe('<p>![alt](url)</p>')
  })
})
