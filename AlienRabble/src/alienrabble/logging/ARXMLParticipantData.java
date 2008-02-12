package alienrabble.logging;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import alienrabble.MainGameStateSystem;


/**
 * This class handles information about our participant, loading the 
 * details from the initialization file and allowing them to be saved 
 * with the performance data in the output file. 
 * 
 * @author monkey
 *
 */
public class ARXMLParticipantData{
	private static final long serialVersionUID = 1L;
	private static final Logger logger 
			= Logger.getLogger(ARXMLParticipantData.class.getName());

	
	//the names of the various node types in our xml doc
	public static final String TOPLEVEL_NODE = "alienrabbledata";
	public static final String ARVERSION_NODE = "AlienRabbleVersion";
	public static final String NAME_NODE = "name";
	public static final String ID_NODE = "id";
	public static final String DOB_NODE = "dob";
	public static final String TESTDATE_NODE = "testdate";
	public static final String TESTSTART_NODE = "starttime";
	public static final String TESTEND_NODE = "endtime";
	public static final String MODELSET_NODE = "modelset";
	public static final String MODELSET_GRAB = "grab";
	public static final String MODELSET_SORT = "sort";
	public static final String EXPERIMENTER_NODE = "experimenter";
	public static final String PARTICIPANT_NODE = "participant";

	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String TIME_FORMAT = "hh:mm:ss";

	
	private String initfile; // the initialization file 
	
	private String name; // participant's name
	private String ID;   // ID number
	private Date dob;	// date of birth
	private Date testdate; // testing date
	private Date starttime; // when testing began
	private Date endtime; // when testing ended
	private String modelset_Grab; //which modelset do we use in grab phase
	private String modelset_Sort; //which modelset do we use in sort phase
	
	/**
	 * 
	 * @param filename
	 */
	
	public ARXMLParticipantData(String initfile) {
		this.initfile = initfile;
		this.starttime = new Date();
		this.testdate = new Date();
	
	}
	
	
	public String getID(){
		return this.ID;
	}
	
	public void loadParticipantInit(){
	try {
		File file = new File(initfile);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();
		logger.info("Information for this particpant");
		logger.info("Root element " + doc.getDocumentElement().getNodeName());
		NodeList nodeList = doc.getElementsByTagName(PARTICIPANT_NODE);
		Element participant =  (Element) nodeList.item(0);
				
		//get the name of participant
		NodeList names = participant.getElementsByTagName(NAME_NODE);
		Element name = (Element) names.item(0);
		this.name = name.getTextContent();
		
		//get ID 
		NodeList ids = participant.getElementsByTagName(ID_NODE);
		Element id = (Element) ids.item(0);
		this.ID = id.getTextContent();
		
		//get dob 
		NodeList dobs = participant.getElementsByTagName(DOB_NODE);
		Element dob = (Element) dobs.item(0);
		String strdob = dob.getTextContent();
		this.dob = new SimpleDateFormat(DATE_FORMAT).parse(strdob);
		
		//get the name of participant
		NodeList modelsets = participant.getElementsByTagName(MODELSET_NODE);
		for (int t = 0; t<  modelsets.getLength(); t++){
			Element thisproperty = (Element) modelsets.item(t);
			String mname = thisproperty.getAttribute("name");
			String value = thisproperty.getAttribute("value");
			if (mname.equals(MODELSET_SORT)){
				modelset_Sort = value;
			}else if (mname.equals(MODELSET_GRAB)){
				modelset_Grab = value;
			}
		}
		} 
	catch (Exception e) {
			e.printStackTrace();
			logger.severe(e.getMessage());
		}
	}
	
    public String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date date = new Date();
        return dateFormat.format(date);
    }
    
    public String getCurrentTime() {
        DateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
        Date date = new Date();
        return dateFormat.format(date);
    }
	
    public String getName(){
    	return name;
    }
 
    public String getModelSet_Grab(){
    	return modelset_Grab;
    }
 		
    public String getModelSet_Sort(){
    	return modelset_Sort;
    }

    /**
	 * adds the participant data to existing DOM document.
	 * checks if there is an existing element with this name and
	 * overwrites it
	 * @param doc - the current DOM settings document 
	 */
	public Document addParticipantData(Document doc){
		
		NodeList origDoc = doc.getElementsByTagName(TOPLEVEL_NODE);
		Element topElement =  (Element) origDoc.item(0);
		if (topElement == null){
			topElement = doc.createElement(TOPLEVEL_NODE);
			doc.appendChild(topElement);
		}
		
		
		NodeList oldPartList = topElement.getElementsByTagName(PARTICIPANT_NODE);
		Node oldParticipant = oldPartList.item(0);
		
		Element e1 = doc.createElement(PARTICIPANT_NODE);
		if (oldParticipant != null)
		{
			topElement.replaceChild(e1, oldParticipant);
		}else{
			topElement.appendChild(e1);
		}	

		//add the name of this participant
		Element e11 = doc.createElement(NAME_NODE);
		e11.setTextContent(name);
		e1.appendChild(e11);
		
		//add the ID
		Element e12 = doc.createElement(ID_NODE);
		e12.setTextContent(ID);
		e1.appendChild(e12);

		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		DateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);

		//add  dob
		Element e13 = doc.createElement(DOB_NODE);
		e13.setTextContent(dateFormat.format(dob));
		e1.appendChild(e13);
		
		//add  current date
		Element e14 = doc.createElement(TESTDATE_NODE);
		e14.setTextContent(dateFormat.format(testdate));
		e1.appendChild(e14);
		
		//start time (approximate - accurate logging in sort and grob data)
		Element e15 = doc.createElement(TESTSTART_NODE);
		e15.setTextContent(timeFormat.format(starttime));
		e1.appendChild(e15);
		
		//end time - which is right now!
		endtime = new Date();
		Element e16 = doc.createElement(TESTEND_NODE);
		e16.setTextContent(timeFormat.format(endtime));
		e1.appendChild(e16);
		
		//add the ID
		Element e17 = doc.createElement(ARVERSION_NODE);
		e17.setTextContent(MainGameStateSystem.ARVersionNumber);
		e1.appendChild(e17);

		//add the model set this participant saw.
		Element e18 = doc.createElement(MODELSET_NODE);
		e1.setAttribute(MODELSET_GRAB, modelset_Grab);
		e1.setAttribute(MODELSET_SORT, modelset_Sort);
	    e1.appendChild(e18);
		
		return doc;
	}
}
