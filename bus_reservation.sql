CREATE DATABASE bus_reservation_app;
USE bus_reservation_app;

CREATE TABLE buses (
    bus_id INT PRIMARY KEY AUTO_INCREMENT,
    bus_name VARCHAR(80) NOT NULL,
    source VARCHAR(80) NOT NULL,
    destination VARCHAR(80) NOT NULL,
    fare DECIMAL(10,2) NOT NULL,
    CONSTRAINT chk_bus_name CHECK (CHAR_LENGTH(TRIM(bus_name)) > 0),
    CONSTRAINT chk_route CHECK (CHAR_LENGTH(TRIM(source)) > 0
        AND CHAR_LENGTH(TRIM(destination)) > 0 AND source <> destination),
    CONSTRAINT chk_fare CHECK (fare > 0)
) ;

CREATE TABLE passengers (
    passenger_id INT PRIMARY KEY AUTO_INCREMENT,
    passenger_name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    phone VARCHAR(20) NOT NULL,
    CONSTRAINT chk_name CHECK (CHAR_LENGTH(TRIM(passenger_name)) > 0),
    CONSTRAINT chk_age CHECK (age BETWEEN 1 AND 120),
    CONSTRAINT chk_phone CHECK (CHAR_LENGTH(TRIM(phone)) > 0)
) ;

CREATE TABLE seat_numbers (
    seat_number INT PRIMARY KEY,
    CONSTRAINT chk_seat CHECK (seat_number BETWEEN 1 AND 20)
) ;

CREATE TABLE tickets (
    ticket_id INT PRIMARY KEY AUTO_INCREMENT,
    passenger_id INT NOT NULL,
    bus_id INT NOT NULL,
    seat_number INT NOT NULL,
    fare_paid DECIMAL(10,2) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'Booked',
    booked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    active_seat INT GENERATED ALWAYS AS
        (CASE WHEN status = 'Booked' THEN seat_number ELSE NULL END) STORED,
    CONSTRAINT fk_ticket_passenger FOREIGN KEY (passenger_id)
        REFERENCES passengers(passenger_id),
    CONSTRAINT fk_ticket_bus FOREIGN KEY (bus_id) REFERENCES buses(bus_id),
    CONSTRAINT fk_ticket_seat FOREIGN KEY (seat_number)
        REFERENCES seat_numbers(seat_number),
    CONSTRAINT chk_status CHECK (status IN ('Booked', 'Cancelled')),
    CONSTRAINT chk_paid CHECK (fare_paid > 0),
    CONSTRAINT uq_active_seat UNIQUE (bus_id, active_seat)
) ;

INSERT INTO buses (bus_name, source, destination, fare) VALUES
('Goldline', 'Jaipur', 'Delhi', 2000),
('Maharani', 'Jaipur', 'Delhi', 1500),
('Laxmi Travels', 'Jaipur', 'Delhi', 2200),
('Ram Dalal', 'Jaipur', 'Delhi', 1700);

INSERT INTO seat_numbers VALUES
(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),
(11),(12),(13),(14),(15),(16),(17),(18),(19),(20);

CREATE VIEW ticket_details AS
SELECT t.ticket_id, p.passenger_name, p.age, p.phone,
       b.bus_id, b.bus_name, b.source, b.destination,
       t.seat_number, t.fare_paid, t.status, t.booked_at
FROM tickets t
JOIN passengers p ON p.passenger_id = t.passenger_id
JOIN buses b ON b.bus_id = t.bus_id;

CREATE VIEW seat_status AS
SELECT b.bus_id, b.bus_name, s.seat_number,
       CASE WHEN t.ticket_id IS NULL THEN 'Available' ELSE 'Booked' END AS status
FROM buses b
CROSS JOIN seat_numbers s
LEFT JOIN tickets t ON t.bus_id = b.bus_id
    AND t.active_seat = s.seat_number;

-- DROP DATABASE bus_reservation_app;