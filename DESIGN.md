---
name: 辽港伐谋知识管理平台
description: 港口调度知识管理平台 — 结构化知识对象装配式提示词管理，适配专业调度场景
colors:
  canvas-bg: "#F5F6F8"
  paper-bg: "#FFFFFF"
  rail-bg: "#0F1E2E"
  rail-active: "#173552"
  grid-line: "#ECEEF1"
  border: "#DCE0E6"
  border-strong: "#B0B7C0"
  text-primary: "#1A2332"
  text-secondary: "#5A6373"
  text-tertiary: "#8A92A0"
  text-on-dark: "#E5EAF0"
  text-on-dark-dim: "#8FA0B5"
  port-blue: "#0F4C75"
  port-blue-light: "#1E6A9D"
  steel: "#2D3748"
  signal-orange: "#ED8936"
  signal-orange-deep: "#C26418"
  signal-red: "#C53030"
  signal-green: "#2F855A"
  signal-yellow: "#D69E2E"
  tag-bg: "#EEF2F7"
typography:
  display:
    fontFamily: "Noto Serif SC, Georgia, serif"
    fontSize: "16px"
    fontWeight: 700
    letterSpacing: "0.06em"
  body:
    fontFamily: "Noto Sans SC, -apple-system, BlinkMacSystemFont, sans-serif"
    fontSize: "13px"
    lineHeight: 1.5
  label:
    fontFamily: "JetBrains Mono, monospace"
    fontSize: "10px"
    letterSpacing: "0.04em"
  mono:
    fontFamily: "JetBrains Mono, monospace"
rounded:
  sm: "2px"
  md: "4px"
  lg: "6px"
spacing:
  xs: "4px"
  sm: "8px"
  md: "16px"
  lg: "24px"
  xl: "32px"
components:
  button-primary:
    backgroundColor: "{colors.port-blue}"
    textColor: "#FFFFFF"
    rounded: "{rounded.sm}"
    padding: "6px 12px"
  button-primary-hover:
    backgroundColor: "{colors.port-blue-light}"
  button-secondary:
    backgroundColor: "#FFFFFF"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.sm}"
    padding: "6px 12px"
    border: "1px solid {colors.border}"
  button-ghost:
    backgroundColor: "transparent"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.sm}"
    padding: "6px 12px"
  button-warn:
    backgroundColor: "{colors.signal-orange}"
    textColor: "#FFFFFF"
    rounded: "{rounded.sm}"
    padding: "6px 12px"
  tag:
    backgroundColor: "{colors.tag-bg}"
    textColor: "{colors.text-secondary}"
    rounded: "{rounded.sm}"
    padding: "1px 6px"
  chip-hard:
    backgroundColor: "#FED7D7"
    textColor: "{colors.signal-red}"
  chip-soft:
    backgroundColor: "#FEEBC8"
    textColor: "{colors.signal-orange-deep}"
  chip-rec:
    backgroundColor: "#C6F6D5"
    textColor: "{colors.signal-green}"
  chip-emp:
    backgroundColor: "#E2E8F0"
    textColor: "{colors.steel}"
  card:
    backgroundColor: "{colors.paper-bg}"
    border: "1px solid {colors.border}"
    rounded: "{rounded.sm}"
    padding: "16px"
  modal:
    backgroundColor: "{colors.paper-bg}"
    border: "1px solid {colors.border}"
    rounded: "{rounded.sm}"
    shadow: "0 8px 32px rgba(0,0,0,0.2)"
    width: "min(480px, 92vw)"
  table:
    backgroundColor: "{colors.paper-bg}"
    borderCollapse: "collapse"
    fontSize: "12px"
  table-header:
    backgroundColor: "#FAFBFC"
    textColor: "{colors.text-secondary}"
    fontSize: "11px"
    fontWeight: 500
    textTransform: "uppercase"
    letterSpacing: "0.04em"
    padding: "8px 10px"
  nav-rail:
    backgroundColor: "{colors.rail-bg}"
    width: "196px"
  nav-item:
    padding: "9px 16px"
    fontSize: "13px"
    borderLeft: "2px solid transparent"
  nav-item-active:
    backgroundColor: "{colors.rail-active}"
    borderLeftColor: "{colors.signal-orange}"
---

# Design System: 辽港伐谋知识管理平台

## 1. Overview

**Creative North Star: "The Master Scheduler's Log"**
A living archive of operational wisdom. Structured knowledge meets adaptive intelligence. Not just a dashboard for monitoring, but an active partner that translates veteran experience into algorithmic precision through natural language.

This is a professional tool for dock operations experts at 1440p workstations in well-lit control rooms. The interface serves long-running, data-intensive sessions where errors are costly and clarity is safety. Every pixel earns its place. Dense when density serves the workflow. The expert trusts the instrument panel, not decoration.

**Key Characteristics:**
- **Visibility first**: Global KO health signal on dashboard. Conflict counts always visible in nav rail.
- **Workflow over discovery**: Primary path is KO creation to assembly to render to snapshot, not browsing.
- **No guesswork about state**: KO status, conflict level, version number always legible. Never ambiguous.
- **Industrial aesthetic**: Dark rail navigation, grid-textured canvas, port blue and signal orange palette. Not a SaaS landing page.
- **1440p optimized**: 11-13px JetBrains Mono in table cells for dense data display. No cramped mobile-first design.

**The Flat-By-Default Rule.** Surfaces are flat at rest. Shadows appear only on elevated elements (modals, dropdowns, overlays). Cards and stat panels use borders and tonal backgrounds, not drop shadows. This is a control room instrument, not a consumer app.

**The Density-is-a-Feature Rule.** This is not a consumer-grade simplified UI. Tables show 9 columns. Navigation shows KO type counts. The information density is intentional. Do not strip data from tables to make it "cleaner."

## 2. Colors

The palette is anchored in maritime-industrial convention: dark navigation rail for instrument-panel authority, cool off-white canvas for operational clarity, signal orange for warnings and actions.

### Primary
- **Port Blue** (`#0F4C75`): Primary actions, links, active states, KO IDs in tables. The platform's anchor color. Used on buttons, nav rail active state, breadcrumb highlights, modal borders on focus.
- **Signal Orange** (`#ED8936`): Warnings, precheck panel accent bar, conflict type badges, live indicator. The action-requiring signal. Paired with `--signal-orange-deep` for hover states.

### Secondary
- **Signal Orange Deep** (`#C26418`): Hover state for signal orange elements. Active state for orange buttons.
- **Port Blue Light** (`#1E6A9D`): Hover state for port blue primary buttons. Secondary blue for authority tier badges (L3).

### Signal States
- **Signal Red** (`#C53030`): Errors, destructive actions, C1 value conflicts, tag-hard chips, KO value in conflict rows.
- **Signal Green** (`#2F855A`): Success states, Active KO status, Rec authority tag, approval confirmations.
- **Signal Yellow** (`#D69E2E`): Low-priority badges, warning states.

### Neutral
- **Rail Dark** (`#0F1E2E`): Side navigation background. The instrument panel.
- **Rail Active** (`#173552`): Active nav item background.
- **Canvas** (`#F5F6F8`): Page background. Grid-textured with subtle 24px grid lines.
- **Paper** (`#FFFFFF`): Card surfaces, table backgrounds, modal backgrounds.
- **Grid Line** (`#ECEEF1`): Internal dividers, table row separators, subtle structural lines.
- **Border** (`#DCE0E6`): Card borders, input borders, structural outlines.
- **Border Strong** (`#B0B7C0`): Secondary borders, hover states on inputs.
- **Text Primary** (`#1A2332`): Body text, table data, headings.
- **Text Secondary** (`#5A6373`): Subtitles, table headers, metadata.
- **Text Tertiary** (`#8A92A0`): Timestamps, placeholders, disabled text.
- **Text On Dark** (`#E5EAF0`): Text on dark backgrounds (nav rail).
- **Text On Dark Dim** (`#8FA0B5`): Secondary text on dark backgrounds.

### KO Type Color System (Signature Pattern)
- **CON** (约束): Red family (`#FED7D7` / `signal-red`)
- **RUL** (规则): Orange family
- **PAR** (参数): Green family (`#C6F6D5` / `signal-green`)
- **ONT** (本体): Purple family (`#E9D8FD` / `#6B46C1`)
- **PRM** (提示词): Pink family (`#FBB6CE`)
- **DOC** (文档): Gray family

**The KO Color Rule.** All six KO types have a consistent color assignment across dots, badges, and type tags. Color is always paired with text label and icon shape, never color alone (colorblind accessibility).

## 3. Typography

**Display Font:** Noto Serif SC (with Georgia fallback)
**Body Font:** Noto Sans SC (with -apple-system, BlinkMacSystemFont, sans-serif fallback)
**Label/Mono Font:** JetBrains Mono (for IDs, versions, codes, technical values)

**Character:** A professional Chinese-language interface with a serif display for the logo and page titles, a clean sans for body text, and a monospace for all technical identifiers. The pairing is functional rather than decorative: serif establishes authority, sans is legible at small sizes in dense tables, mono makes IDs and values scannable.

### Hierarchy
- **Display** (Noto Serif SC, 700, 16px, 0.06em letter-spacing): Page titles, logo. Authoritative serif voice.
- **Headline** (Noto Sans SC, 700, 18px): Page header h1 only.
- **Title** (Noto Sans SC, 500, 13px): Table row titles, card headings.
- **Body** (Noto Sans SC, 400, 13px, line-height 1.5): Standard body text. Max line length 75ch on prose content.
- **Label** (JetBrains Mono, 500, 10-11px, uppercase with 0.04em letter-spacing): Table headers, metadata labels. All-caps by convention for column headers.
- **Mono** (JetBrains Mono, 400-500, 10-11px): KO IDs, version numbers, form field values, conflict values.

### Named Rules
**The Mono-Over-Mono Rule.** Technical identifiers (KO IDs, version strings, parameter values) are always JetBrains Mono. Body text, even when technical, uses Noto Sans SC unless it is an identifier.

## 4. Elevation

**The Flat-By-Default Rule.** Surfaces are flat at rest. Shadows appear only as a response to elevation need (modals, toasts, overlays). No ambient shadows on cards, stat panels, or navigation. Depth is conveyed through tonal contrast: dark rail vs. light content area, paper white vs. canvas gray.

**The Hierarchy-Through-Background Rule.** Background color alone creates depth hierarchy: rail (dark) → canvas (light gray) → paper (white). No shadow needed between a card and the page background.

### Shadow Vocabulary
- **Modal shadow** (`box-shadow: 0 8px 32px rgba(0,0,0,0.2)`): Only applied to `.modal` (the dialog box itself), not to overlays or cards.
- **Timeline node** (`box-shadow: 0 0 0 1px var(--port-blue)`): Timeline items use a ring shadow to separate the node from the connecting line.
- **No card shadows**: Cards use borders (`border: 1px solid var(--line)`) not shadows.

### Focus Rings
- **Standard** (`outline: 2px solid var(--signal-orange); outline-offset: 2px`): Applied to buttons, inputs, nav items on focus-visible.
- **Input focus** (`border-color` shift + `box-shadow`): Input fields shift border color to port-blue on focus with a subtle box-shadow.

## 5. Components

### Buttons
- **Shape:** 2px border-radius. No rounded-everything. Confident, sharp corners.
- **Primary:** Background `port-blue`, white text. Padding 6px 12px. Hover: `port-blue-light`.
- **Secondary/Ghost:** White background, border `line`, dark text. Hover: `tag-bg` background.
- **Warn:** `signal-orange` background, white text. For destructive or high-attention actions.
- **Focus:** Orange focus ring visible on keyboard navigation.
- **Disabled:** Opacity 0.5, cursor not-allowed.

### Chips / Tags
- **KO Status chips:** Soft background tint + dark text. Hard=red, Soft=orange, Rec=green, Emp=gray, Draft=gray, Review=orange, Deprecated=red.
- **Authority tier chips:** L1=deep navy, L2=mid blue, L3=port blue, L4=gray, L5=light gray. Solid fills, white text.
- **KO Type dots:** 8x8px colored squares, color-coded per type. Always accompanied by text label.
- **Shape:** 2px border-radius throughout. No pill shapes.

### Cards / Containers
- **Corner Style:** 2px border-radius. No rounded-everything.
- **Background:** Paper white for content cards, canvas gray for the page.
- **Border:** `1px solid var(--line)` throughout. No colored borders unless intentional (e.g., conflict type cards).
- **Shadow Strategy:** None. Flat by default.
- **Internal Padding:** 14-16px standard padding for cards.

### Tables
- **Style:** `border-collapse: collapse`, `table-layout: auto`. Columns sized to content.
- **Header:** FAFBFC background, uppercase 11px JetBrains Mono, letter-spacing 0.04em.
- **Row hover:** FAFBFC background on hover.
- **Cell padding:** 9px 10px vertical/horizontal.
- **Max width behavior:** Text truncation with ellipsis on `.title-cell` (max-width: 200px) and `.scope-mini` (max-width: 240px). Long text does not break layout.

### Navigation
- **Style:** Dark rail (196px wide) on left. Logo at top, nav items grouped by category.
- **Active state:** Left 2px orange border + rail-active background.
- **Item structure:** Icon + text label + count badge (JetBrains Mono). Text labels always visible in full mode.
- **Collapsed mode:** 52px wide, icons only, at ≤1024px viewport.
- **Group labels:** Uppercase 10px JetBrains Mono, wide letter-spacing, tertiary text color.

### Modals
- **Backdrop:** `rgba(0,0,0,0.5)` overlay.
- **Dialog:** Paper white, 2px border-radius, 1px border, `box-shadow: 0 8px 32px rgba(0,0,0,0.2)`.
- **Header:** FAFBFC background, 14px bold title, close button right-aligned.
- **Footer:** FAFBFC background, right-aligned button group.
- **Width:** `min(480px, 92vw)` default. Larger modals (import, preview) up to 900px.

### Toasts
- **Position:** Fixed bottom-right, stacked.
- **Structure:** Left accent bar (color by type) + icon + title + message + close button.
- **Types:** Success (green), Error (red), Warning (orange), Info (port-blue).
- **Behavior:** Auto-dismiss after 5s. Hover pauses timer. Escape key dismisses.

### Form Inputs
- **Style:** White background, 1px `line` border, 2px border-radius.
- **Focus:** Border shifts to `port-blue`, subtle box-shadow.
- **Placeholder:** Tertiary text color, 4.5:1 contrast verified.
- **Error:** Red border, red error text below.

### Pagination
- **Structure:** Page info (JetBrains Mono) + page-size select + page-jump input + GO button.
- **GO button:** Secondary button style. Not just text.

## 6. Do's and Don'ts

### Do:
- **Do** use dark rail navigation with orange active indicator — it establishes the industrial control-panel authority.
- **Do** show KO type counts in the nav sub-menu — the expert needs to know the state of the knowledge base at a glance.
- **Do** use 2px border-radius throughout — sharp, professional, not consumer-app soft.
- **Do** use JetBrains Mono for all technical identifiers: KO IDs, version strings, parameter values, conflict codes.
- **Do** use the KO type color system consistently — dots, badges, and tags must use the same color per type across all pages.
- **Do** pair color with text label and icon shape on all conflict level indicators — colorblind accessibility is structural.
- **Do** use `border-collapse: collapse` with `table-layout: auto` on all tables — columns must size to content.
- **Do** show the precheck panel before rendering — surfacing C1/C2/C6 conflicts before rendering is the error-prevention mechanism.
- **Do** use the triage panel layout for knowledge governance — severity summary at top, filter pills, batch actions before the detail list.

### Don't:
- **Don't** use pastel cards with rounded-everything — this is explicitly not a SaaS dashboard.
- **Don't** use dark theme with neon accents — reject the AI-startup aesthetic. The rail is dark but the content area is light, and there are no neon glows.
- **Don't** use cream/warm-neutral backgrounds — the canvas is `#F5F6F8` (cool off-white with grid texture). Not cream, not sand.
- **Don't** use `border-left` greater than 1px as a colored accent on cards — the side-stripe is the AI-generated UI tell. Use `::before` pseudo-elements for accent bars or use full borders.
- **Don't** use `gradient text` — decorative gradient text is prohibited. Use solid color with weight or size for emphasis.
- **Don't** use `glassmorphism` — frosted glass panels are prohibited. Surfaces are solid.
- **Don't** use identical card grids for KO type entries — the 6 KO types have different purposes. The type intro cards are intentionally varied in layout, not uniform cards.
- **Don't** use tiny uppercase tracked eyebrows above every section — "ABOUT" / "PROCESS" / "PRICING" style labels are AI grammar. Use purposeful section structure.
- **Don't** use numbered section markers (01 / 02 / 03) as default scaffolding — if the section is not a numbered sequence, don't number it.
- **Don't** use `display: block` on table cell elements (`.title-cell`, `.scope-mini`, `.mono-cell`) — this breaks table column alignment. Use inline behavior only.
- **Don't** use `overflow: hidden` on `.content` — this truncates table columns. Use `overflow-x: auto` to allow horizontal scroll.
- **Don't** use `table-layout: fixed` — this forces all columns to equal width regardless of content. Use `auto` so columns size to their content.
- **Don't** use `max-width: 0` on table cells — this collapses the cell to invisible width.
- **Don't** use bounce/elastic easing (`cubic-bezier(.34, 1.56, .64, 1)`) — this is an AI-generated UI tell. Use `ease-out` or exponential curves for all transitions.
- **Don't** show the tweaks panel in production — it is prototype scaffolding and must be stripped before handoff.