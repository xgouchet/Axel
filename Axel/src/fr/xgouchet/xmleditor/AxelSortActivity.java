package fr.xgouchet.xmleditor;

import java.util.LinkedList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

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
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED);

		mNode = ((AxelApplication) getApplication()).getCurrentSelection();

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

		if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
			setupDoneDiscardActionBar();
		} else {
			setupDoneDiscardButtons();
		}

	}

	private void onDiscard() {
		setResult(RESULT_CANCELED);
		finish();
	}

	private void onApply() {
		if (validateModifications()) {
			applyModifications();
			setResult(RESULT_OK, new Intent());
			finish();
		}
	}

	private void setupDoneDiscardButtons() {
		// setup buttons
		findViewById(R.id.buttonCancel).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(final View v) {
						onDiscard();
					}
				});

		findViewById(R.id.buttonOk).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				onApply();
			}
		});
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void setupDoneDiscardActionBar() {
		LayoutInflater inflater;

		if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
			inflater = (LayoutInflater) getActionBar().getThemedContext()
					.getSystemService(LAYOUT_INFLATER_SERVICE);
		} else {
			inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		}

		final View custom = inflater.inflate(R.layout.ab_done_discard, null);

		custom.findViewById(R.id.buttonDiscard).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(final View v) {
						onDiscard();
					}
				});
		custom.findViewById(R.id.buttonDone).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(final View v) {
						onApply();
					}
				});

		// Show the custom action bar view and hide the normal Home icon and
		// title.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
						| ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setCustomView(custom, new ActionBar.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
	}

	/**
	 * @see com.mobeta.android.dslv.DragSortListView.DropListener#drop(int, int)
	 */
	@Override
	public void drop(final int from, final int to) {
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
