package fr.xgouchet.xmleditor.ui.fragment;

import java.util.LinkedList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.common.AxelUtils;
import fr.xgouchet.xmleditor.data.tree.TreeNode;
import fr.xgouchet.xmleditor.data.xml.XmlData;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlNodeStyler;
import fr.xgouchet.xmleditor.ui.adapter.NodeListAdapter;
import fr.xgouchet.xmleditor.ui.adapter.NodeViewHolder;
import fr.xgouchet.xmleditor.ui.adapter.NodeViewListener;
import fr.xgouchet.xmleditor.ui.widget.FastScrollTrickListener;

public class ElementChildrenEditorFragment extends ANodeEditorFragment {

    private View mParentNodeView;
    private ListView mListView;
    private NodeListAdapter<XmlData> mAdapter;
    private NodeViewListener<XmlData> mNodeViewListener;
    private NodeViewListener<XmlData> mParentNodeViewListener;

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

        mParentNodeView = root.findViewById(R.id.parent_node);

        if (mXmlNode != null) {
            setupParentView();
            updateChildrenView();
        }

        return root;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // OPTIONS MENU
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (mXmlNode == null) {
            return;
        }

        inflater.inflate(R.menu.editor_element_children, menu);

        Menu submenu = menu.findItem(R.id.action_add_child).getSubMenu();
        submenu.setGroupVisible(R.id.action_group_common_add_child, true);
        submenu.setGroupVisible(R.id.action_group_document_add_child, mXmlNode.isDocument());
        submenu.setGroupVisible(R.id.action_group_element_add_child, mXmlNode.isElement());

        if (mXmlNode.isDocument()) {
            submenu.findItem(R.id.action_add_child_doctype).setEnabled(!mXmlNode.hasDoctype());
            submenu.findItem(R.id.action_add_child_element).setEnabled(!mXmlNode.hasRootChild());
        }

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        // probably an Add Node action
        XmlNode child = AxelUtils.createXmlNode(item.getItemId());
        if (child != null) {
            mXmlEditor.addChildToNode(mXmlNode, child, true);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // GETTERS / SETTERS
    // ////////////////////////////////////////////////////////////////////////////////////

    public void setNodeListener(final NodeViewListener<XmlData> nodeListener) {
        mNodeViewListener = nodeListener;
    }

    public void setParentNodeListener(final NodeViewListener<XmlData> parentNodeViewListener) {
        mParentNodeViewListener = parentNodeViewListener;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // UI SETUP
    //////////////////////////////////////////////////////////////////////////////////////

    private void setupParentView() {

        if (mXmlNode.isDocument()) {
            mParentNodeView.setVisibility(View.GONE);
        }

        NodeViewHolder<XmlData> holder = new NodeViewHolder<XmlData>(mParentNodeView,
                getActivity(), mParentNodeViewListener);

        holder.position = 0;
        holder.node = mXmlNode;

        mParentNodeView.setTag(holder);

        updateParentView();

    }

    private void updateParentView() {

        @SuppressWarnings("unchecked")
        NodeViewHolder<XmlData> holder = (NodeViewHolder<XmlData>) mParentNodeView.getTag();
        holder.displayNode(new XmlNodeStyler(), getActivity(), 0);
    }

    /**
     * 
     */
    private void updateChildrenView() {

        // get the node list
        List<TreeNode<XmlData>> list = new LinkedList<TreeNode<XmlData>>();
        list.addAll(mXmlNode.getChildren());

        // Display the tree
        mAdapter = new NodeListAdapter<XmlData>(getActivity(), list, mNodeViewListener);
        mAdapter.setNodeStyler(new XmlNodeStyler());
        mListView.setAdapter(mAdapter);

    }

    //////////////////////////////////////////////////////////////////////////////////////
    // XML EDITOR EVENTS
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onXmlContentChanged() {
        updateChildrenView();
        updateParentView();
    }
}
