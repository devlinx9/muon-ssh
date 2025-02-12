/**
 *
 */
package muon.app.ui.components;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author subhro
 */
@Slf4j
public class ImageView extends JComponent {
    private BufferedImage image;


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 200);
    }

    public void setImage(String imgFile) {
        try {
            image = ImageIO.read(new File(imgFile));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (image != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
