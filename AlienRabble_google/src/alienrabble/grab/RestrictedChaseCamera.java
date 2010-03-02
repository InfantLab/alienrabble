package alienrabble.grab;

import java.util.HashMap;

import com.jme.input.ChaseCamera;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.scene.Spatial;

public class RestrictedChaseCamera extends ChaseCamera {

	public RestrictedChaseCamera(Camera cam, Spatial target) {
		super(cam, target);
	}

	public RestrictedChaseCamera(Camera cam, Spatial target, HashMap props) {
		super(cam, target, props);
	}
	
	public void update(float time, Vehicle player){
        Vector3f camPos = cam.getLocation();
        
        if (player.getVelocity() < 0 ){
	        Vector3f tempVa = new Vector3f();
	        camPos = camPos.addLocal(player.getLocalRotation().getRotationColumn(2, tempVa)
	                .multLocal(player.getVelocity() * time));
        }
        if (!Vector3f.isValidVector(camPos) || !Vector3f.isValidVector(targetPos))
            return;
 
        //enforceBehindAngle(camPos, targetPos);
        //enforceNoInversion(camPos, targetPos);
        super.update(time);
	}
	
	/*
	 * this function is to prevent the camera from flipping back over our heads
	 * when we start moving backwards
	 * 
	 */
	protected void enforceNoInversion(Vector3f camPos, Vector3f targetPos){
    	
    	//1. make a vector from camPos to target Pos
    	Vector3f directionVec = camPos.subtract(targetPos);
    	
    	float vecMagnitude = directionVec.length();
    	directionVec = directionVec.normalize();
    	//System.out.println("directionVec: " + directionVec);
    	
    	// 2. make a vector representing the direction the ship is facing
    	Vector3f horizontalDirectionVec = new Vector3f(directionVec.x,directionVec.y, 0);
    	horizontalDirectionVec = horizontalDirectionVec.normalize();
    	
    	// find the angle between the camera look and horizontal
    	// in direction of look
    	Float angletoHorizon = horizontalDirectionVec.angleBetween(directionVec) * FastMath.RAD_TO_DEG;
    	//System.out.println("angle between: " + angleBetween);
    	
    	float angleOffset = -60f; // the max angle (from horizontal) that the camera is allowed to move
    	
    	if( angletoHorizon < angleOffset){
    		//let's do this in a very simple minded way!
    	
    	    Vector3f newRotVec = directionVec;
    	    newRotVec.z += 0.1f;
    	    newRotVec = newRotVec.normalize();
         
    	    // set the new camera position based on the new rotation vector
    	    Vector3f newCamPos = newRotVec.mult(vecMagnitude).add(targetPos);
    		cam.setLocation(newCamPos);
    		
  		
    		
    	}
    }


	protected void enforceBehindAngle(Vector3f camPos, Vector3f targetPos){
	    	
	    	//1. make a vector from camPos to target Pos
	    	Vector3f directionVec = camPos.subtract(targetPos);
	    	
	    	float vecMagnitude = directionVec.length();
	    	directionVec = directionVec.normalize();
	    	//System.out.println("directionVec: " + directionVec);
	    	
	    	// 2. make a vector representing the direction the ship is facing
	    	Vector3f targetRot = this.target.getWorldRotation().getRotationColumn(0);
	    	targetRot = targetRot.normalize();
	    	
	    	// find the angle between the camera look and ship facing vectors
	    	// will be ~90 when straight behind
	    	Float angleBetween = targetRot.angleBetween(directionVec) * FastMath.RAD_TO_DEG;
	    	//System.out.println("angle between: " + angleBetween);
	    	
	    	float angleOffset = 15f; // the max angle (from 90 degrees) that the camera is allowed to move
	    	
	    	// is the angle less than 75 or more than 105?
	    	if(angleBetween < 90f - angleOffset){
	    		
	    		// get the angle from the target we want to set it at, in radians
	    		Float targetAngle = 90f - angleOffset;
	    		targetAngle *= FastMath.DEG_TO_RAD;
	    		
	    		// reverse the equation for a normalized direction vector
	    		// directionVecNormalized = (camPos - targetPos) / directionVecMagnitude
	    		// so
	    		// newcamPos = newdirectionVec * directionVecMagnitude + targetPos
	    		float sina = FastMath.sin(targetAngle);
	    		float cosa = FastMath.cos(targetAngle);
	    		float x2 = targetRot.x * cosa + targetRot.z * sina; 
	    		float z2 = targetRot.z * cosa - targetRot.x * sina;
	    	    Vector3f newRotVec = new Vector3f(x2, targetRot.y, z2);
	    	    newRotVec.y = directionVec.y;
	         
	    	    // set the new camera position based on the new rotation vector
	    	    Vector3f newCamPos = newRotVec.mult(vecMagnitude).add(targetPos);
	    		cam.setLocation(newCamPos);
	    		
	    		
	    		
	    	}
	    	else if (angleBetween > 90f + angleOffset){
	    		
	    		// get the angle from the target we want to set it at, in radians
	    		Float targetAngle = 90f + angleOffset;
	    		targetAngle *= FastMath.DEG_TO_RAD;
	    		
	    		// reverse the equation for a normalized direction vector
	    		// directionVecNormalized = (camPos - targetPos) / directionVecMagnitude
	    		// so
	    		// newcamPos = newdirectionVec * directionVecMagnitude + targetPos
	    		float sina = FastMath.sin(targetAngle);
	    		float cosa = FastMath.cos(targetAngle);
	    		float x2 = targetRot.x * cosa + targetRot.z * sina; 
	    		float z2 = targetRot.z * cosa - targetRot.x * sina;
	    	    Vector3f newRotVec = new Vector3f(x2, targetRot.y, z2);
	    	    newRotVec.y = directionVec.y;
	         
	    	    // set the new camera position based on the new rotation vector
	    	    Vector3f newCamPos = newRotVec.mult(vecMagnitude).add(targetPos);
	    		cam.setLocation(newCamPos);
	    	
	    	}  	
	    }

}
