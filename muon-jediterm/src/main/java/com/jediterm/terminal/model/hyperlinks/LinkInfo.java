package com.jediterm.terminal.model.hyperlinks;


import lombok.extern.slf4j.Slf4j;

/**
 * @author traff
 */
@Slf4j
public class LinkInfo {
  private final Runnable myNavigateCallback;

  public LinkInfo( Runnable navigateCallback) {
    myNavigateCallback = navigateCallback;
  }

  public void navigate() {
    myNavigateCallback.run();
  }
}
