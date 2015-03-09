/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bibdata;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 *
 * @author thoeyeth
 */
public class Bibdata {
    
    private static JFileChooser fc;

    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            //This is where a real application would open the file.
            String ofilename = convertToFileURL(file);
            System.out.println("Opening: " + ofilename);
            
            try {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setNamespaceAware(true);
                SAXParser saxParser = spf.newSAXParser();
                XMLReader xmlReader = saxParser.getXMLReader();
                xmlReader.setContentHandler(new RecordHandler(file));
                xmlReader.parse(ofilename);
            } catch (IOException ex) {
                Logger.getLogger(Bibdata.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(Bibdata.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(Bibdata.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("Open command cancelled by user.");
        }
    }
    
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
    




}
