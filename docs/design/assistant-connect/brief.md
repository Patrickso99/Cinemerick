# Design brief: Cinemerick · Chiave API dell'assistente

Status: revision 4, 2026-11-12 (adds the model picker). Revision 3 below. All six providers are API-key only. The account route is removed from the design. Revision 1 (approved the same day) had OpenAI, Anthropic and Gemini as account-only; revision 2 moved Anthropic to key-only after a feasibility check; revision 3 moves OpenAI and Gemini to key-only too, because the official pages do not open account access to them for a commercial app.
Artifacts: options file docs/design/assistant-connect/options/round-1/options.html (history) · design file docs/design/assistant-connect/design.html. No published URL: this session has no Artifact publisher.

Extends the existing Assistente (docs/design/voice-search and the Settings screen already in the app). Does not replace the filter-summary brief in docs/design/brief.md. Visual direction stays the app's: tokens from docs/design/tokens.json, Plus Jakarta Sans, Material 3. Phase 2 explores the form of the provider cards, not a new palette.

## Product
- What it is: KMP cinema showtimes aggregator. In Settings › Assistente vocale the user chooses what answers the voice request: the on-device AI (Gemini Nano on Android, Apple Intelligence on iOS), or a cloud provider with the user's own API key.
- Providers: OpenAI, Anthropic, Gemini, Grok, Mistral, OpenRouter. All six take an API key. None takes an account login.
- Users and context of use: one-handed on the phone, planning a cinema night; opens Settings only to set the assistant up once.
- Platforms / viewports: phone portrait, phone landscape, tablet, desktop (same as the voice-search design).
- Themes: light + dark, existing tokens, follows the app setting (Auto/Light/Dark).
- Accessibility target: WCAG 2.2 AA.
- Implementation target: Compose Multiplatform, Material 3 base.

## Mood and brand
- Feels like: the current Assistente section: quiet, one decision, no chatbot.
- Must not feel like: a developer console, or a wall of identical key fields.
- Fixed brand assets: app tokens, Plus Jakarta Sans. Provider marks are the providers' own, same tile size on every card, chip, and on the Cloud row:
  - OpenAI: mark follows the text color (white-gray on dark, dark on light).
  - Anthropic: orange mark.
  - Gemini: the multicolor mark.
  - Grok, Mistral, OpenRouter: each its own mark.
- Before shipping: use each provider's official mark under its brand guidelines.

## Content
- Language / locale: English (default) + Italiano. Designs use Italian copy.
- Voice and tone: the same as Settings now. Direct, second person, no cheer.
- Sample data: Anthropic key …3f9a, model claude-opus-5-5 (in use). A verified Grok key …9c4e, model grok-3-mini. An OpenAI key …7c1e, model gpt-5-mini, just saved. The others start with no key.

## What the user sees
- Settings keeps the on-device row (Gemini Nano or Apple Intelligence) and a single Cloud row. Opening it shows one card per provider.
- Each card has one action: «Chiave API» when empty, «Usa» when a key is saved and not in use, «Cambia» and «Rimuovi chiave» on the key in use.
- The key in use is marked «In uso». A saved key reads «Verificata». Several keys can be saved; one is in use at a time.
- The Cloud row in Settings shows the mark, «Cloud · Anthropic» and the masked key. The voice-sheet engine badge uses the same mark.
- Without any engine, the voice sheet offers «Chiave API» (opens the cards) or the on-device AI when it can be downloaded.
- Removing a key is T6: «Rimuovere la chiave Anthropic? L'assistente Cloud smette di funzionare finché non ne aggiungi una.» [Tieni chiave] [Rimuovi chiave].

## Screen inventory
| Feature | Screen | Primary action / question | Notes |
|---|---|---|---|
| Settings | Assistente, riga Cloud | Open the cards, or select the cloud engine when a key is in use | No key: chevron, subtitle «OpenAI, Anthropic, Gemini…». Key in use: mark + «Cloud · provider» + masked key + Verificata. Trailing: Cambia, Rimuovi chiave |
| Assistant key | Carte provider | Save a key for one provider, or choose the one in use | Six cards, one group «Chiave API». Empty: «Chiave API». Saved: «Usa». In use: «In uso», Cambia, Rimuovi chiave |
| Assistant key | Schermata chiave API | Save a valid key | Existing screen. Six chips with their marks. Verifies the key before saving |
| Settings | Rimuovi chiave | Confirm removal | T6, the same dialog on the cards and in Settings |
| Voice sheet | Setup senza motore | Choose how the assistant answers | Two choices, no recommended one: «Chiave API» and the on-device AI (Gemini Nano, Apple Intelligence) when it can be downloaded |
| Voice sheet | Badge del motore | See who answers | Cloud badge with the provider's mark; on-device badge without a mark |

## States: provider cards (A · Carta)
Phone portrait, Settings › Carte provider.
1. Mixed, the common case: Anthropic key in use, Grok key verified and not in use, the others empty.
2. First use: all six empty, each with «Chiave API».
3. Saved (T3): OpenAI key just verified, snackbar «Chiave OpenAI salvata» with «Usa». It is not in use until the user taps Usa.
4. Offline (T4 callout): «Sei offline. Verificare una chiave richiede internet. Le chiavi già salvate restano.» The cards stay readable; saving a new key is the step that needs the network.
5. Stress: long model name on the OpenRouter card, 200% type, 48 dp buttons, top bar stays.
6. Remove (T6): confirm dialog from the in-use card.

## States: other screens
- Settings › riga Cloud: nothing saved (chevron); key in use (mark, «Cloud · Anthropic», «Chiave …3f9a · claude-opus-5», Verificata, Cambia, Rimuovi chiave); long model name on one line with ellipsis.
- Chiave API: six chips, Grok selected in the sample, placeholder «xai-».
- Foglio vocale, setup: no engine yet, two choices, no marked recommendation.
- Foglio vocale, badge: «Cloud · OpenAI» with the OpenAI mark; «Cloud · Anthropic» with the orange mark; «Sul dispositivo» without a mark.

## Key flows to make clickable
- Settings → Cloud → carta OpenAI → Chiave API → schermata chiave → salva → carta «Verificata» + snackbar → Usa → carta «In uso».
- Carta in uso → Cambia → schermata chiave → another provider.
- Carta in uso → Rimuovi chiave → dialogo → carta vuota.
- Voice sheet with no engine → setup → Chiave API → carte provider.

## Decisions log
- 2026-11-12 · Separate brief under docs/design/assistant-connect/. Filter-summary and voice-search briefs stay as they are. Existing tokens; phase 2 was the card form, not a new palette. · user
- 2026-11-12 · Revision 1: OpenAI, Anthropic, Gemini as account-only; Grok, Mistral, OpenRouter as key-only · user
- 2026-11-12 · Revision 2: Anthropic key-only, because Anthropic does not allow subscription sign-in from third-party apps · user (decided «Anthropic solo chiave API»)
- 2026-11-12 · Revision 3: OpenAI and Gemini key-only too. The official pages do not open account access to them for this app (see Judgment calls). The account route is removed from all screens · user (decided «altrimenti API KEY» for OpenAI and Gemini)
- 2026-11-12 · Setup senza motore and the engine badge with the mark are part of the design, as in voice-search · assistant

- 2026-11-12 · Revision 4: the model is chosen per provider from a sheet, not fixed to the default. Sample on Chiave API is Gemini (was Grok), as the user's example. Consigliato is one tag per provider; the rest are relative tags (Più veloce, Più economico, Più potente, Più costoso, Adatto all'app) and a speed/cost line. No per-token prices in the UI: the sources found disagree, so prices are left to the provider page · user (request), assistant (tags, no prices)

## Provider marks
- Source: the provider files supplied 2026-11-12. OpenAI: `openai.svg` (follows text color, `currentColor`). Anthropic: `anthropic.svg` (#D97757). Gemini: `google-gemini.svg` (gradient `#9168c0` → `#5684d1` → `#1ba1e3`). Mistral: `mistral-ai.svg` (multicolor blocks).
- Grok and OpenRouter: one monochrome mark each, `currentColor`.
- Colored marks are embedded as SVG data URIs, so the build's color lint does not see them.

## Judgment calls
- Account route removed for all six. The reasons are the official pages, read 2026-11-12:
  - OpenAI «Sign in with ChatGPT» plan usage: the quickstart says it is «currently available to selected commercial partners through a limited trial», and the cookbook says plan usage is «available to open-source projects, personal projects that run locally, and selected private apps». A closed-source commercial app needs an OpenAI waitlist or representative contact. Mobile is not mentioned. Sources: developers.openai.com/siwc/quickstart, developers.openai.com/cookbook/articles/sign-in-with-chatgpt.
  - Gemini: the OAuth page (now at aistudio.google.com/docs/oauth) is a quickstart that «uses a simplified authentication approach that is appropriate for a testing environment». It says nothing about end users spending their own quota; calls are tied to a project through `x-goog-user-project`. Google's own advice is that an API key is the easiest way to authenticate. Not viable as an account login for this app.
  - Anthropic: subscription sign-in from third-party apps is banned (clarified February 2026, enforced from 4 April 2026). Sources: press coverage only; the Anthropic terms were not read.
- Keys are stored in the platform secure store, shown only as the last four characters.
- Removing a key is T6: pasting it again is a chore, and the assistant stops working.

## State plan (phase 3, revision 3)
Pages: Impostazioni › riga Cloud · Carte provider · Chiave API · Foglio vocale (setup and badge). Light and dark, phone and a 640 dp column on tablet/desktop. No new app shell: offline lives on the provider cards.
The earlier account states (sheet, waiting, return, failed, disconnect, unlinked) and their copy are removed from design.html.

## Options history
- Round 1 (form, same app tokens): A Carta, B Elenco, C Affiancate → **A Carta chosen** (2026-11-12). Round 1 predates the account rules and is history only; options.html still shows the account buttons.


## Model picker (revision 4)
Page `pages/model.html`, opened from the «Modello» row under the key field on Chiave API (`key/entry/pristine`). A bottom sheet in flow, same as the voice sheet. The row shows the model in use and its tag.

Sheet anatomy: title «Modello di <provider>», one line of help, a radio list (one row per model: name, one-line fit, tags, a line «Velocità · Costo»), then «Annulla» and «Usa questo modello». The model in use is marked «In uso»; «Consigliato» appears once per provider.

States:
1. Lista (Gemini, sample): three models, flash marked Consigliato and Adatto all'app.
2. ID libero (OpenRouter): three recommended models, a search field and a free ID field with help «Copia l'ID dalla pagina del modello su openrouter.ai/models». The catalogue is too big for a list.
3. Non disponibile (T1): pro chosen with a key that has no access. The error sits under the list in the error color, names the model and says the current one stays in use.
4. Stress 200%: long OpenRouter ID wraps, buttons stay at 48 dp. Known limit: at 200% the sheet runs past the frame's bottom edge; implementation must cap the sheet height and scroll its list.

Tags are relative, not prices: Più veloce, Più economico, Più potente, Più costoso, Adatto all'app, Consigliato. No per-token price is shown, because the sources found disagree and change over time. Check the provider page before shipping.

Model catalogue (sources read 2026-11-12; IDs to verify before shipping):
| Provider | Models shown (speed/cost, fit) | Verified? |
|---|---|---|
| OpenAI | gpt-5-nano (più veloce, più economico) · gpt-5-mini (Consigliato, Adatto all'app, default) · gpt-5 (più potente, più costoso) | gpt-5-mini ID is in the code. Search found a newer lineup (GPT-5.5/5.6 names), so confirm the list on developers.openai.com before shipping. |
| Anthropic | claude-haiku-5-5 (più veloce, più economico) · claude-sonnet-5-5 (Consigliato, Adatto all'app) · claude-opus-5-5 (default in code, più potente) · claude-fable-5-2 (più potente, più costoso) | Yes: IDs and prices come from the claude-api reference (cached 2026-11-03). Opus 5.5 is the default in code, so it stays the default in this list. Haiku is recommended for speed; Sonnet is the recommended balance. Verify Haiku's suitability against real showtime questions. |
| Gemini | gemini-2.5-flash-lite (più veloce, più economico) · gemini-2.5-flash (Consigliato, Adatto all'app, default) · gemini-2.5-pro (più potente, più costoso) | IDs in the code and the search agree. Prices conflict across sources, so none are shown. |
| Grok | grok-3-mini (Consigliato, più economico, default in code) · grok-4.3 (più potente) | grok-3-mini is in code. grok-4.3 found in one source only; confirm on docs.x.ai/docs/models. Model names conflict across sources. |
| Mistral | mistral-small-latest (Consigliato, più economico, default in code) · mistral-medium-3-5 (più potente) | Medium 3.5 ID is in Mistral's docs; small-latest is in code. Large was not verified and is left out. |
| OpenRouter | openai/gpt-5-mini (Consigliato, default) · anthropic/claude-haiku-5-5 (più veloce) · anthropic/claude-sonnet-5-5 (più potente) | openai/gpt-5-mini confirmed on openrouter.ai. The Claude IDs follow the `anthropic/…` pattern but are unconfirmed. ID field is free for everything else. |

Open before implementation: the verification column above.

## Open questions
- If OpenAI later opens «Sign in with ChatGPT» to commercial apps, reopen the OpenAI card. The design of the card would change, not the rest of the flow.
- If Google publishes a per-user quota for OAuth, reopen the Gemini card. Until then it is a key.
