# FixIt — Landlord–Tenant Maintenance App

Android app (Java, Room, RecyclerView) for tenants to submit maintenance
requests and landlords to track and resolve them.

## Getting started

1. Clone the repo and open the root folder in Android Studio.
2. Let Android Studio sync Gradle. This project ships without a Gradle
   wrapper jar (binary file, not checked in) — Android Studio will offer to
   download/regenerate it on first sync.
3. Work only inside your own files (table below) so nobody edits the same file.

## Workflow (single `main` branch)

Everyone works directly on `main`. To avoid conflicts:

- Only edit the files you own.
- `git pull --rebase` before you start and before you push.
- Commit small and push often.

## File split

| Person | Screen | Files |
|---|---|---|
| 1 | Login | `LoginActivity.java`, `activity_login.xml`, `User.java`, `UserDao.java` |
| 2 | Sign Up | `SignupActivity.java`, `activity_signup.xml`, `AppDatabase.java`, `build.gradle` (Room + RecyclerView) |
| 3 | Tenant Home | `TenantHomeActivity.java`, `activity_tenant_home.xml`, `RequestAdapter.java`, `item_request.xml` |
| 4 | New Request | `NewRequestActivity.java`, `activity_new_request.xml`, `Request.java`, `RequestDao.java` |
| 5 | Landlord Home | `LandlordHomeActivity.java`, `activity_landlord_home.xml`, `NewRequestReceiver.java` (with the notification) |
| 6 | Request Detail | `RequestDetailActivity.java`, `activity_request_detail.xml`, `AndroidManifest.xml`, end-to-end testing |

Every file already exists as a stub with a `// TODO: PersonN - ...` marker.

## Shared contract

- Logged-in user: `LoginActivity.PREFS_NAME` (`"fixit_prefs"`), `KEY_USER_ID`, `KEY_ROLE`.
- Roles: `User.ROLE_TENANT`, `User.ROLE_LANDLORD`.
- Pending = orange, Done = green, on every screen.

End-to-end test: signup → login → submit request → landlord sees it → mark done.
