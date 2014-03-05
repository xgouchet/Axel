package fr.xgouchet.xmleditor.ui.adapter;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import fr.xgouchet.xmleditor.R;
import fr.xgouchet.xmleditor.data.xml.XmlAttribute;
import fr.xgouchet.xmleditor.data.xml.XmlNode;
import fr.xgouchet.xmleditor.data.xml.XmlNodeStyler;
import fr.xgouchet.xmleditor.ui.dialog.AttributeEditDialog;


/**
 * TODO handle interactions (Edit / Delete)
 */
public class XmlAttributeAdapter extends ArrayAdapter<XmlAttribute> {
    
    /**
     * @param context
     *            the current application context
     * @param list
     *            the list to adapt
     * @param node
     *            the node containing those attributes
     */
    public XmlAttributeAdapter(final Context context,
            final List<XmlAttribute> list, final XmlNode node) {
        super(context, R.layout.item_attribute, list);
        mInflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mList = list;
        mNode = node;
    }
    
    /**
     * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(final int position, final View convertView,
            final ViewGroup parent) {
        View view = convertView;
        
        if (view == null) {
            view = mInflater.inflate(R.layout.item_attribute, parent, false);
        }
        
        final XmlAttribute attr = getItem(position);
        
        if (attr != null) {
            TextView text;
            text = (TextView) view.findViewById(R.id.textAttribute);
            text.setText(XmlNodeStyler.getAttributeSpan(attr, getContext()),
                    BufferType.SPANNABLE);
            
            text.setHorizontallyScrolling(true);
            text.setMovementMethod(new ScrollingMovementMethod());
            text.scrollTo(0, 0);
            
//			view.findViewById(R.id.buttonDelete).setOnClickListener(
//					new OnClickListener() {
//						public void onClick(final View view) {
//							promptDeleteAttribute(attr);
//						}
//					});
//
//			view.findViewById(R.id.buttonEdit).setOnClickListener(
//					new OnClickListener() {
//						public void onClick(final View view) {
//							promptEditAttribute(attr);
//						}
//					});
        }
        
        return view;
    }
    
    /**
     * @param attr
     *            the attribute to delete
     */
    protected void promptDeleteAttribute(final XmlAttribute attr) {
        AlertDialog.Builder builder;
        
        builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.ui_delete);
        builder.setCancelable(true);
        builder.setMessage("Are you sure you want to delete this attribute ?");
        
        builder.setPositiveButton(R.string.ui_delete,
                new DialogInterface.OnClickListener() {
                    
                    @Override
                    public void onClick(final DialogInterface dialog,
                            final int which) {
                        remove(attr);
                        notifyDataSetChanged();
                    }
                });
        builder.setNegativeButton(R.string.ui_cancel,
                new DialogInterface.OnClickListener() {
                    
                    @Override
                    public void onClick(final DialogInterface dialog,
                            final int which) {
                        // cancel
                    }
                });
        
        builder.create().show();
    }
    
    /**
     * @param attr
     *            the attribute to edit
     */
    protected void promptEditAttribute(final XmlAttribute attr) {
        AttributeEditDialog dlg;
        
        dlg = new AttributeEditDialog(getContext(), mInflater, attr);
        dlg.setNode(mNode);
        dlg.setSiblingsAttribute(mList);
        
        dlg.setOnDismissListener(new OnDismissListener() {
            
            @Override
            public void onDismiss(final DialogInterface dialog) {
                notifyDataSetChanged();
            }
        });
        dlg.show();
    }
    
    /**
     * Edits the given attribute
     * 
     * @param attr
     *            the attribute to edit
     */
    public void editAttribute(final XmlAttribute attr) {
        if (mList.contains(attr)) {
            promptEditAttribute(attr);
        }
    }
    
    private final LayoutInflater mInflater;
    private final List<XmlAttribute> mList;
    private final XmlNode mNode;
    
}
