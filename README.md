# Selenium Java Automation Framework (VS Code Edition)

## 📋 Table of Contents
- [Prerequisites](#-prerequisites)
- [Step-by-Step Setup Guide](#-step-by-step-setup-guide)
  - [1. Install Required VS Code Extensions](#1-install-required-vs-code-extensions)
  - [2. Download Selenium & TestNG Dependencies](#2-download-selenium--testng-dependencies)
  - [3. Configure VS Code Referenced Libraries](#3-configure-vs-code-referenced-libraries)
  - [4. Configure Environment Credentials (.env)](#4-configure-environment-credentials-env)
- [Project Structure](#-project-structure)
- [How to Run Tests](#-how-to-run-tests)
  - [Method 1: Run via VS Code GUI (Play Button)](#method-1-run-via-vs-code-gui-play-button)
  - [Method 2: Run via Terminal (TestNG Suite)](#method-2-run-via-terminal-testng-suite)
- [Viewing Test Results & Reports](#-viewing-test-results--reports)

---

## 🛠 Prerequisites
Ensure the following are installed on your machine:
- **Java Development Kit (JDK 17 or higher)**: Run `javac -version` and `java -version` in terminal to verify.
- **Google Chrome**: Up to date.
- **Visual Studio Code**: Latest version.

---

## 🚀 Step-by-Step Setup Guide

### 1. Install Required VS Code Extensions
Open the **Extensions Marketplace** in VS Code (`Ctrl + Shift + X`) and install:
1. **[Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)** (by Microsoft) - Provides Java language support, debugging, and code completion.
2. **[Test Runner for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-test)** (by Microsoft) - Adds test explorer support, one-click test execution, and TestNG/JUnit integration.

---

### 2. Download Selenium & TestNG Dependencies
This project manages dependencies locally via a `lib/` directory.

#### A. Selenium Java Bindings
1. Go to the official download page: [https://www.selenium.dev/downloads/](https://www.selenium.dev/downloads/)
2. Under **Selenium Clients and WebDriver Language Bindings**, download the latest stable **Java** zip file.
3. Extract the downloaded `.zip` file.
4. Create a folder named `lib` in the project root:
   ```powershell
   mkdir lib
   ```
5. Copy all the `.jar` files from the extracted Selenium folder (including files inside the extracted `lib` subfolder) directly into your project's `lib/` directory.

#### B. TestNG Dependencies
Download the following 3 JAR files from Maven Central via your browser and place them into `lib/`:
- **[testng-7.8.0.jar](https://repo1.maven.org/maven2/org/testng/testng/7.8.0/testng-7.8.0.jar)**
- **[jcommander-1.82.jar](https://repo1.maven.org/maven2/com/beust/jcommander/1.82/jcommander-1.82.jar)**
- **[slf4j-api-2.0.9.jar](https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar)**

---

### 3. Configure VS Code Referenced Libraries
To ensure VS Code recognizes all JARs in `lib/` without Maven, create or verify `.vscode/settings.json` in the project root:

```json
{
    "java.project.referencedLibraries": [
        "lib/**/*.jar"
    ]
}
```

> **Note:** If VS Code shows dependency errors, press `Ctrl + Shift + P` and run **Java: Clean Java Language Server Workspace**, then restart VS Code.

---

### 4. Configure Environment Credentials (`.env`)
To protect sensitive credentials (passwords, tokens), the project uses an environment file that is strictly ignored by Git.

In the project root, make a copy of `.env.example` and rename it to `.env`:
   ```powershell
   Copy-Item .env.example .env
   ```
---

## 📁 Project Structure

```text
├── .env.example                         # Template for sensitive credentials
├── .gitignore                           # Excludes lib/, .vscode/, bin/, .env, and test-output/
├── README.md                            # Setup and project documentation
├── lib/                                 # Local dependency JARs (Selenium, TestNG)
├── test-suites/
│   └── testng.xml                       # TestNG XML suite runner configuration
├── testdata/
│   └── LoginLogoutTestData.json         # JSON test data file (URLs, usernames, roles)
├── src/
│   ├── pages/                           # Page Object Model classes
│   │   ├── BasePage.java                # Reusable driver actions & explicit waits
│   │   ├── DashboardPage.java           # Dashboard locators & interactions
│   │   └── LoginPage.java               # Login page locators & interactions
│   ├── tests/                           # Test scripts
│   │   ├── BaseTest.java                # Driver initialization (@BeforeMethod) & teardown (@AfterMethod)
│   │   └── LoginLogoutTest.java         # TestNG test cases
│   └── utils/                           # Framework utilities
│       ├── EnvReader.java               # Loads .env secrets with fallback to system env
│       └── TestDataUtil.java            # Reads JSON test data using Selenium's built-in parser
```

---

## 🧪 How to Run Tests

### Method 1: Run via VS Code GUI (Play Button)
1. Open [`src/tests/LoginLogoutTest.java`](file:///src/tests/LoginLogoutTest.java) in VS Code.
2. You will see a green **Play icon (`▶`)** next to:
   - The class declaration `public class LoginLogoutTest`
   - The test method `@Test public void testLoginLogout()`
3. Click the **Play icon** to run the test, or right-click and select **Debug Test** to step through.
4. You can also view and run all tests from the **Testing** panel (the beaker icon ⚗️ on the left activity bar).

---

### Method 2: Run via Terminal (TestNG Suite)
To compile and execute tests from the command line:

1. **Compile the source code**:
   ```powershell
   javac -cp "lib/*;src" -d bin src/utils/*.java src/pages/*.java src/tests/*.java
   ```

2. **Execute the TestNG Suite**:
   ```powershell
   java -cp "bin;lib/*" org.testng.TestNG test-suites/testng.xml
   ```

---

## 📊 Viewing Test Results & Reports

After running tests via TestNG or terminal, reports are automatically generated in the `test-output/` folder:

1. **Emailable Summary Report**:
   Open in your browser:
   ```powershell
   Start-Process .\test-output\emailable-report.html
   ```
2. **Interactive TestNG Dashboard**:
   Open in your browser:
   ```powershell
   Start-Process .\test-output\index.html
   ```
3. **VS Code Test Results Panel**:
   When running via the GUI, results appear live in the **Test Results** bottom panel showing pass/fail status, execution time, and any assertion failures.
