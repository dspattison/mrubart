package us.patt.mrubart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
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

	private static final String TAG = "MrubartActivity";
	
	private ArrayList<BartStation> _stations;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		Resources res = getResources();
//		String[] names = res.getStringArray(R.array.station_names);
//		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, names));
		
		ArrayList<BartStation> stations = getStations();
		
		if (null == stations || stations.isEmpty()) {
			Log.e(TAG, "Stations is empty");
			return;
		}
		
		//sort stations by geo location
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location != null) {
			final double longitude = location.getLongitude();
			final double latitude = location.getLatitude();
			
			Collections.sort(stations, new Comparator<BartStation>() {
				public int compare(BartStation s1, BartStation s2) {
					return distance(s1, latitude, longitude) < distance(s2, latitude, longitude) ? -1 : 1; 
				}
			});
		}
		
		ArrayList<String> names = new ArrayList<String>();
		
		Iterator<BartStation> itr = stations.iterator();
		while(itr.hasNext()) {
			names.add(itr.next().name);
		}
		
		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, names));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, final View view,
					final int position, long id) {
				String[] codes = getResources().getStringArray(R.array.station_codes);
				showStation(view, codes[position]);
				

			}
		});

		//load the best station!
		//findClosetStation();
	}

	/**
	 * example:
	 * http://api.bart.gov/api/etd.aspx?cmd=etd&orig=NBRK&key=MW9S-E7SL-26DU-VV8V
	 */
	private String parseResponse(CharSequence station) {
		String content = "";

		try {
			URL url = new URL("http://api.bart.gov/api/etd.aspx?cmd=etd&orig="
					+ station + "&key=MW9S-E7SL-26DU-VV8V");
			
			SAXParserFactory factory = SAXParserFactory.newInstance();
			// create a parser
			SAXParser parser = factory.newSAXParser();
			// create the reader (scanner)
			XMLReader xmlreader = parser.getXMLReader();

			BartETDHandler etdHandler = new BartETDHandler();
			xmlreader.setContentHandler(etdHandler);
			
			xmlreader.parse(new InputSource(url.openConnection().getInputStream()));
			
			content = etdHandler.getContent();

		} catch (Exception e) {
			e.printStackTrace();
			return "OOPS! " + e.getClass().toString() + ": " + e.getMessage();
		}

		return content;
	}
	
	/**
	 * call from main ui thread
	 * @param view
	 * @param station
	 */
	private void showStation(final View view, final String station) {
		Log.d(TAG, "Showing station "+ station);
		
		final ProgressDialog loadingDialog = ProgressDialog.show(
				MrubartActivity.this, "", "Loading. Please wait...",
				true);

		// When clicked, show a toast with the TextView text
		new Thread(new Runnable() {
			public void run() {
				final String content = parseResponse(station);

				view.post(new Runnable() {
					public void run() {
						loadingDialog.cancel();
						AlertDialog alertDialog = new AlertDialog.Builder(
								MrubartActivity.this).create();

						alertDialog.setTitle(station);
						alertDialog.setMessage(content);
						alertDialog.setButton("Darn, I missed it",
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog,
											int which) {
									}
								});
						alertDialog.show();

					}
				});
			}
		}).start();
		// Toast.makeText(getApplicationContext(),
		// content, Toast.LENGTH_LONG).show();
	}

	private void findClosetStation() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		double longitude = location.getLongitude();
		double latitude = location.getLatitude();

		Log.v(TAG, "long=" + longitude + " lat=" + latitude);
		
		ArrayList<BartStation> stations = getStations();
		
		if (null == stations || stations.isEmpty()) {
			Log.e(TAG, "Stations is empty");
			return;
		}
		//lets start with this one
		BartStation closetStation = stations.get(0);
		
		
		for (int i=0; i < stations.size(); i++) {
			if (distance(closetStation, latitude, longitude) > distance(stations.get(i), latitude, longitude)) {
				//this one is closer!!
				closetStation = stations.get(i);
				Log.i(TAG, "closer: " + closetStation);
			}
		}
		
		Log.i(TAG, "showing: [" + closetStation.abbr + "]");
		Log.i(TAG, "showing: "+closetStation.toString());
		showStation(getListView(), closetStation.abbr);
		

	}
	/**
	 * warning: don't go near the international date line or poles
	 * 
	 * @param BartStation station
	 * @param double latitude
	 * @param double longitude
	 * @return double
	 */
	private double distance(BartStation station, double latitude, double longitude) {
		return Math.sqrt(Math.pow(station.gtfsLatitude - latitude, 2) + Math.pow(station.gtfsLongitude - longitude, 2));
	}
	
	private ArrayList<BartStation> getStations()
	{
		if (null != _stations) {
			return _stations;
		}
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			// create a parser
			SAXParser parser = factory.newSAXParser();
			// create the reader (scanner)
			XMLReader xmlreader = parser.getXMLReader();
			BartStationHandler stationHandler = new BartStationHandler();
			xmlreader.setContentHandler(stationHandler);

			// get xml data
			InputStream inputStream = getResources().openRawResource(R.raw.stations);
			//String stationsXml = "";

			xmlreader.parse(new InputSource(inputStream));

			_stations = stationHandler.getStations();
			return _stations;
		} catch (Exception e) {
			Log.e(TAG, "Error loading the station list from xml:"
					+ e.getClass().toString() + ": " + e.getMessage());
			e.printStackTrace();
		}
		return new ArrayList<BartStation>();
		
	}

}