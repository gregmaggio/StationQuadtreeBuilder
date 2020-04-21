/**
 * 
 */
package ca.datamagic.station.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import ca.datamagic.quadtree.Station;

/**
 * @author Greg
 *
 */
public class StationParser extends DefaultHandler {
	private static Logger _logger = LogManager.getLogger(StationParser.class);
	private static String _stationNodeName = "station";
	private static String _stationIdNodeName = "station_id";
	private static String _stationNameNodeName = "station_name";
	private static String _stateNodeName = "state";
	private static String _latitudeNodeName = "latitude";
	private static String _longitudeNodeName = "longitude";
	private StationHandler _handler = null;
	private String _currentElement = null;
	private Station _currentStation = null;
	
	public void parse(String fileName, StationHandler handler) throws ParserConfigurationException, SAXException, IOException {
		_handler = handler;
		_currentElement = null;
		_currentStation = null;
		File file = new File(fileName);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		SAXParser parser = factory.newSAXParser();
		parser.parse(file, this);
		if (handler != null) {
			handler.complete();
		}
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		_currentElement = qName;
		if (_currentElement != null) {
			if (_currentElement.compareToIgnoreCase(_stationNodeName) == 0) {
				_currentStation = new Station();
			}
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String value = new String(ch, start, length);
		if ((_currentElement != null) && (_currentStation != null)) {
			if (_currentElement.compareToIgnoreCase(_stationIdNodeName) == 0) {
				_currentStation.setStationId(value);
			} else if (_currentElement.compareToIgnoreCase(_stationNameNodeName) == 0) {
				_currentStation.setStationName(value);
			} else if (_currentElement.compareToIgnoreCase(_stateNodeName) == 0) {
				_currentStation.setState(value);
			} else if (_currentElement.compareToIgnoreCase(_latitudeNodeName) == 0) {
				_currentStation.setLatitude(new Double(value));
			} else if (_currentElement.compareToIgnoreCase(_longitudeNodeName) == 0) {
				_currentStation.setLongitude(new Double(value));
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName != null) {
			if (qName.compareToIgnoreCase(_stationNodeName) == 0) {
				if (_currentStation != null) {
					if (_handler != null) {
						_handler.station(_currentStation);
					}
				}
				_currentStation = null;
			}
		}
		_currentElement = null;
	}

	@Override
	public void warning(SAXParseException ex) throws SAXException {
		_logger.warn("SAXParseException", ex);
	}
}
