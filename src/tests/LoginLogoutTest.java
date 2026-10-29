package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.DashboardPage;
import pages.LoginPage;
import utils.EnvReader;
import utils.TestDataUtil;

public class LoginLogoutTest extends BaseTest {

    @Test(description = "Verify user can log into the portal, verify dashboard rendered, and log out successfully")
    public void testLoginLogout() {
        // 1. Load test data from JSON and credentials from .env
        String testDataFile = "LoginLogoutTestData";
        String loginUrl = TestDataUtil.getJsonString(testDataFile, "loginUrl");
        String username = TestDataUtil.getJsonString(testDataFile, "roles.teacher");
        String password = EnvReader.get("PASSWORD");

        Assert.assertNotNull(password, "Password not found! Ensure PASSWORD is set in the .env file.");
        Assert.assertFalse(password.trim().isEmpty(), "Password in .env cannot be empty.");

        // 2. Navigate to login portal via Page Object (no direct driver.get calls in test class)
        LoginPage loginPage = new LoginPage(driver);
        System.out.println("Navigating to: " + loginUrl);
        loginPage.navigateTo(loginUrl);

        // 3. Perform Login
        System.out.println("Submitting credentials for user: " + username);
        loginPage.login(username, password);

        // 4. Initialize Dashboard Page & Assert it is loaded
        DashboardPage dashboardPage = new DashboardPage(driver);
        System.out.println("Waiting up to 20 seconds for dashboard to render...");
        boolean isLoaded = dashboardPage.isDashboardLoaded();

        Assert.assertTrue(isLoaded,
            "Dashboard failed to load 'My Programs' header within 20s. Current URL: "
                + dashboardPage.getCurrentUrl() + " | Page Title: " + dashboardPage.getPageTitle());
        System.out.println("✅ Dashboard confirmed loaded! Header: " + dashboardPage.getDashboardHeaderText());

        // 5. Perform Logout
        System.out.println("Triggering logout...");
        dashboardPage.logout();

        // 6. Verify redirection back to login via Page Object helpers
        boolean isBackOnLogin = loginPage.isLoginPageDisplayed() || loginPage.getCurrentUrl().contains("login");
        Assert.assertTrue(isBackOnLogin,
            "Did not redirect to login page after logout. Current URL: " + loginPage.getCurrentUrl());
        System.out.println("✅ Test Passed: Successfully logged in, verified dashboard, and logged out.");
    }
}