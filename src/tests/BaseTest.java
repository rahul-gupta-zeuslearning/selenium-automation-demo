package tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class BaseTest {
    protected WebDriver driver;

    public void initializeDriver() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        // Rely exclusively on explicit WebDriverWait (BasePage) to prevent unpredictable wait conflicts
    }

    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
