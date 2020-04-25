/**
 * 
 */
package ca.datamagic.quadtree.builder;

import java.io.File;
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
import ca.datamagic.dao.StationDAO;
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
	private static String log4jFileName = "C:/Dev/Applications/StationQuadtreeBuilder/src/main/resources/log4j.cfg.xml";
	private static String stationsFileName = "C:/Dev/Applications/StationQuadtreeBuilder/src/main/resources/data/stations.xml";
	private static String treeFileName = "C:/Dev/Applications/StationQuadtreeBuilder/src/main/resources/data/tree.ser";
	private static String dataPath = "C:/Dev/Applications/StationQuadtreeBuilder/src/main/resources/data";
	private static double minLatitude = Double.NaN;
	private static double maxLatitude = Double.NaN;
	private static double minLongitude = Double.NaN;
	private static double maxLongitude = Double.NaN;
	
	public static void loadStations() throws Exception {
		final StateDAO stateDAO = new StateDAO();
		final StationDAO stationDAO = new StationDAO();
		final Gson gson = new Gson();
		StationParser parser = new StationParser();
		StationHandler handler = new StationHandler() {	
			@Override
			public void station(Station station) {
				logger.debug("station: " + gson.toJson(station));
				StateDTO state = stateDAO.getState(station.getState());
				if (state != null) {
					station.setWFO("");
					station.setRadar("");
					station.setTimeZoneId("");
					stationDAO.add(station);
				} else {
					logger.warn("State not found: " + station.getState());
				}
			}
			
			@Override
			public void complete() {
				try {
					stationDAO.save();
				} catch (IOException ex) {
					logger.error("IOException", ex);
				}
			}
		};
		parser.parse(stationsFileName, handler);
	}
	
	public static void loadTimeZones() throws Exception {
		boolean changed = false;
		StationDAO stationDAO = new StationDAO();
		TimeZoneDAO timeZoneDAO = new TimeZoneDAO();
		for (int ii = 0; ii < stationDAO.size(); ii++) {
			String stationId = stationDAO.getStationId(ii);
			Station station = stationDAO.getStation(stationId);
			String timeZoneId = station.getTimeZoneId();
			if ((timeZoneId == null) || (timeZoneId.length() < 1) || (timeZoneId.compareToIgnoreCase("null") == 0)) {
				timeZoneId = timeZoneDAO.getTimeZone(station.getLatitude(), station.getLongitude());
				if ((timeZoneId != null) && (timeZoneId.length() > 0)) {
					station.setTimeZoneId(timeZoneId);
					changed = true;
				}
			}
		}
		if (changed) {
			stationDAO.save();
		}
	}
	
	public static void loadNOAAForecastPage() throws Exception {
		boolean changed = false;
		StationDAO stationDAO = new StationDAO();
		NOAAForecastPageDAO forecastPageDAO = new NOAAForecastPageDAO();
		for (int ii = 0; ii < stationDAO.size(); ii++) {
			String stationId = stationDAO.getStationId(ii);
			Station station = stationDAO.getStation(stationId);
			String wfo = station.getWFO();
			String radar = station.getRadar();
			if ((wfo == null) || (wfo.length() < 1) || (wfo.compareToIgnoreCase("null") == 0) || (radar == null) || (radar.length() < 1) || (radar.compareToIgnoreCase("null") == 0)) {
				try {
					NOAAForecastPageDTO forecastPage = forecastPageDAO.load(station.getLatitude(), station.getLongitude());
					wfo = forecastPage.getWFO();
					if ((wfo != null) && (wfo.length() > 0)) {
						station.setWFO(wfo);
						changed = true;
					}
					radar = forecastPage.getRadar();
					if ((radar != null) && (radar.length() > 0)) {
						station.setRadar(radar);
						changed = true;
					}
				} catch (IOException ex) {
					logger.error("IOException", ex);
					logger.error("Error loading forecast page for station: " + stationId);
				}
			}
		}
		if (changed) {
			stationDAO.save();
		}
	}
	
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
		Quad tree = new Quad(new Point(maxLatitude, minLongitude), new Point(minLatitude, maxLongitude));
		StationDAO stationDAO = new StationDAO();
		for (int ii = 0; ii < stationDAO.size(); ii++) {
			String stationId = stationDAO.getStationId(ii);
			Station station = stationDAO.getStation(stationId);
			String wfo = station.getWFO();
			String radar = station.getRadar();
			String timeZoneId = station.getTimeZoneId();
			if ((wfo != null) && (wfo.length() > 0) && (wfo.compareToIgnoreCase("null") != 0) &&
				(radar != null) && (radar.length() > 0) && (radar.compareToIgnoreCase("null") != 0) &&
				(timeZoneId != null) && (timeZoneId.length() > 0) && (timeZoneId.compareToIgnoreCase("null") != 0)) {
				tree.insert(station, station.getLatitude(), station.getLongitude());
			} else {
				logger.warn("Skipping station " + stationId);
			}
		}
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
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String currentDir = System.getProperty("user.dir");
	        System.out.println("currentDir: " + currentDir);
	        File log4jFile = new File(currentDir + "/log4j.cfg.xml");
	        if (log4jFile.exists()) {
	        	System.out.println("log4jFile: " + log4jFile.getCanonicalPath());
	        	log4jFileName = log4jFile.getCanonicalPath();
	        }
	        File dataFile = new File(currentDir + "/data");
	        if (dataFile.exists()) {
	        	System.out.println("dataFile: " + dataFile.getCanonicalPath());
	        	dataPath = dataFile.getCanonicalPath();
	        	File stationsFile = new File(dataPath + "/stations.xml");
	        	if (stationsFile.exists()) {
	        		System.out.println("stationsFile: " + stationsFile.getCanonicalPath());
	        		stationsFileName = stationsFile.getCanonicalPath();
	        	}
	        	File treeFile = new File(dataPath + "/tree.ser");
	        	if (treeFile.exists()) {
	        		System.out.println("treeFile: " + treeFile.getCanonicalPath());
	        		treeFileName = stationsFile.getCanonicalPath();
	        	}
	        }
	        boolean loadStations = false;
	        boolean loadTimeZones = false;
	        boolean loadNOAAForecastPage = false;
	        boolean buildStationsQuadtree = false;
	        boolean showHelp = false;
	        if (args != null) {
		        for (int ii = 0; ii < args.length; ) {
		        	String arg = args[ii++];
		        	if (arg.compareToIgnoreCase("--loadStations") == 0) {
		        		loadStations = true;
		        	} else if (arg.compareToIgnoreCase("--loadTimeZones") == 0) {
		        		loadTimeZones = true;
		        	} else if (arg.compareToIgnoreCase("--loadNOAAForecastPage") == 0) {
		        		loadNOAAForecastPage = true;
		        	} else if (arg.compareToIgnoreCase("--buildStationsQuadtree") == 0) {
		        		buildStationsQuadtree = true;
		        	} else if (arg.compareToIgnoreCase("--help") == 0) {
		        		showHelp = true;
		        	}
		        }
	        }
	        if (showHelp) {
	        	System.out.println("loadStations: java -cp stationquadtreebuilder.jar ca.datamagic.quadtree.builder.Builder --loadStations");
	        	System.out.println("loadTimeZones: java -cp stationquadtreebuilder.jar ca.datamagic.quadtree.builder.Builder --loadTimeZones");
	        	System.out.println("loadNOAAForecastPage: java -cp stationquadtreebuilder.jar ca.datamagic.quadtree.builder.Builder --loadNOAAForecastPage");
	        	System.out.println("buildStationsQuadtree: java -cp stationquadtreebuilder.jar ca.datamagic.quadtree.builder.Builder --buildStationsQuadtree");
	        	return;
	        }
	        DOMConfigurator.configure(log4jFileName);
	        BaseDAO.setDataPath(dataPath);
	        // Step 1
	        if (loadStations) {
	        	System.out.println("loadStations...");
	        	loadStations();
	        }
	        // Step 2
	        if (loadTimeZones) {
	        	System.out.println("loadTimeZones...");
	        	loadTimeZones();
	        }
	        // Step 3
	        if (loadNOAAForecastPage) {
	        	System.out.println("loadNOAAForecastPage...");
	        	loadNOAAForecastPage();
	        }
	        // Step 4
	        if (buildStationsQuadtree) {
	        	System.out.println("buildStationsQuadtree...");
	        	findStationBounds();	        
		        buildStationsQuadtree();
	        }	        	        
		} catch (Throwable t) {
			System.out.println("Exception: " + t.getMessage());
			t.printStackTrace();
		}
	}

}
