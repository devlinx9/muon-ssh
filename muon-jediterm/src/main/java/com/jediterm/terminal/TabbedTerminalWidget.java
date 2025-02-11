package com.jediterm.terminal;

import com.jediterm.terminal.ui.AbstractTabbedTerminalWidget;
import com.jediterm.terminal.ui.AbstractTabs;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TerminalTabsImpl;
import com.jediterm.terminal.ui.settings.TabbedSettingsProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/**
 * @author traff
 */
@Slf4j
public class TabbedTerminalWidget extends AbstractTabbedTerminalWidget<JediTermWidget> {
  public TabbedTerminalWidget( TabbedSettingsProvider settingsProvider,  Function<AbstractTabbedTerminalWidget, JediTermWidget> createNewSessionAction) {
    super(settingsProvider, createNewSessionAction::apply);
  }

  @Override
  public JediTermWidget createInnerTerminalWidget() {
    return new JediTermWidget(getSettingsProvider());
  }

  @Override
  protected AbstractTabs<JediTermWidget> createTabbedPane() {
    return new TerminalTabsImpl();
  }
}
