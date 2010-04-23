package alienrabble.grab.actions;


import alienrabble.grab.Vehicle;
import com.jme.input.action.InputActionEvent;
import com.jme.input.action.KeyInputAction;
import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;

/**
 * VehicleRotateLeftAction turns the vehicle to the left (while 
 * traveling forward).
 * @author Mark Powell
 *
 */
public class VehicleRotateAction extends KeyInputAction {
	private static final long serialVersionUID = 1L;
    public static final int RIGHT = 0;
    public static final int LEFT = 1;
    public static final int FORWARD = 2;
    public static final int BACKWARD = 3;
    
    //temporary variables to handle rotation
    private static final Matrix3f incr = new Matrix3f();
    private static final Matrix3f tempMa = new Matrix3f();
    private static final Matrix3f tempMb = new Matrix3f();

    //we are using +Y as our up
    private Vector3f upAxis = new Vector3f(0,1,0);
    private Vector3f pitchAxis = new Vector3f(1,0,0);
    //the node to manipulate
    private Vehicle vehicle;
    private int direction;
    private int modifier = 1;
    
    /**
     * create a new action with the vehicle to turn.
     * @param vehicle the vehicle to turn
     */
    public VehicleRotateAction(Vehicle vehicle, int direction) {
        this.vehicle = vehicle;
        this.direction = direction;
    }

    /**
     * turn the vehicle by its turning speed. If the vehicle is traveling 
     * backwards, swap direction.
     */
    public void performAction(InputActionEvent evt) {
//        if(vehicle.getVelocity() > -FastMath.FLT_EPSILON && vehicle.getVelocity() < FastMath.FLT_EPSILON) {
//            return;
//        }
    	
    	Vector3f axis = new Vector3f();
    	axis = upAxis;
    	
    	//affect the direction
        if(direction == LEFT) {
            modifier = 1;
            axis = upAxis;
        } else if(direction == RIGHT) {
            modifier = -1;
            axis = upAxis;
        } else if(direction == FORWARD) {
            modifier = 1;
            axis = pitchAxis;
        } else if(direction == BACKWARD) {
            modifier = -1;
            axis = pitchAxis;
        }
        //we want to turn differently depending on which direction we are traveling in.
        if(vehicle.getVelocity() < 0) {
            incr.fromAngleNormalAxis(modifier * vehicle.getTurnSpeed() * evt.getTime(), axis);
        } else {
            incr.fromAngleNormalAxis(modifier * vehicle.getTurnSpeed() * evt.getTime(), axis);
        }
        vehicle.getLocalRotation().fromRotationMatrix(
                incr.mult(vehicle.getLocalRotation().toRotationMatrix(tempMa),
                        tempMb));
        vehicle.getLocalRotation().normalize();
        vehicle.setRotateOn(modifier);
    }
}