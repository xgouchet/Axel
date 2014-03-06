package fr.xgouchet.xmleditor.ui.adapter;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.tree.TreeNode;


/**
 * Holder for a view displaying a TreeNode
 * 
 * @param <T>
 *            the content type of the {@link TreeNode}
 * @author Xavier Gouchet
 */
public class NodeViewHolder<T> implements OnTouchListener,
        OnGestureListener, OnDoubleTapListener {
    
    private final GestureDetector mDetector;
    private final View mRootView;
    public final TextView content;
    public final ImageView decorator;
    private final NodeViewListener<T> mListener;
    
    public TreeNode<T> node;
    public int position;
    
    
    /**
     * 
     * @param view
     */
    public NodeViewHolder(final View view, final Context context, final NodeViewListener<T> listener) {
        
        mListener = listener;
        mRootView = view;
        
        // Get widgets
        content = (TextView) view.findViewById(R.id.textNode);
        decorator = (ImageView) view.findViewById(R.id.imageDecorator);
        
        // Set touch interactions
        content.setOnTouchListener(this);
        mRootView.setOnTouchListener(this);
        mDetector = new GestureDetector(context, this);
        mDetector.setOnDoubleTapListener(this);
    }
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////
    // GESTURE DETECTOR
    //////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        return mDetector.onTouchEvent(event);
        
    }
    
    @Override
    public boolean onDown(final MotionEvent e) {
        return true;
    }
    
    @Override
    public boolean onFling(final MotionEvent e1, final MotionEvent e2,
            final float velocityX, final float velocityY) {
        return false;
    }
    
    @Override
    public void onLongPress(final MotionEvent e) {
        if (mListener != null) {
            mListener.onNodeLongPressed(node, mRootView, position);
        }
    }
    
    @Override
    public boolean onScroll(final MotionEvent e1, final MotionEvent e2,
            final float distanceX, final float distanceY) {
        return false;
    }
    
    @Override
    public void onShowPress(final MotionEvent e) {
        
    }
    
    @Override
    public boolean onSingleTapUp(final MotionEvent e) {
        return true;
    }
    
    @Override
    public boolean onDoubleTapEvent(final MotionEvent e) {
        return true;
    }
    
    @Override
    public boolean onDoubleTap(final MotionEvent e) {
        if (mListener != null) {
            mListener.onNodeDoubleTapped(node, mRootView, position);
        }
        return true;
    }
    
    @Override
    public boolean onSingleTapConfirmed(final MotionEvent e) {
        if (mListener != null) {
            mListener.onNodeTapped(node, mRootView, position);
        }
        return true;
    }
    
}
