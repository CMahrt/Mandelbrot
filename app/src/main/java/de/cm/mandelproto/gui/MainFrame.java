package de.cm.mandelproto.gui;

import de.cm.mandelproto.I18n;
import de.cm.mandelproto.math.ComplexNumber;
import de.cm.mandelproto.math.MandelbrotPointMap;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

@Slf4j
public class MainFrame extends JFrame implements ActionListener {

    public static final String CREATE_MANDELBROT_TREE = "Create Mandelbrot Tree";

    private ImageFrame currentImageFrame;
    private RunningColorsFrame runningColorsFrame;

    private final Panel_Mandelbrot panelMandelbrot;

    private JMenu menuCreate;
    private JMenuItem itemCreateMandelbrot;
    private JMenu menuView;
    private JMenuItem itemRunningColors;
    private JMenu menuSettings;
    private JRadioButtonMenuItem itemLangDe;
    private JRadioButtonMenuItem itemLangEn;

    public final static MandelbrotPointMap STARTINGMAP =
            new MandelbrotPointMap(
                    new ComplexNumber(-.5d, 0d),
                    3.84d,
                    2.16d,
                    1280,
                    250
            );

    public MainFrame() {
        super();
        createMenu();
        setSize(480, 240);
        //setAlwaysOnTop(true);
        panelMandelbrot = new Panel_Mandelbrot(this);
        getContentPane().add(panelMandelbrot);
        I18n.addListener(this::applyTexts);
        setVisible(true);
        createMandelBrotImage(STARTINGMAP);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setVisible(true);

        menuCreate = new JMenu();
        menuBar.add(menuCreate);

        itemCreateMandelbrot = new JMenuItem();
        itemCreateMandelbrot.setActionCommand(CREATE_MANDELBROT_TREE);
        itemCreateMandelbrot.addActionListener(this);
        menuCreate.add(itemCreateMandelbrot);

        menuView = new JMenu();
        menuBar.add(menuView);

        itemRunningColors = new JMenuItem();
        itemRunningColors.addActionListener(e -> {
            if (runningColorsFrame == null || !runningColorsFrame.isDisplayable()) {
                runningColorsFrame = new RunningColorsFrame(panelMandelbrot.getPalette());
            }
            runningColorsFrame.setVisible(true);
        });
        menuView.add(itemRunningColors);

        menuSettings = new JMenu();
        menuBar.add(menuSettings);

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

        applyTexts();
        setJMenuBar(menuBar);
    }

    private void applyTexts() {
        menuCreate.setText(I18n.get("menu.create"));
        itemCreateMandelbrot.setText(I18n.get("menu.create.mandelbrot"));
        menuView.setText(I18n.get("menu.view"));
        itemRunningColors.setText(I18n.get("menu.view.runningColors"));
        menuSettings.setText(I18n.get("menu.settings"));
        itemLangDe.setText(I18n.get("menu.settings.lang.de"));
        itemLangEn.setText(I18n.get("menu.settings.lang.en"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        log.debug("start actionPerformed : Thread {}", Thread.currentThread().getName());
    }

    public void createMandelBrotImage(MandelbrotPointMap mandelbrotPointMap) {
        currentImageFrame = new ImageFrame(mandelbrotPointMap, this, panelMandelbrot.getPalette());
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                long t = System.currentTimeMillis();
                mandelbrotPointMap.tileIterate();
                log.info("tileIterate = {} ms", System.currentTimeMillis() - t);
                return null;
            }
            @Override
            protected void done() {
                currentImageFrame.drawImage();
                toFront();
            }
        }.execute();
    }

    public void initNewMandelbrotMap(ComplexNumber center, double width, double height) {
        panelMandelbrot.init(center, width, height);
        if (currentImageFrame != null) {
            currentImageFrame.updatePreviewRect(center, width, height);
        }
    }

    public void triggerRender() {
        panelMandelbrot.triggerRender();
    }

    public void updatePreviewRect(ComplexNumber center, double width, double height) {
        if (currentImageFrame != null) {
            currentImageFrame.updatePreviewRect(center, width, height);
        }
    }
}
