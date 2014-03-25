package fr.xgouchet.xmleditor.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import fr.xgouchet.xmleditor.R;

/**
 * A utility class to create toasts that provide an undo button
 */
public class UndoableToast {

    public static void showUndoableToast(final Context context, final CharSequence message,
            final Runnable undoAction) {

        Toast toast = new Toast(context);

        // inflate custom view
        LayoutInflater inflate = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.undoable_notification, null);

        // set the message
        TextView tv = (TextView) v.findViewById(android.R.id.message);
        tv.setText(message);

        // set the action
        View uv = v.findViewById(R.id.notification_undo);
        uv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                undoAction.run();
            }
        });

        toast.setView(v);
        
        toast.setDuration(Toast.LENGTH_LONG);

        toast.show();
    }
}
