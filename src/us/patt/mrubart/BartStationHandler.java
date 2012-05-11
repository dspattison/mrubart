package us.patt.mrubart;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.util.Log;

public class BartStationHandler extends DefaultHandler {
	
	private static final String TAG = "BartStationHandler";

	private StringBuffer buffer = new StringBuffer();

	private ArrayList<BartStation> stations= new ArrayList<BartStation>();
	private BartStation currentStation;

	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {

		if (localName.equals("station")) {
			currentStation = new BartStation();
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (localName.equals("name")) {
			currentStation.name = buffer.toString().trim();
		} else if (localName.equals("abbr")) {
			currentStation.abbr = buffer.toString().trim();
		} else if (localName.equals("gtfs_latitude")) {
			currentStation.gtfsLatitude = new Double(buffer.toString());
		} else if (localName.equals("gtfs_longitude")) {
			currentStation.gtfsLongitude = new Double(buffer.toString());
		} else if (localName.equals("address")) {
			currentStation.address = buffer.toString().trim();
		} else if (localName.equals("city")) {
			currentStation.city = buffer.toString().trim();
		} else if (localName.equals("county")) {
			currentStation.county = buffer.toString().trim();
		} else if (localName.equals("state")) {
			currentStation.state = buffer.toString().trim();
		} else if (localName.equals("zipcode")) {
			currentStation.zipcode = buffer.toString().trim();
		} else if (localName.equals("station")) {
			//end station element, place in array
			stations.add(currentStation);
			Log.d(TAG, currentStation.toString());
		}
		
		buffer = new StringBuffer();//reset buffer
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		System.out.println(ch);
		buffer.append(ch, start, length);
	}

	public ArrayList<BartStation> getStations() {
		return stations;
	}

}