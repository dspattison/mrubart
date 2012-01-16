package us.patt.mrubart;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BartETDHandler extends DefaultHandler {

	private StringBuffer buffer = new StringBuffer();

	private String content = "";

	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {

		buffer.setLength(0);
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (localName.equals("etd")) {
			content += "\n";
		} else if (localName.equals("date")) {
			content += buffer.toString();
			content += " ";
		} else if (localName.equals("time")) {
			content += buffer.toString();
			content += "\n";
		} else if (localName.equals("name")) {
			content += buffer.toString();
			content += "\n";
		} else if (localName.equals("destination")) {
			content += buffer.toString();
			content += ": ";
		} else if (localName.equals("minutes")) {
			content += buffer.toString();
			content += ", ";
		}

	}

	@Override
	public void characters(char[] ch, int start, int length) {
		System.out.println(ch);
		buffer.append(ch, start, length);
	}

	public String getContent() {
		return content;
	}

}