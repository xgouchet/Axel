package fr.xgouchet.xmleditor;

import android.os.Bundle;
import android.view.View.OnClickListener;
import fr.xgouchet.androidlib.ui.activity.AboutActivity;

/**
 * 
 */
public class AxelAboutActivity extends AboutActivity implements OnClickListener {

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_about);
	}

}
