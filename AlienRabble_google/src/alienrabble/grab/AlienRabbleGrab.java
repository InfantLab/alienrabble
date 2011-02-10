package alienrabble.grab;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import alienrabble.MenuState;
import alienrabble.logging.ARDataLoadandSave;
import alienrabble.logging.ARXMLExperimentData;
import alienrabble.logging.ARXMLGrabData;
import alienrabble.logging.ARXMLExperimentData.GameType;
import alienrabble.logging.ARXMLGrabData.GrabEvent;
import alienrabble.logging.ARXMLGrabData.PlayerLocation;
import alienrabble.model.ARXMLModelData;
import alienrabble.model.Model;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.image.Texture;
import com.jme.image.Texture.CombinerScale;
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
import com.jme.scene.state.BlendState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.system.GameSettings;
import com.jme.util.TextureManager;
import com.jme.util.Timer;
import com.jme.util.export.binary.BinaryImporter;
import com.jmex.audio.AudioSystem;
import com.jmex.audio.AudioTrack;
import com.jmex.game.state.CameraGameState;
import com.jmex.game.state.GameStateManager;
import com.jmex.terrain.TerrainBlock;
import com.jmex.terrain.util.MidPointHeightMap;
import com.jmex.terrain.util.ProceduralTextureGenerator;

public class AlienRabbleGrab extends CameraGameState{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AlienRabbleGrab.class.getName());
	
	private static final float GRAB_RADIUS = 2.5f;
	    
	// temporary vector for the rotation
    private static final Vector3f tempVa = new Vector3f();
	
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
    private Node onscreentext;
    private Text alienCount;
    private int score;
    private Text runningScore;
    private Text alienPoints;
    //private CollisionTreeManager collisionTreeManager;
	private CollisionResults results;
	
    private TimeGauge timeGauge;
    private Node guiNode;
    private int gaugeVal;
    private int gaugeChangeValue;
    private float lastUpdate = 0;
		
	/** Our display system. */
	private DisplaySystem display;
	 /** Game display properties. */
    protected GameSettings settings;
		
    protected InputHandler input;
    //the timer
    protected Timer timer;
    //The chase camera, this will follow our player as he zooms around the level
    private RestrictedChaseCamera chaser;
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

    ARXMLExperimentData expdata;
    
    //the data logger
    ARXMLGrabData grabdata;
    ARXMLGrabData ruledata;
    
    ARXMLModelData modeldata;
    
    private RightWrong rightWrong;  // the feedback icon in centre of screen
    private RightWrong[] rightCounter; //the column of cumulatively collected correct items
    private RightWrong[] wrongCounter; //the column of cumulatively collected correct items
    private int rightRunningTotal = 0;  //count
    private int wrongRunningTotal = 0;  //count
    private int sizeCategory1 = 8;
    
    private int blockCount = 0;
    private int roundCount =0;
    
    //sound effects
    AudioTrack laserSound;
   	AudioTrack targetSound;
    
	public AlienRabbleGrab(String name, GameSettings settings) {
		super(name);
		this.settings = settings;
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

        //We don't want the chase camera to go below the world, so always keep 
        //it at least 1.7 units above the level.
        if(cam.getLocation().y < (tb.getHeight(cam.getLocation())+1.7f)) {
            cam.getLocation().y = tb.getHeight(cam.getLocation())+1.7f;
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
        
        //update the player to check for collisions
        player.update(interpolation);        
        
        //update the chase camera to handle the player moving around.
       	chaser.update(interpolation, player);
        //update the fence to animate the force field texture
        fence.update(interpolation);
        
        //we want to keep the skybox around our eyes, so move it with
        //the camera
        skybox.setLocalTranslation(cam.getLocation());
        skybox.updateGeometricState(0, true);
        

        // if escape was pressed, we exit
        if (expdata.gameType == ARXMLExperimentData.GameType.RULEDISCOVERY){
        	if ( rightRunningTotal == sizeCategory1 
        	 ||  aliencontainer.getChildren().size() == 0){
        		//have collected all the 'good' aliens
        		//potentially we go round again
        		if (blockCount < expdata.getNumBlocks())
        		{
	        		if(roundCount < expdata.getNumRounds(blockCount)){
		        		MenuState ms = (MenuState) GameStateManager.getInstance().getChild("menu");
		        		ms.menuStatus = MenuState.MENU_RULE_NEWROUND;
		        		ms.setActive(true);
		        		this.setActive(false);
	        		}else{
	        			MenuState ms = (MenuState) GameStateManager.getInstance().getChild("menu");
		        		ms.menuStatus = MenuState.MENU_RULE_NEWBLOCK;
		        		ms.setActive(true);
		        		this.setActive(false);
	        		}
        		}else{
            		MenuState ms = (MenuState) GameStateManager.getInstance().getChild("menu");
            		ms.menuStatus = MenuState.MENU_FINISH;
            		ms.setActive(true);
            		// And remove this state, because we don't want to keep it in memory.
        			GameStateManager.getInstance().detachChild("ingrabgame");
        		}
        	}
        }else{
	        if (aliencontainer.getChildren().size() == 0) {
	    		// Here we switch to the menu state which is already loaded
	    		MenuState ms = (MenuState) GameStateManager.getInstance().getChild("menu");
	    		ms.menuStatus = MenuState.MENU_SORT_INSTRUCTIONS;
	    		ms.setActive(true);
	    		
				// And remove this state, because we don't want to keep it in memory.
				GameStateManager.getInstance().detachChild("ingrabgame");
			}
        }        
        

        
        //Because we are changing the scene (moving the skybox and player) we need to update
        //the graph.
        scene.updateGeometricState(interpolation, true);
        
        if (expdata.getTimeGauge() > 0){
        	//update the countdown gauge
	        if (timer.getTimeInSeconds() > lastUpdate + 0.1f) {
	            gaugeVal += gaugeChangeValue;
	            if (gaugeVal > timeGauge.getMaximum() ||
	            		gaugeVal < timeGauge.getMinimum()) {
	                gaugeChangeValue *= -1;
	            }
	            lastUpdate = timer.getTimeInSeconds();
	            timeGauge.setGauge(gaugeVal);
	        }
        }
        rightWrong.updateRenderState();
        
		for(int i = 0; i < rightCounter.length ; i++){
			rightCounter[i].updateRenderState();
		}
		for(int i = 0; i < wrongCounter.length ; i++){
			wrongCounter[i].updateRenderState();
		}
        
       List<Spatial> remainingaliens = aliencontainer.getChildren();
       for(int i= 0; i< remainingaliens.size();i++){
    	   Alien thisalien = (Alien) remainingaliens.get(i);
    	   thisalien.update(interpolation);
       }
       
        //check for collisions
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
		        		
		        	
//					/** Signal our sound to play laser during collision */		        	
		        	
                    relativePosition.set( element.getWorldTranslation() );
                    relativePosition.subtractLocal( player.getWorldTranslation() );
                    final float distance = relativePosition.length();
                    if ( distance < GRAB_RADIUS ) {
                    	player.setVelocity(-0.1f * player.getVelocity());
    		        	//we should make this vanish and log 
                    	
                    	Alien as = (Alien) element;
    		        	
			            laserSound.setWorldPosition(element.getWorldTranslation());
			            laserSound.play();

    		        		
    					//log the grab location
    					GrabEvent ge = grabdata.new GrabEvent();
    					ge.alienid= as.getID();
    					ge.alienname = as.getName();
    					//need current time for logging
    					ge.clockTicks = timer.getTime();
    					ge.timeInSecs =  ge.clockTicks * 1f / timer.getResolution(); // *1f to get result as float
    					ge.x_location = as.getLocalTranslation().x;
    					ge.z_location = as.getLocalTranslation().z;
    					ge.block = blockCount;
    					ge.round = roundCount;
    					grabdata.addGrabEvent(ge);
    					

    					score += as.getModel().Points();
//    					//remove from scene
//    		        	as.removeFromParent();
    					as.grabAlien(player);

    					// show the feedback icon
    					if (expdata.gameType == ARXMLExperimentData.GameType.RULEDISCOVERY){    						
    						if (as.Category() == 1){
    							rightWrong.setShowPoints(expdata.getShowScores(), as.getModel().Points());
    							rightWrong.setIsRight(true);
	    						rightWrong.setBlankTime(3);
	    						rightCounter[rightRunningTotal].setIsRight(true);
	    						rightCounter[rightRunningTotal].setBlankTime(-1);
	    						rightRunningTotal++;
    						}else{
    							rightWrong.setShowPoints(expdata.getShowScores(), as.getModel().Points());
    							rightWrong.setIsRight(false);
	    						rightWrong.setBlankTime(3);
	    						wrongCounter[wrongRunningTotal].setIsRight(false);
	    						wrongCounter[wrongRunningTotal].setBlankTime(-1);
	    						wrongRunningTotal++;
    						}
    		        	}

    					
    					//update count 
    		        	numAliens--;
    		        	
    		        	alienCount.print("Aliens: " + numAliens);
    		        	runningScore.print("Score: " + score);
    		        	break;                    	
                    }
		        }
            }
        } 
    }
    
    
    private void clearRightWrongCounters(){

    	for (int counter=0;counter<sizeCategory1;counter++){
    		//is there a better way to blank these?
        	rightCounter[counter].setBlankTime(.001f);
        	wrongCounter[counter].setBlankTime(.001f);
	    }
    }


	/**
	 * Gets called every time the game state manager switches to this game state.
	 * Sets the window title.
	 */
	public void onActivate() {
		DisplaySystem.getDisplaySystem().setTitle("Alien Rabble - Grab Stage");
		super.onActivate();
	}
    
	
	private void setupSounds() {
        /** Set the 'ears' for the sound API */
        AudioSystem audio = AudioSystem.getSystem();
        audio.getEar().trackOrientation(cam);
        audio.getEar().trackPosition(cam);
		
		/** Create program sound */
		targetSound = audio.createAudioTrack( AlienRabbleGrab.class.getClassLoader().getResource( "alienrabble/data/sound/explosion.ogg" ), false);
        targetSound.setMaxAudibleDistance(1000);
        targetSound.setVolume(1.0f);
		laserSound = audio.createAudioTrack( AlienRabbleGrab.class.getClassLoader().getResource( "alienrabble/data/sound/whizzoop.ogg" ), false);
        laserSound.setMaxAudibleDistance(1000);
        laserSound.setVolume(1.0f);
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
//        display.getRenderer().draw(guiNode);
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
        width = settings.getWidth();
        height = settings.getHeight();
        depth = settings.getDepth();
        freq = settings.getFrequency();
        fullscreen = settings.getBoolean("Fullscreen",false);
        
        
        display = DisplaySystem.getDisplaySystem(settings.getRenderer());
        // set the background to black
        display.getRenderer().setBackgroundColor(ColorRGBA.black.clone());

        // initialize the camera
        cam.setFrustumPerspective(40.0f, (float) width / (float) height, 2,
                400);
        cam.setLocation(new Vector3f(200,400,200));
        
        /** Signal that we've changed our camera's location/frustum. */
        cam.update();

        display.getRenderer().setCamera(cam);

        /** Get a high resolution timer for FPS updates. */
        timer = Timer.getTimer();
        timer.reset();
    }

    public void newBlock(){
    	blockCount++;
    	roundCount = 0;
    	//initGame();
        
    	newRound();
    	
     }

    public void newRound(){
    	roundCount++;
    	//initGame();
        
    	//reset counters
    	rightRunningTotal = 0;
        wrongRunningTotal = 0;
    	//remove any remaining aliens from previous round. 
    	clearRightWrongCounters();
    	

    	
    	//Add aliens randomly to the terrain
    	addAliens();
        if (expdata.getTimeGauge() > 0){        
        	timeGauge.setGauge(expdata.getTimeGauge());
        }
    	player.setLocalTranslation(new Vector3f(100,0, 100));
		//log the grab location
		GrabEvent ge = grabdata.new GrabEvent();
		ge.alienid= "-1";
		ge.alienname = new String("Start round " + roundCount);
		//need current time for logging
		ge.clockTicks = timer.getTime();
		ge.timeInSecs =  ge.clockTicks * 1.0f / timer.getResolution(); 
		ge.x_location = player.getLocalTranslation().x;
		ge.z_location = player.getLocalTranslation().z;
		ge.block = blockCount;
		ge.round = roundCount;
		grabdata.addGrabEvent(ge);
    }
    
    /**
     * initializes the scene
     * 
     * @see com.jme.app.BaseGame#initGame()
     */
    protected void initGame() {
        display.setTitle("Alien Rabble");
        
        expdata = ARDataLoadandSave.getInstance().getXmlExperimentData();
        //get a reference to the data logging class
        grabdata = ARDataLoadandSave.getInstance().getXmlGrabData();
        ruledata = ARDataLoadandSave.getInstance().getXmlRuleDiscoveryData(0);
        
        results = new BoundingCollisionResults(); 
        
        scene = rootNode;
        /** Create a ZBuffer to display pixels closest to the camera above farther ones.  */
        ZBufferState buf = display.getRenderer().createZBufferState();
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo );
        scene.setRenderState(buf);
        
        //Time for a little optimization. We don't need to render back face triangles, so lets
        //not. This will give us a performance boost for very little effort.
        CullState cs = display.getRenderer().createCullState();
        cs.setCullFace(CullState.Face.Back);
        scene.setRenderState(cs);
        
        //collisionTreeManager = new CollisionTreeManager( scene, new float[]{0.2f, 1.2f} );
        
        //Add the skybox
        buildSkyBox();
        //Add terrain to the scene
        buildTerrain();
        //Add a flag randomly to the terrain
        //buildFlag();
        //Light the world
        buildLighting();
        //add the force field fence
        buildEnvironment();
        //Build the player
        buildPlayer();
        //build the chase camera
        buildChaseCamera();
        //build the player input
        buildInput();

        buildOnScreenText();
        
        //set up passes
        buildPassManager();

        if (expdata.getTimeGauge() > 0){        
        	buildTimeGauge();
        }
        
        //add sound effects
        setupSounds();
        
        // update the scene graph for rendering
        scene.updateGeometricState(0.0f, true);
        scene.updateRenderState();
        
        setupRightWrong();
        setupRightWrongCount(sizeCategory1);

        //Finally
        //Add  aliens randomly to the terrain
        newRound();

    }
    
    /**
     * Init the TimeGauge:
     * - Position on Screen
     * - Min / Max Values for the Gauge
     */
    private void buildTimeGauge(){
        gaugeChangeValue = 2;
        gaugeVal = expdata.getTimeGauge();
        timeGauge = new TimeGauge(display, "timegauge");
        timeGauge.setMinimum(0);
        timeGauge.setMaximum(expdata.getTimeGauge());
        timeGauge.setIsVertical(true);
        timeGauge.setWidthHeight(40,300);
        //timeGauge.setScale((display.getWidth() / 40),(display.getHeight() / 300));
        // position the timeGauge in the lower left corner
        //timeGauge.setPosition(timeGauge.getWidth(), timeGauge.getHeight()+50);
        timeGauge.setPosition(display.getWidth() - 40f, 20f + 0.5f*timeGauge.getHeight());
        timeGauge.setGauge(gaugeVal);
        buildHUD();
    }
    
    private void setupRightWrong(){
        rightWrong = new RightWrong(display, "rightwrongfeedback");
        rightWrong.setIsSmiley(true);
        rightWrong.setIsRight(false);
        rightWrong.setBlankTime(0);
        
        rightWrong.setPosition(display.getWidth()/2, display.getHeight()/5);
        
        rightWrong.updateRenderState();
        rightWrong.updateGeometricState(0, true);
        
        scene.attachChild(rightWrong);
        
    }

    private void setupRightWrongCount(int maxcount){

    	float scalefactor = 1/FastMath.sqrt(2 + maxcount);
 
    	rightCounter = new RightWrong[maxcount];
    	wrongCounter = new RightWrong[maxcount];
		for(int i = 0; i < maxcount; i++){
			rightCounter[i] = new RightWrong(display, "rwcount"+i);
			rightCounter[i].setIsSmiley(true);
			rightCounter[i].setIsRight(false);
			rightCounter[i].setBlankTime(0);
			rightCounter[i].setScale(scalefactor, scalefactor);
			rightCounter[i].setPosition(80*scalefactor, 100 + i * 160 * scalefactor );
			scene.attachChild(rightCounter[i] );
		}
		for(int i = 0; i < maxcount; i++){
			wrongCounter[i] = new RightWrong(display, "rwcount"+i);
			wrongCounter[i].setIsSmiley(true);
			wrongCounter[i].setIsRight(false);
			wrongCounter[i].setBlankTime(0);
			wrongCounter[i].setScale(scalefactor, scalefactor);
			wrongCounter[i].setPosition(240 * scalefactor, 100 + i * 160 * scalefactor  );
			scene.attachChild(wrongCounter[i] );
		}
    }

    

    
    /**
     * create a GUI Node to attach the TimeGauge to. 
     */
    private void buildHUD() {
        guiNode = new Node("gui swing");
        // Render the Gui node in the Ortho Queue
        guiNode.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        // attach the ProgressBars node to the GuiNode
        guiNode.attachChild(timeGauge.getNode());
        // don't cull the gui away
        guiNode.setCullHint(Spatial.CullHint.Never);
        // gui needs no lighting
        guiNode.setLightCombineMode(Spatial.LightCombineMode.Off);
        // update the render states (especially the texture state of the ProgressBar!)
        guiNode.updateRenderState();
        // update the world vectors (needed as we have altered local translation
        // of the desktop and it's
        // not called in the update loop)
        guiNode.updateGeometricState(0, true);
        
        scene.attachChild(guiNode);
    }
    
    private void buildOnScreenText(){
    	
    	onscreentext = new Node("textparent");
    	runningScore = Text.createDefaultTextLabel("score", "Score: ");
//    	alienPoints = Text.createDefaultTextLabel("alienpoints", "");
        alienCount = Text.createDefaultTextLabel("AlienCount", "Aliens: ");
       
        alienCount.setLocalTranslation(new Vector3f(10, 10, 0));
        alienCount.setTextColor(ColorRGBA.white);
        runningScore.setLocalTranslation(new Vector3f(width-20, 10, 0));
        runningScore.setTextColor(ColorRGBA.white);
//        alienPoints.setLocalTranslation(new Vector3f(width/2, height/2 + 50, 0));
//        alienPoints.setTextColor(ColorRGBA.white);
          
        onscreentext.setLocalScale(3f);
        
        onscreentext.attachChild(alienCount);
//        onscreentext.attachChild(alienPoints);
        onscreentext.attachChild(runningScore);
        scene.attachChild(onscreentext);
//        scene.attachChild(alienCount);
//        scene.attachChild(alienPoints);
//        scene.attachChild(runningScore);

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
        shadowPass.setLightingMethod(ShadowedRenderPass.LightingMethod.Modulative);
        
        passManager.add(shadowPass);
    }
    
    /**
     * Add full set of aliens across environment
     *
     */
    private void addAliens() {
    	
    	aliencontainer = new Node("aliencontainer");
    	scene.attachChild(aliencontainer);
    	
    	if( expdata.gameType == GameType.RULEDISCOVERY){
    		modeldata = ARDataLoadandSave.getInstance().getXmlModelData_RuleDiscovery(blockCount);
    	}else{
    		modeldata = ARDataLoadandSave.getInstance().getXmlModelData_Grab();
    	}
		numAliens = modeldata.getNumModels();
		allAliens = new Alien[numAliens];
		
		for(int i=0;i<numAliens;i++)
		{
			Model thisalien = modeldata.getModel(i);
			allAliens[i] = new Alien(tb, scene,thisalien.getName() ,thisalien);
			aliencontainer.attachChild(allAliens[i]);
			allAliens[i].placeAlien();
			allAliens[i].setID(thisalien.getID());

			//log the initial positions
			GrabEvent ge = grabdata.new GrabEvent();
			ge.block = blockCount;
			ge.round = roundCount;
			ge.alienid= allAliens[i].getID();
			ge.alienname = allAliens[i].getName();
			ge.clockTicks = 0;
			ge.timeInSecs = 0;
			ge.x_location = allAliens[i].getLocalTranslation().x;
			ge.z_location = allAliens[i].getLocalTranslation().z;
			grabdata.addStartingPosition(ge);		
		}
		alienCount.print("Aliens: " + numAliens);
    }
    
    /**
     * we are going to build the player object here. 
     * 
     * We now have a Vehicle object that represents our player. The vehicle object will allow
     * us to have multiple vehicle types with different capabilities.
     *
     */
    private void buildPlayer() {
        Spatial model = null;
        try {
            URL bikeFile = AlienRabbleGrab.class.getClassLoader().getResource("alienrabble/data/model/grabber2.jbin");
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
        player = new Vehicle("player",scene, model);
        player.setAcceleration(8);
        player.setBraking(12);
        player.setTurnSpeed(2.4f);
        player.setWeight(17);
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
        
        PlayerLocation pl = grabdata.new PlayerLocation();
        pl.name =  player.getName();
        pl.clockTicks = 0;
        pl.timeInSecs = 0;
        pl.x_location = player.getLocalTranslation().x;
        pl.z_location = player.getLocalTranslation().z;
        Vector3f rotation = player.getLocalRotation().getRotationColumn(2,tempVa);
        pl.x_velocity = player.getVelocity() * rotation.x;
        pl.z_velocity = player.getVelocity() * rotation.z;
       
        grabdata.logPlayerLocation(pl);
    }
    
    /**
     * buildEnvironment will create a fence. 
     */
    private void buildEnvironment() {
        //This is the main node of our fence
        fence = new ForceFieldFence("fence");
        
        //we will do a little 'tweaking' by hand to make it fit in the terrain a bit better.
        //first we'll scale the entire "model" by a factor of 6
        fence.setLocalScale( expdata.getArenaSize());
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

        DirectionalLight light2 = new DirectionalLight();
        light2.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        light2.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, .5f));
        light2.setDirection(new Vector3f(-1,-1,0.5f));
        light2.setShadowCaster(true);
        light2.setEnabled(true);

        
          /** Attach the light to a lightState and the lightState to rootNode. */
        LightState lightState = display.getRenderer().createLightState();
        lightState.setEnabled(true);
        lightState.setGlobalAmbient(new ColorRGBA(.4f, .4f, .4f, 1f));
        lightState.attach(light);
        lightState.attach(light2);
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
                heightMap.getHeightMap(), new Vector3f(0, 0, 0));

        tb.setModelBound(new BoundingBox());
        tb.updateModelBound();

        // generate a terrain texture with 2 textures
        ProceduralTextureGenerator pt = new ProceduralTextureGenerator(
                heightMap);
        pt.addTexture(new ImageIcon(AlienRabbleGrab.class.getClassLoader()
                .getResource("alienrabble/data/texture/grassb.png")), -128, 0, 128);
        pt.addTexture(new ImageIcon(AlienRabbleGrab.class.getClassLoader()
                .getResource("alienrabble/data/texture/dirt.jpg")), 0, 128, 255);
        pt.addTexture(new ImageIcon(AlienRabbleGrab.class.getClassLoader()
                .getResource("alienrabble/data/texture/highest.jpg")), 128, 255,
                384);
        pt.createTexture(32);
        
        // assign the texture to the terrain
        TextureState ts = display.getRenderer().createTextureState();
        Texture t1 = TextureManager.loadTexture(pt.getImageIcon().getImage(),
                Texture.MinificationFilter.Trilinear, Texture.MagnificationFilter.Bilinear, true);
        ts.setTexture(t1, 0);
        
        //load a detail texture and set the combine modes for the two terrain textures.
        Texture t2 = TextureManager.loadTexture(
        		AlienRabbleGrab.class.getClassLoader().getResource(
                "alienrabble/data/texture/Detail.jpg"),
                Texture.MinificationFilter.Trilinear,
                Texture.MagnificationFilter.Bilinear);

        ts.setTexture(t2, 1);
        t2.setWrap(Texture.WrapMode.Repeat);

        t1.setApply(Texture.ApplyMode.Combine);
        t1.setCombineFuncRGB(Texture.CombinerFunctionRGB.Modulate);
        t1.setCombineSrc0RGB(Texture.CombinerSource.CurrentTexture);
        t1.setCombineOp0RGB(Texture.CombinerOperandRGB.SourceColor);
        t1.setCombineSrc1RGB(Texture.CombinerSource.PrimaryColor);
        t1.setCombineOp1RGB(Texture.CombinerOperandRGB.SourceColor);
        t1.setCombineScaleRGB(CombinerScale.One);

        t2.setApply(Texture.ApplyMode.Combine);
        t2.setCombineFuncRGB(Texture.CombinerFunctionRGB.AddSigned);
        t2.setCombineSrc0RGB(Texture.CombinerSource.CurrentTexture);
        t2.setCombineOp0RGB(Texture.CombinerOperandRGB.SourceColor);
        t2.setCombineSrc1RGB(Texture.CombinerSource.Previous);
        t2.setCombineOp1RGB(Texture.CombinerOperandRGB.SourceColor);
        t2.setCombineScaleRGB(CombinerScale.One);

        tb.setRenderState(ts);
        //set the detail parameters.
        tb.setDetailTexture(1, 16);
        tb.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        scene.attachChild(tb);
        
        
        TextureState treeTex = display.getRenderer().createTextureState();
        treeTex.setEnabled(true);
        Texture tr = TextureManager.loadTexture(
                AlienRabbleGrab.class.getClassLoader().getResource(
                        "alienrabble/data/texture/grass.jpg"), Texture.MinificationFilter.Trilinear,
                Texture.MagnificationFilter.Bilinear);
        treeTex.setTexture(tr);
        
        Pyramid p = new Pyramid("Pyramid", 3, 6);
        p.setModelBound(new BoundingBox());
        p.updateModelBound();
        p.setRenderState(treeTex);
        p.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        p.setTextureCombineMode(Spatial.TextureCombineMode.Replace);
        
        int numtrees = expdata.getNumTrees();
        for (int i = 0; i < numtrees; i++) {
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
        		AlienRabbleGrab.class.getClassLoader().getResource(
            "alienrabble/data/skybox/north.jpg"),
            Texture.MinificationFilter.BilinearNearestMipMap,
            Texture.MagnificationFilter.Bilinear);
        Texture south = TextureManager.loadTexture(
        		AlienRabbleGrab.class.getClassLoader().getResource(
            "alienrabble/data/skybox/south.jpg"),
            Texture.MinificationFilter.BilinearNearestMipMap,
            Texture.MagnificationFilter.Bilinear);
        Texture east = TextureManager.loadTexture(
        		AlienRabbleGrab.class.getClassLoader().getResource(
            "alienrabble/data/skybox/east.jpg"),
            Texture.MinificationFilter.BilinearNearestMipMap,
            Texture.MagnificationFilter.Bilinear);
        Texture west = TextureManager.loadTexture(
        		AlienRabbleGrab.class.getClassLoader().getResource(
            "alienrabble/data/skybox/west.jpg"),
            Texture.MinificationFilter.BilinearNearestMipMap,
            Texture.MagnificationFilter.Bilinear);
        Texture up = TextureManager.loadTexture(
        		AlienRabbleGrab.class.getClassLoader().getResource(
            "alienrabble/data/skybox/top.jpg"),
            Texture.MinificationFilter.BilinearNearestMipMap,
            Texture.MagnificationFilter.Bilinear);
        Texture down = TextureManager.loadTexture(
        		AlienRabbleGrab.class.getClassLoader().getResource(
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
        scene.attachChild(skybox);
    }
    
    public void setGrabDataLogger(ARXMLGrabData gd){
    	grabdata = gd;
    }
    public ARXMLGrabData getGrabDataLogger(){
    	return grabdata;
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
//        HashMap<String, Object> props = new HashMap<String, Object>();
//        Vector3f targetOffset = new Vector3f();
//        targetOffset.y = ((BoundingSphere) player.getWorldBound()).radius * 1.8f;
//        props.put(ThirdPersonMouseLook.PROP_ENABLED, "false");
//        props.put(ChaseCamera.PROP_TARGETOFFSET,targetOffset);
//        props.put(ChaseCamera.PROP_INITIALSPHERECOORDS, new Vector3f(4.4f, 0,  20 * FastMath.DEG_TO_RAD));
//        props.put(ThirdPersonMouseLook.PROP_MAXASCENT, ""+45 * FastMath.DEG_TO_RAD);
//        props.put(ChaseCamera.PROP_DAMPINGK, "3");
//        props.put(ChaseCamera.PROP_SPRINGK, "7");
//        props.put(ChaseCamera.PROP_STAYBEHINDTARGET, "true");
//       
//        chaser = new RestrictedChaseCamera(cam, player, props);
//        chaser.setMaxDistance(7f);
//        chaser.setMinDistance(3.2f);	
        HashMap<String, Object> props = new HashMap<String, Object>();
        Vector3f targetOffset = new Vector3f();
        targetOffset.y = ((BoundingSphere) player.getWorldBound()).radius * 2.0f;
        props.put(ThirdPersonMouseLook.PROP_ENABLED, "false");
        props.put(ChaseCamera.PROP_TARGETOFFSET,targetOffset);
        props.put(ChaseCamera.PROP_INITIALSPHERECOORDS, new Vector3f(6f, 0,  20 * FastMath.DEG_TO_RAD));
        props.put(ThirdPersonMouseLook.PROP_MAXASCENT, +70 * FastMath.DEG_TO_RAD);
        props.put(ChaseCamera.PROP_DAMPINGK, "3");
        props.put(ChaseCamera.PROP_SPRINGK, "3");
        props.put(ChaseCamera.PROP_STAYBEHINDTARGET, "true");
       
        chaser = new RestrictedChaseCamera(cam, player, props);
        chaser.setMaxDistance(10f);
        chaser.setMinDistance(2.8f);	
    }

    /**
     * create our custom input handler.
     *
     */
    private void buildInput() {
        input = new AlienRabbleHandler(player, settings.getRenderer());
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

