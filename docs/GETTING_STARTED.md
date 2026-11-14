# FixIt: Getting Started

How to get the project on your computer, open it and start working.
(The same guide is available as `GETTING_STARTED.pdf` / `.docx`.)

## 1. Before you start

1. Install **Android Studio**: https://developer.android.com/studio
2. **Accept the GitHub invitation** (check your email, or open https://github.com/2005kairi/FixIt/invitations).
   Without accepting you can read the repo but not push.
3. Install **Git** if you don't have it: https://git-scm.com/downloads

## 2. Clone the project

In Android Studio: **Get from VCS** (or File > New > Project from Version Control), URL
`https://github.com/2005kairi/FixIt.git`, pick a folder and click **Clone**.

Or in a terminal:

```bash
git clone https://github.com/2005kairi/FixIt.git
```

Then open the `FixIt` folder in Android Studio.

## 3. Open and build

1. Wait for **Gradle sync** to finish (first time can take a few minutes).
2. If a message says the Gradle wrapper is missing, click the option to download or regenerate it.
3. Create a phone: **Tools > Device Manager > Create Device** (any Pixel, recent Android).
4. Click the green **Run** button.

If the app complains about Java or Gradle versions, open File > Settings > Build Tools > Gradle
and set the Gradle JDK to the bundled JDK.

## 4. Log in and try it

The app creates these accounts and eight sample requests the first time it starts with an empty database.

| Role | Username | Password | Unit | Landlord |
|---|---|---|---|---|
| Landlord | `landlord1` | `landlord123` | | |
| Landlord | `landlord2` | `landlord123` | | |
| Tenant | `tenant1` | `tenant123` | A-101 | landlord1 |
| Tenant | `tenant2` | `tenant123` | A-102 | landlord1 |
| Tenant | `tenant3` | `tenant123` | B-201 | landlord2 |

If you already ran an older version, clear the app's storage (long-press the app icon > App info >
Storage > Clear storage) or uninstall it, so the new demo data is created.

Quick tour: log in as `tenant1` and submit a request; log in as `landlord1`, open it and mark it Done
or Reject it with reasons; log back in as `tenant1` to see the result.

## 5. Start working

Everyone works on the single `main` branch. **Only edit the files you own:**

| Person | Screen | Files |
|---|---|---|
| 1 | Login | `LoginActivity.java`, `activity_login.xml`, `User.java`, `UserDao.java` |
| 2 | Sign Up | `SignupActivity.java`, `activity_signup.xml`, `AppDatabase.java`, `build.gradle` |
| 3 | Tenant Home | `TenantHomeActivity.java`, `activity_tenant_home.xml`, `RequestAdapter.java`, `item_request.xml` |
| 4 | New Request | `NewRequestActivity.java`, `activity_new_request.xml`, `Request.java`, `RequestDao.java` |
| 5 | Landlord Home | `LandlordHomeActivity.java`, `activity_landlord_home.xml`, `NewRequestReceiver.java` |
| 6 | Request Detail | `RequestDetailActivity.java`, `activity_request_detail.xml`, `AndroidManifest.xml`, testing |

Java files are in `app/src/main/java/com/fixit/app/`, layouts in `app/src/main/res/layout/`.

## 6. Save and share your work

```bash
git pull --rebase          # get everyone's latest changes first
git add <your files>       # only the files you changed
git commit -m "Short description of what you did"
git pull --rebase          # again, in case someone pushed meanwhile
git push
```

- Pull before you start each day and before every push.
- Commit small and push often.
- If `git pull --rebase` reports a conflict, you most likely edited someone else's file. Ask the owner first.
- Do not commit the `build/` folder or `local.properties` (they are already ignored).

## 7. More documentation

- `PROJECT_DOCUMENTATION`: screens, features, data model, architecture, rules and the test plan.
- `PERSON1_LOGIN_WORK`: the Login screen with its full source.
- `CHECKLISTS_ALL_STUDENTS`: one checklist page per team member.
