# Positive Financial

A local-first personal finance app for Android, built with Kotlin and Jetpack
Compose. Track daily income and expenses, manage multiple wallets (cash,
bank, e-wallet, credit card), and see where your money goes — all data stays
on the device.

## Highlights

- **Dashboard** — total balance, this month's income/expenses, savings
  progress, spending breakdown (donut chart), account balances, a 6-month
  income/expense bar chart, recent activity, and a fully on-device "AI
  Insights" card that highlights your top spending category and gives
  savings tips based on simple rules (no network calls, nothing leaves the
  device).
- **Transactions** — quick add/edit flow for income and expenses, search and
  filter (by type, text) across all transactions, and transfers between
  accounts (including paying down a credit card).
- **Accounts** — Cash plus starter presets for BCA, Mandiri, GoPay, OVO,
  DANA, and ShopeePay; add/edit/delete your own; per-account transaction
  history.
- **Credit cards** — separate from regular accounts: track credit limit,
  outstanding balance, available limit, billing cycle day, payment due day,
  payment history, and a record-payment flow. Each card has its own
  configurable reminder (day of month + time).
- **Notifications** — a daily reminder (default 9:00 PM) to log your day's
  transactions, plus per-card payment reminders, both scheduled locally with
  `AlarmManager` and restored after device reboot.
- **Privacy** — Room (SQLite) database + DataStore preferences, both
  excluded from Android auto-backup. Optional biometric app lock
  (fingerprint/face/device credential) in Settings. No account, no sign-up,
  no internet permission.
- **Currency** — Indonesian Rupiah (IDR) formatting throughout, with an
  Indonesian-oriented set of starter categories (Food & Beverages, Bonus &
  THR, Donation & Zakat, etc.) and an English UI.

## Tech stack

- Kotlin, Jetpack Compose (Material 3), Navigation Compose
- Room (SQLite) for persistence, DataStore for preferences
- Hilt for dependency injection
- `AlarmManager` + `BroadcastReceiver`s for local notification scheduling
- `androidx.biometric` for the optional app lock
- Charts are hand-drawn with Compose `Canvas` — no external charting
  dependency

## Project structure

```
app/src/main/java/com/positivefinancial/app/
├── data/
│   ├── local/        Room entities, DAOs, database, default seed data
│   ├── repository/   Repositories used by ViewModels
│   ├── settings/      DataStore-backed app settings
│   └── model/         Shared enums
├── di/                Hilt modules
├── notification/      AlarmManager scheduling + BroadcastReceivers
├── ui/
│   ├── theme/          Colors, typography, Material3 theme
│   ├── navigation/     Nav graph + bottom navigation
│   ├── components/     Reusable composables (charts, cards, rows, dialogs)
│   ├── dashboard/       Home screen
│   ├── transactions/    Add/edit, list + search/filter, transfers
│   ├── accounts/        Accounts list, detail, add/edit
│   ├── creditcards/     Cards list, detail, add/edit, payment recording
│   ├── biometric/       App lock gate + BiometricPrompt wrapper
│   └── settings/        Settings screen
└── util/                Formatters, date helpers, icon mapping, insights
```

## Building

This repository was authored without access to the Android SDK, so it has
not been compiled in this environment. To build it:

1. Open the project root in Android Studio (Koala or newer recommended).
2. Let Gradle sync — it will download the Android Gradle Plugin, Kotlin,
   Compose, Room, and Hilt dependencies from Google's Maven and Maven
   Central.
3. Run the `app` configuration on a device or emulator running Android 8.0
   (API 26) or newer.

Command line, once the Android SDK is installed and `ANDROID_HOME`/
`local.properties` is configured:

```bash
./gradlew assembleDebug
```

### Requirements

- Android Studio Koala (2024.1) or newer / Gradle 8.7 (via the included
  wrapper)
- JDK 17
- Android SDK Platform 34, Build-Tools 34.x
- minSdk 26, targetSdk 34

## Notes on permissions

- `POST_NOTIFICATIONS` — requested at first launch (Android 13+) for the
  daily reminder and card payment reminders.
- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` — used opportunistically for
  precise reminder timing; the app falls back to inexact (but still
  near-time) alarms if exact-alarm scheduling isn't permitted.
- `RECEIVE_BOOT_COMPLETED` — reschedules reminders after the device
  restarts.
- `USE_BIOMETRIC` — only used if you turn on the optional biometric lock in
  Settings.
- No internet permission is requested; the app has no network dependency.
