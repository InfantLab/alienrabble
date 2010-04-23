package alienrabble.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Logger;

import alienrabble.dynamicalien.DynamicExemplar;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.util.export.Savable;
import com.jme.util.export.binary.BinaryExporter;
import com.jme.util.export.binary.BinaryImporter;
import com.jmex.model.converters.AseToJme;
import com.jmex.model.converters.FormatConverter;
import com.jmex.model.converters.MaxToJme;
import com.jmex.model.converters.Md2ToJme;
import com.jmex.model.converters.Md3ToJme;
import com.jmex.model.converters.MilkToJme;
import com.jmex.model.converters.ObjToJme;
import com.jmex.model.util.ModelLoader;


/**
 * Holds data about the individual objects; their name, the binaryLocation
 * of the jbin file (if there is one), the objects dimensions and other 
 * categorical properties.
 * 
 * @author Caspar Addyman
 *
 */
public class Model extends Node {
	
	private static final Logger logger 
	= Logger.getLogger(ARXMLModelData.class.getName());
	private static final long serialVersionUID = 1L;
	private String ID;
	private String name;
	private String binaryLocation;
	private boolean hasmodel;
	private Spatial model;
	private Hashtable<String, String> properties; //categorical properties as set of name value pairs
	private Hashtable<String, Float> dimensions; //scalar dimensional properties as name value pairs
	private Quaternion initialRotation;
	private Vector3f initialScale;
	private boolean isDynamicAlien;
	private DynamicExemplar dynamicAlien;
	private int whichCategory; 
	private int points;
	
	public Model(){
		properties = new Hashtable<String, String>();
		dimensions = new Hashtable<String, Float>();
		binaryLocation = null;
		hasmodel = false;
		whichCategory = 0;
		initialRotation = new Quaternion(0,0,0,0);
		initialScale = new Vector3f(1,1,1);
		points = 10;
	}
	
	public Model(String name)
	{
		this();
		this.name = name;
		this.ID = name;
	}
	
	public String getName(){
		return name;
	}
	public void setName(String name){
		this.name = name;
	}

	/*
	 * the value for the category that this object is assigned to 
	 * read from the dimensions in the xml file.
	 */
	public int Category(){
		return whichCategory;
	}
	
	public int Points(){
		return points;
	}
	
	public void setPoints(int alienPoints){
		points = alienPoints;
	}
	
	public boolean IsDynamicAlien(){
		return isDynamicAlien;
	}
	public void setIsDynamicAlien(boolean isDynamicAlien){
		this.isDynamicAlien = isDynamicAlien;
	}
	
	public String getID(){
		return ID;
	}
	public void setID(String ID){
		this.ID = ID;
	}
	
	public Quaternion getInitialRotation(){
		return initialRotation;
	}
	public void setInitialRotation(Quaternion q){
		this.initialRotation = q;
	}
	
	public Vector3f getInitialScale(){
		return initialScale;
	}
	public void setInitialScale(Vector3f s){
		this.initialScale = s;
	}
	
	public String getBinaryLocation(){
		return binaryLocation;
	}
	public void setBinaryLocation(String binaryLocation){
		this.binaryLocation = binaryLocation;
	}
	
	
	public int numProperties(){
		return properties.size();
	}
	public int numDimensions(){
		return dimensions.size();
	}
	
	public void setProperty(String name, String value){
		properties.put(name, value);
	}
	public void setDimension(String name, Float value){
		dimensions.put(name, value);
	}
	public String getProperty(String name){
		return properties.get(name);
	}
	
	public Float getDimension(String name){
		return dimensions.get(name);
	}	
	
	public Enumeration<String> getAllProperties(){
		return properties.keys();
	}

	public Enumeration<String> getAllDimensions(){
		return dimensions.keys();
	}

    /*
     *  This method opens a model in various format evaluating the extension
     *  In case in the same directory is already presents the same model in jbin format loads it
     *  Otherways load the model and save a jbin copy for the next time.
     *  
     *  Attention : in case the original model is changed you'll have to delete the jbin one the reload it. 
     */	
	public boolean loadModelBinary(){
		if (binaryLocation == null) return false;
		if (hasmodel == true) return true;

	   	ByteArrayOutputStream 	BO 		= new ByteArrayOutputStream();
    	Spatial			loadedModel	= null;
    	FormatConverter	formatConverter = null;		
    	String			modelFormat 	= binaryLocation.substring(binaryLocation.lastIndexOf(".") + 1, binaryLocation.length());
    	String			modelBinary	= binaryLocation.substring(0, binaryLocation.lastIndexOf(".") + 1) + "jbin";
    	URL				alienURL	= ModelLoader.class.getClassLoader().getResource(modelBinary);

    	BinaryImporter BI = new BinaryImporter();

    	
    	//verify the presence of the jbin model
    	if (alienURL == null){
    		
    		alienURL		= ModelLoader.class.getClassLoader().getResource(binaryLocation);
    		
    		//evaluate the format
    		if (modelFormat.equals("3ds")){
    			formatConverter = new MaxToJme();
    		} else if (modelFormat.equals("md2")){
    			formatConverter = new Md2ToJme();
    		} else if (modelFormat.equals("md3")){
    			formatConverter = new Md3ToJme();
    		} else if (modelFormat.equals("ms3d")){
    			formatConverter = new MilkToJme();
    		} else if (modelFormat.equals("ase")){
    			formatConverter = new AseToJme();
    		} else if (modelFormat.equals("obj")){
    			formatConverter = new ObjToJme();
    		}
    		formatConverter.setProperty("mtllib", alienURL);
    		
    		try {
    			formatConverter.convert(alienURL.openStream(), BO);
    			model = (Spatial) BI.load(new ByteArrayInputStream(BO.toByteArray()));
    			
    			//save the jbin format
    			BinaryExporter.getInstance().save((Savable)loadedModel, new File(modelBinary));
    		} catch (IOException e) {				
    			e.printStackTrace();
    			return false;
    		}
    	}else{
    		try {
    			//load the jbin format
    			model = (Spatial) BI.load(alienURL.openStream());
    		} catch (IOException e) {
    			logger.severe("failed to load model binary" + e.getMessage());
    			return false;
    		}
    	}
    	
  
		this.attachChild(model);
		this.setLocalRotation(initialRotation);
		this.setLocalScale(initialScale);
		whichCategory = getDimension("Category").intValue();
		hasmodel = true;
		return true;
	}		

	
	public boolean createDynamicExemplar(){
		if (!isDynamicAlien) return false;
		if (hasmodel == true) return true;
		
		//set up the details for this alien
		dynamicAlien = new DynamicExemplar(this.getName());
		
		//body
		dynamicAlien.setBodyHeight(getDimension("BodyHeight"));
		dynamicAlien.setBodyWidth(getDimension("BodyWidth"));
		dynamicAlien.setBodyType(getDimension("BodyType").intValue());
		
		//legs
		dynamicAlien.setLegType(getDimension("LegType").intValue());
		dynamicAlien.setLegCount(getDimension("LegCount").intValue());
		
		//arms
		dynamicAlien.setArmType(getDimension("ArmType").intValue());
		dynamicAlien.setArmSize(getDimension("ArmSize"));
		
		//Stripes
		dynamicAlien.setStripeColours(getDimension("StripeColours").intValue());
		dynamicAlien.setStripeAngle(getDimension("StripeAngle").intValue());
		dynamicAlien.setStripeFreq(getDimension("StripeFreq").intValue());

		//eyes
		dynamicAlien.setEyeCount(getDimension("EyeCount").intValue());
		dynamicAlien.setEyeSize(getDimension("EyeSize"));

		dynamicAlien.setUpExemplar();
		this.attachChild(dynamicAlien);
		this.setLocalRotation(initialRotation);
		this.setLocalScale(initialScale);
		
		whichCategory = getDimension("Category").intValue();
		points = getDimension("Score").intValue();
		
		hasmodel = true;
		return true;
	}
	
	public void reset(){
		this.detachAllChildren();
		hasmodel = false;
		binaryLocation = null;
	}
}
	
