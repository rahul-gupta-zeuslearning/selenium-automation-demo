# Repository Architecture & Framework Reference

This document is the single source of truth for the Selenium + TestNG automation framework's structure, existing classes, method signatures, and coding standards we follow in this repository.

## 1. Directory Structure

```text
├── .env                          # Local secret credentials (gitignored)
├── .env.example                  # Template for sensitive credentials (PASSWORD)
├── .gitignore                    # Excludes lib/, .vscode/, bin/, .env, and test-output/
├── README.md                     # Setup and compile/run documentation
├── architecture.md               # Repository architecture, class inventory, and POM conventions
├── lib/                          # Local dependency JARs (Selenium, TestNG)
├── test-suites/
│   └── testng.xml                # TestNG XML suite runner configuration
├── testdata/
│   └── LoginLogoutTestData.json  # JSON test data files (URLs, usernames, roles, scenario data)
└── src/
    ├── pages/                    # Page Object Model (POM) classes
    │   ├── BasePage.java         # Reusable WebDriver actions, navigation, & explicit waits
    │   ├── DashboardPage.java    # Dashboard locators & interactions
    │   └── LoginPage.java        # Login page locators & interactions
    ├── tests/                    # TestNG test classes
    │   ├── BaseTest.java         # Browser setup (@BeforeMethod) & quit (@AfterMethod)
    │   └── LoginLogoutTest.java  # End-to-end login and logout test
    └── utils/                    # Framework utilities
        ├── EnvReader.java        # Loads .env secrets with fallback to System env/properties
        └── TestDataUtil.java     # Reads JSON test data using Selenium's built-in parser
```

## 2. Repository Conventions & POM Rules

- **Tech stack**: Java + Selenium WebDriver + TestNG. Preserve the existing project's no-build-tool setup (`lib/` JARs) unless the user explicitly asks to migrate it.
- **Driver lifecycle**: Test classes extend `tests.BaseTest`; use its `driver` and its lifecycle (`@BeforeMethod` / `@AfterMethod`) rather than creating or closing a second driver.
- **Strict Page Object Model rule for this repository**:
  - Page objects live in `src/pages/` and extend `BasePage` when appropriate.
  - Test classes may contain scenario orchestration, test-data/config reads (`TestDataUtil`, `EnvReader`), logging (`System.out.println`), normal Java control flow, and TestNG assertions (`org.testng.Assert`), but **all browser interactions and browser-state inspection must go through page-object methods**.
  - **No direct Selenium/WebDriver code in test classes.** This is a hard requirement, not a preference. Test classes must not use `driver` except to pass it into page-object constructors. Do not call `driver.get()`, `driver.findElement()`, `driver.getCurrentUrl()`, `driver.getTitle()`, `driver.navigate()`, `driver.switchTo()`, `driver.manage()`, or any other Selenium WebDriver API directly from a test class. Do not import `org.openqa.selenium.*` into a test class. Navigation, URL/title reads, window handling, element interactions, and browser-state checks belong in page objects.
  - If a required action or state check is missing, add a cohesive method to the appropriate page object (or `BasePage` only when the operation is genuinely shared across pages) and call that method from the test. Avoid adding one-off methods to `BasePage`.
- **Method reuse & discovery**:
  - Before inventing a page-object method, search all existing page objects and `BasePage` (see Section 3 below) for an equivalent. Reuse the exact existing method name/signature when available; never assume a method exists without checking its implementation.
  - If a needed method does not exist, add it to the most relevant page object and implement it using existing `BasePage` helpers and the repository's established selector/wait patterns.
- **Test focus & assertions**:
  - Keep each generated test focused on one scenario. Use one `@Test(description = "...")` method per scenario; do not combine unrelated scenarios.
  - Use `org.testng.Assert` assertions consistent with existing tests. Assertions belong in test classes; locators and Selenium calls do not.
  - Assertions must verify the requested outcome, not merely that an action did not throw. Wait for the resulting state before asserting.
- **Selector & locator strategy**:
  - Use existing selectors when they are relevant and reliable. Prefer stable `data-*` attributes (`data-automation-id`), `query-id`, accessible labels, IDs, or stable CSS selectors. Avoid brittle absolute XPath, positional selectors, and selectors based on styling classes unless the DOM leaves no better option.
  - **Static locators**: Declare at the top of each page class as `private final By <elementName> = By.cssSelector(...)`.
  - **Dynamic / parameterized locators**: When a locator depends on runtime test data (such as a class name `"XXXX"` or a student name in a table), encapsulate it as a `private By` helper method inside the page object:
    ```java
    private By classActionDropdown(String className) {
        return By.xpath(String.format("//*[contains(text(),'%s')]/ancestor::*[...]//button", className));
    }
    ```
- **Synchronization & explicit waits**:
  - Use explicit waits through `BasePage` helpers (`waitForVisibility`, `click`, `sendKeys`, `getText`, `isDisplayed`, `findElements`) or `WebDriverWait` with `ExpectedConditions`. Do not add `Thread.sleep()` as a synchronization strategy.
  - When interacting with transient UI elements (like toast banners), wait for the toast to be visible to assert its text, and ensure any lingering toast overlay does not intercept subsequent clicks (such as opening `userMenuDropdown` to log out).
- **Test data & configuration**:
  - Do not put environment-specific URLs, credentials, or secrets in Java source.
  - Use `TestDataUtil` for non-secret JSON test data under `testdata/`; use `EnvReader` for secrets/configuration (`.env`). Do not silently break existing test data schemas.Also Each test class should have a dedicated JSON test-data file under `testdata/<ScenarioName>TestData.json` (e.g., `testdata/ClassRosterEnrollmentTestData.json`) for scenario-specific data. 

## 3. Existing Class & Method Inventory

Always check this inventory and reuse existing methods before writing new ones.

### `src/pages/BasePage.java`
Base class for all Page Objects (`DEFAULT_TIMEOUT = 10` seconds).
- `protected WebElement waitForVisibility(By locator)`
- `protected void click(By locator)`
- `protected void sendKeys(By locator, String text)`
- `protected String getText(By locator)`
- `protected boolean isDisplayed(By locator)`
- `protected List<WebElement> findElements(By locator)`
- `public void navigateTo(String url)` — Navigates to the given URL (`driver.get(url)`).
- `public void resetSessionAndNavigate(String url)` — Clears cookies (`driver.manage().deleteAllCookies()`) and navigates to `url`.
- `public String getPageTitle()` — Returns `driver.getTitle()`.
- `public String getCurrentUrl()` — Returns `driver.getCurrentUrl()`.

### `src/pages/LoginPage.java` (extends `BasePage`)
- `public void enterUsername(String username)`
- `public void enterPassword(String password)`
- `public void clickLogin()`
- `public void login(String username, String password)` — Enters credentials and clicks Login.
- `public boolean isLoginPageDisplayed()` — Checks visibility of `[data-automation-id='login-username']`.

### `src/pages/DashboardPage.java` (extends `BasePage`)
- `public boolean isDashboardLoaded()` — Waits up to 60s for `[data-automation-id='lib-ilp-my-classes-header']`.
- `public String getDashboardHeaderText()`
- `public void openUserMenu()`
- `public void clickLogout()`
- `public void logout()` — Opens user menu dropdown and clicks logout link.

### `src/tests/BaseTest.java`
Base class for all TestNG test classes.
- `protected WebDriver driver`
- `@BeforeMethod public void setUp()` — Initializes `new ChromeDriver()` and maximizes window.
- `@AfterMethod(alwaysRun = true) public void tearDown()` — Quits `driver` if non-null.

### `src/utils/EnvReader.java`
- `public static String get(String key)` — Reads from `.env` &rarr; `System.getenv(key)` &rarr; `System.getProperty(key)`.
- `public static String get(String key, String defaultValue)`

### `src/utils/TestDataUtil.java`
- `public static Map<String, Object> loadJsonData(String fileName)`
- `public static Object getJsonValue(String fileName, String keyPath)`
- `public static String getJsonString(String fileName, String keyPath)` — Supports nested keys (e.g., `"roles.teacher"`).
- `public static String getJsonString(String fileName, String keyPath, String defaultValue)`

## 4. Build & Execution Commands

This repository uses local JARs in `lib/` without Maven or Gradle:

```powershell
# Windows (PowerShell / CMD)
javac -cp "lib/*;src" -d bin src/utils/*.java src/pages/*.java src/tests/*.java
java -cp "bin;lib/*" org.testng.TestNG test-suites/testng.xml

# macOS / Linux
javac -cp "lib/*:src" -d bin src/utils/*.java src/pages/*.java src/tests/*.java
java -cp "bin:lib/*" org.testng.TestNG test-suites/testng.xml
```
