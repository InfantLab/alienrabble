package alienrabble;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import jmetest.terrain.TestTerrain;

import alienrabble.FlagRushHandler;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.image.Texture;
import com.jme.input.ChaseCamera;
import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.thirdperson.ThirdPersonMouseLook;
import com.jme.light.DirectionalLight;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.renderer.pass.BasicPassManager;
import com.jme.renderer.pass.RenderPass;
import com.jme.renderer.pass.ShadowedRenderPass;
import com.jme.scene.Node;
import com.jme.scene.Skybox;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Box;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.system.JmeException;
import com.jme.util.TextureManager;
import com.jme.util.Timer;
import com.jme.util.export.binary.BinaryImporter;
import com.jmex.game.state.CameraGameState;
import com.jmex.game.state.DebugGameState;
import com.jmex.game.state.GameStateManager;
import com.jmex.terrain.TerrainBlock;
import com.jmex.terrain.util.MidPointHeightMap;
import com.jmex.terrain.util.ProceduralTextureGenerator;

/**
 * Category learning search and collection phase
 * Adapted from FlagRush Tutorial Lesson 9 by Mark Powell
 * @author Caspar Addyman
 * @version 0.001
 */
public class Grab extends DebugGameState {
    private static final Logger logger = Logger.getLogger(Grab.class
            .getName());
    
    private DisplaySystem display; //not sure i should be using this var

    // the terrain we will drive over.
    private TerrainBlock tb;
    // fence that will keep us in.
    private ForceFieldFence fence;
    //Sky box (we update it each frame)
    private Skybox skybox;
    //the new player object
    private Vehicle player;
    //the flag to grab
    private Flag flag;
    //private ChaseCamera chaser;
    protected InputHandler input;
    //the timer
    protected Timer timer;
    // Our camera object for viewing the scene
    private Camera cam;
    //The chase camera, this will follow our player as he zooms around the level
    private ChaseCamera chaser;
    // the root node of the scene graph
    private Node scene;

    // display attributes for the window. We will keep these values
    // to allow the user to change them
    private int width, height, depth, freq;
    private boolean fullscreen;
    
    //store the normal of the terrain
    private Vector3f normal = new Vector3f();
    
    //height above ground level
    private float agl;
    
    private static ShadowedRenderPass shadowPass = new ShadowedRenderPass();
    private BasicPassManager passManager;
    private String clsname;
    
    /**
     * Main init point of the application
     */
    public Grab(String name) {
    	this(name, true);
    }
    
    public Grab(String name, boolean handleInput) {
    	clsname = name;
        init(handleInput);
    }
	
	protected final void init(boolean handleInput) {
			
		
			CameraGameState camstate = new CameraGameState("Follow Cam");
			// Add it to the manager
			GameStateManager.getInstance().attachChild(camstate);
		
	        /** Get a high resolution timer for FPS updates. */
	        timer = Timer.getTimer();

	        display.getRenderer().setCamera(camstate.getCamera());

	        KeyBindingManager.getKeyBindingManager().set("exit",
	                KeyInput.KEY_ESCAPE);
	        display.setTitle("Alien Rabble Grab");
	        
	        scene = new Node("Scene graph node");
	        /** Create a ZBuffer to display pixels closest to the camera above farther ones.  */
	        ZBufferState buf = display.getRenderer().createZBufferState();
	        buf.setEnabled(true);
	        buf.setFunction(ZBufferState.CF_LEQUAL);
	        scene.setRenderState(buf);
	        
	        //Time for a little optimization. We don't need to render back face triangles, so lets
	        //not. This will give us a performance boost for very little effort.
	        CullState cs = display.getRenderer().createCullState();
	        cs.setCullMode(CullState.CS_BACK);
	        scene.setRenderState(cs);
	        
	        //Add terrain to the scene
	        buildTerrain();
	        //Add a flag randomly to the terrain
	        buildFlag();
	        //Light the world
	        buildLighting();
	        //add the force field fence
	        buildEnvironment();
	        //Add the skybox
	        buildSkyBox();
	        //Build the player
	        buildPlayer();
	        //build the chase cam
	        buildChaseCamera();
	        //build the player input
	        buildInput();
	        
	        //set up passes
	        buildPassManager();
	        
	        // update the scene graph for rendering
	        scene.updateGeometricState(0.0f, true);
	        scene.updateRenderState();

	 }
	 
	    /**
	     * The update phase for the 
	     * 
	     * @see com.jme.app.BaseGame#update(float)
	     */
	 public void update(float interpolation) {
		 super.update(interpolation);
	        // update the time to get the framerate
	        timer.update();
	        interpolation = timer.getTimePerFrame();
	        //update the keyboard input (move the player around)
	        input.update(interpolation);
	        //update the chase camera to handle the player moving around.
	        chaser.update(interpolation);
	        //update the fence to animate the force field texture
	        fence.update(interpolation);
	        //update the flag to make it flap in the wind
	        flag.update(interpolation);
	        
	        //we want to keep the skybox around our eyes, so move it with
	        //the camera
	        skybox.setLocalTranslation(cam.getLocation());
	        skybox.updateGeometricState(0, true);
	        
	        // if escape was pressed, we exit
	        if (KeyBindingManager.getKeyBindingManager().isValidCommand("exit")) {
	            //finished = true;
	        	// not sure how to end this fing
	        }
	        
	        //We don't want the chase camera to go below the world, so always keep 
	        //it 2 units above the level.
	        if(cam.getLocation().y < (tb.getHeight(cam.getLocation())+2)) {
	            cam.getLocation().y = tb.getHeight(cam.getLocation()) + 2;
	            cam.update();
	        }
	        
	        //make sure that if the player left the level we don't crash. When we add collisions,
	        //the fence will do its job and keep the player inside.
	        float characterMinHeight = tb.getHeight(player
	                .getLocalTranslation())+agl;
	        if (!Float.isInfinite(characterMinHeight) && !Float.isNaN(characterMinHeight)) {
	            player.getLocalTranslation().y = characterMinHeight;
	        }
	        
	        //get the normal of the terrain at our current location. We then apply it to the up vector
	        //of the player.
	        tb.getSurfaceNormal(player.getLocalTranslation(), normal);
	        if(normal != null) {
	            player.rotateUpTo(normal);
	        }
	        
	        //Because we are changing the scene (moving the skybox and player) we need to update
	        //the graph.
	        scene.updateGeometricState(interpolation, true);

	 }
	 
    /**
     * draws the scene graph
     * 
     * @see com.jme.app.BaseGame#render(float)
     */
	 public void render(float tpf) {
		    super.render(tpf);
	        // Clear the screen
	        display.getRenderer().clearBuffers();
	        //display.getRenderer().draw(scene);
	        /** Have the PassManager render. */
	        passManager.renderPasses(display.getRenderer());
	 }
	 
	    private void buildPassManager() {
	        passManager = new BasicPassManager();

	        // Add skybox first to make sure it is in the background
	        RenderPass rPass = new RenderPass();
	        rPass.add(skybox);
	        passManager.add(rPass);

	        shadowPass.add(scene);
	        shadowPass.addOccluder(player);
//	        shadowPass.addOccluder(flag);
	        shadowPass.setRenderShadows(true);
	        shadowPass.setLightingMethod(ShadowedRenderPass.MODULATIVE);
	        passManager.add(shadowPass);
	    }

	    /**
	     * we created a new Flag class, so we'll use it to add the flag to the world.
	     * This is the flag that we desire, the one to get.
	     *
	     */
	    private void buildFlag() {
	        //create the flag and place it
	        flag = new Flag(tb);
	        scene.attachChild(flag);
	        flag.placeFlag();
	    }
	    
	    /**
	     * we are going to build the player object here. Now, we will load a .3ds model and convert it
	     * to .jme in realtime. The next lesson will show how to store as .jme so this conversion doesn't
	     * have to take place every time. 
	     * 
	     * We now have a Vehicle object that represents our player. The vehicle object will allow
	     * us to have multiple vehicle types with different capabilities.
	     *
	     */
	    private void buildPlayer() {
	        Spatial model = null;
	        try {
	            URL bikeFile = Grab.class.getClassLoader().getResource("jmetest/data/model/bike.jme");
	            BinaryImporter importer = new BinaryImporter();
	            model = (Spatial)importer.load(bikeFile.openStream());
	            model.setModelBound(new BoundingBox());
	            model.updateModelBound();
	            //scale it to be MUCH smaller than it is originally
	            model.setLocalScale(.0025f);
	        } catch (IOException e) {
	            logger
	                    .throwing(this.getClass().toString(), "buildPlayer()",
	                            e);
	        }
	        
	        //set the vehicles attributes (these numbers can be thought
	        //of as Unit/Second).
	        player = new Vehicle("Player Node", model);
	        player.setAcceleration(15);
	        player.setBraking(15);
	        player.setTurnSpeed(2.5f);
	        player.setWeight(25);
	        player.setMaxSpeed(25);
	        player.setMinSpeed(15);
	        
	        player.setLocalTranslation(new Vector3f(100,0, 100));
	        scene.attachChild(player);
	        scene.updateGeometricState(0, true);
	        //we now store this initial value, because we are rotating the wheels the bounding box will
	        //change each frame.
	        agl = ((BoundingBox)player.getWorldBound()).yExtent;
	        player.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
	    }
	    
	    /**
	     * buildEnvironment will create a fence. 
	     */
	    private void buildEnvironment() {
	        //This is the main node of our fence
	        fence = new ForceFieldFence("fence");
	        
	        //we will do a little 'tweaking' by hand to make it fit in the terrain a bit better.
	        //first we'll scale the entire "model" by a factor of 5
	        fence.setLocalScale(5);
	        //now let's move the fence to to the height of the terrain and in a little bit.
	        fence.setLocalTranslation(new Vector3f(25, tb.getHeight(25,25)+10, 25));
	        
	        scene.attachChild(fence);
	    }

	    /**
	     * creates a light for the terrain.
	     */
	    private void buildLighting() {
	        /** Set up a basic, default light. */
	        DirectionalLight light = new DirectionalLight();
	        light.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
	        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, .5f));
	        light.setDirection(new Vector3f(1,-1,0));
	        light.setShadowCaster(true);
	        light.setEnabled(true);

	          /** Attach the light to a lightState and the lightState to rootNode. */
	        LightState lightState = display.getRenderer().createLightState();
	        lightState.setEnabled(true);
	        lightState.setGlobalAmbient(new ColorRGBA(.2f, .2f, .2f, 1f));
	        lightState.attach(light);
	        scene.setRenderState(lightState);
	    }

	    /**
	     * build the height map and terrain block.
	     */
	    private void buildTerrain() {
	        
	        
	        MidPointHeightMap heightMap = new MidPointHeightMap(64, 1f);
	        // Scale the data
	        Vector3f terrainScale = new Vector3f(4, 0.0575f, 4);
	        // create a terrainblock
	         tb = new TerrainBlock("Terrain", heightMap.getSize(), terrainScale,
	                heightMap.getHeightMap(), new Vector3f(0, 0, 0), false);

	        tb.setModelBound(new BoundingBox());
	        tb.updateModelBound();

	        // generate a terrain texture with 2 textures
	        ProceduralTextureGenerator pt = new ProceduralTextureGenerator(
	                heightMap);
	        pt.addTexture(new ImageIcon(TestTerrain.class.getClassLoader()
	                .getResource("jmetest/data/texture/grassb.png")), -128, 0, 128);
	        pt.addTexture(new ImageIcon(TestTerrain.class.getClassLoader()
	                .getResource("jmetest/data/texture/dirt.jpg")), 0, 128, 255);
	        pt.addTexture(new ImageIcon(TestTerrain.class.getClassLoader()
	                .getResource("jmetest/data/texture/highest.jpg")), 128, 255,
	                384);
	        pt.createTexture(32);
	        
	        // assign the texture to the terrain
	        TextureState ts = display.getRenderer().createTextureState();
	        Texture t1 = TextureManager.loadTexture(pt.getImageIcon().getImage(),
	                Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR, true);
	        ts.setTexture(t1, 0);
	        
	        //load a detail texture and set the combine modes for the two terrain textures.
	        Texture t2 = TextureManager.loadTexture(
	                TestTerrain.class.getClassLoader().getResource(
	                "jmetest/data/texture/Detail.jpg"),
	                Texture.MM_LINEAR_LINEAR,
	                Texture.FM_LINEAR);

	        ts.setTexture(t2, 1);
	        t2.setWrap(Texture.WM_WRAP_S_WRAP_T);

	        t1.setApply(Texture.AM_COMBINE);
	        t1.setCombineFuncRGB(Texture.ACF_MODULATE);
	        t1.setCombineSrc0RGB(Texture.ACS_TEXTURE);
	        t1.setCombineOp0RGB(Texture.ACO_SRC_COLOR);
	        t1.setCombineSrc1RGB(Texture.ACS_PRIMARY_COLOR);
	        t1.setCombineOp1RGB(Texture.ACO_SRC_COLOR);
	        t1.setCombineScaleRGB(1.0f);

	        t2.setApply(Texture.AM_COMBINE);
	        t2.setCombineFuncRGB(Texture.ACF_ADD_SIGNED);
	        t2.setCombineSrc0RGB(Texture.ACS_TEXTURE);
	        t2.setCombineOp0RGB(Texture.ACO_SRC_COLOR);
	        t2.setCombineSrc1RGB(Texture.ACS_PREVIOUS);
	        t2.setCombineOp1RGB(Texture.ACO_SRC_COLOR);
	        t2.setCombineScaleRGB(1.0f);

	        tb.setRenderState(ts);
	        //set the detail parameters.
	        tb.setDetailTexture(1, 16);
	        tb.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
	        scene.attachChild(tb);
	        
	        
	    }
	    
	    /**
	     * buildSkyBox creates a new skybox object with all the proper textures. The
	     * textures used are the standard skybox textures from all the tests.
	     *
	     */
	    private void buildSkyBox() {
	        skybox = new Skybox("skybox", 10, 10, 10);

	        Texture north = TextureManager.loadTexture(
	            Grab.class.getClassLoader().getResource(
	            "jmetest/data/texture/north.jpg"),
	            Texture.MM_LINEAR,
	            Texture.FM_LINEAR);
	        Texture south = TextureManager.loadTexture(
	        		Grab.class.getClassLoader().getResource(
	            "jmetest/data/texture/south.jpg"),
	            Texture.MM_LINEAR,
	            Texture.FM_LINEAR);
	        Texture east = TextureManager.loadTexture(
	        		Grab.class.getClassLoader().getResource(
	            "jmetest/data/texture/east.jpg"),
	            Texture.MM_LINEAR,
	            Texture.FM_LINEAR);
	        Texture west = TextureManager.loadTexture(
	        		Grab.class.getClassLoader().getResource(
	            "jmetest/data/texture/west.jpg"),
	            Texture.MM_LINEAR,
	            Texture.FM_LINEAR);
	        Texture up = TextureManager.loadTexture(
	        		Grab.class.getClassLoader().getResource(
	            "jmetest/data/texture/top.jpg"),
	            Texture.MM_LINEAR,
	            Texture.FM_LINEAR);
	        Texture down = TextureManager.loadTexture(
	        		Grab.class.getClassLoader().getResource(
	            "jmetest/data/texture/bottom.jpg"),
	            Texture.MM_LINEAR,
	            Texture.FM_LINEAR);

	        skybox.setTexture(Skybox.NORTH, north);
	        skybox.setTexture(Skybox.WEST, west);
	        skybox.setTexture(Skybox.SOUTH, south);
	        skybox.setTexture(Skybox.EAST, east);
	        skybox.setTexture(Skybox.UP, up);
	        skybox.setTexture(Skybox.DOWN, down);
	        skybox.preloadTextures();
	        skybox.updateRenderState();
	       // scene.attachChild(skybox);
	    }
	    
	    /**
	     * set the basic parameters of the chase camera. This includes the offset. We want
	     * to be behind the vehicle and a little above it. So we will the offset as 0 for
	     * x and z, but be 1.5 times higher than the node.
	     * 
	     * We then set the roll out parameters (2 units is the closest the camera can get, and
	     * 5 is the furthest).
	     *
	     */
	    private void buildChaseCamera() {
	        HashMap<String, Object> props = new HashMap<String, Object>();
	        props.put(ThirdPersonMouseLook.PROP_MAXROLLOUT, "6");
	        props.put(ThirdPersonMouseLook.PROP_MINROLLOUT, "3");
	        props.put(ThirdPersonMouseLook.PROP_MAXASCENT, ""+45 * FastMath.DEG_TO_RAD);
	        props.put(ChaseCamera.PROP_INITIALSPHERECOORDS, new Vector3f(5, 0, 30 * FastMath.DEG_TO_RAD));
	        props.put(ChaseCamera.PROP_DAMPINGK, "4");
	        props.put(ChaseCamera.PROP_SPRINGK, "9");
	        chaser = new ChaseCamera(cam, player, props);
	        chaser.setMaxDistance(8);
	        chaser.setMinDistance(2);
	    }

	    /**
	     * create our custom input handler.
	     *
	     */
	    private void buildInput() {
	        input = new FlagRushHandler(player, DisplaySystem.getDisplaySystem().getRenderer().toString());
	    }
}
