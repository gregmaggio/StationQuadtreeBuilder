/**
 * 
 */
package ca.datamagic.quadtree.map;

import java.awt.Color;
import java.util.List;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.policy.BufferedImageRenderPolicy;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMTextLabeler;

import ca.datamagic.quadtree.Station;

/**
 * @author Greg
 *
 */
public class BasicLayer extends OMGraphicHandlerLayer {
	private static final long serialVersionUID = 1L;
	private List<Station> stations = null;
	
	public BasicLayer(List<Station> stations) {
		this.stations = stations;
		setName("Basic Layer");
		setProjectionChangePolicy(new com.bbn.openmap.layer.policy.StandardPCPolicy(this, true));
		setRenderPolicy(new BufferedImageRenderPolicy());
	}
	
	public synchronized OMGraphicList prepare() {
		OMGraphicList list = getList();
		if (list == null) {
			list = init();
		}
		list.generate(getProjection());
		return list;
	}
	
	public OMGraphicList init() {
		OMGraphicList omList = new OMGraphicList();
		if (this.stations != null) {
			OMGraphicList pointList = new OMGraphicList();
			for (int ii = 0; ii < this.stations.size(); ii++) {
				Station station = this.stations.get(ii);
				OMPoint point = new OMPoint(station.getLatitude(), station.getLongitude());
				point.putAttribute(OMGraphicConstants.LABEL, new OMTextLabeler(station.getStationId()));
				point.setFillPaint(Color.black);
				point.setOval(true);
				pointList.add(point);
			}
			omList.add(pointList);
		}
		return omList;
	}
	
	public boolean isHighlightable(OMGraphic omg) {
		return true;
	}
	
	public boolean isSelectable(OMGraphic omg) {
		return true;
	}
	
	public void select(OMGraphicList list) {
        super.select(list);

        // selectedList is a member variable held by OMGraphicHandlerLayer.
        if (selectedList != null) {
            System.out.println("Current selection list: " + selectedList.getDescription());
        }
    }
	
	public void deselect(OMGraphicList list) {
        super.deselect(list);

        // selectedList is a member variable held by OMGraphicHandlerLayer.
        if (selectedList != null) {
            System.out.println("Current selection list: " + selectedList.getDescription());
        }
    }
	
	public String getInfoText(OMGraphic omg) {
        String classname = omg.getClass().getName();
        return "Interaction Layer OMGraphic - "
                + classname.substring(classname.lastIndexOf('.') + 1);
    }
	
	public String getToolTipTextFor(OMGraphic omg) {
        Object tt = omg.getAttribute(OMGraphic.TOOLTIP);
        if (tt instanceof String) {
            return (String) tt;
        } else {
            return null;
        }
    }
}
