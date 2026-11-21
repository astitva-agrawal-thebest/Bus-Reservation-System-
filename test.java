import java.util.ArrayList;
import java.util.Scanner;

class Bus {
    private int busId;
    private String busName;
    private String source;
    private String destination;
    private java.math.BigDecimal fare;
    private boolean[] seats;

    public Bus(int busId, String busName, String source,
               String destination, java.math.BigDecimal fare) {
        this.busId = busId;
        this.busName = busName;
        this.source = source;
        this.destination = destination;
        this.fare = fare;
        this.seats = new boolean[20];
    }

    public int getBusId() {
        return busId;
    }

    public String getBusName() {
        return busName;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public java.math.BigDecimal getFare() {
        return fare;
    }

    public boolean isBooked(int seatNumber) {
        return seats[seatNumber - 1];
    }

    public void bookSeat(int seatNumber) {
        seats[seatNumber - 1] = true;
    }

    public void cancelSeat(int seatNumber) {
        seats[seatNumber - 1] = false;
    }

    public void display() {
        System.out.println(
            busId + " | " + busName + " | "
            + source + " -> " + destination
            + " | Rs." + fare
        );
    }

    public void displaySeats() {
        System.out.println("\nSeat Status - " + busName);
        System.out.println("A = Available, B = Booked\n");

        for (int i = 1; i <= 20; i++) {
            String status = isBooked(i) ? "B" : "A";

            System.out.print(
                "Seat " + i + "[" + status + "]\t"
            );

            if (i % 4 == 0) {
                System.out.println();
            }
        }

        System.out.println();
    }
}

class Passenger {
    private String name;
    private int age;
    private String phone;

    public Passenger(String name, int age, String phone) {
        this.name = name;
        this.age = age;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getPhone() {
        return phone;
    }
}

class Ticket {
    private int ticketId;
    private Passenger passenger;
    private Bus bus;
    private int seatNumber;
    private String status;
    private java.math.BigDecimal farePaid;

    public Ticket(int ticketId, Passenger passenger,
                  Bus bus, int seatNumber) {
        this.ticketId = ticketId;
        this.passenger = passenger;
        this.bus = bus;
        this.seatNumber = seatNumber;
        this.status = "Booked";
        this.farePaid = bus.getFare();
    }

    public void setFarePaid(java.math.BigDecimal farePaid) { this.farePaid = farePaid; }

    public int getTicketId() {
        return ticketId;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public Bus getBus() {
        return bus;
    }

    public String getStatus() {
        return status;
    }

    public void cancel() {
        status = "Cancelled";
    }

    public void display() {
        System.out.println("\n======================================");
        System.out.println("             BUS TICKET");
        System.out.println("======================================");
        System.out.println("Ticket ID   : " + ticketId);
        System.out.println("Passenger   : " + passenger.getName());
        System.out.println("Age         : " + passenger.getAge());
        System.out.println("Phone       : " + passenger.getPhone());
        System.out.println("Bus         : " + bus.getBusName());
        System.out.println("Route       : " + bus.getSource()
                           + " -> " + bus.getDestination());
        System.out.println("Seat Number : " + seatNumber);
        System.out.println("Fare        : Rs." + farePaid);
        System.out.println("Status      : " + status);
        System.out.println("======================================");
    }
}

class Reservation {
    private ArrayList<Ticket> tickets;
    private Database database;

    public Reservation() {
        tickets = new ArrayList<>();
        
    }

    public void connect(Database database) { this.database = database; }

    public int addBus(String name, String source, String destination, java.math.BigDecimal fare) throws java.sql.SQLException {
        return database.addBus(name, source, destination, fare);
    }

    public void refresh(ArrayList<Bus> buses) throws java.sql.SQLException {
        database.load(buses, tickets);
    }

    public void book(Bus bus, Passenger passenger, int seatNumber) throws java.sql.SQLException {
        if (passenger.getName().isBlank() || passenger.getPhone().isBlank()
                || passenger.getAge() < 1 || passenger.getAge() > 120
                || passenger.getName().length() > 100 || passenger.getPhone().length() > 20) {
            System.out.println("Enter a name (up to 100 characters), phone (up to 20), and age between 1 and 120.");
            return;
        }

        if (seatNumber < 1 || seatNumber > 20) {
            System.out.println(
                "Invalid seat number. Please choose 1-20."
            );
            return;
        }

        if (bus.isBooked(seatNumber)) {
            System.out.println(
                "Sorry! This seat is already booked."
            );
            return;
        }

        Ticket savedTicket = database.book(bus, passenger, seatNumber);
        bus.bookSeat(seatNumber);

        Ticket ticket = savedTicket;

        tickets.add(ticket);

        System.out.println("\nSeat booked successfully!");
        ticket.display();

    }

    public Ticket findTicket(int ticketId) {

        for (Ticket ticket : tickets) {
            if (ticket.getTicketId() == ticketId) {
                return ticket;
            }
        }

        return null;
    }

    public void cancel(int ticketId) throws java.sql.SQLException {

        Ticket ticket = findTicket(ticketId);

        if (ticket == null) {
            System.out.println("Ticket not found.");
            return;
        }

        if (ticket.getStatus().equals("Cancelled")) {
            System.out.println(
                "This ticket is already cancelled."
            );
            return;
        }

        if (!database.cancel(ticketId)) {
            System.out.println("Ticket is no longer booked. Please try again.");
            return;
        }

        ticket.getBus().cancelSeat(
            ticket.getSeatNumber()
        );

        ticket.cancel();

        System.out.println(
            "Ticket cancelled successfully."
        );

        System.out.println(
            "Seat " + ticket.getSeatNumber()
            + " is available again."
        );
    }

    public void viewTicket(int ticketId) {

        Ticket ticket = findTicket(ticketId);

        if (ticket == null) {
            System.out.println("Ticket not found.");
        } else {
            ticket.display();
        }
    }
}

public class test {

    static Scanner sc = new Scanner(System.in);

    static ArrayList<Bus> buses = new ArrayList<>();
    static Reservation reservation = new Reservation();

    public static void main(String[] args) {

        try {
            try (Database database = new Database()) {
                reservation.connect(database);
                reservation.refresh(buses);
                startSystem();
            }
        } catch (Exception e) {
            System.err.println("Unable to run reservation system: " + e.getMessage());
            System.exit(1);
        }
    }
    public static void startSystem() {

        int choice = 0;

        do {
            System.out.println("\n======================================");
            System.out.println("       BUS RESERVATION SYSTEM");
            System.out.println("======================================");
            System.out.println("1. View Buses");
            System.out.println("2. View Seats");
            System.out.println("3. Book Seat");
            System.out.println("4. Cancel Ticket");
            System.out.println("5. View Ticket");
            System.out.println("6. Add Bus");
            System.out.println("7. Exit");
            System.out.println("======================================");
            System.out.print("Enter your choice: ");

            try {
                if (!sc.hasNextLine()) return;
                reservation.refresh(buses);
                choice = Integer.parseInt(sc.nextLine().trim());

                switch (choice) {

                    case 1:
                        showBuses();
                        break;

                    case 2:
                        showSeats();
                        break;

                    case 3:
                        bookSeat();
                        break;

                    case 4:
                        cancelTicket();
                        break;

                    case 5:
                        viewTicket();
                        break;

                    case 6:
                        addBus();
                        break;

                    case 7:
                        System.out.println(
                            "\nThank you for using the system!"
                        );
                        break;

                    default:
                        System.out.println(
                            "Invalid choice. Please try again."
                        );
                }

            } catch (java.sql.SQLException e) {
                System.out.println("Database operation failed; no success was recorded: " + e.getMessage());
                choice = 0;
            } catch (java.util.NoSuchElementException e) {
                return;
            } catch (IllegalArgumentException e) {

                System.out.println(
                    "Invalid input. Please enter a number."
                );

                choice = 0;
            }

        } while (choice != 7);
    }

    public static void showBuses() {

        System.out.println(
            "\n======== AVAILABLE BUSES =============="
        );

        System.out.println(
            "ID | Bus Name | Route | Fare"
        );

        for (Bus bus : buses) {
            bus.display();
        }
    }

    public static Bus findBus(int busId) {

        for (Bus bus : buses) {

            if (bus.getBusId() == busId) {
                return bus;
            }
        }

        return null;
    }

    public static void showSeats() {

        showBuses();

        System.out.print("\nEnter Bus ID: ");
        int busId = Integer.parseInt(sc.nextLine().trim());

        Bus bus = findBus(busId);

        if (bus == null) {
            System.out.println("Bus not found.");
            return;
        }

        bus.displaySeats();
    }

    public static void bookSeat() throws java.sql.SQLException {

        showBuses();

        System.out.print("\nEnter Bus ID: ");
        int busId = Integer.parseInt(sc.nextLine().trim());

        Bus bus = findBus(busId);

        if (bus == null) {
            System.out.println("Bus not found.");
            return;
        }

        bus.displaySeats();

        System.out.print("\nEnter Seat Number: ");
        int seatNumber = Integer.parseInt(sc.nextLine().trim());

        System.out.print("Enter Passenger Name: ");
        String name = sc.nextLine().trim();

        System.out.print("Enter Passenger Age: ");
        int age = Integer.parseInt(sc.nextLine().trim());

        System.out.print("Enter Phone Number: ");
        String phone = sc.nextLine().trim();

        Passenger passenger =
            new Passenger(name, age, phone);

        reservation.book(
            bus,
            passenger,
            seatNumber
        );
    }

    public static void cancelTicket() throws java.sql.SQLException {

        System.out.print("\nEnter Ticket ID: ");
        int ticketId = Integer.parseInt(sc.nextLine().trim());

        reservation.cancel(ticketId);
    }

    public static void viewTicket() {

        System.out.print("\nEnter Ticket ID: ");
        int ticketId = Integer.parseInt(sc.nextLine().trim());

        reservation.viewTicket(ticketId);
    }

    public static void addBus() throws java.sql.SQLException {
        System.out.print("Bus name: ");
        String name = sc.nextLine().trim();
        System.out.print("Source: ");
        String source = sc.nextLine().trim();
        System.out.print("Destination: ");
        String destination = sc.nextLine().trim();
        System.out.print("Fare: ");
        java.math.BigDecimal fare = new java.math.BigDecimal(sc.nextLine().trim());
        if (name.isBlank() || source.isBlank() || destination.isBlank()
                || name.length() > 80 || source.length() > 80 || destination.length() > 80
                || source.equalsIgnoreCase(destination) || fare.signum() <= 0
                || fare.compareTo(new java.math.BigDecimal("99999999.99")) > 0
                || fare.stripTrailingZeros().scale() > 2) {
            System.out.println("Enter nonempty names up to 80 characters, different cities, and a positive fare with at most two decimal places.");
            return;
        }
        int id = reservation.addBus(name, source, destination, fare);
        System.out.println("Bus added with ID " + id + " and 20 available seats.");
    }
}

class Database implements AutoCloseable {
    private final java.sql.Connection connection;

    public Database() throws Exception {
        java.util.Properties settings = new java.util.Properties();
        try (java.io.InputStream input = java.nio.file.Files.newInputStream(
                java.nio.file.Path.of("database.properties"))) {
            settings.load(input);
        }
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = java.sql.DriverManager.getConnection(
            settings.getProperty("url"), settings.getProperty("user"), settings.getProperty("password"));
        connection.setTransactionIsolation(java.sql.Connection.TRANSACTION_REPEATABLE_READ);
    }

    public void load(ArrayList<Bus> buses, ArrayList<Ticket> tickets) throws java.sql.SQLException {
        ArrayList<Bus> loadedBuses = new ArrayList<>();
        ArrayList<Ticket> loadedTickets = new ArrayList<>();
        java.util.Map<Integer, Bus> byId = new java.util.HashMap<>();
        connection.setAutoCommit(false);
        try {
            try (java.sql.Statement statement = connection.createStatement();
                 java.sql.ResultSet rows = statement.executeQuery("SELECT * FROM buses ORDER BY bus_id")) {
                while (rows.next()) {
                    Bus bus = new Bus(rows.getInt("bus_id"), rows.getString("bus_name"),
                        rows.getString("source"), rows.getString("destination"), rows.getBigDecimal("fare"));
                    loadedBuses.add(bus);
                    byId.put(bus.getBusId(), bus);
                }
            }
            try (java.sql.Statement statement = connection.createStatement();
                 java.sql.ResultSet rows = statement.executeQuery("SELECT * FROM ticket_details ORDER BY ticket_id")) {
                while (rows.next()) {
                    Bus bus = byId.get(rows.getInt("bus_id"));
                    Passenger passenger = new Passenger(rows.getString("passenger_name"),
                        rows.getInt("age"), rows.getString("phone"));
                    Ticket ticket = new Ticket(rows.getInt("ticket_id"), passenger, bus, rows.getInt("seat_number"));
                    ticket.setFarePaid(rows.getBigDecimal("fare_paid"));
                    if (rows.getString("status").equals("Cancelled")) {
                        ticket.cancel();
                    } else {
                        bus.bookSeat(ticket.getSeatNumber());
                    }
                    loadedTickets.add(ticket);
                }
            }
            connection.commit();
            buses.clear();
            buses.addAll(loadedBuses);
            tickets.clear();
            tickets.addAll(loadedTickets);
        } catch (java.sql.SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private int generatedId(java.sql.PreparedStatement statement) throws java.sql.SQLException {
        try (java.sql.ResultSet keys = statement.getGeneratedKeys()) {
            if (!keys.next()) throw new java.sql.SQLException("No generated ID returned.");
            return keys.getInt(1);
        }
    }

    public int addBus(String name, String source, String destination, java.math.BigDecimal fare)
            throws java.sql.SQLException {
        String sql = "INSERT INTO buses (bus_name,source,destination,fare) VALUES (?,?,?,?)";
        try (java.sql.PreparedStatement statement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, source);
            statement.setString(3, destination);
            statement.setBigDecimal(4, fare);
            statement.executeUpdate();
            return generatedId(statement);
        }
    }

    public Ticket book(Bus bus, Passenger passenger, int seatNumber) throws java.sql.SQLException {
        connection.setAutoCommit(false);
        try {
            java.math.BigDecimal fare;
            try (java.sql.PreparedStatement statement = connection.prepareStatement(
                    "SELECT fare FROM buses WHERE bus_id=? FOR UPDATE")) {
                statement.setInt(1, bus.getBusId());
                try (java.sql.ResultSet rows = statement.executeQuery()) {
                    if (!rows.next()) throw new java.sql.SQLException("Bus no longer exists.");
                    fare = rows.getBigDecimal(1);
                }
            }
            int passengerId;
            try (java.sql.PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO passengers (passenger_name,age,phone) VALUES (?,?,?)", java.sql.Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, passenger.getName());
                statement.setInt(2, passenger.getAge());
                statement.setString(3, passenger.getPhone());
                statement.executeUpdate();
                passengerId = generatedId(statement);
            }
            int ticketId;
            try (java.sql.PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO tickets (passenger_id,bus_id,seat_number,fare_paid) VALUES (?,?,?,?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, passengerId);
                statement.setInt(2, bus.getBusId());
                statement.setInt(3, seatNumber);
                statement.setBigDecimal(4, fare);
                statement.executeUpdate();
                ticketId = generatedId(statement);
            }
            connection.commit();
            Ticket ticket = new Ticket(ticketId, passenger, bus, seatNumber);
            ticket.setFarePaid(fare);
            return ticket;
        } catch (java.sql.SQLException e) {
            connection.rollback();
            if (e.getErrorCode() == 1062) {
                throw new java.sql.SQLException("This seat has already been booked. Choose another seat.", e);
            }
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public boolean cancel(int ticketId) throws java.sql.SQLException {
        try (java.sql.PreparedStatement statement = connection.prepareStatement(
                "UPDATE tickets SET status='Cancelled' WHERE ticket_id=? AND status='Booked'")) {
            statement.setInt(1, ticketId);
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public void close() throws java.sql.SQLException {
        connection.close();
    }
}
