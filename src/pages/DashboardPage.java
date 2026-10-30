package pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DashboardPage extends BasePage {

    private final By myProgramsHeader = By.cssSelector("[data-automation-id='lib-ilp-my-classes-header']");
    private final By userMenuDropdown = By.cssSelector("[data-automation-id='user-menu-dropdown']");
    private final By logoutLink       = By.cssSelector("a[data-automation-id='log out']");
    private static final int DASHBOARD_LOAD_TIMEOUT_SECONDS = 60;

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public boolean isDashboardLoaded() {
        try {
            WebDriverWait extendedWait =
                new WebDriverWait(driver, Duration.ofSeconds(DASHBOARD_LOAD_TIMEOUT_SECONDS));
            return extendedWait.until(
                ExpectedConditions.visibilityOfElementLocated(myProgramsHeader)).isDisplayed();
        } catch (TimeoutException e) {
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
