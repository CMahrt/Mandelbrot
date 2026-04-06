package de.cm.mandelproto.gui;

import de.cm.mandelproto.I18n;
import de.cm.mandelproto.graphics.Palette;
import de.cm.mandelproto.graphics.PaletteLibrary;
import de.cm.mandelproto.math.ComplexNumber;
import de.cm.mandelproto.math.MandelbrotPointMap;
import de.cm.mandelproto.math.RenderParameters;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
public class MainFrame extends JFrame {

    private final List<ImageFrame> imageFrames = new ArrayList<>();

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
        createImageFramePair(STARTING_PARAMS);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

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
        menuWindows.setText(I18n.get("menu.windows"));
        menuSettings.setText(I18n.get("menu.settings"));
        itemLangDe.setText(I18n.get("menu.settings.lang.de"));
        itemLangEn.setText(I18n.get("menu.settings.lang.en"));
        updateWindowsMenu();
    }

    public void createImageFramePair(RenderParameters params) {
        int number = imageFrames.size() + 1;
        log.debug("Erstelle ImageFrame-Paar #{}: center={}, complexWidth={}, pixelWidth={}",
                number, params.center(), params.complexWidth(), params.pixelWidth());
        Palette palette = new Palette(PaletteLibrary.grayscale());
        MandelbrotPointMap map = new MandelbrotPointMap(params);
        ImageFrame imgFrame = new ImageFrame("Mandelbrot " + number, map, this, palette);
        InspectorFrame inspector = new InspectorFrame(params, palette, imgFrame);
        imgFrame.setInspector(inspector);
        imageFrames.add(imgFrame);
        updateWindowsMenu();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                long t = System.currentTimeMillis();
                map.tileIterate();
                log.info("tileIterate = {} ms", System.currentTimeMillis() - t);
                return null;
            }
            @Override
            protected void done() {
                imgFrame.drawImage();
                toFront();
            }
        }.execute();
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
