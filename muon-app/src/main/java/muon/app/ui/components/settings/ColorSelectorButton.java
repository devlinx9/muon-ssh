
package muon.app.ui.components.settings;

import com.jediterm.core.Color;
import lombok.Getter;
import muon.app.App;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author subhro
 */
@Getter
public class ColorSelectorButton extends JLabel {

    private java.awt.Color color;

    public ColorSelectorButton() {
        setBorder(new CompoundBorder(
                new LineBorder(App.getCONTEXT().getSkin().getDefaultBorderColor()),
                new CompoundBorder(
                        new MatteBorder(5, 5, 5, 5,
                                        App.getCONTEXT().getSkin().getSelectedTabColor()),
                        new LineBorder(App.getCONTEXT().getSkin().getDefaultBorderColor()))));
        setOpaque(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                java.awt.Color pColor = JColorChooser.showDialog(null, "Select color",
                                                                 getColor());
                if (color != null) {
                    Color jeditermColor = new Color(pColor.getRGB());
                    setColor(jeditermColor);
                }
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(50, 30);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /**
     * @param color the color to set
     */
    public void setColor(Color color) {
        java.awt.Color awtColor = new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        this.setBackground(awtColor);
        this.color = awtColor;
    }

}
