/**
 * 
 */
package ca.datamagic.quadtree.map;

import com.bbn.openmap.layer.policy.BufferedImageRenderPolicy;
import com.bbn.openmap.layer.shape.BufferedShapeLayer;

/**
 * @author Greg
 *
 */
public class StateLayer extends BufferedShapeLayer {
	private static final long serialVersionUID = 1L;

	public StateLayer() {
		super("C:/Dev/Applications/StationQuadtreeBuilder/src/main/resources/data/cb_2018_us_state_500k/cb_2018_us_state_500k.shp");
		setName("States Layer");
		setProjectionChangePolicy(new com.bbn.openmap.layer.policy.StandardPCPolicy(this, true));
		setRenderPolicy(new BufferedImageRenderPolicy());
	}
}
