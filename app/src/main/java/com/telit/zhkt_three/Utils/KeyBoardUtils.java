package com.telit.zhkt_three.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

/**
 * *****************************************************************
 * author: Administrator
 * time: 2017/9/7 16:30
 * name;
 * overview:
 * usage:软键盘的监听
 * ******************************************************************
 */

public class KeyBoardUtils {
    private Context ctx;

    public KeyBoardUtils(Context ctx) {
        this.ctx = ctx;
    }

    OnKeyboardVisibilityListener keyboardListener;

    public OnKeyboardVisibilityListener getKeyboardListener() {
        return keyboardListener;
    }

    public interface OnKeyboardVisibilityListener {


        void onVisibilityChanged(boolean visible);
    }

    public void setKeyboardListener(final OnKeyboardVisibilityListener listener, Activity activity) {
        final View activityRootView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);

        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            private boolean wasOpened;

            private final int DefaultKeyboardDP = 100;

            // From @nathanielwolf answer... Lollipop includes button bar in the root. Add height of button bar (48dp) to maxDiff
            private final int EstimatedKeyboardDP = DefaultKeyboardDP + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 48 : 0);

            private final Rect r = new Rect();

            @Override
            public void onGlobalLayout() {
                // Convert the dp to pixels.
                int estimatedKeyboardHeight = (int) TypedValue
                        .applyDimension(TypedValue.COMPLEX_UNIT_DIP, EstimatedKeyboardDP, activityRootView.getResources().getDisplayMetrics());

                // Conclude whether the keyboard is shown or not.
                activityRootView.getWindowVisibleDisplayFrame(r);
                int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
                boolean isShown = heightDiff >= estimatedKeyboardHeight;

                if (isShown == wasOpened) {
                    Log.e("Keyboard state", "Ignoring global layout change...");
                    return;
                }

                wasOpened = isShown;
                listener.onVisibilityChanged(isShown);
            }
        });
    }
}
