/**
 *
 */
package muon.app.ssh;

import muon.app.App;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author subhro
 *
 */
public class GraphicalInputBlocker extends JDialog implements InputBlocker, ActionListener {
    private final JFrame window;

    private final JPanel jPanel = new JPanel(null);
    
    //Todo devlinx9 fix this.
    private final JLabel connectingLabel = new JLabel(App.bundle.getString("connecting"));
    
    private final JButton cancelButton = new JButton();
    
    private Runnable cancellable;
    
    
    /**
     *
     */
    public GraphicalInputBlocker(JFrame window) {
        super(window);
        this.window = window;
        
        connectingLabel.setLocation(0, 0);
        connectingLabel.setSize(200, 100);
        
        cancelButton.setIcon(new Cross(new Color(255, 0, 0)));
        cancelButton.setRolloverIcon(new Cross(new Color(255, 100, 100)));
        cancelButton.setPressedIcon(new Cross(new Color(255, 150, 150)));
        
        cancelButton.setToolTipText(App.bundle.getString("cancel"));
        cancelButton.setLocation(200 - 25, 5);
        cancelButton.setSize(20, 20);
        cancelButton.setBackground(null);
        cancelButton.setOpaque(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setBorder(null);
        cancelButton.setMargin(new Insets(0, 0, 0, 0));
        cancelButton.setContentAreaFilled(false);
        cancelButton.setVisible(false);
        cancelButton.addActionListener(this);
        
        jPanel.add(connectingLabel);
        jPanel.add(cancelButton);
        
        this.setUndecorated(true);
        setModal(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(200, 100);
    }

    @Override
    public void blockInput(Runnable cancellable) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Making visible...");
            this.setLocationRelativeTo(window);
            connectingLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            this.remove(jPanel);
            this.add(jPanel);
            
            this.cancellable = cancellable;
            
            cancelButton.setVisible(cancellable != null);
            
            this.setVisible(true);
        });
    }

    @Override
    public void unblockInput() {
        SwingUtilities.invokeLater(() -> {
            this.cancellable = null;
            cancelButton.setVisible(false);
            this.setVisible(false);
        });
    }
    
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
    
        Runnable cancellable = this.cancellable;
        if(cancellable != null)
            cancellable.run();
        
    }
    
    static class Cross implements Icon{
        
        int w = 20, h = 20;
        Color color;
        
        Cross(Color color){
            this.color = color;
        }
        
        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            
            Graphics2D g = (Graphics2D) graphics;
            RenderingHints hints = new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHints(hints);
            
            g.setColor(color);
            g.setStroke(new BasicStroke(2));
            int off = 2;
            g.drawLine(x+ off, y+ off, x+w- off, y+h- off);
            g.drawLine(x+ off, y+h- off, x+w- off, y+ off);
            
        }
        
        @Override
        public int getIconWidth() {
            return w;
        }
        
        @Override
        public int getIconHeight() {
            return h;
        }
    }
    
}
