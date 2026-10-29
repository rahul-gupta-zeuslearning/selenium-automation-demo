package pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ClassRosterPage extends BasePage {

    private final By successToast = By.xpath("//*[normalize-space()='Student Roster was successfully updated!']");
    private final By loadingBackdrop = By.cssSelector(".spinner-with-backdrop");
    private final By searchSpinner = By.cssSelector("div.spinner-border.text-primary");
    private final By rosterNavigationLink = By.cssSelector("a[data-automation-id='manage-class-rosters']");
    private final By schoolRosterSearchButton = By.cssSelector("button[aria-label='Search']");
    private final By addStudentsButton = By.cssSelector("button[data-automation-id='add-students-to-roster-btn']");
    private final By removeStudentsButton = By.cssSelector("button[data-automation-id='remove-students-from-roster-btn']");

    public ClassRosterPage(WebDriver driver) {
        super(driver);
    }

    public void openManageRosterForClass(String className) {
        click(classActionDropdown(className));
        click(openDropdownMenuItem("Manage Class Roster"));
    }

    public void openRosterTab() {
        waitForLoadingToFinish();
        click(rosterNavigationLink);
        waitForLoadingToFinish();
    }

    public void openEditStudentRoster() {
        click(actionLocator("Edit Student Roster"));
    }

    public void searchSchoolRoster(String studentName) {
        sendKeys(searchInput("School Student Roster"), studentName);
        waitForSearchToFinish();
        click(schoolRosterSearchButton);
        waitForSearchToFinish();
    }

    public void selectSchoolStudent(String studentName) {
        click(schoolStudentOption(studentName));
    }

    public void addOneStudent() {
        waitForRosterControlsToFinishLoading();
        WebElement button = waitForVisibility(addStudentsButton);
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block: 'center', behavior: 'instant'});", button);
        waitForRosterControlsToFinishLoading();
        click(addStudentsButton);
    }

    public void saveRoster() {
        click(actionLocator("Save"));
    }

    public boolean isSuccessToastDisplayed() {
        return isDisplayed(successToast);
    }

    public boolean isStudentListedInClassRoster(String studentName) {
        waitForClassRosterLoaded();
        return !driver.findElements(classRosterStudentOption(studentName)).isEmpty();
    }

    public void waitForStudentListedInClassRoster(String studentName) {
        new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(20))
            .until(ExpectedConditions.visibilityOfElementLocated(classRosterStudentOption(studentName)));
    }

    public boolean waitForStudentListedInClassRosterIfPresent(String studentName) {
        try {
            waitForStudentListedInClassRoster(studentName);
            return true;
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    public void waitForStudentListedInRosterTable(String displayedName) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(rosterTableStudentRow(displayedName)));
    }

    public boolean isStudentListedInRosterTable(String displayedName) {
        return isDisplayed(rosterTableStudentRow(displayedName));
    }

    public void removeStudentFromClassRoster(String studentName) {
        click(classRosterStudentOption(studentName));
        waitForRosterControlsToFinishLoading();
        WebElement button = waitForVisibility(removeStudentsButton);
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block: 'center', behavior: 'instant'});", button);
        waitForRosterControlsToFinishLoading();
        click(removeStudentsButton);
    }

    public boolean isClassRosterEmpty() {
        waitForClassRosterLoaded();
        return driver.findElements(classRosterStudentOptions()).isEmpty()
            && isDisplayed(emptyClassRosterText());
    }

    private By classActionDropdown(String className) {
        return By.xpath("//*[normalize-space()='" + className
            + "']/ancestor::*[.//button][1]//button");
    }

    private By schoolStudentOption(String studentName) {
        return By.xpath("//ul[@role='listbox' and @aria-label='School Student Roster']"
            + "/li[@role='option'][.//*[normalize-space()='" + studentName + "']]");
    }

    private By classRosterStudentOption(String studentName) {
        return By.xpath(rosterContainer("Class Student Roster")
            + "//ul[@role='listbox' and @aria-label='Class Student Roster']"
            + "/li[@role='option'][.//*[normalize-space()='" + studentName + "']]");
    }

    private By classRosterStudentOptions() {
        return By.xpath(rosterContainer("Class Student Roster")
            + "//ul[@role='listbox' and @aria-label='Class Student Roster']/li[@role='option']");
    }

    private By rosterTableStudentRow(String displayedName) {
        return By.xpath("//table[.//caption[normalize-space()='Students Roster']]"
            + "//tbody/tr[.//th[@scope='row' and normalize-space()='" + displayedName + "']]");
    }

    private By emptyClassRosterText() {
        return By.xpath(rosterContainer("Class Student Roster")
            + "//*[normalize-space()='0 Students']");
    }

    private void waitForClassRosterLoaded() {
        waitForVisibility(By.xpath(rosterContainer("Class Student Roster")));
        wait.until(ignored -> !driver.findElements(classRosterStudentOptions()).isEmpty()
            || driver.findElements(emptyClassRosterText()).stream().anyMatch(element -> element.isDisplayed()));
    }

    private By searchInput(String rosterTitle) {
        return By.xpath(rosterContainer(rosterTitle) + "//input");
    }

    private String rosterContainer(String rosterTitle) {
        return "//*[self::h2 or self::h3][normalize-space()='" + rosterTitle
            + "']/ancestor::div[contains(concat(' ', normalize-space(@class), ' '), ' rounded ')][1]";
    }

    private By actionLocator(String text) {
        return By.xpath("//*[self::button or self::a or @role='button' or @role='menuitem']"
            + "[normalize-space(text()[1])='" + text + "' or normalize-space(.)='" + text + "']");
    }

    private By openDropdownMenuItem(String text) {
        return By.xpath("//*[contains(concat(' ', normalize-space(@class), ' '), ' dropdown-menu ')"
            + " and contains(concat(' ', normalize-space(@class), ' '), ' show ')]"
            + "//*[@role='menuitem' and normalize-space(text()[1])='" + text + "']");
    }

    private void waitForLoadingToFinish() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingBackdrop));
    }

    private void waitForSearchToFinish() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(searchSpinner));
    }

    private void waitForRosterControlsToFinishLoading() {
        waitForLoadingToFinish();
        waitForSearchToFinish();
    }
}