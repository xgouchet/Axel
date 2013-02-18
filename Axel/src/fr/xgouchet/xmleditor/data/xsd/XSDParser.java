package fr.xgouchet.xmleditor.data.xsd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.stream.StreamSource;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;

import android.util.Log;
import fr.xgouchet.androidlib.common.LogUtils;

public class XSDParser {

	public void parseXSD(final File file) throws FileNotFoundException {
		InputStream is = new FileInputStream(file);
		XmlSchemaCollection schemaCol = new XmlSchemaCollection();
		XmlSchema schema = schemaCol.read(new StreamSource(is), null);

		if (schema != null) {
			OutputStream stream = LogUtils.getOutputStream("XSD", Log.INFO);
			schema.write(stream);
			try {
				stream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
