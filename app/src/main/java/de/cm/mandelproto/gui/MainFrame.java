package de.cm.mandelproto.gui;

import de.cm.mandelproto.I18n;
import de.cm.mandelproto.io.FractalIO;
import de.cm.mandelproto.io.FractalSnapshot;
import de.cm.mandelproto.math.ComplexNumber;
import de.cm.mandelproto.math.RenderParameters;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
public class MainFrame extends JFrame {

    private final List<ImageFrame> imageFrames = new ArrayList<>();

    private JMenu menuFile;
    private JMenuItem itemLoadFractal;
    private JMenu menuWindows;
    private JMenu menuSettings;
    private JRadioButtonMenuItem itemLangDe;
    private JRadioButtonMenuItem itemLangEn;

    public static final RenderParameters STARTING_PARAMS = new RenderParameters(
            new ComplexNumber(-.5d, 0d), 3.84d, 2.16d, 1280, 250);

    public MainFrame() {
        super();
        createMenu();
        pack();
        setVisible(true);
        openImage(STARTING_PARAMS);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        menuFile = new JMenu();
        itemLoadFractal = new JMenuItem();
        itemLoadFractal.addActionListener(e -> loadFractalFromFile());
        menuFile.add(itemLoadFractal);
        menuBar.add(menuFile);

        menuWindows = new JMenu();
        menuBar.add(menuWindows);

        menuSettings = new JMenu();
        itemLangDe = new JRadioButtonMenuItem();
        itemLangDe.addActionListener(e -> I18n.setLocale(Locale.GERMAN));
        menuSettings.add(itemLangDe);
        itemLangEn = new JRadioButtonMenuItem();
        itemLangEn.addActionListener(e -> I18n.setLocale(Locale.ENGLISH));
        menuSettings.add(itemLangEn);
        ButtonGroup langGroup = new ButtonGroup();
        langGroup.add(itemLangDe);
        langGroup.add(itemLangEn);
        itemLangDe.setSelected(true);
        menuBar.add(menuSettings);

        I18n.addListener(this::applyTexts);
        applyTexts();
        setJMenuBar(menuBar);
    }

    private void applyTexts() {
        menuFile.setText(I18n.get("menu.file"));
        itemLoadFractal.setText(I18n.get("menu.file.loadFractal"));
        menuWindows.setText(I18n.get("menu.windows"));
        menuSettings.setText(I18n.get("menu.settings"));
        itemLangDe.setText(I18n.get("menu.settings.lang.de"));
        itemLangEn.setText(I18n.get("menu.settings.lang.en"));
        updateWindowsMenu();
    }

    public void openImage(RenderParameters params) {
        imageFrames.add(new ImageFrame("Mandelbrot " + (imageFrames.size() + 1), params, this));
        updateWindowsMenu();
    }

    private void loadFractalFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(I18n.get("dialog.loadFractal.title"));
        chooser.setFileFilter(new FileNameExtensionFilter(I18n.get("filefilter.mfrac.description"), "mfrac"));
        FractalPreviewPanel previewPanel = new FractalPreviewPanel();
        chooser.setAccessory(previewPanel);
        chooser.addPropertyChangeListener(previewPanel);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try {
            FractalSnapshot snapshot = FractalIO.load(file);
            String title = "Mandelbrot " + (imageFrames.size() + 1) + " [" + file.getName() + "]";
            imageFrames.add(new ImageFrame(title, snapshot, this));
            updateWindowsMenu();
        } catch (IOException ex) {
            log.error("Laden fehlgeschlagen", ex);
            JOptionPane.showMessageDialog(this,
                    I18n.get("error.loadFailed") + "\n" + ex.getMessage(),
                    I18n.get("error.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void onImageFrameClosing(ImageFrame frame) {
        log.debug("ImageFrame geschlossen: {}", frame.getTitle());
        imageFrames.remove(frame);
        updateWindowsMenu();
    }

    private void updateWindowsMenu() {
        menuWindows.removeAll();
        for (ImageFrame frame : imageFrames) {
            JMenuItem item = new JMenuItem(frame.getTitle());
            item.addActionListener(e -> {
                frame.setVisible(true);
                frame.toFront();
            });
            menuWindows.add(item);
        }
    }
}
