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

import org.fenggui.ComboBox;
import org.fenggui.TextEditor;
import org.fenggui.composites.Window;
import org.fenggui.event.ISelectionChangedListener;
import org.fenggui.event.SelectionChangedEvent;
import org.fenggui.layout.StaticLayout;

import com.jme.image.Texture;
import com.jme.input.AbsoluteMouse;
import com.jme.input.InputHandler;
import com.jme.input.Mouse;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Text;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.system.PropertiesIO;
import com.jme.util.TextureManager;
import com.jmex.game.state.CameraGameState;
import com.jmex.game.state.GameState;

/** 
 * @author Per Thulin
 */
public class MenuState extends CameraGameState {
	private static final long serialVersionUID = 1L;
	
	//the different possible menu states
	public static final int MENU_START = 0;
	public static final int MENU_GRAB_INSTRUCTIONS = 1;
	public static final int MENU_GRAB_FEEDBACK = 2;
	public static final int MENU_SORT_INSTRUCTIONS = 3;
	public static final int MENU_SORT_FEEDBACK = 4;
	public static final int MENU_FINISH = 5;

	//what type of menu should be displaying 
	public int menuStatus;
	
	/** The cursor node which holds the mouse gotten from input. */
	private Node cursor;
	
	/** Our display system. */
	private DisplaySystem display;
	
	private PropertiesIO properties;

    private Text text;
    private Text text2;
    private Text text3;

    private InputHandler input;
    private Mouse mouse;
    
    org.fenggui.Display disp; // FengGUI's display
	FengInputHandler fenginput;

    public MenuState(String name, PropertiesIO properties) {
        super(name);

        display = DisplaySystem.getDisplaySystem();
        this.properties =  properties;
        initInput();
        initCursor();
        initText();
        menuStatus = MENU_GRAB_INSTRUCTIONS;
        writeText();
        initGUI();
        
        rootNode.setLightCombineMode(LightState.OFF);
        rootNode.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        rootNode.updateRenderState();
        rootNode.updateGeometricState(0, true);
    }
	
	/**
	 * @see com.jmex.game.state.CameraGameState#onActivate()
	 */
	public void onActivate() {
		display.setTitle("Alien Rabble- Menu");
		super.onActivate();
		writeText();
	}
	
	/**
	 * Inits the input handler we will use for navigation of the menu.
	 */
	protected void initInput() {
		input = new MenuHandler( this, properties );

        DisplaySystem display = DisplaySystem.getDisplaySystem();
        mouse = new AbsoluteMouse("Mouse Input", display.getWidth(),
                display.getHeight());
        mouse.registerWithInputHandler( input );
	}
	
	/**
	 * Create our GUI.  FengGUI init code goes in here
	 *
	 */
	protected void initGUI()
	{
		// Grab a display using an LWJGL binding
		//	   (obviously, since jME uses LWJGL)
		disp = new org.fenggui.Display(new org.fenggui.render.lwjgl.LWJGLBinding());
 
		fenginput = new FengInputHandler(disp);
 
		//	 Create a dialog and set it to some location on the screen
		Window frame = new Window();
		disp.addWidget(frame);
		frame.setX(20);
		frame.setY(350);
		frame.setSize(200, 100);
		frame.setShrinkable(false);
		//frame.setExpandable(true);
		frame.setTitle("Pick a color");
		frame.getContentContainer().setLayoutManager(new StaticLayout());
 
		// Create a combobox with some random values in it
		//   we'll change these values to something more useful later on.
		ComboBox<String> list = new ComboBox<String>();
		frame.addWidget(list);
		list.setSize(150, list.getMinHeight());
		list.setShrinkable(false);
		list.setX(25);
		list.setY(25);
		list.addItem("White");
		list.addItem("Green");
		list.addItem("Blue");
		list.addItem("Red");
 
		list.addSelectionChangedListener(new CBListener());
 
		//try to add TextArea here but get OpenGLException
		TextEditor ta = new TextEditor(false);
		disp.addWidget(ta);
		ta.setText("Hallo Text");
		ta.setX(40);
		ta.setY(50);
		//ta.setSize(100, ta.getAppearance().getFont().get)
		ta.setSizeToMinSize();
 
		// Update the display with the newly added components
		disp.layout();
	}
	
	/**
	 * Creates a pretty cursor.
	 */
	private void initCursor() {		
		Texture texture =
	        TextureManager.loadTexture(
	    	        MenuState.class.getClassLoader().getResource(
	    	        "jmetest/data/cursor/cursor1.png"),
	    	        Texture.MM_LINEAR_LINEAR,
	    	        Texture.FM_LINEAR);
		
		TextureState ts = display.getRenderer().createTextureState();
		ts.setEnabled(true);
		ts.setTexture(texture);
		
		AlphaState alpha = display.getRenderer().createAlphaState();
		alpha.setBlendEnabled(true);
		alpha.setSrcFunction(AlphaState.SB_SRC_ALPHA);
		alpha.setDstFunction(AlphaState.DB_ONE);
		alpha.setTestEnabled(true);
		alpha.setTestFunction(AlphaState.TF_GREATER);
		alpha.setEnabled(true);
		
		mouse.setRenderState(ts);
        mouse.setRenderState(alpha);
        mouse.setLocalScale(new Vector3f(1, 1, 1));
		
		cursor = new Node("Cursor");
		cursor.attachChild( mouse );
		
		rootNode.attachChild(cursor);
	}
	
	/**
	 * Inits the button placed at the center of the screen.
	 */
	private void initText() {
        text = Text.createDefaultTextLabel( "instructions 1" );
        text.getLocalTranslation().set( 400, 600, 0 );
        rootNode.attachChild( text );

        text2 = Text.createDefaultTextLabel( "instructions 2" );
        text2.getLocalTranslation().set( 400, 400, 0 );
        rootNode.attachChild( text2 );
        
        text3 = Text.createDefaultTextLabel( "info" );
        text3.getLocalTranslation().set( 400, 200, 0 );
        rootNode.attachChild( text3 );
 	}
	 
	private void writeText(){
		if (menuStatus == MENU_START){
		}else if (menuStatus == MENU_GRAB_INSTRUCTIONS){
			text.print( "Use the arrow keys to navigate round the space." );
			text2.print( "Collect all the aliens as fast as you can." );
			text3.print( "press ENTER to begin" );
		}else if (menuStatus == MENU_GRAB_FEEDBACK){
			
		}else if (menuStatus == MENU_SORT_INSTRUCTIONS){
		    text.print( "Use mouse to select individual aliens." );
	        text2.print( "Then click on the box you want to place them in." );
	        text3.print( "press ENTER to begin" );
		}else if (menuStatus == MENU_SORT_FEEDBACK){
			
		}else if (menuStatus == MENU_FINISH){
		    text.print( "Thank you for your participation" );
	        text2.print( "" );
			text3.print( "press ENTER to finish" );
		}		
	}
	
	
		
	/**
	 * Updates input and button.
	 * 
	 * @param tpf The time since last frame.
	 * @see GameState#update(float)
	 */
	protected void stateUpdate(float tpf) {
		input.update(tpf);
		// Check if the button has been pressed.
		rootNode.updateGeometricState(tpf, true);
	}
	
	private class CBListener implements ISelectionChangedListener
	{
		public void selectionChanged(SelectionChangedEvent selectionChangedEvent)
		{
			if (!selectionChangedEvent.isSelected()) return;
			String value = selectionChangedEvent.getToggableWidget().getText();
			if ("White".equals(value)) text3.print("White");
			if ("Red".equals(value)) text3.print("Red");
			if ("Blue".equals(value)) text3.print("Blue");
			if ("Green".equals(value)) text3.print("Green");
		}
 
	}
	
}