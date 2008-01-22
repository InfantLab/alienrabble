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

package alienrabble.sort;


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

/**
 * Alien maintains a single alien object. The participant 
 * aims to grab as many aliens as possible. The main job of 
 * the class is to build and manage the alien's movement 
 * @author Caspar Addyman
 *
 */
public class AlienSort extends Node{

	private static final long serialVersionUID = 1L;
	
	public static final int STATUS_UNSORTED_UNSELECTED = 0;
	public static final int STATUS_SELECTED = 1;
	public static final int STATUS_SORTED = 2;
	
	private static final float GROWFACTOR = 3f; // how much bigger are selected aliens?
	
	private int currentStatus;
	
	Vector3f initialLocation = new Vector3f();
	Vector3f initialSize = new Vector3f();
	Quaternion initialRotation = new Quaternion();
	SpatialTransformer alienspinner;
	SpatialTransformer aliengrow;
	SpatialTransformer alienshrink;
	SpatialTransformer alienputinbox;
    Spatial model;
	
    /**
     * Constructor builds the flag, taking the terrain as the parameter. This
     * is just the reference to the game's terrain object so that we can 
     * randomly place this flag on the level.
     * @param tb the terrain used to place the flag.
     */
    public AlienSort(String name, Spatial model) {
    	super(name);

    	if (model == null){
	        //Create a cylinder
	        Cylinder c = new Cylinder("cylinder", 10, 10, 2, 25 );
	        BoundingBox box = new BoundingBox();
	        c.setModelBound(box);
	        this.attachChild(c);
	        Quaternion q = new Quaternion();
	        //rotate the cylinder to be vertical
	        q.fromAngleAxis(FastMath.PI/2, new Vector3f(1,0,0));
	        c.setLocalRotation(q);
	        c.setLocalTranslation(new Vector3f(-12.5f,-12.5f,0));
	        c.setDefaultColor(ColorRGBA.randomColor());
	        c.updateModelBound();
        }else{
        	setModel(model);
        }
        this.currentStatus = STATUS_UNSORTED_UNSELECTED;
        

        this.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        //setInitialValues();
    }
    
     /**
     * retrieves the model Spatial of this alien.
     * @return the model Spatial of this alien.
     */
    public Spatial getModel() {
        return model;
    }
    
    /**
     * sets the model spatial of this vehicle. It first
     * detaches any previously attached models.
     * @param model the model to attach to this vehicle.
     */
    public void setModel(final Spatial model) {
        this.detachChild(this.model);
        this.model = model;
        BoundingBox box = new BoundingBox();
        model.setModelBound(box);
        model.updateModelBound();
        this.attachChild(this.model);      
    }
    
    /**
     * During the update, we decrement the time. When it reaches zero, we will
     * reset the flag.
     * @param time the time between frame.
     */
    public void update(float time) {
    	super.updateRenderState();
    	super.updateWorldBound();
    	
    	if (this.currentStatus == STATUS_SELECTED) {
    		if (!alienspinner.isActive()){
    			if (aliengrow.getCurTime() >= aliengrow.getMaxTime()){
    				aliengrow.setActive(false);
    				alienspinner.setActive(true);
    			}			
    		}
    	}
        	
    }

    public void setInitialValues(){
    	initialLocation = this.localTranslation.clone();
    	initialSize = this.localScale.clone();
    	Quaternion q = new Quaternion(this.localRotation.x,this.localRotation.y,this.localRotation.z,this.localRotation.w);
    	initialRotation.set(q);
    }
    public void addAllControllers(){
    	alienspinner = AlienSpinner();
    	aliengrow = AlienGrow();
    	alienshrink = AlienShrink();
    	this.addController(alienspinner);
    	this.addController(aliengrow);
    	this.addController(alienshrink);
    }
    
    
	private SpatialTransformer AlienSpinner(){
		SpatialTransformer st = new SpatialTransformer(1);
		Quaternion q0 = new Quaternion();
		q0.set(localRotation);
		Quaternion q = new Quaternion();
		q.fromAngleAxis(FastMath.PI, Vector3f.UNIT_Z);
		st.setObject(this, 0, -1);
		st.setRepeatType(SpatialTransformer.RT_WRAP);
		st.setScale(0,0,initialSize.mult(GROWFACTOR));
		st.setPosition(0,0, new Vector3f(0,0,0));
		st.setRotation(0,0, q0);
		st.setRotation(0,3, q0.mult(q));
		st.setRotation(0,6, q0.mult(q).mult(q));
		st.interpolateMissing();
		st.setActive(false);
		return st;
	}
	private SpatialTransformer AlienGrow(){
		SpatialTransformer st = new SpatialTransformer(1);
		st.setRepeatType(SpatialTransformer.RT_CLAMP);
		st.setObject(this, 0, -1);
		st.setScale(0,0, initialSize);
		st.setScale(0,2, initialSize.mult(GROWFACTOR));
		st.setPosition(0, 0, initialLocation);
		st.setPosition(0,2, new Vector3f(0,0,0));
		st.interpolateMissing();
		st.setActive(false);
		return st;
	}
	private SpatialTransformer AlienShrink(){
		SpatialTransformer st = new SpatialTransformer(1);
		st.setRepeatType(SpatialTransformer.RT_CLAMP);
		st.setObject(this, 0, -1);
		st.setScale(0,0, initialSize.mult(GROWFACTOR));
		st.setScale(0,2, initialSize);
		st.setPosition(0,0, new Vector3f(0,0,0));
		st.setPosition(0,2, initialLocation);
		st.interpolateMissing();
		st.setActive(false);
		return st;
	}
	private SpatialTransformer AlienPutInBox(Node box){
		SpatialTransformer st = new SpatialTransformer(1);
		st.setRepeatType(SpatialTransformer.RT_CLAMP);
		st.setObject(this, 0, -1);
		st.setScale(0,0,initialSize.mult(GROWFACTOR));
		st.setScale(0,2, initialSize);
		st.setPosition(0, 0, new Vector3f(0,0,0));
		st.setPosition(0,2, box.getWorldTranslation());
		st.interpolateMissing();
		st.setActive(false);
		return st;
	}
	
	public void UpdateStatus(){
		if( (this.currentStatus&STATUS_SELECTED)==STATUS_SELECTED)
		{
			Unselect();
		}else
		{
			Select();
		}
	}
	
	public void Select(){
    	if (this.currentStatus == STATUS_UNSORTED_UNSELECTED ){
    		currentStatus = STATUS_SELECTED;
    		alienshrink.setActive(false);
    		alienspinner.setActive(false);
    		aliengrow.setActive(true);
        	if (aliengrow.getCurTime() > aliengrow.getMaxTime()){
    			aliengrow.setCurTime(0);
    		}	
    	}
	}
	public void Unselect(){
		if (this.currentStatus == STATUS_SELECTED){
    		this.currentStatus = STATUS_UNSORTED_UNSELECTED;
    		alienshrink.setActive(true);
    		aliengrow.setActive(false);
    		alienspinner.setActive(false);
    		if (alienshrink.getCurTime() > alienshrink.getMaxTime()){
    			alienshrink.setCurTime(0);
    		}
    	}
	}
	public void putInBox(Node box)
	{
		if (currentStatus == STATUS_SORTED) return;
		alienputinbox = AlienPutInBox(box);
		this.addController(alienputinbox);
		alienshrink.setActive(false);
		aliengrow.setActive(false);
		alienspinner.setActive(false);
		alienputinbox.setActive(true);
		currentStatus = STATUS_SORTED;
	}
}
