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

package alienrabble;


import com.jme.bounding.BoundingBox;
import com.jme.intersection.BoundingCollisionResults;
import com.jme.intersection.CollisionResults;
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

    //10 second life time
    private static final int LIFE_TIME = 999;
    //start off with a full life time
    float countdown = LIFE_TIME;
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
    
    private final Node rootNode;
    private Spatial model;
    private CollisionResults results;
    private Node player;
    
    
    /**
     * Constructor builds the flag, taking the terrain as the parameter. This
     * is just the reference to the game's terrain object so that we can 
     * randomly place this flag on the level.
     * @param tb the terrain used to place the flag.
     */
    public Alien(TerrainBlock tb, Node scene, String name, Spatial model) {
        super(name);
        this.tb = tb;
        this.rootNode = scene;
        BoundingBox box = new BoundingBox();
        this.setModelBound(box);
        results = new BoundingCollisionResults();
        
        if (model == null){
	        //Create the flag pole
	        Cylinder c = new Cylinder("alien1", 10, 10, 2, 25 );
	        this.attachChild(c);
	        Quaternion q = new Quaternion();
	        //rotate the cylinder to be vertical
	        q.fromAngleAxis(FastMath.PI/2, new Vector3f(1,0,0));
	        c.setLocalRotation(q);
	        c.setLocalTranslation(new Vector3f(-12.5f,-12.5f,0));
	        c.setDefaultColor(ColorRGBA.randomColor());
        }else{
        	setModel(model);
        }

        this.updateModelBound();

        this.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        this.setLocalScale(0.25f);
        
    }
    
    public void setPlayer(Node player){
    	this.player = player;
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
        this.attachChild(this.model);
        this.updateModelBound();
    }
    
    /**
     * During the update, we decrement the time. When it reaches zero, we will
     * reset the flag.
     * @param time the time between frame.
     */
    public void update(float time) {
    	super.updateRenderState();
        countdown -= time;
        
        if(countdown <= 0) {
            reset();
        }
        
//        results.clear();
//        player.calculateCollisions(this,  results);
//		for ( int i = results.getNumber() - 1; i >= 0; i-- ) {
//			Node element = results.getCollisionData(i).getSourceMesh().getParent();
//            while (!(element instanceof Vehicle) && !(element == null) ) { 
//              element = element.getParent();
//            }
//	        if ( element instanceof Vehicle ) {
//	        	//we should make this vanish and log 
//				removeFromParent();
//			}
//		}	
    }
    /**
     * reset sets the life time back to 10 seconds, and then randomly places the flag
     * on the terrain.
     *
     */
    public void reset() {
        countdown = LIFE_TIME;
        placeAlien();
    }
    
    /**
     * place flag picks a random point on the terrain and places the flag there. I
     * set the values to be between (45 and 175) which places it within the force field
     * level.
     *
     */
    public void placeAlien() {
        float x = 45 + FastMath.nextRandomFloat() * 130;
        float z = 45 + FastMath.nextRandomFloat() * 130;
        float y = tb.getHeight(x,z) + 0.2f;
        localTranslation.x = x;
        localTranslation.y = y;
        localTranslation.z = z;
    }
}
