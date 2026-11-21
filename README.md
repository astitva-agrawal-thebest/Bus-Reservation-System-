# 🚌 Bus Reservation System

A console-based bus reservation application written in **Java** and backed by a **MySQL** database via JDBC. Users can browse buses, view live seat availability, book and cancel tickets, and add new buses, with all data persisted in the database.

Built as a project for the RDBMS Lab (CSC2131) at Manipal University Jaipur.

---

## ✨ Features

- **View buses**: list all buses with route and fare
- **View seats**: 20-seat layout per bus showing available (`A`) and booked (`B`) seats
- **Book a seat**: enter passenger name, age and phone; a ticket is generated instantly
- **Cancel a ticket**: frees the seat so it can be booked again
- **View ticket**: look up any ticket by its ID
- **Add a bus**: create a new bus with name, route and fare
- **Input validation** in both the Java layer and the database (`CHECK` constraints)
- **Double-booking protection** enforced by the database, even if two users book at the same time

---

## 🛠️ Tech Stack

| Layer      | Technology                         |
|------------|------------------------------------|
| Language   | Java (uses `Path.of` / `isBlank`, so JDK 11+) |
| Database   | MySQL 8.0+ (needs generated columns and `CHECK` constraints) |
| Connectivity | JDBC with MySQL Connector/J      |

---

## 📁 Project Structure

```
.
├── test.java                          # Source code (all classes + main menu)
├── bus_reservation.sql                # Database schema, sample data and views
├── database.properties                # DB connection settings (URL, user, password)
└── mysql-connector-j-26_10_0.jar      # MySQL JDBC driver
```

### Classes (in `test.java`)

| Class         | Responsibility                                                        |
|---------------|-----------------------------------------------------------------------|
| `Bus`         | Bus details and the in-memory 20-seat availability array              |
| `Passenger`   | Passenger name, age and phone                                         |
| `Ticket`      | Ticket details, status (`Booked` / `Cancelled`) and ticket printout   |
| `Reservation` | Business logic: validation, booking, cancelling, ticket lookup        |
| `Database`    | All JDBC code: loading data, transactions, inserts and updates        |
| `test`        | Entry point with the interactive menu                                 |

---

## 🗄️ Database Design

**Database name:** `bus_reservation_app`

### Tables

| Table          | Purpose                                         | Key columns |
|----------------|-------------------------------------------------|-------------|
| `buses`        | Bus name, route and fare                        | `bus_id` (PK), `bus_name`, `source`, `destination`, `fare` |
| `passengers`   | Passenger details                               | `passenger_id` (PK), `passenger_name`, `age`, `phone` |
| `seat_numbers` | Valid seat numbers (1–20)                       | `seat_number` (PK) |
| `tickets`      | Bookings linking passenger, bus and seat        | `ticket_id` (PK), `passenger_id` (FK), `bus_id` (FK), `seat_number` (FK), `fare_paid`, `status`, `booked_at` |

### Views

- **`ticket_details`**: joins tickets, passengers and buses into one readable record
- **`seat_status`**: shows every seat of every bus as `Available` or `Booked`

### Integrity rules

- `fare > 0`, `age BETWEEN 1 AND 120`, seat number `BETWEEN 1 AND 20`
- Source and destination of a bus must be different, and names must not be blank
- Ticket `status` can only be `Booked` or `Cancelled`
- **No double booking:** `tickets` has a generated column `active_seat` that holds the seat number only while the ticket is `Booked`, plus a `UNIQUE (bus_id, active_seat)` constraint. A seat can therefore be held by only one active ticket per bus, while cancelled tickets stay in history and the seat becomes bookable again.

### Sample data

The script seeds four Jaipur → Delhi buses:

| Bus           | Fare (₹) |
|---------------|----------|
| Goldline      | 2000     |
| Maharani      | 1500     |
| Laxmi Travels | 2200     |
| Ram Dalal     | 1700     |

---

## 🚀 Getting Started

### Prerequisites

- JDK 11 or later
- MySQL Server 8.0 or later, running locally (or reachable over the network)

### 1. Set up the database

```bash
mysql -u root -p < bus_reservation.sql
```

Or open `bus_reservation.sql` in MySQL Workbench and run it.

### 2. Configure the connection

Edit `database.properties` to match your MySQL setup:

```properties
url=jdbc:mysql://localhost:3306/bus_reservation_app
user=root
password=your_password_here
```

> ⚠️ **Do not commit real passwords to GitHub.** Keep your own password out of the repo, for example by adding `database.properties` to `.gitignore` and committing a `database.properties.example` instead.

### 3. Compile

**Windows**
```bash
javac -cp ".;mysql-connector-j-26_10_0.jar" test.java
```

**Linux / macOS**
```bash
javac -cp ".:mysql-connector-j-26_10_0.jar" test.java
```

### 4. Run

Run it from the folder that contains `database.properties`, because the program reads that file from the current directory.

**Windows**
```bash
java -cp ".;mysql-connector-j-26_10_0.jar" test
```

**Linux / macOS**
```bash
java -cp ".:mysql-connector-j-26_10_0.jar" test
```

---

## 🖥️ Usage

```
======================================
       BUS RESERVATION SYSTEM
======================================
1. View Buses
2. View Seats
3. Book Seat
4. Cancel Ticket
5. View Ticket
6. Add Bus
7. Exit
======================================
Enter your choice:
```

### Example: viewing seats

```
Seat Status - Goldline
A = Available, B = Booked

Seat 1[A]   Seat 2[A]   Seat 3[B]   Seat 4[A]
Seat 5[A]   Seat 6[A]   Seat 7[A]   Seat 8[A]
...
```

### Example: a booked ticket

```
======================================
             BUS TICKET
======================================
Ticket ID   : 1
Passenger   : Rahul Sharma
Age         : 21
Phone       : 9876543210
Bus         : Goldline
Route       : Jaipur -> Delhi
Seat Number : 3
Fare        : Rs.2000.00
Status      : Booked
======================================
```

---

## 🔒 How Booking Stays Safe

- Every booking runs inside a **database transaction** and rolls back if any step fails
- The bus row is locked with `SELECT ... FOR UPDATE` while the fare is read, so the fare charged is the fare stored in the database
- If two users try to book the same seat, the unique constraint rejects the second one (MySQL error 1062) and the app shows *"This seat has already been booked."*
- All queries use **`PreparedStatement`**, which protects against SQL injection
- The app reloads data from the database before each menu action, so seat availability always reflects the latest state

---

## 🔮 Possible Improvements

- User login and admin roles
- Travel dates and times, so the same bus can run on multiple days
- Search buses by source and destination
- Configurable seat count per bus
- GUI or web front-end
- Move credentials to environment variables

---

## 📄 License

This project was created for educational purposes. Add a license of your choice (for example MIT) before publishing if you want others to reuse it.
