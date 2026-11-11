# Design brief: Cinemerick · Riepilogo filtri (filter summary bar)

Status: iterating (full design published 2026-11-11)
Artifacts: options https://claude.ai/artifact/Jo9LcbF9Hxv1wmKfF2zdmj · design https://claude.ai/artifact/MJpBQbEKFakxuXzxxPXj3h

Tokens: docs/design/tokens.json mirrors the app (dark `error` lifted to #F2B8B5 and `outline` adjusted so text pairs pass AA; the app's dark error #B3261E fails 4.5:1 as text on dark surfaces).

Scope: one new component added to the existing redesign (docs/design/*.dc.html, core/designsystem). The visual
direction is fixed: tokens come from `Color.kt`, `Type.kt`, `Shape.kt`, `Spacing.kt`. Phase 2 explores the
component's form and placement, not palettes.

## Product
- What it is: KMP cinema showtimes aggregator. The user picks venues across 8 chains, days, a time window per day, formats and an optional film filter, searches, then copies the poll text for a group chat.
- Users and context of use: one-handed on the phone, planning a cinema night with friends; scrolls long result lists.
- Problem: on phone the filters scroll away with the results, so the user forgets what is selected (e.g. Notorious Fiume Veneto + The Space Silea).
- Platforms / viewports: phone portrait and phone landscape (Android + iOS). Not tablet/desktop (filter pane always visible).
- Themes: light + dark, existing tokens, follows the app setting (Auto/Light/Dark)
- Accessibility target: WCAG 2.2 AA
- Implementation target: Compose Multiplatform, Material 3 base

## Mood and brand
- Fixed brand assets: all existing tokens, Plus Jakarta Sans, chain badge colors (`CinemaBrandColors`), icon set in `CinemerickIcons`.

## Content
- Language / locale: English (default) + Italiano. Designs use Italian copy.
- Sample data: Notorious · Fiume Veneto, The Space · Silea, UCI Luxe Marcon; Gio 13, Ven 14; 20:00–24:00; 3D hidden; "Dune, Wicked".

## Component requirements (interview, 2026-11-11)
- Summarizes: cinemas/venues, days, time windows, formats, film text filter (all filters).
- Appears exactly when the compact top bar appears today (large header collapsed after 72 dp of scroll), even while Cinemas is still on screen; disappears only when the large header is back.
- Tap on a summary item = navigation only: scroll to the exact component and highlight it (no remove ×).
- Phone portrait (single scrolling column) and phone landscape (inside the scrolling filter pane).
- Replaces the pinned compact top bar (title + settings) on phone: it takes its place at the top once Cinemas scrolls away. Settings button stays at the trailing end of the bar.
- Coexists with the "Copia sondaggio" extended FAB (bottom end) and snackbars.
- Stale state: when filters change after a search, the bar says "Filtri modificati" with an "Aggiorna" action that re-runs the search.

## Screen inventory
| Feature | Screen | Primary action / question | Notes |
|---|---|---|---|
| Showtimes | Home, phone portrait, scrolled | "What did I select?" → tap to jump back | bar over results |
| Showtimes | Home, phone landscape | same, inside filter pane | |

### States: filter summary bar (B · Riepilogo espandibile)
Phone portrait, Home (single scrolling column):
1. Hidden: top of the page, large header visible. Trigger: scroll < 72 dp.
2. Collapsed · results: results under the bar. Default state.
3. Collapsed · over filters: header collapsed, Cinemas/Days still on screen, no search yet.
4. Expanded panel: rows per filter group, each row jumps. Scrim over content; tap scrim/back/chevron closes.
5. Jumped: panel closed, list scrolled so the target sits just under the bar, target card opened + primary ring ~1.5 s; bar stays. For days/time/format/film targets the ring goes on that section.
6. Stale · collapsed: line 2 = "Filtri modificati", Aggiorna replaces Settings. Trigger: any filter differs from the last search.
7. Stale · expanded: changed rows marked "Nuovo"/"Modificato", Aggiorna button at the bottom of the panel.
8. Searching after Aggiorna: Aggiorna shows a spinner, line 2 "Ricerca in corso…", results keep showing until replaced.
9. Missing filter: no venues / no days. Line 1 "Nessuna sede" / "Nessun giorno" in the primary color; row jumps to the section. Aggiorna pressed without days → T3 snackbar "Seleziona almeno un giorno" (existing copy), bar stays stale.
10. Search failed after Aggiorna → T3 snackbar "Ricerca non riuscita · Riprova", bar stays stale; partial venue failures keep the existing inline error in results.
Stress: 8 venues in 5 chains + 5 days with different windows + long film filter (panel scrolls inside, max ~70% of the screen; line 1 shows 4 dots + "+4"; line 2 truncates), 1 venue/1 day (singular "1 sala · Gio 13"), 200% font scale (line 1 and 2 each stay on one line, truncated; panel rows wrap).
Phone landscape: bar at the top of the filter pane, panel drops inside that pane only.
Motion: bar slides in from the top (200 ms), panel expands (250 ms), highlight fades; with Animations off all are instant and the ring stays 1.5 s without fading.
Accessibility: the summary is one button with a full spoken summary and aria-expanded; after a jump, accessibility focus moves to the target; touch targets ≥ 48 dp.

## Key flows to make clickable
- Hidden → Collapsed → open panel → tap "Notorious · Fiume Veneto" → Jumped (Notorious card open and highlighted).
- Stale collapsed → Aggiorna → Searching → Collapsed with new results.
- Stale expanded → Aggiorna in panel → Searching.

## Decisions log
- 2026-11-11 · All filters in the summary · user choice
- 2026-11-11 · Trigger: same as today's compact bar (header collapse), not Cinemas leaving · user revision during state plan
- 2026-11-11 · Navigation only, no removal · avoids results going out of sync with the filters
- 2026-11-11 · Replaces the compact top bar, settings moves into it · user choice
- 2026-11-11 · Stale indicator with "Aggiorna" action · user choice
- 2026-11-11 · Phone portrait + landscape · user choice (pushback given: landscape pane is always visible; kept because the pane scrolls on its own)

## Options history
- Round 1 (form, same tokens): A Striscia (one chip per value, horizontal strip), B Riepilogo espandibile (two-line summary + drop-down panel), C Gruppi (one chip per filter type + menu) → **B chosen** (2026-11-11): full overview that stays readable with many venues; two taps to jump accepted.

## Judgment calls
- Cinema rows in the panel are always two lines: chain name, then its venues below (user request, 2026-11-11).
- Days and time windows: one row per day "Gio 13 · 20:00–24:00" under "Giorni e orari", jumping to that day's slider (split tried and reverted by the user, 2026-11-11).
- Formato row jumps to the format chips block, Filtro film row to the text field without opening the keyboard (2026-11-11).
- One panel row per chain (not per venue): the jump target is the chain card anyway, and 8 venues stay readable.
- Missing venue/day shown in primary, not error: it's the next step, not a failure.
- Search failure after Aggiorna is T3 (snackbar + Riprova): previous results stay valid to browse.
- "Filtri modificati" is T4 inside the bar; Aggiorna temporarily takes the Settings spot (Settings still in the large header).
- Chain dots are decorative (name always written); dark theme adds the 1 dp chainEdge stroke.

## Open questions
-
