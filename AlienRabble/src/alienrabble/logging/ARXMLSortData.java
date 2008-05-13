package alienrabble.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jme.util.Timer;


/**
 * This class stores the data recorded during the Sort phase of the game.
 * It stores the initial locations of the objects and the order and timing 
 * of how they are collected. 
 * 
 * @author Caspar Addyman
 *
 */
public class ARXMLSortData{
	private static final long serialVersionUID = 1L;
	private static final Logger logger 
			= Logger.getLogger(ARXMLSortData.class.getName());

	
	//the names of the various node types in our xml doc
	public static final String TOPLEVEL_NODE = "alienrabbledata";
	public static final String ARVERSION_NODE = "AlienRabbleVersion";
	public static final String SORT_SESSION_DETAILS_NODE = "SortSessionDetails";
	public static final String ID_NODE = "id";
	public static final String NAME_NODE = "name";
	public static final String MODELSET_NODE = "modelset";
	public static final String EXPERIMENTER_NODE = "experimenter";
	public static final String PARTICIPANT_NODE = "participant";
	public static final String TESTDATE_NODE = "testdate";
	public static final String STARTING_POSITIONS = "startingpositions";
	public static final String SORT_EVENTS = "sortevents";
	public static final String SORT_ORDER = "sortorder";
	public static final String SORT_GROUPS = "sortgroups";
	public static final String SORT_GROUP = "sortgroup";
	public static final String SORT_EVENT = "sortevent";
	public static final String MOUSE_CLICKS = "mouseclicks";
	public static final String MOUSE_CLICK = "mouseclick";;
	public static final String ALIEN_ID_NODE = "alienid";
	public static final String ALIEN_NAME_NODE = "alienname";
	public static final String TESTSTART_NODE = "starttime";
	public static final String EVENTTIME_NODE = "time";
	public static final String TICKS_VALUE = "ticks";
	public static final String SECS_VALUE = "secs";
	public static final String LOCATION_NODE = "location";
	public static final String CLICKED_OBJECT = "clickedobject";
	
	public static final String EVENT_TYPE = "eventtype";
	public static final String TYPE_SELECTALIEN = "selectalien";
	public static final String TYPE_DESELECTALIEN = "deselectalien";
	public static final String TYPE_SORTALIEN = "sortalien";
	public static final String TYPE_ADDBOX = "addbox";
	public static final String TYPE_REMOVEBOX = "removebox";
	public static final String TYPE_SORTRESULT = "sortresult";
	public static final String TYPE_CLICKNOTHING = "clicknothing";

	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String TIME_FORMAT = "hh:mm:ss";

	
	private String ID;   // participant's ID number
	private String modelset; //the models used in this session
	private String experimenter; 
	private Date testdate; // testing date
	private Date starttime; // when testing began
	private Date endtime; // when testing ended
	
	
	private ArrayList<MouseEvent> startingpositions = new ArrayList<MouseEvent>();
	private ArrayList<MouseEvent> mouseclicks = new ArrayList<MouseEvent>();
    private ArrayList<SortEvent> sortevents = new ArrayList<SortEvent>();
    private ArrayList<SortEvent> sortgroups = new ArrayList<SortEvent>();
    private ArrayList<SortEvent> sortorder = new ArrayList<SortEvent>();
	
	
	private Timer timer; //hold a reference to the main timer
	/**
	 * 
	 * @param filename
	 */
	
	public ARXMLSortData() {
		timer = Timer.getTimer(); //there is only one timer in application, this fn retrieves it.
		testdate = new Date();
		starttime = new Date(); //should probably think of better time & place to set this
	}
	
	
		
	/**
	 * adds the Sort data to existing DOM document.
	 * checks if there is an existing element with this name and
	 * overwrites it
	 * @param doc - the current DOM settings document 
	 */
	public Document writeSortData(Document doc){
		
		NodeList origDoc = doc.getElementsByTagName(TOPLEVEL_NODE);
		Element topElement =  (Element) origDoc.item(0);
		if (topElement == null){
			topElement = doc.createElement(TOPLEVEL_NODE);
			doc.appendChild(topElement);
		}
		
		NodeList oldSortList = topElement.getElementsByTagName(SORT_SESSION_DETAILS_NODE);
		Node oldSortData = oldSortList.item(0);
		
		Element e1 = doc.createElement(SORT_SESSION_DETAILS_NODE);
		if (oldSortData != null)
		{
			topElement.replaceChild(e1, oldSortData);
		}else{
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
		
		
		//where were the individual aliens
		Element e15 = doc.createElement(STARTING_POSITIONS);
		e1.appendChild(e15);
		for(int i=0; i<startingpositions.size();i++)
		{
			Element e15i = writeMouseEvent(doc,startingpositions.get(i));
			e15.appendChild(e15i);
		}
		
		//where did the participants click (even if they didn't click on anything)
		Element e16 = doc.createElement(MOUSE_CLICKS);
		e1.appendChild(e16);
		for(int i=0; i<mouseclicks.size();i++)
		{
			Element e16i = writeMouseEvent(doc,mouseclicks.get(i));
			e16.appendChild(e16i);
		}

		Element e17 = doc.createElement(SORT_EVENTS);
		e1.appendChild(e17);
		for(int i=0; i<sortevents.size();i++)
		{
			Element e17i = writeSortEvent(doc,sortevents.get(i));
			e17.appendChild(e17i);
		}
		
		Element e18 = doc.createElement(SORT_GROUPS);
		e1.appendChild(e18);
		for(int i=0; i<sortgroups.size();i++)
		{
			Element e18i = writeSortEvent(doc,sortgroups.get(i));
			e18.appendChild(e18i);
		}

		Element e19 = doc.createElement(SORT_ORDER);
		e1.appendChild(e19);
		for(int i=0; i<sortorder.size();i++)
		{
			Element e19i = writeSortEvent(doc,sortorder.get(i));
			e19.appendChild(e19i);
		}

		return doc;

	}
	
	/**
	 * writes the xml for a single mouse click event 
	 * 
	 * @param doc - xml document we are working with
	 * @param me - a single mouse click
	 * @return the element to add to the document
	 */
	public Element writeMouseEvent(Document doc, MouseEvent me){
		Element mouseclick = doc.createElement(MOUSE_CLICK);
		if (me.objectclicked){
			Element g1 = doc.createElement(NAME_NODE);
			g1.setTextContent(me.objectname);
			mouseclick.appendChild(g1);
			Element g2 = doc.createElement(ID_NODE);
			g2.setTextContent(me.objectid);
			mouseclick.appendChild(g2);
		}
		Element g3 = doc.createElement(EVENTTIME_NODE);
		g3.setAttribute(TICKS_VALUE,Long.toString(me.clockTicks));
		g3.setAttribute(SECS_VALUE,Float.toString(me.timeInSecs));
		mouseclick.appendChild(g3);
		Element g4 = doc.createElement(LOCATION_NODE);
		g4.setAttribute("x",Float.toString(me.x_location));
		g4.setAttribute("y",Float.toString(me.y_location));
		mouseclick.appendChild(g4);	
		return mouseclick;
	}

	/**
	 * writes the xml for a single Sort event 
	 * 
	 * @param doc - xml document we are working with
	 * @param ge - a single Sort event
	 * @return the element to add to the document
	 */
	public Element writeSortEvent(Document doc, SortEvent se){
		Element sortevent = doc.createElement(SORT_EVENT);
		sortevent.setAttribute(EVENT_TYPE, se.type);
		Element g1 = doc.createElement(NAME_NODE);
		g1.setTextContent(se.objectname);
		sortevent.appendChild(g1);
		Element g1b = doc.createElement(ID_NODE);
		g1b.setTextContent(se.objectid);
		sortevent.appendChild(g1b);
		Element g1c = doc.createElement(SORT_GROUP);
		g1c.setTextContent(String.valueOf(se.sortgroup));
		sortevent.appendChild(g1c);
		
		Element g2 = doc.createElement(EVENTTIME_NODE);
		g2.setAttribute(TICKS_VALUE,Long.toString(se.clockTicks));
		g2.setAttribute(SECS_VALUE,Float.toString(se.timeInSecs));
		sortevent.appendChild(g2);
		
		Element g3 = doc.createElement(LOCATION_NODE);
		g3.setAttribute("x",Float.toString(se.x_location));
		g3.setAttribute("y",Float.toString(se.y_location));
		sortevent.appendChild(g3);	
		return sortevent;
	}

	public void addStartingPosition(MouseEvent me){
		startingpositions.add(me);
	}
	
	
	public void addSortEvent(SortEvent se){
		//add a time stamp if one has been forgotten
		if (se.clockTicks <= 0){
			se.clockTicks = timer.getTime();
			se.timeInSecs = se.clockTicks * 1f /timer.getResolution();
		}
		sortevents.add(se);
	}
	public void addSortOrderItem(SortEvent se){
		//add a time stamp if one has been forgotten
		if (se.clockTicks <= 0){
			se.clockTicks = timer.getTime();
			se.timeInSecs = se.clockTicks * 1f /timer.getResolution();
		}
		sortorder.add(se);
	}
	public void addSortGroupItem(SortEvent se){
		//add a time stamp if one has been forgotten
		if (se.clockTicks <= 0){
			se.clockTicks = timer.getTime();
			se.timeInSecs = se.clockTicks * 1f /timer.getResolution();
		}
		sortgroups.add(se);
	}

	public void addMouseEvent(MouseEvent me){
		//add a time stamp if one has been forgotten
		if (me.clockTicks <= 0){
			me.clockTicks = timer.getTime();
			me.timeInSecs = me.clockTicks * 1f /timer.getResolution();
		}
		mouseclicks.add(me);
	}


	
	/**
	 * A small helper class to hold a single mouse click event
	 * @author monkey
	 *
	 */
	public class MouseEvent{
		public boolean objectclicked;
		public String objectname;
		public String objectid;
		public long clockTicks;
		public float timeInSecs;
		public float x_location; 
		public float y_location; 
		//public float z_location; //only move in the vertical(screen) plane 
		public MouseEvent(){}	
	}
	
	/** 
	 * A helper class that will track the actual effects of the mouse clicks.
	 * Selecting/deselecting aliens, sending them into sorting boxes, etc.
	 * Also used for to record the sort results
	 * @author monkey
	 *
	 */
	public class SortEvent{
		public String type;
		public int sortgroup;  //which group was this object placed in (if applicable)
		public String objectname;
		public String objectid;
		public long clockTicks;
		public float timeInSecs;
		public float x_location; 
		public float y_location; 
		//public float z_location; //only move in the vertical(screen) plane 
		public SortEvent(){}
	}
	
}

