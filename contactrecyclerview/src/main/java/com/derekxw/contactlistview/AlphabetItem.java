package com.derekxw.contactlistview;

/**
 * https://github.com/viethoa/recyclerview-alphabet-fast-scroller-android
 * Created by Derek on 5/17/2017.
 */

public class AlphabetItem {
    public int position;
    public String word;
    public boolean isActive;

    public AlphabetItem(int pos, String word, boolean isActive) {
        this.position = pos;
        this.word = word;
        this.isActive = isActive;
    }
}
