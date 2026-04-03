package de.cm.mandelproto.gui;

import de.cm.mandelproto.math.ComplexNumber;
import de.cm.mandelproto.math.MandelbrotPointMap;

import javax.swing.*;
import java.awt.*;

public class Panel_Mandelbrot extends JPanel{

    private final MainFrame mainFrame;
    private final int screenWidth;
    private final int screenHeight;

    private DoubleTextField dtf_centerReal;
    private DoubleTextField dtf_centerImag;
    private DoubleTextField dtf_width;
    private DoubleTextField dtf_height;
    private IntTextField itf_pixelWidth;
    private IntTextField itf_pixelHeight;

    public Panel_Mandelbrot(MainFrame frame){
        super(new BorderLayout());
        this.mainFrame = frame;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        this.screenWidth  = screen.width;
        this.screenHeight = screen.height;
        createDialog();
    }

    private void createDialog() {

       add(new JLabel("Go!", JLabel.CENTER), BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new GridLayout(0,2));
        dtf_centerReal  = new DoubleTextField("Center Real");
        dtf_centerImag  = new DoubleTextField("Center Imag");
        dtf_width       = new DoubleTextField("Width");
        dtf_height      = new DoubleTextField("Height");
        itf_pixelWidth  = new IntTextField("Width [px]");
        itf_pixelWidth.setInt(1280);
        itf_pixelHeight = new IntTextField("Height [px]");
        itf_pixelHeight.setReadOnly(true);

        centerPanel.add(dtf_centerReal);
        centerPanel.add(dtf_centerImag);
        centerPanel.add(dtf_width);
        centerPanel.add(dtf_height);
        centerPanel.add(itf_pixelWidth);
        centerPanel.add(itf_pixelHeight);

        Runnable onFieldChanged = () -> {
            try {
                updatePixelDimensions();
                mainFrame.updatePreviewRect(
                        new ComplexNumber(dtf_centerReal.getDouble(), dtf_centerImag.getDouble()),
                        dtf_width.getDouble(),
                        dtf_height.getDouble()
                );
            } catch (NumberFormatException ignored) {}
        };
        dtf_centerReal.addFocusLostListener(onFieldChanged);
        dtf_centerImag.addFocusLostListener(onFieldChanged);
        dtf_width.addFocusLostListener(onFieldChanged);
        dtf_height.addFocusLostListener(onFieldChanged);
        itf_pixelWidth.addFocusLostListener(onFieldChanged);

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
                            dtf_width.getDouble(),
                            dtf_height.getDouble(),
                            itf_pixelWidth.getInt(),
                            250
                    );
            mainFrame.createMandelBrotImage(mandelbrotPointMap);
        });
        return okButton;
    }

    public void init(ComplexNumber center, double width, double height) {
        dtf_centerReal.setDouble(center.getReal());
        dtf_centerImag.setDouble(center.getImag());
        dtf_width.setDouble(width);
        dtf_height.setDouble(height);
        updatePixelDimensions();
    }

    private void updatePixelDimensions() {
        double w  = dtf_width.getDouble();
        double h  = dtf_height.getDouble();
        int pw    = Math.min(itf_pixelWidth.getInt(), screenWidth);
        int ph    = (int) ((h / w) * pw);
        if (ph > screenHeight) {
            ph = screenHeight;
            pw = (int) ((w / h) * ph);
        }
        itf_pixelWidth.setInt(pw);
        itf_pixelHeight.setInt(ph);
    }
}
