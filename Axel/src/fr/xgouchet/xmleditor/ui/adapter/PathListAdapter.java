package fr.xgouchet.xmleditor.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fr.xgouchet.xmleditor.R;

/**
 * A File List Adapter
 * 
 * @author x.gouchet
 * 
 */
public class PathListAdapter extends ArrayAdapter<String> {

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The current context
	 * @param objects
	 *            The objects to represent in the ListView.
	 */
	public PathListAdapter(final Context context, final List<String> objects) {
		super(context, R.layout.item_file, objects);
		mInflater = LayoutInflater.from(context);
	}

	/**
	 * @see ArrayAdapter#getView(int, View, ViewGroup)
	 */
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {
		View view;
		String path;
		TextView compound;

		// recycle view
		view = convertView;
		if (view == null) {
			view = mInflater.inflate(R.layout.item_file, null);
		}

		// get displayed file and current view
		path = getItem(position);

		// set the layout content
		compound = (TextView) view.findViewById(R.id.textFileName);
		if (compound != null) {
			if (path == null) {
				compound.setText("");
				compound.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.file_unknown, 0, 0, 0);
			} else {
				compound.setText(path);
				compound.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.file, 0, 0, 0);
			}
		}
		return view;
	}

	private final LayoutInflater mInflater;
}
