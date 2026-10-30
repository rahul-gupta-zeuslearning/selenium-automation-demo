# User Story: Add a Student to a Class Roster

## Story

As a teacher, I want to add an eligible student to the roster for my “Sample class” so that the student is enrolled in that class.

## Environment and test data

- Environment: QA staging
- Start URL: `https://my-qastg.mheducation.com/login?logout=true`
- User role: Teacher
- Sign in with with 'Username - RishanTeacher' and password - 'Aleks1234'.
- Use students- 'Rahul student' and 'Sairaj student' for student roaster actions.

## User workflow

1. Navigate to the star URL mentioned above and sign in as the teacher using the given credentials.
2. Confirm the My Programs page is displayed and inspect the class cards. The scenario expects two class cards.
3. On the 'Sample class' card, open 'Classroom actions' dropdown.
4. Choose 'Manage Class Roster' and confirm the student roster area opens.
5. In the side navigation, select 'Roster'.
6. Select 'Edit Student Roster'.
7. In 'School Student Roster', search for the designated test student's name.
8. Select the matching student from the search results. Verify the selected student and the add action before submitting (Add them one by one)
9. Select 'Add 2 Students' and verify if both the student names are visible under the 'Class Student roaster'.
10. Select 'Save'.
11. Observe the result and verify the success notification 'Student Roster was successfully updated!' appears.
12. Verify the designated student is listed in the class students table after the update.
13. Sign out as the teacher by using the user menu.

The planner should use the configured Playwright browser only to explore the UI. Record observed labels, roles, useful DOM attributes, locator candidates, and confidence for the navigation controls, search field, student result, add/save controls, success notification, and roster row. Do not invent selectors or assume that a click succeeded without checking the resulting state.

## Acceptance criteria

- Given the teacher is signed in and the designated student is eligible and not already enrolled, when the teacher adds that student to `Sample class` and saves, then the success notification is shown.
- After saving, the designated student appears in the class students table.
- The roster update can be verified from the UI; successful clicks alone are not sufficient evidence.
- The teacher can sign out after the update.

## Cleanup and safety

- After testing the above workflow we should remove students both students added from the 'Class student Roaster' by selecting students and then clicking on 'Remove students' button.
- Verify the target student's removal and the original roster state, then sign out.
- **Do not remove all students from the class roster.** The original teardown steps could delete unrelated roster data.
- If the app does not support safely removing only the test student, or the initial enrollment state cannot be confirmed, stop and ask for cleanup guidance rather than modifying other students.
- Logout from teacher user by clicking on the username (RishanTeacher) dropdown button on top right corner.

