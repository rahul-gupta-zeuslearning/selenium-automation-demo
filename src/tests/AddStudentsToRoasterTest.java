package tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import pages.ClassRosterPage;
import pages.DashboardPage;
import pages.LoginPage;
import utils.EnvReader;
import utils.TestDataUtil;

public class AddStudentsToRoasterTest extends BaseTest {

    private String loginUrl;
    private String username;
    private String password;
    private String className;
    private String firstStudent;
    private String secondStudent;
    private String successMessage;
    private boolean emptyBaselineConfirmed;

    @Test(description = "Add two students to the class roster and restore the original roster")
    public void addStudentsToRoaster() {
        String testDataFile = "AddStudentsToRoasterTestData";
        loginUrl = TestDataUtil.getJsonString(testDataFile, "loginUrl");
        username = TestDataUtil.getJsonString(testDataFile, "username");
        className = TestDataUtil.getJsonString(testDataFile, "className");
        firstStudent = TestDataUtil.getJsonString(testDataFile, "students.first");
        secondStudent = TestDataUtil.getJsonString(testDataFile, "students.second");
        successMessage = TestDataUtil.getJsonString(testDataFile, "successMessage");
        password = EnvReader.get("PASSWORD");

        Assert.assertNotNull(password, "Password not found! Ensure PASSWORD is set in .env or environment.");
        Assert.assertFalse(password.trim().isEmpty(), "PASSWORD cannot be empty.");

        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(loginUrl);
        loginPage.login(username, password);

        DashboardPage dashboardPage = new DashboardPage(driver);
        Assert.assertTrue(dashboardPage.isDashboardLoaded(),
            "My Programs should be displayed after login. Current URL: "
                + dashboardPage.getCurrentUrl());

        ClassRosterPage rosterPage = new ClassRosterPage(driver);
        rosterPage.openClassStudentRoster(className);
        rosterPage.openRosterTab();
        Assert.assertTrue(rosterPage.isSavedClassRosterEmpty(),
            "Sample class must have an empty student roster before any changes.");
        emptyBaselineConfirmed = true;
        rosterPage.openEditStudentRoster();

        assertEligibleStudent(rosterPage, firstStudent);
        rosterPage.selectSchoolStudent(firstStudent);
        Assert.assertEquals(rosterPage.getSelectedSchoolStudentNames(),
            List.of(firstStudent), "Rahul should be the only selected school-roster result.");
        Assert.assertEquals(rosterPage.getAddStudentsButtonText(), "Add 1 Student");
        rosterPage.clickAddStudents();
        Assert.assertTrue(rosterPage.isClassStudentRosterCountDisplayed("1 Student"),
            "The staged class roster should show 1 Student after adding Rahul.");
        rosterPage.waitForClassStudentRosterNames(List.of(firstStudent));
        Assert.assertEquals(rosterPage.getClassStudentRosterNames(),
            List.of(firstStudent), "Rahul should be the only staged class-roster student.");

        assertEligibleStudent(rosterPage, secondStudent);
        rosterPage.selectSchoolStudent(secondStudent);
        Assert.assertEquals(rosterPage.getSelectedSchoolStudentNames(),
            List.of(secondStudent), "Sairaj should be the only selected school-roster result.");
        Assert.assertEquals(rosterPage.getAddStudentsButtonText(), "Add 1 Student");
        rosterPage.clickAddStudents();
        Assert.assertTrue(rosterPage.isClassStudentRosterCountDisplayed("2 Students"),
            "The staged class roster should show 2 Students after adding Sairaj.");

        rosterPage.waitForClassStudentRosterNames(Arrays.asList(firstStudent, secondStudent));
        List<String> stagedStudentNames = rosterPage.getClassStudentRosterNames();
        Assert.assertEquals(stagedStudentNames.size(), 2,
            "Exactly two students should be staged before saving.");
        Assert.assertEquals(new HashSet<>(stagedStudentNames),
            new HashSet<>(Arrays.asList(firstStudent, secondStudent)),
            "Only Rahul and Sairaj should be staged before saving.");
        Assert.assertEquals(rosterPage.getAddStudentsButtonText(), "Add 0 Students",
            "No additional school-roster students should remain selected for adding.");
        Assert.assertTrue(rosterPage.isSaveButtonEnabled(),
            "Save should be available after staging the two confirmed students.");

        boolean successNotificationObserved =
            rosterPage.clickSaveAndObserveNotification(successMessage);
        Assert.assertTrue(rosterPage.isSavedStudentResultCountDisplayed(2),
            "The saved class roster should report exactly two results.");
        Assert.assertEquals(rosterPage.getSavedStudentRowCount(firstStudent), 1,
            "Rahul should appear in exactly one saved roster row.");
        Assert.assertEquals(rosterPage.getSavedStudentRowCount(secondStudent), 1,
            "Sairaj should appear in exactly one saved roster row.");
        Assert.assertTrue(successNotificationObserved,
            "Expected success notification: " + successMessage);
    }

    @AfterMethod(alwaysRun = true)
    public void restoreOnlyTestStudentsAndLogOut() {
        if (loginUrl == null || loginUrl.trim().isEmpty()) {
            return;
        }

        LoginPage loginPage = new LoginPage(driver);
        if (!emptyBaselineConfirmed) {
            loginPage.resetSessionAndNavigate(loginUrl);
            Assert.assertTrue(loginPage.isLoginPageDisplayed(),
                "The browser should be returned to the logged-out login page.");
            return;
        }

        DashboardPage dashboardPage = new DashboardPage(driver);
        ClassRosterPage rosterPage = new ClassRosterPage(driver);
        boolean authenticated = false;

        try {
            loginPage.resetSessionAndNavigate(loginUrl);
            loginPage.login(username, password);

            boolean dashboardLoaded = dashboardPage.isDashboardLoaded();
            if (dashboardLoaded) {
                authenticated = true;
            }
            Assert.assertTrue(dashboardLoaded,
                "Cleanup login should reach My Programs. Current URL: "
                    + dashboardPage.getCurrentUrl());

            rosterPage.openClassStudentRoster(className);
            rosterPage.openRosterTab();
            rosterPage.openEditStudentRoster();

            List<String> rosterBeforeCleanup = rosterPage.getClassStudentRosterNames();
            List<String> targetStudents = Arrays.asList(firstStudent, secondStudent);
            List<String> targetsPresent = new ArrayList<>();
            for (String target : targetStudents) {
                long occurrenceCount = rosterBeforeCleanup.stream()
                    .filter(target::equals)
                    .count();
                Assert.assertTrue(occurrenceCount <= 1,
                    "Cleanup stopped because the target appears more than once: " + target);
                if (occurrenceCount == 1) {
                    targetsPresent.add(target);
                }
            }

            rosterPage.selectClassStudentRosterNames(targetsPresent);
            List<String> selectedForRemoval = rosterPage.getSelectedClassStudentRosterNames();
            Assert.assertEquals(new HashSet<>(selectedForRemoval),
                new HashSet<>(targetsPresent),
                "Cleanup may select only Rahul and Sairaj.");
            Assert.assertEquals(selectedForRemoval.size(), targetsPresent.size(),
                "Cleanup must not select duplicate or unrelated students.");

            List<String> expectedRemaining = new ArrayList<>();
            for (String student : rosterBeforeCleanup) {
                if (!targetsPresent.contains(student)) {
                    expectedRemaining.add(student);
                }
            }
            if (!targetsPresent.isEmpty()) {
                String expectedRemoveLabel = "Remove " + targetsPresent.size() + " "
                    + (targetsPresent.size() == 1 ? "Student" : "Students");
                Assert.assertEquals(
                    rosterPage.getRemoveStudentsButtonText(targetsPresent.size()),
                    expectedRemoveLabel,
                    "The remove control must identify exactly the selected test students.");
                rosterPage.clickRemoveStudents(targetsPresent.size());
                rosterPage.waitForClassStudentRosterNames(expectedRemaining);
                List<String> rosterAfterRemoval = rosterPage.getClassStudentRosterNames();
                Assert.assertEquals(rosterAfterRemoval.size(), expectedRemaining.size(),
                    "Cleanup should preserve the existing number of non-target students.");
                Assert.assertEquals(new HashSet<>(rosterAfterRemoval),
                    new HashSet<>(expectedRemaining),
                    "Cleanup must preserve every non-target student.");
            }

            String expectedCount = expectedRemaining.size() == 1
                ? "1 Student" : expectedRemaining.size() + " Students";
            Assert.assertTrue(rosterPage.isClassStudentRosterCountDisplayed(expectedCount),
                "The editor should show the remaining roster count before saving.");
            rosterPage.clickSave();
            Assert.assertTrue(rosterPage.isSavedClassRosterEmpty(),
                "Cleanup must restore the original empty class roster.");
        } finally {
            if (authenticated) {
                dashboardPage.logout();
                Assert.assertTrue(loginPage.isLoginPageDisplayed()
                        || loginPage.getCurrentUrl().contains("login"),
                    "Cleanup should sign out and return to the login page. Current URL: "
                        + loginPage.getCurrentUrl());
            } else if (!authenticated) {
                loginPage.resetSessionAndNavigate(loginUrl);
                Assert.assertTrue(loginPage.isLoginPageDisplayed(),
                    "Cleanup should leave the browser on the logged-out login page.");
            }
        }
    }

    private void assertEligibleStudent(ClassRosterPage rosterPage, String studentName) {
        rosterPage.searchSchoolRoster(studentName);
        Assert.assertEquals(rosterPage.getSchoolStudentOptionCount(studentName), 1,
            "Search should return exactly one option for " + studentName + ".");
        Assert.assertTrue(rosterPage.isSchoolStudentOptionEnabledAndUnselected(studentName),
            studentName + " should be enabled and unselected before adding.");
    }
}
