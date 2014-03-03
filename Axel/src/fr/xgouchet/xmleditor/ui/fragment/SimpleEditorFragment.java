package fr.xgouchet.xmleditor.ui.fragment;

import android.os.Bundle;
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


/**
 * 
 * An editor displaying the XML as a list view, node by node (new view for Axel 3.x)
 * 
 * @author Xavier Gouchet
 * 
 */
public class SimpleEditorFragment extends AEditorFragment {
    
    
    private BreadCrumbsView mBreadCrumbsView;
    private ListView mListView;
    private NodeListAdapter<XmlData> mAdapter;
    
    private XmlNode mCurrentNode;
    
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
            displayNode(mXmlRoot);
        }
    }
    
    protected void displayNode(final XmlNode node) {
        if (getView() != null) {
            
            mCurrentNode = node;
            
            // add node to bread crumb
            mBreadCrumbsView.addBreadCrumb(node.getXPathName(), 0, node);
            
            // Display the tree 
            mAdapter = new NodeListAdapter<XmlData>(getActivity(), node.getChildren(),
                    mNodeListener);
            mAdapter.setNodeStyler(new XmlNodeStyler());
            mListView.setAdapter(mAdapter);
        }
    }
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////
    // UI EVENT
    //////////////////////////////////////////////////////////////////////////////////////
    
    private NodeViewListener<XmlData> mNodeListener = new NodeViewListener<XmlData>() {
        
        @Override
        public void onNodeTapped(final TreeNode<XmlData> node, final View view, final int position) {
            if (node.hasChildren()) {
                displayNode((XmlNode) node);
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
