package fr.xgouchet.xmleditor.ui.widget;

import java.util.Stack;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.xgouchet.xmleditor.R;


/**
 * A view displaying a breadcrumb stack
 * 
 * @author Xavier Gouchet
 * 
 */
public class BreadCrumbsView extends HorizontalScrollView {
    
    
    private final LinearLayout mBreadCrumbsContainer;
    private final Stack<BreadCrumb> mBreadCrumbs = new Stack<BreadCrumb>();
    
    /**
     * Simple constructor to use when creating a view from code.
     * 
     * @param context
     *            The Context the view is running in, through which it can
     *            access the current theme, resources, etc.
     */
    public BreadCrumbsView(final Context context) {
        this(context, null, 0);
    }
    
    /**
     * Constructor that is called when inflating a view from XML. This is called
     * when a view is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.
     * 
     * <p>
     * The method onFinishInflate() will be called after all children have been added.
     * 
     * @param context
     *            The Context the view is running in, through which it can
     *            access the current theme, resources, etc.
     * @param attrs
     *            The attributes of the XML tag that is inflating the view.
     * @see #View(Context, AttributeSet, int)
     */
    public BreadCrumbsView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    
    /**
     * Perform inflation from XML and apply a class-specific base style. This
     * constructor of View allows subclasses to use their own base style when
     * they are inflating. For example, a Button class's constructor would call
     * this version of the super class constructor and supply <code>R.attr.buttonStyle</code> for
     * <var>defStyle</var>; this allows
     * the theme's button style to modify all of the base view attributes (in
     * particular its background) as well as the Button class's attributes.
     * 
     * @param context
     *            The Context the view is running in, through which it can
     *            access the current theme, resources, etc.
     * @param attrs
     *            The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr
     *            An attribute in the current theme that contains a
     *            reference to a style resource to apply to this view. If 0, no
     *            default style will be applied.
     * @see #View(Context, AttributeSet)
     */
    public BreadCrumbsView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        
        // create the container
        mBreadCrumbsContainer = new LinearLayout(context);
        mBreadCrumbsContainer.setOrientation(LinearLayout.HORIZONTAL);
        
        // add the linear layout container
        addView(mBreadCrumbsContainer);
        
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // BREAD CRUMBS ACTIONS
    //////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Pushes a new breadcrumb on the stack
     * 
     * @param title
     *            the title to display
     * @param item
     *            the underlying content
     */
    public void push(final String title, final Object item) {
        
        // create the bread crumb
        final BreadCrumb bc = new BreadCrumb();
        
        // set the metadata
        bc.title = title;
        bc.item = item;
        
        // inflate the layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        bc.view = (TextView) inflater.inflate(R.layout.item_breadcrumb, mBreadCrumbsContainer,
                false);
        
        // configure the view
        bc.view.setText(bc.title);
        bc.view.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(final View v) {
                onBreadCrumbClicked(bc);
            }
        });
        
        // add the bread crumb and view to their respective containers
        mBreadCrumbs.push(bc);
        mBreadCrumbsContainer.addView(bc.view);
    }
    
    /**
     * Clear all views in the bread crumb
     */
    public void clearBreadCrumbs() {
        mBreadCrumbs.clear();
        mBreadCrumbsContainer.removeAllViews();
    }
    
    /**
     * @return if we can still go up one level
     */
    public boolean canPop() {
        return mBreadCrumbs.size() > 1;
    }
    
    /**
     * Pops the current breadcrumb
     */
    public Object pop() {
        
        // check 
        if (mBreadCrumbs.isEmpty()) {
            throw new IllegalStateException();
        }
        
        // pop the first item
        BreadCrumb popped = mBreadCrumbs.pop();
        
        int count = mBreadCrumbsContainer.getChildCount();
        mBreadCrumbsContainer.removeViews(mBreadCrumbs.size(), 1);
        invalidate();
        
        Log.i("BCV", "Had " + count + " views, now has " + mBreadCrumbsContainer.getChildCount());
        
        return popped.item;
    }
    /**
     * @return the item displayed by the top of the stack breadcrumb
     */
    public Object peek() {
        if (mBreadCrumbs.isEmpty()) {
            return null;
        }
        return mBreadCrumbs.peek().item;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // BREAD CRUMBS EVENTS
    //////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Called when a bread crumb is clicked
     * 
     * @param bc
     */
    protected void onBreadCrumbClicked(final BreadCrumb bc) {
        
    }
    
    
    /**
     * A simple POJO representation of a breadcrumb data
     */
    private class BreadCrumb {
        
        public String title;
        public Object item;
        public TextView view;
    }
    
    
    
}
