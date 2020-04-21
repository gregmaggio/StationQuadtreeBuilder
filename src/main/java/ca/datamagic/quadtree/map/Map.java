/**
 * 
 */
package ca.datamagic.quadtree.map;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.CenterEvent;
import com.bbn.openmap.event.ZoomEvent;

import ca.datamagic.quadtree.Quad;
import ca.datamagic.quadtree.Station;

/**
 * @author Greg
 *
 */
public class Map extends JFrame implements ActionListener, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	private static final Logger logger= LogManager.getLogger(Map.class);
	private static String treeFileName = "C:/Dev/Applications/StationQuadtreeBuilder/src/main/resources/data/tree.ser";
	private static String iconPath = "C:/Dev/Applications/StationQuadtreeBuilder/src/main/resources/icons";
	private static final double distance = 5;
	private static final String units = "statute miles";
	private enum Tool {	None, ZoomIn, ZoomOut, Pan, Info };
	private Quad tree = null;
	private JToolBar toolBar = null;
	private MapBean mapBean = null;
	private Tool currentTool = Tool.None;
	
	public Map(Quad tree) throws MalformedURLException {
		super("Stations");
		this.tree = tree;
		this.toolBar = new JToolBar("Map Tools");
		addButtons(this.toolBar);
		this.mapBean = new MapBean();
		this.mapBean.add(new BasicLayer(tree.list()));
		this.mapBean.add(new StateLayer());
		this.mapBean.addMouseListener(this);
		this.mapBean.addMouseMotionListener(this);
		setSize(640, 480);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(toolBar, BorderLayout.PAGE_START);
		getContentPane().add(BorderLayout.CENTER, this.mapBean);
		pack();
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		logger.debug("actionPerformed: " + e.paramString());
		String command = e.getActionCommand();
		logger.debug("command: " + command);
		if (command != null) {
			this.currentTool = Tool.valueOf(command);
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		logger.debug("mouseClicked");
		switch (this.currentTool) {
			case ZoomIn:
			{
				Point2D point = this.mapBean.getCoordinates(e);
				this.mapBean.center(new CenterEvent(this, point.getY(), point.getX()));
				this.mapBean.zoom(new ZoomEvent(this, ZoomEvent.RELATIVE, 0.5f));
				break;
			}
			case ZoomOut:
			{
				Point2D point = this.mapBean.getCoordinates(e);
				this.mapBean.center(new CenterEvent(this, point.getY(), point.getX()));
				this.mapBean.zoom(new ZoomEvent(this, ZoomEvent.RELATIVE, 2.0f));
				break;
			}
			case Info:
			{
				Point2D point = this.mapBean.getCoordinates(e);
				Station station = this.tree.readNearest(point.getY(), point.getX(), distance, units);
				if (station != null) {
					StringBuilder builder = new StringBuilder();
					builder.append("Station ID: " + station.getStationId() + "\n");
					builder.append("Station Name: " + station.getStationName() + "\n");
					builder.append("Latitude: " + station.getLatitude() + "\n");
					builder.append("Longitude: " + station.getLongitude() + "\n");
					builder.append("WFO: " + station.getWFO() + "\n");
					builder.append("Radar: " + station.getRadar() + "\n");
					builder.append("Time Zone ID: " + station.getTimeZoneId());
					JOptionPane.showMessageDialog(this, builder.toString());
				}
				break;
			}
		default:
			break;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		logger.debug("mouseEntered");
	}

	@Override
	public void mouseExited(MouseEvent e) {
		logger.debug("mouseExited");
	}

	@Override
	public void mousePressed(MouseEvent e) {
		logger.debug("mousePressed");
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		logger.debug("mouseReleased");
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		logger.debug("mouseDragged");
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		logger.debug("mouseMoved");		
	}
	
	private void addButtons(JToolBar toolBar) throws MalformedURLException {
	    toolBar.add(makeNavigationButton("zoom-in-icon.gif", "ZoomIn", "Zoom In", "Zoom In"));
	    toolBar.add(makeNavigationButton("zoom-out-icon.gif", "ZoomOut", "Zoom Out", "Zoom Out"));
	    //toolBar.add(makeNavigationButton("pan-icon.gif", "Pan", "Pan", "Pan"));
	    toolBar.add(makeNavigationButton("info-icon.gif", "Info", "Info", "Info"));
	}
	
	private JButton makeNavigationButton(String imageName, String actionCommand, String toolTipText, String altText) throws MalformedURLException {
		String imgLocation = MessageFormat.format("{0}/{1}", iconPath, imageName);
		File imgFile = new File(imgLocation);
		@SuppressWarnings("deprecation")
		URL imageURL = imgFile.toURL();

		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(this);
		if (imageURL != null) {
			button.setIcon(new ImageIcon(imageURL, altText));
		} else {
			button.setText(altText);
		}
		return button;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DOMConfigurator.configure("src/main/resources/log4j.cfg.xml");
			ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(treeFileName));
			Quad tree = (Quad)inputStream.readObject();
			inputStream.close();
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			new Map(tree);			
		} catch (Throwable t) {
			System.out.println("Exception: " + t.getMessage());
			t.printStackTrace();
		}
	}
}
