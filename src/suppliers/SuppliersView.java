/*
 * SuppliersView.java
 */
package suppliers;

import com.mysql.jdbc.Statement;
import java.awt.Desktop;
import java.awt.Graphics;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import org.jdesktop.application.Task;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.RollbackException;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.beansbinding.AbstractBindingListener;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.PropertyStateEvent;

/**
 * The application's main frame.
 */
public class SuppliersView extends FrameView {

    Connection con = null;
    Statement st = null;
    ResultSet rs = null;

    public SuppliersView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        // tracking table selection
        masterTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    public void valueChanged(ListSelectionEvent e) {
                        firePropertyChange("recordSelected", !isRecordSelected(), isRecordSelected());
                    }
                });

        // tracking changes to save
        bindingGroup.addBindingListener(new AbstractBindingListener() {

            @Override
            public void targetChanged(Binding binding, PropertyStateEvent event) {
                // save action observes saveNeeded property
                setSaveNeeded(true);
            }
        });

        // have a transaction started
        entityManager.getTransaction().begin();
    }

    public boolean isSaveNeeded() {
        return saveNeeded;
    }

    private void setSaveNeeded(boolean saveNeeded) {
        if (saveNeeded != this.saveNeeded) {
            this.saveNeeded = saveNeeded;
            firePropertyChange("saveNeeded", !saveNeeded, saveNeeded);
        }
    }

    public boolean isRecordSelected() {
        return masterTable.getSelectedRow() != -1;
    }

    @Action
    public void newRecord() {
        if (System.getProperty("user.name").toLowerCase().equals("rvandommelen") 
                || System.getProperty("user.name").toLowerCase().equals("amaslowiec")  
                || System.getProperty("user.name").toLowerCase().equals("pchukwu")
                || System.getProperty("user.name").toLowerCase().equals("promisec")
                || System.getProperty("user.name").toLowerCase().equals("wrademakers")
                || System.getProperty("user.name").toLowerCase().equals("dhungs")
                || System.getProperty("user.name").toLowerCase().equals("rvandergaag")) {
            suppliers.Suppliers s = new suppliers.Suppliers();
            entityManager.persist(s);
            list.add(s);
            int row = list.size() - 1;
            masterTable.setRowSelectionInterval(row, row);
            masterTable.scrollRectToVisible(masterTable.getCellRect(row, 0, true));
            setSaveNeeded(true);
        } else {
            JOptionPane.showMessageDialog(null, "You have no rights to add a new supplier", "No privileges", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Action(enabledProperty = "recordSelected")
    public void deleteRecord() {
        if (System.getProperty("user.name").toLowerCase().equals("rvandommelen") 
                || System.getProperty("user.name").toLowerCase().equals("amaslowiec")
                || System.getProperty("user.name").toLowerCase().equals("pchukwu")
                || System.getProperty("user.name").toLowerCase().equals("promisec")
                || System.getProperty("user.name").toLowerCase().equals("wrademakers")
                || System.getProperty("user.name").toLowerCase().equals("dhungs")
                || System.getProperty("user.name").toLowerCase().equals("rvandergaag")) {
            int reply = JOptionPane.showConfirmDialog(ContactPanel, "Are you sure?", "DELETE", JOptionPane.OK_CANCEL_OPTION);
            if (reply == JOptionPane.OK_OPTION) {
                int[] selected = masterTable.getSelectedRows();
                List<suppliers.Suppliers> toRemove = new ArrayList<suppliers.Suppliers>(selected.length);
                for (int idx = 0; idx < selected.length; idx++) {
                    suppliers.Suppliers s = list.get(masterTable.convertRowIndexToModel(selected[idx]));
                    toRemove.add(s);
                    entityManager.remove(s);
                }
                list.removeAll(toRemove);
                setSaveNeeded(true);
            }
        } else {
            JOptionPane.showMessageDialog(null, "You have no rights to delete any supplier", "No privileges", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Action(enabledProperty = "saveNeeded")
    public Task save() {
        return new SaveTask(getApplication());
    }

    private class SaveTask extends Task {

        SaveTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @Override
        protected Void doInBackground() {
            if (System.getProperty("user.name").toLowerCase().equals("rvandommelen") 
                    || System.getProperty("user.name").toLowerCase().equals("amaslowiec")  
                    || System.getProperty("user.name").toLowerCase().equals("pchukwu")
                    || System.getProperty("user.name").toLowerCase().equals("promisec")
                    || System.getProperty("user.name").toLowerCase().equals("wrademakers")
                    || System.getProperty("user.name").toLowerCase().equals("dhungs")
                    || System.getProperty("user.name").toLowerCase().equals("rvandergaag")) {
                try {
                    entityManager.getTransaction().commit();
                    entityManager.getTransaction().begin();
                } catch (RollbackException rex) {
                    rex.printStackTrace();
                    entityManager.getTransaction().begin();
                    List<suppliers.Suppliers> merged = new ArrayList<suppliers.Suppliers>(list.size());
                    for (suppliers.Suppliers s : list) {
                        merged.add(entityManager.merge(s));
                    }
                    list.clear();
                    list.addAll(merged);
                }
            }else {
            JOptionPane.showMessageDialog(null, "You have no rights to make any changes for suppliers", "No privileges", JOptionPane.ERROR_MESSAGE);
        }
            return null;

        }

        @Override
        protected void finished() {
            setSaveNeeded(false);
        }
    }

    /**
     * An example action method showing how to create asynchronous tasks
     * (running on background) and how to show their progress. Note the
     * artificial 'Thread.sleep' calls making the task long enough to see the
     * progress visualization - remove the sleeps for real application.
     */
    @Action
    public Task refresh() {
        return new RefreshTask(getApplication());
    }

    private class RefreshTask extends Task {

        RefreshTask(org.jdesktop.application.Application app) {
            super(app);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground() {
            try {
                setProgress(0, 0, 4);
                setMessage("Rolling back the current changes...");
                setProgress(1, 0, 4);
                entityManager.getTransaction().rollback();
                Thread.sleep(1000L); // remove for real app
                setProgress(2, 0, 4);

                setMessage("Starting a new transaction...");
                entityManager.getTransaction().begin();
                Thread.sleep(500L); // remove for real app
                setProgress(3, 0, 4);

                setMessage("Fetching new data...");
                java.util.Collection data = query.getResultList();
                for (Object entity : data) {
                    entityManager.refresh(entity);
                }
                Thread.sleep(1300L); // remove for real app
                setProgress(4, 0, 4);

                Thread.sleep(150L); // remove for real app
                list.clear();
                list.addAll(data);
            } catch (InterruptedException ignore) {
            }
            return null;
        }

        @Override
        protected void finished() {
            setMessage("Done.");
            setSaveNeeded(false);
        }
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = SuppliersApp.getApplication().getMainFrame();
            aboutBox = new SuppliersAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        SuppliersApp.getApplication().show(aboutBox);
    }

    @Action
    public void printFrame() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new Printable() {

            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
                if (pageIndex == 0) {
                    mainPanel.print(graphics);
                    return Printable.PAGE_EXISTS;
                }
                return Printable.NO_SUCH_PAGE;
            }
        });

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                // handle exception
            }
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        mainPanel = new javax.swing.JPanel();
        masterScrollPane = new javax.swing.JScrollPane();
        masterTable = new javax.swing.JTable();
        FilterTextField = new javax.swing.JTextField();
        supplierLabel = new javax.swing.JLabel();
        supplierField = new javax.swing.JTextField();
        SapLabel = new javax.swing.JLabel();
        SapTextField = new javax.swing.JTextField();
        logoLabel = new javax.swing.JLabel();
        ContactPanel = new javax.swing.JPanel();
        ContactLabel = new javax.swing.JLabel();
        OfficeLabel = new javax.swing.JLabel();
        Factory1Label = new javax.swing.JLabel();
        Factory2Label = new javax.swing.JLabel();
        Factory3Label = new javax.swing.JLabel();
        NoteLabel = new javax.swing.JLabel();
        SAPLabel = new javax.swing.JLabel();
        NameLabel = new javax.swing.JLabel();
        AddressLabel = new javax.swing.JLabel();
        CityLabel = new javax.swing.JLabel();
        StateLabel = new javax.swing.JLabel();
        CountryLabel = new javax.swing.JLabel();
        ZipLabel = new javax.swing.JLabel();
        WebLabel = new javax.swing.JLabel();
        DBIDLabel = new javax.swing.JLabel();
        officeVendor = new javax.swing.JTextField();
        officeName1 = new javax.swing.JFormattedTextField();
        officeName2 = new javax.swing.JFormattedTextField();
        officeAddress1 = new javax.swing.JFormattedTextField();
        officeAddress2 = new javax.swing.JFormattedTextField();
        officeAddress3 = new javax.swing.JFormattedTextField();
        officeCity = new javax.swing.JFormattedTextField();
        officeProvince = new javax.swing.JFormattedTextField();
        officeCountry = new javax.swing.JFormattedTextField();
        officeZip = new javax.swing.JFormattedTextField();
        officeWww = new javax.swing.JFormattedTextField();
        DBIDField = new javax.swing.JTextField();
        factory1Vendor = new javax.swing.JTextField();
        factory1Name1 = new javax.swing.JFormattedTextField();
        factory1Name2 = new javax.swing.JFormattedTextField();
        factory1Address1 = new javax.swing.JFormattedTextField();
        factory1Address2 = new javax.swing.JFormattedTextField();
        factory1Address3 = new javax.swing.JFormattedTextField();
        factory1City = new javax.swing.JFormattedTextField();
        factory1Province = new javax.swing.JFormattedTextField();
        factory1Country = new javax.swing.JFormattedTextField();
        factory1Zip = new javax.swing.JFormattedTextField();
        factory1Www = new javax.swing.JFormattedTextField();
        DBIDField1 = new javax.swing.JTextField();
        factory2Vendor = new javax.swing.JTextField();
        factory1Name3 = new javax.swing.JFormattedTextField();
        factory1Name4 = new javax.swing.JFormattedTextField();
        factory1Address4 = new javax.swing.JFormattedTextField();
        factory1Address5 = new javax.swing.JFormattedTextField();
        factory1Address6 = new javax.swing.JFormattedTextField();
        factory1City1 = new javax.swing.JFormattedTextField();
        factory1Province1 = new javax.swing.JFormattedTextField();
        factory1Country1 = new javax.swing.JFormattedTextField();
        factory1Zip1 = new javax.swing.JFormattedTextField();
        factory1Www1 = new javax.swing.JFormattedTextField();
        DBIDField2 = new javax.swing.JTextField();
        factory3Vendor = new javax.swing.JTextField();
        factory3Name1 = new javax.swing.JFormattedTextField();
        factory3Name2 = new javax.swing.JFormattedTextField();
        factory3Address1 = new javax.swing.JFormattedTextField();
        factory3Address2 = new javax.swing.JFormattedTextField();
        factory3Address3 = new javax.swing.JFormattedTextField();
        factory3City = new javax.swing.JFormattedTextField();
        factory3Province = new javax.swing.JFormattedTextField();
        factory3Country = new javax.swing.JFormattedTextField();
        factory3Zip = new javax.swing.JFormattedTextField();
        factory3Www = new javax.swing.JFormattedTextField();
        DBIDField3 = new javax.swing.JTextField();
        NoteScrollPane = new javax.swing.JScrollPane();
        jTextArea5 = new javax.swing.JTextArea();
        ContactNameLabel = new javax.swing.JLabel();
        EmailLabel = new javax.swing.JLabel();
        PhoneLabel = new javax.swing.JLabel();
        FunctionLabel = new javax.swing.JLabel();
        Contact1Label = new javax.swing.JLabel();
        contact1NameField = new javax.swing.JTextField();
        Contact2lLabel = new javax.swing.JLabel();
        contact2NameField = new javax.swing.JTextField();
        Contact3Label = new javax.swing.JLabel();
        contact3NameField = new javax.swing.JTextField();
        Contact4Label = new javax.swing.JLabel();
        contact4NameField = new javax.swing.JTextField();
        Contact5Label = new javax.swing.JLabel();
        contact5NameField = new javax.swing.JTextField();
        Contact6Label = new javax.swing.JLabel();
        contact6NameField = new javax.swing.JTextField();
        contact1EmailField = new javax.swing.JTextField();
        contact2EmailField = new javax.swing.JTextField();
        contact3EmailField = new javax.swing.JTextField();
        contact4EmailField = new javax.swing.JTextField();
        contact5EmailField = new javax.swing.JTextField();
        contact6EmailField = new javax.swing.JTextField();
        contact1PhoneField = new javax.swing.JTextField();
        contact2PhoneField = new javax.swing.JTextField();
        contact3PhoneField = new javax.swing.JTextField();
        contact4PhoneField = new javax.swing.JTextField();
        contact5PhoneField = new javax.swing.JTextField();
        contact6PhoneField = new javax.swing.JTextField();
        contact1FunctionField = new javax.swing.JTextField();
        contact2FunctionField = new javax.swing.JTextField();
        contact3FunctionField = new javax.swing.JTextField();
        contact4FunctionField = new javax.swing.JTextField();
        contact5FunctionField = new javax.swing.JTextField();
        contact6FunctionField = new javax.swing.JTextField();
        BsciPanel = new javax.swing.JPanel();
        bsciLabel = new javax.swing.JLabel();
        bsciPartLabel = new javax.swing.JLabel();
        bsciPartComboBox = new javax.swing.JComboBox();
        bsciResultLabel = new javax.swing.JLabel();
        bsciResultComboBox = new javax.swing.JComboBox();
        bsciFromLabel = new javax.swing.JLabel();
        bsciFromDateChooser = new com.toedter.calendar.JDateChooser();
        bsciFromButton = new javax.swing.JButton();
        bsciTillLabel = new javax.swing.JLabel();
        bsciTillDateChooser = new com.toedter.calendar.JDateChooser();
        bsciTillButton = new javax.swing.JButton();
        bsciOtherLabel = new javax.swing.JLabel();
        bsciOther1NameLabel = new javax.swing.JLabel();
        bsciOther2NameLabel = new javax.swing.JLabel();
        bsciOther3NameLabel = new javax.swing.JLabel();
        bsciOther1NameField = new javax.swing.JTextField();
        bsciOther2NameField = new javax.swing.JTextField();
        bsciOther3NameField = new javax.swing.JTextField();
        bsciOther1DateFrom = new com.toedter.calendar.JDateChooser();
        bsciOther1FromButton = new javax.swing.JButton();
        bsciOther2DateFrom = new com.toedter.calendar.JDateChooser();
        bsciOther2FromButton = new javax.swing.JButton();
        bsciOther3DateFrom = new com.toedter.calendar.JDateChooser();
        bsciOther3FromButton = new javax.swing.JButton();
        bsciOther1DateTill = new com.toedter.calendar.JDateChooser();
        bsciOther1TillButton = new javax.swing.JButton();
        bsciOther2DateTill = new com.toedter.calendar.JDateChooser();
        bsciOther2TillButton = new javax.swing.JButton();
        bsciOther3DateTill = new com.toedter.calendar.JDateChooser();
        bsciOther3TillButton = new javax.swing.JButton();
        bsciOtherFromLabel = new javax.swing.JLabel();
        bsciOtherTillLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        FOBCheckBox = new javax.swing.JCheckBox();
        buyerLabel = new javax.swing.JLabel();
        buyerComboBox = new javax.swing.JComboBox();
        qmLabel = new javax.swing.JLabel();
        qmComboBox = new javax.swing.JComboBox();
        CePanel = new javax.swing.JPanel();
        certLabel = new javax.swing.JLabel();
        certIso9000TextField = new javax.swing.JTextField();
        CertIso9001DateFrom = new com.toedter.calendar.JDateChooser();
        CertIso140000DateFrom = new com.toedter.calendar.JDateChooser();
        certIso14000TextField = new javax.swing.JTextField();
        CertOther1DateFrom = new com.toedter.calendar.JDateChooser();
        CertOther2DateFrom = new com.toedter.calendar.JDateChooser();
        CertOther3DateFrom = new com.toedter.calendar.JDateChooser();
        CertFromLabel = new javax.swing.JLabel();
        CertTillLabel = new javax.swing.JLabel();
        CertIso9001DateTill = new com.toedter.calendar.JDateChooser();
        CertIso140000DateTill = new com.toedter.calendar.JDateChooser();
        CertOther1DateTill = new com.toedter.calendar.JDateChooser();
        CertOther2DateTill = new com.toedter.calendar.JDateChooser();
        CertOther3DateTill = new com.toedter.calendar.JDateChooser();
        CertNameLabel = new javax.swing.JLabel();
        certIso9000NameField = new javax.swing.JTextField();
        certIso14000NameField = new javax.swing.JTextField();
        certOther1NameField = new javax.swing.JTextField();
        certOther2NameField = new javax.swing.JTextField();
        certOther3NameField = new javax.swing.JTextField();
        CertOther1TextField = new javax.swing.JTextField();
        CertOther2TextField = new javax.swing.JTextField();
        CertOther6NameField = new javax.swing.JTextField();
        CertIso9001FromButton = new javax.swing.JButton();
        CertIso140000FromButton = new javax.swing.JButton();
        CertOther1FromButton = new javax.swing.JButton();
        CertOther2FromButton = new javax.swing.JButton();
        CertOther3FromButton = new javax.swing.JButton();
        CertIso9001TillButton = new javax.swing.JButton();
        CertIso140000TillButton = new javax.swing.JButton();
        CertOther1TillButton = new javax.swing.JButton();
        CertOther2TillButton = new javax.swing.JButton();
        CertOther3TillButton = new javax.swing.JButton();
        CertFromLabel1 = new javax.swing.JLabel();
        CertTillLabel1 = new javax.swing.JLabel();
        CertNameLabel1 = new javax.swing.JLabel();
        CertOther7NameField = new javax.swing.JTextField();
        CertOther4DateFrom = new com.toedter.calendar.JDateChooser();
        CertOther4FromButton = new javax.swing.JButton();
        CertOther4DateTill = new com.toedter.calendar.JDateChooser();
        CertOther4TillButton = new javax.swing.JButton();
        certOther4NameField = new javax.swing.JTextField();
        CertOther8NameField = new javax.swing.JTextField();
        CertOther5DateFrom = new com.toedter.calendar.JDateChooser();
        CertOther5FromButton = new javax.swing.JButton();
        CertOther5DateTill = new com.toedter.calendar.JDateChooser();
        CertOther5TillButton = new javax.swing.JButton();
        certOther5NameField = new javax.swing.JTextField();
        CertOther6DateFrom = new com.toedter.calendar.JDateChooser();
        CertOther6FromButton = new javax.swing.JButton();
        CertOther6DateTill = new com.toedter.calendar.JDateChooser();
        CertOther6TillButton = new javax.swing.JButton();
        CertFromLabel2 = new javax.swing.JLabel();
        CertTillLabel2 = new javax.swing.JLabel();
        CertNameLabel2 = new javax.swing.JLabel();
        CertOther7DateFrom = new com.toedter.calendar.JDateChooser();
        CertOther8DateFrom = new com.toedter.calendar.JDateChooser();
        CertOther9DateFrom = new com.toedter.calendar.JDateChooser();
        CertOther10DateFrom = new com.toedter.calendar.JDateChooser();
        CertOther7DateTill = new com.toedter.calendar.JDateChooser();
        CertOther8DateTill = new com.toedter.calendar.JDateChooser();
        CertOther9DateTill = new com.toedter.calendar.JDateChooser();
        CertOther10DateTill = new com.toedter.calendar.JDateChooser();
        CertOther7FromButton = new javax.swing.JButton();
        CertOther8FromButton = new javax.swing.JButton();
        CertOther9FromButton = new javax.swing.JButton();
        CertOther10FromButton = new javax.swing.JButton();
        CertOther3TillButton1 = new javax.swing.JButton();
        CertOther4TillButton1 = new javax.swing.JButton();
        CertOther5TillButton1 = new javax.swing.JButton();
        CertOther6TillButton1 = new javax.swing.JButton();
        CertOther9NameField = new javax.swing.JTextField();
        CertOther10NameField = new javax.swing.JTextField();
        CertOther3TextField = new javax.swing.JTextField();
        CertOther4TextField = new javax.swing.JTextField();
        CertOther5TextField = new javax.swing.JTextField();
        CertOther6TextField = new javax.swing.JTextField();
        CertOther7TextField = new javax.swing.JTextField();
        CertOther8TextField = new javax.swing.JTextField();
        CertOther9TextField = new javax.swing.JTextField();
        CertOther10TextField = new javax.swing.JTextField();
        DeclPanel = new javax.swing.JPanel();
        declLabel = new javax.swing.JLabel();
        declRecLabel = new javax.swing.JLabel();
        declSigLabel = new javax.swing.JLabel();
        declBrandLabel = new javax.swing.JLabel();
        DeclBrandCheckBox = new javax.swing.JCheckBox();
        DeclBrandDateChooser = new com.toedter.calendar.JDateChooser();
        DeclBrandButton = new javax.swing.JButton();
        declPackLabel = new javax.swing.JLabel();
        DeclPackCheckBox = new javax.swing.JCheckBox();
        DeclPackDateChooser = new com.toedter.calendar.JDateChooser();
        DeclPackButton = new javax.swing.JButton();
        declContrLabel = new javax.swing.JLabel();
        DeclContrCheckBox = new javax.swing.JCheckBox();
        DeclContrDateChooser = new com.toedter.calendar.JDateChooser();
        DeclContrButton = new javax.swing.JButton();
        declReachLabel = new javax.swing.JLabel();
        declReachCheckBox = new javax.swing.JCheckBox();
        declReachDateChooser = new com.toedter.calendar.JDateChooser();
        DeclReachButton = new javax.swing.JButton();
        declRohsLabel = new javax.swing.JLabel();
        declRohsCheckBox = new javax.swing.JCheckBox();
        declRohsDateChooser = new com.toedter.calendar.JDateChooser();
        DeclRohsButton = new javax.swing.JButton();
        declSdaLabel = new javax.swing.JLabel();
        declSdaCheckBox = new javax.swing.JCheckBox();
        declSdaDateChooser = new com.toedter.calendar.JDateChooser();
        declSdaButton = new javax.swing.JButton();
        declSopLabel = new javax.swing.JLabel();
        declSopCheckBox = new javax.swing.JCheckBox();
        declSopDateChooser = new com.toedter.calendar.JDateChooser();
        declSopButton = new javax.swing.JButton();
        declWarrantyLabel = new javax.swing.JLabel();
        declWarrantyScrollPane = new javax.swing.JScrollPane();
        declWarrantyTextArea = new javax.swing.JTextArea();
        declRecLabel1 = new javax.swing.JLabel();
        declSigLabel1 = new javax.swing.JLabel();
        declPayLabel = new javax.swing.JLabel();
        declPayComboBox = new javax.swing.JComboBox();
        declPayLabel1 = new javax.swing.JLabel();
        SapLabel1 = new javax.swing.JLabel();
        SapTextField1 = new javax.swing.JTextField();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem newRecordMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem deleteRecordMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        javax.swing.JMenuItem saveMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem refreshMenuItem = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        FolderButton = new javax.swing.JButton();
        folderTextField = new javax.swing.JTextField();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(suppliers.SuppliersApp.class).getContext().getResourceMap(SuppliersView.class);
        entityManager = java.beans.Beans.isDesignTime() ? null : javax.persistence.Persistence.createEntityManagerFactory(resourceMap.getString("entityManager.persistenceUnit")).createEntityManager(); // NOI18N
        query = java.beans.Beans.isDesignTime() ? null : entityManager.createQuery(resourceMap.getString("query.query")); // NOI18N
        list = java.beans.Beans.isDesignTime() ? java.util.Collections.emptyList() : org.jdesktop.observablecollections.ObservableCollections.observableList(query.getResultList());
        rowSorterToStringConverter1 = new suppliers.RowSorterToStringConverter();

        mainPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new java.awt.Dimension(1000, 753));
        mainPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        masterScrollPane.setName("masterScrollPane"); // NOI18N

        masterTable.setFont(resourceMap.getFont("masterTable.font")); // NOI18N
        masterTable.setName("masterTable"); // NOI18N

        org.jdesktop.swingbinding.JTableBinding jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, list, masterTable);
        org.jdesktop.swingbinding.JTableBinding.ColumnBinding columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${supplier}"));
        columnBinding.setColumnName("Supplier");
        columnBinding.setColumnClass(String.class);
        columnBinding.setEditable(false);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${officeName}"));
        columnBinding.setColumnName("Office Name");
        columnBinding.setColumnClass(String.class);
        columnBinding.setEditable(false);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${officeVendor}"));
        columnBinding.setColumnName("Office Vendor");
        columnBinding.setColumnClass(Integer.class);
        columnBinding.setEditable(false);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${officeVendorSFE}"));
        columnBinding.setColumnName("Office Vendor SFE");
        columnBinding.setColumnClass(Integer.class);
        columnBinding.setEditable(false);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${factory1Name}"));
        columnBinding.setColumnName("Factory1 Name");
        columnBinding.setColumnClass(String.class);
        columnBinding.setEditable(false);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${factory2Name}"));
        columnBinding.setColumnName("Factory2 Name");
        columnBinding.setColumnClass(String.class);
        columnBinding.setEditable(false);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${factory3Name}"));
        columnBinding.setColumnName("Factory3 Name");
        columnBinding.setColumnClass(String.class);
        columnBinding.setEditable(false);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${vendorName}"));
        columnBinding.setColumnName("Vendor Name");
        columnBinding.setColumnClass(String.class);
        columnBinding.setEditable(false);
        bindingGroup.addBinding(jTableBinding);
        jTableBinding.bind();
        masterScrollPane.setViewportView(masterTable);
        masterTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("masterTable.columnModel.title0")); // NOI18N
        masterTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("masterTable.columnModel.title1")); // NOI18N
        masterTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("masterTable.columnModel.title6")); // NOI18N
        masterTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("masterTable.columnModel.title7")); // NOI18N
        masterTable.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("masterTable.columnModel.title2")); // NOI18N
        masterTable.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("masterTable.columnModel.title3")); // NOI18N
        masterTable.getColumnModel().getColumn(6).setHeaderValue(resourceMap.getString("masterTable.columnModel.title4")); // NOI18N
        masterTable.getColumnModel().getColumn(7).setHeaderValue(resourceMap.getString("masterTable.columnModel.title5")); // NOI18N

        mainPanel.add(masterScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 140, 625));

        FilterTextField.setName("FilterTextField"); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${rowSorter}"), FilterTextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setConverter(rowSorterToStringConverter1);
        bindingGroup.addBinding(binding);

        mainPanel.add(FilterTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 110, -1));

        supplierLabel.setText(resourceMap.getString("supplierLabel.text")); // NOI18N
        supplierLabel.setName("supplierLabel"); // NOI18N
        supplierLabel.setPreferredSize(new java.awt.Dimension(80, 23));
        mainPanel.add(supplierLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(195, 10, 50, -1));

        supplierField.setFont(resourceMap.getFont("supplierField.font")); // NOI18N
        supplierField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        supplierField.setName("supplierField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.supplier}"), supplierField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceUnreadableValue(null);
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), supplierField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        mainPanel.add(supplierField, new org.netbeans.lib.awtextra.AbsoluteConstraints(245, 10, 630, -1));

        SapLabel.setText(resourceMap.getString("SapLabel.text")); // NOI18N
        SapLabel.setName("SapLabel"); // NOI18N
        SapLabel.setPreferredSize(new java.awt.Dimension(80, 26));
        mainPanel.add(SapLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 10, 70, -1));

        SapTextField.setFont(resourceMap.getFont("SapTextField.font")); // NOI18N
        SapTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        SapTextField.setName("SapTextField"); // NOI18N
        SapTextField.setPreferredSize(new java.awt.Dimension(59, 26));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.officeVendor}"), SapTextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), SapTextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        mainPanel.add(SapTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(1000, 10, 90, -1));

        logoLabel.setIcon(resourceMap.getIcon("logoLabel.icon")); // NOI18N
        logoLabel.setText(resourceMap.getString("logoLabel.text")); // NOI18N
        logoLabel.setName("logoLabel"); // NOI18N
        mainPanel.add(logoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(1330, 0, -1, 40));

        ContactPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(-16777216,true)));
        ContactPanel.setName("ContactPanel"); // NOI18N
        binding.setSourceUnreadableValue(null);
        ContactPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        ContactLabel.setFont(resourceMap.getFont("ContactLabel.font")); // NOI18N
        ContactLabel.setText(resourceMap.getString("ContactLabel.text")); // NOI18N
        ContactLabel.setName("ContactLabel"); // NOI18N
        ContactPanel.add(ContactLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 1, -1, -1));

        OfficeLabel.setFont(resourceMap.getFont("OfficeLabel.font")); // NOI18N
        OfficeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        OfficeLabel.setText(resourceMap.getString("OfficeLabel.text")); // NOI18N
        OfficeLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        OfficeLabel.setName("OfficeLabel"); // NOI18N
        OfficeLabel.setPreferredSize(new java.awt.Dimension(80, 14));
        ContactPanel.add(OfficeLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 7, 230, -1));
        OfficeLabel.getAccessibleContext().setAccessibleParent(ContactPanel);

        Factory1Label.setFont(resourceMap.getFont("OfficeLabel.font")); // NOI18N
        Factory1Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Factory1Label.setText(resourceMap.getString("Factory1Label.text")); // NOI18N
        Factory1Label.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        Factory1Label.setName("Factory1Label"); // NOI18N
        Factory1Label.setPreferredSize(new java.awt.Dimension(80, 14));
        ContactPanel.add(Factory1Label, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 7, 230, -1));
        Factory1Label.getAccessibleContext().setAccessibleParent(ContactPanel);

        Factory2Label.setFont(resourceMap.getFont("OfficeLabel.font")); // NOI18N
        Factory2Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Factory2Label.setText(resourceMap.getString("Factory2Label.text")); // NOI18N
        Factory2Label.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        Factory2Label.setName("Factory2Label"); // NOI18N
        Factory2Label.setPreferredSize(new java.awt.Dimension(80, 14));
        ContactPanel.add(Factory2Label, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 7, 230, -1));
        Factory2Label.getAccessibleContext().setAccessibleParent(ContactPanel);

        Factory3Label.setFont(resourceMap.getFont("OfficeLabel.font")); // NOI18N
        Factory3Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Factory3Label.setText(resourceMap.getString("Factory3Label.text")); // NOI18N
        Factory3Label.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        Factory3Label.setName("Factory3Label"); // NOI18N
        Factory3Label.setPreferredSize(new java.awt.Dimension(80, 14));
        ContactPanel.add(Factory3Label, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 7, 230, -1));
        Factory3Label.getAccessibleContext().setAccessibleParent(ContactPanel);

        NoteLabel.setFont(resourceMap.getFont("OfficeLabel.font")); // NOI18N
        NoteLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        NoteLabel.setText(resourceMap.getString("NoteLabel.text")); // NOI18N
        NoteLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        NoteLabel.setName("NoteLabel"); // NOI18N
        NoteLabel.setPreferredSize(new java.awt.Dimension(80, 14));
        ContactPanel.add(NoteLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 8, 240, -1));
        NoteLabel.getAccessibleContext().setAccessibleParent(ContactPanel);

        SAPLabel.setText(resourceMap.getString("SAPLabel.text")); // NOI18N
        SAPLabel.setName("SAPLabel"); // NOI18N
        SAPLabel.setPreferredSize(new java.awt.Dimension(80, 12));
        ContactPanel.add(SAPLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 21, -1, -1));

        NameLabel.setText(resourceMap.getString("NameLabel.text")); // NOI18N
        NameLabel.setName("NameLabel"); // NOI18N
        NameLabel.setPreferredSize(new java.awt.Dimension(80, 12));
        ContactPanel.add(NameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, -1, -1));
        NameLabel.getAccessibleContext().setAccessibleParent(ContactPanel);

        AddressLabel.setText(resourceMap.getString("AddressLabel.text")); // NOI18N
        AddressLabel.setName("AddressLabel"); // NOI18N
        AddressLabel.setPreferredSize(new java.awt.Dimension(80, 12));
        ContactPanel.add(AddressLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 69, -1, -1));
        AddressLabel.getAccessibleContext().setAccessibleParent(ContactPanel);

        CityLabel.setText(resourceMap.getString("CityLabel.text")); // NOI18N
        CityLabel.setName("CityLabel"); // NOI18N
        CityLabel.setPreferredSize(new java.awt.Dimension(80, 12));
        ContactPanel.add(CityLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 107, 80, -1));

        StateLabel.setText(resourceMap.getString("StateLabel.text")); // NOI18N
        StateLabel.setName("StateLabel"); // NOI18N
        StateLabel.setPreferredSize(new java.awt.Dimension(80, 12));
        ContactPanel.add(StateLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 121, -1, -1));
        StateLabel.getAccessibleContext().setAccessibleParent(ContactPanel);

        CountryLabel.setText(resourceMap.getString("CountryLabel.text")); // NOI18N
        CountryLabel.setName("CountryLabel"); // NOI18N
        CountryLabel.setPreferredSize(new java.awt.Dimension(80, 12));
        ContactPanel.add(CountryLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 135, -1, -1));
        CountryLabel.getAccessibleContext().setAccessibleParent(ContactPanel);

        ZipLabel.setText(resourceMap.getString("ZipLabel.text")); // NOI18N
        ZipLabel.setName("ZipLabel"); // NOI18N
        ZipLabel.setPreferredSize(new java.awt.Dimension(80, 12));
        ContactPanel.add(ZipLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 149, -1, -1));
        ZipLabel.getAccessibleContext().setAccessibleParent(ContactPanel);

        WebLabel.setText(resourceMap.getString("WebLabel.text")); // NOI18N
        WebLabel.setName("WebLabel"); // NOI18N
        WebLabel.setPreferredSize(new java.awt.Dimension(80, 12));
        ContactPanel.add(WebLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 163, -1, -1));

        DBIDLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        DBIDLabel.setText(resourceMap.getString("DBIDLabel.text")); // NOI18N
        DBIDLabel.setName("DBIDLabel"); // NOI18N
        DBIDLabel.setPreferredSize(new java.awt.Dimension(80, 14));
        ContactPanel.add(DBIDLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, 80, -1));

        officeVendor.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        officeVendor.setName("officeVendor"); // NOI18N
        officeVendor.setPreferredSize(new java.awt.Dimension(6, 14));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.officeVendor}"), officeVendor, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), officeVendor, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(officeVendor, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 21, 230, -1));

        officeName1.setBorder(null);
        try {
            officeName1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        officeName1.setName("officeName1"); // NOI18N
        officeName1.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.officeName1}"), officeName1, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(officeName1, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 40, 230, -1));

        officeName2.setBorder(null);
        try {
            officeName2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        officeName2.setName("officeName2"); // NOI18N
        officeName2.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.officeName2}"), officeName2, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(officeName2, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 52, 230, -1));

        officeAddress1.setBorder(null);
        try {
            officeAddress1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        officeAddress1.setName("officeAddress1"); // NOI18N
        officeAddress1.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.officeAddress1}"), officeAddress1, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(officeAddress1, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 69, 230, -1));

        officeAddress2.setBorder(null);
        try {
            officeAddress2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        officeAddress2.setName("officeAddress2"); // NOI18N
        officeAddress2.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.officeAddress2}"), officeAddress2, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(officeAddress2, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 81, 230, -1));

        officeAddress3.setBorder(null);
        try {
            officeAddress3.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        officeAddress3.setName("officeAddress3"); // NOI18N
        officeAddress3.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.officeAddress3}"), officeAddress3, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(officeAddress3, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 93, 230, -1));

        officeCity.setBorder(null);
        try {
            officeCity.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        officeCity.setName("officeCity"); // NOI18N
        officeCity.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.officeCity}"), officeCity, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(officeCity, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 107, 230, -1));

        officeProvince.setBorder(null);
        try {
            officeProvince.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        officeProvince.setName("officeProvince"); // NOI18N
        officeProvince.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.officeProvince}"), officeProvince, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(officeProvince, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 121, 230, -1));

        officeCountry.setBorder(null);
        try {
            officeCountry.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        officeCountry.setName("officeCountry"); // NOI18N
        officeCountry.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.officeCountry}"), officeCountry, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(officeCountry, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 135, 230, -1));

        officeZip.setBorder(null);
        try {
            officeZip.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        officeZip.setName("officeZip"); // NOI18N
        officeZip.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.officeZip}"), officeZip, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(officeZip, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 149, 230, -1));

        officeWww.setBorder(null);
        try {
            officeWww.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        officeWww.setName("officeWww"); // NOI18N
        officeWww.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.officeWww}"), officeWww, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(officeWww, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 163, 230, -1));

        DBIDField.setName("DBIDField"); // NOI18N
        DBIDField.setPreferredSize(new java.awt.Dimension(6, 14));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.DBID}"), DBIDField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), DBIDField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(DBIDField, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 180, 230, -1));

        factory1Vendor.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        factory1Vendor.setName("factory1Vendor"); // NOI18N
        factory1Vendor.setPreferredSize(new java.awt.Dimension(6, 14));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory1Vendor}"), factory1Vendor, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), factory1Vendor, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Vendor, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 21, 230, -1));

        factory1Name1.setBorder(null);
        try {
            factory1Name1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1Name1.setName("factory1Name1"); // NOI18N
        factory1Name1.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory1Name1}"), factory1Name1, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Name1, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 40, 230, -1));

        factory1Name2.setBorder(null);
        try {
            factory1Name2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1Name2.setName("factory1Name2"); // NOI18N
        factory1Name2.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory1Name2}"), factory1Name2, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Name2, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 52, 230, -1));

        factory1Address1.setBorder(null);
        try {
            factory1Address1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1Address1.setName("factory1Address1"); // NOI18N
        factory1Address1.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory1Address1}"), factory1Address1, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Address1, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 69, 230, -1));

        factory1Address2.setBorder(null);
        try {
            factory1Address2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1Address2.setName("factory1Address2"); // NOI18N
        factory1Address2.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory1Address2}"), factory1Address2, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Address2, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 81, 230, -1));

        factory1Address3.setBorder(null);
        try {
            factory1Address3.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1Address3.setName("factory1Address3"); // NOI18N
        factory1Address3.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory1Address3}"), factory1Address3, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Address3, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 93, 230, -1));

        factory1City.setBorder(null);
        try {
            factory1City.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1City.setName("factory1City"); // NOI18N
        factory1City.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory1City}"), factory1City, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1City, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 107, 230, -1));

        factory1Province.setBorder(null);
        try {
            factory1Province.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1Province.setName("factory1Province"); // NOI18N
        factory1Province.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory1Province}"), factory1Province, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Province, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 121, 230, -1));

        factory1Country.setBorder(null);
        try {
            factory1Country.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1Country.setName("factory1Country"); // NOI18N
        factory1Country.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory1Country}"), factory1Country, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Country, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 135, 230, -1));

        factory1Zip.setBorder(null);
        try {
            factory1Zip.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1Zip.setName("factory1Zip"); // NOI18N
        factory1Zip.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory1Zip}"), factory1Zip, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Zip, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 149, 230, -1));

        factory1Www.setBorder(null);
        try {
            factory1Www.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1Www.setName("factory1Www"); // NOI18N
        factory1Www.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory1Www}"), factory1Www, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Www, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 163, 230, -1));

        DBIDField1.setName("DBIDField1"); // NOI18N
        DBIDField1.setPreferredSize(new java.awt.Dimension(6, 14));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.DBID1}"), DBIDField1, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), DBIDField1, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(DBIDField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 180, 230, -1));

        factory2Vendor.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        factory2Vendor.setName("factory2Vendor"); // NOI18N
        factory2Vendor.setPreferredSize(new java.awt.Dimension(6, 14));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory2Vendor}"), factory2Vendor, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), factory2Vendor, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory2Vendor, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 21, 230, -1));

        factory1Name3.setBorder(null);
        try {
            factory1Name3.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1Name3.setName("factory1Name3"); // NOI18N
        factory1Name3.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory2Name1}"), factory1Name3, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Name3, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 40, 230, -1));

        factory1Name4.setBorder(null);
        try {
            factory1Name4.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1Name4.setName("factory1Name4"); // NOI18N
        factory1Name4.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory2Name2}"), factory1Name4, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Name4, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 52, 230, -1));

        factory1Address4.setBorder(null);
        try {
            factory1Address4.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1Address4.setName("factory1Address4"); // NOI18N
        factory1Address4.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory2Address1}"), factory1Address4, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Address4, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 69, 230, -1));

        factory1Address5.setBorder(null);
        try {
            factory1Address5.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1Address5.setName("factory1Address5"); // NOI18N
        factory1Address5.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory2Address2}"), factory1Address5, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Address5, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 81, 230, -1));

        factory1Address6.setBorder(null);
        try {
            factory1Address6.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1Address6.setName("factory1Address6"); // NOI18N
        factory1Address6.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory2Address3}"), factory1Address6, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Address6, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 93, 230, -1));

        factory1City1.setBorder(null);
        try {
            factory1City1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1City1.setName("factory1City1"); // NOI18N
        factory1City1.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory2City}"), factory1City1, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1City1, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 107, 230, -1));

        factory1Province1.setBorder(null);
        try {
            factory1Province1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1Province1.setName("factory1Province1"); // NOI18N
        factory1Province1.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory2Province}"), factory1Province1, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Province1, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 121, 230, -1));

        factory1Country1.setBorder(null);
        try {
            factory1Country1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1Country1.setName("factory1Country1"); // NOI18N
        factory1Country1.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory2Country}"), factory1Country1, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Country1, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 135, 230, -1));

        factory1Zip1.setBorder(null);
        try {
            factory1Zip1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1Zip1.setName("factory1Zip1"); // NOI18N
        factory1Zip1.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory2Zip}"), factory1Zip1, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Zip1, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 149, 230, -1));

        factory1Www1.setBorder(null);
        try {
            factory1Www1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory1Www1.setName("factory1Www1"); // NOI18N
        factory1Www1.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory2Www}"), factory1Www1, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory1Www1, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 163, 230, -1));

        DBIDField2.setName("DBIDField2"); // NOI18N
        DBIDField2.setPreferredSize(new java.awt.Dimension(6, 14));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.DBID2}"), DBIDField2, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), DBIDField2, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(DBIDField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 180, 230, -1));

        factory3Vendor.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        factory3Vendor.setName("factory3Vendor"); // NOI18N
        factory3Vendor.setPreferredSize(new java.awt.Dimension(6, 14));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory3Vendor}"), factory3Vendor, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), factory3Vendor, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory3Vendor, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 21, 230, -1));

        factory3Name1.setBorder(null);
        try {
            factory3Name1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory3Name1.setName("factory3Name1"); // NOI18N
        factory3Name1.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory3Name1}"), factory3Name1, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory3Name1, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 40, 230, -1));

        factory3Name2.setBorder(null);
        try {
            factory3Name2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory3Name2.setName("factory3Name2"); // NOI18N
        factory3Name2.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory3Name2}"), factory3Name2, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory3Name2, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 52, 230, -1));

        factory3Address1.setBorder(null);
        try {
            factory3Address1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory3Address1.setName("factory3Address1"); // NOI18N
        factory3Address1.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory3Address1}"), factory3Address1, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory3Address1, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 69, 230, -1));

        factory3Address2.setBorder(null);
        try {
            factory3Address2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory3Address2.setName("factory3Address2"); // NOI18N
        factory3Address2.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory3Address2}"), factory3Address2, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory3Address2, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 81, 230, -1));

        factory3Address3.setBorder(null);
        try {
            factory3Address3.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory3Address3.setName("factory3Address3"); // NOI18N
        factory3Address3.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory3Address3}"), factory3Address3, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory3Address3, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 93, 230, -1));

        factory3City.setBorder(null);
        try {
            factory3City.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory3City.setName("factory3City"); // NOI18N
        factory3City.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory3City}"), factory3City, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory3City, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 107, 230, -1));

        factory3Province.setBorder(null);
        try {
            factory3Province.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory3Province.setName("factory3Province"); // NOI18N
        factory3Province.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory3Province}"), factory3Province, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory3Province, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 121, 230, -1));

        factory3Country.setBorder(null);
        try {
            factory3Country.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory3Country.setName("factory3Country"); // NOI18N
        factory3Country.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory3Country}"), factory3Country, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory3Country, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 135, 230, -1));

        factory3Zip.setBorder(null);
        try {
            factory3Zip.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory3Zip.setName("factory3Zip"); // NOI18N
        factory3Zip.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory3Zip}"), factory3Zip, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory3Zip, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 149, 230, -1));

        factory3Www.setBorder(null);
        try {
            factory3Www.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("***********************************")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        factory3Www.setName("factory3Www"); // NOI18N
        factory3Www.setPreferredSize(new java.awt.Dimension(109, 12));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.factory3Www}"), factory3Www, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(factory3Www, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 163, 230, -1));

        DBIDField3.setName("DBIDField3"); // NOI18N
        DBIDField3.setPreferredSize(new java.awt.Dimension(6, 14));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.DBID3}"), DBIDField3, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), DBIDField3, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(DBIDField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 180, 230, -1));

        NoteScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        NoteScrollPane.setName("NoteScrollPane"); // NOI18N

        jTextArea5.setColumns(20);
        jTextArea5.setLineWrap(true);
        jTextArea5.setRows(10);
        jTextArea5.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jTextArea5.setName("jTextArea5"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.note}"), jTextArea5, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        binding.setSourceUnreadableValue(null);
        NoteScrollPane.setViewportView(jTextArea5);

        ContactPanel.add(NoteScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 21, 240, 299));

        ContactNameLabel.setFont(resourceMap.getFont("ContactNameLabel.font")); // NOI18N
        ContactNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ContactNameLabel.setText(resourceMap.getString("ContactNameLabel.text")); // NOI18N
        ContactNameLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        ContactNameLabel.setName("ContactNameLabel"); // NOI18N
        ContactNameLabel.setPreferredSize(new java.awt.Dimension(80, 14));
        ContactPanel.add(ContactNameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 200, 60, -1));
        ContactNameLabel.getAccessibleContext().setAccessibleParent(ContactPanel);

        EmailLabel.setFont(resourceMap.getFont("ContactNameLabel.font")); // NOI18N
        EmailLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        EmailLabel.setText(resourceMap.getString("EmailLabel.text")); // NOI18N
        EmailLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        EmailLabel.setName("EmailLabel"); // NOI18N
        EmailLabel.setPreferredSize(new java.awt.Dimension(80, 14));
        ContactPanel.add(EmailLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 200, 230, -1));
        EmailLabel.getAccessibleContext().setAccessibleParent(ContactPanel);

        PhoneLabel.setFont(resourceMap.getFont("ContactNameLabel.font")); // NOI18N
        PhoneLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        PhoneLabel.setText(resourceMap.getString("PhoneLabel.text")); // NOI18N
        PhoneLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        PhoneLabel.setName("PhoneLabel"); // NOI18N
        PhoneLabel.setPreferredSize(new java.awt.Dimension(80, 14));
        ContactPanel.add(PhoneLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 200, 230, -1));
        PhoneLabel.getAccessibleContext().setAccessibleParent(ContactPanel);

        FunctionLabel.setFont(resourceMap.getFont("ContactNameLabel.font")); // NOI18N
        FunctionLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        FunctionLabel.setText(resourceMap.getString("FunctionLabel.text")); // NOI18N
        FunctionLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        FunctionLabel.setName("FunctionLabel"); // NOI18N
        FunctionLabel.setPreferredSize(new java.awt.Dimension(80, 14));
        ContactPanel.add(FunctionLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 200, 230, -1));
        FunctionLabel.getAccessibleContext().setAccessibleParent(ContactPanel);

        Contact1Label.setText(resourceMap.getString("Contact1Label.text")); // NOI18N
        Contact1Label.setName("Contact1Label"); // NOI18N
        Contact1Label.setPreferredSize(new java.awt.Dimension(80, 18));
        ContactPanel.add(Contact1Label, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 212, -1, -1));
        Contact1Label.getAccessibleContext().setAccessibleParent(ContactPanel);

        contact1NameField.setName("contact1NameField"); // NOI18N
        contact1NameField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact1Name}"), contact1NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceUnreadableValue(null);
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact1NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact1NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(91, 212, 230, -1));
        contact1NameField.getAccessibleContext().setAccessibleParent(ContactPanel);

        Contact2lLabel.setText(resourceMap.getString("Contact2lLabel.text")); // NOI18N
        Contact2lLabel.setName("Contact2lLabel"); // NOI18N
        Contact2lLabel.setPreferredSize(new java.awt.Dimension(80, 18));
        ContactPanel.add(Contact2lLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 230, -1, -1));
        Contact2lLabel.getAccessibleContext().setAccessibleParent(ContactPanel);

        contact2NameField.setName("contact2NameField"); // NOI18N
        contact2NameField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact2Name}"), contact2NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceUnreadableValue(null);
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact2NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact2NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(91, 230, 230, -1));
        contact2NameField.getAccessibleContext().setAccessibleParent(ContactPanel);

        Contact3Label.setText(resourceMap.getString("Contact3Label.text")); // NOI18N
        Contact3Label.setName("Contact3Label"); // NOI18N
        Contact3Label.setPreferredSize(new java.awt.Dimension(80, 18));
        ContactPanel.add(Contact3Label, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 248, -1, -1));
        Contact3Label.getAccessibleContext().setAccessibleParent(ContactPanel);

        contact3NameField.setName("contact3NameField"); // NOI18N
        contact3NameField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact3Name}"), contact3NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceUnreadableValue(null);
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact3NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact3NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(91, 248, 230, -1));
        contact3NameField.getAccessibleContext().setAccessibleParent(ContactPanel);

        Contact4Label.setText(resourceMap.getString("Contact4Label.text")); // NOI18N
        Contact4Label.setName("Contact4Label"); // NOI18N
        Contact4Label.setPreferredSize(new java.awt.Dimension(80, 18));
        ContactPanel.add(Contact4Label, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 266, -1, -1));
        Contact4Label.getAccessibleContext().setAccessibleName(resourceMap.getString("Contact3Label1.AccessibleContext.accessibleName")); // NOI18N

        contact4NameField.setName("contact4NameField"); // NOI18N
        contact4NameField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact4Name}"), contact4NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceUnreadableValue(null);
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact4NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact4NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(91, 266, 230, -1));
        contact4NameField.getAccessibleContext().setAccessibleParent(ContactPanel);

        Contact5Label.setText(resourceMap.getString("Contact5Label.text")); // NOI18N
        Contact5Label.setName("Contact5Label"); // NOI18N
        Contact5Label.setPreferredSize(new java.awt.Dimension(80, 18));
        ContactPanel.add(Contact5Label, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 284, -1, -1));

        contact5NameField.setName("contact5NameField"); // NOI18N
        contact5NameField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact5Name}"), contact5NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceUnreadableValue(null);
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact5NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact5NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(91, 284, 230, -1));
        contact5NameField.getAccessibleContext().setAccessibleParent(ContactPanel);

        Contact6Label.setText(resourceMap.getString("Contact6Label.text")); // NOI18N
        Contact6Label.setName("Contact6Label"); // NOI18N
        Contact6Label.setPreferredSize(new java.awt.Dimension(80, 18));
        ContactPanel.add(Contact6Label, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 302, -1, -1));

        contact6NameField.setName("contact6NameField"); // NOI18N
        contact6NameField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact6Name}"), contact6NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceUnreadableValue(null);
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact6NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact6NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(91, 302, 230, -1));
        contact6NameField.getAccessibleContext().setAccessibleParent(ContactPanel);

        contact1EmailField.setName("contact1EmailField"); // NOI18N
        contact1EmailField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact1Email}"), contact1EmailField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact1EmailField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact1EmailField, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 212, 230, -1));

        contact2EmailField.setName("contact2EmailField"); // NOI18N
        contact2EmailField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact2Email}"), contact2EmailField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact2EmailField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact2EmailField, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 230, 230, -1));

        contact3EmailField.setName("contact3EmailField"); // NOI18N
        contact3EmailField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact3Email}"), contact3EmailField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact3EmailField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact3EmailField, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 248, 230, -1));

        contact4EmailField.setName("contact4EmailField"); // NOI18N
        contact4EmailField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact4Email}"), contact4EmailField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact4EmailField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact4EmailField, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 266, 230, -1));

        contact5EmailField.setName("contact5EmailField"); // NOI18N
        contact5EmailField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact5Email}"), contact5EmailField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact5EmailField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact5EmailField, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 284, 230, -1));

        contact6EmailField.setName("contact6EmailField"); // NOI18N
        contact6EmailField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact6Email}"), contact6EmailField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact6EmailField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact6EmailField, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 302, 230, -1));

        contact1PhoneField.setName("contact1PhoneField"); // NOI18N
        contact1PhoneField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact1Phone}"), contact1PhoneField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact1PhoneField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact1PhoneField, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 212, 230, -1));

        contact2PhoneField.setName("contact2PhoneField"); // NOI18N
        contact2PhoneField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact2Phone}"), contact2PhoneField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact2PhoneField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact2PhoneField, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 230, 230, -1));

        contact3PhoneField.setName("contact3PhoneField"); // NOI18N
        contact3PhoneField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact3Phone}"), contact3PhoneField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact3PhoneField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact3PhoneField, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 248, 230, -1));

        contact4PhoneField.setName("contact4PhoneField"); // NOI18N
        contact4PhoneField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact4Phone}"), contact4PhoneField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact4PhoneField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact4PhoneField, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 266, 230, -1));

        contact5PhoneField.setName("contact5PhoneField"); // NOI18N
        contact5PhoneField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact5Phone}"), contact5PhoneField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact5PhoneField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact5PhoneField, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 284, 230, -1));

        contact6PhoneField.setName("contact6PhoneField"); // NOI18N
        contact6PhoneField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact6Phone}"), contact6PhoneField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact6PhoneField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact6PhoneField, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 302, 230, -1));

        contact1FunctionField.setName("contact1FunctionField"); // NOI18N
        contact1FunctionField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact1Function}"), contact1FunctionField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact1FunctionField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact1FunctionField, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 212, 230, -1));

        contact2FunctionField.setName("contact2FunctionField"); // NOI18N
        contact2FunctionField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact2Function}"), contact2FunctionField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact2FunctionField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact2FunctionField, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 230, 230, -1));

        contact3FunctionField.setName("contact3FunctionField"); // NOI18N
        contact3FunctionField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact3Function}"), contact3FunctionField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact3FunctionField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact3FunctionField, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 248, 230, -1));

        contact4FunctionField.setName("contact4FunctionField"); // NOI18N
        contact4FunctionField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact4Function}"), contact4FunctionField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact4FunctionField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact4FunctionField, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 266, 230, -1));

        contact5FunctionField.setName("contact5FunctionField"); // NOI18N
        contact5FunctionField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact5Function}"), contact5FunctionField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact5FunctionField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact5FunctionField, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 284, 230, -1));

        contact6FunctionField.setName("contact6FunctionField"); // NOI18N
        contact6FunctionField.setPreferredSize(new java.awt.Dimension(6, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.contact6Function}"), contact6FunctionField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), contact6FunctionField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        ContactPanel.add(contact6FunctionField, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 302, 230, -1));

        mainPanel.add(ContactPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(155, 40, 1275, 325));

        BsciPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(-16777216,true)));
        BsciPanel.setName("BsciPanel"); // NOI18N
        BsciPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        bsciLabel.setFont(resourceMap.getFont("bsciLabel.font")); // NOI18N
        bsciLabel.setText(resourceMap.getString("bsciLabel.text")); // NOI18N
        bsciLabel.setName("bsciLabel"); // NOI18N
        BsciPanel.add(bsciLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 1, -1, -1));

        bsciPartLabel.setFont(resourceMap.getFont("bsciPartLabel.font")); // NOI18N
        bsciPartLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        bsciPartLabel.setText(resourceMap.getString("bsciPartLabel.text")); // NOI18N
        bsciPartLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        bsciPartLabel.setName("bsciPartLabel"); // NOI18N
        bsciPartLabel.setPreferredSize(new java.awt.Dimension(80, 20));
        BsciPanel.add(bsciPartLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 0, 110, 20));
        bsciPartLabel.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciPartComboBox.setMaximumRowCount(3);
        bsciPartComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", "Required", "Not Required" }));
        bsciPartComboBox.setName("bsciPartComboBox"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ObjectProperty.create(), bsciPartComboBox, org.jdesktop.beansbinding.BeanProperty.create("elements"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.bsciPart}"), bsciPartComboBox, org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), bsciPartComboBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding.setSourceUnreadableValue(null);
        BsciPanel.add(bsciPartComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, 110, 20));
        bsciPartComboBox.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciResultLabel.setFont(resourceMap.getFont("bsciPartLabel.font")); // NOI18N
        bsciResultLabel.setText(resourceMap.getString("bsciResultLabel.text")); // NOI18N
        bsciResultLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        bsciResultLabel.setName("bsciResultLabel"); // NOI18N
        bsciResultLabel.setPreferredSize(new java.awt.Dimension(80, 20));
        BsciPanel.add(bsciResultLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 0, 140, 20));
        bsciResultLabel.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciResultComboBox.setMaximumRowCount(11);
        bsciResultComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", "Good", "Improvements Needed", "Non Compliant", "----------------------------", "A-Outstanding", "B-Good", "C-Acceptable", "D-Insufficient", "E-Unacceptable", "Zero Compliance" }));
        bsciResultComboBox.setName("bsciResultComboBox"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ObjectProperty.create(), bsciResultComboBox, org.jdesktop.beansbinding.BeanProperty.create("elements"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.bsciResult}"), bsciResultComboBox, org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), bsciResultComboBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding.setSourceUnreadableValue(null);
        BsciPanel.add(bsciResultComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 20, 140, 20));
        bsciResultComboBox.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciFromLabel.setFont(resourceMap.getFont("bsciFromLabel.font")); // NOI18N
        bsciFromLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        bsciFromLabel.setText(resourceMap.getString("bsciFromLabel.text")); // NOI18N
        bsciFromLabel.setName("bsciFromLabel"); // NOI18N
        bsciFromLabel.setPreferredSize(new java.awt.Dimension(80, 20));
        BsciPanel.add(bsciFromLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 40, 110, -1));
        bsciFromLabel.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciFromDateChooser.setDateFormatString(resourceMap.getString("bsciFromDateChooser.dateFormatString")); // NOI18N
        bsciFromDateChooser.setName("bsciFromDateChooser"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.bsciFrom}"), bsciFromDateChooser, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), bsciFromDateChooser, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        BsciPanel.add(bsciFromDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 60, 100, -1));
        bsciFromDateChooser.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciFromButton.setBackground(resourceMap.getColor("bsciFromButton.background")); // NOI18N
        bsciFromButton.setForeground(resourceMap.getColor("bsciFromButton.foreground")); // NOI18N
        bsciFromButton.setText(resourceMap.getString("bsciFromButton.text")); // NOI18N
        bsciFromButton.setName("bsciFromButton"); // NOI18N
        bsciFromButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bsciFromButtonActionPerformed(evt);
            }
        });
        BsciPanel.add(bsciFromButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 60, 5, 5));

        bsciTillLabel.setFont(resourceMap.getFont("bsciTillLabel.font")); // NOI18N
        bsciTillLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        bsciTillLabel.setText(resourceMap.getString("bsciTillLabel.text")); // NOI18N
        bsciTillLabel.setName("bsciTillLabel"); // NOI18N
        bsciTillLabel.setPreferredSize(new java.awt.Dimension(80, 20));
        BsciPanel.add(bsciTillLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 40, 80, -1));
        bsciTillLabel.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciTillDateChooser.setDateFormatString(resourceMap.getString("bsciTillDateChooser.dateFormatString")); // NOI18N
        bsciTillDateChooser.setName("bsciTillDateChooser"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.bsciTill}"), bsciTillDateChooser, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), bsciTillDateChooser, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        BsciPanel.add(bsciTillDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 60, 100, -1));
        bsciTillDateChooser.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciTillButton.setBackground(resourceMap.getColor("bsciTillButton.background")); // NOI18N
        bsciTillButton.setForeground(resourceMap.getColor("bsciTillButton.foreground")); // NOI18N
        bsciTillButton.setName("bsciTillButton"); // NOI18N
        bsciTillButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bsciTillButtonActionPerformed(evt);
            }
        });
        BsciPanel.add(bsciTillButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 60, 5, 5));

        bsciOtherLabel.setFont(resourceMap.getFont("bsciOtherLabel.font")); // NOI18N
        bsciOtherLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        bsciOtherLabel.setText(resourceMap.getString("bsciOtherLabel.text")); // NOI18N
        bsciOtherLabel.setName("bsciOtherLabel"); // NOI18N
        bsciOtherLabel.setPreferredSize(new java.awt.Dimension(80, 20));
        BsciPanel.add(bsciOtherLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 0, 420, -1));
        bsciOtherLabel.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciOther1NameLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        bsciOther1NameLabel.setText(resourceMap.getString("bsciOther1NameLabel.text")); // NOI18N
        bsciOther1NameLabel.setName("bsciOther1NameLabel"); // NOI18N
        bsciOther1NameLabel.setPreferredSize(new java.awt.Dimension(80, 20));
        BsciPanel.add(bsciOther1NameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 20, 20, -1));

        bsciOther2NameLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        bsciOther2NameLabel.setText(resourceMap.getString("bsciOther2NameLabel.text")); // NOI18N
        bsciOther2NameLabel.setName("bsciOther2NameLabel"); // NOI18N
        bsciOther2NameLabel.setPreferredSize(new java.awt.Dimension(80, 20));
        BsciPanel.add(bsciOther2NameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 40, 20, -1));
        bsciOther2NameLabel.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciOther3NameLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        bsciOther3NameLabel.setText(resourceMap.getString("bsciOther3NameLabel.text")); // NOI18N
        bsciOther3NameLabel.setName("bsciOther3NameLabel"); // NOI18N
        bsciOther3NameLabel.setPreferredSize(new java.awt.Dimension(80, 20));
        BsciPanel.add(bsciOther3NameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 60, 20, -1));
        bsciOther3NameLabel.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciOther1NameField.setName("bsciOther1NameField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.bsciOther1Name}"), bsciOther1NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceUnreadableValue(null);
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), bsciOther1NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        BsciPanel.add(bsciOther1NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 20, 420, -1));
        bsciOther1NameField.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciOther2NameField.setName("bsciOther2NameField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.bsciOther2Name}"), bsciOther2NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceUnreadableValue(null);
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), bsciOther2NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        BsciPanel.add(bsciOther2NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 40, 420, -1));
        bsciOther2NameField.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciOther3NameField.setName("bsciOther3NameField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.bsciOther3Name}"), bsciOther3NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), bsciOther3NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        BsciPanel.add(bsciOther3NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 60, 420, -1));
        bsciOther3NameField.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciOther1DateFrom.setDateFormatString(resourceMap.getString("bsciOther1DateFrom.dateFormatString")); // NOI18N
        bsciOther1DateFrom.setName("bsciOther1DateFrom"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.bsciOther1From}"), bsciOther1DateFrom, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), bsciOther1DateFrom, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding.setSourceUnreadableValue(null);
        BsciPanel.add(bsciOther1DateFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 20, 100, -1));
        bsciOther1DateFrom.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciOther1FromButton.setBackground(resourceMap.getColor("bsciOther1FromButton.background")); // NOI18N
        bsciOther1FromButton.setForeground(resourceMap.getColor("bsciOther1FromButton.foreground")); // NOI18N
        bsciOther1FromButton.setName("bsciOther1FromButton"); // NOI18N
        bsciOther1FromButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bsciOther1FromButtonActionPerformed(evt);
            }
        });
        BsciPanel.add(bsciOther1FromButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(895, 20, 5, 5));

        bsciOther2DateFrom.setDateFormatString(resourceMap.getString("bsciOther2DateFrom.dateFormatString")); // NOI18N
        bsciOther2DateFrom.setName("bsciOther2DateFrom"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.bsciOther2From}"), bsciOther2DateFrom, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), bsciOther2DateFrom, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding.setSourceUnreadableValue(null);
        BsciPanel.add(bsciOther2DateFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 40, 100, -1));
        bsciOther2DateFrom.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciOther2FromButton.setBackground(resourceMap.getColor("bsciOther2FromButton.background")); // NOI18N
        bsciOther2FromButton.setForeground(resourceMap.getColor("bsciOther2FromButton.foreground")); // NOI18N
        bsciOther2FromButton.setName("bsciOther2FromButton"); // NOI18N
        bsciOther2FromButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bsciOther2FromButtonActionPerformed(evt);
            }
        });
        BsciPanel.add(bsciOther2FromButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(895, 40, 5, 5));

        bsciOther3DateFrom.setDateFormatString(resourceMap.getString("bsciOther3DateFrom.dateFormatString")); // NOI18N
        bsciOther3DateFrom.setName("bsciOther3DateFrom"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.bsciOther3From}"), bsciOther3DateFrom, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), bsciOther3DateFrom, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding.setSourceUnreadableValue(null);
        BsciPanel.add(bsciOther3DateFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 60, 100, -1));
        bsciOther3DateFrom.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciOther3FromButton.setBackground(resourceMap.getColor("bsciOther3FromButton.background")); // NOI18N
        bsciOther3FromButton.setForeground(resourceMap.getColor("bsciOther3FromButton.foreground")); // NOI18N
        bsciOther3FromButton.setName("bsciOther3FromButton"); // NOI18N
        bsciOther3FromButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bsciOther3FromButtonActionPerformed(evt);
            }
        });
        BsciPanel.add(bsciOther3FromButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(895, 60, 5, 5));

        bsciOther1DateTill.setDateFormatString(resourceMap.getString("bsciOther1DateTill.dateFormatString")); // NOI18N
        bsciOther1DateTill.setName("bsciOther1DateTill"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.bsciOther1Till}"), bsciOther1DateTill, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), bsciOther1DateTill, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding.setSourceUnreadableValue(null);
        BsciPanel.add(bsciOther1DateTill, new org.netbeans.lib.awtextra.AbsoluteConstraints(905, 20, 100, -1));
        bsciOther1DateTill.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciOther1TillButton.setBackground(resourceMap.getColor("bsciOther1TillButton.background")); // NOI18N
        bsciOther1TillButton.setForeground(resourceMap.getColor("bsciOther1TillButton.foreground")); // NOI18N
        bsciOther1TillButton.setName("bsciOther1TillButton"); // NOI18N
        bsciOther1TillButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bsciOther1TillButtonActionPerformed(evt);
            }
        });
        BsciPanel.add(bsciOther1TillButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1005, 20, 5, 5));

        bsciOther2DateTill.setDateFormatString(resourceMap.getString("bsciOther2DateTill.dateFormatString")); // NOI18N
        bsciOther2DateTill.setName("bsciOther2DateTill"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.bsciOther2Till}"), bsciOther2DateTill, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), bsciOther2DateTill, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding.setSourceUnreadableValue(null);
        BsciPanel.add(bsciOther2DateTill, new org.netbeans.lib.awtextra.AbsoluteConstraints(905, 40, 100, -1));
        bsciOther2DateTill.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciOther2TillButton.setBackground(resourceMap.getColor("bsciOther2TillButton.background")); // NOI18N
        bsciOther2TillButton.setForeground(resourceMap.getColor("bsciOther2TillButton.foreground")); // NOI18N
        bsciOther2TillButton.setName("bsciOther2TillButton"); // NOI18N
        bsciOther2TillButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bsciOther2TillButtonActionPerformed(evt);
            }
        });
        BsciPanel.add(bsciOther2TillButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1005, 40, 5, 5));

        bsciOther3DateTill.setDateFormatString(resourceMap.getString("bsciOther3DateTill.dateFormatString")); // NOI18N
        bsciOther3DateTill.setName("bsciOther3DateTill"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.bsciOther3Till}"), bsciOther3DateTill, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), bsciOther3DateTill, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding.setSourceUnreadableValue(null);
        BsciPanel.add(bsciOther3DateTill, new org.netbeans.lib.awtextra.AbsoluteConstraints(905, 60, 100, -1));
        bsciOther3DateTill.getAccessibleContext().setAccessibleParent(BsciPanel);

        bsciOther3TillButton.setBackground(resourceMap.getColor("bsciOther3TillButton.background")); // NOI18N
        bsciOther3TillButton.setForeground(resourceMap.getColor("bsciOther3TillButton.foreground")); // NOI18N
        bsciOther3TillButton.setName("bsciOther3TillButton"); // NOI18N
        bsciOther3TillButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bsciOther3TillButtonActionPerformed(evt);
            }
        });
        BsciPanel.add(bsciOther3TillButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1005, 60, 5, 5));

        bsciOtherFromLabel.setFont(resourceMap.getFont("bsciOtherLabel.font")); // NOI18N
        bsciOtherFromLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        bsciOtherFromLabel.setText(resourceMap.getString("bsciOtherFromLabel.text")); // NOI18N
        bsciOtherFromLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        bsciOtherFromLabel.setName("bsciOtherFromLabel"); // NOI18N
        bsciOtherFromLabel.setPreferredSize(new java.awt.Dimension(80, 20));
        BsciPanel.add(bsciOtherFromLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(795, 0, 80, 20));

        bsciOtherTillLabel.setFont(resourceMap.getFont("bsciOtherTillLabel.font")); // NOI18N
        bsciOtherTillLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        bsciOtherTillLabel.setText(resourceMap.getString("bsciOtherTillLabel.text")); // NOI18N
        bsciOtherTillLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        bsciOtherTillLabel.setName("bsciOtherTillLabel"); // NOI18N
        bsciOtherTillLabel.setPreferredSize(new java.awt.Dimension(80, 20));
        BsciPanel.add(bsciOtherTillLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(905, 0, 80, 20));

        mainPanel.add(BsciPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(155, 365, 1025, 85));

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(-16777216,true)));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        FOBCheckBox.setText(resourceMap.getString("FOBCheckBox.text")); // NOI18N
        FOBCheckBox.setName("FOBCheckBox"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.FOB}"), FOBCheckBox, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        jPanel1.add(FOBCheckBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, -1, -1));

        buyerLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        buyerLabel.setText(resourceMap.getString("buyerLabel.text")); // NOI18N
        buyerLabel.setName("buyerLabel"); // NOI18N
        buyerLabel.setPreferredSize(new java.awt.Dimension(80, 20));
        jPanel1.add(buyerLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 50, 35, -1));
        buyerLabel.getAccessibleContext().setAccessibleParent(ContactPanel);

        buyerComboBox.setMaximumRowCount(4);
        buyerComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", "Dennis", "Mark", "Dennis / Mark" }));
        buyerComboBox.setName("buyerComboBox"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ObjectProperty.create(), buyerComboBox, org.jdesktop.beansbinding.BeanProperty.create("elements"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.buyer}"), buyerComboBox, org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), buyerComboBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding.setSourceUnreadableValue(null);
        jPanel1.add(buyerComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 50, 110, -1));
        buyerComboBox.getAccessibleContext().setAccessibleParent(ContactPanel);

        qmLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        qmLabel.setText(resourceMap.getString("qmLabel.text")); // NOI18N
        qmLabel.setEnabled(false);
        qmLabel.setName("qmLabel"); // NOI18N
        jPanel1.add(qmLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 50, -1, 20));

        qmComboBox.setMaximumRowCount(4);
        qmComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", "Ad", "Jason", "Ruud" }));
        qmComboBox.setName("qmComboBox"); // NOI18N
        jPanel1.add(qmComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(185, 50, 60, -1));

        mainPanel.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1180, 365, 250, 85));

        CePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(-16777216,true)));
        CePanel.setName("CePanel"); // NOI18N
        CePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        certLabel.setFont(resourceMap.getFont("certLabel.font")); // NOI18N
        certLabel.setText(resourceMap.getString("certLabel.text")); // NOI18N
        certLabel.setName("certLabel"); // NOI18N
        CePanel.add(certLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(3, 0, -1, -1));

        certIso9000TextField.setName("certIso9000TextField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certIso9000}"), certIso9000TextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), certIso9000TextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(certIso9000TextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 70, -1));

        CertIso9001DateFrom.setDateFormatString(resourceMap.getString("CertIso9001DateFrom.dateFormatString")); // NOI18N
        CertIso9001DateFrom.setName("CertIso9001DateFrom"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certIso9000From}"), CertIso9001DateFrom, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertIso9001DateFrom, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding.setSourceUnreadableValue(null);
        CePanel.add(CertIso9001DateFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 20, 85, -1));
        CertIso9001DateFrom.getAccessibleContext().setAccessibleParent(CePanel);

        CertIso140000DateFrom.setDateFormatString(resourceMap.getString("CertIso140000DateFrom.dateFormatString")); // NOI18N
        CertIso140000DateFrom.setName("CertIso140000DateFrom"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certIso14000From}"), CertIso140000DateFrom, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertIso140000DateFrom, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertIso140000DateFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 40, 85, -1));
        CertIso140000DateFrom.getAccessibleContext().setAccessibleParent(CePanel);

        certIso14000TextField.setName("certIso14000TextField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certIso14000}"), certIso14000TextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), certIso14000TextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(certIso14000TextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 70, -1));

        CertOther1DateFrom.setDateFormatString(resourceMap.getString("CertOther1DateFrom.dateFormatString")); // NOI18N
        CertOther1DateFrom.setName("CertOther1DateFrom"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther1From}"), CertOther1DateFrom, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther1DateFrom, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther1DateFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 60, 85, -1));
        CertOther1DateFrom.getAccessibleContext().setAccessibleParent(CePanel);

        CertOther2DateFrom.setDateFormatString(resourceMap.getString("CertOther2DateFrom.dateFormatString")); // NOI18N
        CertOther2DateFrom.setName("CertOther2DateFrom"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther2From}"), CertOther2DateFrom, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther2DateFrom, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther2DateFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 80, 85, -1));
        CertOther2DateFrom.getAccessibleContext().setAccessibleParent(CePanel);

        CertOther3DateFrom.setDateFormatString(resourceMap.getString("CertOther3DateFrom.dateFormatString")); // NOI18N
        CertOther3DateFrom.setName("CertOther3DateFrom"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther3From}"), CertOther3DateFrom, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther3DateFrom, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther3DateFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 20, 85, -1));
        CertOther3DateFrom.getAccessibleContext().setAccessibleParent(CePanel);

        CertFromLabel.setFont(resourceMap.getFont("CertFromLabel.font")); // NOI18N
        CertFromLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        CertFromLabel.setText(resourceMap.getString("CertFromLabel.text")); // NOI18N
        CertFromLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        CertFromLabel.setName("CertFromLabel"); // NOI18N
        CertFromLabel.setPreferredSize(new java.awt.Dimension(70, 20));
        CePanel.add(CertFromLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 0, 60, -1));
        CertFromLabel.getAccessibleContext().setAccessibleParent(CePanel);

        CertTillLabel.setFont(resourceMap.getFont("CertFromLabel.font")); // NOI18N
        CertTillLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        CertTillLabel.setText(resourceMap.getString("CertTillLabel.text")); // NOI18N
        CertTillLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        CertTillLabel.setName("CertTillLabel"); // NOI18N
        CertTillLabel.setPreferredSize(new java.awt.Dimension(70, 20));
        CePanel.add(CertTillLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 0, 60, -1));
        CertTillLabel.getAccessibleContext().setAccessibleParent(CePanel);

        CertIso9001DateTill.setDateFormatString(resourceMap.getString("CertIso9001DateTill.dateFormatString")); // NOI18N
        CertIso9001DateTill.setName("CertIso9001DateTill"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certIso9000Till}"), CertIso9001DateTill, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertIso9001DateTill, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding.setSourceUnreadableValue(null);
        CePanel.add(CertIso9001DateTill, new org.netbeans.lib.awtextra.AbsoluteConstraints(175, 20, 85, -1));
        CertIso9001DateTill.getAccessibleContext().setAccessibleParent(CePanel);

        CertIso140000DateTill.setDateFormatString(resourceMap.getString("CertIso140000DateTill.dateFormatString")); // NOI18N
        CertIso140000DateTill.setName("CertIso140000DateTill"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certIso14000Till}"), CertIso140000DateTill, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertIso140000DateTill, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertIso140000DateTill, new org.netbeans.lib.awtextra.AbsoluteConstraints(175, 40, 85, -1));
        CertIso140000DateTill.getAccessibleContext().setAccessibleParent(CePanel);

        CertOther1DateTill.setDateFormatString(resourceMap.getString("CertOther1DateTill.dateFormatString")); // NOI18N
        CertOther1DateTill.setName("CertOther1DateTill"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther1Till}"), CertOther1DateTill, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther1DateTill, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther1DateTill, new org.netbeans.lib.awtextra.AbsoluteConstraints(175, 60, 85, -1));
        CertOther1DateTill.getAccessibleContext().setAccessibleParent(CePanel);

        CertOther2DateTill.setDateFormatString(resourceMap.getString("CertOther2DateTill.dateFormatString")); // NOI18N
        CertOther2DateTill.setName("CertOther2DateTill"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther2Till}"), CertOther2DateTill, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther2DateTill, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther2DateTill, new org.netbeans.lib.awtextra.AbsoluteConstraints(175, 80, 85, -1));
        CertOther2DateTill.getAccessibleContext().setAccessibleParent(CePanel);

        CertOther3DateTill.setDateFormatString(resourceMap.getString("CertOther3DateTill.dateFormatString")); // NOI18N
        CertOther3DateTill.setName("CertOther3DateTill"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther3Till}"), CertOther3DateTill, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther3DateTill, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther3DateTill, new org.netbeans.lib.awtextra.AbsoluteConstraints(685, 20, 85, -1));
        CertOther3DateTill.getAccessibleContext().setAccessibleParent(CePanel);

        CertNameLabel.setFont(resourceMap.getFont("CertFromLabel.font")); // NOI18N
        CertNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        CertNameLabel.setText(resourceMap.getString("CertNameLabel.text")); // NOI18N
        CertNameLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        CertNameLabel.setName("CertNameLabel"); // NOI18N
        CertNameLabel.setPreferredSize(new java.awt.Dimension(80, 20));
        CePanel.add(CertNameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 0, 200, -1));
        CertNameLabel.getAccessibleContext().setAccessibleParent(CePanel);

        certIso9000NameField.setName("certIso9000NameField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certIso9000Name}"), certIso9000NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceUnreadableValue(null);
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), certIso9000NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(certIso9000NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 20, 200, -1));
        certIso9000NameField.getAccessibleContext().setAccessibleParent(CePanel);

        certIso14000NameField.setName("certIso14000NameField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certIso14000Name}"), certIso14000NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceUnreadableValue(null);
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), certIso14000NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(certIso14000NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 40, 200, -1));
        certIso14000NameField.getAccessibleContext().setAccessibleParent(CePanel);

        certOther1NameField.setName("certOther1NameField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther1Name}"), certOther1NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceUnreadableValue(null);
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), certOther1NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(certOther1NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 60, 200, -1));
        certOther1NameField.getAccessibleContext().setAccessibleParent(CePanel);

        certOther2NameField.setName("certOther2NameField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther2Name}"), certOther2NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceUnreadableValue(null);
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), certOther2NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(certOther2NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 80, 200, -1));
        certOther2NameField.getAccessibleContext().setAccessibleParent(CePanel);

        certOther3NameField.setName("certOther3NameField"); // NOI18N
        certOther3NameField.setVerifyInputWhenFocusTarget(false);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther3Name}"), certOther3NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceUnreadableValue(null);
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), certOther3NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(certOther3NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 20, 100, -1));
        certOther3NameField.getAccessibleContext().setAccessibleParent(CePanel);

        CertOther1TextField.setName("CertOther1TextField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther1}"), CertOther1TextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther1TextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther1TextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 70, -1));

        CertOther2TextField.setName("CertOther2TextField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther2}"), CertOther2TextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther2TextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther2TextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 70, -1));

        CertOther6NameField.setName("CertOther6NameField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther6Name}"), CertOther6NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther6NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther6NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 80, 100, 20));

        CertIso9001FromButton.setBackground(resourceMap.getColor("CertIso9001FromButton.background")); // NOI18N
        CertIso9001FromButton.setForeground(resourceMap.getColor("CertIso9001FromButton.foreground")); // NOI18N
        CertIso9001FromButton.setName("CertIso9001FromButton"); // NOI18N
        CertIso9001FromButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertIso9001FromButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertIso9001FromButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(165, 20, 5, 5));

        CertIso140000FromButton.setBackground(resourceMap.getColor("CertIso140000FromButton.background")); // NOI18N
        CertIso140000FromButton.setForeground(resourceMap.getColor("CertIso140000FromButton.foreground")); // NOI18N
        CertIso140000FromButton.setName("CertIso140000FromButton"); // NOI18N
        CertIso140000FromButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertIso140000FromButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertIso140000FromButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(165, 40, 5, 5));

        CertOther1FromButton.setBackground(resourceMap.getColor("CertOther1FromButton.background")); // NOI18N
        CertOther1FromButton.setForeground(resourceMap.getColor("CertOther1FromButton.foreground")); // NOI18N
        CertOther1FromButton.setName("CertOther1FromButton"); // NOI18N
        CertOther1FromButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther1FromButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertOther1FromButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(165, 60, 5, 5));

        CertOther2FromButton.setBackground(resourceMap.getColor("CertOther2FromButton.background")); // NOI18N
        CertOther2FromButton.setForeground(resourceMap.getColor("CertOther2FromButton.foreground")); // NOI18N
        CertOther2FromButton.setName("CertOther2FromButton"); // NOI18N
        CertOther2FromButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther2FromButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertOther2FromButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(165, 80, 5, 5));

        CertOther3FromButton.setBackground(resourceMap.getColor("CertOther3FromButton.background")); // NOI18N
        CertOther3FromButton.setForeground(resourceMap.getColor("CertOther3FromButton.foreground")); // NOI18N
        CertOther3FromButton.setName("CertOther3FromButton"); // NOI18N
        CertOther3FromButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther3FromButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertOther3FromButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(675, 20, 5, 5));

        CertIso9001TillButton.setBackground(resourceMap.getColor("CertIso9001TillButton.background")); // NOI18N
        CertIso9001TillButton.setForeground(resourceMap.getColor("CertIso9001TillButton.foreground")); // NOI18N
        CertIso9001TillButton.setName("CertIso9001TillButton"); // NOI18N
        CertIso9001TillButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertIso9001TillButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertIso9001TillButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 20, 5, 5));

        CertIso140000TillButton.setBackground(resourceMap.getColor("CertIso140000TillButton.background")); // NOI18N
        CertIso140000TillButton.setForeground(resourceMap.getColor("CertIso140000TillButton.foreground")); // NOI18N
        CertIso140000TillButton.setName("CertIso140000TillButton"); // NOI18N
        CertIso140000TillButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertIso140000TillButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertIso140000TillButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 40, 5, 5));

        CertOther1TillButton.setBackground(resourceMap.getColor("CertOther1TillButton.background")); // NOI18N
        CertOther1TillButton.setForeground(resourceMap.getColor("CertOther1TillButton.foreground")); // NOI18N
        CertOther1TillButton.setName("CertOther1TillButton"); // NOI18N
        CertOther1TillButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther1TillButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertOther1TillButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 60, 5, 5));

        CertOther2TillButton.setBackground(resourceMap.getColor("CertOther2TillButton.background")); // NOI18N
        CertOther2TillButton.setForeground(resourceMap.getColor("CertOther2TillButton.foreground")); // NOI18N
        CertOther2TillButton.setName("CertOther2TillButton"); // NOI18N
        CertOther2TillButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther2TillButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertOther2TillButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 80, 5, 5));

        CertOther3TillButton.setBackground(resourceMap.getColor("CertOther3TillButton.background")); // NOI18N
        CertOther3TillButton.setForeground(resourceMap.getColor("CertOther3TillButton.foreground")); // NOI18N
        CertOther3TillButton.setName("CertOther3TillButton"); // NOI18N
        CertOther3TillButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther3TillButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertOther3TillButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 20, 5, 5));

        CertFromLabel1.setFont(resourceMap.getFont("CertFromLabel.font")); // NOI18N
        CertFromLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        CertFromLabel1.setText(resourceMap.getString("CertFromLabel1.text")); // NOI18N
        CertFromLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        CertFromLabel1.setName("CertFromLabel1"); // NOI18N
        CertFromLabel1.setPreferredSize(new java.awt.Dimension(70, 20));
        CePanel.add(CertFromLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 0, 65, -1));

        CertTillLabel1.setFont(resourceMap.getFont("CertFromLabel.font")); // NOI18N
        CertTillLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        CertTillLabel1.setText(resourceMap.getString("CertTillLabel1.text")); // NOI18N
        CertTillLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        CertTillLabel1.setName("CertTillLabel1"); // NOI18N
        CertTillLabel1.setPreferredSize(new java.awt.Dimension(70, 20));
        CePanel.add(CertTillLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 0, 65, -1));

        CertNameLabel1.setFont(resourceMap.getFont("CertNameLabel1.font")); // NOI18N
        CertNameLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        CertNameLabel1.setText(resourceMap.getString("CertNameLabel1.text")); // NOI18N
        CertNameLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        CertNameLabel1.setName("CertNameLabel1"); // NOI18N
        CertNameLabel1.setPreferredSize(new java.awt.Dimension(80, 20));
        CePanel.add(CertNameLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 0, 100, -1));

        CertOther7NameField.setName("CertOther7NameField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther7Name}"), CertOther7NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther7NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther7NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(1170, 20, 100, 20));

        CertOther4DateFrom.setDateFormatString(resourceMap.getString("CertOther4DateFrom.dateFormatString")); // NOI18N
        CertOther4DateFrom.setName("CertOther4DateFrom"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther4From}"), CertOther4DateFrom, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther4DateFrom, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther4DateFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 40, 85, -1));

        CertOther4FromButton.setBackground(resourceMap.getColor("CertOther4FromButton.background")); // NOI18N
        CertOther4FromButton.setForeground(resourceMap.getColor("CertOther4FromButton.foreground")); // NOI18N
        CertOther4FromButton.setName("CertOther4FromButton"); // NOI18N
        CertOther4FromButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther4FromButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertOther4FromButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(675, 40, 5, 5));

        CertOther4DateTill.setDateFormatString(resourceMap.getString("CertOther4DateTill.dateFormatString")); // NOI18N
        CertOther4DateTill.setName("CertOther4DateTill"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther4Till}"), CertOther4DateTill, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther4DateTill, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther4DateTill, new org.netbeans.lib.awtextra.AbsoluteConstraints(685, 40, 85, -1));

        CertOther4TillButton.setBackground(resourceMap.getColor("CertOther4TillButton.background")); // NOI18N
        CertOther4TillButton.setForeground(resourceMap.getColor("CertOther4TillButton.foreground")); // NOI18N
        CertOther4TillButton.setName("CertOther4TillButton"); // NOI18N
        CertOther4TillButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther4TillButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertOther4TillButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 40, 5, 5));

        certOther4NameField.setName("certOther4NameField"); // NOI18N
        certOther4NameField.setVerifyInputWhenFocusTarget(false);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther4Name}"), certOther4NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), certOther4NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(certOther4NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 40, 100, -1));

        CertOther8NameField.setName("CertOther8NameField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther8Name}"), CertOther8NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther8NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther8NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(1170, 40, 100, 20));

        CertOther5DateFrom.setDateFormatString(resourceMap.getString("CertOther5DateFrom.dateFormatString")); // NOI18N
        CertOther5DateFrom.setName("CertOther5DateFrom"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther5From}"), CertOther5DateFrom, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther5DateFrom, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther5DateFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 60, 85, -1));

        CertOther5FromButton.setBackground(resourceMap.getColor("CertOther5FromButton.background")); // NOI18N
        CertOther5FromButton.setForeground(resourceMap.getColor("CertOther5FromButton.foreground")); // NOI18N
        CertOther5FromButton.setName("CertOther5FromButton"); // NOI18N
        CertOther5FromButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther5FromButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertOther5FromButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(675, 60, 5, 5));

        CertOther5DateTill.setDateFormatString(resourceMap.getString("CertOther5DateTill.dateFormatString")); // NOI18N
        CertOther5DateTill.setName("CertOther5DateTill"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther5Till}"), CertOther5DateTill, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther5DateTill, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther5DateTill, new org.netbeans.lib.awtextra.AbsoluteConstraints(685, 60, 85, -1));

        CertOther5TillButton.setBackground(resourceMap.getColor("CertOther5TillButton.background")); // NOI18N
        CertOther5TillButton.setForeground(resourceMap.getColor("CertOther5TillButton.foreground")); // NOI18N
        CertOther5TillButton.setName("CertOther5TillButton"); // NOI18N
        CertOther5TillButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther5TillButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertOther5TillButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 60, 5, 5));

        certOther5NameField.setName("certOther5NameField"); // NOI18N
        certOther5NameField.setVerifyInputWhenFocusTarget(false);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther5Name}"), certOther5NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), certOther5NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(certOther5NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 60, 100, -1));

        CertOther6DateFrom.setDateFormatString(resourceMap.getString("CertOther6DateFrom.dateFormatString")); // NOI18N
        CertOther6DateFrom.setName("CertOther6DateFrom"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther6From}"), CertOther6DateFrom, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther6DateFrom, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther6DateFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 80, 85, -1));

        CertOther6FromButton.setBackground(resourceMap.getColor("CertOther6FromButton.background")); // NOI18N
        CertOther6FromButton.setForeground(resourceMap.getColor("CertOther6FromButton.foreground")); // NOI18N
        CertOther6FromButton.setName("CertOther6FromButton"); // NOI18N
        CertOther6FromButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther6FromButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertOther6FromButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(675, 80, 5, 5));

        CertOther6DateTill.setDateFormatString(resourceMap.getString("CertOther6DateTill.dateFormatString")); // NOI18N
        CertOther6DateTill.setName("CertOther6DateTill"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther6Till}"), CertOther6DateTill, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther6DateTill, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther6DateTill, new org.netbeans.lib.awtextra.AbsoluteConstraints(685, 80, 85, -1));

        CertOther6TillButton.setBackground(resourceMap.getColor("CertOther6TillButton.background")); // NOI18N
        CertOther6TillButton.setForeground(resourceMap.getColor("CertOther6TillButton.foreground")); // NOI18N
        CertOther6TillButton.setName("CertOther6TillButton"); // NOI18N
        CertOther6TillButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther6TillButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertOther6TillButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 80, 5, 5));

        CertFromLabel2.setFont(resourceMap.getFont("CertFromLabel2.font")); // NOI18N
        CertFromLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        CertFromLabel2.setText(resourceMap.getString("CertFromLabel2.text")); // NOI18N
        CertFromLabel2.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        CertFromLabel2.setName("CertFromLabel2"); // NOI18N
        CertFromLabel2.setPreferredSize(new java.awt.Dimension(70, 20));
        CePanel.add(CertFromLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 0, 65, -1));

        CertTillLabel2.setFont(resourceMap.getFont("CertFromLabel2.font")); // NOI18N
        CertTillLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        CertTillLabel2.setText(resourceMap.getString("CertTillLabel2.text")); // NOI18N
        CertTillLabel2.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        CertTillLabel2.setName("CertTillLabel2"); // NOI18N
        CertTillLabel2.setPreferredSize(new java.awt.Dimension(70, 20));
        CePanel.add(CertTillLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1075, 0, 65, -1));

        CertNameLabel2.setFont(resourceMap.getFont("CertFromLabel2.font")); // NOI18N
        CertNameLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        CertNameLabel2.setText(resourceMap.getString("CertNameLabel2.text")); // NOI18N
        CertNameLabel2.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        CertNameLabel2.setName("CertNameLabel2"); // NOI18N
        CertNameLabel2.setPreferredSize(new java.awt.Dimension(80, 20));
        CePanel.add(CertNameLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1170, 0, 100, -1));

        CertOther7DateFrom.setDateFormatString(resourceMap.getString("CertOther7DateFrom.dateFormatString")); // NOI18N
        CertOther7DateFrom.setName("CertOther7DateFrom"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther7From}"), CertOther7DateFrom, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther7DateFrom, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther7DateFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 20, 85, -1));

        CertOther8DateFrom.setDateFormatString(resourceMap.getString("CertOther8DateFrom.dateFormatString")); // NOI18N
        CertOther8DateFrom.setName("CertOther8DateFrom"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther8From}"), CertOther8DateFrom, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther8DateFrom, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther8DateFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 40, 85, -1));

        CertOther9DateFrom.setDateFormatString(resourceMap.getString("CertOther9DateFrom.dateFormatString")); // NOI18N
        CertOther9DateFrom.setName("CertOther9DateFrom"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther9From}"), CertOther9DateFrom, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther9DateFrom, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther9DateFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 60, 85, -1));

        CertOther10DateFrom.setDateFormatString(resourceMap.getString("CertOther10DateFrom.dateFormatString")); // NOI18N
        CertOther10DateFrom.setName("CertOther10DateFrom"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther10From}"), CertOther10DateFrom, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther10DateFrom, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther10DateFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 80, 85, -1));

        CertOther7DateTill.setDateFormatString(resourceMap.getString("CertOther7DateTill.dateFormatString")); // NOI18N
        CertOther7DateTill.setName("CertOther7DateTill"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther7Till}"), CertOther7DateTill, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther7DateTill, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther7DateTill, new org.netbeans.lib.awtextra.AbsoluteConstraints(1075, 20, 85, -1));

        CertOther8DateTill.setDateFormatString(resourceMap.getString("CertOther8DateTill.dateFormatString")); // NOI18N
        CertOther8DateTill.setName("CertOther8DateTill"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther8Till}"), CertOther8DateTill, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther8DateTill, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther8DateTill, new org.netbeans.lib.awtextra.AbsoluteConstraints(1075, 40, 85, -1));

        CertOther9DateTill.setDateFormatString(resourceMap.getString("CertOther9DateTill.dateFormatString")); // NOI18N
        CertOther9DateTill.setName("CertOther9DateTill"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther9Till}"), CertOther9DateTill, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther9DateTill, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther9DateTill, new org.netbeans.lib.awtextra.AbsoluteConstraints(1075, 60, 85, -1));

        CertOther10DateTill.setDateFormatString(resourceMap.getString("CertOther10DateTill.dateFormatString")); // NOI18N
        CertOther10DateTill.setName("CertOther10DateTill"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther10Till}"), CertOther10DateTill, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther10DateTill, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther10DateTill, new org.netbeans.lib.awtextra.AbsoluteConstraints(1075, 80, 85, -1));

        CertOther7FromButton.setBackground(resourceMap.getColor("CertOther7FromButton.background")); // NOI18N
        CertOther7FromButton.setForeground(resourceMap.getColor("CertOther7FromButton.foreground")); // NOI18N
        CertOther7FromButton.setName("CertOther7FromButton"); // NOI18N
        CertOther7FromButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther7FromButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertOther7FromButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1065, 20, 5, 5));

        CertOther8FromButton.setBackground(resourceMap.getColor("CertOther8FromButton.background")); // NOI18N
        CertOther8FromButton.setForeground(resourceMap.getColor("CertOther8FromButton.foreground")); // NOI18N
        CertOther8FromButton.setName("CertOther8FromButton"); // NOI18N
        CertOther8FromButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther8FromButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertOther8FromButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1065, 40, 5, 5));

        CertOther9FromButton.setBackground(resourceMap.getColor("CertOther9FromButton.background")); // NOI18N
        CertOther9FromButton.setForeground(resourceMap.getColor("CertOther9FromButton.foreground")); // NOI18N
        CertOther9FromButton.setName("CertOther9FromButton"); // NOI18N
        CertOther9FromButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther9FromButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertOther9FromButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1065, 60, 5, 5));

        CertOther10FromButton.setBackground(resourceMap.getColor("CertOther10FromButton.background")); // NOI18N
        CertOther10FromButton.setForeground(resourceMap.getColor("CertOther10FromButton.foreground")); // NOI18N
        CertOther10FromButton.setName("CertOther10FromButton"); // NOI18N
        CertOther10FromButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther10FromButtonActionPerformed(evt);
            }
        });
        CePanel.add(CertOther10FromButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1065, 80, 5, 5));

        CertOther3TillButton1.setBackground(resourceMap.getColor("CertOther3TillButton1.background")); // NOI18N
        CertOther3TillButton1.setForeground(resourceMap.getColor("CertOther3TillButton1.foreground")); // NOI18N
        CertOther3TillButton1.setName("CertOther3TillButton1"); // NOI18N
        CertOther3TillButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther3TillButton1ActionPerformed(evt);
            }
        });
        CePanel.add(CertOther3TillButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1160, 20, 5, 5));

        CertOther4TillButton1.setBackground(resourceMap.getColor("CertOther4TillButton1.background")); // NOI18N
        CertOther4TillButton1.setForeground(resourceMap.getColor("CertOther4TillButton1.foreground")); // NOI18N
        CertOther4TillButton1.setName("CertOther4TillButton1"); // NOI18N
        CertOther4TillButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther4TillButton1ActionPerformed(evt);
            }
        });
        CePanel.add(CertOther4TillButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1160, 40, 5, 5));

        CertOther5TillButton1.setBackground(resourceMap.getColor("CertOther5TillButton1.background")); // NOI18N
        CertOther5TillButton1.setForeground(resourceMap.getColor("CertOther5TillButton1.foreground")); // NOI18N
        CertOther5TillButton1.setName("CertOther5TillButton1"); // NOI18N
        CertOther5TillButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther5TillButton1ActionPerformed(evt);
            }
        });
        CePanel.add(CertOther5TillButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1160, 60, 5, 5));

        CertOther6TillButton1.setBackground(resourceMap.getColor("CertOther6TillButton1.background")); // NOI18N
        CertOther6TillButton1.setForeground(resourceMap.getColor("CertOther6TillButton1.foreground")); // NOI18N
        CertOther6TillButton1.setName("CertOther6TillButton1"); // NOI18N
        CertOther6TillButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CertOther6TillButton1ActionPerformed(evt);
            }
        });
        CePanel.add(CertOther6TillButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1160, 80, 5, 5));

        CertOther9NameField.setName("CertOther9NameField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther9Name}"), CertOther9NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther9NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther9NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(1170, 60, 100, 20));

        CertOther10NameField.setName("CertOther10NameField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther10Name}"), CertOther10NameField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther10NameField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther10NameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(1170, 80, 100, 20));

        CertOther3TextField.setName("CertOther3TextField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther3}"), CertOther3TextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther3TextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther3TextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 20, 70, -1));

        CertOther4TextField.setName("CertOther4TextField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther4}"), CertOther4TextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther4TextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther4TextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 40, 70, -1));

        CertOther5TextField.setName("CertOther5TextField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther5}"), CertOther5TextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther5TextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther5TextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 60, 70, -1));

        CertOther6TextField.setName("CertOther6TextField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther6}"), CertOther6TextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther6TextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther6TextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 80, 70, -1));

        CertOther7TextField.setName("CertOther7TextField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther7}"), CertOther7TextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther7TextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther7TextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 20, 70, -1));

        CertOther8TextField.setName("CertOther8TextField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther8}"), CertOther8TextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther8TextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther8TextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 40, 70, -1));

        CertOther9TextField.setName("CertOther9TextField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther9}"), CertOther9TextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther9TextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther9TextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 60, 70, -1));

        CertOther10TextField.setName("CertOther10TextField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.certOther10}"), CertOther10TextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), CertOther10TextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        CePanel.add(CertOther10TextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 80, 70, -1));

        mainPanel.add(CePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(155, 450, 1275, 110));

        DeclPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(-16777216,true)));
        DeclPanel.setName("DeclPanel"); // NOI18N
        DeclPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        declLabel.setFont(resourceMap.getFont("declLabel.font")); // NOI18N
        declLabel.setText(resourceMap.getString("declLabel.text")); // NOI18N
        declLabel.setName("declLabel"); // NOI18N
        DeclPanel.add(declLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 1, -1, -1));

        declRecLabel.setFont(resourceMap.getFont("declSigLabel.font")); // NOI18N
        declRecLabel.setText(resourceMap.getString("declRecLabel.text")); // NOI18N
        declRecLabel.setName("declRecLabel"); // NOI18N
        DeclPanel.add(declRecLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(147, 5, 50, -1));

        declSigLabel.setFont(resourceMap.getFont("declSigLabel.font")); // NOI18N
        declSigLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        declSigLabel.setText(resourceMap.getString("declSigLabel.text")); // NOI18N
        declSigLabel.setName("declSigLabel"); // NOI18N
        DeclPanel.add(declSigLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(197, 5, 70, -1));

        declBrandLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        declBrandLabel.setText(resourceMap.getString("declBrandLabel.text")); // NOI18N
        declBrandLabel.setName("declBrandLabel"); // NOI18N
        declBrandLabel.setPreferredSize(new java.awt.Dimension(80, 20));
        DeclPanel.add(declBrandLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 20, 100, -1));
        declBrandLabel.getAccessibleContext().setAccessibleParent(DeclPanel);

        DeclBrandCheckBox.setText(resourceMap.getString("DeclBrandCheckBox.text")); // NOI18N
        DeclBrandCheckBox.setName("DeclBrandCheckBox"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.declBrand}"), DeclBrandCheckBox, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), DeclBrandCheckBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding.setSourceUnreadableValue(null);
        DeclPanel.add(DeclBrandCheckBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 20, -1, -1));

        DeclBrandDateChooser.setDateFormatString(resourceMap.getString("DeclBrandDateChooser.dateFormatString")); // NOI18N
        DeclBrandDateChooser.setName("DeclBrandDateChooser"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.declBrandDate}"), DeclBrandDateChooser, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, DeclBrandCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), DeclBrandDateChooser, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        DeclPanel.add(DeclBrandDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 20, 100, -1));

        DeclBrandButton.setBackground(resourceMap.getColor("DeclBrandButton.background")); // NOI18N
        DeclBrandButton.setForeground(resourceMap.getColor("DeclBrandButton.foreground")); // NOI18N
        DeclBrandButton.setName("DeclBrandButton"); // NOI18N
        DeclBrandButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeclBrandButtonActionPerformed(evt);
            }
        });
        DeclPanel.add(DeclBrandButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 20, 5, 5));

        declPackLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        declPackLabel.setText(resourceMap.getString("declPackLabel.text")); // NOI18N
        declPackLabel.setName("declPackLabel"); // NOI18N
        declPackLabel.setPreferredSize(new java.awt.Dimension(80, 20));
        DeclPanel.add(declPackLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 150, -1));
        declPackLabel.getAccessibleContext().setAccessibleParent(DeclPanel);

        DeclPackCheckBox.setText(resourceMap.getString("DeclPackCheckBox.text")); // NOI18N
        DeclPackCheckBox.setName("DeclPackCheckBox"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.declPack}"), DeclPackCheckBox, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), DeclPackCheckBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding.setSourceUnreadableValue(null);
        DeclPanel.add(DeclPackCheckBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 40, -1, -1));

        DeclPackDateChooser.setDateFormatString(resourceMap.getString("DeclPackDateChooser.dateFormatString")); // NOI18N
        DeclPackDateChooser.setName("DeclPackDateChooser"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.declPackDate}"), DeclPackDateChooser, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, DeclPackCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), DeclPackDateChooser, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        DeclPanel.add(DeclPackDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 40, 100, -1));

        DeclPackButton.setBackground(resourceMap.getColor("DeclPackButton.background")); // NOI18N
        DeclPackButton.setForeground(resourceMap.getColor("DeclPackButton.foreground")); // NOI18N
        DeclPackButton.setName("DeclPackButton"); // NOI18N
        DeclPackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeclPackButtonActionPerformed(evt);
            }
        });
        DeclPanel.add(DeclPackButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 40, 5, 5));

        declContrLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        declContrLabel.setText(resourceMap.getString("declContrLabel.text")); // NOI18N
        declContrLabel.setName("declContrLabel"); // NOI18N
        declContrLabel.setPreferredSize(new java.awt.Dimension(80, 20));
        DeclPanel.add(declContrLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 140, -1));
        declContrLabel.getAccessibleContext().setAccessibleParent(DeclPanel);

        DeclContrCheckBox.setText(resourceMap.getString("DeclContrCheckBox.text")); // NOI18N
        DeclContrCheckBox.setName("DeclContrCheckBox"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.declContr}"), DeclContrCheckBox, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), DeclContrCheckBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding.setSourceUnreadableValue(null);
        DeclPanel.add(DeclContrCheckBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 60, -1, -1));

        DeclContrDateChooser.setDateFormatString(resourceMap.getString("DeclContrDateChooser.dateFormatString")); // NOI18N
        DeclContrDateChooser.setName("DeclContrDateChooser"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.declContrDate}"), DeclContrDateChooser, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, DeclContrCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), DeclContrDateChooser, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        DeclPanel.add(DeclContrDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 60, 100, -1));

        DeclContrButton.setBackground(resourceMap.getColor("DeclContrButton.background")); // NOI18N
        DeclContrButton.setForeground(resourceMap.getColor("DeclContrButton.foreground")); // NOI18N
        DeclContrButton.setName("DeclContrButton"); // NOI18N
        DeclContrButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeclContrButtonActionPerformed(evt);
            }
        });
        DeclPanel.add(DeclContrButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 60, 5, 5));

        declReachLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        declReachLabel.setText(resourceMap.getString("declReachLabel.text")); // NOI18N
        declReachLabel.setName("declReachLabel"); // NOI18N
        declReachLabel.setPreferredSize(new java.awt.Dimension(34, 20));
        DeclPanel.add(declReachLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 60, 40, -1));

        declReachCheckBox.setText(resourceMap.getString("declReachCheckBox.text")); // NOI18N
        declReachCheckBox.setName("declReachCheckBox"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.declReach}"), declReachCheckBox, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), declReachCheckBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        DeclPanel.add(declReachCheckBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 60, -1, -1));

        declReachDateChooser.setDateFormatString(resourceMap.getString("declReachDateChooser.dateFormatString")); // NOI18N
        declReachDateChooser.setName("declReachDateChooser"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.declReachDate}"), declReachDateChooser, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, declReachCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), declReachDateChooser, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        DeclPanel.add(declReachDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 60, 100, -1));

        DeclReachButton.setBackground(resourceMap.getColor("DeclReachButton.background")); // NOI18N
        DeclReachButton.setForeground(resourceMap.getColor("DeclReachButton.foreground")); // NOI18N
        DeclReachButton.setName("DeclReachButton"); // NOI18N
        DeclReachButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeclReachButtonActionPerformed(evt);
            }
        });
        DeclPanel.add(DeclReachButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 60, 5, 5));

        declRohsLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        declRohsLabel.setText(resourceMap.getString("declRohsLabel.text")); // NOI18N
        declRohsLabel.setName("declRohsLabel"); // NOI18N
        declRohsLabel.setPreferredSize(new java.awt.Dimension(40, 20));
        DeclPanel.add(declRohsLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 80, 40, -1));

        declRohsCheckBox.setText(resourceMap.getString("declRohsCheckBox.text")); // NOI18N
        declRohsCheckBox.setName("declRohsCheckBox"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.declRohs}"), declRohsCheckBox, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), declRohsCheckBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        DeclPanel.add(declRohsCheckBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 80, -1, -1));

        declRohsDateChooser.setDateFormatString(resourceMap.getString("declRohsDateChooser.dateFormatString")); // NOI18N
        declRohsDateChooser.setName("declRohsDateChooser"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.declRohsDate}"), declRohsDateChooser, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, declRohsCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), declRohsDateChooser, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        DeclPanel.add(declRohsDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 80, 100, -1));

        DeclRohsButton.setBackground(resourceMap.getColor("DeclRohsButton.background")); // NOI18N
        DeclRohsButton.setForeground(resourceMap.getColor("DeclRohsButton.foreground")); // NOI18N
        DeclRohsButton.setName("DeclRohsButton"); // NOI18N
        DeclRohsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeclRohsButtonActionPerformed(evt);
            }
        });
        DeclPanel.add(DeclRohsButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 80, 5, 5));

        declSdaLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        declSdaLabel.setText(resourceMap.getString("declSdaLabel.text")); // NOI18N
        declSdaLabel.setName("declSdaLabel"); // NOI18N
        declSdaLabel.setPreferredSize(new java.awt.Dimension(40, 20));
        DeclPanel.add(declSdaLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 20, 40, -1));

        declSdaCheckBox.setName("declSdaCheckBox"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.declSda}"), declSdaCheckBox, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), declSdaCheckBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        DeclPanel.add(declSdaCheckBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 20, -1, -1));

        declSdaDateChooser.setDateFormatString(resourceMap.getString("declSdaDateChooser.dateFormatString")); // NOI18N
        declSdaDateChooser.setName("declSdaDateChooser"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.declSdaDate}"), declSdaDateChooser, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, declSdaCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), declSdaDateChooser, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        DeclPanel.add(declSdaDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 20, 100, -1));

        declSdaButton.setBackground(resourceMap.getColor("declSdaButton.background")); // NOI18N
        declSdaButton.setForeground(resourceMap.getColor("declSdaButton.foreground")); // NOI18N
        declSdaButton.setName("declSdaButton"); // NOI18N
        declSdaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                declSdaButtonActionPerformed(evt);
            }
        });
        DeclPanel.add(declSdaButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 20, 5, 5));

        declSopLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        declSopLabel.setText(resourceMap.getString("declSopLabel.text")); // NOI18N
        declSopLabel.setName("declSopLabel"); // NOI18N
        declSopLabel.setPreferredSize(new java.awt.Dimension(40, 20));
        DeclPanel.add(declSopLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 40, 40, -1));

        declSopCheckBox.setName("declSopCheckBox"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.declSop}"), declSopCheckBox, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), declSopCheckBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        DeclPanel.add(declSopCheckBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 40, -1, -1));

        declSopDateChooser.setDateFormatString(resourceMap.getString("declSopDateChooser.dateFormatString")); // NOI18N
        declSopDateChooser.setName("declSopDateChooser"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.declSopDate}"), declSopDateChooser, org.jdesktop.beansbinding.BeanProperty.create("date"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, declSopCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), declSopDateChooser, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        DeclPanel.add(declSopDateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 40, 100, -1));

        declSopButton.setBackground(resourceMap.getColor("declSopButton.background")); // NOI18N
        declSopButton.setForeground(resourceMap.getColor("declSopButton.foreground")); // NOI18N
        declSopButton.setName("declSopButton"); // NOI18N
        declSopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                declSopButtonActionPerformed(evt);
            }
        });
        DeclPanel.add(declSopButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 40, 5, 5));

        declWarrantyLabel.setText(resourceMap.getString("declWarrantyLabel.text")); // NOI18N
        declWarrantyLabel.setName("declWarrantyLabel"); // NOI18N
        declWarrantyLabel.setPreferredSize(new java.awt.Dimension(80, 20));
        DeclPanel.add(declWarrantyLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 30, 110, -1));

        declWarrantyScrollPane.setName("declWarrantyScrollPane"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), declWarrantyScrollPane, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        declWarrantyTextArea.setColumns(20);
        declWarrantyTextArea.setRows(5);
        declWarrantyTextArea.setName("declWarrantyTextArea"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.declWarranty}"), declWarrantyTextArea, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), declWarrantyTextArea, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        declWarrantyScrollPane.setViewportView(declWarrantyTextArea);

        DeclPanel.add(declWarrantyScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 30, 590, 70));

        declRecLabel1.setFont(resourceMap.getFont("declRecLabel1.font")); // NOI18N
        declRecLabel1.setText(resourceMap.getString("declRecLabel1.text")); // NOI18N
        declRecLabel1.setName("declRecLabel1"); // NOI18N
        DeclPanel.add(declRecLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 5, 50, -1));

        declSigLabel1.setFont(resourceMap.getFont("declRecLabel1.font")); // NOI18N
        declSigLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        declSigLabel1.setText(resourceMap.getString("declSigLabel1.text")); // NOI18N
        declSigLabel1.setName("declSigLabel1"); // NOI18N
        DeclPanel.add(declSigLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 5, 70, -1));

        declPayLabel.setText(resourceMap.getString("declPayLabel.text")); // NOI18N
        declPayLabel.setName("declPayLabel"); // NOI18N
        DeclPanel.add(declPayLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 5, -1, 20));

        declPayComboBox.setMaximumRowCount(25);
        declPayComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", "IB00", "IB10", "IB11", "IB12", "IB19", "IB20", "IB21", "IB43", "IB49", "IB84", "IB90", "IB91", "IB95", "IBA2", "IBA5", "IBA7", "IBE4", "IBF1", "IBF3", "IBF8", "IBH7", "IBH8", "IBH9", "VC68" }));
        declPayComboBox.setName("declPayComboBox"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ObjectProperty.create(), declPayComboBox, org.jdesktop.beansbinding.BeanProperty.create("elements"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.declPay}"), declPayComboBox, org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), declPayComboBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        declPayComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                declPayComboBoxActionPerformed(evt);
            }
        });
        DeclPanel.add(declPayComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 5, -1, -1));

        declPayLabel1.setFont(resourceMap.getFont("declPayLabel1.font")); // NOI18N
        declPayLabel1.setText(resourceMap.getString("declPayLabel1.text")); // NOI18N
        declPayLabel1.setName("declPayLabel1"); // NOI18N
        DeclPanel.add(declPayLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 5, -1, 20));

        mainPanel.add(DeclPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(155, 560, 1275, 105));

        SapLabel1.setText(resourceMap.getString("SapLabel1.text")); // NOI18N
        SapLabel1.setName("SapLabel1"); // NOI18N
        SapLabel1.setPreferredSize(new java.awt.Dimension(80, 26));
        mainPanel.add(SapLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1150, 10, 50, -1));

        SapTextField1.setFont(resourceMap.getFont("SapTextField1.font")); // NOI18N
        SapTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        SapTextField1.setName("SapTextField1"); // NOI18N
        SapTextField1.setPreferredSize(new java.awt.Dimension(59, 26));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.officeVendorSFE}"), SapTextField1, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), SapTextField1, org.jdesktop.beansbinding.BeanProperty.create("editable"));
        bindingGroup.addBinding(binding);

        mainPanel.add(SapTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1200, 10, 90, -1));

        menuBar.setName("menuBar"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(suppliers.SuppliersApp.class).getContext().getActionMap(SuppliersView.class, this);
        fileMenu.setAction(actionMap.get("printFrame")); // NOI18N
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        newRecordMenuItem.setAction(actionMap.get("newRecord")); // NOI18N
        newRecordMenuItem.setName("newRecordMenuItem"); // NOI18N
        fileMenu.add(newRecordMenuItem);

        deleteRecordMenuItem.setAction(actionMap.get("deleteRecord")); // NOI18N
        deleteRecordMenuItem.setName("deleteRecordMenuItem"); // NOI18N
        fileMenu.add(deleteRecordMenuItem);

        jSeparator1.setName("jSeparator1"); // NOI18N
        fileMenu.add(jSeparator1);

        saveMenuItem.setAction(actionMap.get("save")); // NOI18N
        saveMenuItem.setName("saveMenuItem"); // NOI18N
        fileMenu.add(saveMenuItem);

        refreshMenuItem.setAction(actionMap.get("refresh")); // NOI18N
        refreshMenuItem.setName("refreshMenuItem"); // NOI18N
        fileMenu.add(refreshMenuItem);

        jMenuItem1.setAction(actionMap.get("printFrame")); // NOI18N
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        fileMenu.add(jMenuItem1);

        jSeparator2.setName("jSeparator2"); // NOI18N
        fileMenu.add(jSeparator2);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        menuBar.add(jMenu1);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N
        statusPanel.setPreferredSize(new java.awt.Dimension(1441, 37));
        statusPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N
        statusPanel.add(statusPanelSeparator, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1441, -1));

        newButton.setAction(actionMap.get("newRecord")); // NOI18N
        newButton.setName("newButton"); // NOI18N
        statusPanel.add(newButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 8, 71, -1));

        deleteButton.setAction(actionMap.get("deleteRecord")); // NOI18N
        deleteButton.setName("deleteButton"); // NOI18N
        statusPanel.add(deleteButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 8, 71, -1));

        refreshButton.setAction(actionMap.get("refresh")); // NOI18N
        refreshButton.setName("refreshButton"); // NOI18N
        statusPanel.add(refreshButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 8, -1, -1));

        saveButton.setAction(actionMap.get("save")); // NOI18N
        saveButton.setName("saveButton"); // NOI18N
        statusPanel.add(saveButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 8, 71, -1));

        FolderButton.setText(resourceMap.getString("FolderButton.text")); // NOI18N
        FolderButton.setName("FolderButton"); // NOI18N
        FolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FolderButtonActionPerformed(evt);
            }
        });
        statusPanel.add(FolderButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 8, -1, -1));

        folderTextField.setName("folderTextField"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement.folder}"), folderTextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, masterTable, org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"), folderTextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding.setSourceUnreadableValue(null);
        statusPanel.add(folderTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 8, 360, 23));

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N
        statusPanel.add(statusMessageLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(1270, 10, -1, -1));

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N
        statusPanel.add(statusAnimationLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(1410, 10, -1, -1));

        progressBar.setName("progressBar"); // NOI18N
        progressBar.setPreferredSize(new java.awt.Dimension(146, 10));
        statusPanel.add(progressBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(1270, 25, -1, -1));

        rowSorterToStringConverter1.setTable(masterTable);

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void bsciFromButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bsciFromButtonActionPerformed
        bsciFromDateChooser.setDate(null);
    }//GEN-LAST:event_bsciFromButtonActionPerformed

    private void bsciTillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bsciTillButtonActionPerformed
        bsciTillDateChooser.setDate(null);
    }//GEN-LAST:event_bsciTillButtonActionPerformed

    private void bsciOther1FromButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bsciOther1FromButtonActionPerformed
        bsciOther1DateFrom.setDate(null);
    }//GEN-LAST:event_bsciOther1FromButtonActionPerformed

    private void bsciOther2FromButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bsciOther2FromButtonActionPerformed
        bsciOther2DateFrom.setDate(null);
    }//GEN-LAST:event_bsciOther2FromButtonActionPerformed

    private void bsciOther3FromButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bsciOther3FromButtonActionPerformed
        bsciOther3DateFrom.setDate(null);
    }//GEN-LAST:event_bsciOther3FromButtonActionPerformed

    private void bsciOther1TillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bsciOther1TillButtonActionPerformed
        bsciOther1DateTill.setDate(null);
    }//GEN-LAST:event_bsciOther1TillButtonActionPerformed

    private void bsciOther2TillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bsciOther2TillButtonActionPerformed
        bsciOther2DateTill.setDate(null);
    }//GEN-LAST:event_bsciOther2TillButtonActionPerformed

    private void bsciOther3TillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bsciOther3TillButtonActionPerformed
        bsciOther3DateTill.setDate(null);
    }//GEN-LAST:event_bsciOther3TillButtonActionPerformed

    private void CertIso9001FromButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertIso9001FromButtonActionPerformed
        CertIso9001DateFrom.setDate(null);
    }//GEN-LAST:event_CertIso9001FromButtonActionPerformed

    private void CertIso140000FromButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertIso140000FromButtonActionPerformed
        CertIso140000DateFrom.setDate(null);
    }//GEN-LAST:event_CertIso140000FromButtonActionPerformed

    private void CertOther1FromButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther1FromButtonActionPerformed
        CertOther1DateFrom.setDate(null);
    }//GEN-LAST:event_CertOther1FromButtonActionPerformed

    private void CertOther2FromButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther2FromButtonActionPerformed
        CertOther2DateFrom.setDate(null);
    }//GEN-LAST:event_CertOther2FromButtonActionPerformed

    private void CertOther3FromButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther3FromButtonActionPerformed
        CertOther3DateFrom.setDate(null);
    }//GEN-LAST:event_CertOther3FromButtonActionPerformed

    private void CertIso9001TillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertIso9001TillButtonActionPerformed
        CertIso9001DateTill.setDate(null);
    }//GEN-LAST:event_CertIso9001TillButtonActionPerformed

    private void CertIso140000TillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertIso140000TillButtonActionPerformed
        CertIso140000DateTill.setDate(null);
    }//GEN-LAST:event_CertIso140000TillButtonActionPerformed

    private void CertOther1TillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther1TillButtonActionPerformed
        CertOther1DateTill.setDate(null);
    }//GEN-LAST:event_CertOther1TillButtonActionPerformed

    private void CertOther2TillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther2TillButtonActionPerformed
        CertOther2DateTill.setDate(null);
    }//GEN-LAST:event_CertOther2TillButtonActionPerformed

    private void CertOther3TillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther3TillButtonActionPerformed
        CertOther3DateTill.setDate(null);
    }//GEN-LAST:event_CertOther3TillButtonActionPerformed

    private void DeclBrandButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeclBrandButtonActionPerformed
        DeclBrandDateChooser.setDate(null);
    }//GEN-LAST:event_DeclBrandButtonActionPerformed

    private void DeclPackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeclPackButtonActionPerformed
        DeclPackDateChooser.setDate(null);
    }//GEN-LAST:event_DeclPackButtonActionPerformed

    private void DeclContrButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeclContrButtonActionPerformed
        DeclContrDateChooser.setDate(null);
    }//GEN-LAST:event_DeclContrButtonActionPerformed

    private void DeclReachButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeclReachButtonActionPerformed
        declReachDateChooser.setDate(null);
    }//GEN-LAST:event_DeclReachButtonActionPerformed

    private void DeclRohsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeclRohsButtonActionPerformed
        declRohsDateChooser.setDate(null);
    }//GEN-LAST:event_DeclRohsButtonActionPerformed

    private void CertOther4FromButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther4FromButtonActionPerformed
        CertOther4DateFrom.setDate(null);
    }//GEN-LAST:event_CertOther4FromButtonActionPerformed

    private void CertOther4TillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther4TillButtonActionPerformed
        CertOther4DateTill.setDate(null);
    }//GEN-LAST:event_CertOther4TillButtonActionPerformed

    private void CertOther5FromButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther5FromButtonActionPerformed
        CertOther5DateFrom.setDate(null);
    }//GEN-LAST:event_CertOther5FromButtonActionPerformed

    private void CertOther5TillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther5TillButtonActionPerformed
        CertOther5DateTill.setDate(null);
    }//GEN-LAST:event_CertOther5TillButtonActionPerformed
    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed

        if (raport == null) {
            JFrame mainFrame = SuppliersApp.getApplication().getMainFrame();
            raport = new SupplierRaport(mainFrame);
            raport.setLocationRelativeTo(mainFrame);
        }
        SuppliersApp.getApplication().show(raport);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void FolderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FolderButtonActionPerformed
        String path = folderTextField.getText();

        File mainpath = new File("G:/QC/Suppliers/Asia");

        File file = new File(mainpath + "/" + path);

        Desktop desktop = null;

        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }
        try {
            if (file.exists()) {
                desktop.open(file);
            } else {
                JOptionPane.showMessageDialog(null, "Folder doesn't exist, check the name.", "Folder error", JOptionPane.INFORMATION_MESSAGE);                
            }

        } catch (IOException e) {
        }
    }//GEN-LAST:event_FolderButtonActionPerformed

    private void declSdaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_declSdaButtonActionPerformed
        declSdaDateChooser.setDate(null);
    }//GEN-LAST:event_declSdaButtonActionPerformed

    private void declSopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_declSopButtonActionPerformed
        declSopDateChooser.setDate(null);
    }//GEN-LAST:event_declSopButtonActionPerformed

    private void declPayComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_declPayComboBoxActionPerformed
        switch (declPayComboBox.getSelectedIndex())
        {
            case 1: declPayLabel1.setText("Within 8 days Due net");break;
            case 2: declPayLabel1.setText("Within 14 days 2% cash dis, Within 30 days Due net");break;
            case 3: declPayLabel1.setText("Within 14 days 3% cash dis, Within 30 days Due net");break;
            case 4: declPayLabel1.setText("Within 14 days Due net");break;
            case 5: declPayLabel1.setText("Within 30 days Due net");break;
            case 6: declPayLabel1.setText("Within 45 days 3% cash dis, Within 60 days Due net");break;
            case 7: declPayLabel1.setText("Within 60 days Due net");break;
            case 8: declPayLabel1.setText("Within 21 days Due net");break;
            case 9: declPayLabel1.setText("Within 45 days Due net");break;
            case 10: declPayLabel1.setText("Within 15 days 3% cash dis, Within 60 days Due net");break;
            case 11: declPayLabel1.setText("Payable immediately Due net - TT direct na B/L");break;
            case 12: declPayLabel1.setText("Payable immediately Due net - TT 5 dagen na B/L");break;
            case 13: declPayLabel1.setText("Payable immediately - TT 20% na PI en TT 80% direc");break;
            case 14: declPayLabel1.setText("Payable immediately - TT 10% NA PI, 90% DIRECT NA");break;
            case 15: declPayLabel1.setText("Payable immediately - TT 30% na PI en TT 70% direc");break;
            case 16: declPayLabel1.setText("Payable immediately Due net");break;
            case 17: declPayLabel1.setText("Within 30 days 3% cash dis, Within 60 days Due net");break;
            case 18: declPayLabel1.setText("Within 90 days Due net");break;
            case 19: declPayLabel1.setText("Within 120 days Due net");break;
            case 20: declPayLabel1.setText("Within 15 days Due net");break;
            case 21: declPayLabel1.setText("Payable immediately - TT 30% na PI en TT 70% B/S");break;
            case 22: declPayLabel1.setText("Payable imm. TT 50% na PI en TT 50% 60 Days");break;
            case 23: declPayLabel1.setText("DB Smartwares Finance Program 120 days");break;
            case 24: declPayLabel1.setText("Within 75 days Due net");break;
        }
    }//GEN-LAST:event_declPayComboBoxActionPerformed

    private void CertOther6FromButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther6FromButtonActionPerformed
        CertOther6DateFrom.setDate(null);
    }//GEN-LAST:event_CertOther6FromButtonActionPerformed

    private void CertOther6TillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther6TillButtonActionPerformed
        CertOther6DateTill.setDate(null);
    }//GEN-LAST:event_CertOther6TillButtonActionPerformed

    private void CertOther7FromButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther7FromButtonActionPerformed
        CertOther7DateFrom.setDate(null);
    }//GEN-LAST:event_CertOther7FromButtonActionPerformed

    private void CertOther8FromButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther8FromButtonActionPerformed
        CertOther8DateFrom.setDate(null);
    }//GEN-LAST:event_CertOther8FromButtonActionPerformed

    private void CertOther9FromButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther9FromButtonActionPerformed
        CertOther9DateFrom.setDate(null);
    }//GEN-LAST:event_CertOther9FromButtonActionPerformed

    private void CertOther10FromButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther10FromButtonActionPerformed
        CertOther10DateFrom.setDate(null);
    }//GEN-LAST:event_CertOther10FromButtonActionPerformed

    private void CertOther3TillButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther3TillButton1ActionPerformed
        CertOther7DateTill.setDate(null);
    }//GEN-LAST:event_CertOther3TillButton1ActionPerformed

    private void CertOther4TillButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther4TillButton1ActionPerformed
        CertOther8DateTill.setDate(null);
    }//GEN-LAST:event_CertOther4TillButton1ActionPerformed

    private void CertOther5TillButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther5TillButton1ActionPerformed
        CertOther9DateTill.setDate(null);
    }//GEN-LAST:event_CertOther5TillButton1ActionPerformed

    private void CertOther6TillButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CertOther6TillButton1ActionPerformed
        CertOther10DateTill.setDate(null);
    }//GEN-LAST:event_CertOther6TillButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel AddressLabel;
    private javax.swing.JPanel BsciPanel;
    private javax.swing.JPanel CePanel;
    private javax.swing.JLabel CertFromLabel;
    private javax.swing.JLabel CertFromLabel1;
    private javax.swing.JLabel CertFromLabel2;
    private com.toedter.calendar.JDateChooser CertIso140000DateFrom;
    private com.toedter.calendar.JDateChooser CertIso140000DateTill;
    private javax.swing.JButton CertIso140000FromButton;
    private javax.swing.JButton CertIso140000TillButton;
    private com.toedter.calendar.JDateChooser CertIso9001DateFrom;
    private com.toedter.calendar.JDateChooser CertIso9001DateTill;
    private javax.swing.JButton CertIso9001FromButton;
    private javax.swing.JButton CertIso9001TillButton;
    private javax.swing.JLabel CertNameLabel;
    private javax.swing.JLabel CertNameLabel1;
    private javax.swing.JLabel CertNameLabel2;
    private com.toedter.calendar.JDateChooser CertOther10DateFrom;
    private com.toedter.calendar.JDateChooser CertOther10DateTill;
    private javax.swing.JButton CertOther10FromButton;
    private javax.swing.JTextField CertOther10NameField;
    private javax.swing.JTextField CertOther10TextField;
    private com.toedter.calendar.JDateChooser CertOther1DateFrom;
    private com.toedter.calendar.JDateChooser CertOther1DateTill;
    private javax.swing.JButton CertOther1FromButton;
    private javax.swing.JTextField CertOther1TextField;
    private javax.swing.JButton CertOther1TillButton;
    private com.toedter.calendar.JDateChooser CertOther2DateFrom;
    private com.toedter.calendar.JDateChooser CertOther2DateTill;
    private javax.swing.JButton CertOther2FromButton;
    private javax.swing.JTextField CertOther2TextField;
    private javax.swing.JButton CertOther2TillButton;
    private com.toedter.calendar.JDateChooser CertOther3DateFrom;
    private com.toedter.calendar.JDateChooser CertOther3DateTill;
    private javax.swing.JButton CertOther3FromButton;
    private javax.swing.JTextField CertOther3TextField;
    private javax.swing.JButton CertOther3TillButton;
    private javax.swing.JButton CertOther3TillButton1;
    private com.toedter.calendar.JDateChooser CertOther4DateFrom;
    private com.toedter.calendar.JDateChooser CertOther4DateTill;
    private javax.swing.JButton CertOther4FromButton;
    private javax.swing.JTextField CertOther4TextField;
    private javax.swing.JButton CertOther4TillButton;
    private javax.swing.JButton CertOther4TillButton1;
    private com.toedter.calendar.JDateChooser CertOther5DateFrom;
    private com.toedter.calendar.JDateChooser CertOther5DateTill;
    private javax.swing.JButton CertOther5FromButton;
    private javax.swing.JTextField CertOther5TextField;
    private javax.swing.JButton CertOther5TillButton;
    private javax.swing.JButton CertOther5TillButton1;
    private com.toedter.calendar.JDateChooser CertOther6DateFrom;
    private com.toedter.calendar.JDateChooser CertOther6DateTill;
    private javax.swing.JButton CertOther6FromButton;
    private javax.swing.JTextField CertOther6NameField;
    private javax.swing.JTextField CertOther6TextField;
    private javax.swing.JButton CertOther6TillButton;
    private javax.swing.JButton CertOther6TillButton1;
    private com.toedter.calendar.JDateChooser CertOther7DateFrom;
    private com.toedter.calendar.JDateChooser CertOther7DateTill;
    private javax.swing.JButton CertOther7FromButton;
    private javax.swing.JTextField CertOther7NameField;
    private javax.swing.JTextField CertOther7TextField;
    private com.toedter.calendar.JDateChooser CertOther8DateFrom;
    private com.toedter.calendar.JDateChooser CertOther8DateTill;
    private javax.swing.JButton CertOther8FromButton;
    private javax.swing.JTextField CertOther8NameField;
    private javax.swing.JTextField CertOther8TextField;
    private com.toedter.calendar.JDateChooser CertOther9DateFrom;
    private com.toedter.calendar.JDateChooser CertOther9DateTill;
    private javax.swing.JButton CertOther9FromButton;
    private javax.swing.JTextField CertOther9NameField;
    private javax.swing.JTextField CertOther9TextField;
    private javax.swing.JLabel CertTillLabel;
    private javax.swing.JLabel CertTillLabel1;
    private javax.swing.JLabel CertTillLabel2;
    private javax.swing.JLabel CityLabel;
    private javax.swing.JLabel Contact1Label;
    private javax.swing.JLabel Contact2lLabel;
    private javax.swing.JLabel Contact3Label;
    private javax.swing.JLabel Contact4Label;
    private javax.swing.JLabel Contact5Label;
    private javax.swing.JLabel Contact6Label;
    private javax.swing.JLabel ContactLabel;
    private javax.swing.JLabel ContactNameLabel;
    private javax.swing.JPanel ContactPanel;
    private javax.swing.JLabel CountryLabel;
    private javax.swing.JTextField DBIDField;
    private javax.swing.JTextField DBIDField1;
    private javax.swing.JTextField DBIDField2;
    private javax.swing.JTextField DBIDField3;
    private javax.swing.JLabel DBIDLabel;
    private javax.swing.JButton DeclBrandButton;
    private javax.swing.JCheckBox DeclBrandCheckBox;
    private com.toedter.calendar.JDateChooser DeclBrandDateChooser;
    private javax.swing.JButton DeclContrButton;
    private javax.swing.JCheckBox DeclContrCheckBox;
    private com.toedter.calendar.JDateChooser DeclContrDateChooser;
    private javax.swing.JButton DeclPackButton;
    private javax.swing.JCheckBox DeclPackCheckBox;
    private com.toedter.calendar.JDateChooser DeclPackDateChooser;
    private javax.swing.JPanel DeclPanel;
    private javax.swing.JButton DeclReachButton;
    private javax.swing.JButton DeclRohsButton;
    private javax.swing.JLabel EmailLabel;
    private javax.swing.JCheckBox FOBCheckBox;
    private javax.swing.JLabel Factory1Label;
    private javax.swing.JLabel Factory2Label;
    private javax.swing.JLabel Factory3Label;
    private javax.swing.JTextField FilterTextField;
    private javax.swing.JButton FolderButton;
    private javax.swing.JLabel FunctionLabel;
    private javax.swing.JLabel NameLabel;
    private javax.swing.JLabel NoteLabel;
    private javax.swing.JScrollPane NoteScrollPane;
    private javax.swing.JLabel OfficeLabel;
    private javax.swing.JLabel PhoneLabel;
    private javax.swing.JLabel SAPLabel;
    private javax.swing.JLabel SapLabel;
    private javax.swing.JLabel SapLabel1;
    private javax.swing.JTextField SapTextField;
    private javax.swing.JTextField SapTextField1;
    private javax.swing.JLabel StateLabel;
    private javax.swing.JLabel WebLabel;
    private javax.swing.JLabel ZipLabel;
    private javax.swing.JButton bsciFromButton;
    private com.toedter.calendar.JDateChooser bsciFromDateChooser;
    private javax.swing.JLabel bsciFromLabel;
    private javax.swing.JLabel bsciLabel;
    private com.toedter.calendar.JDateChooser bsciOther1DateFrom;
    private com.toedter.calendar.JDateChooser bsciOther1DateTill;
    private javax.swing.JButton bsciOther1FromButton;
    private javax.swing.JTextField bsciOther1NameField;
    private javax.swing.JLabel bsciOther1NameLabel;
    private javax.swing.JButton bsciOther1TillButton;
    private com.toedter.calendar.JDateChooser bsciOther2DateFrom;
    private com.toedter.calendar.JDateChooser bsciOther2DateTill;
    private javax.swing.JButton bsciOther2FromButton;
    private javax.swing.JTextField bsciOther2NameField;
    private javax.swing.JLabel bsciOther2NameLabel;
    private javax.swing.JButton bsciOther2TillButton;
    private com.toedter.calendar.JDateChooser bsciOther3DateFrom;
    private com.toedter.calendar.JDateChooser bsciOther3DateTill;
    private javax.swing.JButton bsciOther3FromButton;
    private javax.swing.JTextField bsciOther3NameField;
    private javax.swing.JLabel bsciOther3NameLabel;
    private javax.swing.JButton bsciOther3TillButton;
    private javax.swing.JLabel bsciOtherFromLabel;
    private javax.swing.JLabel bsciOtherLabel;
    private javax.swing.JLabel bsciOtherTillLabel;
    private javax.swing.JComboBox bsciPartComboBox;
    private javax.swing.JLabel bsciPartLabel;
    private javax.swing.JComboBox bsciResultComboBox;
    private javax.swing.JLabel bsciResultLabel;
    private javax.swing.JButton bsciTillButton;
    private com.toedter.calendar.JDateChooser bsciTillDateChooser;
    private javax.swing.JLabel bsciTillLabel;
    private javax.swing.JComboBox buyerComboBox;
    private javax.swing.JLabel buyerLabel;
    private javax.swing.JTextField certIso14000NameField;
    private javax.swing.JTextField certIso14000TextField;
    private javax.swing.JTextField certIso9000NameField;
    private javax.swing.JTextField certIso9000TextField;
    private javax.swing.JLabel certLabel;
    private javax.swing.JTextField certOther1NameField;
    private javax.swing.JTextField certOther2NameField;
    private javax.swing.JTextField certOther3NameField;
    private javax.swing.JTextField certOther4NameField;
    private javax.swing.JTextField certOther5NameField;
    private javax.swing.JTextField contact1EmailField;
    private javax.swing.JTextField contact1FunctionField;
    private javax.swing.JTextField contact1NameField;
    private javax.swing.JTextField contact1PhoneField;
    private javax.swing.JTextField contact2EmailField;
    private javax.swing.JTextField contact2FunctionField;
    private javax.swing.JTextField contact2NameField;
    private javax.swing.JTextField contact2PhoneField;
    private javax.swing.JTextField contact3EmailField;
    private javax.swing.JTextField contact3FunctionField;
    private javax.swing.JTextField contact3NameField;
    private javax.swing.JTextField contact3PhoneField;
    private javax.swing.JTextField contact4EmailField;
    private javax.swing.JTextField contact4FunctionField;
    private javax.swing.JTextField contact4NameField;
    private javax.swing.JTextField contact4PhoneField;
    private javax.swing.JTextField contact5EmailField;
    private javax.swing.JTextField contact5FunctionField;
    private javax.swing.JTextField contact5NameField;
    private javax.swing.JTextField contact5PhoneField;
    private javax.swing.JTextField contact6EmailField;
    private javax.swing.JTextField contact6FunctionField;
    private javax.swing.JTextField contact6NameField;
    private javax.swing.JTextField contact6PhoneField;
    private javax.swing.JLabel declBrandLabel;
    private javax.swing.JLabel declContrLabel;
    private javax.swing.JLabel declLabel;
    private javax.swing.JLabel declPackLabel;
    private javax.swing.JComboBox declPayComboBox;
    private javax.swing.JLabel declPayLabel;
    private javax.swing.JLabel declPayLabel1;
    private javax.swing.JCheckBox declReachCheckBox;
    private com.toedter.calendar.JDateChooser declReachDateChooser;
    private javax.swing.JLabel declReachLabel;
    private javax.swing.JLabel declRecLabel;
    private javax.swing.JLabel declRecLabel1;
    private javax.swing.JCheckBox declRohsCheckBox;
    private com.toedter.calendar.JDateChooser declRohsDateChooser;
    private javax.swing.JLabel declRohsLabel;
    private javax.swing.JButton declSdaButton;
    private javax.swing.JCheckBox declSdaCheckBox;
    private com.toedter.calendar.JDateChooser declSdaDateChooser;
    private javax.swing.JLabel declSdaLabel;
    private javax.swing.JLabel declSigLabel;
    private javax.swing.JLabel declSigLabel1;
    private javax.swing.JButton declSopButton;
    private javax.swing.JCheckBox declSopCheckBox;
    private com.toedter.calendar.JDateChooser declSopDateChooser;
    private javax.swing.JLabel declSopLabel;
    private javax.swing.JLabel declWarrantyLabel;
    private javax.swing.JScrollPane declWarrantyScrollPane;
    private javax.swing.JTextArea declWarrantyTextArea;
    private javax.swing.JButton deleteButton;
    private javax.persistence.EntityManager entityManager;
    private javax.swing.JFormattedTextField factory1Address1;
    private javax.swing.JFormattedTextField factory1Address2;
    private javax.swing.JFormattedTextField factory1Address3;
    private javax.swing.JFormattedTextField factory1Address4;
    private javax.swing.JFormattedTextField factory1Address5;
    private javax.swing.JFormattedTextField factory1Address6;
    private javax.swing.JFormattedTextField factory1City;
    private javax.swing.JFormattedTextField factory1City1;
    private javax.swing.JFormattedTextField factory1Country;
    private javax.swing.JFormattedTextField factory1Country1;
    private javax.swing.JFormattedTextField factory1Name1;
    private javax.swing.JFormattedTextField factory1Name2;
    private javax.swing.JFormattedTextField factory1Name3;
    private javax.swing.JFormattedTextField factory1Name4;
    private javax.swing.JFormattedTextField factory1Province;
    private javax.swing.JFormattedTextField factory1Province1;
    private javax.swing.JTextField factory1Vendor;
    private javax.swing.JFormattedTextField factory1Www;
    private javax.swing.JFormattedTextField factory1Www1;
    private javax.swing.JFormattedTextField factory1Zip;
    private javax.swing.JFormattedTextField factory1Zip1;
    private javax.swing.JTextField factory2Vendor;
    private javax.swing.JFormattedTextField factory3Address1;
    private javax.swing.JFormattedTextField factory3Address2;
    private javax.swing.JFormattedTextField factory3Address3;
    private javax.swing.JFormattedTextField factory3City;
    private javax.swing.JFormattedTextField factory3Country;
    private javax.swing.JFormattedTextField factory3Name1;
    private javax.swing.JFormattedTextField factory3Name2;
    private javax.swing.JFormattedTextField factory3Province;
    private javax.swing.JTextField factory3Vendor;
    private javax.swing.JFormattedTextField factory3Www;
    private javax.swing.JFormattedTextField factory3Zip;
    private javax.swing.JTextField folderTextField;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextArea jTextArea5;
    private java.util.List<suppliers.Suppliers> list;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JScrollPane masterScrollPane;
    private javax.swing.JTable masterTable;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton newButton;
    private javax.swing.JFormattedTextField officeAddress1;
    private javax.swing.JFormattedTextField officeAddress2;
    private javax.swing.JFormattedTextField officeAddress3;
    private javax.swing.JFormattedTextField officeCity;
    private javax.swing.JFormattedTextField officeCountry;
    private javax.swing.JFormattedTextField officeName1;
    private javax.swing.JFormattedTextField officeName2;
    private javax.swing.JFormattedTextField officeProvince;
    private javax.swing.JTextField officeVendor;
    private javax.swing.JFormattedTextField officeWww;
    private javax.swing.JFormattedTextField officeZip;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JComboBox qmComboBox;
    private javax.swing.JLabel qmLabel;
    private javax.persistence.Query query;
    private javax.swing.JButton refreshButton;
    private suppliers.RowSorterToStringConverter rowSorterToStringConverter1;
    private javax.swing.JButton saveButton;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTextField supplierField;
    private javax.swing.JLabel supplierLabel;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog raport;
    private JDialog aboutBox;
    private boolean saveNeeded;
}
