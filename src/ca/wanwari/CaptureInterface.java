package ca.wanwari;

/*
 * CaptureInterface.java
 * Author: Wiesa Anwari
 * Displays the interface that is used to overlay the screen and display the area selected by the user
 * This class will render a grayed out transparent overlay covering the screen
 * When the user clicks and drags their mouse it will display a rectangle surrounding the area selected
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CaptureInterface extends MouseAdapter {

    private final JFrame displayWindow;
    private final DrawPanel drawPanel;

    //windows bar offset that may occur on some desktop environments
    private final int minX = (int)GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getMinX();
    private final int minY = (int)GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getMinY();

    private final int screen_width = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    private final int screen_height = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    private Rectangle captureRectangle;

    CaptureInterface() {
        displayWindow = new JFrame();
        displayWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
        displayWindow.setUndecorated(true);
        displayWindow.setLocation(new Point(minX, minY));
        displayWindow.setSize(new Dimension(screen_width, screen_height));
        displayWindow.setOpacity(0.3f);
        displayWindow.addMouseListener(this);
        displayWindow.addMouseMotionListener(this);

        drawPanel = new DrawPanel();
        displayWindow.add(drawPanel);

        captureRectangle = null;
    }

    void showDisplay() {
        displayWindow.setVisible(true);
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        super.mousePressed(mouseEvent);
        drawPanel.setClickedX((mouseEvent.getX()));
        drawPanel.setClickedY((mouseEvent.getY()));
    }

    /*
     * When the mouse is released hide the overlay and save the Rectangle that the user selected
     * Then setup and show the SaveInterface passing it the area that was selected
     */
    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        super.mouseReleased(mouseEvent);

        drawPanel.setVisible(false);
        displayWindow.remove(drawPanel);
        displayWindow.setVisible(false);
        displayWindow.dispose();

        //save the area the user selected
        captureRectangle = drawPanel.getRectangle();

        //Some desktop environments will now allow the overlay to be rendered over the system bar
        //this takes that offset into account and adjusts the points to accurately reflect teh area the user selected
        captureRectangle.setLocation(captureRectangle.x + minX, captureRectangle.y + minY);

        /*
        * PROBLEM: the drawPanel and displayWindow frames do not hide fast enough
        * EXPLANATION: while the frames are set to be hidden the actual process of hiding them is
        * not full completed before the next line of code is ran, thus resulting in the drawPanel
        * overlay being shown in the screen capture
        * SOLUTION: suspend the thread for 500ms to insure the frames have properly disposed
        * prior to the screen capture being taken
        * TODO: find a better solution
        */
        try {
            Thread.sleep(500);
        } catch(Exception e) {
            e.printStackTrace();
        }

        if (captureRectangle != null)
            setupSaveInterface(captureRectangle);
        else {
            System.out.println("ERROR: Selected area is null");
        }
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        super.mouseDragged(mouseEvent);
        drawPanel.setDraggedX(mouseEvent.getX());
        drawPanel.setDraggedY(mouseEvent.getY());
    }

    private void setupSaveInterface(Rectangle captureRectangle) {
        java.awt.EventQueue.invokeLater(() -> {
            try {
                SaveInterface saveInterface = new SaveInterface(captureRectangle);
                saveInterface.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
