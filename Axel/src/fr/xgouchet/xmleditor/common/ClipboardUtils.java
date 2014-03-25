package fr.xgouchet.xmleditor.common;

import java.util.LinkedList;
import java.util.List;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardUtils {

    /**
     * Sets the text content of the clipboard with the given data
     * 
     * @param text
     *            the complete content of the data
     * @param label
     *            the user visible title of the data
     * @param context
     *            the current context
     */
    public static void setText(final String text, final String label, final Context context) {
        getManager(context).setPrimaryClip(ClipData.newPlainText(label, text));
    }

    /**
     * Gets the content of the primary data in the clipboard (assuming it's text data)
     * 
     * @param context
     *            the current context
     * @return the list of string data in the primary clip
     */
    public static String[] getText(final Context context) {
        ClipData.Item item;
        List<String> content = new LinkedList<String>();
        ClipData data = getManager(context).getPrimaryClip();
        CharSequence strData;

        if (data != null) {
            int count = data.getItemCount();
            for (int i = 0; i < count; ++i) {
                item = data.getItemAt(i);

                strData = item.getText();
                if (strData != null) {
                    content.add(strData.toString());
                    continue;
                }
            }
        }

        return content.toArray(new String[content.size()]);
    }

    /**
     * @param context
     * @return the current manager
     */
    private static ClipboardManager getManager(final Context context) {
        return (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }
}
