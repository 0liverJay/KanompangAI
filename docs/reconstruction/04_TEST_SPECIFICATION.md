# Ultimate Budgeter — Test Specification

## 1. Goal, status, and acceptance mode

Verify that an independently reconstructed Android app matches [Product](01_PRODUCT_REQUIREMENTS.md), [Design](02_DESIGN_SPECIFICATION.md), and [Technical](03_TECHNICAL_SPECIFICATION.md). This document is a test plan derived from inspected source and reference screenshots. The original repository contains no unit or instrumentation test source files. No build, emulator session, or application test was run while producing these documents.

Mandatory cases below cover ordinary supported input and explicit source behavior. Characterization cases document unresolved library/platform behavior or existing defects; they must be investigated and reported, but are not instructions to silently fix them. Use faithful source reconstruction as the default mode.

## 2. Test environment and evidence

Run unit tests on the build JVM and instrumentation/manual checks on an Android emulator or device. Recommended minimum coverage: API 28 and API 34, English locale, portrait, normal font scale, light mode. Select one reference device and record its resolution, density, usable app area, font scale, OS build, navigation mode, and library versions before visual comparison. Original screenshot metadata does not establish all these parameters.

Also characterize night mode, a comma-decimal locale, RTL locale, a short portrait screen, increased font scale, keyboard-open layout, activity recreation, and process restart. Use airplane mode to verify offline behavior after dependencies and APK are installed.

For each case record: test ID, input strings, action sequence, expected versus actual result, status, device/environment, and relevant screenshot or log. Capture result screenshots after the 1,400 ms animation finishes, with a small scheduling margin. Toasts may require a dedicated assertion or timed capture; blank screenshots alone do not verify their text.

## 3. Build checks

| ID | Action | Expected |
|---|---|---|
| B-01 | Inspect Gradle/configuration | SDK 34/34/28, app ID, version 1/1.0, Java 8 compatibility, pinned dependencies match Technical |
| B-02 | Run :app:assembleDebug | Build succeeds and debug APK exists |
| B-03 | Install and launch on API 28 and 34 | Launcher activity opens, no runtime permission prompt |
| B-04 | Run meaningful unit tests and instrumentation tests | Implemented mandatory cases pass |
| B-05 | Run :app:lintDebug | Record findings; resolve reconstruction-introduced failures and document inherited fidelity limitations |
| B-06 | Inspect app manifest/code for external services | No added network permission, API client, account flow, or financial persistence |

Commands and expected APK path are in Technical section 10. Do not report tests as passed merely because the test dependencies exist or an empty test task succeeds.

## 4. Navigation and state tests

| ID | Steps | Expected |
|---|---|---|
| N-01 | Fresh launch | Heading Ultimate Budgeter; Estimated selected; empty income; no result text/chart |
| N-02 | Tap Actual | Actual selected; four empty inputs; no output/chart |
| N-03 | Swipe in each direction | Correct page and selected indicator stay synchronized |
| N-04 | Calculate Estimated, switch to Actual and back | Live Estimated input and output retained; Actual independent |
| N-05 | Calculate both pages, alternate tabs | Each page retains its own live values; no cross-page calculation |
| N-06 | Change valid input without submitting | Old result remains unchanged until submission |
| N-07 | Submit valid values repeatedly | Results replaced; no duplicate lines/slices; animation restarts |

N-04 and N-05 apply to ordinary tab switching without activity destruction. Activity/process restoration is characterized separately.

## 5. Estimated numerical oracle

Output columns are Education, Housing, Food, Transport, Balance. All expected text numbers below use an English locale and two decimals without thousands separators. Assert arithmetic using a suitable double tolerance (for example 1e-8 for these fixtures); assert UI strings exactly. At the model level, the sum of unrounded amounts should approximate income.

| ID | Income string | Expected five displayed amounts |
|---|---:|---|
| E-01 | 9856 | 739.20 / 3203.20 / 1724.80 / 1232.00 / 2956.80 |
| E-02 | 40000 | 3000.00 / 13000.00 / 7000.00 / 5000.00 / 12000.00 |
| E-03 | 49999 | 3749.93 / 16249.68 / 8749.83 / 6249.88 / 14999.70 |
| E-04 | 50000 | 6250.00 / 13750.00 / 6250.00 / 6250.00 / 17500.00 |
| E-05 | 75000 | 9375.00 / 20625.00 / 9375.00 / 9375.00 / 26250.00 |
| E-06 | 100000 | 12500.00 / 27500.00 / 12500.00 / 12500.00 / 35000.00 |
| E-07 | 100001 | 17500.18 / 22500.23 / 7500.08 / 12500.13 / 40000.40 |
| E-08 | 120000 | 21000.00 / 27000.00 / 9000.00 / 15000.00 / 48000.00 |
| E-09 | 1 | 0.08 / 0.33 / 0.18 / 0.13 / 0.30 |

E-03, E-07, and E-09 must use Java String.format semantics rather than a different language's tie-rounding convention. Check full line prefixes and the exact `End-Of-Month` capitalization. Chart data must use unrounded amounts and the order Education/Housing/Food/Transport/Balance. Do not require rounded text totals to equal income exactly.

## 6. Actual numerical oracle

Input order is Transport, Education, Housing, Food. Expected text order is also Transport, Education, Housing, Food, but chart order is Education, Housing, Food, Transport.

| ID | Inputs T / E / H / F | Internal total | Expected displayed percentages T / E / H / F |
|---|---|---:|---|
| A-01 | 250 / 6980 / 486 / 666 | 8382 | 2.98 / 83.27 / 5.80 / 7.95 |
| A-02 | 100 / 100 / 100 / 100 | 400 | 25.00 / 25.00 / 25.00 / 25.00 |
| A-03 | 100 / 200 / 300 / 400 | 1000 | 10.00 / 20.00 / 30.00 / 40.00 |
| A-04 | 1 / 1 / 1 / 3 | 6 | 16.67 / 16.67 / 16.67 / 50.00 |
| A-05 | 1 / 1 / 1 / 1 | 4 | 25.00 / 25.00 / 25.00 / 25.00 |

Assert `Transport: 2.98%` with a space after the colon and `Education:83.27%`, `Housing:5.80%`, `Food:7.95%` without spaces. A-04 deliberately totals 100.01% after rounding and must not be normalized. A-03 chart values must be `[200, 300, 400, 100]`; no Balance slice or separate total label appears.

## 7. Validation matrix

Run each applicable invalid case both from an initially blank result state and after a successful calculation. For Actual, supply valid positive values in fields not targeted by the case. Repeat missing and leading-zero tests for each of the four fields.

| ID | Page / input | Expected Toast | Expected result state |
|---|---|---|---|
| V-01 | Estimated untouched | Numbers cannot be null. | All five texts and chart GONE |
| V-02 | Estimated type then clear | Numbers cannot be null. | All five texts and chart GONE |
| V-03 | Actual all untouched | content is empty | All four texts and chart GONE |
| V-04 | Actual any one field empty | content is empty | All four texts and chart GONE |
| V-05 | Either, relevant value 0 | Numbers cannot start with 0. | Page outputs GONE |
| V-06 | Either, relevant value 0123 | Numbers cannot start with 0. | Page outputs GONE |
| V-07 | Either, injected value 0.5 | Numbers cannot start with 0. | Page outputs GONE |
| V-08 | Either, injected unparsable abc | Invalid number format. | Prior output content and visibility unchanged |
| V-09 | Actual blank plus leading-zero field | content is empty | Outputs GONE; missing check wins |
| V-10 | Actual leading-zero plus malformed field, no blanks | Numbers cannot start with 0. | Outputs GONE; startsWith check wins |
| V-11 | Correct invalid value and submit again | No Toast on success | Correct outputs and chart shown |

Injected parser inputs may need a test seam or instrumentation that bypasses number-only EditText filtering. Failure to type alphabetic text through the ordinary keyboard is not proof that the parsing error branch works. A model/controller test may verify the branch if UI filtering prevents it. Validate exact punctuation, case, and Toast.LENGTH_SHORT semantics.

## 8. Visual and chart acceptance

| ID | Check | Expected |
|---|---|---|
| D-01 | Shared heading and tabs | Heading 24sp bold centered; tabs 14sp black; short 12dp × 3dp indicator |
| D-02 | Input rows | 16dp page padding; bold 14sp labels; 40dp outlined inputs; exact hints |
| D-03 | Actual field alignment | Individual label widths produce staggered input left edges |
| D-04 | Buttons | 300dp × 40dp, 6dp corners, white fill and gray outline; specified margins and copy |
| D-05 | Result ordering | Estimated five amounts; Actual four percentages; exact labels and spacing |
| D-06 | Initial / empty error states | Hidden outputs consume no visible result/chart space |
| C-01 | Estimated chart data | Five raw-amount entries in specified order |
| C-02 | Actual chart data | Four raw-amount entries in specified order, no Balance |
| C-03 | Palette | #FF6384, #36A2EB, #FFCE56, #4BC0C0, optional #9966FF mapped correctly |
| C-04 | Chart description | Enabled, Monthly Expenditures, 12f, DKGRAY, configured pixel position 900/750 |
| C-05 | Legend and labels | Pinned library defaults; category labels and Expenditures dataset name preserved |
| C-06 | Animation | Successful submission triggers animateY(1400); capture final state afterward |
| C-07 | Tap slice | Slice highlighting enabled; no app-created details dialog |
| C-08 | Recalculate | New data replaces old entries; palette mapping unchanged |

Create four acceptance captures corresponding to the existing Estimated result, Actual result, Estimated empty, and Actual incomplete reference states. For Actual incomplete, use 250 / 6980 / empty / 666 and submit. Compare layout structure, spacing, proportions, copy, and state. Account for OS chrome and font/density differences. Explicit source RGB values override screenshot hue differences; an offscreen fixed description is a fidelity limitation, not grounds for silently moving it.

## 9. Offline, lifecycle, and edge characterization

| ID | Scenario | Evaluation |
|---|---|---|
| Q-01 | Airplane mode, calculate on both tabs | Mandatory: ordinary flows still work |
| Q-02 | Background and foreground without destruction | Existing in-memory view data should remain |
| Q-03 | Recreate activity / kill and relaunch process | Record restored inputs, cached strings, output visibility; no explicit persistence guarantee |
| Q-04 | Enable night mode | Record contrast; explicit black text and white button surfaces may be unsuitable |
| Q-05 | Short screen, keyboard open, large fonts | Record clipping/overlap; no ScrollView in baseline |
| Q-06 | RTL locale | Record legacy left/right alignment behavior despite supportsRtl |
| Q-07 | Comma-decimal locale | Record default-locale formatting; do not require English decimal points in this mode |
| Q-08 | Inject -1, 1.5, 1e3, whitespace-prefixed numbers | Record parser acceptance versus keyboard restrictions; no general positivity or trim rule |
| Q-09 | Inject NaN, Infinity, huge finite values | Record unguarded results, float overflow, chart behavior or crashes; classify as inherited limitation |
| Q-10 | Inject Actual amounts summing to zero | Record division-by-zero behavior; ordinary valid integer input cannot reach this state |
| Q-11 | Highly uneven actual expenses | Record small-slice label overlap; preserve categories and mapping |
| Q-12 | Portrait orientation enforcement | Mandatory: app remains in portrait when device orientation changes |

Q-08 through Q-10 are not promises of supported financial input. If implementing an improvement mode, update the product contract and expected tests before changing their behavior.

## 10. Automation guidance

Prefer unit tests for band selection, multiplication, percentage arithmetic, and ordered validation. Use Espresso/instrumentation or equivalent for navigation, input/button actions, visibility, exact text, and live tab state. Chart assertions should inspect data entries and configuration where possible; pixel-only tests are insufficient to establish arithmetic correctness. Do not add tests that merely repeat implementation without checking independent expected values.

For the original public calculation helpers, account for their TextView side effects; they cannot be invoked as pure JVM functions without Android dependencies. A reconstruction may expose a pure calculation layer and keep UI binding separate while preserving the same behavior. Test parser-failure stale-output behavior explicitly so a refactor does not accidentally change it.

## 11. Traceability and completion checklist

| Product requirement | Primary tests |
|---|---|
| P-01 Home launch | B-03, N-01, D-01 |
| P-02 Tab navigation | N-02, N-03 |
| P-03 Estimated allocation | E-01 through E-09, D-05, C-01 |
| P-04 Actual composition | A-01 through A-05, D-05, C-02 |
| P-05 Text/chart results | N-07, C-01 through C-08 |
| P-06 Validation | V-01 through V-11, D-06 |
| P-07 Offline local operation | B-06, Q-01 |
| P-08 Independent live page state | N-04 through N-06 |

Before delivery, verify the APK builds and launches, mandatory cases pass on recorded environments, four reference-state screenshots are captured, and characterization findings are documented. List all deviations explicitly. Do not mark a blocked build or unexecuted test as passed. Deliver commands, test reports, screenshots, and the debug APK path alongside the rebuilt project.
