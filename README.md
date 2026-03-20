# Bus Management System

## **Introduction**
The **Bus Management System** is a professional desktop application built with **JavaFX**. It provides a centralized platform for transportation logistics, offering tailored interfaces for Admins, Managers, and Passengers. The system ensures a seamless flow from route definition to seat booking and payment verification.

---

## **Features**
- **Role-Based Access Control (RBAC):** Distinct portals for Admin, Manager, and Passenger roles.
- **Dynamic Fleet Management:** Admin-controlled route creation and base pricing.
- **Interactive Booking:** Visual seat selection for passengers with real-time availability.
- **Financial Workflow:** Managerial oversight for payment verification and flexible pricing.
- **Automated Ticketing:** Instant ticket generation upon booking approval.
- **Data Persistence:** Robust backend integration using the H2 Database Engine.

---

## **Technologies Used**
- **Programming Language:** Java (Core)
- **GUI Framework:** JavaFX / FXML (Scene Builder)
- **Database:** H2 Database Engine (`bus_system_db`)
- **Architecture:** MVC (Model-View-Controller) logic
- **Security:** Encrypted Role-Based Login

---

## **Project Visuals & Logic**

### **1. Entry Point**
The application starts with a unified Welcome Screen where users select their specific role to access their respective dashboards.

![Welcome Dashboard](screenshots/Screenshot%202026-03-20%20160809.png)

---

### **2. Admin Portal (Master Control)**
The Admin acts as the system architect, managing the personnel and the fundamental constraints of the fleet.
- **Manager Management:** Create and manage manager credentials.
- **Fleet Definition:** Define routes and set "floor" (base) prices that cannot be undercut.

| Admin Login | Manager List | Bus Schedules |
| :---: | :---: | :---: |
| ![Admin Login](screenshots/Screenshot%202026-03-20%20160911.png) | ![Managers](screenshots/Screenshot%202026-03-20%20161101.png) | ![Buses](screenshots/Screenshot%202026-03-20%20161326.png) |

---

### **3. Manager Portal (Verification & Pricing)**
Managers handle the day-to-day operations and financial adjustments.
- **Approval Workflow:** Review passenger payment screenshots to confirm seat reservations.
- **Price Synchronization:** Adjust seat prices dynamically (Validation: Price must be $\ge$ Admin Base Price).

| Manager Login | Booking Requests | Pricing Logic |
| :---: | :---: | :---: |
| ![Manager Login](screenshots/Screenshot%202026-03-20%20161722.png) | ![Requests](screenshots/Screenshot%202026-03-20%20161951.png) | ![Pricing](screenshots/Screenshot%202026-03-20%20162305.png) |

---

### **4. Passenger Journey (Booking Flow)**
A logical, four-step process designed for ease of use:
1.  **Search:** Query buses based on source and destination.
2.  **Seat Selection:** Interactive UI to pick specific seats.
3.  **Payment:** Upload transaction proof for verification.
4.  **History:** Track status (Pending/Approved/Rejected) in real-time.

| 1. Search Bus | 2. Seat Selection | 3. Payment Upload | 4. Ticket History |
| :---: | :---: | :---: | :---: |
| ![Search](screenshots/Screenshot%202026-03-20%20174803.png) | ![Seats](screenshots/Screenshot%202026-03-20%20174853.png) | ![Payment](screenshots/Screenshot%202026-03-20%20174950.png) | ![History](screenshots/Screenshot%202026-03-20%20175035.png) |

---

## **Core Logic & Implementation**
### **Database Schema**
The system utilizes **H2** for lightweight, high-performance data storage. Key tables include:
- `Users`: Stores credentials and roles.
- `Buses/Routes`: Stores fleet information and Admin price constraints.
- `Bookings`: Tracks seat status and payment references.

### **Price Validation Formula**
To maintain profitability, the system enforces a strict pricing logic:
$$Price_{Manager} \geq Price_{Admin\_Base}$$

---

## **Conclusion**
The **Bus Management System** demonstrates the power of JavaFX in creating professional enterprise tools. By separating concerns between Admin, Manager, and Passenger, it provides a secure and scalable solution for modern transport management.

---

## **Contact**
For inquiries or collaborations:
- **Developer:** Armaghan Malik
- **Email:** [armaghanmalik81@gmail.com](mailto:armaghanmalik81@gmail.com)
- **Phone:** +91 305 5356221
- **LinkedIn:** [View Profile](https://www.linkedin.com/in/malik-armaghan-4629493aa)

---

## **License**
> **This project is owned by Armaghan Malik and is intended for educational purposes. All rights reserved.**
