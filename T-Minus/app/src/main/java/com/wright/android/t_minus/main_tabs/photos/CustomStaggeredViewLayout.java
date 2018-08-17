package com.wright.android.t_minus.main_tabs.photos;

import android.support.v7.widget.StaggeredGridLayoutManager;

// Trae Wright
public class CustomStaggeredViewLayout extends StaggeredGridLayoutManager {

    private boolean isScrollEnabled = true;

    public CustomStaggeredViewLayout(int spanCount, int orientation) {
        super(spanCount, orientation);
    }

    public void setScrollEnabled(boolean enabled) {
        this.isScrollEnabled = enabled;
    }

    @Override
    public boolean canScrollVertically() {
        return isScrollEnabled && super.canScrollVertically();
    }
}
