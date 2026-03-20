# 🚌 Bus Management System

A professional Desktop Application built with **JavaFX**, providing a seamless experience for Admin, Managers, and Passengers.

---

## 📸 Project Visuals & Logic

### 1. Welcome Screen
Users select their role (Admin, Manager, or Passenger) to enter the system.
![Welcome Dashboard](screenshots/Screenshot%202026-03-20%20160809.png)

---

### 2. Admin Portal (Master Control)
- **Security:** Default Login: `admin@bus.com` | Password: `admin123`.
- **Manager Management:** Admin creates manager accounts. Managers can then login using those credentials.
- **Fleet Control:** Admin defines routes and base prices.

| Admin Login | Manager List | Bus Schedules |
| :---: | :---: | :---: |
| ![Admin Login](screenshots/Screenshot%202026-03-20%20160911.png) | ![Managers](screenshots/Screenshot%202026-03-20%20161101.png) | ![Buses](screenshots/Screenshot%202026-03-20%20161326.png) |

---

### 3. Manager Portal (Verification & Pricing)
- **Approval Workflow:** Managers review the passenger's payment screenshot. Once verified, they approve the seat and a ticket is generated.
- **Price Synchronization:** Managers can update prices (Price must be ≥ Admin's floor price). Changes reflect instantly for passengers.

| Manager Login | Booking Requests | Pricing Logic |
| :---: | :---: | :---: |
| ![Manager Login](screenshots/Screenshot%202026-03-20%20161722.png) | ![Requests](screenshots/Screenshot%202026-03-20%20161951.png) | ![Pricing](screenshots/Screenshot%202026-03-20%20162305.png) |

---

### 4. Passenger Journey (Booking Flow)
The passenger portal follows a logical step-by-step booking process:
1. **Search:** Find buses based on source and destination.
2. **Seat Selection:** Choose preferred seats from the interactive layout.
3. **Payment:** Upload a screenshot of the transaction for manager's verification.
4. **History:** Real-time tracking of booking status (Approved/Pending/Rejected).

| 1. Search Bus | 2. Seat Selection | 3. Payment Upload | 4. Ticket History |
| :---: | :---: | :---: | :---: |
| ![Search](screenshots/Screenshot%202026-03-20%20174803.png) | ![Seats](screenshots/Screenshot%202026-03-20%20174853.png) | ![Payment](screenshots/Screenshot%202026-03-20%20174950.png) | ![History](screenshots/Screenshot%202026-03-20%20175035.png) |

---

## 🛠️ Technical Details
- **Frontend:** JavaFX / FXML (Scene Builder).
- **Backend:** Java (Core).
- **Database:** H2 Database Engine (`bus_system_db.mv.db`).
- **Core Logic:** Price validation (Manager Price vs Admin Base Price) and RBAC (Role-Based Access Control).

---
**Developer:** Armaghan Malik  
📧 [armaghanmalik81@gmail.com](mailto:armaghanmalik81@gmail.com) | 📞 +91 305 5356221
