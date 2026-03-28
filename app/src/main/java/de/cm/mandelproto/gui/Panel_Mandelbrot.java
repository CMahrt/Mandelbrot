package de.cm.mandelproto.gui;

import de.cm.mandelproto.math.ComplexNumber;
import de.cm.mandelproto.math.MandelbrotPointMap;

import javax.swing.*;
import java.awt.*;

public class Panel_Mandelbrot extends JPanel{

    private final MainFrame mainFrame;

    private DoubleTextField dtf_centerReal;
    private DoubleTextField dtf_centerImag;
    private DoubleTextField dtf_width;

    public Panel_Mandelbrot(MainFrame frame){
        super(new BorderLayout());
        this.mainFrame = frame;
        createDialog();
    }

    private void createDialog() {

       add(new JLabel("Go!", JLabel.CENTER), BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new GridLayout(0,2));
        dtf_centerReal = new DoubleTextField("Center Real");
        dtf_centerImag = new DoubleTextField("Center Imag");
        dtf_width = new DoubleTextField("Size");

        centerPanel.add(dtf_centerReal);
        centerPanel.add(dtf_centerImag);
        centerPanel.add(dtf_width);

        Runnable onFieldChanged = () -> {
            try {
                mainFrame.updatePreviewRect(
                        new ComplexNumber(dtf_centerReal.getDouble(), dtf_centerImag.getDouble()),
                        16 * dtf_width.getDouble()
                );
            } catch (NumberFormatException ignored) {}
        };
        dtf_centerReal.addFocusLostListener(onFieldChanged);
        dtf_centerImag.addFocusLostListener(onFieldChanged);
        dtf_width.addFocusLostListener(onFieldChanged);

        add(centerPanel);
        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout());

        JButton okButton = getOkButton();

        panel1.add(okButton);
        add(panel1, BorderLayout.SOUTH);

    }

    private JButton getOkButton() {
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(event -> {
            MandelbrotPointMap mandelbrotPointMap =
                    new MandelbrotPointMap(
                            new ComplexNumber(dtf_centerReal.getDouble(), dtf_centerImag.getDouble()),
                            16 * (dtf_width.getDouble()),
                            9 * (dtf_width.getDouble()),
                            1280,
                            250
                    );
            mainFrame.createMandelBrotImage(mandelbrotPointMap);
        });
        return okButton;
    }

    public void init(ComplexNumber center, Double width) {
        dtf_centerReal.setDouble(center.getReal());
        dtf_centerImag.setDouble(center.getImag());
        dtf_width.setDouble(width/16);
    }
}
