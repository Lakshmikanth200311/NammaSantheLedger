# Namma-Santhe Ledger 📒
### AI-Based Digital Khata App for Village Market Vendors
### MindMatrix VTU Internship Program — Project #56

---

## 📋 Problem Statement
Weekly village markets (Santhe) are the lifeblood of rural retail. Small vendors selling vegetables, bangles, and snacks struggle to keep track of "Credit" (Udari) given to regular customers. They often lose money because they forget who owes them what.

---

## 💡 Solution
Namma-Santhe Ledger is a "Simplified Digital Khata" for small vendors. It replaces the messy pocket diary and helps the smallest businessman understand their Daily Profit and Total Pending Dues at the end of the market day.

---

## ✅ Features
- **Total Outstanding** shown on home screen
- **Search customers** by name
- **2-step transaction entry** — tap customer → enter amount → Udari or Payment
- **Daily Summary** — dues pending and total outstanding
- **WhatsApp Reminder** — pre-filled Kannada message with due amount
- **Payment Log** — record when customer pays back
- **Offline** — works without internet using Room DB

---

## 🛠️ Tech Stack
| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM |
| Database | Room DB (SQLite) |
| State Management | StateFlow + Coroutines |
| Build System | Gradle with KSP |

---

## 📁 Project Structure
app/src/main/java/com/example/santheledger/
├── MainActivity.kt           ← UI (Jetpack Compose)
├── data/
│   ├── Customer.kt           ← Room Entity
│   ├── Transaction.kt        ← Room Entity
│   ├── CustomerDao.kt        ← Database Access
│   ├── TransactionDao.kt     ← Database Access
│   └── AppDatabase.kt        ← Room Database
├── viewmodel/
│   └── LedgerViewModel.kt    ← Business Logic
└── ui/theme/
└── Theme.kt              ← App Theme

---

## ⚙️ Setup & Installation

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 11
- Android SDK 34

### Steps to Run
1. Clone the repository:
```bash
git clone https://github.com/YOURUSERNAME/NammaSantheLedger.git
```
2. Open in **Android Studio**
3. Wait for **Gradle sync** to complete
4. Run on emulator **(API 33 or 34 recommended)** or physical Android device

---

## 📱 Screenshots

| Home Screen | Add Customer | Add Transaction |
|---|---|---|
| ![Home](screenshots/home.png) | ![Add Customer](screenshots/add_customer.png) | ![Transaction](screenshots/transaction.png) |

---

## 🏗️ Architecture
UI (Jetpack Compose)
↕ collectAsState()
ViewModel (StateFlow)
↕ coroutines
Repository
↕ DAOs
Room Database (SQLite)
