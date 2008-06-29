package alienrabble.dynamicalien;

import java.io.IOException;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import alienrabble.debug.SceneGraphDump;
import alienrabble.grab.AlienRabble;
import alienrabble.logging.ARXMLGrabData.PlayerLocation;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.image.Texture;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.SharedMesh;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Capsule;
import com.jme.scene.shape.Cylinder;
import com.jme.scene.shape.Pyramid;
import com.jme.scene.shape.RoundedBox;
import com.jme.scene.shape.Sphere;
import com.jme.scene.shape.Torus;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.export.binary.BinaryImporter;

/***
 * A class that will dynamically generate a
 * @author monkey
 *
 */
public class DynamicExemplar extends Node {
	private static final long serialVersionUID = 1L;
	
	//the names for the dimensions in the xml
	public static final String TOPLEVEL_NODE = "aliendimension";
	public static final String NAME_NODE = "NAME";
	public static final String BODYINFO_NODE = "bodyinfo";
	public static final String BODYHEIGHT_NODE = "bodyheight";
	public static final String BODYWIDTH_NODE = "bodywidth";
	public static final String LEGINFO_NODE = "leginfo";
	public static final String LEGCOUNT_NODE = "legcount";
	public static final String LEGTYPE_NODE = "legtype";
	public static final String ARMTYPE_NODE = "armtype";
	public static final String ARMSIZE_NODE = "armsize";
	public static final String EYEINFO_NODE = "eyeinfo";
	public static final String EYECOUNT_NODE = "eyecount";
	public static final String EYESIZE_NODE = "eyesize";
	public static final String EYECOLOUR_NODE = "eyesize";
	public static final String STRIPEINFO_NODE = "stripeinfo";
	public static final String STRIPEANGLE_NODE = "stripeangle";
	public static final String STRIPEFREQ_NODE = "stripefreq";
	public static final String STRIPECOLOURS_NODE = "stripecolours";
	public static final String CATEGORYMEMBER_NODE = "bodycolour";
	
	//the colour constants
	public static final int YELLOWBLUE = 0;
	public static final int MAGENTACYAN = 1;
	public static final int BLACKWHITE = 2;

	//the body types
	public static final int CAPSULE = 1;
	public static final int BOX = 2;
	public static final int SPHERE = 3;
	public static final int DONUT = 4;
	public static final int LIGHTBULB = 5;
	
	
	//the variables that will store the dimensions
	//make them private so that we make sure we update
	//everything when any value is changed
	private String name;
	private int legCount; 
	private int legType;
	private float legLength;
	private int stripeAngle; // 12 values from -75¼ to +90¼
	private int stripeFreq;
	private int stripeColours;
	private int bodyType;  // for now just a cylinder allowed
	private float bodyHeight;
	private float bodyWidth;
	private int eyeCount;
	private float eyeSize;
	private int armType;
	private float armSize;
	private ColorRGBA eyeColour;
	
	private boolean created = false;
	
	private TriMesh body;
	private TriMesh arms;
	private SharedMesh legs[];
	private SharedMesh eyes[];


	public DynamicExemplar(String name){
		super(name);
		
		//set values for the default median alien
		legCount = 9; 
		legType = 1;
		legLength = 5; //hardcoded value to translate everything else this amount
		stripeAngle = 75; 
		stripeFreq = 12;
		stripeColours = MAGENTACYAN;
		bodyType = CAPSULE;
		bodyHeight = 16f;
		bodyWidth = 8f;	
		eyeCount = 2;
		eyeSize = 1;
		armType = 1;
		armSize = 1.3f;
		//eyeColour = new ColorRGBA(1,1,0,0);
        eyeColour = new ColorRGBA( 0.3f, 0.7f, 0.2f, 1f );
		created = false;
		
	}
	
	public boolean getIsCreated(){
		return created;
	}
	
	////////////
	// get set functions for the parameters 
	////////
	public void setLegCount(int n){
		legCount = n;
		created = false;
	}
	public void setLegType(int n){
		legType = n;
		created = false;
	}
	public void setStripeAngle(int n){
		if (FastMath.abs(n) < 7){
			stripeAngle = 15 * n;
		}else if ((n % 15) == 0){
			stripeAngle = n;
		}else
		{
			//hmm, not sure
		}
		created = false;
	}
	
	public void setStripeFreq(int n){
		stripeFreq = n;
		created = false;
	}
	public void setStripeColours(int n){
		stripeColours = n;
		created = false;
	}
	public void setBodyHeight(float n){
		bodyHeight = n;
		created = false;
	}
	public void setBodyWidth(float n){
		bodyWidth = n;
		created = false;
	}
	public void setBodyType(int n){
		bodyType = n;
		created = false;
	}
	public void setEyeColour(ColorRGBA col){
		eyeColour = col;
		created = false;
	}
	public void setEyeCount(int n){
		eyeCount = n;
		created = false;
	}

	public void setEyeSize(float n){
		eyeSize = n;
		created = false;
	}
	public void setArmType(int n){
		armType = n;
		created = false;
	}
	public void setArmSize(float x){
		armSize = x;
		created = false;
	}
	
	public void setUpExemplar(){
		
        float ztrans = 0;
        float ytrans = 0;
		
		switch (bodyType){
		case CAPSULE://capsule
			body = new Capsule("body", 12, 12, 12, bodyWidth/2, bodyHeight);
			break;
		case BOX://rounded box
			Vector3f boxextent = new Vector3f(bodyWidth/2,bodyHeight/2,bodyWidth/2);
			body = new RoundedBox("body",boxextent,boxextent.mult(0.3f),boxextent.mult(0.3f));
			ytrans = -6;
			break;
		case SPHERE://sphere
			body = new Sphere("body", 12, 12, bodyHeight/2);
			break;
		case DONUT:// torus (well, why not!)
			Float innerradius = FastMath.abs(bodyWidth - bodyHeight);
			Float outerradius = Math.max(bodyWidth, bodyHeight);
			body = new Torus("body", 12, 12, innerradius,outerradius);
			break;	
		case LIGHTBULB:
	        try {
			          URL bikeFile = AlienRabble.class.getClassLoader().getResource("alienrabble/data/model/lightbulbbody.jbin");
			          BinaryImporter importer = new BinaryImporter();
			          body = (TriMesh)importer.load(bikeFile.openStream());
			      } catch (IOException e) {
			      	//do something
			      }
			break;
		default:
			body = new Capsule("body", 12, 12, 12, bodyWidth/2, bodyHeight);
		}
		body.setModelBound(new BoundingBox());
		body.setLocalTranslation(0, legLength + ytrans + bodyHeight/2, 0);
		body.updateModelBound();
	
		String colours = "";
		switch (stripeColours) {
		case YELLOWBLUE:
			colours = "yellowblue";
			break;
		case BLACKWHITE:
			colours = "blackwhite";
			break;
		case MAGENTACYAN:
			colours = "magentacyan";
			break;
		default:
			colours = "yellowblue";
		}
			
		//build the path to the texture 		
		String stripetexture = "alienrabble/data/manystripes/" + colours ;
		stripetexture += "_stripe_" + Long.toString(stripeFreq); 
		stripetexture += "_angle_" + Long.toString(stripeAngle) + ".png";
				
        TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        ts.setEnabled(true);
        Texture texture = TextureManager.loadTexture(TestDynamic.class
                .getClassLoader().getResource(stripetexture),
                Texture.MM_LINEAR, Texture.FM_LINEAR);
        ts.setTexture(texture);
        ts.getTexture().setWrap(Texture.WM_WRAP_S_WRAP_T);
        body.setRenderState(ts);
        this.attachChild(body);
        
        MaterialState material = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
        material.setShininess( 128 );
        material.setDiffuse( eyeColour );
        material.setAmbient( eyeColour.mult( new ColorRGBA( 0.1f, 0.7f, 0.1f, 1 ) ) );
            
        String legmodel = "";
        ztrans = 0;
        ytrans = 0;
        
        switch (legType){
        case 1:
        	legmodel = "alienrabble/data/model/blobleg3.jbin";
        	break;
        case 2: 
        	legmodel  = "alienrabble/data/model/legcurved.jbin";
        	ytrans = 13;
        	break;
        default:
        	legmodel  = "alienrabble/data/model/blobleg3.jbin";
        }
        
        
        TriMesh legGeometry = null;
        try {
            URL bikeFile = AlienRabble.class.getClassLoader().getResource(legmodel);
            BinaryImporter importer = new BinaryImporter();
            legGeometry = (TriMesh)importer.load(bikeFile.openStream());
            legGeometry.setModelBound(new BoundingBox());
            //scale it to be MUCH smaller than it is originally
            legGeometry.setLocalScale(1f);
            legGeometry.setLocalTranslation(0f,0f,ztrans);
            legGeometry.updateModelBound();
            //this.attachChild(model);
        } catch (IOException e) {
        	//do something
        }
        
        // add the right number of legs.
        legs = new SharedMesh[legCount];
        for(int i=0; i<legCount;i++)
        {
        	legs[i] = new SharedMesh("leg"+i, legGeometry);
        
        	legs[i].setModelBound(new BoundingBox());
        	legs[i].updateModelBound();
        	float angle = 0;
        	if (legCount%2==0){
        		angle = i * 2 * FastMath.PI / legCount; 
        	}else{
        		angle = FastMath.PI / 4 + i * 2 * FastMath.PI / legCount; 
        	}
        		
	        Quaternion q = new Quaternion();
	        q.fromAngleAxis(angle, new Vector3f(0,1,0));
	        legs[i].setLocalRotation(q);        
	  	
	        this.attachChild(legs[i]);
        }
        
        TriMesh eyeMesh = new Sphere("eye",8,8,eyeSize);
        eyeMesh.setRenderState(material);
        
        // add the right number of eyes.
        eyes = new SharedMesh[eyeCount];
        for(int i=0; i<eyeCount;i++)
        {
        	eyes[i] = new SharedMesh("eye"+i, eyeMesh);
        
        	eyes[i].setModelBound(new BoundingBox());
        	eyes[i].updateModelBound();
        	float angle = 0;
        	if (eyeCount==2){
        		angle =-0.4f * FastMath.PI  - i * 0.2f *  FastMath.PI; 
        	}else{	
        		angle = -1*FastMath.PI / 4 - i * 2 * FastMath.PI / eyeCount; 
        	}
        		
	        Quaternion q = new Quaternion();
	        Vector3f loc = new Vector3f(bodyWidth/2,0,0);
	        q.fromAngleAxis(angle,new Vector3f(0,1,0) );
	        q.toRotationMatrix().mult(loc, loc);
	        loc = loc.add(0,legLength +  bodyHeight * 0.8f, 0 );
	        eyes[i].setLocalTranslation(loc);        
	        this.attachChild(eyes[i]);
        }

        String armmodel = "";
        ztrans = 0;
        ytrans = 0;
        
        switch (armType){
        case 1:
        	armmodel = "alienrabble/data/model/claw1.jbin";
        	ztrans = 1f;
        	ytrans = 4f;
        	break;
        case 2: 
        	armmodel  = "alienrabble/data/model/claw3.jbin";
        	ytrans = -4f;
        	break;
        default:
        	armmodel  = "alienrabble/data/model/claw3.jbin";
        }
        
        
        arms = null;
        try {
            URL bikeFile = AlienRabble.class.getClassLoader().getResource(armmodel);
            BinaryImporter importer = new BinaryImporter();
            arms = (TriMesh)importer.load(bikeFile.openStream());
            arms.setModelBound(new BoundingBox());
            arms.setLocalScale(new Vector3f(1f,1f,armSize));
            arms.setLocalTranslation(0,ytrans + legLength + bodyHeight/4,ztrans);
            arms.updateModelBound();
            //this.attachChild(model);
        } catch (IOException e) {
        	//do something
        }
        this.attachChild(arms);
	}	
	
	
	/**
	 * writes the xml for a single grab event 
	 * 
	 * @param doc - xml document we are working with
	 * @param ge - a single grab event
	 * @return the element to add to the document
	 */
	public Element writeExemplarDimensions(Document doc){
		
		Element playerloc = doc.createElement(TOPLEVEL_NODE);
		Element g1 = doc.createElement(NAME_NODE);
		g1.setTextContent(name);
		playerloc.appendChild(g1);
		Element g2 = doc.createElement(STRIPEINFO_NODE);
		g2.setAttribute(STRIPEANGLE_NODE,Long.toString(stripeAngle));
		g2.setAttribute(STRIPEFREQ_NODE,Float.toString(stripeFreq));
		g2.setAttribute(STRIPECOLOURS_NODE,Long.toString(stripeColours));
		playerloc.appendChild(g2);
		Element g3 = doc.createElement(BODYINFO_NODE);
		g3.setAttribute(BODYHEIGHT_NODE,Float.toString(bodyHeight));
		g3.setAttribute(BODYWIDTH_NODE,Float.toString(bodyWidth));
		playerloc.appendChild(g3);	
		Element g4 = doc.createElement(LEGINFO_NODE);
		g4.setAttribute(LEGCOUNT_NODE,Long.toString(legCount));
		g4.setAttribute(LEGTYPE_NODE,Float.toString(legType));
		playerloc.appendChild(g4);
		Element g5 = doc.createElement(EYEINFO_NODE);
		g5.setAttribute(EYECOUNT_NODE,Long.toString(eyeCount));
		g5.setAttribute(EYESIZE_NODE,Float.toString(eyeSize));
		Element eyeRGBA = doc.createElement(EYECOLOUR_NODE);
		eyeRGBA.setAttribute("R",Float.toString(eyeColour.r));
		eyeRGBA.setAttribute("G",Float.toString(eyeColour.g));
		eyeRGBA.setAttribute("B",Float.toString(eyeColour.b));
		eyeRGBA.setAttribute("A",Float.toString(eyeColour.a));
		g5.appendChild(eyeRGBA);
		playerloc.appendChild(g5);
		
		return playerloc;
	}

	
	
}
