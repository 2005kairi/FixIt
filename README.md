# FixIt — Landlord–Tenant Maintenance App

Android app (Java, Room, RecyclerView) for tenants to submit maintenance
requests and landlords to track and resolve them.

## Getting started

1. Clone the repo and open the root folder in Android Studio.
2. Let Android Studio sync Gradle. This project ships without a Gradle
   wrapper jar (binary file, not checked in) — Android Studio will offer to
   download/regenerate it on first sync. If it doesn't prompt automatically,
   run `gradle wrapper` once you have Gradle installed locally.
3. Check out your branch (see below) and work only inside your files —
   everyone shares `main` as the integration branch.

## Team split & branches

| Branch | Owner | Files |
|---|---|---|
| `person1-login-signup` | Person 1 | `LoginActivity.java`, `SignupActivity.java`, `activity_login.xml`, `activity_signup.xml` |
| `person2-user-data` | Person 2 | `User.java`, `UserDao.java` (role-based routing used by `LoginActivity`) |
| `person3-tenant` | Person 3 | `TenantHomeActivity.java`, `NewRequestActivity.java`, `activity_tenant_home.xml`, `activity_new_request.xml`, `item_request.xml`, `RequestAdapter.java` |
| `person4-landlord` | Person 4 | `LandlordHomeActivity.java`, `RequestDetailActivity.java`, `activity_landlord_home.xml`, `activity_request_detail.xml` |
| `person5-request-broadcast` | Person 5 | `Request.java`, `RequestDao.java`, `NewRequestReceiver.java`, notification setup, `<receiver>` entry in `AndroidManifest.xml` |
| `person6-database-integration` | Person 6 | `AppDatabase.java`, `strings.xml`, `colors.xml`, `themes.xml`, final `AndroidManifest.xml` merge, `build.gradle` dependencies, end-to-end testing |

Each branch starts from `main` and already contains the full skeleton
(all activities/receiver wired into the manifest, Room + RecyclerView deps
in `app/build.gradle`) so the project compiles from day one. Every stub file
has a `// TODO: PersonN - ...` comment marking what's left to build.

Workflow: work on your branch, open a PR into `main` when your slice is
ready, Person 6 integrates and runs the end-to-end test:
signup → login → submit request → landlord sees it → mark done.
