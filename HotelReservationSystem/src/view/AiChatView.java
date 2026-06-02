package view;

import controller.AIChatController;
import service.RoomService;         
import service.ReservationService;  

import javax.swing.*;
import java.awt.*;

public class AiChatView extends JPanel {
    private final AIChatController aiChatController;
    private JTextArea txtHistory;
    private JTextField txtInput;
    private JButton btnSubmit;

    // FIXED: Changed the parameter from HotelController to the two required services
    public AiChatView(RoomService roomService, ReservationService reservationService) {
        this.aiChatController = new AIChatController(roomService, reservationService);
        buildLayout();
    }

    private void buildLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtHistory = new JTextArea();
        txtHistory.setEditable(false);
        txtHistory.setLineWrap(true);
        txtHistory.setWrapStyleWord(true);
        txtHistory.setText("Assistant: System activated. Ask me regarding room inventories, reservation processes, pricing models, or validations...\n\n");

        add(new JScrollPane(txtHistory), BorderLayout.CENTER);

        JPanel bottomRow = new JPanel(new BorderLayout(5, 5));
        txtInput = new JTextField();
        btnSubmit = new JButton("Submit Query");

        Runnable executeAction = () -> {
            String query = txtInput.getText().trim();
            if (!query.isEmpty()) {
                txtHistory.append("You: " + query + "\n");
                String response = aiChatController.processQuery(query);
                txtHistory.append(response + "\n\n");
                txtInput.setText("");
                txtHistory.setCaretPosition(txtHistory.getDocument().getLength());
            }
        };

        btnSubmit.addActionListener(e -> executeAction.run());
        txtInput.addActionListener(e -> executeAction.run());

        bottomRow.add(txtInput, BorderLayout.CENTER);
        bottomRow.add(btnSubmit, BorderLayout.EAST);
        add(bottomRow, BorderLayout.SOUTH);
    }
}