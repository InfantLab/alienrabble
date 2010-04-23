/*
 * Copyright (c) 2003-2006 jMonkeyEngine
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

package alienrabble.grab;


import java.util.logging.Logger;


import alienrabble.model.Model;

import com.jme.animation.SpatialTransformer;
import com.jme.bounding.BoundingBox;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Cylinder;
import com.jmex.terrain.TerrainBlock;

/**
 * Alien maintains a single alien object. The participant 
 * aims to grab as many aliens as possible. The main job of 
 * the class is to build and manage the alien's movement 
 * @author Caspar Addyman
 *
 */
public class Alien extends Node{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(AlienRabbleGrab.class
            .getName());
	
	private static float SHRINKFACTOR = 0.3f;
	
	private SpatialTransformer grabanimation;

	//reference to the level terrain for placement
    TerrainBlock tb;
    /**
     * speed of this alien (distance it moves each frame).
     */
    private float speed;
    /**
     * current direction.
     */
    private Vector3f orientation;
    
    private Model model;
    
    private String ID; //an ID for this alien.. simpler than the name probably just a number
    
    
    /**
     * Constructor builds the alien, taking the terrain as the parameter. This
     * is just the reference to the game's terrain object so that we can 
     * randomly place this alien on the level.
     * @param tb the terrain used to place the alien.
     * @param scene the scene the alien will be added to
     * @param name the name for this node/alien
     * @param model the 3D model for this alien
     */
    public Alien(TerrainBlock tb, Node scene, String name, Model model) {
        super(name);
        this.tb = tb;
        BoundingBox box = new BoundingBox();
        this.setModelBound(box);
        
        if (model == null){
        	//For the lack of anything better to do (and for debug purposes)..
	        //Create a cylinder
	        Cylinder c = new Cylinder(name, 10, 10, 2, 25 );
	        this.attachChild(c);
	        Quaternion q = new Quaternion();
	        //rotate the cylinder to be vertical
	        q.fromAngleAxis(FastMath.PI/2, new Vector3f(-1,0,0));
	        c.setLocalRotation(q);
	        c.setLocalTranslation(new Vector3f(-12.5f,-12.5f,0));
	        c.setDefaultColor(ColorRGBA.randomColor());
        }else{
        	setModel(model);
        }
        
        this.updateModelBound();

        this.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
    }
    
    /**
     * retrieves the model Spatial of this alien.
     * @return the model Spatial of this alien.
     */
    public Model getModel() {
        return model;
    }
    
    /**
     * sets the model spatial of this vehicle. It first
     * detaches any previously attached models.
     * @param model the model to attach to this vehicle.
     */
    public void setModel(final Model model) {
        this.detachChild(this.model);
        this.model = model;
        BoundingBox b = new BoundingBox();
        model.setModelBound(b);
        model.updateModelBound();
        this.attachChild(this.model);
    }
    
    public void update(float time){
    	super.updateRenderState();
    	if (grabanimation != null){
 			if (grabanimation.getCurTime() >= grabanimation.getMaxTime()){
				this.removeFromParent();
			}			    			
		}
	}
    
	public int Category(){
		if (this.model == null){
			return 0;
		}else{
			return this.model.Category();
		}
	}
    /**
     * reset sets the life time back to 10 seconds, and then randomly places the flag
     * on the terrain.
     *
     */
    public void reset() {
//        countdown = LIFE_TIME;
        placeAlien();
    }
    
    /**
     * place flag picks a random point on the terrain and places the flag there. I
     * set the values to be between (45 and 175) which places it within the force field
     * level.
     *
     */
    public void placeAlien() {
    	//random location
    	float x = 45 + FastMath.nextRandomFloat() * 130;
        float z = 45 + FastMath.nextRandomFloat() * 130;
        float y = tb.getHeight(x,z) + 3f;
        localTranslation.x = x;
        localTranslation.y = y;
        localTranslation.z = z;
        //facing random direction
        Quaternion q = new Quaternion();
		q.fromAngleAxis(2 * FastMath.nextRandomFloat() * FastMath.PI, new Vector3f(0,1, 0));
		this.setLocalRotation(q);
		//traveling at random speed (not implemented yet)
		speed = FastMath.nextRandomFloat();    
    }
    
    public void grabAlien(Vehicle player){
    	if (grabanimation != null) return;
    	grabanimation = AlienGrabbedAnimation(player);
    	this.addController(grabanimation);
    	grabanimation.setActive(true);
    	this.setIsCollidable(false);
    }
    
    
    /***
     * a routine that animates the alien when we capture it,
     * spinning it round and shrinking it. 
     * @return
     */
	private SpatialTransformer AlienGrabbedAnimation(Vehicle player){
		SpatialTransformer st = new SpatialTransformer(1);
		Quaternion q0 = new Quaternion();
		q0.set(localRotation);
		Quaternion q = new Quaternion();
		q.fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
		st.setObject(this, 0, -1);
		st.setRepeatType(SpatialTransformer.RT_CLAMP);
		st.setScale(0,0,localScale);
		st.setScale(0,3,localScale.mult(SHRINKFACTOR) );
		st.setPosition(0,0,localTranslation);
	
		Vector3f playerdirection = player.getLocalRotation().getRotationColumn(2);
		playerdirection.y =+ 0.5f;
		st.setPosition(0,3, localTranslation.addLocal(playerdirection));
		st.setRotation(0,0, q0);
		st.setRotation(0,1.5f, q0.mult(q));
		st.setRotation(0,3, q0.mult(q).mult(q));
		st.interpolateMissing();
		st.setActive(false);
		return st;
	}
    
    /**
     * <code>removeFromParent</code> removes this Spatial from it's parent.
     *
     * @return true if it has a parent and performed the remove.
     */
    public boolean removeFromParent() {
    	logger.info(this.getName() + " removed @ " );
		grabanimation.setActive(false);
		this.detachAllChildren();
		this.updateRenderState();
		return super.removeFromParent();
    }
    
    public String getID(){
    	return ID;
    }
    public void setID(String id){
    	ID = id;
    }
}
