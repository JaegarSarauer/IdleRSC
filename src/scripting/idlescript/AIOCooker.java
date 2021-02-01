package scripting.idlescript;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import orsc.ORSCharacter;

public class AIOCooker extends IdleScript {
	JFrame scriptFrame = null;
	boolean guiSetup = false;
	boolean scriptStarted = false;
	
	
    class FoodObject {
		String name;
		int rawId;
		int cookedId;
		int burntId;
		
		public FoodObject(String _name, int _rawId, int _cookedId, int _burntId) {
			name = _name;
			rawId = _rawId;
			cookedId = _cookedId;
			burntId = _burntId;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof FoodObject) {
				if(((FoodObject)o).name.equals(this.name)) {
					return true;
				}
			}
		
			return false;
		}
	}
    
    FoodObject target = null;
    boolean dropBurnt = true;
    boolean gauntlets = true;
    
	ArrayList<FoodObject> objects = new ArrayList<FoodObject>() {{
		add(new FoodObject("Chicken", 133, 132, 134)); //raw, cooked, burnt
		add(new FoodObject("Shrimp", 349, 350, 353));
		add(new FoodObject("Anchovies", 351, 351, 353));
		add(new FoodObject("Sardine", 351, 355, 360));
		add(new FoodObject("Salmon", 356, 357, 360));
		add(new FoodObject("Trout", 358, 359, 360));
		add(new FoodObject("Herring", 361, 362, 365));
		add(new FoodObject("Pike", 363, 364, 365));
		add(new FoodObject("Cod", 550, 551, 360)); //pointed
		add(new FoodObject("Mackerel", 552, 553, 365)); //not pointed		
		add(new FoodObject("Tuna", 366, 367, 368));
		add(new FoodObject("Lobster", 372, 373, 374));
		add(new FoodObject("Swordfish", 369, 370, 371));
		add(new FoodObject("Bass", 554, 555, 368));
		add(new FoodObject("Shark", 545, 546, 547));
		add(new FoodObject("Sea Turtle", 1192, 1193, 1248));
		add(new FoodObject("Manta Ray", 1190, 1191, 1247));
	}};
	
	public void start(String parameters[]) {
		if(!guiSetup) {
    		setupGUI();
    		guiSetup = true;
    	}
    	
    	if(scriptStarted) {
    		scriptStart();
    	}
	}
	
	public void scriptStart() {
		while(controller.isRunning()) {
			
			if(controller.getInventoryItemCount(target.rawId) == 0) { 
				bank();
			} else {
				cook();
			}
			
			controller.sleep(250);
			
		}
	}
	
	public void bank() {
		
		controller.walkTo(439, 497);
		openDoor();
		
		while(controller.isInBank() == false) {
			ORSCharacter npc = controller.getNearestNpcById(95, false);
			
			controller.talkToNpc(npc.serverIndex);
			
			while(controller.isInOptionMenu() == false) controller.sleep(100);
			
			controller.optionAnswer(0);
			
			controller.sleep(5000);
		}
		
		
		
		if(controller.getInventoryItemCount(target.rawId) == 0) {
			controller.withdrawItem(target.rawId, 30);
			controller.sleep(250);
		}
		
		if(controller.getInventoryItemCount(target.cookedId) > 0) {
			controller.depositItem(target.cookedId);
			controller.sleep(250);
		}
		
		if(controller.getInventoryItemCount(target.burntId) > 0) {
			controller.depositItem(target.burntId);
			controller.sleep(250);
		}	
		
		controller.walkTo(439, 496);
		openDoor();
	}
	
	public void cook() {
		
		controller.walkTo(435, 486);
		openCookDoor();
		
		if(this.gauntlets == true) {
			
			if(controller.getInventoryItemCount(700) < 1) {
				controller.displayMessage("@red@Please withdraw gauntlets. Stopping script.");
				controller.stop();
				return;
			}
			
			if(!controller.isEquipped(controller.getInventoryItemIdSlot(700))) {
				controller.equipItem(controller.getInventoryItemIdSlot(700));
				controller.sleep(618);
			}
		}
		
		while(controller.getInventoryItemCount(target.rawId) > 0) {
			
			if(controller.isBatching() == false)
				controller.useItemOnObject(432, 480, target.rawId);
			
			
			
			if(this.dropBurnt) { 
				if(controller.getInventoryItemCount(target.burntId) > 0)
					controller.dropItem(controller.getInventoryItemIdSlot(target.burntId));
			}
			
			controller.sleep(250);
		}
		
		
		controller.walkTo(435, 485);
		openCookDoor();
	}
	
	public void openDoor() {
		while(controller.getObjectAtCoord(439, 497) == 64) {
			controller.objectAt(439, 497, 0, 64);
			controller.sleep(100);
		}
	}
	
	public void openCookDoor() {
		while(!controller.isDoorOpen(435, 486)) {
			controller.openDoor(435, 486);
		}
	}

	
	public static void centerWindow(Window frame) {
	    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
	    int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
	    frame.setLocation(x, y);
	}
	
    public void setupGUI() { 	
    	JLabel headerLabel = new JLabel("Start in Catherby!");
    	JComboBox<String> foodField = new JComboBox<String>();
    	JCheckBox dropBurntCheckbox = new JCheckBox("Drop Burnt?", true);
    	JCheckBox gauntletsCheckbox = new JCheckBox("Cooking Gauntlets?", true);
    	
    	
        JButton startScriptButton = new JButton("Start");
        
        for(FoodObject obj : objects) {
        	foodField.addItem(obj.name);
        }
        
        startScriptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            		
            		dropBurnt = dropBurntCheckbox.isSelected();
            		gauntlets = gauntletsCheckbox.isSelected();
            		target = objects.get(foodField.getSelectedIndex());
            		
	            	scriptFrame.setVisible(false);
	            	scriptFrame.dispose();
	            	scriptStarted = true;
	            	
	            	controller.displayMessage("@red@AIOCooker by Dvorak. Let's party like it's 2004!");
            	}
        });
        
        
        
    	scriptFrame = new JFrame("Script Options");
    	
    	scriptFrame.setLayout(new GridLayout(0,1));
    	scriptFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	scriptFrame.add(headerLabel);
    	scriptFrame.add(foodField);
    	scriptFrame.add(dropBurntCheckbox);
    	scriptFrame.add(gauntletsCheckbox);
    	scriptFrame.add(startScriptButton);
    		
    	centerWindow(scriptFrame);
    	scriptFrame.setVisible(true);
    	scriptFrame.pack();
    }
    	
}
