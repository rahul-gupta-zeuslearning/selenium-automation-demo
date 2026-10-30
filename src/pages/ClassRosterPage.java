package pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ClassRosterPage extends BasePage {

    private final By classActionButton = By.cssSelector(
        "[data-automation-id='section-options-lh-drop-menu-button']");
    private final By manageClassRosterMenuItem = By.xpath(
        ".//*[@role='menuitem' and starts-with(normalize-space(.), 'Manage Class Roster')]");
    private final By rosterTab = By.cssSelector("a[href$='/manage-class-rosters']");
    private final By loadingBackdrop = By.cssSelector(".spinner-with-backdrop");
    private final By editStudentRosterLink = By.cssSelector(
        "a[href$='/manage-class-rosters/class/student/manage']");
    private final By noResultsMessage = By.xpath(
        "//*[normalize-space(.)='No results found.']");
    private final By studentRosterRows = By.xpath("//table//tr[td]");
    private final By schoolRosterSearchCandidates = By.cssSelector(
        "input, textarea, [contenteditable='true'], [role='textbox']");
    private final By searchButton = By.xpath(
        "//*[@role='search']//button[@aria-label='Search']");
    private final By schoolStudentRosterListbox = By.xpath(
        "//*[@role='listbox' and @aria-label='School Student Roster']");
    private final By classStudentRosterListbox = By.xpath(
        "//*[@role='listbox' and @aria-label='Class Student Roster']");
    private final By addStudentsButton = By.cssSelector(
        "[data-automation-id='add-students-to-roster-btn']");
    private final By saveButton = By.cssSelector(
        "[data-automation-id='save-school-roster-btn']");

    public ClassRosterPage(WebDriver driver) {
        super(driver);
    }

    public void openClassStudentRoster(String className) {
        By classCard = classCard(className);
        wait.until(driver -> !driver.findElements(classCard).isEmpty());
        List<WebElement> cards = driver.findElements(classCard);
        if (cards.size() != 1) {
            throw new IllegalStateException(
                "Expected one class card named '" + className + "', but found " + cards.size());
        }

        wait.until(driver -> {
            List<WebElement> currentCards = driver.findElements(classCard);
            return currentCards.size() == 1
                && currentCards.get(0).findElements(classActionButton).size() == 1;
        });
        cards = driver.findElements(classCard);
        List<WebElement> actionButtons = cards.get(0).findElements(classActionButton);
        if (actionButtons.size() != 1) {
            throw new IllegalStateException(
                "Expected one Classroom Actions button for '" + className
                    + "', but found " + actionButtons.size());
        }

        wait.until(ExpectedConditions.elementToBeClickable(actionButtons.get(0))).click();
        WebElement manageRosterMenuItem = wait.until(currentDriver -> {
            List<WebElement> currentCards = currentDriver.findElements(classCard);
            if (currentCards.size() != 1) {
                return null;
            }

            List<WebElement> menuItems =
                currentCards.get(0).findElements(manageClassRosterMenuItem);
            return menuItems.size() == 1 && menuItems.get(0).isDisplayed()
                ? menuItems.get(0) : null;
        });
        wait.until(ExpectedConditions.elementToBeClickable(manageRosterMenuItem)).click();
        waitForVisibility(rosterTab);
    }

    public void openRosterTab() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingBackdrop));
        click(rosterTab);
        wait.until(ExpectedConditions.urlContains("/manage-class-rosters"));
    }

    public void openEditStudentRoster() {
        click(editStudentRosterLink);
        wait.until(ExpectedConditions.urlContains("/class/student/manage"));
        waitForSchoolRosterSearch();
        waitForVisibility(schoolStudentRosterListbox);
        waitForClassStudentRosterReady();
    }

    public boolean isSavedClassRosterEmpty() {
        wait.until(driver -> driver.findElements(noResultsMessage).stream()
            .anyMatch(WebElement::isDisplayed));
        return driver.findElements(studentRosterRows).stream()
            .noneMatch(WebElement::isDisplayed);
    }

    public void searchSchoolRoster(String studentName) {
        WebElement search = waitForSchoolRosterSearch();
        search.clear();
        search.sendKeys(studentName);
        click(searchButton);
    }

    private WebElement waitForSchoolRosterSearch() {
        return wait.until(currentDriver -> currentDriver
            .findElements(schoolRosterSearchCandidates)
            .stream()
            .filter(WebElement::isDisplayed)
            .filter(element -> "textbox".equals(element.getAriaRole()))
            .filter(element -> "Search Student Roster".equals(element.getAccessibleName()))
            .findFirst()
            .orElse(null));
    }

    public int getSchoolStudentOptionCount(String studentName) {
        By option = schoolStudentOption(studentName);
        wait.until(driver -> !driver.findElements(option).isEmpty());
        return driver.findElements(option).size();
    }

    public boolean isSchoolStudentOptionEnabledAndUnselected(String studentName) {
        List<WebElement> options = driver.findElements(schoolStudentOption(studentName));
        if (options.size() != 1) {
            return false;
        }

        WebElement option = options.get(0);
        return option.isDisplayed()
            && option.isEnabled()
            && "false".equals(option.getAttribute("aria-disabled"))
            && "false".equals(option.getAttribute("aria-selected"));
    }

    public void selectSchoolStudent(String studentName) {
        List<WebElement> options = driver.findElements(schoolStudentOption(studentName));
        if (options.size() != 1) {
            throw new IllegalStateException(
                "Expected exactly one School Student Roster option for '" + studentName
                    + "', but found " + options.size());
        }

        WebElement option = options.get(0);
        if (!option.isEnabled()
                || !"false".equals(option.getAttribute("aria-disabled"))
                || !"false".equals(option.getAttribute("aria-selected"))) {
            throw new IllegalStateException(
                "School Student Roster option is not enabled and unselected: " + studentName);
        }

        option.click();
        wait.until(driver -> {
            List<WebElement> selectedOptions =
                driver.findElements(schoolStudentOption(studentName));
            return selectedOptions.size() == 1
                && "true".equals(selectedOptions.get(0).getAttribute("aria-selected"));
        });
    }

    public List<String> getSelectedSchoolStudentNames() {
        return driver.findElements(By.xpath(
                "//*[@role='listbox' and @aria-label='School Student Roster']"
                    + "//*[@role='option' and @aria-selected='true']"))
            .stream()
            .filter(WebElement::isDisplayed)
            .map(element -> element.getText().trim())
            .collect(Collectors.toList());
    }

    public String getAddStudentsButtonText() {
        return getText(addStudentsButton).trim();
    }

    public void clickAddStudents() {
        clickAfterScrollingIntoView(addStudentsButton);
    }

    public List<String> getClassStudentRosterNames() {
        waitForClassStudentRosterReady();
        return driver.findElements(classStudentRosterOptions()).stream()
            .filter(WebElement::isDisplayed)
            .map(element -> element.getText().trim())
            .collect(Collectors.toList());
    }

    private void waitForClassStudentRosterReady() {
        // The empty state shows "0 Students" without rendering the Class Student Roster listbox.
        By emptyRosterCount = exactText("0 Students");
        wait.until(currentDriver ->
            currentDriver.findElements(classStudentRosterListbox).stream()
                .anyMatch(WebElement::isDisplayed)
                || currentDriver.findElements(emptyRosterCount).stream()
                    .anyMatch(WebElement::isDisplayed));
    }

    public void waitForClassStudentRosterNames(List<String> expectedNames) {
        wait.until(ExpectedConditions.refreshed(driver -> {
            List<String> currentNames = getClassStudentRosterNames();
            return currentNames.size() == expectedNames.size()
                && new HashSet<>(currentNames).equals(new HashSet<>(expectedNames));
        }));
    }

    public List<String> getSelectedClassStudentRosterNames() {
        return driver.findElements(By.xpath(
                "//*[@role='listbox' and @aria-label='Class Student Roster']"
                    + "//*[@role='option' and @aria-selected='true']"))
            .stream()
            .filter(WebElement::isDisplayed)
            .map(element -> element.getText().trim())
            .collect(Collectors.toList());
    }

    public boolean isClassStudentRosterCountDisplayed(String countText) {
        return waitForVisibility(exactText(countText)).isDisplayed();
    }

    public boolean isSaveButtonEnabled() {
        WebElement button = waitForVisibility(saveButton);
        return button.isEnabled();
    }

    public void selectClassStudentRosterNames(List<String> studentNames) {
        List<WebElement> optionsToSelect = new ArrayList<>();
        for (String studentName : studentNames) {
            List<WebElement> matchingOptions =
                driver.findElements(classStudentRosterOption(studentName));
            if (matchingOptions.size() != 1) {
                throw new IllegalStateException(
                    "Expected exactly one Class Student Roster option for '" + studentName
                        + "', but found " + matchingOptions.size());
            }
            optionsToSelect.add(matchingOptions.get(0));
        }

        for (WebElement option : optionsToSelect) {
            if (!"true".equals(option.getAttribute("aria-selected"))) {
                option.click();
            }
        }

        wait.until(driver -> getSelectedClassStudentRosterNames().containsAll(studentNames));
    }

    public String getRemoveStudentsButtonText(int selectedStudentCount) {
        return getText(removeStudentsButton(selectedStudentCount)).trim();
    }

    public void clickRemoveStudents(int selectedStudentCount) {
        click(removeStudentsButton(selectedStudentCount));
    }

    public void clickSave() {
        clickSaveButton();
        wait.until(this::isSavedRosterPage);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingBackdrop));
    }

    public boolean clickSaveAndObserveNotification(String message) {
        clickSaveButton();
        By notification = exactText(message);
        String notificationOutcome = wait.until(driver -> {
            if (driver.findElements(notification).stream().anyMatch(WebElement::isDisplayed)) {
                return "visible";
            }
            return isSavedRosterPage(driver) ? "not-visible" : null;
        });
        wait.until(this::isSavedRosterPage);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingBackdrop));
        return "visible".equals(notificationOutcome);
    }

    private void clickSaveButton() {
        clickAfterScrollingIntoView(saveButton);
    }

    private void clickAfterScrollingIntoView(By locator) {
        WebElement button = waitForVisibility(locator);
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", button);
        click(locator);
    }

    public boolean isSavedStudentResultCountDisplayed(int expectedCount) {
        return waitForVisibility(exactText("Displaying " + expectedCount + " results"))
            .isDisplayed();
    }

    public int getSavedStudentRowCount(String studentName) {
        String[] nameParts = studentName.trim().split("\\s+");
        if (nameParts.length != 2) {
            throw new IllegalArgumentException(
                "Expected a first and last name for the saved roster row: " + studentName);
        }
        wait.until(driver -> driver.findElements(studentRosterRows).stream()
            .anyMatch(row -> isSavedStudentRow(row, nameParts)));
        return (int) driver.findElements(studentRosterRows).stream()
            .filter(WebElement::isDisplayed)
            .filter(row -> isSavedStudentRow(row, nameParts))
            .count();
    }

    private boolean isSavedRosterPage(WebDriver currentDriver) {
        String currentUrl = currentDriver.getCurrentUrl();
        return currentUrl.contains("/manage-class-rosters")
            && !currentUrl.contains("/class/student/manage");
    }

    private By classCard(String className) {
        return By.xpath("//*[@data-automation-id='ilp-section-card'][.//*[normalize-space(.)="
            + xpathLiteral(className) + "]]");
    }

    private By schoolStudentOption(String studentName) {
        return By.xpath("//*[@role='listbox' and @aria-label='School Student Roster']"
            + "//*[@role='option' and normalize-space(.)=" + xpathLiteral(studentName) + "]");
    }

    private By classStudentRosterOptions() {
        return By.xpath("//*[@role='listbox' and @aria-label='Class Student Roster']"
            + "//*[@role='option']");
    }

    private By classStudentRosterOption(String studentName) {
        return By.xpath("//*[@role='listbox' and @aria-label='Class Student Roster']"
            + "//*[@role='option' and normalize-space(.)=" + xpathLiteral(studentName) + "]");
    }

    private boolean isSavedStudentRow(WebElement row, String[] nameParts) {
        if (!row.isDisplayed()) {
            return false;
        }

        List<String> rowTokens = Arrays.asList(normalizeWhitespace(row.getText())
            .toLowerCase(Locale.ROOT)
            .split("[^\\p{L}\\p{N}]+"));
        return Arrays.stream(nameParts)
            .map(namePart -> namePart.toLowerCase(Locale.ROOT))
            .allMatch(rowTokens::contains);
    }

    private String normalizeWhitespace(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }

    private By removeStudentsButton(int selectedStudentCount) {
        String noun = selectedStudentCount == 1 ? "Student" : "Students";
        return By.xpath("//button[normalize-space(.)='Remove " + selectedStudentCount
            + " " + noun + "']");
    }

    private By exactText(String text) {
        return By.xpath("//*[normalize-space(.)=" + xpathLiteral(text) + "]");
    }

    private String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }

        String[] parts = value.split("'");
        StringBuilder literal = new StringBuilder("concat(");
        for (int index = 0; index < parts.length; index++) {
            if (index > 0) {
                literal.append(", \"'\", ");
            }
            literal.append("'").append(parts[index]).append("'");
        }
        return literal.append(")").toString();
    }
}
