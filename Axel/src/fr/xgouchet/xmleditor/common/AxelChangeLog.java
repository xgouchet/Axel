package fr.xgouchet.xmleditor.common;

import android.content.Context;
import fr.xgouchet.androidlib.common.AbstractChangeLog;
import fr.xgouchet.xmleditor.R;

/**
 * 
 */
public class AxelChangeLog extends AbstractChangeLog {

	@Override
	protected String getChangelogMessage(final Context context) {
		StringBuilder builder = new StringBuilder();

		builder.append(context.getString(R.string.quick_help));
		builder.append("\n\n");

		builder.append(context.getString(getTitleResource(context)));
		builder.append("\n\n");
		builder.append(context.getString(getChangeLogResource(context)));

		return builder.toString();
	}

	/**
	 * @see fr.xgouchet.androidlib.common.AbstractChangeLog#getTitleResourceForVersion(int)
	 */
	@Override
	public int getTitleResourceForVersion(final int version) {
		int title;
		switch (version) {
		case 1:
			title = R.string.release1;
			break;
		case 2:
			title = R.string.release2;
			break;
		case 3:
			title = R.string.release3;
			break;
		case 4:
			title = R.string.release4;
			break;
		case 5:
			title = R.string.release5;
			break;
		case 6:
			title = R.string.release6;
			break;
		case 7:
		case 8:
		case 9:
		case 10:
			title = R.string.release7;
			break;
		case 11:
			title = R.string.release11;
			break;
		case 12:
			title = R.string.release12;
			break;
		case 13:
		default:
			title = R.string.release13;
			break;
		}
		return title;
	}

	/**
	 * @see fr.xgouchet.androidlib.common.AbstractChangeLog#getChangeLogResourceForVersion(int)
	 */
	@Override
	public int getChangeLogResourceForVersion(final int version) {
		int log;
		switch (version) {
		case 1:
			log = R.string.release1_log;
			break;
		case 2:
			log = R.string.release2_log;
			break;
		case 3:
			log = R.string.release3_log;
			break;
		case 4:
			log = R.string.release4_log;
			break;
		case 5:
			log = R.string.release5_log;
			break;
		case 6:
			log = R.string.release6_log;
			break;
		case 7:
		case 8:
		case 9:
		case 10:
			log = R.string.release7_log;
			break;
		case 11:
			log = R.string.release11_log;
			break;
		case 12:
			log = R.string.release12_log;
			break;
		case 13:
		default:
			log = R.string.release13_log;
			break;
		}
		return log;
	}

}
