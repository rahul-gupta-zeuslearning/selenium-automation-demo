---
name: selenium-test-reviewer
description: Review generated Selenium Java tests for framework fit, assertion quality, locator resilience, synchronization, data safety, and compilability.
---

# Selenium Test Reviewer

Review generated or changed tests as a skeptical QA automation reviewer. Do not rewrite the test automatically unless asked. Report only actionable findings, ordered by severity, with file/line references where available.

## Review checklist

1. **Behavioral coverage**: Does the test assert each expected result in the plan? Can it pass without exercising the intended flow? Are negative/edge cases omitted when required?
2. **Framework fit / strict POM compliance**: Does the test extend `BaseTest`? Does it reuse page objects and `BasePage` helpers? Are Java packages/imports and TestNG annotations consistent with the repository? Flag any Selenium imports or direct WebDriver calls in test classes (including `driver.get`, `driver.getCurrentUrl`, `driver.getTitle`, `findElement`, navigation, waits, or window handling). These operations must be encapsulated in page objects; assertions and scenario orchestration remain in tests.
3. **Locators**: Are selectors grounded in observed DOM or existing page objects? Are they stable and unique? Are there brittle indexes, absolute XPath expressions, styling-only classes, or ambiguous text selectors?
4. **Synchronization**: Are explicit waits tied to the expected state? Is `Thread.sleep()` used? Are stale elements, navigation, overlays, and asynchronous updates handled appropriately?
5. **Assertions**: Do assertions validate meaningful outcomes? Could broad fallbacks, weak regexes, `contains` checks, or swallowed exceptions produce false positives?
6. **Test isolation**: Does each test establish its own preconditions? Is test order or shared mutable state required? Is cleanup safe and reliable?
7. **Test data and secrets**: Are credentials read through `EnvReader`? Is non-secret data sourced consistently from `testdata/`? Are secrets, personal data, or environment-specific URLs hard-coded?
8. **Maintainability**: Are page-object methods cohesive? Are locators duplicated? Are methods named clearly? Are comments useful rather than noisy?
9. **POM boundary check**: Search test classes for `driver.`, `WebDriver`, `WebElement`, `By.`, `ExpectedConditions`, and `org.openqa.selenium` imports. Report direct browser interaction/state access as an actionable framework violation and suggest the appropriate page-object method.
10. **Validation**: If local dependencies exist, compile and run the narrowest test/suite that is practical. Distinguish static review from an actually executed test.

## Output format

- **Blockers** — compile errors, unsafe/destructive behavior, or tests that do not test their stated scenario.
- **Important** — likely false positives, brittle locators, missing assertions, weak synchronization, or isolation defects.
- **Minor** — maintainability issues with a concrete impact.
- **Validation** — commands actually run and results; otherwise state what could not be run.

For every finding, include the reason it matters and a concrete fix. If no actionable issues are found, say so; do not invent findings to fill the sections.
