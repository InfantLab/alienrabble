package alienrabble.grab;

import java.awt.Color;
import java.nio.FloatBuffer;

import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.TexCoords;
import com.jme.scene.Text;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
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
    private Text alienPoints;
  
    private float height = 154, width = 154;
    private float widthScale = 1, heightScale = 1;
    
    private boolean isSmiley = true;
    private boolean isRight = true;
    private int showPoints = 0; //0 = false, 1=true, 2=possibly something else not yet decided
    private int numPoints = 10;
    
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
                .getResource("alienrabble/data/texture/rightwrong.png"), Texture.MinificationFilter.Trilinear,
                Texture.MagnificationFilter.Bilinear, 1.0f, true));
        textureWidth = ts.getTexture().getImage().getWidth();
        textureHeight = ts.getTexture().getImage().getHeight();
        ts.setEnabled(true);
        this.setRenderState(ts);
     
        BlendState as = display.getRenderer().createBlendState();
        as.setBlendEnabled(true);
        as.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        as.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        as.setTestEnabled(false);
        as.setEnabled(true);
        this.setRenderState(as);
        
        // Render the Gui node in the Ortho Queue
        this.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        // don't cull the gui away
        this.setCullHint(Spatial.CullHint.Never);
        // gui needs no lighting
        this.setLightCombineMode(Spatial.LightCombineMode.Off);
        // update the render states (especially the texture state of the ProgressBar!)

        alienPoints = Text.createDefaultTextLabel("alienPoints", "");
        alienPoints.setTextColor(ColorRGBA.blue);
        alienPoints.setLocalScale(3f);
        alienPoints.setLocalTranslation(10, 20, 0);
        this.attachChild(alienPoints);
        
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
		this.attachChild(alienPoints);
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
        if (showPoints == 1){
        	alienPoints.print(Integer.toString(numPoints));
        }
		if (timeTilBlank < 0 ||  (timer.getTimeInSeconds() -timerStarted) < timeTilBlank){
        	//do nothing
			 if (showPoints == 1){
				 alienPoints.setTextColor(ColorRGBA.randomColor());
			 }
        }else{
        	//override the previous choice and show blank icon instead 
        	v = 308;
        	u = 308;
        	timeTilBlank = -1;
        	alienPoints.print("");
        }
        
		
		FloatBuffer fbuffer = BufferUtils.createVector2Buffer(4);
		fbuffer.put(getUForPixel(u)).put(getVForPixel(v));
		fbuffer.put(getUForPixel(u)).put(getVForPixel(v+height));
		fbuffer.put(getUForPixel(u+width)).put(getVForPixel(v+height));
		fbuffer.put(getUForPixel(u+width)).put(getVForPixel(v));

        rightwrongQuad.setTextureCoords(new TexCoords(fbuffer));	
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
     public void setShowPoints(int pointssetting, int points){
    	 showPoints = pointssetting;
    	 numPoints = points;
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
