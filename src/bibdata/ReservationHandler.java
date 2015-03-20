/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bibdata;

import static bibdata.BorrowerHandler.bytesToHex;
import static bibdata.BorrowerHandler.hexArray;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author thoeyeth
 */
public class ReservationHandler extends DefaultHandler {
    
    public final static int PARSEMODE_NONE = 0;
    public final static int PARSEMODE_RECORD = 1;
    
    private JFileChooser fc;
    
    private File reservationfile;
    private BufferedWriter reservationbf;

    private ArrayList<String> reservations;

    private int parsemode;
    private int id;
    private String rid;
    private String lener;
    private String datum;
    private String locatie;
    
    private String parsekey;
    private String cnt;

    public ReservationHandler(File file) {  
        
        // hold your horses!
        this.parsemode = PARSEMODE_NONE;
        this.parsekey = "NONE";
        this.cnt = "";
        this.id = 0;
        this.reservations = new ArrayList<String>();
        
        this.fc = new JFileChooser();

        // Locate file to save records
        fc.setDialogTitle("Save reservations CSV to:");
        int returnVal = fc.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.reservationfile = fc.getSelectedFile();
            //This is where a real application would save the file.
            // System.out.println("Saving records to: " + recordfile.getAbsolutePath());
 
            try {
                FileWriter fw = new FileWriter(reservationfile);
                reservationbf = new BufferedWriter(fw);
            } catch (IOException ex) {
                System.out.println("Cannot save file");
                return;
            }  

        } else {
            System.out.println("Cannot save file");
            return;
        }
        
        // wrtite headers
        String lineout = "id;rid;datum;locatie;lener;barcode";
        
        try {
            this.reservationbf.write(lineout);
            this.reservationbf.newLine();
        } catch (IOException ex) {
            System.out.println("Could not write headers");
        }
    }
    
    public void startDocument() throws SAXException {

    }

    public void endDocument() throws SAXException {
        try {
            reservationbf.close();
        } catch (IOException ex) {
            System.out.println("Could not finnish writing to " + reservationfile.getName());
        }
    }
 
    public void startElement(String nsURI,String localName, String qName, Attributes atts)
    throws SAXException {

        // RECOGNIZE RECORD START TAG
        if (localName == "CirculationTransactions.Record") {
            this.lener = "";
            this.datum = "";
            this.locatie = "";
            this.rid = atts.getValue("Value");
            this.reservations.clear();
            this.parsemode = PARSEMODE_RECORD;
            return;
        } 
        
        if (parsemode == PARSEMODE_RECORD) {
            this.cnt = atts.getValue("Cnt");
            this.parsekey = localName;
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) {
        if (localName == "CirculationTransactions.Record") {
            id++;
            System.out.println("writing " + rid);
            this.parsemode = PARSEMODE_NONE;
            String lineout = id + ";" + rid + ";" + datum + ";" + locatie + ";" + lener + ";";
            for (String barcode : reservations) {
               
                try {
                    reservationbf.write(lineout + barcode);
                    reservationbf.newLine();
                } catch (IOException ex) {
                    System.out.println("Error writing to " + reservationfile.getName());
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

            switch (this.parsekey) {
                case "Lener":
                    this.lener = bytesToHex(xor(value.getBytes(), "harakiri".getBytes()));
                    break;
                case "Reservering-nieuwExemplaar-id":
                    this.reservations.add(value);
                    break;
                case "Reservering-nieuwIngangsdatum":
                    this.datum = value;
                    break;
                case "Reservering-nieuwAfhaallocatie":
                    this.locatie = value;
                    break;
            } 

            this.parsekey = "NONE";
        }
    }
    
    public static String bytesToHex(byte[] bytes) {
	char[] hexChars = new char[bytes.length * 2];
	for (int j = 0; j < bytes.length; j++) {
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
