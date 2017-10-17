package com.taiko.noblenote;


import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.AttributeSet;
import android.view.View;

import com.github.clans.fab.FloatingActionMenu;

/**
 * used inside the activity_main_two_pane.xml layout to animate the clans FAB menu when a snackbar appears
 */

@SuppressWarnings("unused")
public class FAMBehavior extends CoordinatorLayout.Behavior<View> // generic specialization must match the first coordinator layout's child's type
{
    private final Context mContext;
    private float mTranslationY;


    public FAMBehavior(Context context, AttributeSet attrs) {
        mContext = context;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency)
    {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout coordinatorLayout, View child, View snackbarDependency)
    {
        if (snackbarDependency instanceof Snackbar.SnackbarLayout) {
            FloatingActionMenu fabMenu = coordinatorLayout.findViewById(R.id.fab_menu);
            if(isSnackbarOverlappingFab(coordinatorLayout,snackbarDependency))
            {
                this.updateTranslation(coordinatorLayout, fabMenu, snackbarDependency);
            }
        }

        return false;
    }

    // handle snackbar dismissed
    @Override
    public void onDependentViewRemoved(CoordinatorLayout parent, View child, View dependency)
    {
        if (dependency instanceof Snackbar.SnackbarLayout) {
            FloatingActionMenu fabMenu = parent.findViewById(R.id.fab_menu);
            this.updateTranslation(parent, fabMenu, dependency);
        }
    }

    private void updateTranslation(CoordinatorLayout parent, View child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        if (translationY != this.mTranslationY) {
            ViewCompat.animate(child)
                    .cancel();
            if (Math.abs(translationY - this.mTranslationY) == (float) dependency.getHeight()) {
                ViewCompat.animate(child)
                        .translationY(translationY)
                        .setListener((ViewPropertyAnimatorListener) null);
            } else {
                ViewCompat.setTranslationY(child, translationY);
            }

            this.mTranslationY = translationY;
        }

    }

    private boolean isSnackbarOverlappingFab(CoordinatorLayout coordinatorLayout, View snackbar) {


        snackbar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        int fabWidth = ScreenUtil.dpToPx(mContext,30);
        int fabMargin = mContext.getResources().getDimensionPixelSize(R.dimen.fab_margin);
        return snackbar.getMeasuredWidth() >= (coordinatorLayout.getMeasuredWidth() - fabMargin - fabWidth);

    }
}
