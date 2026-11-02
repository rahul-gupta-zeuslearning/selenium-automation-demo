package pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ClassRosterPage extends BasePage {

    private static final By BODY = By.tagName("body");
    private static final By LINKS = By.tagName("a");
    private static final By ACTIVE_STUDENTS_LINK =
        By.cssSelector("a[data-automation-id='roster-tabs-student'][aria-current='page']");
    private static final By BUTTONS = By.tagName("button");
    private static final By LISTBOXES = By.cssSelector("[role='listbox']");
    private static final By OPTIONS = By.cssSelector("[role='option']");
    private static final By SEARCH_INPUTS = By.cssSelector("input");
    private static final By MENU_ITEMS = By.cssSelector("[role='menuitem']");
    private static final By SAVE_BUTTON =
        By.cssSelector("button[data-automation-id='save-school-roster-btn']");
    private static final By LOADING_BACKDROP = By.cssSelector(".spinner-with-backdrop");
    private static final int STATE_TIMEOUT_SECONDS = 10;
    private static final int LOADING_TIMEOUT_SECONDS = 30;
    private static final int ROSTER_UPDATE_TIMEOUT_SECONDS = 30;

    public ClassRosterPage(WebDriver driver) {
        super(driver);
    }

    public void openStudentsRoster() {
        List<WebElement> activeStudentsLinks = driver.findElements(ACTIVE_STUDENTS_LINK);
        if (!activeStudentsLinks.isEmpty()) {
            waitForLoadingBackdropToDisappear();
            waitForRosterListLoaded();
            return;
        }

        waitForLoadingBackdropToDisappear();
        WebElement studentsLink = findByAccessibleName(LINKS, "Students", true);
        wait.until(ExpectedConditions.elementToBeClickable(studentsLink)).click();
        waitForRosterListLoaded();
    }

    public void openEditor() {
        findByAccessibleName(LINKS, "Edit Student Roster", true).click();
        waitForBodyText("School Student Roster");
    }

    public boolean isRosterEmpty() {
        try {
            return stateWait(ROSTER_UPDATE_TIMEOUT_SECONDS).until(currentDriver -> {
                String bodyText = currentDriver.findElement(BODY).getText();
                return !bodyText.contains("School Student Roster")
                    && !bodyText.contains("Class Student Roster")
                    && bodyText.contains("No results found.");
            });
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isClassRosterEmpty() {
        return waitForCondition(() -> {
            String text = driver.findElement(BODY).getText();
            return text.contains("Class Student Roster")
                && text.contains("0 Students")
                && classRosterOptions().isEmpty();
        });
    }

    public void searchForStudent(String studentName) {
        WebElement searchInput = findByAccessibleName(
            SEARCH_INPUTS, "Search Student Roster", true);
        searchInput.clear();
        searchInput.sendKeys(studentName);
        findByAccessibleName(BUTTONS, "Search", true).click();
    }

    public void selectSchoolStudent(String studentName) {
        List<WebElement> matchingOptions = waitForOptions(studentName);
        if (matchingOptions.size() != 1) {
            throw new IllegalStateException(
                "Expected one School Student Roster result for '" + studentName
                    + "' but found " + matchingOptions.size());
        }
        matchingOptions.get(0).click();
    }

    public void addOneStudent() {
        WebElement addButton = findByAccessibleName(BUTTONS, "Add 1 Student", true);
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", addButton);
        wait.until(ExpectedConditions.elementToBeClickable(addButton)).click();
    }

    public boolean classRosterContainsExactly(String... studentNames) {
        return waitForCondition(() -> {
            List<String> actualNames = classRosterOptions();
            if (actualNames.size() != studentNames.length) {
                return false;
            }
            for (String expectedName : studentNames) {
                if (!actualNames.contains(expectedName)) {
                    return false;
                }
            }
            String bodyText = driver.findElement(BODY).getText();
            String expectedCount = studentNames.length == 1
                ? "1 Student"
                : studentNames.length + " Students";
            return bodyText.contains("Class Student Roster")
                && bodyText.contains(expectedCount);
        });
    }

    public void saveChanges() {
        WebElement searchInput = findByAccessibleName(
            SEARCH_INPUTS, "Search Student Roster", true);
        searchInput.clear();
        searchInput.sendKeys(Keys.ESCAPE, Keys.TAB);
        WebElement saveButton = wait.until(
            ExpectedConditions.elementToBeClickable(SAVE_BUTTON));
        saveButton.sendKeys(Keys.ENTER);
    }

    public boolean savedRosterContainsExactly(String firstStudent, String secondStudent) {
        return stateWait(ROSTER_UPDATE_TIMEOUT_SECONDS).until(currentDriver -> {
            String bodyText = driver.findElement(BODY).getText().replaceAll("\\s+", " ");
            return bodyText.contains("Displaying 2 results")
                && bodyText.contains(rosterRowHeader(firstStudent))
                && bodyText.contains(rosterRowHeader(secondStudent));
        });
    }

    private String rosterRowHeader(String studentName) {
        String[] nameParts = studentName.trim().split("\\s+");
        if (nameParts.length < 2) {
            throw new IllegalArgumentException(
                "Expected first and last name in roster test data: " + studentName);
        }
        return nameParts[nameParts.length - 1] + ", " + nameParts[0];
    }

    public boolean isSuccessMessageDisplayed(String message) {
        try {
            return waitForCondition(() -> driver.findElement(BODY).getText().contains(message));
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void removeTargetStudentsIfPresent(String... studentNames) {
        List<String> presentNames = classRosterOptions();
        List<String> targetsToRemove = new ArrayList<>();
        for (String studentName : studentNames) {
            if (presentNames.contains(studentName)) {
                targetsToRemove.add(studentName);
            }
        }

        if (targetsToRemove.isEmpty()) {
            if (!isClassRosterEmpty()) {
                throw new IllegalStateException(
                    "The target students were not found, but the class roster is not empty. "
                        + "Refusing to remove unrelated students.");
            }
            return;
        }

        for (String studentName : targetsToRemove) {
            findOptionInClassRoster(studentName).click();
        }

        String removeButtonPrefix = "Remove " + targetsToRemove.size();
        findByAccessibleNameStartingWith(BUTTONS, removeButtonPrefix).click();
        if (!isClassRosterEmpty()) {
            throw new IllegalStateException(
                "The roster did not become empty after removing only the specified students.");
        }
    }

    public void openUserMenuAndLogout() {
        waitForLoadingBackdropToDisappear();
        findByAccessibleName(BUTTONS, "Rishan Teacher Account", false).click();
        findByAccessibleName(MENU_ITEMS, "Log out", false).click();
    }

    private List<WebElement> waitForOptions(String studentName) {
        return stateWait().until(currentDriver -> {
                List<WebElement> matches = new ArrayList<>();
                for (WebElement option : currentDriver.findElements(OPTIONS)) {
                    if (option.isDisplayed()
                        && studentName.equals(option.getAccessibleName())) {
                        matches.add(option);
                    }
                }
                return matches.isEmpty() ? null : matches;
            });
    }

    private WebElement findOptionInClassRoster(String studentName) {
        return stateWait().until(currentDriver -> {
                WebElement listbox = findClassRosterListbox();
                if (listbox == null) {
                    return null;
                }
                for (WebElement option : listbox.findElements(OPTIONS)) {
                    if (option.isDisplayed()
                        && studentName.equals(option.getAccessibleName())) {
                        return option;
                    }
                }
                return null;
            });
    }

    private List<String> classRosterOptions() {
        WebElement listbox = findClassRosterListbox();
        if (listbox == null) {
            return new ArrayList<>();
        }

        List<String> names = new ArrayList<>();
        for (WebElement option : listbox.findElements(OPTIONS)) {
            if (option.isDisplayed()) {
                names.add(option.getAccessibleName());
            }
        }
        return names;
    }

    private WebElement findClassRosterListbox() {
        for (WebElement listbox : driver.findElements(LISTBOXES)) {
            if (listbox.isDisplayed()
                && listbox.getAccessibleName().contains("Class Student Roster")) {
                return listbox;
            }
        }
        return null;
    }

    private WebElement findByAccessibleName(By locator, String name, boolean exact) {
        return stateWait().until(currentDriver -> {
                for (WebElement element : currentDriver.findElements(locator)) {
                    if (!element.isDisplayed()) {
                        continue;
                    }
                    String accessibleName = element.getAccessibleName();
                    if (exact ? name.equals(accessibleName) : accessibleName.contains(name)) {
                        return element;
                    }
                }
                return null;
            });
    }

    private WebElement findByAccessibleNameStartingWith(By locator, String prefix) {
        return stateWait().until(currentDriver -> {
                for (WebElement element : currentDriver.findElements(locator)) {
                    if (element.isDisplayed()
                        && element.getAccessibleName().startsWith(prefix)) {
                        return element;
                    }
                }
                return null;
            });
    }

    private boolean waitForBodyText(String expectedText) {
        return waitForCondition(() -> driver.findElement(BODY).getText().contains(expectedText));
    }

    private void waitForLoadingBackdropToDisappear() {
        new WebDriverWait(driver, Duration.ofSeconds(LOADING_TIMEOUT_SECONDS))
            .until(ExpectedConditions.invisibilityOfElementLocated(LOADING_BACKDROP));
    }

    private void waitForRosterListLoaded() {
        new WebDriverWait(driver, Duration.ofSeconds(LOADING_TIMEOUT_SECONDS))
            .until(currentDriver -> {
                String bodyText = currentDriver.findElement(BODY).getText();
                return bodyText.contains("No results found.")
                    || bodyText.contains("Displaying ");
            });
    }

    private boolean waitForCondition(java.util.function.BooleanSupplier condition) {
        return stateWait()
            .until(currentDriver -> condition.getAsBoolean());
    }

    private WebDriverWait stateWait() {
        return stateWait(STATE_TIMEOUT_SECONDS);
    }

    private WebDriverWait stateWait(int timeoutSeconds) {
        WebDriverWait stateWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        stateWait.ignoring(StaleElementReferenceException.class);
        return stateWait;
    }
}
