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

import java.util.logging.Logger;

import alienrabble.grab.AlienRabble;
import alienrabble.logging.ARDataLoadandSave;
import alienrabble.logging.ARXMLSortData;
import alienrabble.logging.ARXMLSortData.MouseEvent;
import alienrabble.model.ARXMLModelData;
import alienrabble.model.Model;
import alienrabble.util.RandomPermutation;

import com.jme.image.Texture;
import com.jme.input.AbsoluteMouse;
import com.jme.input.InputHandler;
import com.jme.light.DirectionalLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.pass.BasicPassManager;
import com.jme.renderer.pass.RenderPass;
import com.jme.renderer.pass.ShadowedRenderPass;
import com.jme.renderer.pass.ShadowedRenderPass.LightingMethod;
import com.jme.scene.Node;
import com.jme.scene.Skybox;
import com.jme.scene.Text;
import com.jme.scene.Spatial;
import com.jme.scene.Spatial.CullHint;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.system.GameSettings;
import com.jme.util.TextureManager;
import com.jmex.audio.AudioSystem;
import com.jmex.audio.AudioTrack;
import com.jmex.game.state.CameraGameState;


/**
 * <code>TestPick</code>
 * 
 * @author Mark Powell
 * @version $Id: TestPick.java,v 1.35 2007/08/17 22:04:20 nca Exp $
 */
public class AlienRabbleSort extends CameraGameState {
	private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(AlienRabbleSort.class
            .getName());

	/** Our display system. */
	private DisplaySystem display;
	 /** Game display properties. */
    protected GameSettings settings;
    /** Input handler. */
    protected InputHandler input;
    
    private static ShadowedRenderPass shadowPass = new ShadowedRenderPass();
    private BasicPassManager passManager;
    
    // display attributes for the window. We will keep these values
    // to allow the user to change them
    private int width, height, depth, freq;
    private boolean fullscreen;
    
    private Node scene;
	private AlienSort[] allAlienSort;
	private int numAliens;
    private AbsoluteMouse mouse;
    private PackingCases packingcases;
    private Skybox skybox;
    
    //the data logger
    private ARXMLSortData sortdata;
    
    private ARXMLModelData modeldata;
    
    //sound effects
    private AudioTrack laserSound;
    
    
    public AlienRabbleSort(String name, GameSettings settings){
    	super(name);
    	this.settings = settings;
		initGame();
    }
	

	/**
	 * builds the trimesh.
	 * 
	 * @see com.jme.app.SimpleGame#initGame()
	 */
	protected void initGame() {

		//get the logging object
		sortdata = ARDataLoadandSave.getInstance().getXmlSortData();

		
        // store the settings information
        width = settings.getWidth();
        height = settings.getHeight();
        depth = settings.getDepth();
        freq = settings.getFrequency();
        fullscreen = settings.getBoolean("Fullscreen", false);
        
        display = DisplaySystem.getDisplaySystem(settings.getRenderer());
        // set the background to black
        display.getRenderer().setBackgroundColor(ColorRGBA.black.clone());

        display.setTitle("Alien Rabble - Sort");
		cam.setLocation(new Vector3f(0f, 0f, 100f));
		cam.setDirection(Vector3f.UNIT_Z.mult(-1));
		cam.update();
		
		scene = rootNode;
		
        /** Create a ZBuffer to display pixels closest to the camera above farther ones.  */
        ZBufferState buf = display.getRenderer().createZBufferState();
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        scene.setRenderState(buf);
        
        //Time for a little optimization. We don't need to render back face triangles, so lets
        //not. This will give us a performance boost for very little effort.
        CullState cs = display.getRenderer().createCullState();
        cs.setCullFace(CullState.Face.Back);
        scene.setRenderState(cs);
        
        buildLighting();
//		buildSkyBox();
//		scene.attachChild(skybox);
		
		//set up passes
        buildPassManager();
		
        setupSounds();
        
        if (input != null) input.removeAllFromAttachedHandlers();
        
        Text text = Text.createDefaultTextLabel("Test Label", "Hits: 0 Shots: 0");
        text.setCullHint(CullHint.Never);
        text.setTextureCombineMode(Spatial.TextureCombineMode.Replace);
        text.setLocalTranslation(new Vector3f(1, 60, 0));
       
        //in production do so show this text information
        //scene.attachChild(text);    	
	

		modeldata = ARDataLoadandSave.getInstance().getXmlModelData_Sort();
		
		numAliens = modeldata.getNumModels();
		allAlienSort = new AlienSort[numAliens];
		
		RandomPermutation randperm = new RandomPermutation();
		randperm.setSize(numAliens);
		int[] perm;
		if (randperm.next()){
			perm = randperm.getPermutation();
		}else{
			//permutation failed, just do them in order
			//there must be a cleverer way but i'm too stupid
			perm = new int[numAliens];
			for(int p = 0;p<numAliens;p++){
				perm[p]=p;
			}
		}
		
		for(int i=0;i<numAliens;i++)
		{
			Model model = modeldata.getModel(perm[i]);

			allAlienSort[i] = new AlienSort(model);
			allAlienSort[i].setLocalTranslation(-5*numAliens + 10*i,30,0);
			scene.attachChild(allAlienSort[i]);				
			allAlienSort[i].setInitialValues();
			allAlienSort[i].addAllControllers();
			
			MouseEvent me = sortdata.new  MouseEvent();
			me.objectid = model.getID();
			me.objectname = model.getName();
			me.x_location = allAlienSort[i].getLocalTranslation().x;
			me.y_location = allAlienSort[i].getLocalTranslation().y;
			sortdata.addStartingPosition(me); 
		}
		
		packingcases = new PackingCases(scene, 3);
		
		scene.attachChild(packingcases);
		
		packingcases.setLightCombineMode(Spatial.LightCombineMode.Replace);

        MaterialState ms = display.getRenderer().createMaterialState();
        ms.setEnabled(true);
        ms.setColorMaterial(MaterialState.ColorMaterial.AmbientAndDiffuse);
		ms.setShininess(64);
        packingcases.setRenderState(ms);
        
        BlendState as = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
        as.setEnabled(true);
        as.setBlendEnabled(true);
        as.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        as.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        packingcases.setRenderState(as);


		input = new AlienRabbleSortHandler(packingcases, settings.getRenderer());
		
        mouse = new AbsoluteMouse( "Mouse Cursor", display.getWidth(), display.getHeight() );
        TextureState cursorTextureState = display.getRenderer().createTextureState();
        cursorTextureState.setTexture(
                TextureManager.loadTexture(
                        AlienRabbleSort.class.getClassLoader().getResource( "alienrabble/data/cursor/cursor1.png" ),
                        Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear )
        );
        mouse.setRenderState( cursorTextureState );
        mouse.registerWithInputHandler( input );
    
        scene.attachChild(mouse);
        BlendState as1 = display.getRenderer().createBlendState();
        as1.setBlendEnabled(true);
        as1.setSourceFunction(BlendState.SourceFunction.One);
        as1.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceColor);
        as1.setTestEnabled(true);
        as1.setTestFunction(BlendState.TestFunction.GreaterThan);
        mouse.setRenderState(as1);

        AlienPick pick = new AlienPick(display, scene, text);
		input.addAction(pick);	
	}
	
	private void setupSounds() {
        /** Set the 'ears' for the sound API */
        AudioSystem audio = AudioSystem.getSystem();
        audio.getEar().trackOrientation(cam);
        audio.getEar().trackPosition(cam);
		
		/** Create program sound */
//		targetSound = audio.createAudioTrack( AlienRabble.class.getClassLoader().getResource( "alienrabble/data/sound/explosion.ogg" ), false);
//        targetSound.setMaxAudibleDistance(1000);
//        targetSound.setVolume(1.0f);
		laserSound = audio.createAudioTrack( AlienRabble.class.getClassLoader().getResource( "alienrabble/data/sound/whizzoop.ogg" ), false);
        laserSound.setMaxAudibleDistance(1000);
        laserSound.setVolume(1.0f);
	}
	
    /**
     * creates a light for the terrain.
     */
    private void buildLighting() {
        /** Set up a basic, default light. */
        DirectionalLight light = new DirectionalLight();
        light.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, .5f));
        light.setDirection(new Vector3f(1,-1,-.5f));
        light.setShadowCaster(true);
        light.setEnabled(true);
        
        /** Set up a basic, default light. */
        DirectionalLight light2 = new DirectionalLight();
        light2.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        light2.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, .5f));
        light2.setDirection(new Vector3f(-1,-1,-.5f));
        light2.setShadowCaster(true);
        light2.setEnabled(true);
        
          /** Attach the light to a lightState and the lightState to rootNode. */
        LightState lightState = display.getRenderer().createLightState();
        lightState.setEnabled(true);
        lightState.setGlobalAmbient(new ColorRGBA(.6f, .6f, .6f, 1f));
        lightState.attach(light);
        lightState.attach(light2);
         scene.setRenderState(lightState);
    }
	
	/**
	 * Gets called every time the game state manager switches to this game state.
	 * Sets the window title.
	 */
	public void onActivate() {
		DisplaySystem.getDisplaySystem().
			setTitle("Alien Rabble - Grab Stage");
		super.onActivate();
	}
    
    /**
     * draws the scene graph
     * 
     * @see com.jme.app.BaseGame#render(float)
     */
    protected void stateRender(float interpolation) {
        // Clear the screen
        display.getRenderer().clearBuffers();
        display.getRenderer().draw(scene);
        /** Have the PassManager render. */
        passManager.renderPasses(display.getRenderer());
    }
	
    public void stateUpdate(float interpolation) {
    	super.stateUpdate(interpolation);
        //update the keyboard input 
        input.update(interpolation);
        
    	for(int i=0;i<allAlienSort.length;i++){
    		allAlienSort[i].update(interpolation);
    	}
    	scene.updateRenderState();
    }
	
	
    private void buildPassManager() {
        passManager = new BasicPassManager();

        // Add skybox first to make sure it is in the background
        RenderPass rPass = new RenderPass();
        rPass.add(skybox);
        passManager.add(rPass);

        shadowPass.add(scene);
        shadowPass.setRenderShadows(true);
        shadowPass.setLightingMethod(LightingMethod.Modulative);
        passManager.add(shadowPass);
    }
    
    /**
     * buildSkyBox creates a new skybox object with all the proper textures. The
     * textures used are the standard skybox textures from all the tests.
     *
     */
    private void buildSkyBox() {
        skybox = new Skybox("skybox", 200, 200, 200);

        Texture north = TextureManager.loadTexture(
        		AlienRabbleSort.class.getClassLoader().getResource(
            "alienrabble/data/skybox/north.jpg"),
            Texture.MinificationFilter.BilinearNearestMipMap,
            Texture.MagnificationFilter.Bilinear);
        Texture south = TextureManager.loadTexture(
        		AlienRabbleSort.class.getClassLoader().getResource(
            "alienrabble/data/skybox/south.jpg"),
            Texture.MinificationFilter.BilinearNearestMipMap,
            Texture.MagnificationFilter.Bilinear);
        Texture east = TextureManager.loadTexture(
        		AlienRabbleSort.class.getClassLoader().getResource(
            "alienrabble/data/skybox/east.jpg"),
            Texture.MinificationFilter.BilinearNearestMipMap,
            Texture.MagnificationFilter.Bilinear);
        Texture west = TextureManager.loadTexture(
        		AlienRabbleSort.class.getClassLoader().getResource(
            "alienrabble/data/skybox/west.jpg"),
            Texture.MinificationFilter.BilinearNearestMipMap,
            Texture.MagnificationFilter.Bilinear);
        Texture up = TextureManager.loadTexture(
        		AlienRabbleSort.class.getClassLoader().getResource(
            "alienrabble/data/skybox/top.jpg"),
            Texture.MinificationFilter.BilinearNearestMipMap,
            Texture.MagnificationFilter.Bilinear);
        Texture down = TextureManager.loadTexture(
        		AlienRabbleSort.class.getClassLoader().getResource(
            "alienrabble/data/skybox/bottom.jpg"),
            Texture.MinificationFilter.BilinearNearestMipMap,
            Texture.MagnificationFilter.Bilinear);

        skybox.setTexture(Skybox.Face.North, north);
        skybox.setTexture(Skybox.Face.West, west);
        skybox.setTexture(Skybox.Face.South, south);
        skybox.setTexture(Skybox.Face.East, east);
        skybox.setTexture(Skybox.Face.Up, up);
        skybox.setTexture(Skybox.Face.Down, down);
        skybox.preloadTextures();
        skybox.updateRenderState();
    }
	
    /**
     * will be called if the resolution changes
     * 
     * @see com.jme.app.BaseGame#reinit()
     */
    protected void reinit() {
        display.recreateWindow(width, height, depth, freq, fullscreen);
    }
}