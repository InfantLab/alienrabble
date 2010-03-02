package alienrabble.sort.actions;

import alienrabble.sort.PackingCases;

import com.jme.input.action.InputActionEvent;
import com.jme.input.action.KeyInputAction;

/**
 * PackingCasesAction defines the action that occurs when the key is pressed to 
 * add or remove cases. 
 * @author Caspar Addyman
 *
 */
public class PackingCasesAction extends KeyInputAction {
    public static final int ADDCASE = 0;
    public static final int REMOVECASE = 1;
    //the node to manipulate
    private PackingCases cases;
    //whether we add or remove a case
    private int direction;

    /**
     * The set of cases to modify  is supplied during construction.
     * @param node the set of cases to increase or decrease.
     * @param direction Constant either ADDCASE or REMOVECASE
     */
    public PackingCasesAction(PackingCases node, int direction) {
        this.cases = node;
        this.direction = direction;
    }

    /**
     * the action calls the vehicle's accelerate or brake command which adjusts its velocity.
     */
    public void performAction(InputActionEvent evt) {
        if(direction == ADDCASE) {
        	cases.addCase();
        } else if(direction == REMOVECASE){
        	cases.removeCase();
        }
    }
}