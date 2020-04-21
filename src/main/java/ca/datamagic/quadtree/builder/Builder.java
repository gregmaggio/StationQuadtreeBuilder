/**
 * 
 */
package ca.datamagic.quadtree.builder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.google.gson.Gson;

import ca.datamagic.dao.BaseDAO;
import ca.datamagic.dao.NOAAForecastPageDAO;
import ca.datamagic.dao.StateDAO;
import ca.datamagic.dao.TimeZoneDAO;
import ca.datamagic.dto.NOAAForecastPageDTO;
import ca.datamagic.dto.StateDTO;
import ca.datamagic.quadtree.Point;
import ca.datamagic.quadtree.Quad;
import ca.datamagic.quadtree.Station;
import ca.datamagic.station.xml.StationHandler;
import ca.datamagic.station.xml.StationParser;

/**
 * @author Greg
 *
 */
public class Builder {
	private static final Logger logger = LogManager.getLogger(Builder.class);
	private static String stationsFileName = "C:/Dev/Applications/StationQuadtreeBuilder/src/main/resources/data/stations.xml";
	private static String treeFileName = "C:/Dev/Applications/StationQuadtreeBuilder/src/main/resources/data/tree.ser";
	private static String dataPath = "C:/Dev/Applications/StationQuadtreeBuilder/src/main/resources/data";
	private static double minLatitude = Double.NaN;
	private static double maxLatitude = Double.NaN;
	private static double minLongitude = Double.NaN;
	private static double maxLongitude = Double.NaN;
	
	public static void findStationBounds() throws Exception {		
		StationParser parser = new StationParser();
		StationHandler handler = new StationHandler() {
			@Override
			public void station(Station station) {				
				if (Double.isNaN(minLatitude)) {
					minLatitude = station.getLatitude().doubleValue();
				} else if (minLatitude > station.getLatitude().doubleValue()) {
					minLatitude = station.getLatitude().doubleValue();
				}
				if (Double.isNaN(maxLatitude)) {
					maxLatitude = station.getLatitude().doubleValue();
				} else if (maxLatitude < station.getLatitude().doubleValue()) {
					maxLatitude = station.getLatitude().doubleValue();
				}
				if (Double.isNaN(minLongitude)) {
					minLongitude = station.getLongitude().doubleValue();
				} else if (minLongitude > station.getLongitude().doubleValue()) {
					minLongitude = station.getLongitude().doubleValue();
				}
				if (Double.isNaN(maxLongitude)) {
					maxLongitude = station.getLongitude().doubleValue();
				} else if (maxLongitude < station.getLongitude().doubleValue()) {
					maxLongitude = station.getLongitude().doubleValue();
				}
			}
			
			@Override
			public void complete() {
				logger.debug("minLatitude: " + minLatitude);
				logger.debug("maxLatitude: " + maxLatitude);
				logger.debug("minLongitude: " + minLongitude);
				logger.debug("maxLongitude: " + maxLongitude);
			}
		};
		parser.parse(stationsFileName, handler);
	}
	
	public static void buildStationsQuadtree() throws Exception {
		final Quad tree = new Quad(new Point(maxLatitude, minLongitude), new Point(minLatitude, maxLongitude));
		final StateDAO stateDAO = new StateDAO();
		final NOAAForecastPageDAO forecastPageDAO = new NOAAForecastPageDAO();
		final TimeZoneDAO timeZoneDAO = new TimeZoneDAO();
		final Gson gson = new Gson();
		StationParser parser = new StationParser();
		StationHandler handler = new StationHandler() {	
			@Override
			public void station(Station station) {
				logger.debug("station: " + gson.toJson(station));
				StateDTO state = stateDAO.getState(station.getState());
				if (state != null) {
					try {
						NOAAForecastPageDTO forecastPage = forecastPageDAO.load(station.getLatitude().doubleValue(), station.getLongitude().doubleValue());
						logger.debug("wfo: " + forecastPage.getWFO());
						logger.debug("radar: " + forecastPage.getRadar());
						String timeZoneId = timeZoneDAO.getTimeZone(station.getLatitude().doubleValue(), station.getLongitude().doubleValue());
						logger.debug("timeZoneId: " + timeZoneId);
						station.setWFO(forecastPage.getWFO());
						station.setRadar(forecastPage.getRadar());
						station.setTimeZoneId(timeZoneId);
						if ((station.getWFO() != null) && (station.getWFO().length() > 0) && (station.getRadar() != null) && (station.getRadar().length() > 0) && (station.getTimeZoneId() != null) && (station.getTimeZoneId().length() > 0)) {
							tree.insert(station, station.getLatitude(), station.getLongitude());
						} else {
							logger.warn("Skipping station: " + station.getStationId());
						}
					} catch (Throwable t) {
						logger.warn("Exception", t);
					}
				}
			}
			
			@Override
			public void complete() {
				ObjectOutputStream outputStream = null;
				try {
					outputStream = new ObjectOutputStream(new FileOutputStream(treeFileName));
					outputStream.writeObject(tree);
				} catch (IOException ex) {
					logger.error("IOException", ex);
				} finally {
					if (outputStream != null) {
						try {
							outputStream.close();
						} catch (IOException ex) {
							logger.warn("IOException", ex);
						}
					}
				}
			}
		};
		parser.parse(stationsFileName, handler);		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String currentDir = System.getProperty("user.dir");
	        System.out.println("currentDir: " + currentDir);
	        DOMConfigurator.configure("src/main/resources/log4j.cfg.xml");
	        BaseDAO.setDataPath(dataPath);
	        findStationBounds();	        
	        buildStationsQuadtree();
		} catch (Throwable t) {
			System.out.println("Exception: " + t.getMessage());
			t.printStackTrace();
		}
	}

}
