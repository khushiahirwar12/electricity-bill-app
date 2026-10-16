import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ElectricityBillApp extends JFrame {

    private final Color PRIMARY      = new Color(21, 101, 192);
    private final Color PRIMARY_DARK = new Color(13, 71, 161);
    private final Color SUCCESS      = new Color(46, 125, 50);
    private final Color WARNING      = new Color(230, 81, 0);
    private final Color DANGER       = new Color(198, 40, 40);
    private final Color BG           = new Color(240, 244, 248);
    private final Color CARD         = Color.WHITE;
    private final Color BORDER       = new Color(210, 218, 226);
    private final Color TEXT_DARK    = new Color(33, 37, 41);
    private final Color MUTED        = new Color(108, 117, 125);

    private JTextField nameField, meterField, prevReadingField, currReadingField;
    private JComboBox<String> connectionCombo;
    private JEditorPane invoicePane;
    private JLabel statusLabel, totalLabel, unitsLabel;

    private static final AtomicInteger billCounter = new AtomicInteger(1001);
    private final List<String> billHistory = new ArrayList<>();

    private static final double DOM_FIXED = 75.0;
    private static final double COM_FIXED = 200.0;
    private static final double GREEN_CESS = 0.05;
    private static final double ELEC_DUTY  = 0.06;
    private static final double HIGH_USAGE = 0.10;

    public ElectricityBillApp() {
        setTitle("State Power Corporation • Official Billing Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 920);
        setMinimumSize(new Dimension(900, 850));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createMainContent(), BorderLayout.CENTER);
        root.add(createFooter(), BorderLayout.SOUTH);

        add(root);
        setupKeyBindings();
        nameField.requestFocusInWindow();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(12, 28, 12, 28));
        header.setPreferredSize(new Dimension(0, 72));

        JLabel title = new JLabel("STATE POWER CORPORATION");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Official Electricity Billing Portal");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(187, 222, 251));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(subtitle);

        JButton rateBtn = new JButton("Rate Card");
        rateBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rateBtn.setForeground(Color.BLACK);
        rateBtn.setBackground(Color.WHITE);
        rateBtn.setFocusPainted(false);
        rateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rateBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(8, 18, 8, 18)
        ));
        rateBtn.setContentAreaFilled(true);
        rateBtn.setOpaque(true);

        rateBtn.addActionListener(e -> showRateCard());

        rateBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                rateBtn.setBackground(new Color(240, 240, 240));
            }
            public void mouseExited(MouseEvent e) {
                rateBtn.setBackground(Color.WHITE);
            }
        });

        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        right.add(rateBtn);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel createMainContent() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(16, 24, 12, 24));

        main.add(createInputCard());
        main.add(Box.createVerticalStrut(12));
        main.add(createButtonPanel());
        main.add(Box.createVerticalStrut(12));
        main.add(createInvoiceCard());

        return main;
    }

    private JPanel createInputCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(18, 22, 16, 22)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.28;
        card.add(label("Consumer Name *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.72;
        nameField = field();
        card.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.28;
        card.add(label("Meter Serial No *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.72;
        meterField = field();
        card.add(meterField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.28;
        card.add(label("Connection Type *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.72;
        connectionCombo = new JComboBox<>(new String[]{
                "Domestic (Residential)",
                "Commercial (Business)"
        });
        connectionCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        connectionCombo.setBackground(Color.WHITE);
        connectionCombo.setPreferredSize(new Dimension(0, 36));
        card.add(connectionCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.28;
        card.add(label("Previous Reading *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.72;
        prevReadingField = field();
        card.add(prevReadingField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.28;
        card.add(label("Current Reading *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.72;
        currReadingField = field();
        card.add(currReadingField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.28;
        card.add(label("Units Consumed"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.72;
        unitsLabel = new JLabel("— kWh");
        unitsLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        unitsLabel.setForeground(PRIMARY_DARK);
        card.add(unitsLabel, gbc);

        FocusAdapter calc = new FocusAdapter() {
            public void focusLost(FocusEvent e) { calculateUnits(); }
        };
        prevReadingField.addFocusListener(calc);
        currReadingField.addFocusListener(calc);

        return card;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 6));
        panel.setBackground(BG);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton generateBtn = createButton("Generate Invoice", PRIMARY);
        JButton historyBtn  = createButton("History", new Color(94, 53, 177));
        JButton pdfBtn      = createButton("Save as PDF", new Color(0, 121, 107));
        JButton printBtn    = createButton("Print", new Color(55, 71, 79));
        JButton clearBtn    = createButton("Clear", new Color(120, 120, 120));

        generateBtn.addActionListener(e -> generateInvoice());
        historyBtn.addActionListener(e -> showHistory());
        pdfBtn.addActionListener(e -> saveAsPDF());
        printBtn.addActionListener(e -> printInvoice());
        clearBtn.addActionListener(e -> clearForm());

        getRootPane().setDefaultButton(generateBtn);

        panel.add(generateBtn);
        panel.add(historyBtn);
        panel.add(pdfBtn);
        panel.add(printBtn);
        panel.add(clearBtn);
        return panel;
    }

    private JPanel createInvoiceCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(14, 16, 12, 16)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel invTitle = new JLabel("ELECTRICITY TAX INVOICE / RECEIPT");
        invTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        invTitle.setForeground(PRIMARY_DARK);
        invTitle.setHorizontalAlignment(SwingConstants.CENTER);
        invTitle.setBorder(new EmptyBorder(0, 0, 4, 0));

        invoicePane = new JEditorPane();
        invoicePane.setContentType("text/html");
        invoicePane.setEditable(false);
        invoicePane.setBackground(new Color(250, 251, 252));
        invoicePane.setBorder(new EmptyBorder(6, 8, 6, 8));
        invoicePane.setText(getEmptyReceiptHtml());

        JScrollPane scroll = new JScrollPane(invoicePane);
        scroll.setBorder(new LineBorder(new Color(225, 230, 235), 1));
        scroll.setPreferredSize(new Dimension(0, 320));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(6, 0, 0, 0));

        totalLabel = new JLabel(" ");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel.setForeground(PRIMARY_DARK);
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setBorder(new EmptyBorder(4, 0, 0, 0));

        bottom.add(totalLabel);
        bottom.add(statusLabel);

        card.add(invTitle, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        footer.setBackground(new Color(225, 232, 239));
        footer.setBorder(new EmptyBorder(8, 0, 8, 0));
        footer.setPreferredSize(new Dimension(0, 36));

        JLabel text = new JLabel("© 2026 State Power Corporation  •  Helpline: 1912  •  www.statepower.gov.in");
        text.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        text.setForeground(MUTED);
        footer.add(text);
        return footer;
    }

    private void setupKeyBindings() {
        KeyAdapter enter = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) generateInvoice();
            }
        };
        nameField.addKeyListener(enter);
        meterField.addKeyListener(enter);
        prevReadingField.addKeyListener(enter);
        currReadingField.addKeyListener(enter);
        connectionCombo.addKeyListener(enter);

        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK), "print");
        am.put("print", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { printInvoice(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "pdf");
        am.put("pdf", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { saveAsPDF(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clear");
        am.put("clear", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { clearForm(); }
        });
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT_DARK);
        return l;
    }

    private JTextField field() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setPreferredSize(new Dimension(0, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(7, 11, 7, 11)
        ));
        f.setBackground(Color.WHITE);

        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(PRIMARY, 2, true),
                        new EmptyBorder(6, 10, 6, 10)
                ));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER, 1, true),
                        new EmptyBorder(7, 11, 7, 11)
                ));
            }
        });
        return f;
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(8, 16, 8, 16));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    private void calculateUnits() {
        try {
            double prev = Double.parseDouble(prevReadingField.getText().trim());
            double curr = Double.parseDouble(currReadingField.getText().trim());
            if (curr >= prev) {
                unitsLabel.setText(String.format("%.2f kWh", curr - prev));
                unitsLabel.setForeground(PRIMARY_DARK);
            } else {
                unitsLabel.setText("Invalid readings");
                unitsLabel.setForeground(DANGER);
            }
        } catch (Exception e) {
            unitsLabel.setText("— kWh");
            unitsLabel.setForeground(MUTED);
        }
    }

    private void generateInvoice() {
        String name = nameField.getText().trim();
        String meterId = meterField.getText().trim();
        String prevStr = prevReadingField.getText().trim();
        String currStr = currReadingField.getText().trim();

        if (name.isEmpty() || meterId.isEmpty() || prevStr.isEmpty() || currStr.isEmpty()) {
            showStatus("⚠  Please fill all required fields (*)", WARNING);
            totalLabel.setText(" ");
            return;
        }

        double prev, curr, units;
        try {
            prev = Double.parseDouble(prevStr);
            curr = Double.parseDouble(currStr);
            if (curr < prev) {
                showStatus("⚠  Current reading cannot be less than previous reading", WARNING);
                return;
            }
            units = curr - prev;
        } catch (NumberFormatException ex) {
            showStatus("⚠  Please enter valid numbers for readings", WARNING);
            return;
        }

        int type = connectionCombo.getSelectedIndex() + 1;
        double baseBill = 0, fixedCharge;
        String connName;
        StringBuilder slabDetails = new StringBuilder();

        if (type == 1) {
            connName = "Domestic (Residential)";
            fixedCharge = DOM_FIXED;

            if (units <= 100) {
                baseBill = units * 3.50;
                slabDetails.append(String.format("0–100 units × ₹3.50 = ₹%.2f", baseBill));
            } else if (units <= 200) {
                baseBill = 100 * 3.50 + (units - 100) * 5.50;
                slabDetails.append(String.format("0–100 × ₹3.50 + 101–%.0f × ₹5.50 = ₹%.2f", units, baseBill));
            } else if (units <= 300) {
                baseBill = 100 * 3.50 + 100 * 5.50 + (units - 200) * 7.00;
                slabDetails.append(String.format("0–100 × ₹3.50 + 101–200 × ₹5.50 + 201–%.0f × ₹7.00 = ₹%.2f", units, baseBill));
            } else {
                baseBill = 100 * 3.50 + 100 * 5.50 + 100 * 7.00 + (units - 300) * 8.50;
                slabDetails.append(String.format("0–100 × ₹3.50 + 101–200 × ₹5.50 + 201–300 × ₹7.00 + 301–%.0f × ₹8.50 = ₹%.2f", units, baseBill));
            }
        } else {
            connName = "Commercial (Business)";
            fixedCharge = COM_FIXED;

            if (units <= 100) {
                baseBill = units * 6.00;
                slabDetails.append(String.format("0–100 units × ₹6.00 = ₹%.2f", baseBill));
            } else if (units <= 200) {
                baseBill = 100 * 6.00 + (units - 100) * 8.50;
                slabDetails.append(String.format("0–100 × ₹6.00 + 101–%.0f × ₹8.50 = ₹%.2f", units, baseBill));
            } else {
                baseBill = 100 * 6.00 + 100 * 8.50 + (units - 200) * 11.00;
                slabDetails.append(String.format("0–100 × ₹6.00 + 101–200 × ₹8.50 + 201–%.0f × ₹11.00 = ₹%.2f", units, baseBill));
            }
        }

        double greenTax  = baseBill * GREEN_CESS;
        double duty      = baseBill * ELEC_DUTY;
        double surcharge = (units > 300) ? baseBill * HIGH_USAGE : 0;
        double total     = baseBill + fixedCharge + greenTax + duty + surcharge;

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        String dueDate = LocalDate.now().plusDays(15).format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        String billNo = "SPC-" + billCounter.getAndIncrement();

        String html = buildReceiptHtml(billNo, name, meterId, connName, prev, curr, units,
                date, dueDate, baseBill, fixedCharge, greenTax, duty, surcharge, total, slabDetails.toString());

        invoicePane.setText(html);
        totalLabel.setText("Net Payable Amount  :  ₹ " + String.format("%,.2f", total));

        billHistory.add(0, String.format("%s | %s | ₹%.2f | %s", billNo, name, total, date));
        if (billHistory.size() > 5) billHistory.remove(billHistory.size() - 1);

        if (units > 300) {
            showStatus("⚠  High consumption detected. Consider energy-efficient appliances.", WARNING);
        } else if (units > 200) {
            showStatus("💡  Tip: You are close to the next higher slab. Monitor usage.", WARNING);
        } else {
            showStatus("✅  Excellent! Your consumption is within eco-friendly limits.", SUCCESS);
        }
    }

    private String buildReceiptHtml(String billNo, String name, String meter, String conn,
                                    double prev, double curr, double units, String date, String dueDate,
                                    double base, double fixed, double green, double duty,
                                    double surcharge, double total, String slabText) {

        String surchargeRow = "";
        if (surcharge > 0) {
            surchargeRow = String.format(
                "<tr><td style='padding:4px 0; color:#c62828;'>High Usage Penalty (10%%)</td>" +
                "<td style='text-align:right; padding:4px 0; color:#c62828;'>₹ %.2f</td></tr>", surcharge);
        }

        return String.format(
            "<html><body style='font-family:Segoe UI,Arial; font-size:12.5px; color:#222; margin:8px;'>" +
            "<div style='text-align:center; margin-bottom:8px;'>" +
            "<div style='font-size:15px; font-weight:bold; color:#0D47A1;'>STATE POWER CORPORATION</div>" +
            "<div style='font-size:10px; color:#555;'>Official Electricity Tax Invoice / Receipt</div>" +
            "</div>" +
            "<table width='100%%' style='border-collapse:collapse; margin-bottom:6px;'>" +
            "<tr><td style='padding:2px 0; width:40%%; color:#555;'>Bill Number</td><td style='padding:2px 0;'><b>%s</b></td></tr>" +
            "<tr><td style='padding:2px 0; color:#555;'>Bill Date</td><td style='padding:2px 0;'><b>%s</b></td></tr>" +
            "<tr><td style='padding:2px 0; color:#555;'>Due Date</td><td style='padding:2px 0;'><b style='color:#c62828;'>%s</b></td></tr>" +
            "<tr><td style='padding:2px 0; color:#555;'>Consumer Name</td><td style='padding:2px 0;'><b>%s</b></td></tr>" +
            "<tr><td style='padding:2px 0; color:#555;'>Meter Serial No</td><td style='padding:2px 0;'><b>%s</b></td></tr>" +
            "<tr><td style='padding:2px 0; color:#555;'>Connection Type</td><td style='padding:2px 0;'><b>%s</b></td></tr>" +
            "<tr><td style='padding:2px 0; color:#555;'>Previous Reading</td><td style='padding:2px 0;'><b>%.0f</b></td></tr>" +
            "<tr><td style='padding:2px 0; color:#555;'>Current Reading</td><td style='padding:2px 0;'><b>%.0f</b></td></tr>" +
            "<tr><td style='padding:2px 0; color:#555;'>Units Consumed</td><td style='padding:2px 0;'><b>%.2f kWh</b></td></tr>" +
            "</table>" +
            "<hr style='border:none; border-top:1px solid #ddd; margin:6px 0;'>" +
            "<div style='font-weight:bold; margin-bottom:4px; color:#0D47A1; font-size:12px;'>ENERGY CHARGES (Slab-wise)</div>" +
            "<div style='font-size:11px; color:#444; margin-bottom:6px;'>%s</div>" +
            "<table width='100%%' style='border-collapse:collapse;'>" +
            "<tr><td style='padding:3px 0;'>Energy Charges</td><td style='text-align:right; padding:3px 0;'>₹ %.2f</td></tr>" +
            "<tr><td style='padding:3px 0;'>Fixed / Meter Rent</td><td style='text-align:right; padding:3px 0;'>₹ %.2f</td></tr>" +
            "<tr><td style='padding:3px 0;'>Green Energy Cess (5%%)</td><td style='text-align:right; padding:3px 0;'>₹ %.2f</td></tr>" +
            "<tr><td style='padding:3px 0;'>Electricity Duty (6%%)</td><td style='text-align:right; padding:3px 0;'>₹ %.2f</td></tr>" +
            "%s" +
            "</table>" +
            "<hr style='border:none; border-top:2px solid #0D47A1; margin:8px 0 5px 0;'>" +
            "<table width='100%%'>" +
            "<tr><td style='font-size:13px; font-weight:bold;'>NET PAYABLE AMOUNT</td>" +
            "<td style='text-align:right; font-size:14px; font-weight:bold; color:#0D47A1;'>₹ %.2f</td></tr>" +
            "</table>" +
            "<div style='text-align:center; margin-top:10px; font-size:10px; color:#777;'>" +
            "Pay before due date to avoid late fee • This is a computer generated receipt<br>" +
            "Helpline: 1912  •  www.statepower.gov.in" +
            "</div>" +
            "</body></html>",
            billNo, date, dueDate, name, meter, conn, prev, curr, units,
            slabText, base, fixed, green, duty, surchargeRow, total
        );
    }

    private String getEmptyReceiptHtml() {
        return "<html><body style='font-family:Segoe UI; color:#999; text-align:center; padding-top:80px;'>" +
               "<div style='font-size:14px;'>Your official invoice will appear here</div>" +
               "<div style='font-size:12px; margin-top:8px;'>Fill the form and press <b>Enter</b> or click <b>Generate Invoice</b></div>" +
               "<div style='font-size:11px; margin-top:14px; color:#bbb;'>Shortcuts: Ctrl+P (Print)  •  Ctrl+S (PDF)  •  Esc (Clear)</div>" +
               "</body></html>";
    }

    private void showRateCard() {
        String msg =
            "<html>" +
            "<body style='font-family:Segoe UI, Arial; font-size:14px; color:#000000; width:340px; padding:8px;'>" +
            "<div style='font-size:16px; font-weight:bold; color:#000000; margin-bottom:10px;'>DOMESTIC (Residential)</div>" +
            "• 0 – 100 units &nbsp;&nbsp;&nbsp;&nbsp;: ₹ 3.50 / unit<br>" +
            "• 101 – 200 units &nbsp;&nbsp;: ₹ 5.50 / unit<br>" +
            "• 201 – 300 units &nbsp;&nbsp;: ₹ 7.00 / unit<br>" +
            "• Above 300 units &nbsp;: ₹ 8.50 / unit<br>" +
            "• Fixed Charge &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;: ₹ 75<br><br>" +
            "<div style='font-size:16px; font-weight:bold; color:#000000; margin-bottom:10px;'>COMMERCIAL (Business)</div>" +
            "• 0 – 100 units &nbsp;&nbsp;&nbsp;&nbsp;: ₹ 6.00 / unit<br>" +
            "• 101 – 200 units &nbsp;&nbsp;: ₹ 8.50 / unit<br>" +
            "• Above 200 units &nbsp;: ₹ 11.00 / unit<br>" +
            "• Fixed Charge &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;: ₹ 200<br><br>" +
            "<div style='font-size:15px; font-weight:bold; color:#000000; margin-bottom:8px;'>Additional Charges (Both)</div>" +
            "• Green Energy Cess &nbsp;&nbsp;&nbsp;: 5% of energy charges<br>" +
            "• Electricity Duty &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;: 6% of energy charges<br>" +
            "• High Usage Penalty &nbsp;: 10% (if units > 300)" +
            "</body></html>";

        JOptionPane.showMessageDialog(this, msg, "Current Rate Card", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showHistory() {
        if (billHistory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No bills generated yet in this session.", "Bill History", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder("<html><body style='font-family:Segoe UI; font-size:12px; color:#000000;'>");
        sb.append("<b>Last ").append(billHistory.size()).append(" bills (this session):</b><br><br>");
        for (String h : billHistory) {
            sb.append("• ").append(h).append("<br>");
        }
        sb.append("</body></html>");
        JOptionPane.showMessageDialog(this, sb.toString(), "Bill History", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveAsPDF() {
        if (invoicePane.getText().contains("Your official invoice will appear here")) {
            showStatus("⚠  Generate an invoice first", WARNING);
            return;
        }
        JOptionPane.showMessageDialog(this,
                "<html>The print dialog will open.<br><br>" +
                "Select <b>Microsoft Print to PDF</b> or <b>Save as PDF</b> from the printer list.<br>" +
                "Then choose the location and click <b>Save</b>.</html>",
                "Save as PDF", JOptionPane.INFORMATION_MESSAGE);
        printInvoice();
    }

    private void printInvoice() {
        if (invoicePane.getText().contains("Your official invoice will appear here")) {
            showStatus("⚠  Generate an invoice first", WARNING);
            return;
        }
        try {
            boolean done = invoicePane.print();
            if (done) showStatus("✅  Receipt sent to printer / saved as PDF", SUCCESS);
            else showStatus("Printing cancelled", WARNING);
        } catch (PrinterException ex) {
            showStatus("⚠  Print failed: " + ex.getMessage(), WARNING);
        }
    }

    private void clearForm() {
        nameField.setText("");
        meterField.setText("");
        prevReadingField.setText("");
        currReadingField.setText("");
        connectionCombo.setSelectedIndex(0);
        unitsLabel.setText("— kWh");
        unitsLabel.setForeground(MUTED);
        invoicePane.setText(getEmptyReceiptHtml());
        totalLabel.setText(" ");
        statusLabel.setText(" ");
        nameField.requestFocus();
    }

    private void showStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new ElectricityBillApp().setVisible(true));
    }
}
