package de.cm.mandelproto.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class IntTextField extends JPanel {

    private final JTextField textField;
    public IntTextField(String label) {
        super();
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JLabel label1 = new JLabel(label + " : ");
        label1.setHorizontalAlignment(JLabel.CENTER);
        this.textField = new JTextField();
        this.textField.setHorizontalAlignment(JTextField.CENTER);
        panel.add(label1);
        panel.add(textField);
        add(panel);
    }

    public int getInt() {
        return Integer.parseInt(textField.getText());
    }

    public void setInt(int value) {
        textField.setText(Integer.toString(value));
    }

    public void setReadOnly(boolean readOnly) {
        textField.setEditable(!readOnly);
    }

    public void addFocusLostListener(Runnable callback) {
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                callback.run();
            }
        });
    }
}
