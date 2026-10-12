package vetclinic;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import vetclinic.exception.StorageException;
import vetclinic.service.AuthService;
import vetclinic.service.ClinicManager;
import vetclinic.storage.FileStorage;
import vetclinic.storage.MySqlStorage;
import vetclinic.storage.Storable;
import vetclinic.ui.LoginFrame;

/**
 * Main - the entry point. It builds the layers and opens the login window.
 * Owner: JP
 *
 * Layers:  LoginFrame -> AuthService -> ClinicManager -> Storable -> FileStorage or MySqlStorage
 * OOP concept: POLYMORPHISM. The variable "storage" has the INTERFACE type Storable, so we can put
 * either storage class in it and nothing else in the program changes.
 */
public class Main {

    // false = save to data/*.dat files (works with no setup). true = save to MySQL (bonus).
    private static final boolean USE_MYSQL = true;

    public static void main(String[] args) {
        // All Swing code must run on the Event Dispatch Thread (EDT).
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                // If the look and feel fails we simply keep the default one.
            }

            Storable storage = USE_MYSQL ? new MySqlStorage() : new FileStorage();
            ClinicManager manager = new ClinicManager(storage);

            // 1. Load the saved records.
            try {
                manager.loadAll();
            } catch (StorageException e) {
                // We stop here on purpose. If we kept going, the empty data would overwrite the saved file on exit.
                JOptionPane.showMessageDialog(null,
                        "The saved records could not be loaded:\n" + e.getMessage()
                                + "\n\nThe program will close so your data is not overwritten.\n"
                                + "If you changed the classes, delete the files in the data folder and start again.",
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
                    JOptionPane.showMessageDialog(null, "Could not save the demo accounts:\n" + e.getMessage(),
                            "Save error", JOptionPane.ERROR_MESSAGE);
                }
            }

            // 3. Open the login window.
            AuthService authService = new AuthService(manager);
            new LoginFrame(manager, authService).setVisible(true);
        });
    }
}
