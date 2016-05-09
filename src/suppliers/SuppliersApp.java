/*
 * SuppliersApp.java
 */

package suppliers;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class SuppliersApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        
//        String msg = "<html>Not all functions work properly because of the migration process<BR>Niet alle functies werken naar behoren vanwege het migratieproces</html>";
//                
//        JLabel label = new JLabel(msg);
//        label.setFont(new Font("serif", Font.PLAIN, 16));
//        JOptionPane.showMessageDialog(null, label, "PROBLEM", JOptionPane.ERROR_MESSAGE); 
        show(new SuppliersView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of SuppliersApp
     */
    public static SuppliersApp getApplication() {
        return Application.getInstance(SuppliersApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(SuppliersApp.class, args);
    }
}
