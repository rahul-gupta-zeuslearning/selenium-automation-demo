package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ClassRosterPage extends BasePage {

    private final By successToast = By.xpath("//*[normalize-space()='Student Roster was successfully updated!']");
    public ClassRosterPage(WebDriver driver) {
        super(driver);
    }

    public void openManageRosterForClass(String className) {
        click(classActionDropdown(className));
        click(actionLocator("Manage Class Roster"));
    }

    public void openRosterTab() {
        click(actionLocator("Roster"));
    }

    public void openEditStudentRoster() {
        click(actionLocator("Edit Student Roster"));
    }

    public void searchSchoolRoster(String studentName) {
        sendKeys(searchInput("School Student Roster"), studentName);
    }

    public void selectSchoolStudent(String studentName) {
        click(studentCheckbox("School Student Roster", studentName));
    }

    public void addOneStudent() {
        click(actionLocator("Add 1 Student"));
    }

    public void saveRoster() {
        click(actionLocator("Save"));
    }

    public boolean isSuccessToastDisplayed() {
        return isDisplayed(successToast);
    }

    public boolean isStudentListedInClassRoster(String studentName) {
        waitForClassRosterLoaded();
        return !driver.findElements(classRosterStudentRow(studentName)).isEmpty();
    }

    public void waitForStudentListedInClassRoster(String studentName) {
        waitForClassRosterLoaded();
        wait.until(ExpectedConditions.visibilityOfElementLocated(classRosterStudentRow(studentName)));
    }

    public void removeStudentFromClassRoster(String studentName) {
        click(studentCheckbox("Class Student Roster", studentName));
        click(actionLocator("Remove Students"));
    }

    public boolean isClassRosterEmpty() {
        waitForClassRosterLoaded();
        return driver.findElements(classRosterStudentRows()).isEmpty()
            && isDisplayed(emptyClassRosterText());
    }

    private By classActionDropdown(String className) {
        return By.xpath("//*[normalize-space()='" + className
            + "']/ancestor::*[.//button][1]//button");
    }

    private By studentCheckbox(String rosterTitle, String studentName) {
        return By.xpath(rosterContainer(rosterTitle) + "//*[self::tr or @role='row']"
            + "[.//*[normalize-space()='" + studentName + "']]//input[@type='checkbox']");
    }

    private By classRosterStudentRow(String studentName) {
        return By.xpath(rosterContainer("Class Student Roster") + "//*[self::tr or @role='row']"
            + "[.//*[normalize-space()='" + studentName + "']]");
    }

    private By classRosterStudentRows() {
        return By.xpath(rosterContainer("Class Student Roster") + "//*[self::tr or @role='row']"
            + "[.//input[@type='checkbox']]");
    }

    private By emptyClassRosterText() {
        return By.xpath(rosterContainer("Class Student Roster")
            + "//*[normalize-space()='0 students']");
    }

    private void waitForClassRosterLoaded() {
        waitForVisibility(By.xpath(rosterContainer("Class Student Roster")));
        wait.until(ignored -> !driver.findElements(classRosterStudentRows()).isEmpty()
            || driver.findElements(emptyClassRosterText()).stream().anyMatch(element -> element.isDisplayed()));
    }

    private By searchInput(String rosterTitle) {
        return By.xpath(rosterContainer(rosterTitle) + "//input");
    }

    private String rosterContainer(String rosterTitle) {
        return "//*[normalize-space()='" + rosterTitle
            + "']/ancestor::*[.//table or .//*[@role='grid'] or .//*[@role='row']][1]";
    }

    private By actionLocator(String text) {
        return By.xpath("//*[self::button or self::a or @role='button']"
            + "[normalize-space()='" + text + "']");
    }
}