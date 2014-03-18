package fr.xgouchet.xmleditor;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import fr.xgouchet.xmleditor.data.xml.XmlNode;

public class AxelPreviewActivity extends Activity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AxelApplication app = (AxelApplication) getApplication();

		setTitle(getString(R.string.title_preview, app.getCurrentDocumentName()));

		XmlNode node = app.getCurrentDocument();
		StringBuilder builder = new StringBuilder();
		node.buildXmlString(builder);
		String data = builder.toString();
		builder = null;

//		String mimetype = app.getMimeType();
//
//		if (mimetype.equals("text/html")) {
//			data = data.replace("%", "%25");
//			data = data.replace("#", "%23");
//			data = data.replace("\\", "%27");
//			data = data.replace("?", "%3f");
//		}

		WebView webview = new WebView(getBaseContext());
//		webview.loadData(data, mimetype, "utf-8");
		webview.getSettings().setBuiltInZoomControls(true);
		setContentView(webview);
	}
}
