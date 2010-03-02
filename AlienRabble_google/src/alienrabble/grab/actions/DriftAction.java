package alienrabble.grab.actions;

import alienrabble.grab.Vehicle;

import com.jme.input.action.InputActionEvent;
import com.jme.input.action.KeyInputAction;

/**
 * DriftAction defines the action that occurs by default each frame. 
 * It obtains the velocity of the vehicle and translates the vehicle by this value.
 * @author Mark Powell
 *
 */
public class DriftAction extends KeyInputAction {
    //the node to manipulate
    private Vehicle vehicle;
    
    /**
     * The vehicle to drift is supplied during construction.
     * @param vehicle the vehicle to drift.
     */
    public DriftAction(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    /**
     * the action calls the vehicle's drift command which adjusts its velocity.
     */
    public void performAction(InputActionEvent evt) {
        vehicle.drift(evt.getTime());
    }
}