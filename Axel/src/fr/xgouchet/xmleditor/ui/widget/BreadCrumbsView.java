package fr.xgouchet.xmleditor.ui.widget;

import java.util.Stack;

import fr.xgouchet.xmleditor.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


public class BreadCrumbsView extends ScrollView {
    
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
        
        addView(mBreadCrumbsContainer);
        
        if (isInEditMode()) {
            addBreadCrumb("Root", 0, null);
            addBreadCrumb("child", 1, null);
            addBreadCrumb("child", 2, null);
        }
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // Bread Crumbs
    //////////////////////////////////////////////////////////////////////////////////////
    
    
    public void addBreadCrumb(final String title, final int id, final Object tag) {
        final BreadCrumb bc = new BreadCrumb();
        
        bc.title = title;
        bc.id = id;
        bc.tag = tag;
        
        LayoutInflater inflater = LayoutInflater.from(getContext());
        bc.view = (TextView) inflater.inflate(R.layout.item_breadcrumb, mBreadCrumbsContainer,
                false);
        bc.view.setText(title);
        bc.view.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(final View v) {
                onBreadCrumbClicked(bc);
            }
        });
        
        mBreadCrumbs.push(bc);
        mBreadCrumbsContainer.addView(bc.view);
    }
    
    public void clearBreadCrumbs() {
        mBreadCrumbs.clear();
        mBreadCrumbsContainer.removeAllViews();
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // BREAD CRUMBS EVENTS
    //////////////////////////////////////////////////////////////////////////////////////
    
    protected void onBreadCrumbClicked(final BreadCrumb bc) {
        
    }
    
    
    private class BreadCrumb {
        
        public String title;
        public int id;
        public Object tag;
        public TextView view;
    }
    
    
    
}
