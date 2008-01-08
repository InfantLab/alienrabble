		/*
 * Copyright (c) 2003-2006 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package alienrabble.sort;

import alienrabble.sort.actions.PackingCasesAction;

import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;

/**
 * Input Handler for the Flag Rush game. This controls a supplied spatial
 * allowing us to move it forward, backward and rotate it left and right.
 * @author Mark Powell
 *
 */
public class AlienRabbleSortHandler extends InputHandler {
	private static final long serialVersionUID = 1L;
	
    //the packing cases we are going to control
    private PackingCases packingcases;
    	
    public void update(float time) {
        if ( !isEnabled() ) return;

        super.update(time);
        packingcases.update(time);
    }
    
    /**
     * Supply the node to control and the api that will handle input creation.
     * @param vehicle the node we wish to move
     * @param api the library that will handle creation of the input.
     */
    public AlienRabbleSortHandler(PackingCases cases, String api) {
        this.packingcases = cases;
        setKeyBindings(api);
        setActions();

    }

    /**
     * creates the keyboard object, allowing us to obtain the values of a keyboard as keys are
     * pressed. It then sets the actions to be triggered based on if certain keys are pressed (WSAD).
     * @param api the library that will handle creation of the input.
     */
    private void setKeyBindings(String api) {
        KeyBindingManager keyboard = KeyBindingManager.getKeyBindingManager();

        keyboard.set("addCase", KeyInput.KEY_UP);
//        keyboard.set("addCase", KeyInput.KEY_RIGHT);
        keyboard.set("removeCase", KeyInput.KEY_DOWN);
 //       keyboard.set("removeCase", KeyInput.KEY_LEFT);
    }

    /**
     * assigns action classes to triggers. These actions handle adding and removing the cases
     * that aliens are sorted into and may handle other features too eventually. 
     */
    private void setActions() {
        PackingCasesAction addcase = new PackingCasesAction(packingcases, PackingCasesAction.ADDCASE);
        addAction(addcase, "addcase", false);
        PackingCasesAction removecase = new PackingCasesAction(packingcases, PackingCasesAction.REMOVECASE);
        addAction(removecase, "removecase", false);
    }
    
    
}
