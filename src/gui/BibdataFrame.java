/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import bibdata.BorrowerHandler;
import bibdata.BorrowingsHandler;
import bibdata.CopyHandler;
import bibdata.DeletedCopyHandler;
import bibdata.RecordHandler;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author Thimo
 */
public class BibdataFrame extends javax.swing.JFrame {

    public static final String START_DIR = "c:/temp/bib";
    private JFileChooser fc;
    private Logger logger;
    
    /**
     * Creates new form BibdataFrame
     */
    public BibdataFrame() {
        initComponents();
        logger = Logger.getGlobal();
        logger.addHandler(new GUILogHandler(StatusText));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        HandlerSelect = new javax.swing.JComboBox();
        HandlerStartButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        StatusText = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        HandlerSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Borrowers", "Borrowings", "Records", "Copies", "Reservations", "DeletedCopies" }));

        HandlerStartButton.setText("Convert");
        HandlerStartButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HandlerStartButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Please select the type of file you want to convert:");

        StatusText.setColumns(20);
        StatusText.setRows(5);
        jScrollPane1.setViewportView(StatusText);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(HandlerSelect, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 270, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(HandlerStartButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .add(5, 5, 5)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(HandlerSelect, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(HandlerStartButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void HandlerStartButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HandlerStartButtonActionPerformed
        
        String selectedHandler = (String)this.HandlerSelect.getSelectedItem();
        
        fc = new JFileChooser(START_DIR);
        int returnVal = fc.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            //This is where a real application would open the file.
            String ofilename = convertToFileURL(file);
            
            
            try {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setNamespaceAware(true);
                SAXParser saxParser = spf.newSAXParser();
                XMLReader xmlReader = saxParser.getXMLReader();
                switch (selectedHandler) {
                    case "Borrowers":
                        logger.log(Level.INFO, "Loading Borrowers handler");
                        xmlReader.setContentHandler(new BorrowerHandler(file));
                        break;
                    case "Borrowings":
                        logger.log(Level.INFO, "Loading Borrowings handler");
                        xmlReader.setContentHandler(new BorrowingsHandler(file));
                        break;
                    case "Records":
                        logger.log(Level.INFO, "Loading Records handler");
                        xmlReader.setContentHandler(new RecordHandler(file, logger));
                        break;
                    case "Copies":
                        logger.log(Level.INFO, "Loading Copies handler");
                        xmlReader.setContentHandler(new CopyHandler(file));
                        break;
                    case "Reservations":
                        logger.log(Level.INFO, "Loading Copies handler");
                        xmlReader.setContentHandler(new CopyHandler(file));
                        break;
                    case "DeletedCopies":
                        logger.log(Level.INFO, "Loading Copies handler");
                        xmlReader.setContentHandler(new DeletedCopyHandler(file));
                        break;
                }
                xmlReader.parse(ofilename);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Cannot parse " + ofilename + " due to I/O", ex);
            } catch (SAXException ex) {
                logger.log(Level.SEVERE, "Cannot parse " + ofilename + " due to Malformed XML contents", ex);
            } catch (ParserConfigurationException ex) {
                logger.log(Level.SEVERE, "Cannot parse " + ofilename + " due to a configuration error", ex);
            }
        } else {
            logger.log(Level.INFO, "User cancelled file open");
        }
    }//GEN-LAST:event_HandlerStartButtonActionPerformed

    private static String convertToFileURL(File file) {
        String path = file.getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }
    
    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox HandlerSelect;
    private javax.swing.JButton HandlerStartButton;
    private javax.swing.JTextArea StatusText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
