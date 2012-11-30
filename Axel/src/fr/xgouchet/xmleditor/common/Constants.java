package fr.xgouchet.xmleditor.common;

/**
 * A list of constant values
 */
public class Constants {

	private Constants() {
	}

	/** Widget edit action */
	public static final String ACTION_PENDING_WIDGET_OPEN = "fr.xgouchet.xmleditor.ACTION_TED_PENDING_WIDGET_OPEN";
	/** Widget edit action */
	public static final String ACTION_WIDGET_OPEN = "fr.xgouchet.xmleditor.ACTION_TED_WIDGET_OPEN";
	/** Box edit action */

	/** name of the shared preferences for this app ( = {@value} ) */
	public static final String PREFERENCES_NAME = "fr.xgouchet.xmleditor";

	/** the list of recent files ( = {@value} ) */
	public static final String PREFERENCE_RECENTS = "recent_files";
	/** the max number of recent file to save ( = {@value} ) */
	public static final String PREFERENCE_MAX_RECENTS = "max_recent_files";
	/** Keep Text nodes exact content */
	public static final String PREFERENCE_KEEP_TEXT_EXACT = "keep_text_exact";
	/** Display attributes on a single line */
	public static final String PREFERENCE_DISPLAY_ATTRIBUTES_INLINE = "display_attributes_inline";
	/** Open an editor when a new node / attr is created */
	public static final String PREFERENCE_EDIT_ON_CREATE = "edit_on_create";

	/** minimum text size */
	public static final int TEXT_SIZE_MIN = 9;
	/** maximum text size */
	public static final int TEXT_SIZE_MAX = 40;

	/** Request code for Save As Activity */
	public static final int REQUEST_SAVE_AS = 107;
	/** Request code for Open Activity */
	public static final int REQUEST_OPEN = 108;
	/** Request code for Edit Node Activity */
	public static final int REQUEST_EDIT_NODE = 109;

	/** */
	public static final String EXTRA_PATH = "path";
	/** Used with templates to ignore the source file */
	public static final String EXTRA_IGNORE_FILE = "ignore_file";

}
