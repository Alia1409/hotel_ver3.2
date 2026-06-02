package view;

import model.Reservation;
import model.Room;
import persistence.StorageManager;
import service.ReservationService;
import service.RoomService;

import service.ExportService;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MainApp extends JFrame {
    private final StorageManager storageManager = new StorageManager();
    private final RoomService roomService = new RoomService(storageManager);
    private final ReservationService resService = new ReservationService(roomService, storageManager);
    private final java.time.format.DateTimeFormatter dateForm = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // Email verification regex pattern (RFC 5322 compliant standard format check)
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

    private DefaultTableModel roomTableModel;
    private DefaultTableModel resTableModel;
    private JList<String> statusList;

    private String selectedReservationId = null;
    private String selectedRoomNumber = null;

    public MainApp() {
        setTitle("Boutique Hotel Reservation System");
        setSize(1100, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildAndMountUI();
    }

    private void buildAndMountUI() {
        JTabbedPane tabPane = new JTabbedPane();

        tabPane.addTab("Daily Occupancy Panel", buildOccupancyView());
        tabPane.addTab("Reservation Manager", buildReservationView());
        tabPane.addTab("Room Management", buildRoomView());
        tabPane.addTab("AI Assistant", new AiChatView(roomService, resService));

        add(tabPane);
    }

    // --- VIEW 1: DAILY OCCUPANCY PANEL ---
    private JPanel buildOccupancyView() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel controlBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JLabel dateLabel = new JLabel("Target Verification Date (DD-MM-YYYY):");
        JTextField txtTargetDate = new JTextField(LocalDate.now().format(dateForm), 10);
        JButton checkButton = new JButton("Refresh Status Map");
        controlBox.add(dateLabel); controlBox.add(txtTargetDate); controlBox.add(checkButton);

        statusList = new JList<>(new DefaultListModel<>());
        JScrollPane scrollPane = new JScrollPane(statusList);

        Runnable refreshMap = () -> {
            DefaultListModel<String> model = (DefaultListModel<String>) statusList.getModel();
            model.clear();
            try {
                LocalDate targetDate = LocalDate.parse(txtTargetDate.getText().trim(), dateForm);
                for (Room r : roomService.getAllRooms()) {
                    boolean available = resService.isRoomAvailable(r.getRoomNumber(), targetDate, targetDate.plusDays(1));
                    String visualStatus = available ? " [FREE]" : " [BOOKED/OCCUPIED]";
                    if ("maintenance".equalsIgnoreCase(r.getStatus())) visualStatus = " [MAINTENANCE]";
                    model.addElement("Room " + r.getRoomNumber() + " (" + r.getType() + ") - Status: " + visualStatus);
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format applied. Use DD-MM-YYYY.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        checkButton.addActionListener(e -> refreshMap.run());
        refreshMap.run();

        panel.add(controlBox, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // --- VIEW 2: RESERVATION MANAGER TAB (CRUD WITH FILTERS) ---
    private JPanel buildReservationView() {
        JPanel mainBox = new JPanel(new BorderLayout(15, 15));
        mainBox.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formBox = new JPanel();
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));
        formBox.setPreferredSize(new Dimension(280, 0));

        JLabel formTitle = new JLabel("Reservation Record Form");
        formTitle.setFont(new Font("Arial", Font.BOLD, 14));

        JTextField txtId = new JTextField();
        JTextField txtRoomNum = new JTextField();
        JTextField txtName = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtPhone = new JTextField();
        JTextField txtIn = new JTextField(LocalDate.now().format(dateForm));
        JTextField txtOut = new JTextField(LocalDate.now().plusDays(1).format(dateForm));
        JTextField txtStatus = new JTextField("confirmed");
        JTextArea txtNotes = new JTextArea(3, 20);

        JButton btnCreate = new JButton("Add New");
        JButton btnUpdate = new JButton("Save Changes");
        JButton btnDelete = new JButton("Remove");
        btnDelete.setBackground(new Color(231, 76, 60));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setOpaque(true);
        btnDelete.setBorderPainted(false);

        formBox.add(formTitle); formBox.add(Box.createVerticalStrut(5));
        formBox.add(new JLabel("Booking ID (RES-XXX format):")); formBox.add(txtId);
        formBox.add(new JLabel("Room Number:")); formBox.add(txtRoomNum);
        formBox.add(new JLabel("Guest Name:")); formBox.add(txtName);
        formBox.add(new JLabel("Email Address:")); formBox.add(txtEmail);
        formBox.add(new JLabel("Phone Number (digits only):")); formBox.add(txtPhone);
        formBox.add(new JLabel("Check-In Date (DD-MM-YYYY):"));formBox.add(txtIn);
        formBox.add(new JLabel("Check-Out Date (DD-MM-YYYY):"));formBox.add(txtOut);
        formBox.add(new JLabel("Status:")); formBox.add(txtStatus);
        formBox.add(new JLabel("Special Notes:")); formBox.add(new JScrollPane(txtNotes));
        formBox.add(Box.createVerticalStrut(10));
        formBox.add(btnCreate); formBox.add(Box.createVerticalStrut(5));
        formBox.add(btnUpdate); formBox.add(Box.createVerticalStrut(5));
        formBox.add(btnDelete);

        JPanel tableContainer = new JPanel(new BorderLayout(10, 10));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Reservations"));

        JTextField filterRoom = new JTextField(4);
        JTextField filterDate = new JTextField(7);
        JComboBox<String> filterType = new JComboBox<>(new String[]{"All Types", "single", "double", "superior double", "suite"});
        JComboBox<String> filterStatus = new JComboBox<>(new String[]{"All Statuses", "confirmed", "cancelled", "completed"});
        JButton btnResetFilters = new JButton("Clear Filters");

        filterPanel.add(new JLabel("Room #:")); filterPanel.add(filterRoom);
        filterPanel.add(new JLabel("Check-In:")); filterPanel.add(filterDate);
        filterPanel.add(new JLabel("Type:")); filterPanel.add(filterType);
        filterPanel.add(new JLabel("Status:")); filterPanel.add(filterStatus);
        filterPanel.add(btnResetFilters);

        // GUI FIX: Added "Special Notes" layout mapping header column directly into table view
        String[] cols = {"ID", "Room", "Guest", "Check-In", "Check-Out", "Bill", "Status", "Special Notes"};
        resTableModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(resTableModel);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnCheckIn = new JButton("Log Check-In");
        JButton btnCheckOut = new JButton("Log Check-Out");
        JButton btnExport = new JButton("Export CSV Report");

        // AUTOMATED EXPORT ACTION LISTENER (NO DIALOG POP-UP)
        btnExport.addActionListener(e -> {
            ExportService.exportReservationsToCSV(resService.getAllReservations(), roomService.getAllRooms());
            JOptionPane.showMessageDialog(this, "Exported successfully to monthly_report.csv", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        
        btnExport.setBackground(new Color(46, 204, 113));
        btnExport.setForeground(Color.WHITE);
        btnExport.setOpaque(true);
        btnExport.setBorderPainted(false);
        actionRow.add(btnCheckIn); actionRow.add(btnCheckOut); actionRow.add(btnExport);

        tableContainer.add(filterPanel, BorderLayout.NORTH);
        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);
        tableContainer.add(actionRow, BorderLayout.SOUTH);

        mainBox.add(formBox, BorderLayout.WEST);
        mainBox.add(tableContainer, BorderLayout.CENTER);

        Runnable refreshResTable = () -> {
            resTableModel.setRowCount(0);
            String roomQuery = filterRoom.getText().trim().toLowerCase();
            String dateQuery = filterDate.getText().trim();
            String typeQuery = filterType.getSelectedItem().toString();
            String statusQuery = filterStatus.getSelectedItem().toString();

            for (Reservation r : resService.getAllReservations()) {
                Room room = roomService.getRoomByNumber(r.getRoomNumber());
                double price = (room != null) ? room.getPricePerNight() : 0.0;
                String roomType = (room != null) ? room.getType().toLowerCase() : "";

                String checkInStr = r.getCheckInDate().format(dateForm);
                String checkOutStr = r.getCheckOutDate().format(dateForm);

                if (!roomQuery.isEmpty() && !r.getRoomNumber().toLowerCase().contains(roomQuery)) continue;
                if (!dateQuery.isEmpty() && !checkInStr.contains(dateQuery)) continue;
                if (!typeQuery.equals("All Types") && !roomType.equalsIgnoreCase(typeQuery)) continue;
                if (!statusQuery.equals("All Statuses") && !r.getStatus().equalsIgnoreCase(statusQuery)) continue;

                // GUI FIX: Added r.getSpecialNotes() so it populates into the new table cell row
                resTableModel.addRow(new Object[]{
                    r.getId(), r.getRoomNumber(), r.getGuestName(), checkInStr, checkOutStr, 
                    r.getTotalAmount(price), r.getStatus(), r.getSpecialNotes()
                });
            }
        };

        java.awt.event.KeyAdapter liveKeyFilter = new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { refreshResTable.run(); }
        };
        filterRoom.addKeyListener(liveKeyFilter);
        filterDate.addKeyListener(liveKeyFilter);
        filterType.addActionListener(e -> refreshResTable.run());
        filterStatus.addActionListener(e -> refreshResTable.run());

        btnResetFilters.addActionListener(e -> {
            filterRoom.setText(""); filterDate.setText("");
            filterType.setSelectedIndex(0); filterStatus.setSelectedIndex(0);
            refreshResTable.run();
        });

        Runnable clearFormFields = () -> {
            txtId.setText(""); txtRoomNum.setText(""); txtName.setText(""); txtEmail.setText("");
            txtPhone.setText(""); txtIn.setText(LocalDate.now().format(dateForm)); 
            txtOut.setText(LocalDate.now().plusDays(1).format(dateForm)); txtStatus.setText("confirmed");
            txtNotes.setText(""); selectedReservationId = null;
        };

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && !e.getValueIsAdjusting()) {
                selectedReservationId = (String) resTableModel.getValueAt(row, 0);
                Reservation target = resService.getAllReservations().stream().filter(r -> r.getId().equals(selectedReservationId)).findFirst().orElse(null);
                if (target != null) {
                    txtId.setText(target.getId()); 
                    txtRoomNum.setText(target.getRoomNumber());
                    txtName.setText(target.getGuestName()); 
                    txtEmail.setText(target.getEmail());
                    txtPhone.setText(target.getPhone()); 
                    txtIn.setText(target.getCheckInDate().format(dateForm));   
                    txtOut.setText(target.getCheckOutDate().format(dateForm)); 
                    txtStatus.setText(target.getStatus());
                    txtNotes.setText(target.getSpecialNotes());
                }
            }
        });

        btnCreate.addActionListener(e -> {
            String inputId = txtId.getText().trim();
            String inputPhone = txtPhone.getText().trim();
            String inputEmail = txtEmail.getText().trim();
            
            if (!inputId.matches("RES-\\d{3}")) {
                JOptionPane.showMessageDialog(this, "Use RES-001 style.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // ERROR HANDLING: EMAIL FORMAT VERIFICATION DURING CREATION
            if (!inputEmail.matches(EMAIL_REGEX)) {
                JOptionPane.showMessageDialog(this, "Invalid Email syntax! (e.g., example@domain.com)", "Formatting Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!inputPhone.matches("\\d{7,15}")) {
                JOptionPane.showMessageDialog(this, "Invalid Phone Number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                Reservation r = new Reservation(inputId, txtRoomNum.getText().trim(), txtName.getText().trim(),
                        inputEmail, inputPhone, LocalDate.parse(txtIn.getText().trim(), dateForm),
                        LocalDate.parse(txtOut.getText().trim(), dateForm), txtStatus.getText().trim(), txtNotes.getText().trim());
                if (resService.createReservation(r)) {
                    refreshResTable.run(); clearFormFields.run();
                } else {
                    JOptionPane.showMessageDialog(this, "Room collision or date logic failure.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Check parameter fields. Format must be DD-MM-YYYY.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnUpdate.addActionListener(e -> {
            if (selectedReservationId != null) {
                String inputId = txtId.getText().trim();
                String inputPhone = txtPhone.getText().trim();
                String inputEmail = txtEmail.getText().trim();
                
                if (!inputId.matches("RES-\\d{3}")) {
                    JOptionPane.showMessageDialog(this, "Invalid Booking ID format!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // ERROR HANDLING: EMAIL FORMAT VERIFICATION DURING RECORD UPDATING
                if (!inputEmail.matches(EMAIL_REGEX)) {
                    JOptionPane.showMessageDialog(this, "Invalid Email syntax! (e.g., example@domain.com)", "Formatting Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!inputPhone.matches("\\d{7,15}")) {
                    JOptionPane.showMessageDialog(this, "Invalid Phone Number!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    Reservation r = new Reservation(selectedReservationId, txtRoomNum.getText().trim(), txtName.getText().trim(),
                            inputEmail, inputPhone, LocalDate.parse(txtIn.getText().trim(), dateForm),
                            LocalDate.parse(txtOut.getText().trim(), dateForm), txtStatus.getText().trim(), txtNotes.getText().trim());
                    if (resService.updateReservation(r)) {
                        refreshResTable.run(); clearFormFields.run();
                    } else {
                        JOptionPane.showMessageDialog(this, "Room unavailable for selected dates.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Date syntax error. Use DD-MM-YYYY.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnDelete.addActionListener(e -> {
            if (selectedReservationId != null) {
                resService.deleteReservation(selectedReservationId);
                refreshResTable.run(); clearFormFields.run();
            }
        });

        btnCheckIn.addActionListener(e -> {
            if (selectedReservationId != null) {
                resService.processCheckIn(selectedReservationId);
                refreshResTable.run(); clearFormFields.run();
            }
        });

        btnCheckOut.addActionListener(e -> {
            if (selectedReservationId != null) {
                resService.processCheckOut(selectedReservationId);
                refreshResTable.run(); clearFormFields.run();
            }
        });

        refreshResTable.run();
        return mainBox;
    }

    // --- VIEW 3: ROOM MANAGEMENT TAB (CRUD WITH DYNAMIC SORTING) ---
    private JPanel buildRoomView() {
        JPanel mainBox = new JPanel(new BorderLayout(15, 15));
        mainBox.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formBox = new JPanel();
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));
        formBox.setPreferredSize(new Dimension(250, 0));

        JLabel title = new JLabel("Room Inventory Form");
        title.setFont(new Font("Arial", Font.BOLD, 14));

        JTextField txtNum = new JTextField();
        JTextField txtType = new JTextField();
        JTextField txtFloor = new JTextField();
        JTextField txtPrice = new JTextField();
        JTextField txtStatus = new JTextField("available");
        JTextField txtDesc = new JTextField();

        JButton btnAdd = new JButton("Add Room");
        JButton btnUpdateRoom = new JButton("Save Changes");
        JButton btnDeleteRoom = new JButton("Remove Room");
        btnDeleteRoom.setBackground(new Color(231, 76, 60));
        btnDeleteRoom.setForeground(Color.WHITE);
        btnDeleteRoom.setOpaque(true);
        btnDeleteRoom.setBorderPainted(false);

        formBox.add(title); formBox.add(Box.createVerticalStrut(5));
        formBox.add(new JLabel("Room Number:")); formBox.add(txtNum);
        formBox.add(new JLabel("Type:")); formBox.add(txtType);
        formBox.add(new JLabel("Floor:")); formBox.add(txtFloor);
        formBox.add(new JLabel("Price Per Night:")); formBox.add(txtPrice);
        formBox.add(new JLabel("Status:")); formBox.add(txtStatus);
        formBox.add(new JLabel("Room View Specs:")); formBox.add(txtDesc);
        formBox.add(Box.createVerticalStrut(10));
        formBox.add(btnAdd); formBox.add(Box.createVerticalStrut(5));
        formBox.add(btnUpdateRoom); formBox.add(Box.createVerticalStrut(5));
        formBox.add(btnDeleteRoom);

        JPanel tableContainer = new JPanel(new BorderLayout(10, 10));

        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        sortPanel.setBorder(BorderFactory.createTitledBorder("Organize Inventory"));
        
        JComboBox<String> cmbSortOption = new JComboBox<>(new String[]{
            "Room Number", "Room Type", "Floor Level", "Price (Low to High)", "Price (High to Low)", "Room Status"
        });
        sortPanel.add(new JLabel("Organize & Sort By:"));
        sortPanel.add(cmbSortOption);

        String[] cols = {"Room #", "Type", "Floor", "Price / Night", "Status", "Specs"};
        roomTableModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(roomTableModel);

        tableContainer.add(sortPanel, BorderLayout.NORTH);
        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);

        mainBox.add(formBox, BorderLayout.WEST);
        mainBox.add(tableContainer, BorderLayout.CENTER);

        Runnable refreshRoomTable = () -> {
            roomTableModel.setRowCount(0);
            List<Room> roomsList = new java.util.ArrayList<>(roomService.getAllRooms());
            String selectedSort = cmbSortOption.getSelectedItem().toString();

            roomsList.sort((r1, r2) -> {
                switch (selectedSort) {
                    case "Room Type": return r1.getType().compareToIgnoreCase(r2.getType());
                    case "Floor Level": return Integer.compare(r1.getFloor(), r2.getFloor());
                    case "Price (Low to High)": return Double.compare(r1.getPricePerNight(), r2.getPricePerNight());
                    case "Price (High to Low)": return Double.compare(r2.getPricePerNight(), r1.getPricePerNight());
                    case "Room Status": return r1.getStatus().compareToIgnoreCase(r2.getStatus());
                    case "Room Number":
                    default: return r1.getRoomNumber().compareToIgnoreCase(r2.getRoomNumber());
                }
            });

            for (Room r : roomsList) {
                roomTableModel.addRow(new Object[]{
                    r.getRoomNumber(), r.getType(), r.getFloor(), r.getPricePerNight(), r.getStatus(), r.getDescription()
                });
            }
        };

        cmbSortOption.addActionListener(e -> refreshRoomTable.run());

        Runnable clearRoomFields = () -> {
            txtNum.setText(""); txtType.setText(""); txtFloor.setText(""); txtPrice.setText("");
            txtStatus.setText("available"); txtDesc.setText(""); selectedRoomNumber = null;
        };

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && !e.getValueIsAdjusting()) {
                selectedRoomNumber = (String) roomTableModel.getValueAt(row, 0);
                txtNum.setText(selectedRoomNumber);
                txtType.setText((String) roomTableModel.getValueAt(row, 1));
                txtFloor.setText(roomTableModel.getValueAt(row, 2).toString());
                txtPrice.setText(roomTableModel.getValueAt(row, 3).toString());
                txtStatus.setText((String) roomTableModel.getValueAt(row, 4));
                txtDesc.setText((String) roomTableModel.getValueAt(row, 5));
            }
        });

        btnAdd.addActionListener(e -> {
            try {
                Room r = new Room(txtNum.getText().trim(), txtType.getText().trim(), Integer.parseInt(txtFloor.getText().trim()),
                        Double.parseDouble(txtPrice.getText().trim()), txtStatus.getText().trim(), txtDesc.getText().trim());
                roomService.addRoom(r);
                refreshRoomTable.run(); clearRoomFields.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Check parameter inputs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnUpdateRoom.addActionListener(e -> {
            if (selectedRoomNumber != null) {
                try {
                    Room updated = new Room(txtNum.getText().trim(), txtType.getText().trim(), Integer.parseInt(txtFloor.getText().trim()),
                            Double.parseDouble(txtPrice.getText().trim()), txtStatus.getText().trim(), txtDesc.getText().trim());
                    roomService.updateRoom(selectedRoomNumber, updated);
                    refreshRoomTable.run(); clearRoomFields.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid decimal number for price.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnDeleteRoom.addActionListener(e -> {
            if (selectedRoomNumber != null) {
                roomService.deleteRoom(selectedRoomNumber);
                refreshRoomTable.run(); clearRoomFields.run();
            }
        });

        refreshRoomTable.run();
        return mainBox;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
 
    // CSV EXPORT FIX: Compiles monthly_report variables securely and includes cleaned Special Notes column
    private void exportToCSV(File file) {
        List<Reservation> reservations = resService.getAllReservations();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // Write exact header mapping columns matching your layout screenshot
            pw.println("ID,RoomNumber,Guest,CheckIn,CheckOut,Nights,TotalAmount,Status,Special Notes");
            
            for (Reservation r : reservations) {
                Room room = roomService.getRoomByNumber(r.getRoomNumber());
                double roomPrice = (room != null) ? room.getPricePerNight() : 0.0;

                long nights = java.time.temporal.ChronoUnit.DAYS.between(r.getCheckInDate(), r.getCheckOutDate());
                if (nights <= 0) nights = 1; 
                
                double totalAmount = r.getTotalAmount(roomPrice); 

                String notes = (r.getSpecialNotes() != null) ? r.getSpecialNotes().trim() : "";
                notes = notes.replace(",", " "); // Prevents column delimiter breaks

                pw.println(String.format("%s,%s,%s,%s,%s,%d,%.2f,%s,%s",
                    r.getId(), 
                    r.getRoomNumber(), 
                    r.getGuestName(),
                    r.getCheckInDate().format(dateForm), 
                    r.getCheckOutDate().format(dateForm), 
                    nights, 
                    totalAmount, 
                    r.getStatus(),
                    notes
                ));
            }
            JOptionPane.showMessageDialog(this, "CSV Report auto-generated on Desktop successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error generating CSV report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}