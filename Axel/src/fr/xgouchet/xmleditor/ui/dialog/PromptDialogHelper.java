package fr.xgouchet.xmleditor.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import fr.xgouchet.xmleditor.R;

/**
 * A Utility class used to show various prompts to the user
 * 
 * @author Xavier Gouchet
 * 
 */
public class PromptDialogHelper {

    public static final int PROMPT_HTML_PARSE_ERROR = 1;
    public static final int PROMPT_XML_PARSE_ERROR = 2;
    public static final int PROMPT_DIRTY_ACTION = 3;

    public static final int CHOICE_CANCEL_IGNORE = -1;
    public static final int CHOICE_PARSE_HTML_SOUP = 1;
    public static final int CHOICE_W3C_VALIDATION = 2;

    public static final int CHOICE_SAVE = 10;
    public static final int CHOICE_DONT_SAVE = 11;

    public interface PromptListener {

        void onPromptEvent(int id, int choice, Object result);
    }

    /**
     * Prompts the user to choose what to do with an HTML file with parse issues
     * 
     * @param context
     * @param listener
     */
    public static void promptHtmlParseErrorAction(final Context context,
            final PromptListener promptListener) {
        final AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.ic_dialog_alert);
        builder.setTitle(R.string.ui_open_error);
        builder.setCancelable(true);
        builder.setMessage(context.getString(R.string.ui_prompt_open_error_html));

        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                int choice;
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        choice = CHOICE_PARSE_HTML_SOUP;
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        choice = CHOICE_W3C_VALIDATION;
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                    default:
                        choice = CHOICE_CANCEL_IGNORE;
                        break;
                }

                promptListener.onPromptEvent(PROMPT_HTML_PARSE_ERROR, choice, null);
            }
        };

        // setup buttons
        builder.setPositiveButton(R.string.ui_convert_html, listener);
        builder.setNeutralButton(R.string.ui_check_errors, listener);
        builder.setNegativeButton(R.string.ui_cancel, listener);

        builder.create().show();
    }

    /**
     * Prompts the user to choose what to do with an XML file with parse issues
     * 
     * @param context
     * @param listener
     */
    public static void promptXmlParseErrorAction(final Context context,
            final PromptListener promptListener) {
        final AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.ic_dialog_alert);
        builder.setTitle(R.string.ui_open_error);
        builder.setCancelable(true);
        builder.setMessage(context.getString(R.string.ui_prompt_open_error_xml));

        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                int choice;
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        choice = CHOICE_W3C_VALIDATION;
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                    default:
                        choice = CHOICE_CANCEL_IGNORE;
                        break;
                }

                promptListener.onPromptEvent(PROMPT_XML_PARSE_ERROR, choice, null);
            }
        };

        // setup buttons
        builder.setPositiveButton(R.string.ui_check_errors, listener);
        builder.setNegativeButton(R.string.ui_cancel, listener);

        builder.create().show();
    }

    /**
     * Prompts the user what to do with the current (dirty) document before
     * doing an action that could loose the data
     * 
     * @param context
     * @param promptListener
     */
    public static void promptDirtyDocumentAction(final Context context,
            final PromptListener promptListener, final boolean allowNoSave) {

        final AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.ui_save_text);

        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                int choice;
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        choice = CHOICE_SAVE;
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        choice = CHOICE_DONT_SAVE;
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                    default:
                        choice = CHOICE_CANCEL_IGNORE;
                        break;
                }

                promptListener.onPromptEvent(PROMPT_DIRTY_ACTION, choice, null);
            }
        };

        builder.setPositiveButton(R.string.ui_save, listener);
        builder.setNegativeButton(R.string.ui_cancel, listener);
        if (allowNoSave) {
            builder.setNeutralButton(R.string.ui_no_save, listener);
        }

        builder.create().show();
    }

}
