/**
 * 
 */
package ca.datamagic.dao;

import java.io.IOException;
import java.text.MessageFormat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ca.datamagic.dto.NOAAForecastPageDTO;

/**
 * @author Greg
 *
 */
public class NOAAForecastPageDAO {
	private static final String HREF_ATTRIBUTE = "href";
	private static final String WFO_DOMAIN = "forecast.weather.gov";
	private static final String WFO_ID = "site=";
	private static final String RADAR_DOMAIN = "radar.weather.gov";
	private static final String RADAR_ID = "rid=";
	private static final String AMP = "&";
	
	public NOAAForecastPageDTO load(double latitude, double longitude) throws IOException {
		String uri = MessageFormat.format("https://forecast.weather.gov/MapClick.php?lat={0}&lon={1}", Double.toString(latitude), Double.toString(longitude));
		Document document = Jsoup.connect(uri).get();
		Elements anchors = document.getElementsByTag("a");
		String wfo = null;
		String radar = null;
		for (int ii = 0; ii < anchors.size(); ii++) {
			Element element = anchors.get(ii);
			if (element.hasAttr(HREF_ATTRIBUTE)) {
				String href = element.attr(HREF_ATTRIBUTE).toLowerCase();
				if (href.contains(WFO_DOMAIN)) {
					int startIndex = href.indexOf(WFO_ID);
					if (startIndex > -1) {
						int endIndex = href.indexOf(AMP, startIndex);
						if (endIndex > -1) {
							wfo = href.substring(startIndex + 5, endIndex).toUpperCase();
						}
					}
				} else if (href.contains(RADAR_DOMAIN)) {
					int startIndex = href.indexOf(RADAR_ID);
					if (startIndex > -1) {
						int endIndex = href.indexOf(AMP, startIndex);
						if (endIndex > -1) {
							radar = href.substring(startIndex + 4, endIndex).toUpperCase();
						}
					}
				}
			}
		}
		return new NOAAForecastPageDTO(wfo, radar);
	}
}
