package net.ksa.myplace.ui.listeners;

import android.support.v7.graphics.Palette;

import net.ksa.myplace.model.PlaceWrapper;

public interface RecyclerClickListener {
    void onClick(PlaceWrapper pw, Palette mPalette);
}
