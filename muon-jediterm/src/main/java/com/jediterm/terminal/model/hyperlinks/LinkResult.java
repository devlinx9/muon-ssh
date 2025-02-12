package com.jediterm.terminal.model.hyperlinks;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author traff
 */
@Slf4j
public class LinkResult {
  private final LinkResultItem myItem;
  private List<LinkResultItem> myItemList;

  public LinkResult( LinkResultItem item) {
    myItem = item;
    myItemList = null;
  }

  public LinkResult( List<LinkResultItem> itemList) {
    myItemList = itemList;
    myItem = null;
  }

  public List<LinkResultItem> getItems() {
    if (myItemList == null) {
      myItemList = new ArrayList<>(Collections.singletonList(myItem));
    }
    return myItemList;
  }
}
