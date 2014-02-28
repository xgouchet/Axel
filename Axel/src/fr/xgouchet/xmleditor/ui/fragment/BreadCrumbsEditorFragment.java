package fr.xgouchet.xmleditor.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.xml.XmlData;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlNodeStyler;
import fr.xgouchet.xmleditor.ui.adapter.NodeListAdapter;
import fr.xgouchet.xmleditor.ui.widget.BreadCrumbsView;


/**
 * 
 * An editor displaying the XML as a tree view (new view for Axel 3.x)
 * 
 * @author Xavier Gouchet
 * 
 */
public class BreadCrumbsEditorFragment extends AEditorFragment {
    
    
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
            
            mCurrentNode = mXmlRoot;
            
            // Reset bread crumbs
            mBreadCrumbsView.clearBreadCrumbs();
            mBreadCrumbsView.addBreadCrumb(mXmlRoot.getContent().getShortName(), 0, mXmlRoot);
            
            // Display the tree 
            mAdapter = new NodeListAdapter<XmlData>(getActivity(), mXmlRoot.getChildren());
            mAdapter.setNodeStyler(new XmlNodeStyler());
            mListView.setAdapter(mAdapter);
        }
    }
    
    @Override
    public void onXmlContentChanged() {
        // TODO Auto-generated method stub
        
    }
    
    
}
