
package muon.app.ui.laf;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import muon.app.ui.components.common.RoundedButtonPainter;
import muon.app.ui.components.session.files.view.AddressBarBreadCrumbs;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;

import static muon.app.util.FontUtils.loadFontAwesomeFonts;
import static muon.app.util.FontUtils.loadFonts;

/**
 * @author subhro
 */
@Slf4j
public abstract class AppSkin {
    public static final String CONTROL = "control";
    public static final String TEXT = "text";
    public static final String NIMBUS_SELECTION_BACKGROUND = "nimbusSelectionBackground";
    public static final String NIMBUS_BORDER = "nimbusBorder";
    public static final String SCROLLBAR = "scrollbar";
    public static final String PAINT_NO_BORDER = "paintNoBorder";
    public static final String TEXT_FIELD_BACKGROUND = "TextField.background";
    protected UIDefaults defaults;

    @Getter
    protected NimbusLookAndFeel laf;


    public AppSkin() {
        initDefaults();
    }

    private void initDefaults() {
        this.laf = new NimbusLookAndFeel();
        this.defaults = this.laf.getDefaults();

        this.defaults.put("defaultFont", loadFonts());
        this.defaults.put("iconFont", loadFontAwesomeFonts());
        this.defaults.put("defaultStroke", new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        this.defaults.put("ScrollPane.contentMargins", new Insets(0, 0, 0, 0));


        Painter<? extends JComponent> scrollPaneBorderPainter = (graphics, component, width, height) -> {
            graphics.setColor(defaults.getColor(CONTROL));
            graphics.setStroke((BasicStroke) defaults.get("defaultStroke"));
            graphics.drawRect(0, 0, width, height);
        };

        this.defaults.put("ScrollPane[Enabled+Focused].borderPainter", scrollPaneBorderPainter);
        this.defaults.put("ScrollPane[Enabled].borderPainter", scrollPaneBorderPainter);

        this.defaults.put("ScrollBar.width", 7);

        Painter<? extends JComponent> treeCellFocusPainter = (g, object, width, height) -> {
        };
        this.defaults.put("Tree:TreeCell[Enabled+Focused].backgroundPainter", treeCellFocusPainter);
    }

    public Color getDefaultBackground() {
        return this.defaults.getColor(CONTROL);
    }

    public Color getDefaultForeground() {
        return this.defaults.getColor(TEXT);
    }

    public Color getDefaultSelectionForeground() {
        return this.defaults.getColor("nimbusSelectedText");
    }

    public Color getDefaultSelectionBackground() {
        return this.defaults.getColor(NIMBUS_SELECTION_BACKGROUND);
    }

    public Color getDefaultBorderColor() {
        return this.defaults.getColor(NIMBUS_BORDER);
    }

    public Font getIconFont() {
        return UIManager.getFont("iconFont");
    }

    public Font getDefaultFont() {
        return UIManager.getFont("defaultFont");
    }

    public Color getInfoTextForeground() {
        return this.defaults.getColor("infoText");
    }

    public Color getAddressBarSelectionBackground() {
        return this.defaults.getColor(SCROLLBAR);
    }

    public Color getAddressBarRolloverBackground() {
        return this.defaults.getColor("scrollbar-hot");
    }

    public UIDefaults getSplitPaneSkin() {
        UIDefaults uiDefaults = new UIDefaults();
        Painter<?> painter = (Painter<Object>) (g, object, width, height) -> {
            g.setColor(defaults.getColor(CONTROL));
            g.fill(new Rectangle(0, 0, width, height));
        };

        for (String key : new String[]{"SplitPane:SplitPaneDivider[Enabled].backgroundPainter",
                                       "SplitPane:SplitPaneDivider[Enabled+Vertical].foregroundPainter",
                                       "SplitPane:SplitPaneDivider[Enabled].backgroundPainter",
                                       "SplitPane:SplitPaneDivider[Enabled].foregroundPainter",
                                       "SplitPane:SplitPaneDivider[Focused].backgroundPainter",
                                       "SplitPane:SplitPaneDivider[Enabled].foregroundPainter",
                                       "SplitPane:SplitPaneDivider[Enabled].foregroundPainter"}) {
            uiDefaults.put(key, painter);
        }

        uiDefaults.put("SplitPane.contentMargins", new Insets(0, 0, 0, 0));
        uiDefaults.put("SplitPane.background", defaults.getColor(CONTROL));

        uiDefaults.put("background", defaults.getColor(CONTROL));
        uiDefaults.put("controlDkShadow", defaults.getColor(CONTROL));
        uiDefaults.put("controlHighlight", defaults.getColor(CONTROL));

        uiDefaults.put("menu", defaults.getColor(CONTROL));
        uiDefaults.put("nimbusBlueGrey", defaults.getColor(CONTROL));
        uiDefaults.put("controlHighlight", defaults.getColor(CONTROL));

        return uiDefaults;
    }

    public void createSkinnedButton(UIDefaults btnSkin) {
        RoundedButtonPainter cs = new RoundedButtonPainter(btnSkin);
        btnSkin.put("Button.contentMargins", new Insets(8, 15, 8, 15));
        btnSkin.put("Button[Default+Focused+MouseOver].backgroundPainter", cs.getHotPainter());
        btnSkin.put("Button[Default+Focused+Pressed].backgroundPainter", cs.getPressedPainter());
        btnSkin.put("Button[Default+Focused].backgroundPainter", cs.getNormalPainter());
        btnSkin.put("Button[Default+MouseOver].backgroundPainter", cs.getHotPainter());
        btnSkin.put("Button[Default+Pressed].backgroundPainter", cs.getPressedPainter());
        btnSkin.put("Button[Default].backgroundPainter", cs.getNormalPainter());
        btnSkin.put("Button[Enabled].backgroundPainter", cs.getNormalPainter());
        btnSkin.put("Button[Focused+MouseOver].backgroundPainter", cs.getHotPainter());
        btnSkin.put("Button[Focused+Pressed].backgroundPainter", cs.getPressedPainter());
        btnSkin.put("Button[Focused].backgroundPainter", cs.getNormalPainter());
        btnSkin.put("Button[MouseOver].backgroundPainter", cs.getHotPainter());
        btnSkin.put("Button[Pressed].backgroundPainter", cs.getPressedPainter());
        btnSkin.put("Button[Default+Pressed].textForeground", defaults.getColor(CONTROL));
        btnSkin.put("Button.foreground", defaults.getColor(CONTROL));
        btnSkin.put("Button[Disabled].textForeground", Color.GRAY);
        btnSkin.put("Button[Disabled].backgroundPainter", cs.getNormalPainter());
    }

    public void createTextFieldSkin(UIDefaults uiDefaults) {
        final Color borderColor = defaults.getColor(NIMBUS_BORDER);
        final Color focusedColor = defaults.getColor(NIMBUS_SELECTION_BACKGROUND);
        Painter<? extends JComponent> focusedBorder = (Painter<JComponent>) (g, object, width, height) -> {
            if (object.getClientProperty(PAINT_NO_BORDER) != null) {
                return;
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(focusedColor);
            g.drawRoundRect(1, 1, width - 2, height - 2, 5, 5);
        };

        Painter<? extends JComponent> normalBorder = (Painter<JComponent>) (g, object, width, height) -> {
            if (object.getClientProperty(PAINT_NO_BORDER) != null) {
                return;
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(borderColor);
            g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawRoundRect(1, 1, width - 2, height - 2, 5, 5);
        };

        uiDefaults.put("FormattedTextField[Disabled].borderPainter", normalBorder);
        uiDefaults.put("FormattedTextField[Enabled].borderPainter", normalBorder);
        uiDefaults.put("FormattedTextField[Focused].borderPainter", focusedBorder);

        uiDefaults.put("PasswordField[Disabled].borderPainter", normalBorder);
        uiDefaults.put("PasswordField[Enabled].borderPainter", normalBorder);
        uiDefaults.put("PasswordField[Focused].borderPainter", focusedBorder);

        uiDefaults.put("TextField[Disabled].borderPainter", normalBorder);
        uiDefaults.put("TextField[Enabled].borderPainter", normalBorder);
        uiDefaults.put("TextField[Focused].borderPainter", focusedBorder);

        uiDefaults.put("TextField.contentMargins", new Insets(8, 8, 8, 8));
        uiDefaults.put("PasswordField.contentMargins", new Insets(8, 8, 8, 8));
    }

    public void createSpinnerSkin(UIDefaults uiDefaults) {
        Color c1 = this.defaults.getColor(TEXT_FIELD_BACKGROUND);
        Color c2 = this.defaults.getColor(NIMBUS_BORDER);

        Painter<? extends JComponent> painter1 = (Painter<JComponent>) (g, object, width, height) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c1);
            g.fillRoundRect(1, 1, width - 2 + 20, height - 2, 5, 5);
            g.setColor(c2);
            g.drawRoundRect(1, 1, width - 2 + 20, height - 2, 5, 5);
        };

        Painter<? extends JComponent> painter2 = (Painter<JComponent>) (g, object, width, height) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c1);
            g.fillRoundRect(1 - 20, 1, width - 2 + 20, height - 2 + 20, 5, 5);
            g.setColor(c2);
            g.drawRoundRect(1 - 20, 1, width - 2 + 20, height - 2 + 20, 5, 5);
        };

        Painter<? extends JComponent> painter3 = (Painter<JComponent>) (g, object, width, height) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c1);
            g.fillRoundRect(1 - 20, 1 - 20, width - 2 + 20, height - 2 + 20, 5, 5);
            g.setColor(c2);
            g.drawRoundRect(1 - 20, 1 - 20, width - 2 + 20, height - 2 + 20, 5, 5);
        };

        uiDefaults.put("Spinner:\"Spinner.nextButton\"[Disabled].backgroundPainter", painter2);
        uiDefaults.put("Spinner:\"Spinner.nextButton\"[Enabled].backgroundPainter", painter2);
        uiDefaults.put("Spinner:\"Spinner.nextButton\"[Focused+MouseOver].backgroundPainter", painter2);
        uiDefaults.put("Spinner:\"Spinner.nextButton\"[Focused+Pressed].backgroundPainter", painter2);
        uiDefaults.put("Spinner:\"Spinner.nextButton\"[Focused].backgroundPainter", painter2);
        uiDefaults.put("Spinner:\"Spinner.nextButton\"[MouseOver].backgroundPainter", painter2);
        uiDefaults.put("Spinner:\"Spinner.nextButton\"[Pressed].backgroundPainter", painter2);

        uiDefaults.put("Spinner:\"Spinner.previousButton\"[Disabled].backgroundPainter", painter3);
        uiDefaults.put("Spinner:\"Spinner.previousButton\"[Enabled].backgroundPainter", painter3);
        uiDefaults.put("Spinner:\"Spinner.previousButton\"[Focused+MouseOver].backgroundPainter", painter3);
        uiDefaults.put("Spinner:\"Spinner.previousButton\"[Focused+Pressed].backgroundPainter", painter3);
        uiDefaults.put("Spinner:\"Spinner.previousButton\"[Focused].backgroundPainter", painter3);
        uiDefaults.put("Spinner:\"Spinner.previousButton\"[MouseOver].backgroundPainter", painter3);
        uiDefaults.put("Spinner:\"Spinner.previousButton\"[Pressed].backgroundPainter", painter3);

        uiDefaults.put("Spinner:Panel:\"Spinner.formattedTextField\"[Enabled].backgroundPainter", painter1);
        uiDefaults.put("Spinner:Panel:\"Spinner.formattedTextField\"[Focused].backgroundPainter", painter1);
        uiDefaults.put("Spinner:Panel:\"Spinner.formattedTextField\"[Focused+Selected].backgroundPainter", painter1);
        uiDefaults.put("Spinner:Panel:\"Spinner.formattedTextField\".contentMargins", new Insets(7, 7, 7, 7));
    }

    public void createComboBoxSkin(UIDefaults uiDefaults) {
        Color c1 = this.defaults.getColor(NIMBUS_BORDER);
        Color c2 = this.defaults.getColor(TEXT_FIELD_BACKGROUND);
        Painter<? extends JComponent> painter1 = (Painter<JComponent>) (g, object, width, height) -> {
            if (object.getClientProperty(PAINT_NO_BORDER) != null) {
                return;
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c2);
            g.fillRoundRect(1, 1, width - 2, height - 2, 5, 5);
            g.setColor(c1);
            g.drawRoundRect(1, 1, width - 2, height - 2, 5, 5);
        };

        Painter<? extends JComponent> painter2 = (Painter<JComponent>) (g, object, width, height) -> {
            if (object.getClientProperty(PAINT_NO_BORDER) != null) {
                return;
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c2);
            g.fillRoundRect(1, 1, width - 2, height - 2, 5, 5);
            g.setColor(c1);
            g.drawRoundRect(1, 1, width - 2, height - 2, 5, 5);
        };

        Painter<? extends JComponent> painter3 = (Painter<JComponent>) (g, object, width, height) -> {
            if (object.getClientProperty(PAINT_NO_BORDER) != null) {
                return;
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c2);
            g.fillRoundRect(1, 1, width - 2, height - 2, 5, 5);
            g.setColor(c1);
            g.drawRoundRect(1, 1, width - 2, height - 2, 5, 5);
        };

        Painter<? extends JComponent> painter4 = (Painter<JComponent>) (g, object, width, height) -> {
            if (object.getClientProperty(PAINT_NO_BORDER) != null) {
                return;
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c2);
            g.fillRoundRect(1, 1, width - 2, height - 2, 5, 5);
            g.setColor(c1);
            g.drawRoundRect(1, 1, width - 2, height - 2, 5, 5);
        };

        Painter<? extends JComponent> painter5 = (Painter<JComponent>) (g, object, width, height) -> {
            if (object.getClientProperty(PAINT_NO_BORDER) != null) {
                return;
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c2);
            g.fillRoundRect(1, 1, width - 2, height - 2, 5, 5);
            g.setColor(c1);
            g.drawRoundRect(1, 1, width - 2, height - 2, 5, 5);
        };
        uiDefaults.put("ComboBox:\"ComboBox.textField\"[Enabled].backgroundPainter", painter3);
        uiDefaults.put("ComboBox:\"ComboBox.textField\"[Selected].backgroundPainter", painter3);
        uiDefaults.put("ComboBox[Enabled].backgroundPainter", painter4);
        uiDefaults.put("ComboBox[Focused+MouseOver].backgroundPainter", painter5);
        uiDefaults.put("ComboBox[Focused+Pressed].backgroundPainter", painter5);
        uiDefaults.put("ComboBox[Focused].backgroundPainter", painter4);
        uiDefaults.put("ComboBox[MouseOver].backgroundPainter", painter1);
        uiDefaults.put("ComboBox[Pressed].backgroundPainter", painter1);
        uiDefaults.put("ComboBox[Editable+Focused].backgroundPainter", painter4);
        uiDefaults.put("ComboBox:\"ComboBox.arrowButton\"[Editable+Enabled].backgroundPainter", painter3);
        uiDefaults.put("ComboBox:\"ComboBox.arrowButton\"[Editable+MouseOver].backgroundPainter", painter2);
        uiDefaults.put("ComboBox:\"ComboBox.arrowButton\"[Editable+Pressed].backgroundPainter", painter2);
        uiDefaults.put("ComboBox:\"ComboBox.arrowButton\"[Editable+Selected].backgroundPainter", painter3);
        uiDefaults.put("ComboBox.contentMargins", new Insets(3, 5, 3, 5));
        uiDefaults.put("ComboBox:\"ComboBox.listRenderer\".contentMargins", new Insets(3, 5, 3, 5));
        uiDefaults.put("ComboBox.rendererUseListColors", Boolean.TRUE);
    }

    public void createTreeSkin(UIDefaults uiDefaults) {
        uiDefaults.put("Tree[Enabled].closedIconPainter", (Painter<JComponent>) (g, object, width, height) -> {
            Font font = g.getFont();
            g.setColor(defaults.getColor("Tree.textForeground"));
            g.setFont(getIconFont().deriveFont(16));
            int h = g.getFontMetrics().getAscent() + g.getFontMetrics().getDescent();

            g.drawString("\uf07b", 0, h);
            g.setFont(font);
        });

        uiDefaults.put("Tree[Enabled].openIconPainter", (Painter<JComponent>) (g, object, width, height) -> {
            g.setColor(defaults.getColor("Tree.textForeground"));
            Font font = g.getFont();
            g.setFont(getIconFont().deriveFont(16));
            int h = g.getFontMetrics().getAscent() + g.getFontMetrics().getDescent();

            g.drawString("\uf07c", 0, h);
            g.setFont(font);
        });

        uiDefaults.put("Tree[Enabled].leafIconPainter", (Painter<JComponent>) (g, object, width, height) -> {
            g.setColor(defaults.getColor("Tree.textForeground"));
            Font font = g.getFont();
            g.setFont(getIconFont().deriveFont(16));
            int h = g.getFontMetrics().getAscent() + g.getFontMetrics().getDescent();

            g.drawString("\uf15b", 0, h);
            g.setFont(font);
        });
        uiDefaults.put("Tree.rendererMargins", new Insets(5, 5, 5, 5));
    }

    public UIDefaults createToolbarSkin() {
        UIDefaults toolBarButtonSkin = new UIDefaults();
        Painter<JButton> toolBarButtonPainterNormal = (g, object, width, height) -> {
            g.setColor(UIManager.getColor(CONTROL));
            g.fillRect(0, 0, width, height);
        };

        Painter<JButton> toolBarButtonPainterHot = (g, object, width, height) -> {
            g.setColor(UIManager.getColor("scrollbar-hot"));
            g.fillRect(0, 0, width, height);
        };

        Painter<JButton> toolBarButtonPainterPressed = (g, object, width, height) -> {
            g.setColor(UIManager.getColor(SCROLLBAR));
            g.fillRect(0, 0, width, height);
        };

        toolBarButtonSkin.put("Button.contentMargins", new Insets(5, 8, 5, 8));

        toolBarButtonSkin.put("Button[Disabled].backgroundPainter", toolBarButtonPainterNormal);
        toolBarButtonSkin.put("Button[Disabled].textForeground", Color.LIGHT_GRAY);

        toolBarButtonSkin.put("Button.foreground", UIManager.getColor(SCROLLBAR));

        AddressBarBreadCrumbs.setTolbarButtonSkin(toolBarButtonPainterNormal, toolBarButtonPainterHot, toolBarButtonPainterPressed, toolBarButtonSkin);

        return toolBarButtonSkin;
    }

    public UIDefaults createTabButtonSkin() {
        UIDefaults toolBarButtonSkin = new UIDefaults();
        Painter<JButton> toolBarButtonPainterNormal = (g, object, width, height) -> {
            g.setColor(getDefaultBackground());
            g.fillRect(0, 0, width, height);
        };

        Painter<JButton> toolBarButtonPainterHot = (g, object, width, height) -> {
            g.setColor(getSelectedTabColor());
            g.fillRect(0, 0, width, height);
        };

        Painter<JButton> toolBarButtonPainterPressed = (g, object, width, height) -> {
            g.setColor(UIManager.getColor(SCROLLBAR));
            g.fillRect(0, 0, width, height);
        };

        toolBarButtonSkin.put("Button.contentMargins", new Insets(5, 8, 5, 8));

        toolBarButtonSkin.put("Button[Disabled].backgroundPainter", toolBarButtonPainterNormal);
        toolBarButtonSkin.put("Button[Disabled].textForeground", Color.LIGHT_GRAY);

        AddressBarBreadCrumbs.setTolbarButtonSkin(toolBarButtonPainterNormal, toolBarButtonPainterHot, toolBarButtonPainterPressed, toolBarButtonSkin);

        return toolBarButtonSkin;
    }

    public void createTableHeaderSkin(UIDefaults uiDefaults) {
        Painter<?> painterNormal = (Graphics2D g, Object object, int width, int height) -> {

        };
        uiDefaults.put("TableHeader.font", new Font(Font.DIALOG, Font.PLAIN, 14));
        uiDefaults.put("TableHeader.background", defaults.getColor(CONTROL));
        uiDefaults.put("TableHeader.foreground", defaults.getColor(TEXT));
        uiDefaults.put("TableHeader:\"TableHeader.renderer\".opaque", false);
        uiDefaults.put("TableHeader:\"TableHeader.renderer\"[Enabled+Focused+Sorted].backgroundPainter", painterNormal);
        uiDefaults.put("TableHeader:\"TableHeader.renderer\"[Enabled+Focused].backgroundPainter", painterNormal);
        uiDefaults.put("TableHeader:\"TableHeader.renderer\"[Enabled+Sorted].backgroundPainter", painterNormal);
        uiDefaults.put("TableHeader:\"TableHeader.renderer\"[Enabled].backgroundPainter", painterNormal);
        uiDefaults.put("TableHeader:\"TableHeader.renderer\"[MouseOver].backgroundPainter", painterNormal);
        uiDefaults.put("TableHeader:\"TableHeader.renderer\"[Pressed].backgroundPainter", painterNormal);
    }

    public void createPopupMenuSkin(UIDefaults uiDefaults) {
        Color controlColor = this.defaults.getColor(CONTROL);
        Color textColor = this.defaults.getColor(TEXT);
        Color selectedTextColor = this.defaults.getColor("nimbusSelectedText");

        uiDefaults.put("PopupMenu.background", controlColor);
        uiDefaults.put("PopupMenu.foreground", textColor);

        uiDefaults.put("Menu.foreground", textColor);
        uiDefaults.put("Menu[Enabled].textForeground", textColor);
        uiDefaults.put("Menu[Enabled+Selected].textForeground", selectedTextColor);

        uiDefaults.put("Menu.contentMargins", new Insets(5, 10, 5, 10));
        uiDefaults.put("MenuItem.contentMargins", new Insets(5, 10, 5, 10));
        uiDefaults.put("MenuItem.foreground", textColor);
        uiDefaults.put("MenuItem[Enabled].textForeground", textColor);
        uiDefaults.put("MenuItem[MouseOver].textForeground", selectedTextColor);
        uiDefaults.put("MenuItem:MenuItemAccelerator[Disabled].textForeground", textColor);
        uiDefaults.put("MenuItem:MenuItemAccelerator[MouseOver].textForeground", textColor);

        Painter<? extends JComponent> popupPainter = (graphics, component, width, height) -> {
            graphics.setColor(this.defaults.getColor(CONTROL));
            graphics.fillRect(0, 0, width, height);
        };

        uiDefaults.put("PopupMenu[Enabled].backgroundPainter", popupPainter);

        Painter<? extends JComponent> menuSelectionPainter = (graphics, component, width, height) -> {
            graphics.setColor(this.defaults.getColor(NIMBUS_SELECTION_BACKGROUND));
            graphics.fillRect(0, 0, width, height);
        };

        uiDefaults.put("MenuItem[MouseOver].backgroundPainter", menuSelectionPainter);
    }

    public Color getTableBackgroundColor() {
        return defaults.getColor("Table.background");
    }

    public Color getSelectedTabColor() {
        return defaults.getColor("button.pressedGradient2");
    }

    public Color getTextFieldBackground() {
        return defaults.getColor(TEXT_FIELD_BACKGROUND);
    }

    public void createCheckboxSkin(UIDefaults uiDefaults) {
        Color c1 = defaults.getColor(TEXT);
        Color c2 = defaults.getColor(TEXT);

        Painter<? extends JComponent> painter1 = (Painter<JComponent>) (g, object, width, height) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(c1);
            g.drawRect(2, 2, width - 4, height - 4);
        };

        Painter<? extends JComponent> painter2 = (Painter<JComponent>) (g, object, width, height) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(c2);
            g.drawRect(2, 2, width - 4, height - 4);
            g.fillRect(4, 4, width - 8, height - 8);
        };

        uiDefaults.put("CheckBox[Disabled].iconPainter", painter1);
        uiDefaults.put("CheckBox[Enabled].iconPainter", painter1);
        uiDefaults.put("CheckBox[Focused+MouseOver+Selected].iconPainter", painter2);
        uiDefaults.put("CheckBox[Focused+Pressed+Selected].iconPainter", painter2);
        uiDefaults.put("CheckBox[Focused+Selected].iconPainter", painter2);
        uiDefaults.put("CheckBox[MouseOver+Selected].iconPainter", painter2);
        uiDefaults.put("CheckBox[Pressed+Selected].iconPainter", painter2);

        uiDefaults.put("CheckBox[Focused+MouseOver].iconPainter", painter1);
        uiDefaults.put("CheckBox[Focused+Pressed].iconPainter", painter1);
        uiDefaults.put("CheckBox[Pressed].iconPainter", painter1);
        uiDefaults.put("CheckBox[Selected].iconPainter", painter2);
        uiDefaults.put("CheckBox[Focused].iconPainter", painter1);
        uiDefaults.put("CheckBox[MouseOver].iconPainter", painter1);

        // This is for checkbox menu item
        uiDefaults.put("CheckBoxMenuItem[Enabled+Selected].checkIconPainter", painter2);
        uiDefaults.put("CheckBoxMenuItem[Disabled+Selected].checkIconPainter", painter2);
        uiDefaults.put("CheckBoxMenuItem[MouseOver+Selected].checkIconPainter", painter2);
        uiDefaults.put("CheckBoxMenuItem.foreground", c1);
        uiDefaults.put("CheckBoxMenuItem[Enabled].textForeground", c1);
        uiDefaults.put("CheckBoxMenuItem.contentMargins", new Insets(5, 10, 5, 10));
    }

    public void createRadioButtonSkin(UIDefaults uiDefaults) {
        Color c1 = defaults.getColor(TEXT);
        Color c2 = defaults.getColor(TEXT);

        Painter<? extends JComponent> painter1 = (Painter<JComponent>) (g, object, width, height) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(c1);
            g.drawOval(1, 1, width - 2, height - 2);
        };

        Painter<? extends JComponent> painter2 = (Painter<JComponent>) (g, object, width, height) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c2);
            g.drawOval(1, 1, width - 2, height - 2);
            g.fillOval(4, 4, width - 8, height - 8);
        };

        uiDefaults.put("RadioButton[Disabled].iconPainter", painter1);
        uiDefaults.put("RadioButton[Enabled].iconPainter", painter1);
        uiDefaults.put("RadioButton[Focused+MouseOver+Selected].iconPainter", painter2);
        uiDefaults.put("RadioButton[Focused+Pressed+Selected].iconPainter", painter2);
        uiDefaults.put("RadioButton[Focused+Selected].iconPainter", painter2);
        uiDefaults.put("RadioButton[MouseOver+Selected].iconPainter", painter2);
        uiDefaults.put("RadioButton[Pressed+Selected].iconPainter", painter2);

        uiDefaults.put("RadioButton[Focused+MouseOver].iconPainter", painter1);
        uiDefaults.put("RadioButton[Focused+Pressed].iconPainter", painter1);
        uiDefaults.put("RadioButton[Pressed].iconPainter", painter1);
        uiDefaults.put("RadioButton[Selected].iconPainter", painter2);
        uiDefaults.put("RadioButton[Focused].iconPainter", painter1);
        uiDefaults.put("RadioButton[MouseOver].iconPainter", painter1);
    }

    public void createTooltipSkin(UIDefaults uiDefaults) {
        Color c1 = defaults.getColor("Tree.background");
        Painter<? extends JComponent> painter2 = (Painter<JComponent>) (g, object, width, height) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c1);
            g.fillRect(0, 0, width, height);
        };
        uiDefaults.put("ToolTip[Enabled].backgroundPainter", painter2);
        uiDefaults.put("ToolTip.background", defaults.getColor(CONTROL));
        uiDefaults.put("ToolTip.foreground", defaults.getColor(TEXT));
    }

    public void createSkinnedToggleButton(UIDefaults btnSkin) {
        RoundedButtonPainter cs = new RoundedButtonPainter(btnSkin);
        btnSkin.put("ToggleButton.contentMargins", new Insets(8, 15, 8, 15));
        btnSkin.put("ToggleButton[Default+Focused+MouseOver].backgroundPainter", cs.getHotPainter());
        btnSkin.put("ToggleButton[Default+Focused+Pressed].backgroundPainter", cs.getPressedPainter());
        btnSkin.put("ToggleButton[Default+Focused].backgroundPainter", cs.getNormalPainter());
        btnSkin.put("ToggleButton[Default+MouseOver].backgroundPainter", cs.getHotPainter());
        btnSkin.put("ToggleButton[Default+Pressed].backgroundPainter", cs.getPressedPainter());
        btnSkin.put("ToggleButton[Default].backgroundPainter", cs.getNormalPainter());
        btnSkin.put("ToggleButton[Enabled].backgroundPainter", cs.getNormalPainter());
        btnSkin.put("ToggleButton[Focused+MouseOver].backgroundPainter", cs.getHotPainter());
        btnSkin.put("ToggleButton[Focused+Pressed].backgroundPainter", cs.getPressedPainter());
        btnSkin.put("ToggleButton[Focused].backgroundPainter", cs.getNormalPainter());
        btnSkin.put("ToggleButton[MouseOver].backgroundPainter", cs.getHotPainter());
        btnSkin.put("ToggleButton[Pressed].backgroundPainter", cs.getPressedPainter());
        btnSkin.put("ToggleButton[Default+Pressed].textForeground", defaults.getColor(CONTROL));
        btnSkin.put("ToggleButton.foreground", defaults.getColor(CONTROL));
        btnSkin.put("ToggleButton[Disabled].textForeground", Color.GRAY);
        btnSkin.put("ToggleButton[Disabled].backgroundPainter", cs.getNormalPainter());
        btnSkin.put("ToggleButton.background", defaults.get("button.normalGradient1"));
    }

    public void createProgressBarSkin(UIDefaults uiDefaults) {
        Color c1 = this.defaults.getColor("nimbusSelection");
        Color c2 = this.defaults.getColor(TEXT_FIELD_BACKGROUND);

        Painter<? extends JComponent> painter1 = (Painter<JComponent>) (g, object, width, height) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c2);
            g.fillRoundRect(1, 1, width - 2, height - 2, 5, 5);
        };

        Painter<? extends JComponent> painter2 = (Painter<JComponent>) (g, object, width, height) -> {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c1);
            g.fillRoundRect(1, 1, width - 2, height - 2, 5, 5);
        };

        uiDefaults.put("ProgressBar.horizontalSize", new Dimension(150, 10));
        uiDefaults.put("ProgressBar.verticalSize", new Dimension(10, 150));

        uiDefaults.put("ProgressBar[Disabled+Finished].foregroundPainter", painter2);
        uiDefaults.put("ProgressBar[Disabled].foregroundPainter", painter2);
        uiDefaults.put("ProgressBar[Enabled+Finished].foregroundPainter", painter2);
        uiDefaults.put("ProgressBar[Enabled].foregroundPainter", painter2);

        uiDefaults.put("ProgressBar[Enabled].backgroundPainter", painter1);
        uiDefaults.put("ProgressBar[Disabled].backgroundPainter", painter1);
    }

}
