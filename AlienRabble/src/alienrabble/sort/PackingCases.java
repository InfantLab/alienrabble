package alienrabble.sort;

import com.jme.bounding.BoundingBox;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Box;
import com.jme.scene.state.AlphaState;
import com.jme.system.DisplaySystem;

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
		
        AlphaState as = DisplaySystem.getDisplaySystem().getRenderer().createAlphaState();
        as.setEnabled(true);
        as.setBlendEnabled(true);
        as.setSrcFunction(AlphaState.SB_SRC_ALPHA);
        as.setDstFunction(AlphaState.DB_ONE_MINUS_SRC_ALPHA);
        this.setRenderState(as);
			
		
	}
	
	public void removeCase(){
		if (countCases > MINCASES){
			if (this.getChild(countCases).removeFromParent()) countCases--;
			rearrangeCases();
		}
	}
	
	public void addCase(){
		if (countCases < MAXCASES){
			Node container = new Node("packingcase"+countCases);
			Box box = new Box("box"+countCases,new Vector3f(0,0,0),1f,1f,1f);
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
		float casespacing = 100 / countCases;
		float casesize = 2f + 2f * FastMath.sqrt(countCases);
		
		for(int i=0;i<countCases;i++)
		{
			Spatial boxi = this.getChild(i);
			boxi.setLocalScale(casesize);
			boxi.setLocalTranslation(-50+(i+1)*casespacing, -30, 0);
		}
	}

}
