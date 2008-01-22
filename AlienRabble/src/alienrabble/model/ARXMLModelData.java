package alienrabble.model;

import java.io.File;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class ARXMLModelData {
	private static final long serialVersionUID = 1L;
	private static final Logger logger 
			= Logger.getLogger(ARXMLModelData.class.getName());
	
	//the names of the various node types in our xml doc
	public static final String MODELSET_NODE = "modelset";
	public static final String NAME_NODE = "name";
	public static final String LOCATION_NODE = "location";
	public static final String DIMENSION_NODE = "dimension";
	public static final String PROPERTY_NODE = "property";
	
	private int numModels;
	private Model[] allModels;
	private String filename; // the file containing model info
	private String modelset; // the name attribute for the model set we want to use

	public ARXMLModelData(String filename) {
		this.filename = filename;
		this.modelset = ""; //default model set
		this.numModels = 0;
		this.allModels = null;
	}
	
	public ARXMLModelData(String filename, String modelset) {
		this.filename = filename;
		this.modelset = modelset;
		this.numModels = 0;
		this.allModels = null;
	}
	
	public void loadModelPaths(){
	  try {
		  File file = new File(filename);
		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		  DocumentBuilder db = dbf.newDocumentBuilder();
		  Document doc = db.parse(file);
		  doc.getDocumentElement().normalize();
		  System.out.println("Root element " + doc.getDocumentElement().getNodeName());
		  NodeList nodeList = doc.getElementsByTagName("modelset");
		  System.out.println("Information of all models");
		  
		  Node nodeModelSet = null;
		  if ((nodeList.getLength() == 1) || (this.modelset.length() == 0)){
			  //if there is just one model set or no specified named set
			  //we just use first set
			  nodeModelSet = nodeList.item(0);
		  }
		  else{
			  //there are several model sets, we select named on 
			  for (int s = 0; s < nodeList.getLength(); s++){
				  Element thismodelset = (Element) nodeList.item(s);
				  String modelsetname = thismodelset.getAttribute("name");
				  if (this.modelset.contentEquals(modelsetname)){
					  nodeModelSet = nodeList.item(s);
					  break;
				  }  
			  }
			  if (nodeModelSet == null){
				  logger.info("Could not find named model set using first available");
				  nodeModelSet = nodeList.item(0);
			  }
		  }
		  
		  //this mess reads the model information out of the DOM object into our model object
		  NodeList modelList = nodeModelSet.getChildNodes();
		  numModels = modelList.getLength();
		  allModels = new Model[numModels];
		  for (int s = 0; s < numModels; s++) {

			Node firstNode = modelList.item(s);
					    
			if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
				Element firstElement = (Element) firstNode;
				//get the name of this model
				NodeList modelDataList = firstElement.getElementsByTagName(NAME_NODE);
				Element firstModelElement = (Element) modelDataList.item(0);
				NodeList fstNm = firstModelElement.getChildNodes();
				System.out.println("Model Name : "  + ((Node) fstNm.item(0)).getNodeValue());
				allModels[s] = new Model(((Node) fstNm.item(0)).getNodeValue());
				//URI/location for the model binary
				NodeList lstNmElmntLst = firstElement.getElementsByTagName(LOCATION_NODE);
				Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
				NodeList lstNm = lstNmElmnt.getChildNodes();
				System.out.println("Last Name : " + ((Node) lstNm.item(0)).getNodeValue());
				allModels[s].setLocation( ((Node) fstNm.item(0)).getNodeValue());
				//now loop through all the properties and set them on the model object
				NodeList propertiesList = firstElement.getElementsByTagName(PROPERTY_NODE);
				for (int t = 0; t<  propertiesList.getLength(); t++){
					Element thisproperty = (Element) propertiesList.item(t);
					String name = thisproperty.getAttribute("name");
					String value = thisproperty.getAttribute("value");
					allModels[s].setProperty(name,value);
				}
				//now loop through all the properties and set them on the model object
				NodeList dimensionsList = firstElement.getElementsByTagName(DIMENSION_NODE);
				for (int t = 0; t<  propertiesList.getLength(); t++){
					Element thisdimension = (Element) dimensionsList.item(t);
					String name = thisdimension.getAttribute("name");
					String value = thisdimension.getAttribute("value");
					allModels[s].setProperty(name,value);
				}
			}
		  }
		  } catch (Exception e) {
		    e.printStackTrace();
		  }
	}
}
