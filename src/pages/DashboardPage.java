package pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DashboardPage extends BasePage {

    // Locators - using tag-agnostic selector to prevent failures if tag changes
    private final By myProgramsHeader = By.cssSelector("[data-automation-id='lib-ilp-my-classes-header']");
    private final By userMenuDropdown = By.cssSelector("[data-automation-id='user-menu-dropdown']");
    private final By logoutLink       = By.cssSelector("a[data-automation-id='log out']");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Waits up to 20 seconds for the 'My Programs' heading to become visible,
     * ensuring the Angular/SPA dashboard has completely rendered.
     */
    public boolean isDashboardLoaded() {
        try {
            WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(20));
            return extendedWait.until(ExpectedConditions.visibilityOfElementLocated(myProgramsHeader)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getDashboardHeaderText() {
        return getText(myProgramsHeader);
    }

    public void openUserMenu() {
        click(userMenuDropdown);
    }

    public void clickLogout() {
        click(logoutLink);
    }

    public void logout() {
        openUserMenu();
        clickLogout();
    }
}
