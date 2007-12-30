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

import com.jme.app.SimpleGame;
import com.jme.image.Texture;
import com.jme.input.AbsoluteMouse;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.SceneElement;
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
    private static final Logger logger = Logger.getLogger(AlienRabbleSort.class
            .getName());

	private AlienSort[] allAlienSort;
    private AbsoluteMouse mouse;

	
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

        
        Text cross = Text.createDefaultTextLabel("Cross hairs", "+");
        cross.setCullMode(SceneElement.CULL_NEVER);
        cross.setTextureCombineMode(TextureState.REPLACE);
        cross.setLocalTranslation(new Vector3f(
				display.getWidth() / 2f - 8f, // 8 is half the width
														// of a font char
				display.getHeight() / 2f - 8f, 0));

		fpsNode.attachChild(text);

		Quaternion q = new Quaternion();
		q.fromAngleAxis(FastMath.PI/2, new Vector3f(-1,0, 0));
        
		String[] allAliens;
		
		allAliens = new String[3];
		allAliens[0] = "alienrabble/data/Greebles/Family1/f1-11.jbin";
		allAliens[1] = "alienrabble/data/Greebles/Family1/f1-12.jbin";
		allAliens[2] = "alienrabble/data/Greebles/Family1/m1_11.jbin";
		
		allAlienSort = new AlienSort[3];
	
		for(int i=0;i<allAliens.length;i++)
		{
			URL alienURL = AlienSort.class.getClassLoader().getResource(allAliens[i]);
			BinaryImporter BI = new BinaryImporter();
			Spatial model;
			try {
				model = (Spatial)BI.load(alienURL.openStream());
				allAlienSort[i] = new AlienSort(allAliens[i],model);
				allAlienSort[i].setLocalTranslation(-10 + 10*i,30,0);
				allAlienSort[i].setLocalRotation(q);
				allAlienSort[i].setupTranslations();
				rootNode.attachChild(allAlienSort[i]);				
			} catch (IOException e) {
				logger.info("darn exceptions:" + e.getMessage());
			}
		}
		AlienPick pick = new AlienPick(display, cam, rootNode, text);
		input.addAction(pick);
	}
}