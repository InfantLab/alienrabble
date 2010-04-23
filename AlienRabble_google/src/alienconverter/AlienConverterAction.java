package alienconverter;

import com.jme.input.action.InputActionEvent;
import com.jme.input.action.KeyInputAction;
import com.jme.math.Vector3f;

public class AlienConverterAction extends KeyInputAction{	
	private static final long serialVersionUID = 1L;


	private AlienConverter alienconverter;
	private Vector3f axis;
	private int direction;
	private float speedchange;
	
	public AlienConverterAction(AlienConverter ac, Vector3f actionType, int direction, float speedchange){
		alienconverter = ac;
		this.axis = actionType;
		this.direction = direction;
		this.speedchange = speedchange;
	}
	
	public void performAction(InputActionEvent evt) {	
		alienconverter.changeRotation(axis, direction, speedchange);
	}

}
