# Ultimate Budgeter

A small offline Android budgeting calculator with two independent tabs: **Estimated** allocates monthly income, and **Actual** shows the percentage composition of four expenses. The interface is English and portrait-only. There are no accounts, servers, stored budgets, or runtime permission requests.

## Specification and fidelity

The requirements, design, implementation contract, and acceptance plan are in [docs/reconstruction](docs/reconstruction/). The original production Java, XML, icons, Gradle configuration, and wrapper are preserved byte for byte. Verification tests and documentation are additions; they do not change application behavior.

Java package ending in `.life`. The supplied  uses **`com.noey`** for its namespace, application ID, Java package, and launcher activity. This project retains `com.noey` to honor the exact-code reconstruction requirement. The root project and application label are `unidramer`; the visible heading is `Ultimate Budgeter`.

## Requirements and setup

- JDK 17, Android SDK platform 34, and Android SDK platform-tools.
- Android 9 / API 28 or newer for installation.
- Network access for the first build to obtain pinned Gradle and Maven dependencies. Runtime calculations work offline.

Open the project root in Android Studio and select JDK 17 for Gradle. Let Android Studio create `local.properties`, or create it yourself:

```properties
sdk.dir=/absolute/path/to/your/Android/sdk
```

On macOS/Linux:

```sh
chmod +x gradlew
./gradlew :app:assembleDebug
```

On Windows, use `gradlew.bat :app:assembleDebug`. The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

Install and launch on a connected device:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.noey/com.noey.HomeActivity
```

## Using the app

**Estimated:** enter monthly income, then tap **Plan My Budget**. Income `9856` produces Education `$739.20`, Housing `$3203.20`, Food `$1724.80`, Transport `$1232.00`, and End-Of-Month Balance `$2956.80`.

| Income | Education | Housing | Food | Transport | Balance |
|---|---:|---:|---:|---:|---:|
| Below 50,000 | 7.5% | 32.5% | 17.5% | 12.5% | 30% |
| 50,000 through 100,000 | 12.5% | 27.5% | 12.5% | 12.5% | 35% |
| Above 100,000 | 17.5% | 22.5% | 7.5% | 12.5% | 40% |

**Actual:** enter Transportation, Education, Housing, and Food expenses, then tap **Calculate Your Budget**. Values `250 / 6980 / 486 / 666` produce `Transport: 2.98%`, `Education:83.27%`, `Housing:5.80%`, and `Food:7.95%`. The app divides each expense by their combined total; it does not use Estimated income.

Tap a tab or swipe horizontally to switch. Both live pages keep their own input and results. Editing input keeps the previous result visible until another submission. Every successful calculation replaces the chart data and starts a 1,400 ms animation. Chart slices use raw amounts, in Education/Housing/Food/Transport/optional Balance order.

## Validation and preserved limitations

Missing Estimated income shows `Numbers cannot be null.` Missing Actual fields show `content is empty`. Values beginning with `0` show `Numbers cannot start with 0.` These cases hide that page's results and chart. A parsing failure shows `Invalid number format.` and leaves previous output unchanged.

Inputs use the unsigned integer keyboard. The source does not add comprehensive positivity, finite-number, or overflow checks for injected values. Displayed numbers use Java's default locale, with two decimal places. Rounded percentages need not total exactly 100%.

There is no explicit saved-result model or durable persistence. The fixed-height layout has no scrolling; small screens, open keyboards, large fonts, and night mode can cause layout or contrast problems. The chart description uses a fixed pixel position. These inherited behaviors are preserved.

## Code structure and execution flow

```text
app/src/main/java/com/noey/
  HomeActivity.java       # Hosts tabs and ViewPager
  UltimateAdapter.java    # Ordered fragment/title lists
  EstimatedFragment.java # Income validation, allocation, money text, chart
  ActualFragment.java    # Expense validation, percentage text, chart
app/src/main/res/
  layout/                # Home and page layouts; original unused layouts retained
  drawable/              # Input/button backgrounds and launcher vectors
  values/                # Strings, colors, theme
  values-night/          # Original night theme
  mipmap-*/              # Launcher icons
  xml/                   # Backup templates
app/src/test/            # JVM source-fidelity verification
app/src/androidTest/     # Android numerical, state, validation, and chart tests
gradle/                  # Version catalog and Gradle wrapper
doc/                     # Original screenshot/video references
docs/                    # Specifications and verification evidence
tools/                   # Baseline verification utility
```

Each fragment inflates its own XML and binds views against that root. TextWatchers cache input strings. Button submission checks missing values, leading zeros, then parsing, in that order. On success, the fragment calculates with Java doubles, updates exact result strings, creates fresh chart entries, and reveals the outputs. This preserves the original structure and prevents cross-page view bindings or chart entry accumulation.

Pinned tooling: Gradle 8.6, Android Gradle Plugin 8.4.0, compile/target SDK 34, min SDK 28, Java 8 source/bytecode compatibility. Libraries: FlycoTabLayout 3.0.0, AppCompat 1.7.0, Material 1.12.0, MPAndroidChart v3.1.0; tests use JUnit 4.13.2, AndroidX test JUnit 1.2.0, and Espresso 3.6.0.

## Verification

```sh
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
python3 tools/verify_archive.py
```

Instrumentation requires a booted emulator or connected Android device. Seven Android tests pass on API 28 and API 34. They exercise all nine Estimated numerical fixtures, all five Actual fixtures, raw chart entries and colors, exact Toasts and validation precedence, validation visibility, parser-failure stale output, live page independence, editing without submission, repeat calculation, native tab taps/swipes, portrait orientation, and four reference-state captures. Injected parser cases use reflection against the cached strings so the number keyboard does not mask the error branches. Two E-03 rounding expectations use observed Android formatting; the discrepancy is recorded in the verification report.

Reports are generated under `app/build/reports/tests/`, `app/build/reports/androidTests/connected/`, and `app/build/reports/lint-results-debug.html`. Screenshots generated by instrumentation are stored on the device under the application's external files directory, `verification/`.

See [verification results](docs/verification/RESULTS.md) for executed checks, device configurations, screenshots, inherited warnings, and remaining acceptance limitations. A successful empty test task is not treated as evidence of functional coverage.
