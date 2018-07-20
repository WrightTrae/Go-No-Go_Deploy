package com.wright.android.t_minus.main_tabs.photos;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;

public abstract class DoubleClickListener implements AdapterView.OnItemClickListener {

    private static final long DEFAULT_QUALIFICATION_SPAN = 200;
    private long doubleClickQualificationSpanInMillis;
    private long timestampLastClick;
    private boolean mHasDoubleClicked = false;

    public DoubleClickListener() {
        doubleClickQualificationSpanInMillis = DEFAULT_QUALIFICATION_SPAN;
        timestampLastClick = 0;
    }

    public DoubleClickListener(long doubleClickQualificationSpanInMillis) {
        this.doubleClickQualificationSpanInMillis = doubleClickQualificationSpanInMillis;
        timestampLastClick = 0;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if ((SystemClock.elapsedRealtime() - timestampLastClick) < doubleClickQualificationSpanInMillis) {
            onDoubleClick(view);
            mHasDoubleClicked = true;
        }else {
            mHasDoubleClicked = false;
            final Handler myHandler = new doubleClickHandler(view);
            myHandler.sendMessageDelayed(new Message(), doubleClickQualificationSpanInMillis);
        }
        timestampLastClick = SystemClock.elapsedRealtime();
    }

    public abstract void onDoubleClick(View view);

    public abstract void onSingleClick(View view);


    private class doubleClickHandler extends Handler{
        private View view;

        doubleClickHandler(View view) {
            this.view = view;
        }

        public void handleMessage(Message m) {
            if (!mHasDoubleClicked) {
                onSingleClick(view);
            }
        }
    }
}
