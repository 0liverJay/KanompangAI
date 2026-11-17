# Ultimate Budgeter — Product Requirements

## 1. Purpose and reconstruction contract

Reconstruct the existing Android application from an empty repository using this four-document specification. The product helps a user allocate monthly income and visualize the composition of actual expenditure. It is a small, offline calculator, with two independent tabs and no server.

Read this document together with [Design](02_DESIGN_SPECIFICATION.md), [Technical](03_TECHNICAL_SPECIFICATION.md), and [Testing](04_TEST_SPECIFICATION.md). These documents describe the supplied repository as inspected on November 17, 2026. Source code and XML are the authority for logic, strings, dimensions, and explicit chart configuration. Existing screenshots are visual references, not proof that the current source has been executed. Runtime behavior that depends on Android or library defaults must be verified in the rebuilt app.

The default target is **faithful source reconstruction**. Preserve the specified validation quirks and error-state behavior. Do not silently modernize the application, add features, or treat optional improvements as required behavior. A functionally equivalent internal implementation is acceptable if all observable requirements match; the technical document also provides the original class and resource structure for close reproduction.

## 2. Identity and audience

| Item | Required value |
|---|---|
| Visible page heading | Ultimate Budgeter |
| Android application label | unidramer |
| Gradle root project | unidramer |
| Application ID / resource namespace | com.github.noey.ultimate.unidramer |
| User interface language | English |
| Currency representation | Literal `$` prefix for estimated monetary outputs; no currency selector |
| Supported platform | Android, minimum API 28, target API 34 |
| Orientation | Portrait |

The user wants a quick monthly budget suggestion or a category percentage breakdown. There is no implemented age, occupation, country, account, or income-period preference. The app does not identify the dollar currency as a specific country's currency.

## 3. Scope

Required capabilities:

- P-01: Launch directly into a home screen with Estimated selected and Actual available.
- P-02: Switch between tabs by tapping a title or swiping the page horizontally.
- P-03: Enter monthly income and generate an estimated allocation across Education, Housing, Food, Transport, and Balance.
- P-04: Enter four actual expense amounts and calculate each category's share of total expenditure.
- P-05: Display textual results and an animated, interactive doughnut chart after successful submission.
- P-06: Provide the exact validation messages and visibility transitions specified below.
- P-07: Perform calculations locally, without accounts, network access, or runtime permission prompts.
- P-08: Maintain independent page state while the existing two fragment views remain alive.

There is no implemented onboarding, transaction ledger, date selector, saved budget, history, database, cloud sync, authentication, notifications, export, settings, income-versus-actual comparison, category editing, or cross-tab data transfer. Actual does not consume Estimated's income or calculate remaining balance. No total expense number is displayed as a separate result.

## 4. Navigation and state

The home screen has a centered heading, two centered tab titles in the order `Estimated`, `Actual`, and a short black indicator under the selected tab. Each page contains its own inputs, action button, result list, and chart. The initial inputs are empty and all result text and charts are hidden using `GONE`.

Input changes only update cached input strings. They do not calculate, clear results, or hide an existing chart. A previous result remains visible while the user edits, until submission causes an update or a validation branch hides it. A successful subsequent submission replaces the previous results and restarts the chart animation. Invalid submission does not erase entered inputs.

Ordinary tab switching keeps each live fragment's inputs and output independent. No explicit saved-state or durable persistence is implemented. Do not promise saved results across activity recreation, force-stop, or process death. Android may restore EditText view state; lifecycle behavior must be characterized rather than assumed.

## 5. Estimated budget workflow

1. Open Estimated.
2. Enter a monthly income in the `Monthly income:` field.
3. Tap `Plan My Budget`.
4. Validate using section 7.
5. Select the allocation band, multiply income by each rate, and display all five monetary lines and chart slices.

P-03 allocation rules:

| Monthly income I | Education | Housing | Food | Transport | End-of-month balance |
|---|---:|---:|---:|---:|---:|
| I < 50,000 | 7.5% | 32.5% | 17.5% | 12.5% | 30% |
| 50,000 ≤ I ≤ 100,000 | 12.5% | 27.5% | 12.5% | 12.5% | 35% |
| I > 100,000 | 17.5% | 22.5% | 7.5% | 12.5% | 40% |

Each category amount is `I × rate`. The implementation computes the balance rate as `1 − educationRate − housingRate − foodRate − transportRate`. Use unrounded amounts for the chart and round only displayed monetary text to two decimal places. Use the default Java formatting locale, matching the source; examples below assume an English locale. There is no thousands grouping in result text.

Exact output templates in display order:

```text
Education Expenses: $<amount with 2 decimals>
Housing Expenses: $<amount with 2 decimals>
Food Expenses: $<amount with 2 decimals>
Transport Expenses: $<amount with 2 decimals>
End-Of-Month Balance: $<amount with 2 decimals>
```

Example: income `9856` produces Education `$739.20`, Housing `$3203.20`, Food `$1724.80`, Transport `$1232.00`, and Balance `$2956.80`. The chart contains these five amounts in the same category order.

## 6. Actual expenditure workflow

1. Open Actual.
2. Fill all four fields in the order Transportation, Education, Housing, Food.
3. Tap `Calculate Your Budget`.
4. Validate all fields using section 7.
5. Let `S = transport + education + housing + food`. For each category, display `amount / S × 100`, formatted to two decimal places.
6. Show four chart slices using raw expense amounts, not the percentage strings.

Exact result order and spacing:

```text
Transport: <percentage with 2 decimals>%
Education:<percentage with 2 decimals>%
Housing:<percentage with 2 decimals>%
Food:<percentage with 2 decimals>%
```

Only Transport has a space after the colon. Input order and chart order differ: the chart order is Education, Housing, Food, Transport. The chart has no Balance slice on Actual. Rounded textual shares may total 99.99% or 100.01%; do not adjust a category to force exactly 100%.

Example: transport `250`, education `6980`, housing `486`, food `666` gives total `8382` internally and output `Transport: 2.98%`, `Education:83.27%`, `Housing:5.80%`, `Food:7.95%`.

## 7. Validation and error-state contract

All fields use Android `inputType="number"`, normally exposing an unsigned integer keyboard. The click handlers nevertheless parse strings as Java doubles. No explicit trimming, maximum length, positive-value test, finite-number test, or amount limit exists.

Validation occurs on button submission in the following order:

| Page | Condition | Exact short Toast | Result visibility |
|---|---|---|---|
| Estimated | Cached income is null or empty | `Numbers cannot be null.` | Hide five result lines and chart |
| Actual | Any cached field is null or empty | `content is empty` | Hide four result lines and chart |
| Either | Any relevant string starts with character `0` | `Numbers cannot start with 0.` | Hide that page's result lines and chart |
| Either | Double parsing throws NumberFormatException | `Invalid number format.` | Preserve prior result/chart visibility and content |
| Either | Parsing succeeds | None | Replace values and show every result line and chart |

Leading-zero rejection applies to `0`, `00`, `0123`, and `0.5`. It is a string rule, not a mathematical zero test. All Actual categories must be filled; a zero category is rejected even though it might make sense in a general budgeting product. Missing values take precedence over leading-zero errors.

Compatibility limitations: injected strings that bypass the number keyboard may include negative numbers, scientific notation, whitespace, NaN, or Infinity. The source does not comprehensively reject them. These are characterization cases, not endorsed supported financial inputs. Do not introduce new validation silently in the faithful reconstruction.

## 8. Acceptance and nonfunctional requirements

- All exact labels, formulas, thresholds, result order, validation messages, and state transitions above must match.
- Correct chart category-to-color mapping and raw amounts must match the design and technical documents.
- Both ordinary user flows must function in airplane mode after installation.
- Successful calculations must not accumulate extra chart entries on repeated submission.
- No financial data is intentionally transmitted or saved by app code.
- The baseline includes a 1,400 ms chart animation on every successful submission. There is no separate loading state.
- Preserve the simple native visual layout. Accessibility, dark-mode contrast, short-screen clipping, and large-font behavior are known quality risks, not verified guarantees.

Completion means the rebuilt project can be built and installed, all mandatory test cases pass, and both screens match the specified layout under a recorded reference device configuration.

## 9. Instructions to a reconstruction agent

Use the following prompt with all four documents supplied:

> Rebuild Ultimate Budgeter as an Android application from scratch. Read 01_PRODUCT_REQUIREMENTS.md, 02_DESIGN_SPECIFICATION.md, 03_TECHNICAL_SPECIFICATION.md, and 04_TEST_SPECIFICATION.md before implementing. Follow faithful source reconstruction mode, preserving exact business rules, UI strings, input validation, visibility transitions, page independence, and explicit chart settings. Do not add features or silently apply optional improvements. Build an installable APK and run the specified functional, numerical, and visual acceptance checks. Record environment-dependent differences and test evidence. If screenshot assets are available, use them as visual references while following the documented source-versus-screenshot precedence.

Deliver the Gradle project, application source/resources, meaningful automated tests, a debug APK, and concise setup and verification instructions. Existing source files are evidence references, not required inputs for a new implementation.
