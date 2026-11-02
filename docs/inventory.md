# Existing Class & Method Inventory

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
- `public boolean isLoggedOutAlertDisplayed()` — Verifies the observed `You have been logged out.` alert.

### `src/pages/DashboardPage.java` (extends `BasePage`)
- `public boolean isDashboardLoaded()` — Waits up to 60s for `[data-automation-id='lib-ilp-my-classes-header']`.
- `public String getDashboardHeaderText()`
- `public void openClassRoster(String className)` — Opens the named class's Classroom Actions menu and selects Manage Class Roster using observed accessible names.
- `public void openUserMenu()`
- `public void clickLogout()`
- `public void logout()` — Opens user menu dropdown and clicks logout link.

### `src/pages/ClassRosterPage.java` (extends `BasePage`)
- `public void openStudentsRoster()` and `public void openEditor()` — Navigate to the student roster and roster editor.
- `public boolean isRosterEmpty()` and `public boolean isClassRosterEmpty()` — Verify the live empty-roster states.
- `public void searchForStudent(String studentName)`, `public void selectSchoolStudent(String studentName)`, and `public void addOneStudent()` — Search and stage an exact student result.
- `public boolean classRosterContainsExactly(String... studentNames)` — Verifies the staged class-roster options and count.
- `public void saveChanges()` and `public boolean savedRosterContainsExactly(String firstStudent, String secondStudent)` — Clear and dismiss search suggestions before saving; allow up to 30 seconds for the saved roster to render, then verify the two observed visible roster labels (`Student, First`) and result count.
- `public boolean isSuccessMessageDisplayed(String message)` — Waits for the requested success notification.
- `public void removeTargetStudentsIfPresent(String... studentNames)` — Removes only the named students and refuses to remove unrelated roster entries.
- `public void openUserMenuAndLogout()` — Signs out through the observed account menu.

### `src/tests/AddStudentsToRoasterTest.java`
- `addStudentsToRosterAndRestoreOriginalState()` — Runs the planner-approved add/save/verify flow, asserts the specified success notification, restores the verified empty baseline, and signs out. Before starting, it may recover only a roster containing exactly the two named scenario students from an interrupted prior run; any other non-empty state is left untouched and fails safely.
- `cleanUpRosterAfterFailure()` — When the test confirmed the initial roster was empty but did not complete cleanup, re-authenticates and removes only the two scenario students.

### `src/tests/BaseTest.java`
Base class for all TestNG test classes.
- `protected WebDriver driver`
- `@BeforeMethod public void setUp()` — Initializes `new ChromeDriver()` and maximizes window.
- `@AfterMethod(alwaysRun = true) public void tearDown()` — Quits `driver` if non-null.

### `src/utils/EnvReader.java`
- `public static String get(String key)` — Reads from `.env` → `System.getenv(key)` → `System.getProperty(key)`.
- `public static String get(String key, String defaultValue)`

### `src/utils/TestDataUtil.java`
- `public static Map<String, Object> loadJsonData(String fileName)`
- `public static Object getJsonValue(String fileName, String keyPath)`
- `public static String getJsonString(String fileName, String keyPath)` — Supports nested keys (e.g., `"roles.teacher"`).
- `public static String getJsonString(String fileName, String keyPath, String defaultValue)`
