package de.cm.mandelproto.gui;

import de.cm.mandelproto.I18n;
import de.cm.mandelproto.math.RenderParameters;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Locale;
import java.util.Optional;

@Slf4j
public class ParameterDialog extends JDialog {

    @FunctionalInterface
    public interface RectAdjuster {
        RenderParameters adjust(int dx, int dy, boolean shift);
    }

    // --- Felder ---
    private final Dimension screen;
    private RenderParameters current;
    private boolean confirmed = false;

    private final JLabel val_centerReal;
    private final JLabel val_centerImag;
    private final JLabel val_complexWidth;
    private final JLabel val_complexHeight;
    private final IntTextField tf_pixelWidth;
    private final IntTextField tf_pixelHeight;
    private final IntTextField tf_maxIterations;

    // --- Öffentlicher Einstiegspunkt ---

    public static Optional<RenderParameters> requestParameters(
            Component parent,
            RenderParameters initialParams,
            RectAdjuster adjuster
    ) {
        ParameterDialog dlg = new ParameterDialog(parent, initialParams);
        dlg.registerArrowDispatcher(adjuster);
        log.debug("ParameterDialog geöffnet");
        dlg.setVisible(true);
        log.debug("ParameterDialog geschlossen: confirmed={}", dlg.confirmed);
        return dlg.getRenderParameters();
    }

    // --- Konstruktor ---

    private ParameterDialog(Component parent, RenderParameters initialParams) {
        super(SwingUtilities.getWindowAncestor(parent),
                I18n.get("dialog.newImage.title"), ModalityType.APPLICATION_MODAL);
        this.screen  = Toolkit.getDefaultToolkit().getScreenSize();
        this.current = initialParams;

        val_centerReal   = makeValueLabel(fmt(initialParams.center().getReal()));
        val_centerImag   = makeValueLabel(fmt(initialParams.center().getImag()));
        val_complexWidth = makeValueLabel(fmt(initialParams.complexWidth()));
        val_complexHeight= makeValueLabel(fmt(initialParams.complexHeight()));

        tf_pixelWidth = new IntTextField(I18n.get("field.pixelWidth"));
        tf_pixelWidth.setInt(initialParams.pixelWidth());
        tf_pixelWidth.addFocusLostListener(this::updatePixelHeight);

        tf_pixelHeight = new IntTextField(I18n.get("field.pixelHeight"));
        tf_pixelHeight.setInt(initialParams.pixelHeight());
        tf_pixelHeight.setReadOnly(true);
        tf_pixelHeight.setFieldFocusable(false);

        tf_maxIterations = new IntTextField(I18n.get("field.maxIterations"));
        tf_maxIterations.setInt(initialParams.maxIterations());

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(buildInfoPanel(),   BorderLayout.NORTH);
        content.add(buildEditPanel(),   BorderLayout.CENTER);
        content.add(buildButtonPanel(), BorderLayout.SOUTH);
        setContentPane(content);
        pack();
        setLocationRelativeTo(null);
    }

    // --- Panel-Aufbau ---

    private JPanel buildInfoPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 2));
        p.add(new JLabel(I18n.get("field.centerReal")));    p.add(val_centerReal);
        p.add(new JLabel(I18n.get("field.centerImag")));    p.add(val_centerImag);
        p.add(new JLabel(I18n.get("field.complexWidth")));  p.add(val_complexWidth);
        p.add(new JLabel(I18n.get("field.complexHeight"))); p.add(val_complexHeight);
        return p;
    }

    private JPanel buildEditPanel() {
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 4));
        p.add(tf_pixelWidth);
        p.add(tf_pixelHeight);
        p.add(tf_maxIterations);
        return p;
    }

    private JPanel buildButtonPanel() {
        JButton btnOk     = new JButton(I18n.get("button.ok"));
        JButton btnCancel = new JButton(I18n.get("button.cancel"));
        btnOk.addActionListener(e -> { confirmed = true; dispose(); });
        btnCancel.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnOk);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.add(btnOk);
        p.add(btnCancel);
        return p;
    }

    // --- Koordinaten-Aktualisierung ---

    private void refreshCoordinates(RenderParameters params) {
        current = params;
        val_centerReal.setText(fmt(params.center().getReal()));
        val_centerImag.setText(fmt(params.center().getImag()));
        val_complexWidth.setText(fmt(params.complexWidth()));
        val_complexHeight.setText(fmt(params.complexHeight()));
        updatePixelHeight();
    }

    private void updatePixelHeight() {
        try {
            int pw = Math.min(tf_pixelWidth.getInt(), screen.width);
            int ph = (int) ((current.complexHeight() / current.complexWidth()) * pw);
            if (ph > screen.height) {
                ph = screen.height;
                pw = (int) ((current.complexWidth() / current.complexHeight()) * ph);
                tf_pixelWidth.setInt(pw);
            }
            tf_pixelHeight.setInt(ph);
        } catch (NumberFormatException ignored) {}
    }

    // --- Pfeiltasten ---

    private void registerArrowDispatcher(RectAdjuster adjuster) {
        if (adjuster == null) return;
        KeyEventDispatcher dispatcher = e -> {
            if (e.getID() != KeyEvent.KEY_PRESSED) return false;
            boolean shift = (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0;
            int dx = 0, dy = 0;
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT  -> dx = -1;
                case KeyEvent.VK_RIGHT -> dx = 1;
                case KeyEvent.VK_UP    -> dy = shift ? 1 : -1;
                case KeyEvent.VK_DOWN  -> dy = shift ? -1 : 1;
                default -> { return false; }
            }
            RenderParameters newParams = adjuster.adjust(dx, dy, shift);
            if (newParams != null) {
                log.debug("Rechteck angepasst: dx={}, dy={}, shift={} → complexWidth={}",
                        dx, dy, shift, newParams.complexWidth());
                refreshCoordinates(newParams);
            }
            return true;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager()
                        .removeKeyEventDispatcher(dispatcher);
            }
        });
    }

    // --- Render-Parameter auslesen ---

    private Optional<RenderParameters> getRenderParameters() {
        if (!confirmed) return Optional.empty();
        try {
            return Optional.of(new RenderParameters(
                    current.center(),
                    current.complexWidth(),
                    current.complexHeight(),
                    tf_pixelWidth.getInt(),
                    tf_maxIterations.getInt())
            );
        } catch (NumberFormatException e) {
            log.warn("Ungültige Eingabe im ParameterDialog — RenderParameters konnten nicht erstellt werden", e);
            return Optional.empty();
        }
    }

    // --- Hilfsmethoden ---

    private static JLabel makeValueLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setHorizontalAlignment(JLabel.RIGHT);
        return lbl;
    }

    private static String fmt(double value) {
        return String.format(Locale.US, "%.8f", value);
    }
}
