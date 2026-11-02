# Add Students to Class Roster Test Plan

## Scope and assumptions
- Covers the confirmed sequence for `Sample class`: stage Rahul, verify him in the Class Student Roster panel; search and stage Sairaj; verify both staged names and displayed button counts; save once; verify the saved roster; remove only those two; verify the original empty roster; sign out.
- Live headed Playwright run began at the scenario's QA staging start URL. The initial class roster was empty, and each target search returned one enabled, unselected option. Both students were staged one at a time and the single Save action returned to the class roster with both student rows visible.
- The scenario-provided success notification was not observed after saving. Both saved rows were verified, so the discrepancy was recorded and the explicitly requested cleanup continued. Rahul Student and Sairaj Student were individually selected in the Class Student Roster, the UI showed `Remove 2 Students`, and after removal the panel showed `0 Students`. Saving returned to the class roster page, which displayed `No results found.`; the teacher was then signed out and the login page was displayed.
- The former one-versus-two-student ambiguity is resolved by the user's explicit two-student sequence. Application knowledge remains contextual; the live observations below take precedence where they differ.
- Repository conventions: Java, Selenium WebDriver, TestNG, Page Object Model, `BaseTest`, `EnvReader`, and `TestDataUtil`; no Maven/Gradle. `LoginLogoutTest` obtains the password through `EnvReader.get("PASSWORD")` and the teacher from `testdata/LoginLogoutTestData.json`. The scenario and JSON specify different teacher identities; resolve the approved account source before generating code. Do not put username or password values in test code or this plan.

## Environment and prerequisites
- Base URL / environment source: QA staging; start at `https://my-qastg.mheducation.com/login?logout=true` (scenario-provided).
- Required role (credential variable names only): Teacher; use the approved QA credential source. The existing test uses `PASSWORD`; its username source conflicts with the scenario. Do not copy credential values into this plan.
- Setup and cleanup: Baseline was confirmed empty before this run. Both test students were added, verified, removed by selecting only those two in the Class Student Roster, and the empty baseline was verified after saving. The teacher was signed out. The success notification remained unobserved. Never use remove-all or affect other students.
- Shared state: Roster changes persist. These scenarios must not run concurrently against the same class or teacher account.

## Scenarios
### 1. Verify starting roster and target eligibility
**Priority:** P0  
**Type:** Positive  
**Preconditions:** QA staging is available; approved teacher identity is resolved; no other test is editing this class roster.  
**Test data:** `Sample class`; candidates `Rahul Student` and `Sairaj Student`.  
**Steps:**
1. Navigate to the scenario start URL and sign in as the approved teacher.
2. Confirm `My Programs` and inspect the active class cards.
3. Open `Classroom Actions` for `Sample class`, choose `Manage Class Roster`, and confirm the class roster page.
4. Confirm the student roster is empty before opening `Edit Student Roster`.
5. Search for each candidate in the School Student Roster and confirm one enabled, unselected result per exact name. Do not select or add either student.
**Expected results:**
- My Programs shows two active program cards, including `Sample class`.
- The `Sample class` student roster displays `No results found.` and no student rows.
- Each candidate search returns one matching, enabled, unselected option (`Rahul Student`; `Sairaj Student`).
- No roster changes are made.
**Cleanup:** None; no roster mutation is permitted.

### 2. Add both confirmed students, save once, and restore the empty roster
**Priority:** P0  
**Type:** Positive  
**Preconditions:** User explicitly confirmed the two-student sequence. QA staging is available; immediately before mutation confirm `Sample class` is empty and both candidates are eligible; no concurrent roster changes.
**Test data:** `Sample class`; `Rahul Student`; `Sairaj Student`.  
**Steps:**
1. Sign in and navigate to `Sample class` → `Manage Class Roster` → `Roster` → `Edit Student Roster`.
2. Search for `Rahul Student`, verify exactly one enabled, unselected result, select it, and confirm the add button says `Add 1 Student`.
3. Click `Add 1 Student`; confirm the Class Student Roster panel shows `1 Student` and `Rahul Student`.
4. Search for `Sairaj Student`, verify exactly one enabled, unselected result, select it, and confirm the add button says `Add 1 Student`.
5. Click `Add 1 Student`; before saving, confirm the Class Student Roster panel shows exactly `2 Students` with Rahul and Sairaj, the add button says `Add 0 Students`, and `Save` is available. Do not stage any other name.
6. Click `Save` once. Verify both saved class-roster rows. Record whether the scenario-provided success notification is visible; if it is absent, record the discrepancy and continue with the explicitly authorized targeted cleanup.
7. Reopen the edit roster UI. Select only Rahul and Sairaj for removal, verify the selected names and exact removal count before clicking `Remove 2 Students`, and save the removal. Never use remove-all.
8. Verify both names are absent and the original empty class roster is restored. Then use the user menu to sign out and verify the logged-out state.
**Expected results:**
- Only Rahul and Sairaj are staged and saved; both rows appear in the class roster after Save.
- Scenario-provided notification text is `Student Roster was successfully updated!`; it was **not observed** in the live post-save check. Record this as a discrepancy; it did not prevent the requested cleanup because the saved target rows were verified.
- Cleanup is complete only after both targets are absent and the empty baseline is visibly restored; sign-out is complete only after the logged-out state is visible.
**Cleanup:** Completed in this run. Only Rahul Student and Sairaj Student were selected; the removal control showed `Remove 2 Students`; the class-roster panel then showed `0 Students`. After saving, the class roster page displayed `No results found.` The account menu's `Log out` action returned to `https://my-qastg.mheducation.com/login?logout=true`.

## Browser observations for Selenium implementation

Confidence reflects the strength of the live observation: **High** = control/state directly inspected and resulting state checked; **Medium** = attribute or control inspected but resulting action was not exercised; **Not observed** = blocked or intentionally not exercised.

| Workflow area | Observed label/role | Useful DOM attributes | Locator candidate | Confidence | Verified resulting state |
|---|---|---|---|---|---|
| Login fields | Username and password inputs | `data-automation-id="login-username"`; `data-automation-id="login-password"`; password input `type="password"` | `input[data-automation-id='login-username']`; `input[data-automation-id='login-password']` | High | Teacher session reached My Programs; no credential values belong in this plan. |
| Login submit | `Log In`, button | `query-id="login.submit"`; submit button | `button[query-id='login.submit']` | High | Navigation proceeded to My Programs after login. |
| Programs and class cards | `My Programs`; `Active programs`; `Sample class`; `Classroom Actions, Sample class ALEKS Adventure Alpha G1` button | Card `data-automation-id="ilp-section-card"`; action button `data-automation-id="section-options-lh-drop-menu-button"` (previously observed); accessible button name confirmed live | Button by accessible role/name, scoped to the `Sample class` card | High | Live page showed two active program cards; opening the Sample class menu exposed `Manage Class Roster`. |
| Class action | `Manage Class Roster`, menuitem | `role="menuitem"`; menu item automation ID previously observed | Menuitem by accessible name `Manage Class Roster` | High | Opened `Students | Roster | Sample class`; original student roster displayed `No results found.` |
| Roster navigation | `Roster`, link in `main menu` navigation | `href` ends `/manage-class-rosters` | `a[href$='/manage-class-rosters']` | High | Clicking retained the Sample class roster route/state. |
| Edit roster navigation | `Edit Student Roster`, link | `href` ends `/manage-class-rosters/class/student/manage` | `a[href$='/manage-class-rosters/class/student/manage']` | High | Opened `Add Existing Students | Roster | Sample class`. |
| School Student Roster search | `Search Student Roster`, textbox; `Search`, button | Search textbox observed by accessible name; `Search` button observed by role/name | `getByRole('textbox', {name:'Search Student Roster'})`; exact `Search` button within the search area | High | Clicking Search returned one exact result for Rahul and one for Sairaj. |
| Candidate results | School Student Roster listbox; target names as options | `role="listbox"`; option `role="option"`, `aria-selected="false"`, `aria-disabled="false"`, `data-automation-tag="-deselected"` observed for each before selection | Scope `getByRole('listbox', {name:'School Student Roster'})` to `getByRole('option', {name:<target>})` | High | Each exact query returned one enabled, unselected option; after selection the option exposed selected state. |
| Starting class roster | `No results found.` on student roster page; edit panel `Class Student Roster`, `0 Students` | No student rows on initial page; empty count observed in editor | Visible empty-state text and editor heading/count | High | Initial baseline was confirmed empty before changes. |
| Add action and staged roster | `Add 1 Student`, `Add 0 Students`; `Class Student Roster`; `1 Student` / `2 Students` | Add button `data-automation-id="add-students-to-roster-btn"` was previously observed; option roles and visible counts observed live | Add button by exact accessible name/count; staged options scoped to `Class Student Roster` listbox | High | Rahul was added first and shown alone; Sairaj was then added; panel showed exactly `2 Students`, Rahul, and Sairaj. Button showed `Add 0 Students` before Save. |
| Save action | `Save`, button | `data-automation-id="save-school-roster-btn"` was previously observed; accessible button name and click confirmed live | Button by exact accessible name `Save` | High | Save was clicked once; app returned to the class student roster page. |
| Success notification | Scenario-provided `Student Roster was successfully updated!` | No matching accessible text; post-save check found no visible `role=alert`/`role=status` message | Not observed | Not observed | Expected notification was not captured, although the save returned to the roster page and both rows were visible. |
| Saved roster rows | Student table; two visible target rows | One table with three `tr` elements (header plus two rows); visible text showed `Displaying 2 results`, `Student, Rahul`, `Student, Sairaj` | Visible row text within the student roster table; refine only after inspecting stable row attributes | High | Both saved rows were directly confirmed after Save. |
| Targeted removal | In `Class Student Roster`, `Rahul Student` and `Sairaj Student` options; `Remove 2 Students` button | Options exposed via `role="option"` in the `Class Student Roster` listbox; removal button accessible name reflected selected count | Select exact options in `getByRole('listbox', {name:'Class Student Roster'})`; click button by exact name `Remove 2 Students` | High | Screenshot confirmed only Rahul and Sairaj selected; removal changed panel count to `0 Students`; saving returned to the roster page with `No results found.` |
| Sign out | `Rishan Teacher Account` button; `Log out` link | Account button `data-automation-id="user-menu-dropdown"`; logout link `data-automation-id="log out"` | `button[data-automation-id='user-menu-dropdown']`; `a[data-automation-id='log out']` | High | Clicking Log out returned to `/login?logout=true` with title `PreK-12 Login | McGraw Hill`. |

## Coverage gaps and questions requiring confirmation
- Determine why the expected success notification was not observed (for example, whether it is transient or no longer emitted); the saved rows and subsequent cleanup were independently verified.
- The original empty roster was restored and verified after saving the targeted removal; no other students were removed.
- Sign-out was verified by return to the QA login URL. A separate logout confirmation alert was not observed in the final snapshot.
- Resolve the teacher account source for generated automation: the scenario and existing `LoginLogoutTestData.json` differ. Use approved secret/configuration inputs only; do not record credential values.
- Framework locator implementation should confirm the observed attributes remain stable; the saved-row DOM attributes were not identified beyond table rows and visible student names.
