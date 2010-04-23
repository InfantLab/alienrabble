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
 * This class handles information about our experimental setup and 
 * our participant, loading the details from the initialization file 
 * and allowing them to be saved 
 * with the performance data in the output file. 
 * 
 * 
 * @author Caspar Addyman
 *
 */
public class ARXMLExperimentData{
	private static final long serialVersionUID = 1L;
	private static final Logger logger 
			= Logger.getLogger(ARXMLExperimentData.class.getName());

	public enum GameType {RULEDISCOVERY, GRABONLY, SORTONLY, GRABANDSORT, SAMEDIFF};
	
	//the names of the various node types in our xml doc
	public static final String TOPLEVEL_NODE = "alienrabbledata";
	public static final String ARVERSION_NODE = "AlienRabbleVersion";
	public static final String NAME_NODE = "name";
	public static final String ID_NODE = "id";
	public static final String DOB_NODE = "dob";
	public static final String TESTDATE_NODE = "testdate";
	public static final String TESTSTART_NODE = "starttime";
	public static final String TESTEND_NODE = "endtime";
	public static final String MODELFILE_NODE = "modelfile";
	public static final String MODELSET_NODE = "modelset";
	public static final String MODELSET_GRAB = "grab";
	public static final String MODELSET_SORT = "sort";
	public static final String EXPERIMENTSETUP_NODE = "experimentsetup";
	public static final String EXPERIMENTER_NODE = "experimenter";
	public static final String TIMEGAUGE_NODE = "timegauge";
	public static final String GAMETYPE_NODE = "gametype";
	public static final String SHOWSCORES_NODE = "showscores";
	public static final String ARENASIZE_NODE = "arenasize";
	public static final String NUMBEROFROUNDS_NODE = "numrounds";
	public static final String NUMBEROFTREES_NODE = "numtrees";
	public static final String PARTICIPANT_NODE = "participant";
	public static final String DYNAMIC = "dynamic1";
	

	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String TIME_FORMAT = "hh:mm:ss";

	
	
	private String initfile; // the initialization file 
	
	//experimental setup data
	private String gamedesc; // rule discovery or grab/+sorting
	private String experimenter;   // name of experimenter
	private int timeGauge;	// how long timer goes for on a single round in seconds, 0 = no time gauge
	private int showScores;	//  0 = smiley face feedback, 1 = show the score for each exemplar
	private int arenaSize;	// how big is the the arena?
	private int numTrees;	// how many distractor trees will there be in grab phase
	private int numRounds; // how many times will they get 
	private String modelfile; //the name of xml file containing model descriptions.
	
	//participant related data
	private String name; // participant's name
	private String ID;   // ID number
	private Date dob;	// date of birth
	private Date testdate; // testing date
	private Date starttime; // when testing began
	private Date endtime; // when testing ended
	private String modelset_Grab; //which modelset do we use in grab phase
	private String modelset_Sort; //which modelset do we use in sort phase
	
	public GameType gameType;
	
	/**
	 * 
	 * @param filename
	 */
	public ARXMLExperimentData(String initfile) {
		this.initfile = initfile;
		starttime = new Date();
		testdate = new Date();
		//default values
		gamedesc = "GRABSORT";
		gameType = getGameType();
		numTrees = 20;
		numRounds = 1;
	}
	
	public GameType getGameType(){
		if (gamedesc.toUpperCase().compareTo("RULE")==0){
			return GameType.RULEDISCOVERY;
		}else if (gamedesc.toUpperCase().compareTo("GRAB")==0){
			return GameType.GRABONLY;
		}else if (gamedesc.toUpperCase().compareTo("SORT")==0){
			return GameType.SORTONLY;
		}else {
			return GameType.GRABANDSORT;
		}
	}
	
	public String getID(){
		return this.ID;
	}

	public String getExperimenter(){
		return experimenter;
	}
	
	public int getShowScores(){
		return showScores;
	}
	
	public int getTimeGauge(){
		return timeGauge;
	}
	public int getNumTrees(){
		return numTrees;
	}
	public int getArenaSize(){
		return arenaSize;
	}
	public int getNumRounds(){
		return numRounds;
	}	
	public void loadExperimentInit(){
	try {
		File file = new File(initfile);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();
		logger.info("Information for this particpant");
		logger.info("Root element " + doc.getDocumentElement().getNodeName());
		
		/////////////////////////////
		// Experimental setup details
		/////////////////////////////
		NodeList nodeList = doc.getElementsByTagName(EXPERIMENTSETUP_NODE);
		Element expsetup =  (Element) nodeList.item(0);
		
		//get the type of game this is 
		NodeList gametypes = expsetup.getElementsByTagName(GAMETYPE_NODE);
		Element gametypeel = (Element) gametypes.item(0);
		this.gamedesc = gametypeel.getTextContent();
		gameType = getGameType();
		
		//do we give feedback with smileys or scores
		NodeList showscores = expsetup.getElementsByTagName(SHOWSCORES_NODE);
		Element showscoresel = (Element) showscores.item(0);
		try{
			this.showScores = Boolean.parseBoolean(showscoresel.getTextContent())?1:0;
		}catch (Exception e)
		{
			this.showScores = Integer.valueOf(showscoresel.getTextContent());			
		}

		//get time gauge value, 0 means no gauge
		NodeList tgauge = expsetup.getElementsByTagName(TIMEGAUGE_NODE);
		Element tgaugeel = (Element) tgauge.item(0);
		this.timeGauge = Long.valueOf(tgaugeel.getTextContent()).intValue();
		
		//get arena size
		NodeList arena = expsetup.getElementsByTagName(ARENASIZE_NODE);
		Element arenael = (Element) arena.item(0);
		this.arenaSize  =  Long.valueOf(arenael.getTextContent()).intValue();

		//get number of repetitions of everything
		NodeList rounds = expsetup.getElementsByTagName(NUMBEROFROUNDS_NODE);
		Element roundsel = (Element) rounds.item(0);
		this.numRounds  =  Long.valueOf(roundsel.getTextContent()).intValue();

		//get number of trees in grab phase
		NodeList trees = expsetup.getElementsByTagName(NUMBEROFTREES_NODE);
		Element treesel = (Element) trees.item(0);
		this.numTrees  =  Long.valueOf(treesel.getTextContent()).intValue();

		//get experimenter name
		NodeList expnames = expsetup.getElementsByTagName(EXPERIMENTER_NODE);
		Element nameel = (Element) expnames.item(0);
		this.experimenter  =  nameel.getTextContent();

		///////////////////////
		// Participant details
		///////////////////////
		nodeList = doc.getElementsByTagName(PARTICIPANT_NODE);
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
		
		//get the name of the modelfile
		NodeList models = participant.getElementsByTagName(MODELFILE_NODE);
		Element model = (Element) models.item(0);
		this.modelfile = model.getTextContent();
		
		//get the name of modelsets used for grab and for sort phases
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
			//TODO
			//Dialog.showError ("Error reading the init.xml file. Please check the contents and formating of this file.");
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

    public String getModelFile(){
    	return modelfile;
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
