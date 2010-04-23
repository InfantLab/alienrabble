/*
 * Copyright (c) 2003-2007 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package alienrabble.sort;

import alienrabble.logging.ARDataLoadandSave;
import alienrabble.logging.ARXMLSortData;
import alienrabble.logging.ARXMLSortData.MouseEvent;
import alienrabble.logging.ARXMLSortData.SortEvent;

import com.jme.input.MouseInput;
import com.jme.input.action.InputActionEvent;
import com.jme.input.action.MouseInputAction;
import com.jme.intersection.BoundingPickResults;
import com.jme.intersection.PickResults;
import com.jme.math.Ray;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.Text;
import com.jme.scene.shape.Quad;
import com.jme.system.DisplaySystem;
import com.jme.util.Timer;

/**
 * <code>MousePick</code>
 * @author Mark Powell
 * @version
 */
public class AlienPick extends MouseInputAction {

	private DisplaySystem display;
    private Node scene;
    private float shotTime = 0;
    private boolean performingAction = false;
    private int hits = 0;
    private int shots = 0;
    private Text text;
    private String hitItems;
    private Quad morebutton;
    private Quad lessbutton;
    private AlienSort selectedAlien;
    private PackingCases packingcases;
     
    private ARXMLSortData sortdata;
    private Timer timer;
    

    public AlienPick(DisplaySystem display, Node scene, Text text, Quad more, Quad less, PackingCases pc) {
        this.display = display;
        this.scene = scene;
        this.text = text;
        morebutton = more;
        lessbutton = less;
        packingcases = pc;
        selectedAlien = null;
        timer = Timer.getTimer();
        sortdata = ARDataLoadandSave.getInstance().getXmlSortData();
    }
    /* (non-Javadoc)
     * @see com.jme.input.action.MouseInputAction#performAction(float)
     */
    public void performAction(InputActionEvent evt) {
        shotTime += evt.getTime();
        if( MouseInput.get().isButtonDown(0) && shotTime > 0.3f && !performingAction) {
        	shotTime = 0;
        	performingAction = true;
            float x = MouseInput.get().getXAbsolute();
            float y = MouseInput.get().getYAbsolute(); 
            
  	      //find out if we have hit a more or less button
            if (getPickHits(morebutton, x, y) ) { // at button down
            	//we should add more packingcases
        		packingcases.addCase();
        		//log this event
        		MouseEvent me = sortdata.new MouseEvent();
	        	me.clockTicks = timer.getTime();
	        	me.timeInSecs =me.clockTicks * 1f / timer.getResolution();// * 1f to get result as float
	        	me.x_location = x;
	        	me.y_location = y;
	        	me.objectname = "moreBoxesButton";
	        	me.objectclicked = true;
	        	sortdata.addMouseEvent(me);
	        	performingAction = false;
	        	return;
            }
            else if (getPickHits(lessbutton, x, y) ) { // at button down
            	//we should remove a packingcase
        		packingcases.removeCase();
        		//log this event
        		MouseEvent me = sortdata.new MouseEvent();
	        	me.clockTicks = timer.getTime();
	        	me.timeInSecs =me.clockTicks * 1f / timer.getResolution();// * 1f to get result as float
	        	me.x_location = x;
	        	me.y_location = y;
	        	me.objectname = "lessBoxesButton";
	        	me.objectclicked = true;
	        	sortdata.addMouseEvent(me);
	        	performingAction = false;
	        	return;
            }
	        
			Vector2f screenPos = new Vector2f();
			// Get the position that the mouse is pointing to
			screenPos.set(x,y);
			// Get the world location of that X,Y value
			Vector3f worldCoords = display.getWorldCoordinates(screenPos, -1);
			Vector3f worldCoords2 = display.getWorldCoordinates(screenPos, 1);
          //  logger.info( worldCoords.toString() );
            // Create a ray starting from the camera, and going in the direction
			// of the mouse's location
			Ray ray = new Ray(worldCoords, worldCoords2
					.subtractLocal(worldCoords).normalizeLocal());
 
			PickResults results = new BoundingPickResults();
            results.setCheckDistance(false);
            scene.findPick(ray,results);
            
            hits += results.getNumber();
            hitItems = "";
            if(results.getNumber() > 0) {
                for(int i = 0; i < results.getNumber(); i++) {
                    hitItems += results.getPickData(i).getTargetMesh().getParent().getName() + " " + results.getPickData(i).getDistance();
                    if(i != results.getNumber() -1) {
                        hitItems += ", ";
                    }
                    //find out if we have hit an alien
                    Node element = results.getPickData(i).getTargetMesh().getParent();
                    while (!(element instanceof AlienSort) && !(element == null) ) { 
                      element = element.getParent();
                    }
        	        if ( element instanceof AlienSort ) {
        	        	//we should make this vanish and log 
        	        	AlienSort as = (AlienSort) element;
        	        	if (as.equals(selectedAlien))
        	        	{
               	        	as.Unselect();
               	        	selectedAlien = null;       
        	        	}else{
        	        		if (selectedAlien != null) selectedAlien.Unselect();
        	        		as.Select();
        	        		selectedAlien = as;
        	        	}	
        	        	MouseEvent me = sortdata.new MouseEvent();
        	        	me.clockTicks = timer.getTime();
        	        	me.timeInSecs =me.clockTicks * 1f / timer.getResolution();// * 1f to get result as float
        	        	me.x_location = worldCoords.x;
        	        	me.y_location = worldCoords.y;
        	        	me.objectname = as.getName();
        	        	me.objectid  = as.getID();
        	        	me.objectclicked = true;
        	        	sortdata.addMouseEvent(me);
        	        	results.clear();
        	        	break;
        			}
        	        //find out if we have it a case
        	        element = results.getPickData(i).getTargetMesh().getParent();
                    while (!(element == null) && !(element.getName().startsWith("packingcase") )  ) { 
                      element = element.getParent();
                    }
        	        if (!(element == null) && (element.getName().startsWith("packingcase") )) {
        	        	//we should make this vanish and log 
        	        	if (selectedAlien != null)
        	        	{
        	        		selectedAlien.removeFromParent();
        	        		element.attachChild((Node) selectedAlien);
        	        		selectedAlien.putInBox((Node) element);
        	        		//get an empty reference to PackingCases class to allow rearrange children
        	        		PackingCases pc = new PackingCases();
        	        		pc.rearrangeChildren(element);
            	        	MouseEvent me = sortdata.new MouseEvent();
            	        	me.clockTicks = timer.getTime();
            	        	me.timeInSecs =me.clockTicks * 1f / timer.getResolution();// * 1f to get result as float
            	        	me.x_location = worldCoords.x;
            	        	me.y_location = worldCoords.y;
            	        	me.objectname = element.getName();
            	        	me.objectclicked = true;
            	        	//me.type = ARXMLSortData.TYPE_SORTALIEN;
            	        	sortdata.addMouseEvent(me);
            	        	SortEvent se = sortdata.new SortEvent();
            	        	se.clockTicks = me.clockTicks;
            	        	se.timeInSecs = me.timeInSecs;
            	        	se.sortgroup = Integer.parseInt(element.getName().substring(11));
            	        	se.objectname = selectedAlien.getName();
            	        	se.objectid  = selectedAlien.getID();
            	        	se.type = ARXMLSortData.TYPE_SORTALIEN;
            	        	sortdata.addSortEvent(se);
        	        		results.clear();
            	        	break;
        	        	}
        			}
        	    }
            }
            shots++;
        	MouseEvent me = sortdata.new MouseEvent();
        	me.clockTicks = timer.getTime();
        	me.timeInSecs =me.clockTicks * 1f / timer.getResolution();// * 1f to get result as float
        	me.x_location = worldCoords.x;
        	me.y_location = worldCoords.y;
        	me.objectclicked = false;
        	//me.type = ARXMLSortData.TYPE_CLICKNOTHING;
        	sortdata.addMouseEvent(me);
            results.clear();
//            text.print("Hits: " + hits + " Shots: " + shots + " : " + hitItems);
            performingAction = false;
        }
    }
    public boolean getPickHits(Quad q, float x, float y) {
        int xFrom, xTo, yFrom, yTo;
        xFrom = (int)(q.getWorldTranslation().x - q.getWidth() / 2);
        xTo = (int)(q.getWorldTranslation().x + q.getWidth() / 2);
        yFrom = (int)(q.getWorldTranslation().y - q.getHeight() / 2);
        yTo = (int)(q.getWorldTranslation().y + q.getHeight() / 2);
        return isBetween(x, xFrom, xTo) && isBetween(y, yFrom, yTo);
      } // getPickHits

      private boolean isBetween(float toCheck, float lower, float upper) {
        return toCheck >= lower && toCheck <= upper;
      } // isBetween
}

