package alienrabble.model;

import java.io.File;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;



public class ARXMLModelData {
	private static final long serialVersionUID = 1L;
	private static final Logger logger 
			= Logger.getLogger(ARXMLModelData.class.getName());
	
	//the names of the various node types in our xml doc
	public static final String TOPLEVEL_NODE = "alienrabbledata";
	public static final String MODELSET_NODE = "modelset";
	public static final String MODELDATA_NODE = "modeldata";
	public static final String NAME_NODE = "name";
	public static final String ISDYNAMIC_NODE = "isdynamic";
	public static final String LOCATION_NODE = "location";
	public static final String DIMENSION_NODE = "dimension";
	public static final String PROPERTY_NODE = "property";
	public static final String ROTATION_NODE = "rotation"; //specific nodes for rotation property
	public static final String SCALE_NODE = "scale";
	
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
		  logger.info("Loading model path information");
		  File file = new File(filename);
		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		  DocumentBuilder db = dbf.newDocumentBuilder();
		  Document doc = db.parse(file);
		  doc.getDocumentElement().normalize();
		  System.out.println("Root element " + doc.getDocumentElement().getNodeName());
		  NodeList nodeList = doc.getElementsByTagName(MODELSET_NODE);
		  System.out.println("Information of all models");
		  
		  Element nodeModelSet = (Element) getNamedNodeFromList(nodeList, this.modelset);

		  //this mess reads the model information out of the DOM object into our model object
		  NodeList modelList = nodeModelSet.getElementsByTagName(MODELDATA_NODE);
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
				NodeList locationList = firstElement.getElementsByTagName(LOCATION_NODE);
				Element locationElement = (Element) locationList.item(0);
				NodeList locationItem = locationElement.getChildNodes();
				System.out.println("Last Name : " + ((Node) locationItem.item(0)).getNodeValue());
				allModels[s].setBinaryLocation( ((Node) locationItem.item(0)).getNodeValue());
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
				for (int t = 0; t<  dimensionsList.getLength(); t++){
					Element thisdimension = (Element) dimensionsList.item(t);
					String name = thisdimension.getAttribute("name");
					Float value = Float.valueOf(thisdimension.getAttribute("value"));
					allModels[s].setDimension(name,value);
				}
				// get the Quaternion that rotates the object initially
				NodeList rotationsList = firstElement.getElementsByTagName(ROTATION_NODE);
				float x = 0f,y = 0f, z = 0f, w = 1f; //the identity rotation
				if (rotationsList.getLength()>0){
					Element rotation = (Element) rotationsList.item(0);
					if (rotation.hasAttribute("x"))	x = Float.valueOf(rotation.getAttribute("x"));
					if (rotation.hasAttribute("y"))	y = Float.valueOf(rotation.getAttribute("y"));
					if (rotation.hasAttribute("z"))	z = Float.valueOf(rotation.getAttribute("z"));
					if (rotation.hasAttribute("w"))	w = Float.valueOf(rotation.getAttribute("w"));
				}
				Quaternion q = new Quaternion(x,y,z,w);
				allModels[s].setInitialRotation(q);
				
				//finally get the Vector3f that scales the object initially
				x = 1f;
				y = 1f; 
				z = 1f;
				NodeList scaleList = firstElement.getElementsByTagName(SCALE_NODE);
				if (scaleList.getLength()>0){
					Element scale = (Element) scaleList.item(0);
					if (scale.hasAttribute("x"))	x = Float.valueOf(scale.getAttribute("x"));
					if (scale.hasAttribute("y"))	y = Float.valueOf(scale.getAttribute("y"));
					if (scale.hasAttribute("z"))	z = Float.valueOf(scale.getAttribute("z"));
				}
				Vector3f sc = new Vector3f(x,y,z);
				allModels[s].setInitialScale(sc);
				
		
				//check if this is dynamically created or a loaded binary model
				NodeList tempList = firstElement.getElementsByTagName(ISDYNAMIC_NODE);
				Element firstTempElement = (Element) tempList.item(0);
				NodeList fstDyn = firstTempElement.getChildNodes();
				String isDynamicStr = fstDyn.item(0).getNodeValue();
				if ( isDynamicStr.toUpperCase().compareTo("TRUE")==0) {
					allModels[s].setIsDynamicAlien(true);
					allModels[s].createDynamicExemplar();
				}else{
					allModels[s].setIsDynamicAlien(false);
					//now try to load this binary 
					allModels[s].loadModelBinary();
				}
							
			}
		  }
		  } catch (Exception e) {
		    e.printStackTrace();
		  }
	}
	

	
	
	/**
	 * utility function to find the Node with given name attribute
	 * if there is no such node or nodes do not have a name attribute
	 * @param nodeList
	 * @return the Node with the specified "name" attribute 
	 */
	public Node getNamedNodeFromList(NodeList nodeList, String namedNode){

		  //there are several model sets, we select named one 
		  for (int s = 0; s < nodeList.getLength(); s++){
			  Element thismodelset = (Element) nodeList.item(s);
			  String modelsetname = thismodelset.getAttribute("name");
			  if (namedNode.contentEquals(modelsetname)){
				  return nodeList.item(s);
			  }  
		  }
		  return null; //nothing of that name found
	}
	
	/**
	 * adds the model data to existing DOM document.
	 * checks if there is an existing element with this name and
	 * overwrites it
	 * @param doc - the current DOM settings document 
	 */
	public Document addModelData(Document doc){
		
		NodeList origDoc = doc.getElementsByTagName(TOPLEVEL_NODE);
		Element topElement =  (Element) origDoc.item(0);
		if (topElement == null){
			topElement = doc.createElement(TOPLEVEL_NODE);
			doc.appendChild(topElement);
		}
		
		NodeList allmodelsets = topElement.getElementsByTagName(MODELSET_NODE);
		Node oldModelSet = getNamedNodeFromList(allmodelsets, this.modelset);
		
		Element e1 = doc.createElement(MODELSET_NODE);
		e1.setAttribute(NAME_NODE,this.modelset);
		if (oldModelSet != null)
		{
			topElement.replaceChild(e1, oldModelSet);
		}else{
			topElement.appendChild(e1);
		}
		
		for(int s = 0; s < numModels; s++){
			Element e11 = doc.createElement(MODELDATA_NODE);
			e1.appendChild(e11);
			//add the name of this model
			Element e111 = doc.createElement(NAME_NODE);
			e111.setTextContent(allModels[s].getName());
			e11.appendChild(e111);
			//add location
			Element e112 = doc.createElement(LOCATION_NODE);
			e112.setTextContent(allModels[s].getBinaryLocation());
			e11.appendChild(e112);
			//add all properties
		    Enumeration<String> p = allModels[s].getAllProperties();
		    while (p.hasMoreElements()) {
		      String key = p.nextElement();
		      Element e113 = doc.createElement(PROPERTY_NODE);
		      e113.setAttribute(key, allModels[s].getProperty(key));
		      e11.appendChild(e113);
		    }
			//add all dimensions
		    Enumeration<String> d = allModels[s].getAllDimensions();
		    while (d.hasMoreElements()) {
		      String key = d.nextElement();
		      Element e114 = doc.createElement(DIMENSION_NODE);
		      e114.setAttribute(key, allModels[s].getDimension(key).toString());
		      e11.appendChild(e114);
		    }
		    //TODO add the scale and rotation info
		}
	
		return doc;
	}	
	
	public int getNumModels(){
		return numModels;
	}
	
	public Model getModel(int idx){
		if ((idx < 0)||(idx >= numModels)) return null;
		
		return allModels[idx];		
	}
	
	public String getModelSetName(){
		return modelset;
	}
	public void setModelSetName(String name){
		modelset = name;
	}
}
