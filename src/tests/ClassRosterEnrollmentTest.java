/*
Agent used : selenium-test-generator
Prompt used : 
Test Scenario: Add student to class roster and verify enrollment
 
1. Login as teacher in 
2. From My Programs page, click on ClassRoom Action dropdown button for class "XXXX"
3. Select "Manage Class Roster" from dropdown menu
4. Select "Roster" from side navigation bar
5. Click on "Edit Student Roster"
6. In School Student Roster section, search for the student name
7. Select the student from search results
8. Click on "Add 1 Student" button
9. Click "Save" button
10. Verify "Student Roster was successfully updated!" toast banner appears
11. Verify the added student name is listed in the students table
12. Logout as teacher
 
Teardown:
 
1. Login as teacher
2. From My Programs page, click on ClassRoom Action dropdown button for class "XXXX"
3. Select "Manage Class Roster" from dropdown menu
4. Select "Roster" from side navigation bar
5. Click on "Edit Student Roster"
6. Select all students from Class Student Roster list
7. Click on "Remove Students" button
8. Verify "0 students" text appears in Class Student Roster section
9. Logout as teacher

above is the test flow we have automate usiing selenium. You should not use playwright mcp here. automate it using the selected generator

Credit used - 6.6
Time taken - 17 mins

Failure fixes - Manually provided the element structure of each failing element, from DevTools, to Copilot
Credit used 7.75
Time taken - 1 hr 

*/

package tests;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import pages.ClassRosterPage;
import pages.DashboardPage;
import pages.LoginPage;
import utils.EnvReader;
import utils.TestDataUtil;

public class ClassRosterEnrollmentTest extends BaseTest {

    private static final String TEST_DATA_FILE = "ClassRosterEnrollmentTestData";

    @Test(description = "Add a student to a class roster and verify enrollment")
    public void addStudentToClassRoster() {
        String loginUrl = TestDataUtil.getJsonString(TEST_DATA_FILE, "loginUrl");
        String username = TestDataUtil.getJsonString(TEST_DATA_FILE, "roles.teacher");
        String password = requirePassword();
        String className = TestDataUtil.getJsonString(TEST_DATA_FILE, "className");
        String studentName = TestDataUtil.getJsonString(TEST_DATA_FILE, "studentName");
        String rosterDisplayName = TestDataUtil.getJsonString(TEST_DATA_FILE, "rosterDisplayName");

        DashboardPage dashboardPage = loginTeacher(loginUrl, username, password);
        ClassRosterPage classRosterPage = new ClassRosterPage(driver);
        classRosterPage.openManageRosterForClass(className);
        classRosterPage.openRosterTab();
        classRosterPage.openEditStudentRoster();
        Assert.assertTrue(classRosterPage.isClassRosterEmpty(),
            "The class roster should be empty before this scenario adds a student.");
        Assert.assertFalse(classRosterPage.isStudentListedInClassRoster(studentName),
            "The student must not already be in the class roster before this test adds them.");
        classRosterPage.searchSchoolRoster(studentName);
        classRosterPage.selectSchoolStudent(studentName);
        classRosterPage.addOneStudent();
        classRosterPage.saveRoster();

        Assert.assertTrue(classRosterPage.isSuccessToastDisplayed(),
            "Student Roster was successfully updated! toast should appear after saving.");
        classRosterPage.waitForStudentListedInRosterTable(rosterDisplayName);
        Assert.assertTrue(classRosterPage.isStudentListedInRosterTable(rosterDisplayName),
            "The added student should be listed in the class roster.");

        dashboardPage.logout();
        LoginPage loginPage = new LoginPage(driver);
        Assert.assertTrue(loginPage.isLoginPageDisplayed(),
            "The teacher should be logged out after the enrollment flow.");
    }

    @AfterMethod(alwaysRun = true)
    public void removeAddedStudentFromClassRoster() {
        String loginUrl = TestDataUtil.getJsonString(TEST_DATA_FILE, "loginUrl");
        String username = TestDataUtil.getJsonString(TEST_DATA_FILE, "roles.teacher");
        String password = requirePassword();
        String className = TestDataUtil.getJsonString(TEST_DATA_FILE, "className");
        String studentName = TestDataUtil.getJsonString(TEST_DATA_FILE, "studentName");

        DashboardPage dashboardPage = loginTeacher(loginUrl, username, password);
        ClassRosterPage classRosterPage = new ClassRosterPage(driver);
        classRosterPage.openManageRosterForClass(className);
        classRosterPage.openRosterTab();
        classRosterPage.openEditStudentRoster();

        if (classRosterPage.waitForStudentListedInClassRosterIfPresent(studentName)) {
            classRosterPage.removeStudentFromClassRoster(studentName);
            classRosterPage.saveRoster();
            Assert.assertTrue(classRosterPage.isSuccessToastDisplayed(),
                "Roster cleanup should be saved successfully.");
        }
        dashboardPage.logout();
    }

    private DashboardPage loginTeacher(String loginUrl, String username, String password) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.resetSessionAndNavigate(loginUrl);
        loginPage.login(username, password);

        DashboardPage dashboardPage = new DashboardPage(driver);
        Assert.assertTrue(dashboardPage.isDashboardLoaded(),
            "My Programs should load after the teacher logs in. Current URL: "
                + dashboardPage.getCurrentUrl());
        return dashboardPage;
    }

    private String requirePassword() {
        String password = EnvReader.get("PASSWORD");
        Assert.assertNotNull(password, "PASSWORD must be configured in .env or the environment.");
        Assert.assertFalse(password.trim().isEmpty(), "PASSWORD cannot be empty.");
        return password;
    }
}