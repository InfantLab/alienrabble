package alienrabble.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jme.util.Timer;


/**
 * This class stores the data recorded during the grab phase of the game.
 * It stores the initial locations of the objects and the order and timing 
 * of how they are collected. 
 * 
 * @author Caspar Addyman
 *
 */
public class ARXMLGrabData{
	private static final long serialVersionUID = 1L;

	
	//the names of the various node types in our xml doc
	public static final String TOPLEVEL_NODE = "alienrabbledata";
	public static final String ARVERSION_NODE = "AlienRabbleVersion";
	public static final String GRAB_SESSION_DETAILS_NODE = "GrabSessionDetails";
	public static final String ID_NODE = "id";
	public static final String MODELSET_NODE = "modelset";
	public static final String EXPERIMENTER_NODE = "experimenter";
	public static final String PARTICIPANT_NODE = "participant";
	public static final String TESTDATE_NODE = "testdate";
	public static final String STARTING_POSITIONS = "startingpositions";
	public static final String GRABBED_POSITIONS = "grabbedpositions";
	public static final String PLAYER_TRAJECTORY = "playertrajectory";
	public static final String GRAB_NODE = "grab";
	public static final String PLAYER_LOCATION = "playerlocation";
	public static final String ALIEN_ID_NODE = "alienid";
	public static final String ALIEN_NAME_NODE = "alienname";
	public static final String VEHICLE_NAME_NODE = "vehiclename";
	public static final String TESTSTART_NODE = "starttime";
	public static final String EVENTTIME_NODE = "time";
	public static final String TICKS_VALUE = "ticks";
	public static final String SECS_VALUE = "secs";
	public static final String LOCATION_NODE = "location";
	public static final String VELOCITY_NODE = "velocity";
	public static final String BLOCK_NODE = "block";
	public static final String ROUND_NODE = "round";
	
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String TIME_FORMAT = "hh=mm=ss";

	
	private String ID;   // participant's ID number
	private String modelset; //the models used in this session
	private String experimenter; 
	private Date testdate; // testing date
	private Date starttime; // when testing began
	private Date endtime; // when testing ended
	

	private ArrayList<GrabEvent> startingpositions = new ArrayList<GrabEvent>();
    private ArrayList<GrabEvent> allgrabs = new ArrayList<GrabEvent>();
	private ArrayList<PlayerLocation> playertrajectory = new ArrayList<PlayerLocation>();
	
	
	private Timer timer; //hold a reference to the main timer
	/**
	 * 
	 * @param filename
	 */
	
	public ARXMLGrabData() {
		timer = Timer.getTimer(); //there is only one timer in application, this fn retrieves it.
		testdate = new Date();
		starttime = new Date(); //should probably think of better time & place to set this
	}
	
	
		
	/**
	 * adds the grab data to existing DOM document.
	 * checks if there is an existing element with this name and
	 * overwrites it
	 * @param doc - the current DOM settings document 
	 */
	public Document writeGrabData(Document doc){
		
		NodeList origDoc = doc.getElementsByTagName(TOPLEVEL_NODE);
		Element topElement =  (Element) origDoc.item(0);
		if (topElement == null){
			topElement = doc.createElement(TOPLEVEL_NODE);
			doc.appendChild(topElement);
		}
		
		NodeList oldGrabList = topElement.getElementsByTagName(GRAB_SESSION_DETAILS_NODE);
		Node oldGrabData = oldGrabList.item(0);
		
		Element e1 = doc.createElement(GRAB_SESSION_DETAILS_NODE);
		//if (oldGrabData != null)
//		{
//			topElement.replaceChild(e1, oldGrabData);
//		}else
		{
			topElement.appendChild(e1);
		}	

		//add the id for this participant
		Element e11 = doc.createElement(ID_NODE);
		e11.setTextContent(ID);
		e1.appendChild(e11);
		
		//add the model set
		Element e12 = doc.createElement(MODELSET_NODE);
		e12.setTextContent(modelset);
		e1.appendChild(e12);

		//add the model set
		Element e12b = doc.createElement(EXPERIMENTER_NODE);
		e12b.setTextContent(experimenter);
		e1.appendChild(e12b);

		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		DateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
	
		//add  current date
		Element e13 = doc.createElement(TESTDATE_NODE);
		e13.setTextContent(dateFormat.format(testdate));
		e1.appendChild(e13);
		
		//start time (approximate - accurate logging in sort and grob data itself)
		Element e14 = doc.createElement(TESTSTART_NODE);
		e14.setTextContent(timeFormat.format(starttime));
		e1.appendChild(e14);
		
		//end time (approximate - accurate logging in sort and grob data itself)
		endtime = new Date();
		Element e14b = doc.createElement(TESTSTART_NODE);
		e14b.setTextContent(timeFormat.format(endtime));
		e1.appendChild(e14b);
		
		
		
		Element e15 = doc.createElement(STARTING_POSITIONS);
		e1.appendChild(e15);
		for(int i=0; i<startingpositions.size();i++)
		{
			Element e15i = writeGrabEvent(doc,startingpositions.get(i));
			e15.appendChild(e15i);
		}
		
		Element e16 = doc.createElement(GRABBED_POSITIONS);
		e1.appendChild(e16);
		for(int i=0; i<allgrabs.size();i++)
		{
			Element e16i = writeGrabEvent(doc,allgrabs.get(i));
			e16.appendChild(e16i);
		}

		Element e17 = doc.createElement(PLAYER_TRAJECTORY);
		e1.appendChild(e17);
		for(int i=0; i<playertrajectory.size();i++)
		{
			Element e17i = writePlayerLocation(doc,playertrajectory.get(i));
			e17.appendChild(e17i);
		}
		
		return doc;

	}
	
	/**
	 * writes the xml for a single grab event 
	 * 
	 * @param doc - xml document we are working with
	 * @param ge - a single grab event
	 * @return the element to add to the document
	 */
	public Element writeGrabEvent(Document doc, GrabEvent ge){
		Element grab = doc.createElement(GRAB_NODE);
		Element g1 = doc.createElement(ALIEN_NAME_NODE);
		g1.setTextContent(ge.alienname);
		grab.appendChild(g1);
		Element g2 = doc.createElement(ALIEN_ID_NODE);
		g2.setTextContent(ge.alienid);
		grab.appendChild(g2);
		Element g3 = doc.createElement(EVENTTIME_NODE);
		g3.setAttribute(TICKS_VALUE,Long.toString(ge.clockTicks));
		g3.setAttribute(SECS_VALUE,Float.toString(ge.timeInSecs));
		grab.appendChild(g3);
		Element g4 = doc.createElement(LOCATION_NODE);
		g4.setAttribute("x",Float.toString(ge.x_location));
		g4.setAttribute("z",Float.toString(ge.z_location));
		grab.appendChild(g4);	
		Element g5 = doc.createElement(BLOCK_NODE);
		g5.setTextContent(String.valueOf(ge.block));
		grab.appendChild(g5);
		Element g6 = doc.createElement(ROUND_NODE);
		g6.setTextContent(String.valueOf(ge.round));
		grab.appendChild(g6);
		return grab;
	}

	/**
	 * writes the xml for a single grab event 
	 * 
	 * @param doc - xml document we are working with
	 * @param ge - a single grab event
	 * @return the element to add to the document
	 */
	public Element writePlayerLocation(Document doc, PlayerLocation pl){
		Element playerloc = doc.createElement(GRAB_NODE);
		Element g1 = doc.createElement(VEHICLE_NAME_NODE);
		playerloc.setTextContent(pl.name);
		playerloc.appendChild(g1);
		Element g2 = doc.createElement(EVENTTIME_NODE);
		g2.setAttribute(TICKS_VALUE,Long.toString(pl.clockTicks));
		g2.setAttribute(SECS_VALUE,Float.toString(pl.timeInSecs));
		playerloc.appendChild(g2);
		Element g3 = doc.createElement(LOCATION_NODE);
		g3.setAttribute("x",Float.toString(pl.x_location));
		g3.setAttribute("z",Float.toString(pl.z_location));
		playerloc.appendChild(g3);	
		Element g4 = doc.createElement(VELOCITY_NODE);
		g4.setAttribute("x",Float.toString(pl.x_velocity));
		g4.setAttribute("z",Float.toString(pl.z_velocity));
		playerloc.appendChild(g4);	
		return playerloc;
	}

	public void addStartingPosition(GrabEvent ge){
		startingpositions.add(ge);
	}
	
	
	public void addGrabEvent(GrabEvent ge){
		//add a time stamp if one has been forgotten
		if (ge.clockTicks <= 0){
			ge.clockTicks = timer.getTime();
			ge.timeInSecs = ge.clockTicks * 1f / timer.getResolution();
		}
		allgrabs.add(ge);
	}
	
	public void logPlayerLocation(PlayerLocation pl){
		//add a time stamp if one has been forgotten
		if (pl.clockTicks <= 0){
			pl.clockTicks = timer.getTime();
			pl.timeInSecs = pl.clockTicks * 1f / timer.getResolution();
		}
		playertrajectory.add(pl);
	}
	
	/**
	 * A small helper class to hold a single grab event
	 * @author monkey
	 *
	 */
	public class GrabEvent{
		public String alienname;
		public String alienid;
		public long clockTicks;
		public int block;
		public int round;
		public float timeInSecs;
		public float x_location; 
		//public float y_location; //only move in the horizontal plane 
		public float z_location;
		public GrabEvent(){}	
	}
	
	/** 
	 * A helper class that will be used if we decide to log the player's
	 * movements during this phase of the experiment. Most likely this will
	 * be achieved by taking snapshots of player location and velocity at 
	 * regular time intervals. 
	 * @author monkey
	 *
	 */
	public class PlayerLocation{
		public String name;
		public long clockTicks;
		public float timeInSecs;
		public float x_location;
		//public float y_location; //only move in the horizontal plane 
		public float z_location;
		public float x_velocity;
		//public float y_velocity; //only move in the horizontal plane 
		public float z_velocity;
	}
	
}

