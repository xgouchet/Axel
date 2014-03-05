package fr.xgouchet.xmleditor.ui.widget;

import android.os.Build;
import android.os.Handler;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;


public class FastScrollTrickListener implements OnScrollListener {
    
    private ListView mListView;
    
    
    Handler mHandler = new Handler();
    boolean mFastScrollAlwaysVisible = false;
    
    
    public FastScrollTrickListener(ListView listview) {
        mListView = listview;
    }
    
    
    //////////////////////////////////////////////////////////////////////////////////////
    // ON SCROLL LISTENER
    //////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // http://stackoverflow.com/questions/20448371/issues-with-fast-scroll-in-android-4-4
        //fix an issue with 4.4 not showing fast scroll
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (scrollState == SCROLL_STATE_IDLE) {
                if (mFastScrollAlwaysVisible) {
                    mHandler.postDelayed(delayedFastScrollFixRunnable, 750);
                }
            } else {
                if (!mFastScrollAlwaysVisible) {
                    mHandler.removeCallbacks(delayedFastScrollFixRunnable);
                    mListView.setFastScrollAlwaysVisible(true);
                    mFastScrollAlwaysVisible = true;
                }
            }
        }
    }
    
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
    }
    
    
    //    fix for kitkat bug in fast scroll
    Runnable delayedFastScrollFixRunnable = new Runnable() {
        
        @Override
        public void run() {
            if (mListView != null && mFastScrollAlwaysVisible) {
                mListView.setFastScrollAlwaysVisible(false);
                mFastScrollAlwaysVisible = false;
            }
        }
    };
}
