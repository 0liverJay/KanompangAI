# Ultimate Budgeter — Technical Specification

## 1. Reconstruction target

Create a single-module native Android project using Java source, XML layouts, AndroidX fragments, Flyco SlidingTabLayout, and MPAndroidChart. Read [Product](01_PRODUCT_REQUIREMENTS.md), [Design](02_DESIGN_SPECIFICATION.md), and [Testing](04_TEST_SPECIFICATION.md) first. This is a specification extracted from repository source; it does not claim the supplied project was built or executed during documentation.

No backend, API contract, database schema, secret, environment token, remote service, or dependency injection framework is needed. All business data is held in memory. Build-time dependency downloads are distinct from the offline runtime requirement.

## 2. Build configuration

| Setting | Value |
|---|---|
| Gradle wrapper | 8.6, binary distribution |
| Android Gradle Plugin | 8.4.0 |
| Build scripts | Root and app Kotlin DSL; settings uses Groovy |
| Java source / target compatibility | Java 8 |
| Recommended build JVM | JDK 17 for this AGP generation; bytecode target remains Java 8 |
| compileSdk / targetSdk / minSdk | 34 / 34 / 28 |
| Namespace / applicationId | com.github.noey.ultimate.unidramer |
| versionCode / versionName | 1 / 1.0 |
| Root name / module | unidramer / :app |
| Release minification | Disabled |
| Instrumentation runner | androidx.test.runner.AndroidJUnitRunner |
| JVM args | -Xmx2048m -Dfile.encoding=UTF-8 |
| AndroidX / nontransitive R | Both enabled |

Pinned dependencies:

| Purpose | Maven coordinate |
|---|---|
| Tabs | io.github.h07000223:flycoTabLayout:3.0.0 |
| AppCompat | androidx.appcompat:appcompat:1.7.0 |
| Material theme | com.google.android.material:material:1.12.0 |
| Chart | com.github.PhilJay:MPAndroidChart:v3.1.0 |
| Unit tests | junit:junit:4.13.2 |
| Android test JUnit | androidx.test.ext:junit:1.2.0 |
| Espresso | androidx.test.espresso:espresso-core:3.6.0 |

Use a version catalog at `gradle/libs.versions.toml`. The original app lists Flyco twice; a single declaration is sufficient because duplication adds no feature. Plugin repositories are JitPack (`https://www.jitpack.io`), Aliyun public (`https://maven.aliyun.com/repository/public`), Google, Maven Central, and Gradle Plugin Portal. Dependency repositories use the same list without Gradle Plugin Portal. Set `RepositoriesMode.FAIL_ON_PROJECT_REPOS`. MPAndroidChart requires its pinned artifact to resolve; do not silently upgrade it when an environment cannot download dependencies.

Keep a standard Gradle wrapper including scripts and wrapper JAR. A fresh machine needs Android SDK platform 34 and a working JDK/SDK path; `local.properties` is machine-specific and should not contain a copied absolute path from another machine. No custom signing key is provided. Debug uses normal debug signing; release publishing/signing is outside scope.

## 3. Suggested file map matching the source

```text
settings.gradle
build.gradle.kts
gradle.properties
gradlew / gradlew.bat
gradle/wrapper/{gradle-wrapper.properties,gradle-wrapper.jar}
gradle/libs.versions.toml
app/build.gradle.kts
app/proguard-rules.pro
app/src/main/AndroidManifest.xml
app/src/main/java/com/github/microkibaco/ultimate/life/
  HomeActivity.java
  UltimateAdapter.java
  EstimatedFragment.java
  ActualFragment.java
app/src/main/res/layout/
  activity_home.xml
  pie_item_estimated.xml
  pie_item_actual.xml
app/src/main/res/drawable/
  edit_text_bg.xml
  custom_dialog_background.xml
  ic_launcher_background.xml
  ic_launcher_foreground.xml
app/src/main/res/values/{strings.xml,colors.xml,themes.xml}
app/src/main/res/values-night/themes.xml
app/src/main/res/xml/{backup_rules.xml,data_extraction_rules.xml}
app/src/main/res/mipmap-*/ic_launcher*.webp
app/src/main/res/mipmap-anydpi/{ic_launcher.xml,ic_launcher_round.xml}
```

The Java package `com.github.noey.ultimate.life` differs from the generated resource namespace; Java imports must use `com.github.noey.ultimate.unidramer.R`. The original also contains unused `pie_item_home.xml`, `income_input_view.xml`, and an empty `values/refs.xml`. They are not reachable product screens and are not required to reproduce runtime behavior.

## 4. Manifest and system integration

Application label `@string/app_name` is `unidramer`; theme `@style/Theme.Unidramer`; icons `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`; `supportsRtl=true`; `allowBackup=true`; backup resources point to the two XML files above. The supplied backup templates have no active custom include/exclude rules. There are no declared permissions or additional activities/services/receivers/providers in the app manifest.

Declare exported launcher activity `com.github.noey.ultimate.life.HomeActivity`, locked to portrait, with MAIN action and LAUNCHER category. Do not add an INTERNET permission or runtime permissions. Theme settings and icon details are in Design section 7.

## 5. Responsibilities and navigation

### HomeActivity

Extend `FragmentActivity`. In `onCreate`, inflate `activity_home`, find `market_tab` and `view_pager`, create UltimateAdapter with the support fragment manager, add EstimatedFragment titled `Estimated`, then ActualFragment titled `Actual`, and attach it to the ViewPager. Bind SlidingTabLayout using those two titles. Iterate titles to set black text and `Paint.FAKE_BOLD_TEXT_FLAG`; both titles are bold despite the tab layout's selected-bold setting. The first page is initially selected. A final adapter notification exists in the original but is not an additional user action.

### UltimateAdapter

Extend `FragmentPagerAdapter` with `BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT`. Maintain parallel ordered fragment/title lists. Implement `getItem`, `getCount`, `getPageTitle`, and `addFragment`. The default adjacent-page behavior retains the two live pages during ordinary navigation. No navigation graph or secondary destination exists.

### EstimatedFragment

Inflate `pie_item_estimated`; cache the income string through a TextWatcher in `afterTextChanged`. Bind five output TextViews and PieChart and initially set them all to GONE. The button listener performs ordered validation, parses income as double, computes five amounts, updates all texts and chart data, then shows all outputs.

Original public helpers are `calculateExpenditures(View view, double totalIncome)` returning `[education, housing, food, transport, balance]`, and `calculateSpending(double totalIncome, double percentage)` returning the product. The former also updates TextViews; it is not a pure function. A rebuilt calculator may extract pure helpers for meaningful unit tests, provided displayed behavior remains identical.

### ActualFragment

Inflate `pie_item_actual`; keep four independent cached strings via four TextWatchers. Field mapping is transport `et_income`, education `et_income2`, housing `et_income3`, food `et_income4`. Bind four output TextViews and PieChart, all initially GONE. Validate, parse each string, calculate textual shares, bind raw amounts to the chart, and reveal all outputs.

The original public helper `calculateExpenditures(double educationPercentage, double housingPercentage, double foodPercentage, double transportPercentage)` has misleading parameter names: the arguments are raw amounts. It updates text and returns `[educationAmount, housingAmount, foodAmount, transportAmount]`. Never treat these parameters as precomputed percentages.

## 6. Calculation and validation algorithm

Estimated pseudocode:

```text
if cachedIncome == null or cachedIncome.isEmpty():
    toast("Numbers cannot be null."); hideEstimatedOutputs(); return
if cachedIncome.startsWith("0"):
    toast("Numbers cannot start with 0."); hideEstimatedOutputs(); return
try: I = Double.parseDouble(cachedIncome)
catch NumberFormatException:
    toast("Invalid number format."); return  // leave previous output untouched
if I < 50000: E=.075; H=.325; F=.175; T=.125
else if I <= 100000: E=.125; H=.275; F=.125; T=.125
else: E=.175; H=.225; F=.075; T=.125
B = 1 - E - H - F - T
amounts = [I*E, I*H, I*F, I*T, I*B]
set five exact text templates using String.format("%.2f", amount)
replace chart using amounts; show all estimated outputs
```

Actual pseudocode:

```text
if any cached field == null or any cached field.isEmpty():
    toast("content is empty"); hideActualOutputs(); return
if any cached field.startsWith("0"):
    toast("Numbers cannot start with 0."); hideActualOutputs(); return
try: parse transport, education, housing, food with Double.parseDouble
catch NumberFormatException:
    toast("Invalid number format."); return  // leave previous output untouched
sum = education + housing + food + transport
set transport, education, housing, food result strings in display order
    each share = amount / sum * 100
    formatted with String.format("%.2f", share)
replace chart with [education, housing, food, transport]; show outputs
```

Use Java double arithmetic, not integer division. Preserve default-locale String.format behavior and float conversion only at chart entry creation. Do not round values before multiplying or summing. No configurable rate table is exposed to users.

## 7. Chart implementation contract

For each result, create a fresh `List<PieEntry>` in Education, Housing, Food, Transport, optional Balance order. Each entry has `(float) rawAmount` and its exact category label. Create `PieDataSet(entries, "Expenditures")`, configure the five ordered RGB colors from Design section 6, create PieData, and call `pieChart.setData(data)`.

Set description enabled, tap highlight enabled, description text `Monthly Expenditures`, text size `12f`, color `Color.DKGRAY`, position `(900, 750)`. Call invalidate, animateY(1400), and invalidate again, matching the original sequence. All unspecified chart settings use MPAndroidChart v3.1.0 defaults. Chart values are monetary/expenditure amounts; Actual's percentage text is separate.

## 8. Resource IDs and bindings

| Layout | IDs and purpose |
|---|---|
| activity_home | tv_hello_world heading; market_tab tabs; view_pager pages |
| pie_item_estimated | income1 label; et_income1 input; btn_plan_budget action; rl_list result group; text_1 through text_5 outputs; pie_chart |
| pie_item_actual | income, income2, income3, income4 labels; et_income through et_income4 inputs; query_btn action; rl_list result group; text_1 through text_4 outputs; pie_chart |

Repeated IDs across separate page layouts are intentional; bind against each fragment's inflated root, not a global activity search. Layout dimensions and exact text are in the design document.

## 9. State, storage, security, and limitations

Cached input strings start null and are updated only by TextWatchers. There is no ViewModel, saved-instance-state result model, repository, preferences, files written by business logic, database, analytics, or network client. Android backup is enabled but there is no implemented budget persistence to back up.

Do not claim explicit clearing of the keyboard, secure screenshot blocking, encryption, or guaranteed process restoration. The app's numeric keyboard narrows ordinary input but is not a complete validator. Negative/non-finite injected numbers and overflow remain unguarded. Number-format failure leaves a previous result visible, and the chart description's fixed pixel position may not fit the view. Night theme and RTL declarations do not establish visual quality in those modes.

## 10. Build and verification procedure

On a machine with the required JDK and SDK:

```sh
./gradlew --version
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

The final command requires a connected emulator/device and implemented instrumentation tests. The supplied repository has test dependencies but no test source files; the testing document specifies tests for the reconstruction, not tests already passing in the original.

Expected debug APK location: `app/build/outputs/apk/debug/app-debug.apk`. Install it on API 28 and API 34 devices, perform both workflows, and capture the four reference states after animation completion. Record network dependency issues separately from application defects.

Suggested implementation sequence: establish build/manifest/theme; implement home navigation; construct both XML pages; implement input tracking and ordered validation; implement allocation and share calculations; configure charts; add tests; build and visually verify. Complete all mandatory acceptance tests before declaring reconstruction finished.

## 11. Optional improvements — excluded by default

A later improvement mode may introduce pure calculator models, explicit finite-positive validation, locale-stable formatting, saved state, responsive scrolling, accessible labels, larger touch targets, corrected dark-mode colors, modern paging, and responsive description positioning. Internal pure helpers can be used immediately if they do not change behavior. Every observable improvement must be reported as a deviation rather than silently folded into fidelity acceptance.
