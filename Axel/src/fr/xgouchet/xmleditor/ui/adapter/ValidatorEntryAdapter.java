package fr.xgouchet.xmleditor.ui.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.parser.validator.ValidatorEntry;

public class ValidatorEntryAdapter extends BaseExpandableListAdapter {

	private final Context mContext;
	private final LayoutInflater mInflater;
	private final List<ValidatorEntry> mEntries;

	/**
	 * 
	 * @param context
	 * @param entries
	 */
	public ValidatorEntryAdapter(final Context context,
			final List<ValidatorEntry> entries) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mEntries = entries;
	}

	@Override
	public int getGroupCount() {
		return mEntries.size();
	}

	@Override
	public long getGroupId(final int groupPosition) {
		return ((groupPosition | 0L) << 32);
	}

	@Override
	public ValidatorEntry getGroup(final int groupPosition) {
		return mEntries.get(groupPosition);
	}

	@Override
	public View getGroupView(final int groupPosition, final boolean isExpanded,
			final View convertView, final ViewGroup parent) {

		View v = convertView;
		if (v == null) {
			v = mInflater.inflate(R.layout.item_validator, parent, false);
		}

		ValidatorEntry entry = getGroup(groupPosition);

		int color;
		switch (entry.getType()) {
		case ERROR:
			color = Color.rgb(231, 76, 60);
			break;
		case WARNING:
			color = Color.rgb(241, 196, 15);
			break;
		default:
			color = Color.argb(0, 0, 0, 0);
			break;
		}

		v.findViewById(android.R.id.custom).setBackgroundColor(color);

		TextView text = (TextView) v.findViewById(android.R.id.text1);
		text.setText(entry.getMessage());

		return v;
	}

	@Override
	public Object getChild(final int arg0, final int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getChildId(final int arg0, final int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition,
			final boolean isLastChild, final View convertView,
			final ViewGroup parent) {

		View v = convertView;
		if (v == null) {
			v = mInflater.inflate(R.layout.item_validator_full, parent, false);
		}

		ValidatorEntry entry = getGroup(groupPosition);
		TextView lineCols = (TextView) v.findViewById(android.R.id.text1);
		TextView message = (TextView) v.findViewById(android.R.id.message);

		lineCols.setText(mContext.getString(R.string.ui_validator_line_columns,
				entry.getLine(), entry.getColumn()));
		message.setText(entry.getMessage());

		return v;
	}

	@Override
	public int getChildrenCount(final int groupPosition) {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(final int groupPosition,
			final int childPosition) {
		return false;
	}

}
