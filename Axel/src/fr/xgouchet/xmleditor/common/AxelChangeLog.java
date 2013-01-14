package fr.xgouchet.xmleditor.common;

import fr.xgouchet.androidlib.common.AbstractChangeLog;
import fr.xgouchet.xmleditor.R;

/**
 * 
 */
public class AxelChangeLog extends AbstractChangeLog {

	/**
	 * @see fr.xgouchet.androidlib.common.AbstractChangeLog#getTitleResourceForVersion(int)
	 */
	public int getTitleResourceForVersion(int version) {
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
		default:
			title = R.string.release7;
			break;
		}
		return title;
	}

	/**
	 * @see fr.xgouchet.androidlib.common.AbstractChangeLog#getChangeLogResourceForVersion(int)
	 */
	public int getChangeLogResourceForVersion(int version) {
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
		default:
			log = R.string.release7_log;
			break;
		}
		return log;
	}

}
