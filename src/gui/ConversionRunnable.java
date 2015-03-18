/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author Thimo
 */
public class ConversionRunnable implements Runnable {

    private final XMLReader rdr;
    private final String file;
    
    public ConversionRunnable(XMLReader rdr, String file) {
        this.rdr = rdr;
        this.file = file;
    }
    
    @Override
    public void run() {
        try {
            rdr.parse(file);
        } catch (IOException ex) {
            Logger.getLogger(ConversionRunnable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(ConversionRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
