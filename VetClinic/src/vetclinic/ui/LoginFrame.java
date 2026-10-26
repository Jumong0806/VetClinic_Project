package vetclinic.ui;

import java.awt.*;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JComponent;

import vetclinic.exception.AuthenticationException;
import vetclinic.service.AuthService;
import vetclinic.service.ClinicManager;

public class LoginFrame extends JFrame {

    // Services used by the login screen and the rest of the application.
    private final ClinicManager manager;
    private final AuthService authService;

    // GUI components that need to be accessed by multiple methods.
    private JTextField txtUsername;
    private JPasswordField pwdPassword;
    private JButton btnLogin;
    private JLabel lblPasswordToggle;

    // Tracks whether the password is currently visible.
    private boolean passwordVisible = false;

    // Colors used throughout the login interface.
    private final Color NAVY = new Color(0x192143);
    private final Color BLUE = new Color(0x4772D2);
    private final Color MUTED = new Color(0x6577A5);
    private final Color BACKGROUND = new Color(0xF3F7FD);
    private final Color BORDER = new Color(0xCAD6EB);

    // Shared height keeps the username and password fields consistent.
    private static final int FIELD_HEIGHT = 42;

    // Initializes the login window and its components.
    public LoginFrame(ClinicManager manager, AuthService authService) {
        super("Vet Kita - Login");
        this.manager = manager;
        this.authService = authService;

        setAppIcon();       // Sets the small icon in the window title bar.
        initComponents();   // Creates and arranges the login interface.
    }

    // Sets the window size, background image, and login panel position.
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null); // Centers the window on the screen.

        // Loads the background image from the resources folder.
        BackgroundPanel background =
                new BackgroundPanel("/images/login-bg.png");

        JPanel loginPanel = createLoginPanel();
        loginPanel.setOpaque(false); // Allows the background to show through.

        // GridBagConstraints controls where a component is placed in a layout.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // Gives the available horizontal space to this position.
        gbc.weighty = 1.0; // Gives the available vertical space to this position.
        gbc.anchor = GridBagConstraints.EAST; // Positions the login panel on the right.
        gbc.insets = new Insets(0, 0, 0, 0); // Adds no extra outside spacing.

        background.add(loginPanel, gbc);
        setContentPane(background); // Makes the background the main window content.
    }

    // Loads the custom application icon.
    private void setAppIcon() {
        // getResource() searches for the image inside the application's resources.
        URL iconURL = getClass().getResource("/images/logo-final.png");

        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            setIconImage(icon.getImage());
        } else {
            System.err.println("App logo not found: /images/logo-final.png");
        }
    }

    // Custom panel that draws the background image behind the login form.
    private class BackgroundPanel extends JPanel {
        private final Image backgroundImage;

        public BackgroundPanel(String resourcePath) {
            URL imageURL = getClass().getResource(resourcePath);

            // Stop early if the image path is incorrect.
            if (imageURL == null) {
                throw new IllegalArgumentException(
                        "Background image not found: " + resourcePath
                );
            }

            backgroundImage = new ImageIcon(imageURL).getImage();
            setLayout(new GridBagLayout());
            setBackground(BACKGROUND);
        }

        // Swing calls this method when the panel needs to be drawn again.
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int panelWidth = getWidth();
            int panelHeight = getHeight();

            int imageWidth = backgroundImage.getWidth(this);
            int imageHeight = backgroundImage.getHeight(this);

            // Avoid scaling if the image dimensions are not available.
            if (imageWidth <= 0 || imageHeight <= 0) {
                return;
            }

            // Graphics2D provides extra drawing controls, such as image quality.
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );

            g2.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            // Use the larger scale so the image covers both the width and height.
            // This preserves the image's proportions but may crop some edges.
            double scale = Math.max(
                    (double) panelWidth / imageWidth,
                    (double) panelHeight / imageHeight
            );

            int scaledWidth = (int) (imageWidth * scale);
            int scaledHeight = (int) (imageHeight * scale);

            // Center the scaled image within the window.
            int x = (panelWidth - scaledWidth) / 2;
            int y = (panelHeight - scaledHeight) / 2;

            g2.drawImage(
                    backgroundImage,
                    x, y,
                    scaledWidth, scaledHeight,
                    this
            );

            g2.dispose(); // Releases the copied graphics resource.
        }
    }

    // Makes components inside a vertical BoxLayout share the same width.
    private void fullWidth(JComponent c, int height) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);

        // BoxLayout uses these size limits when arranging components.
        // The small preferred width allows the component to expand as needed.
        c.setPreferredSize(new Dimension(100, height));
        c.setMinimumSize(new Dimension(0, height));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }

    // Creates the login card, its input fields, and its buttons.
    private JPanel createLoginPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(25, 25, 25, 45));

        // BoxLayout stacks the card's contents vertically.
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(500, 450));
        card.setBackground(Color.WHITE);

        // Combines a thin border with padding inside the card.
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xEDF1F8), 1),
                new EmptyBorder(32, 38, 32, 38)
        ));

        // ----- Heading -----
        JLabel lblWelcome = new JLabel("Welcome Back", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("SansSerif", Font.BOLD, 23));
        lblWelcome.setForeground(NAVY);
        fullWidth(lblWelcome, 30);

        JLabel lblSubtitle = new JLabel(
                "Log in to access the Vet Clinic Patient Record System.",
                SwingConstants.CENTER
        );
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSubtitle.setForeground(MUTED);
        fullWidth(lblSubtitle, 20);

        // ----- Username field -----
        txtUsername = new JTextField();
        styleTextField(txtUsername);
        txtUsername.setToolTipText("Enter your admin username");

        // ----- Password field and Show/Hide control -----
        pwdPassword = new JPasswordField();
        pwdPassword.setFont(new Font("SansSerif", Font.PLAIN, 12));
        pwdPassword.setForeground(NAVY);
        pwdPassword.setCaretColor(BLUE);
        pwdPassword.setEchoChar('\u2022'); // Displays bullets instead of the password.
        pwdPassword.setToolTipText("Enter your admin password");
        pwdPassword.setOpaque(false); // Lets the password row provide the white background.
        pwdPassword.setBorder(new EmptyBorder(8, 10, 8, 10));

        lblPasswordToggle = new JLabel("Show", SwingConstants.CENTER);
        lblPasswordToggle.setForeground(BLUE);
        lblPasswordToggle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblPasswordToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblPasswordToggle.setPreferredSize(new Dimension(48, FIELD_HEIGHT));

        // A mouse click switches the password between visible and hidden.
        lblPasswordToggle.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                togglePasswordVisibility();
            }
        });

        // Places the password field and Show/Hide label in one bordered row.
        JPanel passwordRow = new JPanel(new BorderLayout());
        passwordRow.setBackground(Color.WHITE);
        passwordRow.setBorder(BorderFactory.createLineBorder(BORDER));
        passwordRow.add(pwdPassword, BorderLayout.CENTER);
        passwordRow.add(lblPasswordToggle, BorderLayout.EAST);
        fullWidth(passwordRow, FIELD_HEIGHT);

        // ----- Login button -----
        btnLogin = new JButton("Log In");
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(BLUE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setOpaque(true);
        btnLogin.addActionListener(e -> handleLogin()); // Runs login when clicked.
        fullWidth(btnLogin, 44);

        // ----- Authorization notice -----
        JLabel lblNotice = new JLabel(
                "<html><center>ⓘ &nbsp; Only authorized administrators "
                        + "can access this system.</center></html>",
                SwingConstants.CENTER
        );
        lblNotice.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblNotice.setForeground(MUTED);
        lblNotice.setOpaque(true);
        lblNotice.setBackground(new Color(0xF0F5FF));
        lblNotice.setBorder(new EmptyBorder(8, 6, 8, 6));
        fullWidth(lblNotice, 36);

        // ----- Arrange the components from top to bottom -----
        card.add(lblWelcome);
        card.add(Box.createVerticalStrut(5));  // Adds vertical spacing.
        card.add(lblSubtitle);
        card.add(Box.createVerticalStrut(20));

        addFieldLabel(card, "Username");
        card.add(Box.createVerticalStrut(7));
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(16));

        addFieldLabel(card, "Password");
        card.add(Box.createVerticalStrut(7));
        card.add(passwordRow);
        card.add(Box.createVerticalStrut(24));

        card.add(btnLogin);
        card.add(Box.createVerticalStrut(14));
        card.add(lblNotice);

        // Centers the card within the outer panel.
        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.gridx = 0;
        cardGbc.gridy = 0;
        cardGbc.weightx = 1.0;
        cardGbc.anchor = GridBagConstraints.CENTER;
        cardGbc.fill = GridBagConstraints.HORIZONTAL;
        outer.add(card, cardGbc);

        // Pressing Enter while in a login field activates the login button.
        getRootPane().setDefaultButton(btnLogin);

        return outer;
    }

    // Creates a left-aligned label above an input field.
    private void addFieldLabel(JPanel panel, String text) {
        JLabel label = new JLabel(text, SwingConstants.LEFT);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(MUTED);
        fullWidth(label, 20);
        panel.add(label);
    }

    // Applies the same font, colors, border, and dimensions to text fields.
    private void styleTextField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 12));
        field.setForeground(NAVY);
        field.setBackground(Color.WHITE);
        field.setCaretColor(BLUE);

        // Outer line creates the border; inner EmptyBorder adds text padding.
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 10, 8, 10)
        ));

        fullWidth(field, FIELD_HEIGHT);
    }

    // Switches the password between visible text and hidden bullets.
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible; // Reverses the current state.

        if (passwordVisible) {
            pwdPassword.setEchoChar((char) 0); // Shows the actual characters.
            lblPasswordToggle.setText("Hide");
        } else {
            pwdPassword.setEchoChar('\u2022'); // Hides characters with bullets.
            lblPasswordToggle.setText("Show");
        }
    }

    // Checks the entered credentials and opens the main window if successful.
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(pwdPassword.getPassword());

        try {
            authService.login(username, password);

            // Close the login window and open the main application window.
            dispose();
            new MainFrame(manager, authService).setVisible(true);

        } catch (AuthenticationException e) {
            // Display the error returned by the authentication service.
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Login failed",
                    JOptionPane.ERROR_MESSAGE
            );

            pwdPassword.setText(""); // Clear the password after a failed login.
            pwdPassword.requestFocus(); // Place the cursor back in the password field.
        }
    }
}