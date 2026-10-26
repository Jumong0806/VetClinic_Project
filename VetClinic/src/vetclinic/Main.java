package vetclinic;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import vetclinic.exception.StorageException;
import vetclinic.service.AuthService;
import vetclinic.service.ClinicManager;
import vetclinic.storage.MySqlStorage;
import vetclinic.storage.Storable;
import vetclinic.ui.LoginFrame;

/**
 * Main - the entry point. It builds the layers and opens the login window.
 *
 * Owner: JP
 *
 * Layers:  LoginFrame -> AuthService -> ClinicManager -> Storable -> MySqlStorage
 *
 * OOP concept: POLYMORPHISM. The variable "storage" has the INTERFACE type Storable,
 * allowing the service layer to interface cleanly with the MySQL database persistence engine.
 */
public class Main {

    public static void main(String[] args) {
        // All Swing code must run on the Event Dispatch Thread (EDT).
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                // If the look and feel fails we simply keep the default one.
            }

            Storable storage = new MySqlStorage();
            ClinicManager manager = new ClinicManager(storage);

            // 1. Load the saved records from the database.
            try {
                manager.loadAll();
            } catch (StorageException e) {
                JOptionPane.showMessageDialog(null,
                        "The saved records could not be loaded from the database:\n" + e.getMessage()
                                + "\n\nThe program will close so your data is not overwritten.\n"
                                + "Please check your database connection settings in MySqlStorage.java.",
                        "Load error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            // 2. First run: create the demo accounts and save them.
            boolean firstRun = manager.getAllStaff().isEmpty();
            manager.seedDefaultStaffIfEmpty();
            if (firstRun) {
                try {
                    manager.saveAll();
                } catch (StorageException e) {
                    JOptionPane.showMessageDialog(null, "Could not save the demo accounts to the database:\n" + e.getMessage(),
                            "Save error", JOptionPane.ERROR_MESSAGE);
                }
            }

            // 3. Open the login window.
            AuthService authService = new AuthService();
            new LoginFrame(manager, authService).setVisible(true);
        });
    }
}