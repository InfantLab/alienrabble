/**
 * 
 */
package game;

import com.jme.app.SimpleGame;
import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Box;

/**
 * <code>GameMain</code>
 * @author Caspar Addyman
 * @version 0.01 
 */

public class GameMain extends SimpleGame {

	  public static void main(String[] args) {
		  	GameMain app = new GameMain();
		    app.setDialogBehaviour(ALWAYS_SHOW_PROPS_DIALOG);
		    app.start();
		  }

		  protected void simpleInitGame() {
		    display.setTitle("A Simple Test");
		    Box box = new Box("my box", new Vector3f(0, 0, 0), 2, 2, 2);
		    box.setModelBound(new BoundingSphere());
		    box.updateModelBound();
		    rootNode.attachChild(box);
		  }
}
