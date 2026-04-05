package de.cm.mandelproto.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class DoubleTextField extends JPanel {

    private final JTextField textField;
    private final JLabel label1;

    public DoubleTextField(String label) {
        super();
        JPanel panel = new JPanel(new GridLayout(0, 2));
        label1 = new JLabel(label + " : ");
        label1.setHorizontalAlignment(JLabel.CENTER);
        this.textField = new JTextField();
        this.textField.setHorizontalAlignment(JTextField.CENTER);
        panel.add(label1);
        panel.add(textField);
        add(panel);
    }

    public Double getDouble() {
        return Double.parseDouble(textField.getText());
    }

    public void setDouble(double value) {
        textField.setText(Double.toString(value));
    }

    public void setLabel(String text) {
        label1.setText(text + " : ");
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
