/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bibdata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author thoeyeth
 */
public class BorrowerHandler extends DefaultHandler {

    public final static int PARSEMODE_NONE = 0;
    public final static int PARSEMODE_RECORD = 1;

    private int parsemode;
    private int bid;

    private JFileChooser fc;
    private String cnt;
    private String parsekey;
    private File borrowersfile;
    private HashMap<String, Object> record;

    private MessageDigest md;

    private Pattern streetpattern = Pattern.compile("^(.+)\\s\\d+$");

    private HashMap<String, String> streets;
    private BufferedWriter borrowerbf;

    public BorrowerHandler(File file) {
	this.bid = 0;
	this.record = new HashMap<String, Object>();
	this.parsekey = "NONE";
	try {
	    this.md = MessageDigest.getInstance("SHA-256");
	} catch (NoSuchAlgorithmException ex) {
	    return;
	}
	// read streets
	fc = new JFileChooser("c:/temp");
	int returnVal = fc.showOpenDialog(null);

	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    File streetfile = fc.getSelectedFile();
	    try {
		this.streets = getStreets(streetfile);
	    } catch (FileNotFoundException ex) {
		System.out.println("Cannot read streets");
		return;
	    } catch (IOException ex) {
		System.out.println("Cannot read streets");
		return;
	    }
	} else {
	    return;
	}

	// hold your horses!
	this.parsemode = PARSEMODE_NONE;
	this.fc = new JFileChooser("c:/temp");
	// Locate file to save records
	fc.setDialogTitle("Save borrowers CSV to:");
	int rv = fc.showSaveDialog(null);
	if (rv == JFileChooser.APPROVE_OPTION) {
	    this.borrowersfile = fc.getSelectedFile();
	    // This is where a real application would save the file.
	    try {
		FileWriter fw = new FileWriter(borrowersfile);
		borrowerbf = new BufferedWriter(fw);
		borrowerbf
			.write("id;bbnr;geboortejaar;geslacht;statistischesector;postcodestart;inschrijvingsjaar;ingeschrijvinglocatie;lenerscategorie;");
		borrowerbf.newLine();
	    } catch (IOException ex) {
		System.out.println("Cannot save file");
		return;
	    }
	    System.out.println("Saving records to: " + borrowersfile.getAbsolutePath());
	} else {
	    System.out.println("Cannot save file");
	    return;
	}
	System.out.println("Ended conversion");
    }

    public void startDocument() throws SAXException {

    }

    public void endDocument() throws SAXException {

    }

    public void startElement(String nsURI, String localName, String qName, Attributes atts) throws SAXException {

	// RECOGNIZE RECORD START TAG
	if (localName == "Borrowers.Record") {

	    String id = atts.getValue("Value");
	    // If this is a valid record (with a "valid" ID)
	    if (localName == "Borrowers.Record") {
		// We are starting a new record
		bid++;
		this.parsemode = PARSEMODE_RECORD;
		record.clear();
		record.put("id", bid);
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
	if (localName == "Borrowers.Record") {
	    this.parsemode = PARSEMODE_NONE;
	    // only log certain categories
	    if (getAnomizedCategory() != null) {
		String lineout = "";
		lineout += record.get("id") + ";";
		lineout += record.get("code") + ";";
		lineout += getAnomizedGeboortejaar() + ";";
		lineout += getAnomizedGender() + ";";
		lineout += record.get("sector") + ";";
		lineout += getAnomizedPostcode() + ";";
		lineout += record.get("inschrijving_datum") + ";";
		lineout += record.get("inschrijving_locatie") + ";";
		lineout += getAnomizedCategory();
		try {
		    borrowerbf.write(lineout);
		    borrowerbf.newLine();
		} catch (IOException e) {
		    System.out.println("Dough!");
		    e.printStackTrace();
		}
	    }
	}
    }

    /**
     * Returns certain category of null
     * 
     * @return
     */
    private Object getAnomizedCategory() {
	String categorie = (String) record.get("categorie");
	if (categorie != null) {
	    switch (categorie) {
	    case "AD":
	    case "CO":
	    case "FI":
	    case "IBL":
	    case "KI":
	    case "KL":
	    case "VO":
		break;
	    default:
		categorie = null;
		break;
	    }
	}

	return categorie;
    }

    /**
     * Returns the decade
     * 
     * @return decade
     */
    private String getAnomizedGeboortejaar() {
	String geboortejaar = (String) record.get("geboortejaar");
	String decade = null;
	if (geboortejaar != null && geboortejaar.length() > 3) {
	    // top off really old people
	    if (geboortejaar.startsWith("190") || geboortejaar.startsWith("191") || geboortejaar.startsWith("192")
		    || geboortejaar.startsWith("193") || geboortejaar.startsWith("194")) {
		decade = "1940";
	    } else {
		decade = geboortejaar.substring(0, 3) + "0";
	    }
	}
	return decade;
    }

    /**
     * Returns anomized gender
     * 
     * @return
     */
    private String getAnomizedGender() {
	String gender = (String) record.get("geslacht");
	if (gender == null) {
	    gender = "M";
	}
	switch (gender) {
	case "Vrouwelijk":
	    gender = "V";
	    break;
	case "Mannelijk":
	default:
	    gender = "M";
	    break;
	}
	return gender;
    }

    /**
     * Take postcode's first digit
     * 
     * @return
     */
    private String getAnomizedPostcode() {
	String postcode = (String) record.get("postcode");
	if (postcode != null && postcode.length() > 0) {
	    postcode = postcode.substring(0, 1);
	}
	return postcode;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

	String value = new String(ch, start, length);

	// CURRENTLY PARSING A RECORD, IDENTIFY RECORD PROPERTIES
	if (parsemode == PARSEMODE_RECORD && parsekey != "NONE") {
	    // do nothing

	    switch (this.parsekey) {
	    case "IdentiteitGeboortedatum":
		String birthdate[] = value.split("/");
		record.put("geboortejaar", birthdate[2]);
		break;
	    case "IdentiteitGeslacht":
		record.put("geslacht", value);
		break;
	    case "ContributieInschrijfdatum":
		String yearsubscribed[] = value.split("/");
		record.put("inschrijving_datum", yearsubscribed[2]);
		break;
	    case "HuisadresStraathuisnr.":
		String sector = "UNKNOWN";

		Matcher m = this.streetpattern.matcher(value);
		if (m.find()) {
		    String street = m.group(1).toLowerCase();
		    if (streets.containsKey(street)) {
			sector = streets.get(street);
		    }
		}
		record.put("sector", sector);
		break;
	    case "HuisadresPostcode":
		record.put("postcode", value);
		break;
	    case "ContributieLenerscategorie":
		record.put("categorie", value);
		break;
	    case "Locatieinschrijving":
		record.put("inschrijving_locatie", value);
		break;
	    case "Originelelenersbarcode":
		/*
		 * md.update(value.getBytes("UTF-8")); // Change this to "UTF-16" if needed byte[] hash = md.digest();
		 * record.put("code", DatatypeConverter.printBase64Binary(hash));
		 */
		record.put("code", bytesToHex(xor(value.getBytes(), "karahiri".getBytes())));
		break;

	    default:
		// record.put(parsekey, value);
		break;

	    }
	    this.parsekey = "NONE";
	}
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

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

    public HashMap<String, String> getStreets(File file) throws FileNotFoundException, IOException {

	// Expected structure:
	// "postcode";"straatcode";"straatnaam";"onpaar_van";"onpaar_tot";"paar_van";"paar_tot";"sector";"stadsdeel";"wijkNr";"wijknaam"
	BufferedReader br = new BufferedReader(new FileReader(file));
	String line = null;
	HashMap<String, String> streets = new HashMap<String, String>();

	while ((line = br.readLine()) != null) {

	    /*
	     * @TODO: Compare van-tot for even and odd
	     * 
	     * HashMap<String,String> cs = new HashMap<String, String>(); String str[] = line.split(";");
	     * cs.put("postcode", str[0].replaceAll("^\"|\"$", "")); cs.put("straatnaam", str[2].replaceAll("^\"|\"$",
	     * "")); cs.put("onpaar_van", str[3].replaceAll("^\"|\"$", "")); cs.put("onpaar_tot",
	     * str[4].replaceAll("^\"|\"$", "")); ...
	     */

	    String str[] = line.split(";");
	    streets.put(str[2].replaceAll("^\"|\"$", "").toLowerCase(), str[7].replaceAll("^\"|\"$", ""));
	    // System.out.println(str[2].replaceAll("^\"|\"$", "").toLowerCase() + "  " + str[7].replaceAll("^\"|\"$",
	    // ""));
	}

	return streets;

    }
}
