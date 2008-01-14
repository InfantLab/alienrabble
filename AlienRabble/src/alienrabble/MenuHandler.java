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

package alienrabble;

import alienrabble.sort.AlienRabbleSort;

import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.system.PropertiesIO;
import com.jmex.game.state.GameState;
import com.jmex.game.state.GameStateManager;

/**
 * The input handler we use to navigate the menu. E.g. has an absolute mouse.
 * If the escape key is pressed the application will be ended using the static
 * exit method of TestGameStateSystem.
 * 
 * @author Per Thulin
 */
public class MenuHandler extends InputHandler {
	private static final long serialVersionUID = 1L;

    private MenuState inMenuState;
    private GameState inGrabGame;
    private GameState inSortGame;

    public MenuHandler( MenuState menuState, PropertiesIO properties ) {
        setKeyBindings();
        this.inMenuState = menuState;
        //initialize the game states
        inGrabGame = new AlienRabble("ingrabgame", properties);
	    inSortGame = new AlienRabbleSort("insortgame", properties);
	    inGrabGame.setActive(false);
	    inSortGame.setActive(false);
        GameStateManager.getInstance().attachChild(inGrabGame);
        GameStateManager.getInstance().attachChild(inSortGame);
   }

    private void setKeyBindings() {
    	KeyBindingManager keyboard = KeyBindingManager.getKeyBindingManager();

    	keyboard.set("exit", KeyInput.KEY_ESCAPE);
        addAction( new ExitAction(), "exit", false );

        keyboard.set("enter", KeyInput.KEY_RETURN);
        addAction( new EnterAction(), "enter", false );
        
        //a couple of short cut keys for debugging
        keyboard.set("grab", KeyInput.KEY_G);
        addAction( new GrabAction(), "grab", false );

        keyboard.set("sort", KeyInput.KEY_S);
        addAction( new SortAction(), "sort", false );

    }
    
    private class GrabAction extends InputAction {
        public void performAction( InputActionEvent evt ) {
            inGrabGame.setActive(true);		// Activate this state.  
            inSortGame.setActive(false);
            inMenuState.setActive(false);      		 
        }
    }

    private class SortAction extends InputAction {
        public void performAction( InputActionEvent evt ) {
            inGrabGame.setActive(false);
            inSortGame.setActive(true); 	// Activate this state.  
            inMenuState.setActive(false);       		 
         }
    }

    private class EnterAction extends InputAction {
        public void performAction( InputActionEvent evt ) {
        	if (inMenuState.menuStatus == MenuState.MENU_GRAB_INSTRUCTIONS){
                inGrabGame.setActive(true);		// Activate this state.  
                inSortGame.setActive(false);
                inMenuState.setActive(false);    
        	}else if (inMenuState.menuStatus == MenuState.MENU_SORT_INSTRUCTIONS) {
                inGrabGame.setActive(false);
                inSortGame.setActive(true); 	// Activate this state.  
                inMenuState.setActive(false);       
        	}
        }
    }
    
    private static class ExitAction extends InputAction {
        public void performAction( InputActionEvent evt ) {
        	AlienRabbleGameStateSystem.exit();
        }
    }
}