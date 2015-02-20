/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bibdata;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
    
    public static final Map<String, String> KEYS;

    static {
        Map<String, String> map = new HashMap<String, String>();
        map.put("_200a", "title");
        map.put("_200b", "type");
        map.put("_101a", "language");
        map.put("_2104", "publisher");
        map.put("_210d", "year_published");
        map.put("_215a", "pages");
        map.put("_010a", "ISBN");
        map.put("_700a", "author_lastname");
        map.put("_700b", "author_firstname");
        map.put("_7004", "author_type");
        map.put("_015a", "EAN");
        map.put("_900t", "series_title");
        map.put("_205a", "series_edition");
        map.put("_631a", "keyword_adults");
        map.put("_105a", "literarytype");
        map.put("_637a", "category_adults");
        map.put("_633a", "keyword_youth");
        map.put("_636a", "category_youth");
        map.put("_6924", "age");
        map.put("_690a", "SISO");
        map.put("_696a", "ZIZO");
        map.put("_011a", "ISSN");
        map.put("_635a", "keywords_libraries");
        map.put("_694a", "SISO_libraries");
        map.put("_015z", "EAN_wrong");
        map.put("_634a", "category_music");
        map.put("_639a", "keywords_local");
        map.put("_010z", "ISBN_wrong");
        map.put("_632a", "keywords_youth");
        map.put("_6914", "AVI");
        map.put("_011z", "ISSN_wrong");
        KEYS = Collections.unmodifiableMap(map);
    }
    
    private JFileChooser fc;
    
    private File recordfile;
    private File keywordfile;
    private File authorfile;
    
    private HashMap<String, String> keywords;
    private HashMap<String, String> themes;
    private HashMap<String, String> record;
    
    private int parsemode;
    private String parsekey;
    private String cnt;

    public RecordHandler(File file) {  
        
        // hold your horses!
        this.parsemode = PARSEMODE_NONE;
        this.parsekey = "NONE";
        this.cnt = "";
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
        fc.setDialogTitle("Save authors CSV to:");
        returnVal = fc.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.authorfile = fc.getSelectedFile();
            //This is where a real application would save the file.
            System.out.println("Saving authors to: " + authorfile.getAbsolutePath());
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
 
    public void startElement(String nsURI,String localName, String qName, Attributes atts)
    throws SAXException {

        // RECOGNIZE RECORD START TAG
        if (localName == "BibDocumentsGent.Record") {
            
            String id = atts.getValue("Value");
            // If this is a valid record (with a "valid" ID)
            if (localName == "BibDocumentsGent.Record" && Pattern.matches("\\d+\\^\\d+", id)) {
                // We are starting a new record
                this.parsemode = PARSEMODE_RECORD;
                record.clear();
                record.put("id", id);
                return;
            }
        } 
        
        if (parsemode == PARSEMODE_RECORD) {
            if (KEYS.keySet().contains(localName)) {
                parsekey = KEYS.get(localName);
                this.cnt = atts.getValue("Cnt");
            }
        }
        
        
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) {
        if (localName == "BibDocumentsGent.Record") {
            this.parsemode = PARSEMODE_NONE;
            System.out.println("Finishing record: " + record.get("title"));
        }
    }
    
    @Override
    public void characters(char[] ch,
                       int start,
                       int length)
                throws SAXException {
        
        String value = new String(ch, start, length); 
        // CURRENTLY PARSING A RECORD, IDENTIFY RECORD PROPERTIES
        if (parsemode == PARSEMODE_RECORD && parsekey != "NONE") {
            
            switch (this.parsekey) {
                case "author":
                    // Do some crazy stuff
                    System.out.println("Adding author: " + value);
                    break;
                default:
                    record.put(parsekey, value);
                    break;
                      
            } 
            this.parsekey = "NONE";
        }
    }
}
