package fr.xgouchet.xmleditor.ui.fragment;

import java.util.LinkedList;
import java.util.List;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.tree.TreeNode;
import fr.xgouchet.xmleditor.data.xml.XmlData;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlNodeStyler;
import fr.xgouchet.xmleditor.ui.adapter.NodeListAdapter;
import fr.xgouchet.xmleditor.ui.adapter.NodeViewListener;
import fr.xgouchet.xmleditor.ui.widget.BreadCrumbsView;
import fr.xgouchet.xmleditor.ui.widget.FastScrollTrickListener;


/**
 * 
 * An editor displaying the XML as a list view, node by node (new view for Axel 3.x)
 * 
 * @author Xavier Gouchet
 * 
 */
public class SimpleEditorFragment extends ADocumentEditorFragment {
    
    
    private ANodeEditorFragment mNodeEditorFragment;
    private BreadCrumbsView mBreadCrumbsView;
    private ListView mListView;
    private NodeListAdapter<XmlData> mAdapter;
    
    
    //////////////////////////////////////////////////////////////////////////////////////
    // FRAGMENT LIFECYCLE
    //////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        
        View root = inflater.inflate(R.layout.fragment_breadcrumb_editor, container, false);
        
        mBreadCrumbsView = (BreadCrumbsView) root.findViewById(R.id.bread_crumbs);
        
        mListView = (ListView) root.findViewById(android.R.id.list);
        
        // enable fast scroll
        mListView.setFastScrollEnabled(true);
        mListView.setOnScrollListener(new FastScrollTrickListener(mListView));
        
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
    // USER INTERACTIONS
    //////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public boolean onBackPressed() {
        
        // check if the bread crumb can be popped
        if (mBreadCrumbsView.canPop()) {
            
            XmlNode popped = null;
            
            if (mNodeEditorFragment != null) {
                // get the edited node 
                popped = mNodeEditorFragment.getXmlNode();
                // pop the sub fragment  
                getFragmentManager().popBackStack();
                
                mNodeEditorFragment = null;
            }
            
            if ((popped == null) || (popped != mBreadCrumbsView.peek()) || (!popped.hasChildren())) {
                // pop the bread crumbs
                popped = (XmlNode) mBreadCrumbsView.pop();
            }
            
            // update the display
            XmlNode current = (XmlNode) mBreadCrumbsView.peek();
            
            // check for null 
            if (current == null) {
                return true;
            }
            
            // Update the display
            if (current.isElement() || current.isDocument()) {
                displayNodeChildren(current, false);
            } else {
                displayNodeEditor(current, false);
            }
            
            return true;
        }
        
        
        return false;
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
            displayNodeChildren(mXmlRoot, true);
        }
    }
    
    
    /**
     * Displays the content of a node to be edited
     * 
     * @param node
     *            the node to display
     * @param
     */
    private void displayNodeEditor(final XmlNode node, final boolean addBreadCrumb) {
        
        // TODO maybe check  if mNodeEditorFragment is not null ? 
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
        
        // display fragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.frame_sub_fragment, fragment);
        transaction.addToBackStack(node.getXPath());
        transaction.commit();
        
        // hide node list 
        mListView.setVisibility(View.GONE);
        
        // save the fragment
        mNodeEditorFragment = fragment;
    }
    
    protected void displayNodeChildren(final XmlNode node, final boolean addBreadCrumb) {
        if (getView() != null) {
            
            if (addBreadCrumb) {
                // add node to bread crumb
                mBreadCrumbsView.push(node.getXPathName(), node);
            }
            
            // create the node list 
            List<TreeNode<XmlData>> list = new LinkedList<TreeNode<XmlData>>();
            if (!node.isDocument()) {
                list.add(node);
            }
            list.addAll(node.getChildren());
            
            // Display the tree 
            mAdapter = new NodeListAdapter<XmlData>(getActivity(), list, mNodeListener);
            mAdapter.setNodeStyler(new XmlNodeStyler());
            mAdapter.setHasRoot(!node.isDocument());
            mListView.setAdapter(mAdapter);
            mListView.setVisibility(View.VISIBLE);
        }
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
}
