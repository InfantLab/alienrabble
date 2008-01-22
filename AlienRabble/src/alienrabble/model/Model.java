package alienrabble.model;

import java.util.Hashtable;

import com.jme.math.Vector3f;
import com.jme.scene.Spatial;


/**
 * Holds data about the individual objects; their name, the location
 * of the jbin file (if there is one), the objects dimensions and other 
 * categorical properties.
 * 
 * @author monkey
 *
 */
public class Model {
	
	private String name;
	private String location;
	private boolean hasmodel;
	private Vector3f scale;
	private Spatial model;
	private Hashtable<String, String> properties; //categorical properties as set of name value pairs
	private Hashtable<String, Float> dimensions; //scalar dimensional properties as name value pairs
	
	public Model(){
		properties = new Hashtable<String, String>();
		dimensions = new Hashtable<String, Float>();
	}
	
	public Model(String name)
	{
		this();
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	public void setName(String name){
		this.name = name;
	}

	public String getLocation(){
		return location;
	}
	public void setLocation(String location){
		this.location = location;
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
	
	
}
