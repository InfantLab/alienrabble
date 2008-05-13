package alienrabble.grab;

import java.nio.FloatBuffer;

import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.Timer;
import com.jme.util.geom.BufferUtils;
import com.jme.scene.state.LightState;
 
public class TimeGauge {
	private static final long serialVersionUID = 1L;
    private static final float MAXIMUM = 100f;

 	private Timer timer;
    
    private int textureWidth;
    private int textureHeight;
    
    private Node hudNode;
    
    private Quad gauge;
    private Quad hudQuad;
  
    private int minimum = 0, maximum = 100;
    private float widthScale = 0, heightScale = 0;
    private int xpos = 0, ypos = 0;
 
    private int width = 130, height = 14;
    
    //store the offsets of the coloured bars in the texture file
    private final int redoffset = 20;
    private final int yellowoffset = 36;
    private final int greenoffset = 52;
    
    //store the percentages at which gauge changes from green to yellow to red
    private float yellowthreshold = 0.50f; 
    private float redthreshold = 0.25f;
    
    private boolean isVertical = false;
    private float onScreenWidth, onScreenHeight;
    
    

    public TimeGauge(DisplaySystem display, String name){

		timer = Timer.getTimer();
        hudNode = new Node("hudNode");
  
        hudNode.getLocalTranslation().x = xpos;
        hudNode.getLocalTranslation().y = ypos;
 
        LightState ls = display.getRenderer().createLightState();
        ls.setEnabled(false);
        hudNode.setRenderState(ls);
        hudNode.updateRenderState();
        hudNode.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        
  
        TextureState ts = display.getRenderer().createTextureState();
        //TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        ts.setTexture(TextureManager.loadTexture(getClass().getClassLoader()
                .getResource("alienrabble/data/texture/timegauge.png"), Texture.MM_LINEAR,
                Texture.FM_LINEAR, 0.0f, true));
        textureWidth = ts.getTexture().getImage().getWidth();
        textureHeight = ts.getTexture().getImage().getHeight();
        ts.setEnabled(true);
        hudNode.setRenderState(ts);
     
        AlphaState as = display.getRenderer().createAlphaState();
        as.setBlendEnabled(true);
        as.setSrcFunction(AlphaState.SB_SRC_ALPHA);
        as.setDstFunction(AlphaState.DB_ONE_MINUS_SRC_ALPHA);
        as.setTestEnabled(false);
        as.setEnabled(true);
        hudNode.setRenderState(as);
        
        setHudQuad();
 	}
	
    private void setHudQuad(){
    	
    	hudNode.detachAllChildren();
    	if (isVertical){
    		width = 14;
    		height = 130;
        	hudQuad = new Quad("hud", 14f, 130f);
            gauge = new Quad("gauge", 12f, 128f);
    	}else{
    		width = 130;
    		height = 14;
        	hudQuad = new Quad("hud", 130f, 14f);
            gauge = new Quad("gauge", 128f, 12f);
    	}

        hudNode.attachChild(hudQuad);
        hudNode.attachChild(gauge);
    	
        FloatBuffer texCoords = BufferUtils.createVector2Buffer(4);
    	if (isVertical){
    	    texCoords.put(getUForPixel(256-14)).put(getVForPixel(256-130));
            texCoords.put(getUForPixel(256-14)).put(getVForPixel(256));
    	    texCoords.put(getUForPixel(256)).put(getVForPixel(256));
            texCoords.put(getUForPixel(256)).put(getVForPixel(256-130));
        }else{
            texCoords.put(getUForPixel(0)).put(getVForPixel(0));
            texCoords.put(getUForPixel(0)).put(getVForPixel(14));
            texCoords.put(getUForPixel(130)).put(getVForPixel(14));
            texCoords.put(getUForPixel(130)).put(getVForPixel(0));
    	}
        hudQuad.setTextureBuffer(0, texCoords);	
    }
    
    public void setIsVertical(boolean isVertical){
    	this.isVertical = isVertical;
    	setHudQuad();
    }
    
    public void setGauge(float value) {
    	  
//        value %= MAXIMUM;

        float range = maximum - minimum;
        float adjustedRange = value - minimum;
        float percent = adjustedRange / range;
        float newX = (128 * percent);

        FloatBuffer texCoords = BufferUtils.createVector2Buffer(4);
//        float relCoord = 0.5f - (value / MAXIMUM) * 0.5f;
        
        int colouroffset = getColourOffset(percent);

        if (isVertical){
        	texCoords.put(getUForPixel(256 - 12 - colouroffset)).put(getVForPixel(newX));
            texCoords.put(getUForPixel(256 -12 - colouroffset)).put(getVForPixel(128 + newX));
            texCoords.put(getUForPixel(256 - colouroffset)).put(getVForPixel(128 + newX));
            texCoords.put(getUForPixel(256 - colouroffset)).put(getVForPixel(newX));       	
        }else{
            texCoords.put(getUForPixel(128 - newX)).put(getVForPixel(colouroffset));
            texCoords.put(getUForPixel(128 - newX)).put(getVForPixel(colouroffset + 12));
            texCoords.put(getUForPixel(256 - newX)).put(getVForPixel(colouroffset + 12));
            texCoords.put(getUForPixel(256 - newX)).put(getVForPixel(colouroffset));
        }
        gauge.setTextureBuffer(0, texCoords);
   }
    
    private int getColourOffset(float gaugePercent){
    	if (gaugePercent > yellowthreshold){
    		return greenoffset;
    	}else if (gaugePercent > redthreshold){
    		return yellowoffset;
    	}else {
    		return redoffset;
    	}
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
  
     public Node getNode() {
         return hudNode;
     }
  
     public void setPosition(float xpos, float ypos) {
         hudNode.getLocalTranslation().x = xpos;
         hudNode.getLocalTranslation().y = ypos;
     }
      
     public void setMinimum(int minimum) {
         this.minimum = minimum;
     }
  
     public void setMaximum(int maximum) {
         this.maximum = maximum;
     }
  
     public void setScale(float scalewidth,float scaleheight) {
         widthScale = scalewidth;
         heightScale = scaleheight;
         hudNode.setLocalScale(new Vector3f(widthScale, heightScale, 1));
     }
 
  
     public int getMaximum() {
         return maximum;
     }
  
     public int getMinimum() {
         return minimum;
     }
 
}
