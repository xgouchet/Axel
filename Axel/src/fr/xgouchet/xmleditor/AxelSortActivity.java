package fr.xgouchet.xmleditor;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;

import fr.xgouchet.xmleditor.data.tree.TreeNode;
import fr.xgouchet.xmleditor.data.xml.XmlData;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlNodeStyler;
import fr.xgouchet.xmleditor.ui.adapter.NodeAdapter;

/**
 * 
 */
public class AxelSortActivity extends Activity implements DropListener {

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mNode = ((AxelApplication) getApplication()).mCurrentSelection;

		setContentView(R.layout.layout_sort);
		setTitle(getString(R.string.title_editor, mNode.toString()));

		// mirror children
		mChildren = new LinkedList<TreeNode<XmlData>>();
		mChildren.addAll(mNode.getChildren());

		// create adapter
		mNodeAdapter = new NodeAdapter<XmlData>(this, mChildren);
		mNodeAdapter.setNodeStyler(new XmlNodeStyler());

		// setup list view
		mListView = (DragSortListView) findViewById(android.R.id.list);
		mListView.setDropListener(this);
		mListView.setAdapter(mNodeAdapter);

		// setup buttons
		findViewById(R.id.buttonCancel).setOnClickListener(
				new OnClickListener() {
					public void onClick(View v) {
						finish();
					}
				});

		findViewById(R.id.buttonOk).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (validateModifications()) {
					applyModifications();
					finish();
				}
			}
		});
	}

	/**
	 * @see com.mobeta.android.dslv.DragSortListView.DropListener#drop(int, int)
	 */
	public void drop(int from, int to) {
		TreeNode<XmlData> node = mChildren.remove(from);
		mChildren.add(to, node);

		mNodeAdapter.notifyDataSetChanged();
	}

	/**
	 * @return if the modifications can be applied
	 */
	protected boolean validateModifications() {
		return true;
	}

	/**
	 * Apply the modification to the underlying data
	 */
	protected void applyModifications() {
		mNode.getChildren().clear();
		mNode.getChildren().addAll(mChildren);
	}

	/** */
	protected DragSortListView mListView;

	/** */
	protected NodeAdapter<XmlData> mNodeAdapter;
	/** */
	protected XmlNode mNode;
	/** */
	protected List<TreeNode<XmlData>> mChildren;

}
