import { describe, it, expect } from 'vitest'
import { render, type RenderContext } from '@/utils/handlebars'

describe('handlebars 渲染器（T209 OQ-15 自研子集）', () => {
  it('{{var}} 变量替换', () => {
    expect(render('Hello {{name}}!', { name: 'World' })).toBe('Hello World!')
    expect(render('{{a}} + {{b}} = {{c}}', { a: 1, b: 2, c: 3 })).toBe('1 + 2 = 3')
  })

  it('{{var}} 嵌套对象访问', () => {
    const ctx: RenderContext = { user: { name: '李雷', age: 30 } }
    // 简化为 . 访问：{{user.name}}
    expect(render('姓名: {{user.name}}, 年龄: {{user.age}}', ctx))
      .toBe('姓名: 李雷, 年龄: 30')
  })

  it('{{#each items}} 数组循环', () => {
    const items = [{ name: 'A' }, { name: 'B' }, { name: 'C' }]
    expect(render('{{#each items}}[{{this.name}}]{{/each}}', { items }))
      .toBe('[A][B][C]')
  })

  it('{{#each items}} 空数组返回空字符串', () => {
    expect(render('Before{{#each items}}X{{/each}}After', { items: [] }))
      .toBe('BeforeAfter')
  })

  it('{{#if}} 真值条件', () => {
    expect(render('{{#if show}}YES{{/if}}', { show: true })).toBe('YES')
    expect(render('{{#if show}}YES{{/if}}', { show: false })).toBe('')
    expect(render('{{#if show}}YES{{/if}}', { show: 'truthy string' })).toBe('YES')
  })

  it('{{#if}} 假值条件（empty / 0 / false）', () => {
    expect(render('{{#if x}}YES{{/if}}', { x: '' })).toBe('')
    expect(render('{{#if x}}YES{{/if}}', { x: 0 })).toBe('')
    expect(render('{{#if x}}YES{{/if}}', { x: 'false' })).toBe('')
  })

  it('{{#if}} == 比较', () => {
    expect(render('{{#if x == "yes"}}MATCH{{/if}}', { x: 'yes' })).toBe('MATCH')
    expect(render('{{#if x == "no"}}MATCH{{/if}}', { x: 'yes' })).toBe('')
  })

  it('{{#if}} != 比较', () => {
    expect(render('{{#if x != "yes"}}DIFF{{/if}}', { x: 'no' })).toBe('DIFF')
    expect(render('{{#if x != "yes"}}DIFF{{/if}}', { x: 'yes' })).toBe('')
  })

  it('{{#if}} 数值比较 > <', () => {
    expect(render('{{#if x > 5}}BIG{{/if}}', { x: 10 })).toBe('BIG')
    expect(render('{{#if x > 5}}BIG{{/if}}', { x: 3 })).toBe('')
    expect(render('{{#if x < 5}}SMALL{{/if}}', { x: 3 })).toBe('SMALL')
  })

  it('不实现 partials（{{> partial}}）保持原文', () => {
    // partials 不实现，按 OQ-15 决策
    expect(render('{{> header}}', {})).toBe('{{> header}}')
  })

  it('不存在的变量返回空字符串', () => {
    expect(render('A {{missing}} B', {})).toBe('A  B')
  })

  it('混合语法：each + 内部 var', () => {
    const items = [{ x: 1 }, { x: 2 }, { x: 3 }]
    expect(render('{{#each items}}[{{this.x}}]{{/each}}', { items }))
      .toBe('[1][2][3]')
  })

  it('混合语法：if 包裹 each', () => {
    const items = [{ name: 'A' }, { name: 'B' }]
    // 模板：{{#if show}}{{#each items}}{{this.name}} {{/each}}{{/if}}
    expect(render('{{#if show}}{{#each items}}{{this.name}} {{/each}}{{/if}}', { show: true, items }))
      .toBe('A B ')
    expect(render('{{#if show}}{{#each items}}{{this.name}} {{/each}}{{/if}}', { show: false, items }))
      .toBe('')
  })
})
