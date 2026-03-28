package de.cm.mandelproto.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class DoubleTextField extends JPanel {

    private final JTextField textField;

    public DoubleTextField(String label) {
        super();
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JLabel label1 = new JLabel(label + " : ");
        label1.setHorizontalAlignment(JLabel.CENTER);
        this.textField = new JTextField();
        this.textField.setHorizontalAlignment(JTextField.CENTER);
        panel.add(label1);
        panel.add(textField);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {
            }
        });
        add(panel);
    }

    public Double getDouble() {
        return Double.parseDouble(textField.getText());
    }

    public void setDouble(double x) {
        textField.setText(Double.toString(x));
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
