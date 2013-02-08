package fr.xgouchet.xmleditor.common;

/**
 * A list of constant values
 */
public final class Constants {

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

	/** the quick action for single tap( = {@value} ) */
	public static final String PREFERENCE_SINGLE_TAP_QA = "single_tap_quick_action";
	/** the quick action for double taps ( = {@value} ) */
	public static final String PREFERENCE_DOUBLE_TAP_QA = "double_tap_quick_action";
	/** the quick action for long press ( = {@value} ) */
	public static final String PREFERENCE_LONG_PRESS_QA = "long_press_quick_action";

	/** automatically escape text content ( = {@value} ) */
	public static final String PREFERENCE_ESCAPE_TEXT_CONTENT = "escape_text_content";
	/** automatically escape attribute values ( = {@value} ) */
	public static final String PREFERENCE_ESCAPE_ATTR_VALUE = "escape_attr_value";
	/** the indentation size ( = {@value} ) */
	public static final String PREFERENCE_INDENTATION_SIZE = "indentation_size";

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
	/** Request code for Sort Chidlren Activity */
	public static final int REQUEST_SORT_CHILDREN = 110;

	/** */
	public static final String EXTRA_PATH = "path";
	/** Used with templates to ignore the source file */
	public static final String EXTRA_IGNORE_FILE = "ignore_file";

	public static final String QUICK_ACTION_EDIT = "edit";
	public static final String QUICK_ACTION_ADD_CHILD = "add_child";
	public static final String QUICK_ACTION_ORDER_CHILDREN = "order_children";
	public static final String QUICK_ACTION_DISPLAY_MENU = "display_menu";
	public static final String QUICK_ACTION_COMMENT_TOGGLE = "comment_toggle";
	public static final String QUICK_ACTION_DELETE = "delete";
	public static final String QUICK_ACTION_NONE = "nothing";

	public static final String INDENT_SMALL = "small";
	public static final String INDENT_MEDIUM = "medium";
	public static final String INDENT_LARGE = "large";
}
