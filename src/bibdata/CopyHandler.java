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
public class CopyHandler extends DefaultHandler {

    public final static int PARSEMODE_NONE = 0;
    public final static int PARSEMODE_RECORD = 1;
    
    public static final Map<String, String> KEYS;

    static {
        Map<String, String> map = new HashMap<String, String>();
        map.put("Barcode", "id");
        map.put("PK", "PK");
        map.put("Datuminvoer", "datuminvoer");
        map.put("Aard", "aard");
        map.put("Prijs", "prijs");
        map.put("BBnr", "BBnr");
        KEYS = Collections.unmodifiableMap(map);
    }
    
    private JFileChooser fc;
    
    private File copyfile;
    private BufferedWriter copybf;

    private HashMap<String, Object> record;

    private int parsemode;
    private int rid;
    private String parsekey;
    private String cnt;
    private String cid;

    public CopyHandler(File file) {  
        
        // hold your horses!
        this.parsemode = PARSEMODE_NONE;
        this.parsekey = "NONE";
        this.cnt = "";
        this.rid = 0;
        this.fc = new JFileChooser();
        this.record = new HashMap<String, Object>();
        // Locate file to save records
        fc.setDialogTitle("Save records CSV to:");
        int returnVal = fc.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.copyfile = fc.getSelectedFile();
            //This is where a real application would save the file.
            // System.out.println("Saving records to: " + recordfile.getAbsolutePath());
 
            try {
                FileWriter fw = new FileWriter(copyfile);
                copybf = new BufferedWriter(fw);
            } catch (IOException ex) {
                System.out.println("Cannot save file");
                return;
            }
            

        } else {
            System.out.println("Cannot save file");
            return;
        }
        
        
        String lineout = "";
        for (String key : KEYS.keySet()) {
            lineout += KEYS.get(key) + ";";
        }
        
        try {
            this.copybf.write(lineout);
            this.copybf.newLine();
        } catch (IOException ex) {
            System.out.println("Could not write headers");
        }
    }
    
    public void startDocument() throws SAXException {

    }

    public void endDocument() throws SAXException {
        try {
            copybf.close();
        } catch (IOException ex) {
            System.out.println("Could not finnish writing to " + copyfile.getName());
        }
    }
 
    public void startElement(String nsURI,String localName, String qName, Attributes atts)
    throws SAXException {

        // RECOGNIZE RECORD START TAG
        if (localName == "Barcodes.Record") {
            this.rid++;
            String id = atts.getValue("Value");
            // If this is a valid record (with a "valid" ID)
            //if (localName == "Barcodes.Record") {
                
                // We are starting a new record
                this.parsemode = PARSEMODE_RECORD;
                this.cid = id;
                record.put("id", id);
                return;
            //}
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
        if (localName == "Barcodes.Record") {
            System.out.println("writing " + record.get("id"));
            this.parsemode = PARSEMODE_NONE;
            String lineout = record.get("id") + ";";
            for (String key : KEYS.values()) {
                if (record.containsKey(key)) {
                    lineout += record.get(key) + ";";
                } else {
                    lineout += ";";
                }
            }
            try {
                copybf.write(lineout);
                copybf.newLine();
            } catch (IOException ex) {
                System.out.println("Error writing to " + copyfile.getName());
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

            switch (this.parsekey) {
                default:
                    //System.out.println("parse key" + parsekey);
                    record.put(parsekey, value);
                    break;

            } 

            this.parsekey = "NONE";
        }
    }
}
