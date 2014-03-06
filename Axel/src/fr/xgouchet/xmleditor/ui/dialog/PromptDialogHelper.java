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
    
    public static final int CHOICE_CANCEL_IGNORE = -1;
    public static final int CHOICE_PARSE_HTML_SOUP = 1;
    public static final int CHOICE_W3C_VALIDATION = 2;
    
    
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
            public void onClick(DialogInterface dialog, int which) {
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
            public void onClick(DialogInterface dialog, int which) {
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
                
                promptListener.onPromptEvent(PROMPT_HTML_PARSE_ERROR, choice, null);
            }
        };
        
        // setup buttons
        builder.setPositiveButton(R.string.ui_check_errors, listener);
        builder.setNegativeButton(R.string.ui_cancel, listener);
        
        builder.create().show();
    }
}
