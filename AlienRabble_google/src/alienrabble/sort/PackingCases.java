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
	private Node rootNode;
	
	public PackingCases(Node rootNode, int numCases) {
		super("PackingCases");
		this.rootNode = rootNode;
		if (numCases < MINCASES) numCases = MINCASES;
		if (numCases > MAXCASES) numCases = MAXCASES;
		
		for(int i=0;i<numCases;i++){
			addCase();
		}
	}
	
	public PackingCases() {
		}

	
	public void update(float fps){
		super.updateWorldData(fps);
	}
	
	public void removeCase(){
		if (countCases > MINCASES){
			Node caseToRemove = (Node) this.getChild(countCases-1);
			int numkids = caseToRemove.getChildren().size();
			//the box we are trying to remove may contain aliens
			//we ought to put them back 
			for (int kid=numkids-1; kid>=0; kid--){
				Spatial thiskid = caseToRemove.getChild(kid);
				if(thiskid instanceof AlienSort ){
					AlienSort thisalien = (AlienSort) thiskid;
					thisalien.removeFromParent();
					rootNode.attachChild(thisalien);
					thisalien.Unsort();
				}
			}
				
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
			Box box = new Box("box"+countCases,new Vector3f(0,0,0),1f,0.25f,0.42f);
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
		float casesize = 7f + 15f /countCases;
		
		for(int i=0;i<countCases;i++)
		{
			Node boxi = (Node) this.getChild(i);
			boxi.setLocalScale(casesize);
			boxi.setLocalTranslation(-50+i*casespacing, -30, 0);
			//now spread out the children
			rearrangeChildren(boxi);
		}
	}
	
	/***
	 * a routine that spreads the aliens evenly along length of the container
	 * @param box
	 */
	public void rearrangeChildren(Node box){
		
		int numkids = box.getChildren().size()-1; //one of the children is the box itself
		int numaliens = 0;
		float scalefactor;
		if (numkids < 2){
			scalefactor = 0.25f;
		}else{
			scalefactor = 1f/(2*numkids);
		}
		for(int kid = 0;kid<numkids+1;kid++){
			Spatial thiskid = box.getChild(kid);
			if (thiskid instanceof Box){}
			else{
				thiskid.setLocalScale( scalefactor);
				thiskid.setLocalTranslation(-1 + 2.0f/(2*numkids) + numaliens * 2.0f/numkids , 0.5f, 0);
				numaliens++;
			}
		}
	}
	

}
