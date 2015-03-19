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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.swing.JFileChooser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author thoeyeth
 */
public class BorrowingsHandler extends DefaultHandler {
     
    public final static int PARSEMODE_NONE = 0;
    public final static int PARSEMODE_RECORD = 1;
    
    private int parsemode;
    private int bid;
    
    private JFileChooser fc;
    private String cnt;
    private String parsekey;
    private File borrowingsfile;
    private BufferedWriter borrowingsbf;
    private HashMap<String, HashMap> record;
    
    private MessageDigest md;
    private String borrower;
    private String date;
            
    public BorrowingsHandler(File file) {  
        this.bid = 0;
        this.record = new HashMap<String, HashMap>();
        this.parsekey = "NONE";
        try {
            this.md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            return;
        }
            
        // hold your horses!
        this.parsemode = PARSEMODE_NONE;
        this.fc = new JFileChooser();
        // Locate file to save records
        fc.setDialogTitle("Save borrowers CSV to:");
        int rv = fc.showSaveDialog(null);
        if (rv == JFileChooser.APPROVE_OPTION) {
            this.borrowingsfile = fc.getSelectedFile();
            try {
                FileWriter fw = new FileWriter(borrowingsfile);
                borrowingsbf = new BufferedWriter(fw);
                borrowingsbf.write("bid;datumvan;lener;barcode;ontleentermijn");
                borrowingsbf.newLine();
            } catch (IOException ex) {
                System.out.println("Cannot save file");
                return;
            }
            //This is where a real application would save the file.
            System.out.println("Saving records to: " + borrowingsfile.getAbsolutePath());
        } else {
            System.out.println("Cannot save file");
            return;
        } 
    }
    
     public void startDocument() throws SAXException {

    }

    public void endDocument() throws SAXException {
   
    }
 
    public void startElement(String nsURI,String localName, String qName, Attributes atts)
    throws SAXException {

        // RECOGNIZE RECORD START TAG
        if (localName == "CirculationTransactions.Record") {
            
            String id = atts.getValue("Value");
            // If this is a valid record (with a "valid" ID)
            if (localName == "CirculationTransactions.Record") {
                // We are starting a new record
                this.parsemode = PARSEMODE_RECORD;
                this.borrower = "";
                this.date = "";
                this.record.clear();
                return;
            }
        }
        
        if (this.parsemode == PARSEMODE_RECORD) {
            this.cnt = atts.getValue("Cnt");
            this.parsekey = localName;
        }
        
        
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) {
        if (localName == "CirculationTransactions.Record") {
            String lineout = "";
            this.parsemode = PARSEMODE_NONE;
            for (HashMap borrowings : record.values()) {
                try {
                    bid++;
                    lineout = bid + ";" + date + ";" + borrower + ";" + borrowings.get("barcode") + ";" + borrowings.get("uitleentermijn");
                    borrowingsbf.write(lineout);
                    borrowingsbf.newLine();
                } catch (IOException ex) {
                    System.out.println("Skipped record");
                }
            }
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
            
            if (!record.containsKey(cnt)) {
                record.put(cnt, new HashMap<String, String>());
            }
            
            switch (this.parsekey) {
                case "Lener":
                    /*
                    md.update(value.getBytes("UTF-8")); // Change this to "UTF-16" if needed
                    byte[] hash = md.digest();
                    record.put("code", DatatypeConverter.printBase64Binary(hash));
                    */
                    this.borrower = bytesToHex(xor(value.getBytes(), "karahiri".getBytes()));
                    break;
                case "VanDatum":
                    this.date = value;
                    break;
                case "Uitleentermijn":
                    record.get(cnt).put("uitleentermijn", value);
                    break;
                case "Originelebarcode":
                    record.get(cnt).put("barcode", value);
                    break;
                default:
                    //record.put(parsekey, value);
                    break;
                    
            }
            this.parsekey = "NONE";
        }
    }
    
    
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static byte[] xor(final byte[] input, final byte[] secret) {
        final byte[] output = new byte[input.length];
        if (secret.length == 0) {
            throw new IllegalArgumentException("empty security key");
        }
        int spos = 0;
        for (int pos = 0; pos < input.length; ++pos) {
            output[pos] = (byte) (input[pos] ^ secret[spos]);
            ++spos;
            if (spos >= secret.length) {
                spos = 0;
            }
        }
        return output;
    }
}
