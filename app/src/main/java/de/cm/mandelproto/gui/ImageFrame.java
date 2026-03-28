package de.cm.mandelproto.gui;

import de.cm.mandelproto.graphics.PixelCanvas;
import de.cm.mandelproto.math.ComplexNumber;
import de.cm.mandelproto.math.IterationMap;

import java.awt.Rectangle;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ImageFrame extends JFrame implements MouseListener {

    private final IterationMap iterationMap;
    private final PixelCanvas pixelCanvas;
    private final MainFrame mainFrame;

    public ImageFrame(IterationMap iterationMap, MainFrame mainFrame) {
        super();
        this.mainFrame = mainFrame;
        System.out.println("create ImageFrame");
        this.iterationMap = iterationMap;
        setSize(iterationMap.getStepOnWidth(), iterationMap.getStepOnHeight()+40);
        setResizable(false);
        pixelCanvas = new PixelCanvas(getWidth(), getHeight(), iterationMap);
        pixelCanvas.addMouseListener(this);
        add(pixelCanvas);
        setVisible(true);
        //draw();
        System.out.println("created ImageFrame");
    }

    public void draw() {
        pixelCanvas.draw();
        System.out.println("drawed pixelCanvas ");
    }

    public void updatePreviewRect(ComplexNumber previewCenter, double previewWidth) {
        double previewHeight   = previewWidth * (9.0 / 16.0);
        double complexPerPixel = iterationMap.getWidth() / iterationMap.getStepOnWidth();
        double mapLeft         = iterationMap.getCenter().getReal() - iterationMap.getWidth()  / 2;
        double mapTop          = iterationMap.getCenter().getImag() + iterationMap.getHeight() / 2;

        int previewLeft   = (int) ((previewCenter.getReal() - previewWidth  / 2 - mapLeft) / complexPerPixel);
        int previewTop    = (int) ((mapTop - (previewCenter.getImag() + previewHeight / 2)) / complexPerPixel);
        int previewRight  = (int) ((previewCenter.getReal() + previewWidth  / 2 - mapLeft) / complexPerPixel);
        int previewBottom = (int) ((mapTop - (previewCenter.getImag() - previewHeight / 2)) / complexPerPixel);

        pixelCanvas.setPreviewRect(
                new Rectangle(previewLeft, previewTop, previewRight - previewLeft, previewBottom - previewTop)
        );
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        System.out.println("clicked on " + e.getX()+ ", "+e.getY());
        ComplexNumber clickedOn = iterationMap.getComplexNumberForCoordinate(e.getX(),e.getY());
        System.out.println("clicked on " + clickedOn.getReal()+ ", "+clickedOn.getImag());
        mainFrame.initNewMandelbrotMap(clickedOn,iterationMap.getWidth()/2 );
    }

    @Override
    public void mousePressed(MouseEvent e) {
        System.out.println("mousePressed on " + e.getX()+ ", "+e.getY());

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        System.out.println("released on " + e.getX()+ ", "+e.getY());
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

}
