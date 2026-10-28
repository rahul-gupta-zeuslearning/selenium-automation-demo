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
        // 1. Load test data and environment credentials
        String loginUrl = TestDataUtil.getProperty("LoginLogoutTestData", "loginUrl", "https://my-qastg.mheducation.com/login");
        String username = TestDataUtil.getProperty("LoginLogoutTestData", "username", "SRTeacher");
        String password = EnvReader.get("MHE_PASSWORD");
        if (password == null || password.trim().isEmpty()) {
            password = EnvReader.get("PASSWORD");
        }

        Assert.assertNotNull(password, "Password not found! Ensure MHE_PASSWORD is set in the .env file.");
        Assert.assertFalse(password.trim().isEmpty(), "Password in .env cannot be empty.");

        // 2. Navigate to login portal
        System.out.println("Navigating to: " + loginUrl);
        driver.get(loginUrl);

        // 3. Perform Login
        LoginPage loginPage = new LoginPage(driver);
        System.out.println("Submitting credentials for user: " + username);
        loginPage.login(username, password);

        // 4. Initialize Dashboard Page & Assert it is loaded
        DashboardPage dashboardPage = new DashboardPage(driver);
        System.out.println("Waiting up to 20 seconds for dashboard to render...");
        boolean isLoaded = dashboardPage.isDashboardLoaded();
        
        Assert.assertTrue(isLoaded, 
            "Dashboard failed to load 'My Programs' header within 20s. Current URL: " + driver.getCurrentUrl() + " | Page Title: " + driver.getTitle());
        System.out.println("✅ Dashboard confirmed loaded! Header: " + dashboardPage.getDashboardHeaderText());

        // 5. Perform Logout
        System.out.println("Triggering logout...");
        dashboardPage.logout();

        // 6. Verify redirection back to login
        boolean isBackOnLogin = loginPage.isLoginPageDisplayed() || driver.getCurrentUrl().contains("login");
        Assert.assertTrue(isBackOnLogin, 
            "Did not redirect to login page after logout. Current URL: " + driver.getCurrentUrl());
        System.out.println("✅ Test Passed: Successfully logged in, verified dashboard, and logged out.");
    }
}
