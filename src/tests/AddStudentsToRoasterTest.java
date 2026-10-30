package tests;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import pages.ClassRosterPage;
import pages.DashboardPage;
import pages.LoginPage;
import utils.EnvReader;
import utils.TestDataUtil;

public class AddStudentsToRoasterTest extends BaseTest {

    private static final String TEST_DATA_FILE = "AddStudentsToRoasterTestData";
    private boolean originalRosterConfirmedEmpty;
    private boolean cleanupCompleted;
    private String loginUrl;
    private String username;
    private String password;
    private String className;
    private String firstStudent;
    private String secondStudent;

    @Test(description = "Add two students to Sample class, verify the roster, restore its original state, and sign out")
    public void addStudentsToRosterAndRestoreOriginalState() {
        loginUrl = TestDataUtil.getJsonString(TEST_DATA_FILE, "loginUrl");
        username = TestDataUtil.getJsonString(TEST_DATA_FILE, "username");
        className = TestDataUtil.getJsonString(TEST_DATA_FILE, "className");
        firstStudent = TestDataUtil.getJsonString(TEST_DATA_FILE, "students.first");
        secondStudent = TestDataUtil.getJsonString(TEST_DATA_FILE, "students.second");
        String successMessage = TestDataUtil.getJsonString(TEST_DATA_FILE, "successMessage");
        password = EnvReader.get("PASSWORD");

        Assert.assertNotNull(loginUrl, "loginUrl is missing from the scenario test data.");
        Assert.assertNotNull(username, "username is missing from the scenario test data.");
        Assert.assertNotNull(className, "className is missing from the scenario test data.");
        Assert.assertNotNull(firstStudent, "students.first is missing from the scenario test data.");
        Assert.assertNotNull(secondStudent, "students.second is missing from the scenario test data.");
        Assert.assertNotNull(successMessage, "successMessage is missing from the scenario test data.");
        Assert.assertNotNull(password, "PASSWORD is missing from .env or the environment.");
        Assert.assertFalse(password.trim().isEmpty(), "PASSWORD cannot be empty.");

        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(loginUrl);
        loginPage.login(username, password);

        DashboardPage dashboardPage = new DashboardPage(driver);
        Assert.assertTrue(dashboardPage.isDashboardLoaded(),
            "My Programs did not load. Current URL: " + dashboardPage.getCurrentUrl());
        Assert.assertEquals(dashboardPage.getDashboardHeaderText(), "My Programs",
            "The My Programs page should be displayed after sign-in.");
        dashboardPage.openClassRoster(className);

        ClassRosterPage rosterPage = new ClassRosterPage(driver);
        rosterPage.openStudentsRoster();
        if (!rosterPage.isRosterEmpty()) {
            Assert.assertTrue(rosterPage.savedRosterContainsExactly(firstStudent, secondStudent),
                "Sample class is not empty and does not contain exactly the two known "
                    + "scenario students; refusing to modify the roster.");
            rosterPage.openEditor();
            rosterPage.removeTargetStudentsIfPresent(firstStudent, secondStudent);
            Assert.assertTrue(rosterPage.isClassRosterEmpty(),
                "Could not safely restore the empty baseline from the previous scenario run.");
            rosterPage.saveChanges();
            Assert.assertTrue(rosterPage.isRosterEmpty(),
                "Saving recovery cleanup did not restore the empty class roster.");
        }
        originalRosterConfirmedEmpty = true;

        rosterPage.openEditor();
        Assert.assertTrue(rosterPage.isClassRosterEmpty(),
            "The edit page must show 0 Students before adding the test students.");

        rosterPage.searchForStudent(firstStudent);
        rosterPage.selectSchoolStudent(firstStudent);
        rosterPage.addOneStudent();
        Assert.assertTrue(rosterPage.classRosterContainsExactly(firstStudent),
            "Class Student Roster should contain only " + firstStudent + " after the first add.");

        rosterPage.searchForStudent(secondStudent);
        rosterPage.selectSchoolStudent(secondStudent);
        rosterPage.addOneStudent();
        Assert.assertTrue(rosterPage.classRosterContainsExactly(firstStudent, secondStudent),
            "Class Student Roster should contain exactly the two selected students before saving.");

        rosterPage.saveChanges();
        Assert.assertTrue(rosterPage.isSuccessMessageDisplayed(successMessage),
            "The expected roster success notification was not displayed after saving.");
        Assert.assertTrue(rosterPage.savedRosterContainsExactly(firstStudent, secondStudent),
            "The saved roster should display exactly the two test students.");

        rosterPage.openEditor();
        rosterPage.removeTargetStudentsIfPresent(firstStudent, secondStudent);
        Assert.assertTrue(rosterPage.isClassRosterEmpty(),
            "The editor should show 0 Students after removing the two test students.");
        rosterPage.saveChanges();
        Assert.assertTrue(rosterPage.isRosterEmpty(),
            "The original empty roster should be restored after cleanup.");
        cleanupCompleted = true;

        rosterPage.openUserMenuAndLogout();
        boolean loginPageDisplayed = loginPage.isLoginPageDisplayed();
        String currentUrl = loginPage.getCurrentUrl();
        boolean loggedOutAlertDisplayed = loginPage.isLoggedOutAlertDisplayed();
        Assert.assertTrue(loginPageDisplayed
                && currentUrl.contains("logout=true")
                && loggedOutAlertDisplayed,
            "The login page and logged-out state should be displayed after sign-out. "
                + "Login page displayed: " + loginPageDisplayed
                + ", URL: " + currentUrl
                + ", logged-out alert displayed: " + loggedOutAlertDisplayed);
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUpRosterAfterFailure() {
        if (!originalRosterConfirmedEmpty || cleanupCompleted) {
            return;
        }

        LoginPage loginPage = new LoginPage(driver);
        loginPage.resetSessionAndNavigate(loginUrl);
        loginPage.login(username, password);

        DashboardPage dashboardPage = new DashboardPage(driver);
        if (!dashboardPage.isDashboardLoaded()) {
            throw new IllegalStateException(
                "Could not load My Programs to clean up the test roster.");
        }
        dashboardPage.openClassRoster(className);

        ClassRosterPage rosterPage = new ClassRosterPage(driver);
        rosterPage.openStudentsRoster();
        if (!rosterPage.isRosterEmpty()) {
            rosterPage.openEditor();
            rosterPage.removeTargetStudentsIfPresent(firstStudent, secondStudent);
            rosterPage.saveChanges();
            if (!rosterPage.isRosterEmpty()) {
                throw new IllegalStateException(
                    "Cleanup did not restore the originally empty class roster.");
            }
        }

        rosterPage.openUserMenuAndLogout();
        if (!loginPage.isLoginPageDisplayed()
            || !loginPage.getCurrentUrl().contains("/login")) {
            throw new IllegalStateException("Cleanup logout did not return to the login page.");
        }
        cleanupCompleted = true;
    }
}
