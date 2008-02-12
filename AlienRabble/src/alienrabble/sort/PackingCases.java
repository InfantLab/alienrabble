package alienrabble.sort;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Box;

public class PackingCases extends Node {

	private static final long serialVersionUID = 1L;
	public static final int MINCASES = 2;
	public static final int MAXCASES = 5;
	private int countCases = 0;
	
	public PackingCases(int numCases) {
		super("PackingCases");
		if (numCases < MINCASES) numCases = MINCASES;
		if (numCases > MAXCASES) numCases = MAXCASES;
		
		for(int i=0;i<numCases;i++){
			addCase();
		}
		
		
	}
	
	public void update(float fps){
		super.updateWorldData(fps);
	}
	
	public void removeCase(){
		if (countCases > MINCASES){
			if (this.getChild(countCases-1).removeFromParent()){ 
				countCases--;
				rearrangeCases();
			}
		}
	}
	
	public void addCase(){
		if (countCases < MAXCASES){
			Node container = new Node("packingcase"+countCases);
			//actually more like a tray than a box.
			Box box = new Box("box"+countCases,new Vector3f(0,0,0),1f,0.2f,1f);
			box.setRandomColors();
			BoundingBox bbox = new BoundingBox();
			box.setModelBound(bbox);
			box.updateModelBound();
			container.attachChild(box);
		    
			this.attachChild(container);
			countCases++;
			rearrangeCases();
		}
	}
	
	public void rearrangeCases(){
		
		if (countCases < 2) return;
		
		float casespacing = 100 / (countCases- 1);
		float casesize = 3f + 15f /countCases;
		
		for(int i=0;i<countCases;i++)
		{
			Spatial boxi = this.getChild(i);
			boxi.setLocalScale(casesize);
			boxi.setLocalTranslation(-50+i*casespacing, -30, 0);
		}
	}

}
