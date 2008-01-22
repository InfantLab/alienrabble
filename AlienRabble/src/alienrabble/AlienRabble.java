package alienrabble;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import jmetest.renderer.TestText;
import jmetest.terrain.TestTerrainTrees;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.image.Texture;
import com.jme.input.ChaseCamera;
import com.jme.input.InputHandler;
import com.jme.input.thirdperson.ThirdPersonMouseLook;
import com.jme.intersection.BoundingCollisionResults;
import com.jme.intersection.CollisionResults;
import com.jme.light.DirectionalLight;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.renderer.pass.BasicPassManager;
import com.jme.renderer.pass.RenderPass;
import com.jme.renderer.pass.ShadowedRenderPass;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.SharedMesh;
import com.jme.scene.Skybox;
import com.jme.scene.Spatial;
import com.jme.scene.Text;
import com.jme.scene.shape.Pyramid;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.system.PropertiesIO;
import com.jme.util.TextureManager;
import com.jme.util.Timer;
import com.jme.util.export.binary.BinaryImporter;
import com.jmex.game.state.CameraGameState;
import com.jmex.game.state.GameStateManager;
import com.jmex.terrain.TerrainBlock;
import com.jmex.terrain.util.MidPointHeightMap;
import com.jmex.terrain.util.ProceduralTextureGenerator;

public class AlienRabble extends CameraGameState{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AlienRabble.class
	            .getName());
	
	private static final float GRAB_RADIUS = 2.5f;
	    
	
    // the terrain we will drive over.
    private TerrainBlock tb;
    // fence that will keep us in.
    private ForceFieldFence fence;
    //Sky box (we update it each frame)
    private Skybox skybox;
    //the new player object
    private Vehicle player;
    private Node aliencontainer;
    private int numAliens;
    private Alien[] allAliens;
    private String[] strAliens;
    private Text text;
    //private CollisionTreeManager collisionTreeManager;
	private CollisionResults results;
		
	/** Our display system. */
	private DisplaySystem display;
	 /** Game display properties. */
    protected PropertiesIO properties;
		
    protected InputHandler input;
    //the timer
    protected Timer timer;
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

	public AlienRabble(String name, PropertiesIO properties) {
		super(name);
		this.properties = properties;
		initSystem();
		initGame();
	}
	
    /**
     * During an update we look for the escape button and update the timer
     * to get the framerate. Things are now starting to happen, so we will 
     * update 
     * 
     * @see com.jme.app.BaseGame#update(float)
     */
    public void stateUpdate(float interpolation) {
    	super.stateUpdate(interpolation);
        // update the time to get the framerate
        timer.update();
        interpolation = timer.getTimePerFrame();
        //update the keyboard input (move the player around)
        input.update(interpolation);
        //update the chase camera to handle the player moving around.
        chaser.update(interpolation);
        //update the fence to animate the force field texture
        fence.update(interpolation);
        
        //we want to keep the skybox around our eyes, so move it with
        //the camera
        skybox.setLocalTranslation(cam.getLocation());
        skybox.updateGeometricState(0, true);
        

        // if escape was pressed, we exit
        if (numAliens == 0) {
			// Here we switch to the menu state which is already loaded
			GameStateManager.getInstance().activateChildNamed("menusort");
			// And remove this state, because we don't want to keep it in memory.
			GameStateManager.getInstance().detachChild("ingrabgame");
		}
        
        //We don't want the chase camera to go below the world, so always keep 
        //it 2 units above the level.
        if(cam.getLocation().y < (tb.getHeight(cam.getLocation())+2)) {
            cam.getLocation().y = tb.getHeight(cam.getLocation())+2;
            cam.update();
        }
        
//        //make sure that if the player left the level we don't crash. When we add collisions,
//        //the fence will do its job and keep the player inside.
//        float characterMinHeight = tb.getHeight(player
//                .getLocalTranslation())+agl;
//        if (!Float.isInfinite(characterMinHeight) && !Float.isNaN(characterMinHeight)) {
//            player.getLocalTranslation().y = characterMinHeight;
//        }
        
        //get the normal of the terrain at our current location. We then apply it to the up vector
        //of the player.
        tb.getSurfaceNormal(player.getLocalTranslation(), normal);
        if(normal != null) {
            player.rotateUpTo(normal);
        }
        
        //update the player to check for collisions
        player.update(interpolation);
        
        //Because we are changing the scene (moving the skybox and player) we need to update
        //the graph.
        scene.updateGeometricState(interpolation, true);
        
        
        //update all our aliens
        aliencontainer.updateWorldData(interpolation);
        
   
        results.clear();
        player.findCollisions(fence, results);
        if (results.getNumber()>0){
			player.setVelocity(-0.7f * player.getVelocity());
		} 
        results.clear();
        Vector3f relativePosition = new Vector3f();
        player.findCollisions(aliencontainer,results);
        if (results.getNumber()>0){
            for(int i = 0;i<results.getNumber();i++)
            {
            	Geometry geom = results.getCollisionData(i).getTargetMesh();
	            Node element = geom.getParent();
	            while (!(element instanceof Alien) && !(element == null) ) { 
	              element = element.getParent();
	            }
		        if ( element instanceof Alien ) {

                    relativePosition.set( element.getWorldTranslation() );
                    relativePosition.subtractLocal( player.getWorldTranslation() );
                    final float distance = relativePosition.length();
                    if ( distance < GRAB_RADIUS ) {
                    	player.setVelocity(-0.1f * player.getVelocity());
    		        	//we should make this vanish and log 
    		        	Alien as = (Alien) element;
    		        	as.removeFromParent();
    		        	break;                    	
                    }
		        }
            }
        } 
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
    protected void renderState(float interpolation) {
        // Clear the screen
        display.getRenderer().clearBuffers();
        display.getRenderer().draw(scene);
        /** Have the PassManager render. */
        passManager.renderPasses(display.getRenderer());
    }

    /**
     * initializes the display and camera.
     * 
     * @see com.jme.app.BaseGame#initSystem()
     */
    protected void initSystem() {
    	
        // store the properties information
        width = properties.getWidth();
        height = properties.getHeight();
        depth = properties.getDepth();
        freq = properties.getFreq();
        fullscreen = properties.getFullscreen();
        
        
        display = DisplaySystem.getDisplaySystem(properties.getRenderer());
        // set the background to black
        display.getRenderer().setBackgroundColor(ColorRGBA.black.clone());

        // initialize the camera
        cam.setFrustumPerspective(45.0f, (float) width / (float) height, 1,
                2000);
        cam.setLocation(new Vector3f(200,400,200));
        
        /** Signal that we've changed our camera's location/frustum. */
        cam.update();

        /** Get a high resolution timer for FPS updates. */
        timer = Timer.getTimer();

        display.getRenderer().setCamera(cam);

    }

    /**
     * initializes the scene
     * 
     * @see com.jme.app.BaseGame#initGame()
     */
    protected void initGame() {
        display.setTitle("Alien Rabble");
        
        results = new BoundingCollisionResults(); 
        
        scene = rootNode;
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
        

        //collisionTreeManager = new CollisionTreeManager( scene, new float[]{0.2f, 1.2f} );
        
        //Add terrain to the scene
        buildTerrain();
        //Add a flag randomly to the terrain
        //buildFlag();
        //Light the world
        buildLighting();
        //add the force field fence
        buildEnvironment();
        //Add the skybox
        buildSkyBox();
        //Build the player
        buildPlayer();
        //build the chase camera
        buildChaseCamera();
        //build the player input
        buildInput();
        //Add an alien randomly to the terrain
        addAliens();
        
        //set up passes
        buildPassManager();
        
        //add a debug message
        text = new Text("Text Label", "Wall Collision: No");
        
        AlphaState as = display.getRenderer().createAlphaState();
        as.setBlendEnabled(true);
        as.setSrcFunction(AlphaState.SB_SRC_ALPHA);
        as.setDstFunction(AlphaState.DB_ONE);
        as.setTestEnabled(true);
        as.setTestFunction(AlphaState.TF_GREATER);
        as.setEnabled(true);

        TextureState ts = display.getRenderer().createTextureState();
        ts.setTexture(
            TextureManager.loadTexture(
                TestText.class.getClassLoader().getResource(Text.DEFAULT_FONT),
                Texture.MM_LINEAR,
                Texture.FM_LINEAR));
        ts.setEnabled(true);
        text.setRenderState(ts);
        text.setRenderState(as);
        
		text.setLocalTranslation(new Vector3f(1, 60, 0));
		text.setTextColor(ColorRGBA.white);
		text.setZOrder(0);
		scene.attachChild(text);
        
        // update the scene graph for rendering
        scene.updateGeometricState(0.0f, true);
        scene.updateRenderState();
    }
    
    private void buildPassManager() {
        passManager = new BasicPassManager();

        // Add skybox first to make sure it is in the background
        RenderPass rPass = new RenderPass();
        rPass.add(skybox);
        passManager.add(rPass);

        shadowPass.add(scene);
//        shadowPass.addOccluder(player);
//        shadowPass.addOccluder(flag);
        shadowPass.setRenderShadows(true);
        shadowPass.setLightingMethod(ShadowedRenderPass.MODULATIVE);
        passManager.add(shadowPass);
    }
    
    /**
     * Add full set of aliens across environment
     *
     */
    private void addAliens() {
    	
    	aliencontainer = new Node("aliencontainer");
    	scene.attachChild(aliencontainer);
    	
		Quaternion q = new Quaternion();
		q.fromAngleAxis(FastMath.PI/2, new Vector3f(-1,0, 0));
        
	 	
		strAliens = new String[12];
		strAliens[0] = "alienrabble/data/Greebles/Family1/f1_11.jbin";
		strAliens[1] = "alienrabble/data/Greebles/Family1/f1_12.jbin";
		strAliens[2] = "alienrabble/data/Greebles/Family1/f1_13.jbin";
		strAliens[3] = "alienrabble/data/Greebles/Family1/f1_14.jbin";
		strAliens[4] = "alienrabble/data/Greebles/Family1/f1_15.jbin";
		strAliens[5] = "alienrabble/data/Greebles/Family1/f1_16.jbin";
		strAliens[6] = "alienrabble/data/Greebles/Family1/m1_11.jbin";
		strAliens[7] = "alienrabble/data/Greebles/Family1/m1_12.jbin";
		strAliens[8] = "alienrabble/data/Greebles/Family1/m1_13.jbin";
		strAliens[9] = "alienrabble/data/Greebles/Family1/m1_14.jbin";
		strAliens[10] = "alienrabble/data/Greebles/Family1/m1_15.jbin";
		strAliens[11] = "alienrabble/data/Greebles/Family1/m1_16.jbin";
		
		numAliens = strAliens.length;
		allAliens = new Alien[numAliens];
	
		for(int i=0;i<strAliens.length;i++)
		{
			URL alienURL = AlienRabble.class.getClassLoader().getResource(strAliens[i]);
			BinaryImporter BI = new BinaryImporter();
			Spatial model;
			try {
				model = (Spatial)BI.load(alienURL.openStream());
				allAliens[i] = new Alien(tb, scene,"alien"+i,model);
				allAliens[i].setLocalRotation(q);
				aliencontainer.attachChild(allAliens[i]);
				allAliens[i].placeAlien();
				allAliens[i].setPlayer(player);
				} 
			catch (IOException e) {
					logger.info("darn exceptions:" + e.getMessage());
			}
		}			
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
            URL bikeFile = AlienRabble.class.getClassLoader().getResource("alienrabble/data/model/grabber2.jbin");
            BinaryImporter importer = new BinaryImporter();
            model = (Spatial)importer.load(bikeFile.openStream());
            model.setModelBound(new BoundingSphere());
            //scale it to be MUCH smaller than it is originally
            model.setLocalScale(.60f);
            model.updateModelBound();
        } catch (IOException e) {
            logger.throwing(this.getClass().toString(), "buildPlayer()", e);
        }
        
        Quaternion q = new Quaternion();
        Quaternion q0 = new Quaternion();
        q0.fromAngleAxis(FastMath.PI/4, new Vector3f(0,0, 1));
        q.fromAngleAxis(FastMath.PI/4, new Vector3f(0,1,0));
        q = q.add(q0);
        model.setLocalRotation(q);
        //set the vehicles attributes (these numbers can be thought
        //of as Unit/Second).
        player = new Vehicle("Player Node",scene, model);
        player.setAcceleration(8);
        player.setBraking(12);
        player.setTurnSpeed(2.2f);
        player.setWeight(25);
        player.setMaxSpeed(18);
        player.setMinSpeed(6);
        
        player.setLocalTranslation(new Vector3f(100,0, 100));
        scene.attachChild(player);
        scene.updateGeometricState(0, true);
        //we now store this initial value, because we are rotating the wheels the bounding box will
        //change each frame.
        agl = ((BoundingSphere)player.getWorldBound()).radius;
        player.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        //collisionTreeManager.add(player );
    }
    
    /**
     * buildEnvironment will create a fence. 
     */
    private void buildEnvironment() {
        //This is the main node of our fence
        fence = new ForceFieldFence("fence");
        
        //we will do a little 'tweaking' by hand to make it fit in the terrain a bit better.
        //first we'll scale the entire "model" by a factor of 5
        fence.setLocalScale(6);
        //now let's move the fence to to the height of the terrain and in a little bit.
        fence.setLocalTranslation(new Vector3f(25, tb.getHeight(25,25)+10, 25));
        
        scene.attachChild(fence);
       // collisionTreeManager.add(fence);
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
        Vector3f terrainScale = new Vector3f(4, 0.0275f, 4);
        // create a terrainblock
         tb = new TerrainBlock("Terrain", heightMap.getSize(), terrainScale,
                heightMap.getHeightMap(), new Vector3f(0, 0, 0), false);

        tb.setModelBound(new BoundingBox());
        tb.updateModelBound();

        // generate a terrain texture with 2 textures
        ProceduralTextureGenerator pt = new ProceduralTextureGenerator(
                heightMap);
        pt.addTexture(new ImageIcon(AlienRabble.class.getClassLoader()
                .getResource("jmetest/data/texture/grassb.png")), -128, 0, 128);
        pt.addTexture(new ImageIcon(AlienRabble.class.getClassLoader()
                .getResource("jmetest/data/texture/dirt.jpg")), 0, 128, 255);
        pt.addTexture(new ImageIcon(AlienRabble.class.getClassLoader()
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
        		AlienRabble.class.getClassLoader().getResource(
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
        
        
        TextureState treeTex = display.getRenderer().createTextureState();
        treeTex.setEnabled(true);
        Texture tr = TextureManager.loadTexture(
                TestTerrainTrees.class.getClassLoader().getResource(
                        "jmetest/data/texture/grass.jpg"), Texture.MM_LINEAR_LINEAR,
                Texture.FM_LINEAR);
        treeTex.setTexture(tr);
        
        Pyramid p = new Pyramid("Pyramid", 3, 6);
        p.setModelBound(new BoundingBox());
        p.updateModelBound();
        p.setRenderState(treeTex);
        p.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        p.setTextureCombineMode(TextureState.REPLACE);
        
        for (int i = 0; i < 50; i++) {
        	Spatial s1 = new SharedMesh("tree"+i, p);
            float x = 45 + FastMath.nextRandomFloat() * 130;
            float z = 45 + FastMath.nextRandomFloat() * 130;
            float y = tb.getHeight(x,z) + 6f; 
            s1.setLocalTranslation(new Vector3f(x, y, z));
            scene.attachChild(s1);
        }
    }
    
    /**
     * buildSkyBox creates a new skybox object with all the proper textures. The
     * textures used are the standard skybox textures from all the tests.
     *
     */
    private void buildSkyBox() {
        skybox = new Skybox("skybox", 10, 10, 10);

        Texture north = TextureManager.loadTexture(
        		AlienRabble.class.getClassLoader().getResource(
            "jmetest/data/texture/north.jpg"),
            Texture.MM_LINEAR,
            Texture.FM_LINEAR);
        Texture south = TextureManager.loadTexture(
        		AlienRabble.class.getClassLoader().getResource(
            "jmetest/data/texture/south.jpg"),
            Texture.MM_LINEAR,
            Texture.FM_LINEAR);
        Texture east = TextureManager.loadTexture(
        		AlienRabble.class.getClassLoader().getResource(
            "jmetest/data/texture/east.jpg"),
            Texture.MM_LINEAR,
            Texture.FM_LINEAR);
        Texture west = TextureManager.loadTexture(
        		AlienRabble.class.getClassLoader().getResource(
            "jmetest/data/texture/west.jpg"),
            Texture.MM_LINEAR,
            Texture.FM_LINEAR);
        Texture up = TextureManager.loadTexture(
        		AlienRabble.class.getClassLoader().getResource(
            "jmetest/data/texture/top.jpg"),
            Texture.MM_LINEAR,
            Texture.FM_LINEAR);
        Texture down = TextureManager.loadTexture(
        		AlienRabble.class.getClassLoader().getResource(
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
        scene.attachChild(skybox);
    }
    
    /**
     * set the basic parameters of the chase camera. This includes the offset. We want
     * to be behind the vehicle and a little above it. So we will the offset as 0 for
     * x and z, but be 1.5 times higher than the node.
     * 
     * We then set the roll out parameters (2 units is the closest the camera can get, and
     * 8 is the furthest).
     *
     */
    private void buildChaseCamera() {
        HashMap<String, Object> props = new HashMap<String, Object>();
        Vector3f targetOffset = new Vector3f();
        targetOffset.y = ((BoundingSphere) player.getWorldBound()).radius * 2.3f;
        props.put(ThirdPersonMouseLook.PROP_ENABLED, "false");
        props.put(ChaseCamera.PROP_TARGETOFFSET,targetOffset);
        props.put(ChaseCamera.PROP_INITIALSPHERECOORDS, new Vector3f(5.5f, 180 * FastMath.DEG_TO_RAD, 25 * FastMath.DEG_TO_RAD));
        props.put(ChaseCamera.PROP_DAMPINGK, "4");
        props.put(ChaseCamera.PROP_SPRINGK, "7");
        props.put(ChaseCamera.PROP_STAYBEHINDTARGET, "true");
        chaser = new ChaseCamera(cam, player, props);
        chaser.setMaxDistance(9);
        chaser.setMinDistance(3.5f);	
    }

    /**
     * create our custom input handler.
     *
     */
    private void buildInput() {
        input = new AlienRabbleHandler(player, properties.getRenderer());
    }
    


    /**
     * will be called if the resolution changes
     * 
     * @see com.jme.app.BaseGame#reinit()
     */
    protected void reinit() {
        display.recreateWindow(width, height, depth, freq, fullscreen);
    }
    


    /**
     * clean up the textures.
     * 
     * @see com.jme.app.BaseGame#cleanup()
     */
    public void cleanup() {

    }
}

