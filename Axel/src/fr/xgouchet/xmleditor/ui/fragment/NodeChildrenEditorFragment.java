package fr.xgouchet.xmleditor.ui.fragment;

import java.util.LinkedList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.tree.TreeNode;
import fr.xgouchet.xmleditor.data.xml.XmlData;
import fr.xgouchet.xmleditor.data.xml.XmlNodeStyler;
import fr.xgouchet.xmleditor.ui.adapter.NodeListAdapter;
import fr.xgouchet.xmleditor.ui.adapter.NodeViewListener;
import fr.xgouchet.xmleditor.ui.widget.FastScrollTrickListener;


public class NodeChildrenEditorFragment extends ANodeEditorFragment {
    
    private ListView mListView;
    private NodeListAdapter<XmlData> mAdapter;
    private NodeViewListener<XmlData> mNodeListener;
    
    
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
        
        View root = inflater.inflate(R.layout.fragment_node_children_editor, container, false);
        
        // Setup the listview
        mListView = (ListView) root.findViewById(android.R.id.list);
        
        // enable fast scroll
        mListView.setFastScrollEnabled(true);
        mListView.setOnScrollListener(new FastScrollTrickListener(mListView));
        
        // display the node's children
        setupChildrenListView();
        
        return root;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // GETTERS / SETTERS
    //////////////////////////////////////////////////////////////////////////////////////
    
    
    public void setNodeListener(final NodeViewListener<XmlData> nodeListener) {
        mNodeListener = nodeListener;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // UI SETUP
    //////////////////////////////////////////////////////////////////////////////////////
    
    private void setupChildrenListView() {
        
        // create the node list 
        List<TreeNode<XmlData>> list = new LinkedList<TreeNode<XmlData>>();
        if (!mNode.isDocument()) {
            list.add(mNode);
        }
        list.addAll(mNode.getChildren());
        
        // Display the tree 
        mAdapter = new NodeListAdapter<XmlData>(getActivity(), list, mNodeListener);
        mAdapter.setNodeStyler(new XmlNodeStyler());
        mAdapter.setHasRoot(!mNode.isDocument());
        mListView.setAdapter(mAdapter);
    }
    
    
}
