/**
 * 
 */
package ca.datamagic.station.xml;

import ca.datamagic.quadtree.Station;

/**
 * @author Greg
 *
 */
public interface StationHandler {
	void station(Station station);
	void complete();
}
