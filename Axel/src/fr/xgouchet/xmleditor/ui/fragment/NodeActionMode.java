package fr.xgouchet.xmleditor.ui.fragment;

import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.XmlEditor;
import fr.xgouchet.xmleditor.data.xml.XmlData;
import fr.xgouchet.xmleditor.data.xml.XmlNode;

public class NodeActionMode implements Callback {

    private static final int[] WRITE_ACTIONS = new int[] { R.id.action_node_add_child,
            R.id.action_node_comment, R.id.action_node_cut, R.id.action_node_paste,
            R.id.action_node_delete, R.id.action_node_add_attribute };

    private XmlEditor mXmlEditor;
    private XmlNode mTargetNode;

    public void setXmlEditor(final XmlEditor xmlEditor) {
        mXmlEditor = xmlEditor;
    }

    public void setTargetNode(final XmlNode targetNode) {
        mTargetNode = targetNode;
    }

    private boolean mActionModeDisplayed;

    public boolean isActionModeDisplayed() {
        return mActionModeDisplayed;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // ACTION MODE IMPLEMENTATION
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {

        if ((mTargetNode == null) || (mTargetNode.getContent() == null)) {
            return false;
        }

        // choose the menu
        int actionMenu;
        switch (mTargetNode.getContent().getType()) {
            case XmlData.XML_ELEMENT:
                actionMenu = R.menu.action_element;
                break;
            default:
                return false;
        }

        // inflate the menu
        mode.getMenuInflater().inflate(actionMenu, menu);

        // if needed remove write actions
        if (mXmlEditor.isReadOnly()) {
            for (int id : WRITE_ACTIONS) {
                MenuItem item = menu.findItem(id);
                if (item != null) {
                    item.setVisible(false);
                }
            }
        }

        mActionModeDisplayed = true;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {

        // stops the action mode
        mode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(final ActionMode mode) {
        mActionModeDisplayed = false;
    }

}
