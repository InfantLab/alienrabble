package alienconverter;

import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.math.Vector3f;

public class AlienConverterHandler extends InputHandler {

	private AlienConverter ac;
	
	public AlienConverterHandler(AlienConverter alienconverter){
		ac = alienconverter;
		setKeyBindings();
	}
	
    private void setKeyBindings() {
    	KeyBindingManager keyboard = KeyBindingManager.getKeyBindingManager();

    	keyboard.set("spinfwdx", KeyInput.KEY_X);
        addAction( new AlienConverterAction(ac, Vector3f.UNIT_X,1,0f), "spinfwdx", false );

        keyboard.set("spinfwdy", KeyInput.KEY_Y);
        addAction( new AlienConverterAction(ac, Vector3f.UNIT_Y,1,0f), "spinfwdy", false );
        
        keyboard.set("spinfwdz", KeyInput.KEY_Z);
        addAction( new  AlienConverterAction(ac, Vector3f.UNIT_Z,1,0f), "spinfwdz", false );

        keyboard.set("shift", KeyInput.KEY_LSHIFT);
        addAction( new AlienConverterAction(ac, Vector3f.UNIT_X,-1,0f), "shift", false );

        keyboard.set("decr", KeyInput.KEY_LBRACKET);
        addAction( new AlienConverterAction(ac, Vector3f.UNIT_X,-1,-1f), "decr", false );

        keyboard.set("incr", KeyInput.KEY_RBRACKET);
        addAction( new AlienConverterAction(ac, Vector3f.UNIT_X,-1,-1f), "incr", false );

    }
}
