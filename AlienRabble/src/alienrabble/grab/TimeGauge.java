package alienrabble.grab;

import java.nio.FloatBuffer;
import java.util.Random;

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

public class TimeGauge extends Node {
	private static final long serialVersionUID = 1L;
    private static final float MAXIMUM = 100f;

 	private Timer timer;
    private Quad gauge;
    
    private int textureWidth;
    private int textureHeight;

	
	public TimeGauge(String name){
		super(name);
		timer = Timer.getTimer();
        Quad hudQuad = new Quad("hud", 34f, 10f);
        gauge = new Quad("gauge", 32f, 8f);
        this.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        
        this.setLocalTranslation(new Vector3f(DisplaySystem.getDisplaySystem().getWidth()/2,DisplaySystem.getDisplaySystem().getHeight()/2,0));        
        
        TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        ts.setTexture(TextureManager.loadTexture(getClass().getClassLoader()
                .getResource("alienrabble/data/textures/hudtutorial3.png"), Texture.MM_LINEAR,
                Texture.FM_LINEAR, 1.0f, true));
        textureWidth = ts.getTexture().getImage().getWidth();
        textureHeight = ts.getTexture().getImage().getHeight();
        ts.setEnabled(true);
 
        FloatBuffer texCoords = BufferUtils.createVector2Buffer(4);
        texCoords.put(getUForPixel(0)).put(getVForPixel(0));
        texCoords.put(getUForPixel(0)).put(getVForPixel(10));
        texCoords.put(getUForPixel(34)).put(getVForPixel(10));
        texCoords.put(getUForPixel(34)).put(getVForPixel(0));
        hudQuad.setTextureBuffer(0, texCoords);
             
        AlphaState as = DisplaySystem.getDisplaySystem().getRenderer().createAlphaState();
        as.setBlendEnabled(true);
  
        as.setSrcFunction(AlphaState.SB_SRC_ALPHA);
        as.setDstFunction(AlphaState.DB_ONE_MINUS_SRC_ALPHA);
        as.setTestEnabled(false);
        as.setEnabled(true);   
	}
	
    protected void simpleUpdate() {
        /* recalculate rotation for the cylinder */
        if (timer.getTimePerFrame() < 1) {

        }

//        Random ran = new Random();
//        ran.nextFloat();
//        setGauge();
   }

    public void setGauge(float value) {
        value %= MAXIMUM;
        FloatBuffer texCoords = BufferUtils.createVector2Buffer(4);
        float relCoord = 0.5f - (value / MAXIMUM) * 0.5f;
        texCoords.put(relCoord).put(getVForPixel(56));
        texCoords.put(relCoord).put(getVForPixel(63));
        texCoords.put(relCoord + 0.5f).put(getVForPixel(63));
        texCoords.put(relCoord + 0.5f).put(getVForPixel(56));     
        gauge.setTextureBuffer(0, texCoords);
   }
    
    private float getUForPixel(long xPixel) {
        return (float)xPixel / textureWidth;
     }
     
     private float getVForPixel(long yPixel) {
        return 1f - (float)yPixel / textureHeight;
     }

}
