package com.afollestad.sectionedrecyclerview;


interface ItemProvider {

  int getSectionCount();

  int getItemCount(int sectionIndex);

  boolean showHeadersForEmptySections();

  boolean showFooters();
}
