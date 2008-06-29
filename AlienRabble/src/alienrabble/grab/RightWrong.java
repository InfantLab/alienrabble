package alienrabble.grab;

import java.nio.FloatBuffer;

import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.Timer;
import com.jme.util.geom.BufferUtils;
import com.jme.scene.state.LightState;
 
public class RightWrong extends Node {
	private static final long serialVersionUID = 1L;
    
    
    private int textureWidth;
    private int textureHeight;
    
    private Timer timer;
    private float timeTilBlank;
    private float timerStarted;
    private Quad rightwrongQuad;
  
    private float height = 154, width = 154;
    private float widthScale = 0, heightScale = 0;
    
    private boolean isSmiley = true;
    private boolean isRight = true;
    private float onScreenWidth, onScreenHeight;
    
    

    public RightWrong(DisplaySystem display, String name){
    	super(name);
	    
  
        this.getLocalTranslation().x = 200;
        this.getLocalTranslation().y = 200;
 
        LightState ls = display.getRenderer().createLightState();
        ls.setEnabled(false);
        this.setRenderState(ls);
        this.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        
  
        TextureState ts = display.getRenderer().createTextureState();
        ts.setTexture(TextureManager.loadTexture(getClass().getClassLoader()
                .getResource("alienrabble/data/texture/rightwrong.png"), Texture.MM_LINEAR,
                Texture.FM_LINEAR, 0.0f, true));
        textureWidth = ts.getTexture().getImage().getWidth();
        textureHeight = ts.getTexture().getImage().getHeight();
        ts.setEnabled(true);
        this.setRenderState(ts);
     
        AlphaState as = display.getRenderer().createAlphaState();
        as.setBlendEnabled(true);
        as.setSrcFunction(AlphaState.SB_SRC_ALPHA);
        as.setDstFunction(AlphaState.DB_ONE_MINUS_SRC_ALPHA);
        as.setTestEnabled(false);
        as.setEnabled(true);
        this.setRenderState(as);
        
        // Render the Gui node in the Ortho Queue
        this.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        // don't cull the gui away
        this.setCullMode(Spatial.CULL_NEVER);
        // gui needs no lighting
        this.setLightCombineMode(LightState.OFF);
        // update the render states (especially the texture state of the ProgressBar!)
  
        timer = Timer.getTimer();
        timeTilBlank = -1;
        setrightwrongQuad();
        this.updateRenderState();
 	}
	
    public void updateRenderState(){
        if (timeTilBlank < 0 || (timer.getTimeInSeconds()- timerStarted) < timeTilBlank ){
        	//do nothing
        }else{
        	changeRightWrongQuad();
        }
    	super.updateRenderState();
    }
    
    private void setrightwrongQuad(){
    	
    	this.detachAllChildren();
    	rightwrongQuad = new Quad("rightwrongquad", width, height);
    	changeRightWrongQuad();

		this.attachChild(rightwrongQuad);	
    }
 
    private void changeRightWrongQuad(){
    	
    	float u = 0, v = 0;
		if (isSmiley){
			v = 0;
    	}else{
    		v = 154;
    	}
		if (isRight){
			u = 0;
		}else{
			u = 154;
		}    	
        if (timeTilBlank < 0 ||  (timer.getTimeInSeconds() -timerStarted) < timeTilBlank){
        	//do nothing
        }else{
        	//override the previous choice and show blank icon instead 
        	v = 308;
        	u = 308;
        	timeTilBlank = -1;
        }
		
		FloatBuffer texCoords = BufferUtils.createVector2Buffer(4);
	    texCoords.put(getUForPixel(u)).put(getVForPixel(v));
        texCoords.put(getUForPixel(u)).put(getVForPixel(v+width));
	    texCoords.put(getUForPixel(u+height)).put(getVForPixel(v+width));
        texCoords.put(getUForPixel(u+height)).put(getVForPixel(v));
        rightwrongQuad.setTextureBuffer(0, texCoords);	
    }
 
    
    private float getUForPixel(float xPixel) {
        return xPixel / textureWidth;
     }
     
     private float getVForPixel(float yPixel) {
        return 1f - yPixel / textureHeight;
     }
      
     public float getWidth() {
         return width * widthScale;
     }
  
     public float getHeight() {
         return height * heightScale;
     }
     
     public void setWidthHeight(float onScreenWidth,float onScreenHeight) {
    	 this.onScreenWidth =  onScreenWidth;
    	 this.onScreenHeight  = onScreenHeight;
    	 setScale(onScreenWidth / width, onScreenHeight / height);
    }
  
  
     public void setIsSmiley(boolean smiley){
    	 isSmiley = smiley;
    	 changeRightWrongQuad();
     }
     public void setIsRight(boolean right){
    	 isRight = right;
    	 changeRightWrongQuad();
     }     
     
     public void setPosition(float xpos, float ypos) {
    	 this.getLocalTranslation().x = xpos;
    	 this.getLocalTranslation().y = ypos;
     }
     
     /***
      * A function that will clear this icon after the specified 
      * length of time, if secs < 0 then icon remains indefinitely
      * @param secs - time till blank can be fractions of sec
      */
     public void setBlankTime(float secs){
    	 timeTilBlank = secs;
    	 timerStarted = timer.getTimeInSeconds();
     }
     
     public void setScale(float scalewidth,float scaleheight) {
         widthScale = scalewidth;
         heightScale = scaleheight;
         this.setLocalScale(new Vector3f(widthScale, heightScale, 1));
     }
}
