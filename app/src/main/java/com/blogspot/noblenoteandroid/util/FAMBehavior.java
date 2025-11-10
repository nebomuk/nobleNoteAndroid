package com.blogspot.noblenoteandroid.util;


import android.content.Context;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import android.util.AttributeSet;
import android.view.View;

import com.github.clans.fab.FloatingActionMenu;
import com.blogspot.noblenoteandroid.R;

/**
 * used inside the fragment_main_two_pane.xml layout to animate the clans FAB menu when a snackbar appears
 *
 * don't forget to update the package in the layout.xml when moving this class
 *
 * xml attribute must be added to the FAB menu, not to the Coordinator layout
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
        if (snackbarDependency instanceof Snackbar.SnackbarLayout
                && isSnackbarOverlappingFab(coordinatorLayout,snackbarDependency)) {
            FloatingActionMenu fabMenu = coordinatorLayout.findViewById(R.id.fab_menu);
            this.updateTranslation(coordinatorLayout, fabMenu, snackbarDependency);
        }

        return false;
    }

    // handle snackbar dismissed
    @Override
    public void onDependentViewRemoved(CoordinatorLayout coordinatorLayout, View child, View snackbarDependency)
    {
        if (snackbarDependency instanceof Snackbar.SnackbarLayout
                && isSnackbarOverlappingFab(coordinatorLayout,snackbarDependency)) {
            FloatingActionMenu fabMenu = coordinatorLayout.findViewById(R.id.fab_menu);
            this.updateTranslation(coordinatorLayout, fabMenu, snackbarDependency);
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

        // FIXME this only works on tablets, where the snackbar is shorter than the screen
        // and not overlapping the fab anyways
        snackbar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        int fabWidth = ScreenUtil.dpToPx(mContext,30);
        int fabMargin = mContext.getResources().getDimensionPixelSize(R.dimen.fab_margin);
        return snackbar.getMeasuredWidth() >= (coordinatorLayout.getWidth() - fabMargin - fabWidth);

    }
}
