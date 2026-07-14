# Product

## Register

product

## Users

**Primary (all roles work within a project-scoped view):**

- **Knowledge Base Administrator** (e.g., FAN): Owns the KO library, dashboard, governance workflows, system configuration
- **Business Expert** (e.g., dock planners): Creates/edits parameters and rules, submits for review
- **Algorithm Engineer** (e.g., 黄泽文): Authors prompt templates (PRM), triggers conflict detection, manages prompt assembly
- **Compliance Auditor**: Reviews KO changes, approves/rejects submissions
- **System Administrator**: Manages projects, roles, users, dictionary entries, audit logs

**Context**: Users work in 1440×900+ desktop environments, in a data-intensive professional setting. Sessions are long-running. Users are familiar with enterprise software (ERP, dock operations systems) but not necessarily AI tooling.

## Product Purpose

A knowledge management platform for port optimization — transforming scattered port运筹 documents into structured, versioned, conflict-checked knowledge objects (KO) that algorithm engineers assemble into prompts for the 伐谋 LLM.

Core workflows:
1. Browse/search KO by type (CON/RUL/PAR/ONT/PRM/DOC) in project-scoped view
2. Create/edit KO with lifecycle management (Draft → Review → Active/Deprecated)
3. Assemble prompts in a 3-column composer (KO selector → 9-paragraph editor → live preview)
4. Detect and resolve conflicts (C1-C6 object间, H1-H6 self-health) before rendering
5. Create versioned snapshots of rendered prompts
6. Govern: audit log, role permissions, dictionary management, project isolation

## Brand Personality

**3 words**: Industrial, precise, trustworthy

**Emotional tone**: Confident and rigorous — a tool for experts who move real ships and optimize real berths. Not flashy, not minimalist. Dense when density serves the workflow. Every pixel earns its place.

**Scene sentence**: A dock operations expert at a 1440p workstation in a well-lit control room, referencing the platform while coordinating a 72-hour vessel plan — data is alive, errors are costly, clarity is safety.

## Anti-references

- **Generic SaaS dashboard**: No pastel cards, no rounded-everything, no empty-state illustrations with friendly mascots
- **AI startup aesthetic**: No dark theme with neon accents, no "seamless" marketing copy, no floating glass panels
- **Consumer-grade simplicity**: This is not a consumer app. Complexity is acceptable when the domain demands it.
- **Cream/warm-neutral backgrounds**: Reject the AI-default cream/sand body bg. Use the specified canvas color (#F5F6F8) with grid texture.
- **Identical card grids**: Avoid uniform card rows for KO type entries — the types have genuinely different shapes and purposes.

## Design Principles

1. **Visibility first**: Dashboard gives immediate global KO health signal. Conflict counts are always visible in the nav rail. Data density is a feature, not a problem.
2. **Workflow over discovery**: The primary path is KO creation → assembly → render → snapshot, not browsing. Design supports the flow, not every possible entry point.
3. **No guesswork about state**: KO status, conflict level, version number are always legible. Never ambiguous.
4. **Project isolation by default**: Users only see what their project contains. Global admin view is explicit, not the default.
5. **Audit everything, excuse nothing**: All create/update/delete/publish operations log to audit trail. Traceability is structural, not aspirational.

## Accessibility & Inclusion

- **WCAG 2.1 AA** minimum for all text contrast
- **Reduced motion**: Respect `prefers-reduced-motion` — no entrance animations on dashboard cards or lists; instant state transitions only
- **Color blindness**: Conflict levels (high=red, medium=orange, low=yellow) are always paired with text labels and icon shapes, not color alone
- **SSO integration required**: User authentication via corporate SSO; no separate credential system for MVP