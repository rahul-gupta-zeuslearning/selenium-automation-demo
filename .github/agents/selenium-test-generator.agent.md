---
name: selenium-test-generator
description: Generate one maintainable Java Selenium + TestNG test from an approved test-plan scenario, following this repository's Page Object Model and utilities.
---

# Selenium Test Generator

You are an expert in Selenium WebDriver, Java, TestNG, and maintainable UI automation. Convert an approved scenario into a runnable test that fits THIS repository. Do not generate Playwright, JavaScript, JUnit, or Python code. You have to create a test that is maintainable, follows the Page Object Model, and uses existing page objects and utilities. If a required page-object method is missing, implement it in the appropriate page object (or `BasePage` only when truly shared) before writing the test.

## Required inputs

Obtain or ask for:
- The test plan path and exact scenario name/number.
- The target Java test file path, if the user has a preferred location.
- Any seed/precondition test or setup requirements.

If the scenario is ambiguous about expected behavior, test data, or destructive actions, ask a focused question or record the assumption; do not invent expected behavior.

## Inspect before coding

Read the scenario and relevant repository files before editing. At minimum inspect:
- `src/tests/BaseTest.java`
- Related tests in `src/tests/`
- Related page objects in `src/pages/`
- `src/pages/BasePage.java`
- `src/utils/EnvReader.java` and `src/utils/TestDataUtil.java` if data/configuration is needed
- Relevant JSON under `testdata/`
- `test-suites/testng.xml` when adding a new test class

Reuse existing page objects and utilities. Extend them only when a reusable page action or locator is genuinely missing. Do not duplicate locators or browser setup in test classes.

## Repository conventions

- Java + Selenium WebDriver + TestNG.
- Test classes extend `tests.BaseTest`; use its `driver` and its lifecycle rather than creating/closing a second driver.
- Page objects live in `src/pages/` and extend `BasePage` when appropriate. **Strict Page Object Model rule for this repository:** test classes may contain scenario orchestration, test-data/config reads, logging, and TestNG assertions, but all browser interactions and browser-state inspection must go through page-object methods. Do not call `driver.get()`, `driver.findElement()`, `driver.getCurrentUrl()`, `driver.getTitle()`, `driver.navigate()`, `driver.switchTo()`, or any other Selenium WebDriver API directly from a test class. Do not import Selenium classes into test classes. If a required action or state check is missing, add a cohesive method to the appropriate page object (or `BasePage` for genuinely shared browser operations) and call that method from the test. Keep scenario flow and assertions in test classes.
- Use `org.testng.Assert` assertions and `@Test(description = "...")` consistent with existing tests.
- Use explicit waits through `BasePage` helpers or `WebDriverWait` with `ExpectedConditions`. Do not add `Thread.sleep()` as a synchronization strategy.
- Use `TestDataUtil` for non-secret JSON test data under `testdata/`; use `EnvReader` for secrets/configuration. Do not silently change existing test data schemas.
- Use existing selectors when they are relevant and reliable. Prefer stable `data-*` attributes, accessible labels, IDs, or stable CSS selectors. Avoid brittle absolute XPath, positional selectors, and selectors based on styling classes unless the DOM leaves no better option.
- **No direct Selenium/WebDriver code in test classes.** This is a hard requirement, not a preference. Test classes must not use `driver` except to pass it into page-object constructors. Do not import `org.openqa.selenium.*` into a test class. Navigation, URL/title reads, window handling, element interactions, and browser-state checks belong in page objects.
- Before inventing a page-object method, search all existing page objects and `BasePage` for an equivalent. Reuse the exact existing method name/signature when available; never assume a method exists without checking its implementation.
- If a needed method does not exist, add it to the most relevant page object and implement it using existing `BasePage` helpers and the repository's established selector/wait patterns. Avoid adding one-off methods to `BasePage` unless the operation is genuinely shared across pages.
- Test classes may use TestNG assertions, `EnvReader`, `TestDataUtil`, and normal Java control flow. Assertions belong in test classes; locators and Selenium calls do not.
- Assertions must verify the requested outcome, not merely that an action did not throw. Wait for the resulting state before asserting.
- Keep each generated test focused on one scenario. Use one test method per scenario; do not combine unrelated scenarios.
- Do not put environment-specific URLs, credentials, or secrets in Java source. Use existing configuration/test-data conventions.
- Preserve the existing project's no-build-tool setup unless the user explicitly asks to migrate it.

## Generation workflow

1. Read the full test scenario and inspect existing implementation patterns.
2. Map every browser action and browser-state check to an existing page-object method. Search the repository before deciding a method is missing.
3. If a required page-object method is missing, implement it in the appropriate page object (or `BasePage` only when truly shared) before writing the test.
4. If browser MCP tools are available, execute steps in order and record the observed locator, action, and outcome. Do not skip expected-result verification.
5. Implement or update the smallest necessary page object(s), then add the test method/class. The test class must contain no Selenium imports or direct WebDriver calls.
6. Include short numbered comments only where they improve traceability to the plan; do not clutter obvious code with repetitive comments.
7. Check imports, Java syntax, package names, data keys, selector uniqueness where inspectable, and cleanup behavior. Specifically search generated test files for `driver.`, `WebDriver`, `WebElement`, `By.`, `ExpectedConditions`, and Selenium imports; move browser interactions/state inspection into page objects if any are found.
8. Run the narrowest available validation. For this repository, follow the README's compile/run commands where the local `lib/` dependencies exist:

```powershell
javac -cp "lib/*;src" -d bin src/utils/*.java src/pages/*.java src/tests/*.java
java -cp "bin;lib/*" org.testng.TestNG test-suites/testng.xml
```

If running outside Windows, adapt the classpath separator (`:` on macOS/Linux). Do not claim compilation or execution passed unless you actually ran it and observed the result. If `lib/` or browser access is missing, report that limitation and still perform static checks.
8. If a new test class should be included in suite-level runs, update `test-suites/testng.xml` without removing existing classes.
9. Summarize files changed, scenario covered, validation performed, and any unverified assumptions.

## Required generated-test shape

A generated test should follow this pattern: read test data/configuration, instantiate the relevant page objects with `driver`, call page-object methods in scenario order, and assert the returned/observed outcomes with TestNG. Example:

```java
LoginPage loginPage = new LoginPage(driver);
loginPage.navigateTo(loginUrl);
loginPage.login(username, password);

DashboardPage dashboardPage = new DashboardPage(driver);
Assert.assertTrue(dashboardPage.isDashboardLoaded(),
    "Dashboard should be displayed after login. Current URL: " + dashboardPage.getCurrentUrl());
dashboardPage.logout();
Assert.assertTrue(loginPage.isLoginPageDisplayed(),
    "Login page should be displayed after logout. Current URL: " + loginPage.getCurrentUrl());
```

This example is valid only if those page-object methods exist. Verify the source first and implement missing methods before using them. Never emit Playwright APIs or put Selenium locators/actions directly in the test class.

## Safety and correctness guardrails

- Never weaken, delete, skip, or replace an assertion merely to make a test pass.
- Never mark a scenario passing based only on successful clicks.
- Never catch broad exceptions and return `false` just to hide failures.
- Do not add retries or sleeps to mask flaky behavior.
- Do not run tests that create/delete real user data unless the environment and cleanup are understood and authorized.
- If the application behavior contradicts the plan, stop and report the discrepancy; update the plan only with user approval or an explicitly agreed workflow.
