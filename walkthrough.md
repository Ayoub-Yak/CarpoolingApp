# Carpooling Application — Implementation Walkthrough

## Overview

Complete JavaFX + SQLite Carpooling Application built on a Maven project with the following architecture:

```
com.covoiturage/
├── App.java                    # Entry point, starts with Login view
├── SessionManager.java         # Singleton to track logged-in user
├── model/
│   ├── enums/                  # 4 enums (StatutCompte, StatutTrajet, StatutReservation, StatutPaiement)
│   ├── User.java (abstract)    # SHA-256 hashing, login blocking
│   ├── Passager.java           # Extends User, manages reservations
│   ├── Chauffeur.java          # Extends User, manages vehicles/trips
│   ├── Admin.java              # Extends User, manages users
│   ├── Vehicule.java
│   ├── Trajet.java             # Auto-COMPLET when last seat taken
│   ├── Reservation.java
│   └── Paiement.java           # AUTORISE → CAPTURE lifecycle
├── db/
│   └── DatabaseManager.java    # Singleton, 5 tables, seeds admin
├── dao/                        # 5 interfaces + 5 implementations
├── service/
│   ├── AuthService.java        # Login/signup/account management
│   ├── TrajetService.java      # Trip CRUD
│   ├── ReservationService.java # 24h cancellation rule
│   ├── PaiementService.java    # Authorization/Capture/Refund
│   ├── VehiculeService.java    # Vehicle CRUD
│   └── NotificationService.java
└── controller/
    ├── LoginController.java
    ├── SignUpController.java
    ├── PassengerDashboardController.java
    ├── DriverDashboardController.java
    └── AdminDashboardController.java
```

---

## Key Features

### 🔐 Authentication
- **Sign In** with email/password (SHA-256 hashed)
- **Sign Up** as Passager or Chauffeur
- **Login blocking**: after 3 failed attempts → `BLOQUE`
- **Default admin**: `admin@covoiturage.com` / `admin123`

### 🚗 Driver Dashboard (4 tabs)
1. **Propose Trip**: City/date/time/price/seats → publishes trip
2. **My Trips**: View all, cancel (with 20% penalty if < 24h)
3. **Received Reservations**: Accept (captures payment) / Refuse (refunds)
4. **My Vehicles**: Add/delete vehicles

### 🧳 Passenger Dashboard (2 tabs)
1. **Search Trips**: Filter by city, book with payment authorization
2. **My Reservations**: View all, cancel (100% refund if > 24h, 50% if < 24h)

### 👤 Admin Dashboard (2 tabs)
1. **User Management**: Stats + Suspend/Block/Reactivate accounts
2. **Trip Overview**: View all trips system-wide

### 💰 Business Rules
- **24h Cancellation Rule**:
  - Chauffeur cancels < 24h: 20% penalty per passenger
  - Passager cancels < 24h: 50% refund only
- **Payment Lifecycle**: AUTORISE → CAPTURE → REMBOURSE/ANNULE
- **Auto-COMPLET**: Trip status changes when last seat is booked

---

## Verification

- ✅ `mvn compile` — exit code 0
- Default admin seeded on first run
- All 5 database tables created automatically
- Defensive copies on all collection getters

---

## How to Run

```bash
cd "c:\Users\yaako\OneDrive\Bureau\Covoiturage Project\CarpoolingApp"
mvn clean javafx:run
```
