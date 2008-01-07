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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmetest.input.TestInputHandler;
import alienrabble.AlienRabble;

import com.jme.app.SimpleGame;
import com.jme.image.Texture;
import com.jme.input.AbsoluteMouse;
import com.jme.input.KeyInput;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.SceneElement;
import com.jme.scene.Skybox;
import com.jme.scene.Spatial;
import com.jme.scene.Text;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import com.jme.util.export.binary.BinaryImporter;
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.SimpleResourceLocator;


/**
 * <code>TestPick</code>
 * 
 * @author Mark Powell
 * @version $Id: TestPick.java,v 1.35 2007/08/17 22:04:20 nca Exp $
 */
public class AlienRabbleSort extends SimpleGame {
	private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(AlienRabbleSort.class
            .getName());

	private AlienSort[] allAlienSort;
    private AbsoluteMouse mouse;
    private PackingCases packingcases;
    private Skybox skybox;
    private KeyInput key;

	
	/**
	 * Entry point for the test,
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		AlienRabbleSort app = new AlienRabbleSort();
		app.setDialogBehaviour(ALWAYS_SHOW_PROPS_DIALOG);
		app.start();
	}

	/**
	 * builds the trimesh.
	 * 
	 * @see com.jme.app.SimpleGame#initGame()
	 */
	protected void simpleInitGame() {
        try {
            ResourceLocatorTool.addResourceLocator(
                    ResourceLocatorTool.TYPE_TEXTURE,
                    new SimpleResourceLocator(AlienRabbleSort.class
                            .getClassLoader().getResource(
                                    "alienrabble/data/Greebles/Family1/")));
        } catch (URISyntaxException e1) {
            logger.log(Level.WARNING, "unable to setup texture directory.", e1);
        }

        input.removeAllFromAttachedHandlers();
        
        display.setTitle("Mouse Pick");
		cam.setLocation(new Vector3f(0.0f, 0f, 100.0f));
		cam.update();
		
        Text text = Text.createDefaultTextLabel("Test Label", "Hits: 0 Shots: 0");
        text.setCullMode(SceneElement.CULL_NEVER);
        text.setTextureCombineMode(TextureState.REPLACE);
        text.setLocalTranslation(new Vector3f(1, 60, 0));

        mouse = new AbsoluteMouse( "Mouse Cursor", display.getWidth(), display.getHeight() );
        TextureState cursorTextureState = display.getRenderer().createTextureState();
        cursorTextureState.setTexture(
                TextureManager.loadTexture(
                        TestInputHandler.class.getClassLoader().getResource( "jmetest/data/cursor/cursor1.PNG" ),
                        Texture.MM_LINEAR, Texture.FM_LINEAR )
        );
        mouse.setRenderState( cursorTextureState );
        mouse.registerWithInputHandler( input );
        
        AlphaState as1 = display.getRenderer().createAlphaState();
        as1.setBlendEnabled(true);
        as1.setSrcFunction(AlphaState.SB_ONE);
        as1.setDstFunction(AlphaState.DB_ONE_MINUS_SRC_COLOR);
        as1.setTestEnabled(true);
        as1.setTestFunction(AlphaState.TF_GREATER);
        mouse.setRenderState(as1);
        fpsNode.attachChild(mouse);
        fpsNode.attachChild(text);

        AlienPick pick = new AlienPick(display, rootNode, text);
		input.addAction(pick);
		
		/**
		 * Set the action called "firebullet", bound to KEY_F, to performAction
		 * FireBullet
		 */
        input.addAction( new FireBullet(), "addCase", KeyInput.KEY_F, false );
        
		String[] allAliens;
		
		allAliens = new String[4];
		allAliens[0] = "alienrabble/data/Greebles/Family1/f1-11.jbin";
		allAliens[1] = "alienrabble/data/Greebles/Family1/f1-12.jbin";
		allAliens[2] = "alienrabble/data/Greebles/Family1/m1_11.jbin";
		allAliens[3] = "alienrabble/data/Greebles/Family1/m1_12.jbin";
		
		allAlienSort = new AlienSort[4];
	
		for(int i=0;i<allAliens.length;i++)
		{
			URL alienURL = AlienSort.class.getClassLoader().getResource(allAliens[i]);
			BinaryImporter BI = new BinaryImporter();
			Spatial model;
			Quaternion q = new Quaternion();
			q.fromAngleAxis(FastMath.PI/2, new Vector3f(-1,0, 0));
			try {
				model = (Spatial)BI.load(alienURL.openStream());
				allAlienSort[i] = new AlienSort(allAliens[i],model);
				allAlienSort[i].setLocalTranslation(-10 + 10*i,30,0);
				allAlienSort[i].setLocalRotation(q);
				allAlienSort[i].setInitialValues();
				allAlienSort[i].addAllControllers();
				rootNode.attachChild(allAlienSort[i]);				
			} catch (IOException e) {
				logger.info("darn exceptions:" + e.getMessage());
			}
		}
		
		packingcases = new PackingCases(3);
		
		rootNode.attachChild(packingcases);
		
		buildSkyBox();
		rootNode.attachChild(skybox);

		

        key = KeyInput.get();
	}
	
	
	protected void simpleUpdate()  {
        if(key.isKeyDown(KeyInput.KEY_DOWN)) {
        	packingcases.removeCase();     
        }

        if(key.isKeyDown(KeyInput.KEY_UP)) {
        	packingcases.addCase();
        }
//    	for(int i=0;i<allAlienSort.length;i++){
//    		allAlienSort[i].update(tpf);
//    	}
    }
	
	
    /**
     * buildSkyBox creates a new skybox object with all the proper textures. The
     * textures used are the standard skybox textures from all the tests.
     *
     */
    private void buildSkyBox() {
        skybox = new Skybox("skybox", 200, 200, 200);

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
       // scene.attachChild(skybox);
    }
	
	
}