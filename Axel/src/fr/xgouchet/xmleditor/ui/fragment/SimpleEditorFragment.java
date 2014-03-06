package fr.xgouchet.xmleditor.ui.fragment;

import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.tree.TreeNode;
import fr.xgouchet.xmleditor.data.xml.XmlData;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.ui.adapter.NodeViewListener;
import fr.xgouchet.xmleditor.ui.widget.BreadCrumbsView;


/**
 * 
 * An editor displaying the XML as a list view, node by node (new view for Axel 3.x)
 * 
 * @author Xavier Gouchet
 * 
 */
public class SimpleEditorFragment extends ADocumentEditorFragment {
    
    
    private BreadCrumbsView mBreadCrumbsView;
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////
    // FRAGMENT LIFECYCLE
    //////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getFragmentManager().addOnBackStackChangedListener(mBackStackChangedListener);
    }
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        
        View root = inflater.inflate(R.layout.fragment_breadcrumb_editor, container, false);
        
        mBreadCrumbsView = (BreadCrumbsView) root.findViewById(R.id.bread_crumbs);
        
        return root;
    }
    
    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (mXmlRoot != null) {
            displayXmlRoot();
        }
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // OPTIONS MENU
    //////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        
        // TODO add options for the current node (add children, ...)
    }
    
    
    //////////////////////////////////////////////////////////////////////////////////////
    // XML EDITOR EVENTS
    //////////////////////////////////////////////////////////////////////////////////////
    
    
    @Override
    protected void displayXmlRoot() {
        if (getView() != null) {
            // Reset bread crumbs
            mBreadCrumbsView.clearBreadCrumbs();
            
            // display root
            displayNodeChildren(mXmlRoot, false);
        }
    }
    
    /**
     * Displays the children of the given node
     * 
     * @param node
     * @param addBreadCrumb
     */
    private void displayNodeChildren(final XmlNode node, final boolean addBreadCrumb) {
        
        // create the fragment node
        NodeChildrenEditorFragment fragment = new NodeChildrenEditorFragment();
        fragment.setXmlNode(node);
        fragment.setNodeListener(mNodeListener);
        
        
        // add node to bread crumb
        if (addBreadCrumb) {
            mBreadCrumbsView.push(node.getXPathName(), node);
        }
        
        displayNodeFragment(fragment, node.getXPathName(), node.getXPath());
        
    }
    
    
    /**
     * Displays the content of a node to be edited
     * 
     * @param node
     *            the node to display
     * @param
     */
    private void displayNodeEditor(final XmlNode node, final boolean addBreadCrumb) {
        
        ANodeEditorFragment fragment;
        
        // select next fragment
        switch (node.getContent().getType()) {
            case XmlData.XML_ELEMENT:
                fragment = new ElementEditorFragment();
                break;
            
            default:
                Log.w("SimpleEditor", "Unknown editor for node " + node);
                return;
        }
        
        // set the node
        fragment.setXmlNode(node);
        
        // add node to bread crumb
        if (addBreadCrumb) {
            mBreadCrumbsView.push(node.getXPathName(), node);
        }
        
        displayNodeFragment(fragment, node.getXPathName(), node.getXPath());
        
    }
    
    /**
     * Displays the given fragment and add it to the backstack
     * 
     * @param fragment
     *            the fragment
     * @param name
     *            the fragment name (for backstack use)
     */
    private void displayNodeFragment(final ANodeEditorFragment fragment, final String name,
            final String tag) {
        
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        
        transaction.replace(R.id.frame_sub_fragment, fragment, tag);
        
        if (!TextUtils.isEmpty(name)) {
            transaction.addToBackStack(name);
        }
        transaction.commit();
        
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // UI EVENT
    //////////////////////////////////////////////////////////////////////////////////////
    
    private NodeViewListener<XmlData> mNodeListener = new NodeViewListener<XmlData>() {
        
        @Override
        public void onNodeTapped(final TreeNode<XmlData> node, final View view, final int position) {
            if (node.hasChildren()) {
                if (node == mBreadCrumbsView.peek()) {
                    displayNodeEditor((XmlNode) node, false);
                } else {
                    displayNodeChildren((XmlNode) node, true);
                }
            } else {
                displayNodeEditor((XmlNode) node, true);
            }
        }
        
        @Override
        public void onNodeLongPressed(final TreeNode<XmlData> node, final View view,
                final int position) {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public void onNodeDoubleTapped(final TreeNode<XmlData> node, final View view,
                final int position) {
            // TODO Auto-generated method stub
            
        }
    };
    
    //////////////////////////////////////////////////////////////////////////////////////
    // FRAGMENT BACKSTACK LISTENER
    //////////////////////////////////////////////////////////////////////////////////////
    
    private OnBackStackChangedListener mBackStackChangedListener = new OnBackStackChangedListener() {
        
        @Override
        public void onBackStackChanged() {
            int count = getFragmentManager().getBackStackEntryCount();
            while (count < mBreadCrumbsView.getBreadCrumbsCount()) {
                mBreadCrumbsView.pop();
            }
        }
    };
}
