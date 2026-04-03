package de.cm.mandelproto.gui;

import de.cm.mandelproto.graphics.Palette;
import de.cm.mandelproto.graphics.PaletteLibrary;
import de.cm.mandelproto.math.ComplexNumber;
import de.cm.mandelproto.math.MandelbrotPointMap;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

public class Panel_Mandelbrot extends JPanel {

    private final MainFrame mainFrame;
    private final int screenWidth;
    private final int screenHeight;
    @Getter
    private final Palette palette = new Palette(PaletteLibrary.grayscale());

    private DoubleTextField tf_centerReal;
    private DoubleTextField tf_centerImag;
    private DoubleTextField tf_complexWidth;
    private DoubleTextField tf_complexHeight;
    private IntTextField tf_pixelWidth;
    private IntTextField tf_pixelHeight;
    private IntTextField tf_maxIterations;
    private JComboBox<String> cb_palette;

    public Panel_Mandelbrot(MainFrame frame) {
        super(new BorderLayout());
        this.mainFrame = frame;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        this.screenWidth  = screen.width;
        this.screenHeight = screen.height;
        createDialog();
    }

    private void createDialog() {

        add(new JLabel("Go!", JLabel.CENTER), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(0, 2));
        tf_centerReal   = new DoubleTextField("Center Real");
        tf_centerImag   = new DoubleTextField("Center Imag");
        tf_complexWidth = new DoubleTextField("Width");
        tf_complexHeight= new DoubleTextField("Height");
        tf_pixelWidth   = new IntTextField("Width [px]");
        tf_pixelWidth.setInt(1280);
        tf_pixelHeight  = new IntTextField("Height [px]");
        tf_pixelHeight.setReadOnly(true);

        inputPanel.add(tf_centerReal);
        inputPanel.add(tf_centerImag);
        inputPanel.add(tf_complexWidth);
        inputPanel.add(tf_complexHeight);
        tf_maxIterations = new IntTextField("Max. Iterationen");
        tf_maxIterations.setInt(150);

        cb_palette = new JComboBox<>(PaletteLibrary.NAMES);
        cb_palette.addActionListener(e -> palette.loadColors(PaletteLibrary.byName(selectedPaletteName())));
        inputPanel.add(tf_pixelWidth);
        inputPanel.add(tf_pixelHeight);
        inputPanel.add(tf_maxIterations);
        inputPanel.add(new JLabel(""));
        inputPanel.add(new JLabel("Palette"));
        inputPanel.add(cb_palette);

        Runnable onFieldChanged = () -> {
            try {
                updatePixelDimensions();
                mainFrame.updatePreviewRect(
                        new ComplexNumber(tf_centerReal.getDouble(), tf_centerImag.getDouble()),
                        tf_complexWidth.getDouble(),
                        tf_complexHeight.getDouble()
                );
            } catch (NumberFormatException ignored) {}
        };
        tf_centerReal.addFocusLostListener(onFieldChanged);
        tf_centerImag.addFocusLostListener(onFieldChanged);
        tf_complexWidth.addFocusLostListener(onFieldChanged);
        tf_complexHeight.addFocusLostListener(onFieldChanged);
        tf_pixelWidth.addFocusLostListener(onFieldChanged);

        add(inputPanel);
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(getBtn_ok());
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void triggerRender() {
        MandelbrotPointMap mandelbrotPointMap =
                new MandelbrotPointMap(
                        new ComplexNumber(tf_centerReal.getDouble(), tf_centerImag.getDouble()),
                        tf_complexWidth.getDouble(),
                        tf_complexHeight.getDouble(),
                        tf_pixelWidth.getInt(),
                        tf_maxIterations.getInt()
                );
        mainFrame.createMandelBrotImage(mandelbrotPointMap);
    }

    private JButton getBtn_ok() {
        JButton btn_ok = new JButton("Ok");
        btn_ok.addActionListener(event -> triggerRender());
        return btn_ok;
    }

    public void init(ComplexNumber center, double complexWidth, double complexHeight) {
        tf_centerReal.setDouble(center.getReal());
        tf_centerImag.setDouble(center.getImag());
        tf_complexWidth.setDouble(complexWidth);
        tf_complexHeight.setDouble(complexHeight);
        updatePixelDimensions();
    }

    private void updatePixelDimensions() {
        double complexWidth  = tf_complexWidth.getDouble();
        double complexHeight = tf_complexHeight.getDouble();
        if (complexWidth <= 0 || complexHeight <= 0) return;
        int pixelWidth = tf_pixelWidth.getInt();
        if (pixelWidth <= 0) pixelWidth = 1280;
        pixelWidth = Math.min(pixelWidth, screenWidth);
        int pixelHeight = (int) ((complexHeight / complexWidth) * pixelWidth);
        if (pixelHeight > screenHeight) {
            pixelHeight = screenHeight;
            pixelWidth  = (int) ((complexWidth / complexHeight) * pixelHeight);
        }
        tf_pixelWidth.setInt(pixelWidth);
        tf_pixelHeight.setInt(pixelHeight);
        tf_maxIterations.setInt(suggestMaxIterations(complexWidth));
    }

    private String selectedPaletteName() {
        Object selected = cb_palette.getSelectedItem();
        return selected != null ? (String) selected : PaletteLibrary.NAMES[0];
    }

    private static int suggestMaxIterations(double complexWidth) {
        return Math.max(100, (int) (150 * Math.log10(38.4 / complexWidth)));
    }
}
