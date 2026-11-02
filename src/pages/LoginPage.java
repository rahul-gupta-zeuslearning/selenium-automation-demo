package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoginPage extends BasePage {

    private final By usernameInput = By.cssSelector("[data-automation-id='login-username']");
    private final By passwordInput = By.cssSelector("[data-automation-id='login-password']");
    private final By loginButton   = By.cssSelector("button[query-id='login.submit']");
    private final By loggedOutAlert = By.cssSelector("[role='alert']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void enterUsername(String username) {
        sendKeys(usernameInput, username);
    }

    public void enterPassword(String password) {
        sendKeys(passwordInput, password);
    }

    public void clickLogin() {
        click(loginButton);
    }

    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    public boolean isLoginPageDisplayed() {
        return isDisplayed(usernameInput);
    }

    public boolean isLoggedOutAlertDisplayed() {
        try {
            return wait.until(currentDriver -> {
                for (WebElement alert : currentDriver.findElements(loggedOutAlert)) {
                    if (alert.isDisplayed()
                        && alert.getText().contains("You have been logged out.")) {
                        return true;
                    }
                }
                return false;
            });
        } catch (TimeoutException e) {
            return false;
        }
    }
}
