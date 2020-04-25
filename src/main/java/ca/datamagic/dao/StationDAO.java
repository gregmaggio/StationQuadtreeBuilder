/**
 * 
 */
package ca.datamagic.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import ca.datamagic.quadtree.Station;

/**
 * @author Greg
 *
 */
public class StationDAO extends BaseDAO {
	private String fileName = null;
	private Hashtable<String, Station> stations = new Hashtable<String, Station>();
	private List<String> stationIds = new ArrayList<String>();
	
	public StationDAO() throws IOException {
		this.fileName = MessageFormat.format("{0}/stations.csv", getDataPath());
		this.load();
	}
	
	public void add(Station station) {
		String stationId = station.getStationId().toUpperCase();
		if (!this.stations.containsKey(stationId)) {
			station.setStationId(stationId);
			this.stations.put(stationId, station);
			this.stationIds.add(stationId);
		}
	}
	
	public String getStationId(int index) {
		return this.stationIds.get(index);
	}
	
	public int size() {
		return this.stationIds.size();
	}
	
	public Station getStation(String stationId) {
		if ((stationId != null) && (stationId.length() > 0)) {
			stationId = stationId.toUpperCase();
			if (this.stations.containsKey(stationId)) {
				return this.stations.get(stationId);
			}
		}
		return null;
	}
	
	public void load() throws IOException {
		InputStream inputStream = null;	
		try {
			File inputFile = new File(this.fileName);
			if (!inputFile.exists()) {
				return;
			}
			inputStream = new FileInputStream(this.fileName);
			CsvFormat format = new CsvFormat();
			format.setDelimiter(',');
			format.setLineSeparator("\n");
			format.setQuote('\"');
			CsvParserSettings settings = new CsvParserSettings();
			settings.setFormat(format);
			CsvParser csvParser = new CsvParser(settings);
			List<String[]> lines = csvParser.parseAll(inputStream);
			for (int ii = 1; ii < lines.size(); ii++) {
				String[] currentLineItems = lines.get(ii);
				String stationId = currentLineItems[0].toUpperCase();
				String stationName = currentLineItems[1];
				String state = currentLineItems[2];
				String wfo = currentLineItems[3];
				String radar = currentLineItems[4];
				String timeZoneId = currentLineItems[5];
				double latitude = Double.parseDouble(currentLineItems[6]);
				double longitude = Double.parseDouble(currentLineItems[7]);
				Station station = new Station();
				station.setStationId(stationId);
				station.setStationName(stationName);
				station.setState(state);
				station.setWFO(wfo);
				station.setRadar(radar);
				station.setTimeZoneId(timeZoneId);
				station.setLatitude(latitude);
				station.setLongitude(longitude);
				this.stations.put(stationId, station);
				this.stationIds.add(stationId);
			}
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}
	
	public void save() throws IOException {
		OutputStream outputStream = null;
		PrintWriter printWriter = null;
		try {
			outputStream = new FileOutputStream(this.fileName);
			printWriter = new PrintWriter(outputStream);
			printWriter.println("stationId,stationName,state,wfo,radar,timeZoneId,latitude,longitude");
			for (int ii = 0; ii < this.stationIds.size(); ii++) {
				Station station = this.stations.get(this.stationIds.get(ii));
				String line = MessageFormat.format("{0},\"{1}\",{2},{3},{4},{5},{6},{7}", station.getStationId(), station.getStationName(), station.getState(), station.getWFO(), station.getRadar(), station.getTimeZoneId(), Double.toString(station.getLatitude()), Double.toString(station.getLongitude()));
				printWriter.println(line);
			}
		} finally {
			if (printWriter != null) {
				printWriter.close();
			}
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}
}
