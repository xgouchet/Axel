package fr.xgouchet.xmleditor.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.TextView;
import fr.xgouchet.xmleditor.R;

/**
 * Mainly derived from Roman Nurik's code for GMail's Undo notifications
 */
public class UndoBarController {

    //////////////////////////////////////////////////////////////////////////////////////
    //
    //////////////////////////////////////////////////////////////////////////////////////

    private final int mHideDelay, mAnimationDelay;

    private final View mUndoBar;
    private final TextView mMessageView;
    private Runnable mUndoAction;

    /**
     * 
     */
    public UndoBarController(final View undoBar) {

        mUndoBar = undoBar;

        // prepare the view animator
        mBarAnimator = mUndoBar.animate();

        // get time values
        mHideDelay = mUndoBar.getResources().getInteger(R.integer.undo_notification_length);
        mAnimationDelay = mUndoBar.getResources().getInteger(R.integer.undo_notification_animation);

        // fetch message view
        mMessageView = (TextView) mUndoBar.findViewById(android.R.id.message);

        // set undo listener
        View undoButton = mUndoBar.findViewById(R.id.notification_undo);
        Log.i("UndoBarController", "Setting click listener on " + undoButton);
        undoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                Log.i("UndoBarController", "Clicked !");

                hideUndoBar(false);
                if (mUndoAction != null) {
                    mUndoAction.run();
                }

            }
        });

        hideUndoBar(true);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // UI / ANIMATION
    //////////////////////////////////////////////////////////////////////////////////////

    private ViewPropertyAnimator mBarAnimator;

    public void showUndoBar(final boolean immediate, final CharSequence message,
            final Runnable undoAction) {

        mUndoAction = undoAction;

        // Set the text
        mMessageView.setText(message);

        // Prepare auto hiding
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, mHideDelay);

        // Show the view
        mUndoBar.setVisibility(View.VISIBLE);
        mUndoBar.bringToFront();

        // To animate or not animate...
        if (immediate) {
            mUndoBar.setAlpha(1);
        } else {
            mBarAnimator.cancel();
            mBarAnimator.alpha(1).setDuration(mAnimationDelay).setListener(null);
            // XXX why add listener null ?
        }

    }

    public void hideUndoBar(final boolean immediate) {
        // remove callbacks for the runnable
        mHideHandler.removeCallbacks(mHideRunnable);

        // ... that is the question
        if (immediate) {
            mUndoBar.setVisibility(View.GONE);
            mUndoBar.setAlpha(0);
        } else {
            mBarAnimator.cancel();
            mBarAnimator.alpha(0).setDuration(mAnimationDelay)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(final Animator animation) {
                            mUndoBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    /** The handler for animations */
    private final Handler mHideHandler = new Handler();

    /** Runnable to automatically hide the view */
    private final Runnable mHideRunnable = new Runnable() {

        @Override
        public void run() {
            hideUndoBar(false);
        }
    };
}
