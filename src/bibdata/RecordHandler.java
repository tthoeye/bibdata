/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bibdata;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author thoeyeth
 */
public class RecordHandler extends DefaultHandler {

    public final static int PARSEMODE_NONE = 0;
    public final static int PARSEMODE_RECORD = 1;
    public final static int PARSEMODE_ATRRIBUTES = 2;
    
    private JFileChooser fc;
    
    private File recordfile;
    private File keywordfile;
    private File themefile;
    
    private HashMap<String, String> keywords;
    private HashMap<String, String> themes;
    private HashMap<String, String> record;
    
    private int parsemode;
    
    public RecordHandler(File file) {  
        
        // hold your horses!
        this.parsemode = PARSEMODE_NONE;
        this.fc = new JFileChooser();
        // Locate file to save records
        fc.setDialogTitle("Save records CSV to:");
        int returnVal = fc.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.recordfile = fc.getSelectedFile();
            //This is where a real application would save the file.
            System.out.println("Saving records to: " + recordfile.getAbsolutePath());
        } else {
            System.out.println("Cannot save file");
            return;
        }
        
        // Locate file to save keywords
        fc.setDialogTitle("Save keywords CSV to:");
        returnVal = fc.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.keywordfile = fc.getSelectedFile();
            //This is where a real application would save the file.
            System.out.println("Saving records to: " + keywordfile.getAbsolutePath());
        } else {
            System.out.println("Cannot save file");
            return;
        }
        
        // Locate file to save themes
        fc.setDialogTitle("Save themes CSV to:");
        returnVal = fc.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.themefile = fc.getSelectedFile();
            //This is where a real application would save the file.
            System.out.println("Saving themes to: " + themefile.getAbsolutePath());
        } else {
            System.out.println("Cannot save file");
            return;
        }
        
        this.keywords = new HashMap<String, String>();
        this.themes = new HashMap<String, String>();
        this.record = new HashMap<String, String>();
    }
    
    public void startDocument() throws SAXException {

    }

    public void endDocument() throws SAXException {
   
    }
 
    public void startElement(String namespaceURI,
                         String localName,
                         String qName, 
                         Attributes atts)
    throws SAXException {

        if (localName == "BibDocumentsGent.Record") {
            
            String id = atts.getValue("Value");
            // If this is a valid record (with a "valid" ID)
            if (localName == "BibDocumentsGent.Record" && Pattern.matches("\\d+\\^\\d+", id)) {
                // We are starting a new record
                this.parsemode = PARSEMODE_RECORD;
                record.clear();
                record.put("id", id);
            }
        } else if (parsemode == PARSEMODE_RECORD) {
            // This is a child tag of a record
            
        }
    }
    
    public void endElement(String uri, 
                        String localName, 
                        String qName) {
        if (localName == "BibDocumentsGent.Record") {
            this.parsemode = PARSEMODE_NONE;
            System.out.println("Printing record: " + record.get("id"));
        }
    }
}
