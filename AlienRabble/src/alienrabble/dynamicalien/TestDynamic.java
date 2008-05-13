package alienrabble.dynamicalien;

import com.jme.app.SimpleGame;
import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.input.FirstPersonHandler;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Capsule;
import com.jme.scene.state.CullState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;

public class TestDynamic extends SimpleGame {

	    private Quaternion rotQuat = new Quaternion();
	    private float angle = 0;
	    private Vector3f axis = new Vector3f(1, 1, 0).normalizeLocal();
//	    private Capsule t;
	    private DynamicExemplar myalien;

	    /**
	     * Entry point for the test,
	     * 
	     * @param args
	     */
	    public static void main(String[] args) {
	    	TestDynamic app = new TestDynamic();
	        app.setDialogBehaviour(ALWAYS_SHOW_PROPS_DIALOG);
	        app.start();
	    }

	    protected void simpleUpdate() {
	        if (timer.getTimePerFrame() < 1) {
	            angle = angle + (timer.getTimePerFrame() * 1);
	            if (angle > 360) {
	                angle = 0;
	            }
	        }

	        rotQuat.fromAngleNormalAxis(angle, axis);
	        myalien.setLocalRotation(rotQuat);
	    }

	    /**
	     * builds the trimesh.
	     * 
	     * @see com.jme.app.SimpleGame#initGame()
	     */
	    protected void simpleInitGame() {
	        display.setTitle("Cylinder Test");

	        myalien = new DynamicExemplar("onealien", display);
	        myalien.setUpExemplar();

	        lightState.setTwoSidedLighting(false);
	        
	        rootNode.attachChild(myalien);
	    	        
	        CullState cs = display.getRenderer().createCullState();
	        cs.setCullMode(CullState.CS_BACK);
	        rootNode.setRenderState(cs);
	        
	        input = new FirstPersonHandler(cam, 10f, 1f);
	        
	    }

}
