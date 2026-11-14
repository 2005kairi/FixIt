# FixIt — Getting Started

How to get the project on your computer, open it and start working.

## 1. Before you start

1. Install **Android Studio**: https://developer.android.com/studio
2. Make sure you have a GitHub account and **accept the invitation** to the repo
   (check your email, or open https://github.com/2005kairi/FixIt/invitations).
   Without accepting you can read the repo but not push.
3. Install **Git** if you don't have it: https://git-scm.com/downloads

## 2. Clone the project

Option A, in Android Studio (easiest):

1. Open Android Studio, choose **Get from VCS** (or File > New > Project from Version Control).
2. URL: `https://github.com/2005kairi/FixIt.git`
3. Pick a folder and click **Clone**. Sign in to GitHub if it asks.

Option B, in a terminal:

```bash
git clone https://github.com/2005kairi/FixIt.git
```

Then in Android Studio choose **Open** and select the `FixIt` folder.

## 3. Open and build

1. Wait for **Gradle sync** to finish (first time can take a few minutes).
2. If a message says the Gradle wrapper is missing, click the option to
   download/regenerate it. This is expected.
3. Create a phone to test on: **Tools > Device Manager > Create Device**, pick any
   Pixel phone and a recent Android version.
4. Click the green **Run** button.

## 4. Test login

The app creates two demo accounts the first time it opens:

| Role | Username | Password |
|---|---|---|
| Tenant | `tenant1` | `tenant123` |
| Landlord | `landlord1` | `landlord123` |

## 5. Start working

Everyone works on the single `main` branch, and every file already exists as a
stub marked `// TODO: PersonN - ...`. **Only edit the files you own:**

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

Run these in the terminal at the bottom of Android Studio (or use the Git menu):

```bash
git pull --rebase          # get everyone's latest changes first
git add <your files>       # only the files you changed
git commit -m "Short description of what you did"
git pull --rebase          # again, in case someone pushed meanwhile
git push
```

Tips:

- Pull before you start each day and before every push.
- Commit small and push often.
- If `git pull --rebase` reports a conflict, you most likely edited someone else's
  file. Ask the owner before fixing it.
- Do not commit the `build/` folder or `local.properties` (they are already ignored).

## Shared rules

- Logged-in user: `LoginActivity.PREFS_NAME` (`"fixit_prefs"`), `KEY_USER_ID`, `KEY_ROLE`.
- Roles: `User.ROLE_TENANT`, `User.ROLE_LANDLORD`.
- Pending = orange, Done = green, on every screen.
- Test every screen in portrait so report screenshots match.
