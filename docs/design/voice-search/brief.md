# Design brief: Cinemerick · Ricerca vocale (voice search assistant)

Status: iterating (full design published 2026-11-11)
Artifacts: options https://claude.ai/artifact/D19Jjn1xji51TxWccbiK7A · design https://claude.ai/artifact/RE55PEYQkCWxAW4NAk3kqW

Tokens: reuses docs/design/tokens.json (the app's tokens, already AA-checked). The visual direction is fixed;
phase 2 explores the form of the voice sheet and its entry point, not palettes.

## Product
- What it is: KMP cinema showtimes aggregator (8 chains, venues discovered at runtime). New feature: one tap on a mic, say what you want ("The Space Silea, UCI Marcon e Notorious Fiume, giovedì e venerdì dalle 17 alle 20, solo 2D"), the app pre-fills venues, days, per-day time windows, formats and film filter, then searches.
- Users and context of use: one-handed on the phone, planning a cinema night with friends; wants to skip tapping through venue sheet, calendar, sliders and chips.
- Platforms / viewports: phone portrait, phone landscape, tablet, desktop (JVM).
- Themes: light + dark, existing tokens, follows the app setting (Auto/Light/Dark).
- Accessibility target: WCAG 2.2 AA. Voice must never be the only way: every parsed value is editable by touch, and the request can be typed.
- Implementation target: Compose Multiplatform, Material 3 base.

## AI engine (only what the user sees)
- Default engine is the platform's own AI: Gemini Nano on Android, Apple Intelligence on iOS/macOS. No download.
- Where it is missing (older phones, Windows, Linux): the user can download Gemma 4 E2B (~2,6 GB, one-time, resumable) or connect an API key.
- Cloud providers: OpenAI, Anthropic, Google Gemini, xAI Grok, Mistral, OpenRouter (covers Llama, DeepSeek…). Cursor excluded: no model API of its own.
- Even with native AI available the user can switch engine in Settings; native is preselected.
- Privacy: Settings shows "La richiesta viene inviata a <provider>" for cloud; the voice sheet shows a small badge "Sul dispositivo" / "Cloud · Anthropic". No blocking consent dialog.

## Mood and brand
- Fixed brand assets: all existing tokens, Plus Jakarta Sans, chain badge colors (`CinemaBrandColors`), icon set in `CinemerickIcons` (a mic icon to add in the same style).
- Feels like: quick, confident, unobtrusive; the assistant is a shortcut to the filters, not a chatbot.

## Content
- Language: UI in English (default) + Italiano, designs use Italian copy. Speech: Italian and English, detected automatically.
- Sample data: The Space · Silea, UCI · Marcon, Notorious · Fiume Veneto; Gio 12, Ven 13 17:00–20:00 (example 1); Gio 12 17:00–20:00 + Ven 13, Sab 14, Dom 15 20:00–24:00 (example 2; today is Wed 11 Nov 2026); solo 2D; film "Hope".

## Interaction decisions (interview, 2026-11-11)
- Entry icon: the FAB icon must say "AI", not just "microphone" (e.g. mic combined with a sparkle), in the `CinemerickIcons` style; its accessible label names the assistant ("Chiedi all'assistente"), not just "Microfono".
- Entry: dedicated mic FAB (56 dp) at the bottom end. When "Copia sondaggio" is visible they stack: mic above, extended Copia sondaggio below.
- Listening UI: bottom sheet at half height with voice level + live transcript; the same sheet turns into the "Ho capito" review.
- Review: parsed values as editable chips grouped by Cinema / Giorni e orari / Formato / Film; one primary "Cerca".
- Unrecognized or ambiguous items: highlighted chip to resolve ("Notorius Fiume?" → choose "Notorious · Fiume Veneto"), Cerca still works for the clear parts.
- Transcript is an editable text field: fix a word or type the whole request (silent places, users who can't speak).
- Merge rule: the request replaces only the filter groups it mentions; unmentioned groups stay as they are (shown as "invariato" in the review).
- Settings: new "Assistente" section: engine selector (Sul dispositivo / Gemma 4 E2B / Cloud), Gemma download state, provider + API key.
- First mic tap without any engine: setup sheet (download Gemma, or connect a key → Settings).

## Screen inventory
| Feature | Screen | Primary action / question | Notes |
|---|---|---|---|
| Voice search | Home with mic FAB (phone portrait/landscape, tablet, desktop) | Start a voice request | FAB stacks with Copia sondaggio |
| Voice search | Mic permission rationale | Allow the microphone | OS dialog follows; denied / permanently denied states |
| Voice search | Voice sheet · listening | Speak, then stop | live transcript, level, engine badge |
| Voice search | Voice sheet · interpreting | Wait | short progress |
| Voice search | Voice sheet · review "Ho capito" | Cerca | editable chips, items to resolve, editable transcript |
| Voice search | Resolve item (venue/film alternatives) | Pick the right match | inline menu or small sheet |
| Voice search | Engine setup sheet (no engine yet) | Download Gemma or connect a key | |
| Settings | Assistente section | Choose engine | download progress/errors, provider list, API key field + validation, privacy note |
| Settings | API key entry | Save a valid key | provider picker, show/hide key, test |

## State plan (phase 3)
Pages: Design system · Layout per viewport · Voice sheet · First-time setup · Settings › Assistente.

### Home · AI FAB
1. No results: AI FAB alone at bottom end.
2. Results: AI FAB above "Copia sondaggio" (default).
3. Summary panel open: both FABs hidden (as Copia today).
4. Landscape / tablet / desktop: FAB stack at the bottom end of the results pane; the voice sheet is max 640 dp wide, centred; landscape sheet uses up to 90% height.

### Voice sheet
5. Mic rationale (first use, before OS prompt) · inside the sheet: "Consenti microfono" / "Scrivi invece".
6. Mic denied permanently · T4 in sheet: "Apri impostazioni" + typing fallback.
7. Listening · waiting: empty transcript, two example phrases.
8. Listening · live transcript (default).
9. Nothing heard (8 s silence) · T4 in sheet: "Riprova" / "Scrivi".
10. Typing mode: transcript field focused, keyboard, "Interpreta".
11. Interpreting: groups as skeleton, "Annulla".
12. Review · all clear (example 1): Formato "Solo 2D", Film "invariato".
13. Review · to confirm (example 2): resolve box "«Notorius Fiume» è Notorious · Fiume Veneto?".
14. Review · choose another venue: resolve box expands to a candidate list (venues of that chain + closest names).
15. Review · not found (no close match; a close match is state 13 instead): "«Cinema Astra» non è tra le sale" with "Ignora" / "Scegli sede".
16. Review · nothing understood · T4: no Cerca, primary becomes "Riparla".
17. Review · stress: 8 venues in 5 chains, 5 days with different windows, 3 film titles, 200% font → body scrolls, Cerca stays pinned.
18. Engine error · T4 in sheet: "L'assistente non ha risposto" / "Riprova".
19. Cloud key rejected · T4: "La chiave Anthropic non è più valida" / "Apri Impostazioni".
20. Cloud offline · T4: "Sei offline. Con Cloud serve internet" / "Usa Sul dispositivo" (only if available).
21. Cloud quota/rate limit · T4: "Anthropic ha rifiutato la richiesta: limite raggiunto" / "Riprova più tardi".
22. After Cerca: sheet closes, filters update, search runs; T3 "Filtri impostati dalla voce · Annulla" restores the previous filters.

### First-time setup (no engine available)
23. Choice between equal options (none marked recommended): "Gemma 4 E2B · sul dispositivo · download 2,6 GB" and "Chiave API · OpenAI, Anthropic, Gemini, Grok, Mistral, OpenRouter". Same list as Settings.
24. Downloading: determinate, "1,2 di 2,6 GB", can close (continues in background, Settings shows progress).
25. Download interrupted · T4: "Riprende da 1,2 GB" / "Riprendi".
26. Not enough space · T4: "Servono 2,6 GB liberi, ne hai 1,1" / "Usa una chiave API".
27. Ready → goes straight to Listening (T3 "Assistente pronto").
28. Mic tapped while downloading: progress + "Ti avviso quando è pronto"; typing also needs an engine, so the sheet offers "Usa una chiave API" as the other way in.

### Settings › Assistente
29. Native selected (Android "Gemini Nano", iOS/macOS "Apple Intelligence") · default.
30. Native unavailable: option shown disabled with "Non disponibile su questo dispositivo" (Windows/Linux: option hidden).
31. Native not ready (Apple Intelligence off / Gemini Nano not yet downloaded by the system) · T4: "Attiva Apple Intelligence in Impostazioni" / "Apri".
32. Gemma: not downloaded / downloading / downloaded (2,6 GB, "Elimina modello").
33. Delete Gemma · T6: "Eliminare Gemma 4 E2B? Liberi 2,6 GB; per usarlo di nuovo va riscaricato." [Tieni] [Elimina modello].
34. Cloud selected with verified key: "Anthropic · ••••3f9a · verificata" + privacy note.
35. Cloud selected, no key yet: row "Aggiungi chiave".

### API key screen
36. Pristine: provider radios (6), key field with show/hide, "Dove trovo la chiave?" link per provider.
37. Verifying: "Salva" loading, fields read-only.
38. Rejected · T1: "Anthropic non ha accettato questa chiave. Controlla di averla copiata intera."
39. Can't verify (offline) · T3: "Impossibile verificare: sei offline · Riprova".
40. Saved: back to Settings + T3 "Chiave Anthropic salvata".
41. Remove key · T6: "Rimuovere la chiave Anthropic? L'assistente Cloud smette di funzionare finché non ne aggiungi una." [Tieni chiave] [Rimuovi chiave].

Accessibility: FAB "Chiedi all'assistente"; sheet is a dialog, focus on the stop button while listening, on the first item to confirm (else Cerca) in review; transcript is a polite live region; voice level is decorative; Animazioni off → static orb, no pulsing. Targets ≥ 48 dp.

## Key flows to make clickable
- FAB → listening → interpreting → review (example 1, all clear) → Cerca → results with filters applied.
- FAB → listening → review (example 2) with "Notorius Fiume?" to resolve → pick → Cerca.
- First tap, no engine → setup sheet → Download Gemma → progress → ready → listening.
- Settings → Assistente → Cloud → Anthropic → paste key → verified.

## Decisions log
- 2026-11-11 · Separate workspace docs/design/voice-search/, existing filter-summary design untouched · user choice
- 2026-11-11 · Dedicated mic FAB, stacked above Copia sondaggio · user choice
- 2026-11-11 · Review sheet before searching · user choice (protects from transcription errors)
- 2026-11-11 · Request replaces only the groups it mentions · user choice
- 2026-11-11 · Native AI default; Gemma download or cloud API key as alternatives; selectable everywhere · user choice
- 2026-11-11 · Provider list: OpenAI, Anthropic, Gemini, Grok, Mistral, OpenRouter (Cursor dropped) · user choice after pushback
- 2026-11-11 · Ambiguities resolved via chips in the review · user choice
- 2026-11-11 · Speech language auto-detected IT/EN · user choice
- 2026-11-11 · Bottom sheet for listening + review · user choice
- 2026-11-11 · Brief approved · user
- 2026-11-11 · FAB icon must read as AI (mic + AI cue), not a plain mic · user request at approval gate
- 2026-11-11 · Editable transcript (typing fallback) · user choice
- 2026-11-11 · Privacy: note in Settings + engine badge in the sheet · user choice
- 2026-11-11 · Direction A · Scheda chosen · user choice
- 2026-11-11 · Delete Gemma = T6 · user confirmed
- 2026-11-11 · Remove API key = T6 (not T3 + Undo) · user choice
- 2026-11-11 · Unknown venue with no close match is ignored on Cerca; a close match becomes a confirmation · user confirmed
- 2026-11-11 · «Copia sondaggio» becomes an icon-only FAB (56 dp, same CinemerickIcons.Copy / ic_copy icon as today, primary) under the AI FAB; label kept as accessible name + tooltip. Changes the existing app's extended FAB · user request
- 2026-11-11 · Gemma is one option among others: never presented as the recommended or default fallback · user

## Options history
- Round 1 (form, same tokens): A Scheda (mic+spark icon, orb listening, chip groups + resolve box), B Frase evidenziata (wave+spark icon, live highlighted sentence, tap a word to fix), C Filtri che si compilano (mic + AI badge, filter rows fill while speaking, compact voice pill) → **A chosen** (2026-11-11): familiar, reads like the Home filters, resolve box is explicit. Tokens: docs/design/tokens.json (app tokens, unchanged).

## Judgment calls
- Unmentioned groups stay visible in the review as «Non citato: resta com'è», so the merge rule is never invisible.
- Relative times: «sera» 18–24, «pomeriggio» 14–18, «dopo le 20» 20–24; «questo giovedì» = next Thursday, today included.
- Candidate list for a close match: max 3 (same city, same chain, similar name) + «Ignora».
- Offline with Cloud offers «Usa Sul dispositivo» for that request only, when a local engine is ready.
- API key stored in the system secure store (Keystore / Keychain), shown only as last 4 characters.
- Desktop shortcut Ctrl+M / ⌘M opens the assistant (proposal).

## Open questions
- Which model per cloud provider (fixed small default vs. user choice)? Not designed: the app picks a fast default.
