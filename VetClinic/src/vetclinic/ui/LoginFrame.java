package vetclinic.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import vetclinic.exception.AuthenticationException;
import vetclinic.service.AuthService;
import vetclinic.service.ClinicManager;

/**
 * LoginFrame - the first window. The user types a username and password.
 * Owner: Richnie
 *
 * Swing concepts: BorderLayout (frame) + GridBagLayout (form), event listeners, JOptionPane for errors.
 * OOP concepts: INHERITANCE (extends JFrame), ENCAPSULATION (private components).
 */
public class LoginFrame extends JFrame {

    private ClinicManager manager;
    private AuthService authService;
    private JTextField txtUsername;
    private JPasswordField pwdPassword;   // pwd = password field, hides what you type
    private JButton btnLogin;

    public LoginFrame(ClinicManager manager, AuthService authService) {
        super("Vet Kita - Login");
        this.manager = manager;
        this.authService = authService;
        initComponents();
    }

    // Builds the whole window.
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // closing the login window ends the program
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0xF4F7FC));

        // Top: the app name.
        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setOpaque(false);
        pnlTitle.setBorder(BorderFactory.createEmptyBorder(30, 20, 10, 20));
        JLabel lblTitle = new JLabel("Vet Kita", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 30));
        lblTitle.setForeground(new Color(0x1F2A44));
        JLabel lblSubtitle = new JLabel("Veterinary Clinic Patient Record System", SwingConstants.CENTER);
        lblSubtitle.setForeground(new Color(0x6B7A99));
        pnlTitle.add(lblTitle, BorderLayout.CENTER);
        pnlTitle.add(lblSubtitle, BorderLayout.SOUTH);
        add(pnlTitle, BorderLayout.NORTH);

        // Middle: the form. GridBagLayout puts the labels and boxes in rows and columns.
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setOpaque(false);
        pnlForm.setBorder(BorderFactory.createEmptyBorder(10, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUsername = new JTextField(16);
        pwdPassword = new JPasswordField(16);
        btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(0x2563EB));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setOpaque(true);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setPreferredSize(new Dimension(100, 36));

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        pnlForm.add(new JLabel("Username"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        pnlForm.add(txtUsername, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        pnlForm.add(new JLabel("Password"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        pnlForm.add(pwdPassword, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.insets = new Insets(18, 6, 6, 6);
        pnlForm.add(btnLogin, gbc);
        add(pnlForm, BorderLayout.CENTER);

        // Event handling: the button and the Enter key both call handleLogin().
        btnLogin.addActionListener(e -> handleLogin());
        getRootPane().setDefaultButton(btnLogin);

        setSize(440, 380);
        setResizable(false);
        setLocationRelativeTo(null);   // center on the screen
    }

    // Checks the login. On success it opens the main window.
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = new String(pwdPassword.getPassword());   // getPassword() returns a char[]
        try {
            authService.login(username, password);
            dispose();   // close the login window
            new MainFrame(manager, authService).setVisible(true);
        } catch (AuthenticationException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Login failed", JOptionPane.ERROR_MESSAGE);
            pwdPassword.setText("");
            pwdPassword.requestFocus();
        }
    }
}
