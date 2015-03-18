/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bibdata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private Logger logger;
    
    private File recordfile;
    private BufferedWriter recordbf;
    private File keywordfile;
    private File authorfile;
    
    private HashMap<String, String> keywords;
    private HashMap<String, String> themes;
    private HashMap<String, Object> record;
    
    private HashMap<Integer, ArrayList> authors;
    private HashMap<Integer, ArrayList> authorlinks;
    
    private int parsemode;
    private int rid;
    private String parsekey;
    private String cnt;
    private String cid;

    public RecordHandler(File in, Logger logger) {  
        
        // hold your horses!
        this.parsemode = PARSEMODE_NONE;
        this.parsekey = "NONE";
        this.cnt = "";
        this.rid = 0;
        this.fc = new JFileChooser();
        this.logger = logger;
        
        // Locate file to save records
        fc.setDialogTitle("Save records CSV to:");
        int returnVal = fc.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.recordfile = fc.getSelectedFile();
            //This is where a real application would save the file.
            // System.out.println("Saving records to: " + recordfile.getAbsolutePath());
 
            try {
                FileWriter fw = new FileWriter(recordfile);
                recordbf = new BufferedWriter(fw);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Cannot save file");
                return;
            }
            

        } else {
            logger.log(Level.SEVERE,"Cannot save file");
            return;
        }
        
        
        // Locate file to save keywords
        fc.setDialogTitle("Save keywords CSV to:");
        returnVal = fc.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.keywordfile = fc.getSelectedFile();
            //This is where a real application would save the file.
            // System.out.println("Saving records to: " + keywordfile.getAbsolutePath());
        } else {
            logger.log(Level.SEVERE, "Cannot save keywords file");
            return;
        }
        
        // Locate file to save themes
        fc.setDialogTitle("Save authors CSV to:");
        returnVal = fc.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.authorfile = fc.getSelectedFile();
            //This is where a real application would save the file.
            // System.out.println("Saving authors to: " + authorfile.getAbsolutePath());
        } else {
            logger.log(Level.SEVERE, "Cannot save authors file");
            return;
        }
        
        this.keywords = new HashMap<String, String>();  
        this.authors = new HashMap<Integer, ArrayList>();
        this.themes = new HashMap<String, String>();
        this.record = new HashMap<String, Object>();
        
        // Write headers
        String lineout = "id;";
        for (String key : KEYS.keySet()) {
            lineout += KEYS.get(key) + ";";
        }
        
        try {
            this.recordbf.write(lineout);
            this.recordbf.newLine();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Could not write headers to " + recordfile.getName());
        }
    }
    
    public void startDocument() throws SAXException {

    }

    public void endDocument() throws SAXException {
        try {
            recordbf.close();
            logger.log(Level.INFO, "Succesfully finished writing " + this.rid + " records to " + recordfile.getName());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Could not finnish writing to " + recordfile.getName());
        }
    }
 
    public void startElement(String nsURI,String localName, String qName, Attributes atts)
    throws SAXException {

        // RECOGNIZE RECORD START TAG
        if (localName == "BibDocumentsGent.Record") {
            this.rid++;
            String id = atts.getValue("Value");
            // If this is a valid record (with a "valid" ID)
            if (localName == "BibDocumentsGent.Record" && Pattern.matches("\\d+\\^\\d+", id)) {
                
                // We are starting a new record
                this.parsemode = PARSEMODE_RECORD;
                this.cid = id;
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
            logger.log(Level.INFO,"Writing record " + record.get("id"));
            
            if (record.containsKey("type") && record.get("type").equals("Track")) {
                logger.log(Level.INFO,"Skipping record " + record.get("id") + "(because it\'s a Track)");
            }
            String lineout = record.get("id") + ";";
            for (String key : KEYS.values()) {
                if (record.containsKey(key)) {
                    lineout += record.get(key) + ";";
                } else {
                    lineout += ";";
                }
            }
            try {
                recordbf.write(lineout);
                recordbf.newLine();
            } catch (IOException ex) {
                logger.log(Level.SEVERE,"Could not write record to " + recordfile.getName());
            }
            record.clear();
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
            if (this.cnt.trim().equals("1")) {
                switch (this.parsekey) {
                    default:
                        //System.out.println("parse key" + parsekey);
                        record.put(parsekey, value);
                        break;

                } 
            }
            this.parsekey = "NONE";
        }
    }
}
