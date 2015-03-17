/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import javax.swing.JTextArea;

/**
 *
 * @author Thimo
 */
class GUILogHandler extends Handler {
    
    private JTextArea status;
    private String eol;
    
    public GUILogHandler(JTextArea status) {
        this.status = status;
        this.eol = System.getProperty("line.separator");
    }

    @Override
    public void publish(LogRecord record) {
        status.append(record.getLevel().getName() + ": " + record.getMessage() + eol);
        if (record.getThrown() != null) {
           status.append("EXCEPTION: " + record.getThrown().getMessage() + eol);
        }
    }

    @Override
    public void flush() {
        // do nothing
    }

    @Override
    public void close() throws SecurityException {
        // do nothing
    }
    
}
