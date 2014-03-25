package fr.xgouchet.xmleditor.ui.fragment;

import android.app.FragmentManager;
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
import android.widget.TextView;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.tree.TreeNode;
import fr.xgouchet.xmleditor.data.xml.XmlData;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.ui.adapter.NodeViewListener;

/**
 * 
 * An editor displaying the XML as a list view, node by node (new view for Axel
 * 3.x)
 * 
 * @author Xavier Gouchet
 * 
 */
public class SimpleEditorFragment extends ADocumentEditorFragment {

    private TextView mTextXPath;
    private final NodeActionMode mActionModeCallback = new NodeActionMode();

    //////////////////////////////////////////////////////////////////////////////////////
    // FRAGMENT LIFECYCLE
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_breadcrumb_editor, container, false);

        mTextXPath = (TextView) root.findViewById(R.id.text_xpath);

        getFragmentManager().addOnBackStackChangedListener(mBackStackChangedListener);

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

            // Clear fragment backstack
            FragmentManager fm = getFragmentManager();
            while (fm.getBackStackEntryCount() > 0) {
                fm.popBackStackImmediate();
            }

            // display root
            displayNodeChildren(mXmlRoot);
        }
    }

    /**
     * Displays the children of the given node
     * 
     * @param node
     */
    private void displayNodeChildren(final XmlNode node) {

        // create the fragment node
        ElementChildrenEditorFragment fragment = new ElementChildrenEditorFragment();
        fragment.setXmlNode(node);
        fragment.setNodeListener(mNodeListener);
        fragment.setParentNodeListener(mParentNodeListener);
        fragment.setXmlEditor(mXmlEditor);

        displayNodeFragment(fragment, node.getXPathName(), node.getXPath());

    }

    /**
     * Displays the content of a node to be edited
     * 
     * @param node
     *            the node to display
     */
    private void displayNodeEditor(final XmlNode node) {

        ANodeEditorFragment fragment;

        // select next fragment
        switch (node.getContent().getType()) {
            case XmlData.XML_ELEMENT:
                fragment = new ElementNodeEditorFragment();
                break;
            case XmlData.XML_TEXT:
                fragment = new TextNodeEditorFragment();
                break;
            case XmlData.XML_CDATA:
                fragment = new CDataNodeEditorFragment();
                break;
            case XmlData.XML_COMMENT:
                fragment = new CommentNodeEditorFragment();
                break;
            case XmlData.XML_PROCESSING_INSTRUCTION:
                fragment = new ProcessingInstructionNodeEditorFragment();
                break;
            case XmlData.XML_DOCTYPE:
                fragment = new DoctypeNodeEditorFragment();
                break;
            case XmlData.XML_DOCUMENT_DECLARATION:
                fragment = new DocumentDeclarationNodeEditorFragment();
                break;
            default:
                Log.w("SimpleEditor", "Unknown editor for node " + node);
                return;
        }

        // set the node
        fragment.setXmlNode(node);
        fragment.setXmlEditor(mXmlEditor);

        displayNodeFragment(fragment, node.getXPathName(), node.getXPath());

    }

    /**
     * Displays the given fragment and add it to the backstack
     * 
     * @param fragment
     *            the fragment
     * @param name
     *            the fragment name (for backstack use)
     * @param the
     *            full unique xpath of the node
     */
    private void displayNodeFragment(final ANodeEditorFragment fragment, final String name,
            final String xpath) {

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.replace(R.id.frame_sub_fragment, fragment, xpath);

        if (!TextUtils.isEmpty(name)) {
            transaction.addToBackStack(xpath);
        }
        transaction.commit();

    }

    //////////////////////////////////////////////////////////////////////////////////////
    // NODE EVENTS LISTENERS
    //////////////////////////////////////////////////////////////////////////////////////

    private final NodeViewListener<XmlData> mNodeListener = new NodeViewListener<XmlData>() {

        @Override
        public void onNodeTapped(final TreeNode<XmlData> node, final View view, final int position) {
            if (!mActionModeCallback.isActionModeDisplayed()) {
                if (node.getContent().isElement()) {
                    displayNodeChildren((XmlNode) node);
                } else {
                    if (!mXmlEditor.isReadOnly()) {
                        displayNodeEditor((XmlNode) node);
                    }
                }
            }
        }

        @Override
        public void onNodeLongPressed(final TreeNode<XmlData> node, final View view,
                final int position) {
            if (!mActionModeCallback.isActionModeDisplayed()) {
                mActionModeCallback.setTargetNode((XmlNode) node);
                mActionModeCallback.setXmlEditor(mXmlEditor);
                getActivity().startActionMode(mActionModeCallback);
            }
        }

        @Override
        public void onNodeDoubleTapped(final TreeNode<XmlData> node, final View view,
                final int position) {
        }
    };

    private final NodeViewListener<XmlData> mParentNodeListener = new NodeViewListener<XmlData>() {

        @Override
        public void onNodeTapped(final TreeNode<XmlData> node, final View view, final int position) {
            if (!mActionModeCallback.isActionModeDisplayed()) {
                if (!mXmlEditor.isReadOnly()) {
                    displayNodeEditor((XmlNode) node);
                }
            }
        }

        @Override
        public void onNodeLongPressed(final TreeNode<XmlData> node, final View view,
                final int position) {
            if (!mActionModeCallback.isActionModeDisplayed()) {
                mActionModeCallback.setTargetNode((XmlNode) node);
                mActionModeCallback.setXmlEditor(mXmlEditor);
                getActivity().startActionMode(mActionModeCallback);
            }
        }

        @Override
        public void onNodeDoubleTapped(final TreeNode<XmlData> node, final View view,
                final int position) {

        }
    };

    //////////////////////////////////////////////////////////////////////////////////////
    // FRAGMENT BACKSTACK LISTENERS
    //////////////////////////////////////////////////////////////////////////////////////

    private final OnBackStackChangedListener mBackStackChangedListener = new OnBackStackChangedListener() {

        @Override
        public void onBackStackChanged() {

            ANodeEditorFragment fragment = (ANodeEditorFragment) getFragmentManager()
                    .findFragmentById(R.id.frame_sub_fragment);
            if (fragment == null) {
                return;
            }

            mTextXPath.setText(fragment.getXmlNode().getXPath());
        }
    };

}
