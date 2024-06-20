package muon.app.ui.components.session.utilpage.sysload;

import muon.app.App;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

public class LineGraph extends JComponent {
    private static final long serialVersionUID = -8887995348037288952L;
    private double[] values = new double[0];

    private final Stroke lineStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND);
    private final Stroke gridStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND);
    private boolean dynamic = false;
    private String suffix = "%";
    private final Path2D shape = new Path2D.Double();
    private final Color bgColor = App.skin.getDefaultBackground();
    private final Color textColor = App.skin.getDefaultForeground();
    private final Color lineColor = new Color(51, 181, 229);
    private final Color gridColor = new Color(62, 68, 81);
    private final Color gridLineColor = App.skin.getSelectedTabColor();

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(bgColor);
        g2.fillRect(0, 0, getWidth(), getHeight());

        int count = values.length - 1;

        if (count < 1)
            return;

        double den = 100;
        if (dynamic) {

            double min = Float.MAX_VALUE;
            double max = Float.MIN_VALUE;

            for (int i = 0; i < values.length; i++) {
                if (values[i] < min) {
                    min = values[i];
                }
                if (values[i] > max) {
                    max = values[i];
                }
            }

            double extra = ((max - min) * 5) / 100;
            max += extra;
            min -= extra;

            den = max - min;
        }

        double denStep = den / 4;

        int labelWidth = 0;
        int labelPaddingX = 5;
        int labelPaddingY = 5;

        int height = getHeight() - 6;

        float stepy = height / 4;

        int ascent = g2.getFontMetrics().getAscent();

        g2.setColor(textColor);

        int width = getWidth() - 6;

        int xoff = 2 * labelPaddingX + labelWidth;
        int yoff = labelPaddingY;

        g2.translate(3, 3);

        drawGraph(width, height, den, count, g2);

        g2.translate(-3, -3);
        g2.dispose();
    }

    private void drawGraph(int width, int height, double den, int count,
                           Graphics2D g2) {
        shape.reset();
        shape.moveTo(width, height);
        shape.lineTo(0, height);

        double stepy = (double) height / 4;
        double stepx = (double) width / count;

        g2.setColor(gridColor);
        g2.setStroke(gridStroke);

        for (int i = 0; i < count + 1; i++) {
            g2.setColor(gridColor);
            int y1 = (int) Math.floor((values[i] * height) / den);
            int x1 = (int) Math.floor(i * stepx);
            shape.lineTo(x1, height - y1);

            g2.setColor(gridLineColor);
            int y = (int) Math.floor(i * stepy);
            int x = (int) Math.floor(i * stepx);
            g2.drawLine(0, y, width, y);
            g2.drawLine(x, 0, x, height);
        }

        g2.setColor(lineColor);
        g2.setStroke(lineStroke);
        g2.drawRect(0, 0, width, height);
        g2.draw(shape);

        g2.setComposite(AlphaComposite.SrcOver.derive(0.4f));
        g2.fill(shape);
        g2.setComposite(AlphaComposite.SrcOver);

        g2.setColor(gridColor);
        g2.setStroke(gridStroke);

    }

    public double[] getValues() {
        return values;
    }

    public void setValues(double[] values) {
        this.values = values;
        repaint();
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
