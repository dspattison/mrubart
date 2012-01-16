package us.patt.mrubart;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


public class MrubartActivity extends ListActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Resources res = getResources();
		String[] names = res.getStringArray(R.array.station_names);
		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item,
				names));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// When clicked, show a toast with the TextView text
				Resources res = getResources();
				String[] codes = res.getStringArray(R.array.station_codes);
				String content = parseResponse(codes[position]);
				
				AlertDialog alertDialog = new AlertDialog.Builder(MrubartActivity.this).create();
				
				alertDialog.setTitle(((TextView) view).getText());
				alertDialog.setMessage(content);
				alertDialog.setButton("Darn, I missed it", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int which) {}});
				alertDialog.show();
				
//				Toast.makeText(getApplicationContext(),
//					content, Toast.LENGTH_LONG).show();


			}
		});

		// registerForContextMenu(getListView());
	}
	
	
	/**
	 * example: http://api.bart.gov/api/etd.aspx?cmd=etd&orig=NBRK&key=MW9S-E7SL-26DU-VV8V
	 */
	private String parseResponse(CharSequence station) {
		String content="";

		try {
			URL url = new URL("http://api.bart.gov/api/etd.aspx?cmd=etd&orig="+ station +"&key=MW9S-E7SL-26DU-VV8V");
			InputStream response = url.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(response));
			for (String line; (line = reader.readLine()) != null;) {
				content += line;
			}
			
	        SAXParserFactory factory = SAXParserFactory.newInstance();
	        // create a parser
	        SAXParser parser = factory.newSAXParser();
	        // create the reader (scanner)
	        XMLReader xmlreader = parser.getXMLReader();
	        
	        BartETDHandler etdHandler = new BartETDHandler();
            xmlreader.setContentHandler(etdHandler);

	        
	        xmlreader.parse(new InputSource(new StringReader(content)));
	        
	        content = etdHandler.getContent();
			
		} catch (Exception e) {
			return "OOPS! " +e.getClass().toString() + ": " + e.getMessage();
		}
		
		
		return content;
	}
	
}