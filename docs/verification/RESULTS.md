# Reconstruction verification

Verified on November 17, 2026. Production source, resources, wrapper, build configuration, and original reference assets retain their original file contents. Tests, README, verification tools, checksums, and this evidence are additions. Local SDK configuration and generated build outputs are environment-specific.

## Executed checks

| Check | Result | Evidence |
|---|---|---|
| Debug build | PASS; installable APK generated | `app/build/outputs/apk/debug/app-debug.apk` |
| Source fidelity | PASS; 49 restored baseline files match byte for byte | `python3 tools/verify_archive.py`; reconstruction manifest |
| JVM test | PASS; 1 test, 0 failures | [JUnit XML](unit-test-results.xml) |
| Android API 28 | PASS; 7 tests, 0 failures, 0 skipped | [Transcript](instrumentation-api28.txt), [runner log](test-runner-api28.txt) |
| Android API 34 | PASS; 7 tests, 0 failures, 0 skipped | [Transcript](instrumentation-api34.txt), [runner log](test-runner-api34.txt) |
| Android lint | PASS task; 89 warnings, no errors | [Lint XML](lint-results-debug.xml), [build checks](build-checks.txt) |
| Offline operation | PASS for both numerical workflows | API 34 airplane mode enabled; API 28 Wi-Fi and mobile data disabled |
| Visual review | Four specified states captured on each reference emulator | Screenshot links below |

Instrumentation was executed directly with `adb shell am instrument -w com.noey.test/androidx.test.runner.AndroidJUnitRunner`. Both test APKs compiled successfully. The final API 34 transcript is extracted from a shared command-output stream; its unedited stream is retained as `instrumentation-api34-raw.log`. A superseded invocation appended an old termination marker. The final independent TestRunner log confirms **7 tests, 0 failed, 0 ignored** for the completed run.

## Reference environments

Build host: macOS arm64, JDK 17.0.10, Gradle 8.6, AGP 8.4.0. Android SDK 34; Java source/target level 8. Library versions remain those listed in the technical specification.

Both reference emulators use portrait 1080 × 1920 pixels, density 420 dpi (2.625), font scale 1.0, light mode, and English/default formatting set to Locale.US during numerical tests. Screen captures include system chrome and are not cropped. API 34 reports secure `navigation_mode=2` (gesture navigation); API 28 returns `null` for that setting, so no explicit navigation-mode override is recorded. The chart text and result text are platform/library defaults. API 28 status bar occupies 63 pixels in the captures; content begins below it. The API 34 reference capture has the same content start. No reference screenshot's status-bar time is treated as application content.

| Device | OS fingerprint | Network state |
|---|---|---|
| Temporary API 28 arm64 emulator | `Android/sdk_gphone_arm64/generic_arm64:9/PSR1.210301.009.B6/9767327:userdebug/dev-keys` | Wi-Fi off, mobile data off |
| Temporary API 34 arm64 emulator | `google/sdk_gphone64_arm64/emu64a:14/UE1A.230829.050/12077443:userdebug/dev-keys` | Airplane mode on |

The API 28 emulator initially limited a display override. Its physical display configuration was corrected, it was rebooted, and all four final screenshots were recaptured at the reference resolution.

## Functional coverage

`BudgetAcceptanceTest` covers initial hidden outputs; all nine Estimated numerical fixtures and all five Actual fixtures; exact result prefixes and spacing; unrounded chart values, category order, palette, description, highlight enablement, and data replacement; missing/leading-zero validation in every Actual field; injected parser failure preserving prior output; editing without submission; independent live pages; repeated calculation; portrait configuration; real tab taps and horizontal swipes; and four reference-state captures.

`ToastContractTest` observes actual Android accessibility notification events. It asserts all four exact Toast strings and verifies missing input takes precedence over a leading zero, and a leading zero takes precedence over malformed numeric input. `ArchiveFidelityTest` checks actual production files against the independent supplied baseline; it does not reimplement the calculator.

## Document discrepancies and preserved behavior

The production package, namespace, application ID, and launcher are `com.noey`. The documents describe a different package ending in `ultimate.unidramer` and Java classes under `ultimate.life`. The original identity is retained to satisfy the explicit exact-source reconstruction requirement. No production file was renamed or edited to hide that discrepancy.

On Android's formatter, income `49999` produces two strings that differ from the Test Specification E-03 table:

| Output | Specification table | Observed API 28 and 34 |
|---|---:|---:|
| Education | 3749.93 | 3749.92 |
| Food | 8749.83 | 8749.82 |

Other listed numerical fixtures match. The original Java double multiplication and default-locale `String.format("%.2f", ...)` are retained. The initial strict-table check and its observed differences are recorded in [rounding evidence](rounding-api34.txt). Android acceptance fixtures explicitly use the observed original formatter outputs for these two fields. The code was not changed to force the specification table's rounding.

MPAndroidChart converts the configured description text size of 12 to density-scaled pixels, so the getter returns 31.5 at 420 dpi. Tests assert that conversion while preserving the source's `12f` setting.

The original Espresso dependency graph has a runtime tracing conflict (`forceEnableAppTracing` is unavailable in the app-loaded tracing version). Navigation verification uses native instrumentation touch events instead. No dependency upgrades or production changes were needed.

## Visual evidence

| State | API 28 | API 34 |
|---|---|---|
| Estimated result, income 9856 | [Screenshot](api28/estimated-result.png) | [Screenshot](api34/estimated-result.png) |
| Actual result, 250 / 6980 / 486 / 666 | [Screenshot](api28/actual-result.png) | [Screenshot](api34/actual-result.png) |
| Estimated empty submission | [Screenshot](api28/estimated-empty.png) | [Screenshot](api34/estimated-empty.png) |
| Actual housing missing | [Screenshot](api28/actual-incomplete.png) | [Screenshot](api34/actual-incomplete.png) |

Captures wait 1,700 ms after submission, beyond the 1,400 ms chart animation. Error captures may still show the short Toast. Visual review confirms the heading, tab order, indicator, outlined forms, intentionally staggered Actual fields, buttons, output ordering, hidden invalid outputs, and doughnut structure. Palette correctness is also checked from chart data rather than inferred from screenshots.

The fixed chart-description coordinates can overlap the legend or differ in visibility between platforms. API 28's result capture shows description/legend overlap; this is inherited. Small slices have crowded labels. Exact pixel identity across Android versions is not claimed because system and library defaults vary.

## Lint and acceptance limits

The 89 inherited warnings include hardcoded strings, RTL left/right alignment, default-locale formatting, unused resources, old target/tooling versions, missing autofill/accessibility metadata, portrait lock, small text, and deprecated APIs. There are no lint errors. They remain to preserve the original source.

Night mode, large fonts, keyboard-open/short-screen layouts, RTL and comma-decimal locale, activity recreation/process death, injected non-finite/negative/overflow values, and zero-sum injected expenses have not received a full runtime characterization sweep. These are unresolved characterization cases from Q-02 through Q-11, not verified guarantees. No persistent result restoration is promised. Tap-to-highlight is enabled and inspected in chart configuration; individual slice highlighting has not received a separate touch assertion. A literal airplane-mode toggle was not used on API 28, although both network transports were disabled.

The delivered application is built, installed, and its implemented acceptance suite passes on the minimum and target Android versions. Full completion of every characterization item in the test plan remains distinct from these executed checks.
