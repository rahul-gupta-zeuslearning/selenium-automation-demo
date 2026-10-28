package tests;

import pages.MheDashboardPage;
import pages.MheLoginPage;

public class MheLoginLogoutTest extends BaseTest {

    public static void main(String[] args) {
        MheLoginLogoutTest test = new MheLoginLogoutTest();
        test.initializeDriver();

        try {
            // 1. Navigate to staging login portal
            String loginUrl = "https://my-qastg.mheducation.com/login";
            System.out.println("Navigating to: " + loginUrl);
            test.driver.get(loginUrl);

            // 2. Perform Login
            MheLoginPage loginPage = new MheLoginPage(test.driver);
            System.out.println("Submitting login credentials...");
            loginPage.login("SRTeacher", "Aleks1234");

            // 3. Initialize Dashboard Page & Assert it is loaded
            MheDashboardPage dashboardPage = new MheDashboardPage(test.driver);
            
            System.out.println("Waiting up to 20 seconds for dashboard to render...");
            if (dashboardPage.isDashboardLoaded()) {
                System.out.println("✅ Dashboard confirmed loaded! Header: " + dashboardPage.getDashboardHeaderText());
            } else {
                System.err.println("❌ Current URL at timeout: " + test.driver.getCurrentUrl());
                System.err.println("❌ Current Page Title: " + test.driver.getTitle());
                throw new IllegalStateException("Dashboard failed to load 'My Programs' header within 20s. Check if login credentials succeeded or if stuck on login/error page.");
            }

            // 4. Perform Logout
            System.out.println("Triggering logout...");
            dashboardPage.logout();

            // 5. Verify redirection back to login
            boolean isBackOnLogin = loginPage.isLoginPageDisplayed();
            if (isBackOnLogin || test.driver.getCurrentUrl().contains("login")) {
                System.out.println("✅ Test Passed: Successfully logged in, verified dashboard, and logged out.");
            } else {
                System.err.println("❌ Test Failed: Did not redirect to login page. Current URL: " + test.driver.getCurrentUrl());
            }

        } catch (Exception e) {
            System.err.println("❌ Test Failed due to exception: " + e.getMessage());
            if (test.driver != null) {
                try {
                    System.err.println("📍 URL at failure: " + test.driver.getCurrentUrl());
                } catch (Exception ignored) {}
            }
            e.printStackTrace();
        } finally {
            // Clean teardown
            test.tearDown();
        }
    }
}
