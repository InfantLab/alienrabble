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

package alienconverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import com.jme.app.AbstractGame;
import com.jme.app.SimpleGame;
import com.jme.bounding.BoundingSphere;
import com.jme.scene.Node;
import com.jme.util.export.Savable;
import com.jme.util.export.binary.BinaryExporter;
import com.jme.util.export.binary.BinaryImporter;
import com.jmex.model.converters.AseToJme;
import com.jmex.model.converters.FormatConverter;
import com.jmex.model.converters.MaxToJme;
import com.jmex.model.converters.Md2ToJme;
import com.jmex.model.converters.Md3ToJme;
import com.jmex.model.converters.MilkToJme;
import com.jmex.model.converters.ObjToJme;
import com.jmex.model.util.ModelLoader;

/**
 * Started Date: Jul 22, 2004<br><br>
 *
 * Demonstrates loading formats.
 * 
 * @author Jack Lindamood
 */
public class AlienConverter extends SimpleGame {
    private static final Logger logger = Logger
            .getLogger(AlienConverter.class.getName());
    
    public static void main(String[] args) {
        AlienConverter app = new AlienConverter();
        app.setDialogBehaviour(AbstractGame.ALWAYS_SHOW_PROPS_DIALOG);
        // Turn the logger off so we can see the XML later on
        app.start();
    }

    protected void simpleInitGame() {

//    		String modelpath = "jmetest/data/model/bike.3ds";
//    		String modelpath = "jmetest/data/model/grabber2.3ds";
 //   		String modelpath = "jmetest/data/Greebles/f1-11.3ds";
    		String modelpath;
    		Node newmodel;
 
    		
    		modelpath = "alienconverter/data/Greebles/Family1/m1_11.3ds";
    		newmodel = loadModel(modelpath);
    		
    		newmodel.setLocalScale(.1f);
    		newmodel.setModelBound(new BoundingSphere());
    		newmodel.updateModelBound();
            rootNode.attachChild(newmodel);
            
            
    		
    		modelpath = "alienconverter/data/Greebles/Family1/m1_12.3ds";
    		newmodel = loadModel(modelpath);
    		
    	
            
            
    		modelpath = "alienconverter/data/Greebles/Family1/m1_13.3ds";
    		newmodel = loadModel(modelpath);
    		
    		modelpath = "alienconverter/data/Greebles/Family1/m1_14.3ds";
    		newmodel = loadModel(modelpath);
    		
    		modelpath = "alienconverter/data/Greebles/Family1/m1_15.3ds";
    		newmodel = loadModel(modelpath);
    		
    		modelpath = "alienconverter/data/Greebles/Family1/m1_16.3ds";
    		newmodel = loadModel(modelpath);
    		
    		modelpath = "alienconverter/data/Greebles/Family1/m1_17.3ds";
    		newmodel = loadModel(modelpath);
    		
    		modelpath = "alienconverter/data/Greebles/Family1/m1_18.3ds";
    		newmodel = loadModel(modelpath);
    		
    		modelpath = "alienconverter/data/Greebles/Family1/m1_19.3ds";
    		newmodel = loadModel(modelpath);

    		modelpath = "alienconverter/data/Greebles/Family1/m1_110.3ds";
    		newmodel = loadModel(modelpath);
    		
    		
    }
    
    	
    /*
     *  This method opens a model in various format evaluating the extension
     *  In case in the same directory is already presents the same model in jbin format loads it
     *  Otherways load the model and save a jbin copy for the next time.
     *  
     *  Attention : in case the original model is changed you'll have to delete the jbin one the reload it. 
     */		
    public static Node loadModel (String modelFile){
    	Node			loadedModel	= null;
    	FormatConverter		formatConverter = null;		
    	ByteArrayOutputStream 	BO 		= new ByteArrayOutputStream();
    	String			modelFormat 	= modelFile.substring(modelFile.lastIndexOf(".") + 1, modelFile.length());
    	String			modelBinary	= modelFile.substring(0, modelFile.lastIndexOf(".") + 1) + "jbin";
    	URL			modelURL	= ModelLoader.class.getClassLoader().getResource(modelBinary);
    	

    	
    	//verify the presence of the jbin model
    	if (modelURL == null){
    		
    		modelURL		= ModelLoader.class.getClassLoader().getResource(modelFile);
    		
    		//evaluate the format
    		if (modelFormat.equals("3ds")){
    			formatConverter = new MaxToJme();
    		} else if (modelFormat.equals("md2")){
    			formatConverter = new Md2ToJme();
    		} else if (modelFormat.equals("md3")){
    			formatConverter = new Md3ToJme();
    		} else if (modelFormat.equals("ms3d")){
    			formatConverter = new MilkToJme();
    		} else if (modelFormat.equals("ase")){
    			formatConverter = new AseToJme();
    		} else if (modelFormat.equals("obj")){
    			formatConverter = new ObjToJme();
    		}
    		formatConverter.setProperty("mtllib", modelURL);
    		
    		try {
    			formatConverter.convert(modelURL.openStream(), BO);
    			loadedModel = (Node) BinaryImporter.getInstance().load(new ByteArrayInputStream(BO.toByteArray()));
    			
    			//save the jbin format
    			BinaryExporter.getInstance().save((Savable)loadedModel, new File(modelBinary));
    		} catch (IOException e) {				
    			e.printStackTrace();
    			return null;
    		}
    	}else{
    		try {
    			//load the jbin format
    			loadedModel = (Node) BinaryImporter.getInstance().load(modelURL.openStream());
    		} catch (IOException e) {
    			return null;
    		}
    	}
    	
    	return loadedModel;
    }
}
