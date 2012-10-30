package fr.xgouchet.xmleditor.common;

import fr.xgouchet.androidlib.common.ChangeLog;
import fr.xgouchet.xmleditor.R;

/**
 * 
 */
public class AxelChangeLog extends ChangeLog {

	/**
	 * @see fr.xgouchet.androidlib.common.ChangeLog#getTitleResourceForVersion(int)
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
		default:
			title = R.string.release4;
			break;
		}
		return title;
	}

	/**
	 * @see fr.xgouchet.androidlib.common.ChangeLog#getChangeLogResourceForVersion(int)
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
		default:
			log = R.string.release4_log;
			break;
		}
		return log;
	}

}
