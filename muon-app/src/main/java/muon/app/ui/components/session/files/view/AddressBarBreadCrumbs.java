package muon.app.ui.components.session.files.view;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AddressBarBreadCrumbs extends JPanel {
    public UIDefaults toolBarButtonSkin = new UIDefaults();
    private final boolean unix;
    private final MouseAdapter ma;
    private String[] segments;
    private final List<ActionListener> listeners = new ArrayList<>();

    public AddressBarBreadCrumbs(boolean unix,
                                 ActionListener popupTriggerListener) {
        super(new AddressBarLayout());
        this.unix = unix;
        segments = new String[0];
        createAddressButtonStyle();
        ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String selectedPath = calculatePath(
                        (JComponent) e.getComponent());
                log.info("Selected path: {}", selectedPath);
                if (e.getButton() == MouseEvent.BUTTON3) {
                    if (popupTriggerListener != null) {
                        popupTriggerListener.actionPerformed(
                                new ActionEvent(e, hashCode(), selectedPath));
                    }
                } else {
                    for (ActionListener l : listeners) {
                        log.info("Performing action");
                        l.actionPerformed(new ActionEvent(this, hashCode(),
                                selectedPath));
                    }
                }
            }
        };
    }

    private String calculatePath(JComponent c) {
        for (int i = 0; i < this.getComponentCount(); i++) {
            JComponent cc = (JComponent) getComponent(i);
            if (c == cc) {
                Integer index = (Integer) cc.getClientProperty("path.index");
                if (index != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int k = 0; k <= index; k++) {
                        if (k != 0) {
                            stringBuilder.append(unix ? "/" : "\\");
                        }
                        stringBuilder.append(segments[k]);
                    }
                    return stringBuilder.toString();
                }
                break;
            }
        }
        return null;
    }

    @Override
    public Dimension getPreferredSize() {
        return getLayout().preferredLayoutSize(this);
    }

    public void setPath(String path) {
        if (path.endsWith("/") || path.endsWith("\\")) {
            path = path.substring(0, path.length() - 1);
        }

        for (int i = 0; i < this.getComponentCount(); i++) {
            JComponent cc = (JComponent) getComponent(i);
            cc.removeMouseListener(ma);
        }

        this.removeAll();
        segments = path.split(unix ? "\\/" : "\\\\");
        for (int i = 0; i < segments.length; i++) {
            String text = segments[i];
            if (text.isEmpty()) {
                continue;
            }
            JButton btn = new JButton(segments[i]);
            btn.putClientProperty("Nimbus.Overrides", this.toolBarButtonSkin);
            btn.addMouseListener(ma);
            btn.putClientProperty("path.index", i);
            if (i == segments.length - 1) {
                btn.putClientProperty("path.index.last", true);
            }
            add(btn);
        }

        this.doLayout();
        this.revalidate();
        this.repaint();
    }

    public void addActionListener(ActionListener a) {
        this.listeners.add(a);
    }

    public String getSelectedText() {
        return "";
    }

    private void createAddressButtonStyle() {
        Painter<JButton> toolBarButtonPainterNormal = (g, object, width, height) -> {
            if (object
                    .getClientProperty("path.index.last") == Boolean.TRUE) {
                g.setColor(App.getCONTEXT().getSkin().getAddressBarSelectionBackground());
                g.fillRect(0, 0, width - 1, height - 1);
            }
        };

        Painter<JButton> toolBarButtonPainterHot = (g, object, width, height) -> {
            g.setColor(App.getCONTEXT().getSkin().getAddressBarRolloverBackground());
            g.fillRect(0, 0, width - 1, height - 1);
        };

        Painter<JButton> toolBarButtonPainterPressed = (g, object, width, height) -> {
            g.setColor(App.getCONTEXT().getSkin().getAddressBarSelectionBackground());
            g.fillRect(0, 0, width - 1, height - 1);
        };

        toolBarButtonSkin.put("Button.contentMargins", new Insets(2, 8, 2, 8));
        setTolbarButtonSkin(toolBarButtonPainterNormal, toolBarButtonPainterHot, toolBarButtonPainterPressed, toolBarButtonSkin);
    }

    public static void setTolbarButtonSkin(Painter<JButton> toolBarButtonPainterNormal, Painter<JButton> toolBarButtonPainterHot, Painter<JButton> toolBarButtonPainterPressed, UIDefaults toolBarButtonSkin) {
        toolBarButtonSkin.put("Button[Enabled].backgroundPainter",
                              toolBarButtonPainterNormal);
        toolBarButtonSkin.put("Button[Focused].backgroundPainter",
                              toolBarButtonPainterNormal);
        toolBarButtonSkin.put("Button[Default].backgroundPainter",
                              toolBarButtonPainterNormal);
        toolBarButtonSkin.put("Button[Default+Focused].backgroundPainter",
                              toolBarButtonPainterNormal);

        toolBarButtonSkin.put("Button[Pressed].backgroundPainter",
                              toolBarButtonPainterPressed);
        toolBarButtonSkin.put("Button[Focused+Pressed].backgroundPainter",
                              toolBarButtonPainterPressed);
        toolBarButtonSkin.put(
                "Button[Default+Focused+Pressed].backgroundPainter",
                toolBarButtonPainterPressed);
        toolBarButtonSkin.put("Button[Default+Pressed].backgroundPainter",
                              toolBarButtonPainterPressed);

        toolBarButtonSkin.put("Button[MouseOver].backgroundPainter",
                              toolBarButtonPainterHot);
        toolBarButtonSkin.put("Button[Focused+MouseOver].backgroundPainter",
                              toolBarButtonPainterHot);
        toolBarButtonSkin.put("Button[Default+MouseOver].backgroundPainter",
                              toolBarButtonPainterHot);
        toolBarButtonSkin.put(
                "Button[Default+Focused+MouseOver].backgroundPainter",
                toolBarButtonPainterHot);
    }
}
