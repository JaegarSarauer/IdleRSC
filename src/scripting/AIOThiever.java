package scripting;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import orsc.ORSCharacter;

public class AIOThiever extends IdleScript {
	JFrame scriptFrame = null;
	boolean guiSetup = false;
	boolean scriptStarted = false;
	
	
    class ThievingObject {
		String name;
		int id;
		boolean isNpc;
		boolean isObject;
		boolean isImplemented;
		
		public ThievingObject(String _name, int _id, boolean _isNpc, boolean _isObject, boolean _isImplemented) {
			name = _name;
			id = _id;
			isNpc = _isNpc;
			isObject = _isObject;
			isImplemented = _isImplemented;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof ThievingObject) {
				if(((ThievingObject)o).name.equals(this.name)) {
					return true;
				}
			}
		
			return false;
		}
	}
    
    ThievingObject target = null;
    int fightMode = 0;
    int eatingHealth = 0;
    
	ArrayList<ThievingObject> objects = new ArrayList<ThievingObject>() {{
		add(new ThievingObject("Man", 11, true, false, true));
		add(new ThievingObject("Farmer", 63, true, false, true));
		add(new ThievingObject("Warrior", 86, true, false, true));
		add(new ThievingObject("Workman", 722, true, false, true));
		add(new ThievingObject("Rogue", 342, true, false, true));
		add(new ThievingObject("Guard", 321, true, false, true));
		add(new ThievingObject("Knight", 322, true, false, true));
		add(new ThievingObject("Watchman", 574, true, false, true));
		add(new ThievingObject("Paladin", 323, true, false, true));
		add(new ThievingObject("Gnome", 592, true, false, true));
		add(new ThievingObject("Hero", 324, true, false, true));
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
			
			if(controller.getFightMode() != this.fightMode)
				controller.setFightMode(this.fightMode);
			
			
			
			if(!controller.isInCombat()) { 
				ORSCharacter npc = controller.getNearestNpcById(target.id);
				controller.npcCommand1(npc.serverIndex);
			} else {
				controller.walkTo(controller.currentX(), controller.currentZ(), 0, true);
				
				controller.sleep(618);
				
	    		if(controller.getCurrentStat(controller.getStatId("Hits")) <= eatingHealth) {
	    			controller.displayMessage("@red@AIOThiever: Eating food");
	    			controller.walkTo(controller.currentX(), controller.currentZ(), 0, true);
	    			
	    			boolean ate = false;
	    			
	    			for(int id : controller.getFoodIds()) {
	    				if(controller.getInventoryItemCount(id) > 0) {
	    					controller.itemCommand(id);
	    					ate = true;
	    					break;
	    				}
	    			}
	    			
	    			if(!ate) {
	    				controller.displayMessage("@red@AIOThiever: We ran out of food! Logging out.");
	    				controller.setAutoLogin(false);
	    				controller.logout();
	    			}
	    			
	    			continue;
	    		}
				
			}
			
			controller.sleep(250);
			
		}
	}

	
	public static void centerWindow(Window frame) {
	    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
	    int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
	    frame.setLocation(x, y);
	}
	
    public void setupGUI() { 	
    	JLabel fightModeLabel = new JLabel("Fight Mode:");
    	JComboBox<String> fightModeField = new JComboBox<String>(new String[] {"Controlled", "Aggressive", "Accurate", "Defensive"});
    	JLabel eatAtHpLabel = new JLabel("Eat at HP: (food is automatically detected)");
    	JTextField eatAtHpField = new JTextField(String.valueOf(controller.getCurrentStat(controller.getStatId("Hits")) / 2));
    	JComboBox<String> targetField = new JComboBox<String>();  
        JButton startScriptButton = new JButton("Start");
        
        for(ThievingObject obj : objects) {
        	targetField.addItem(obj.name);
        }
        
        startScriptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            		fightMode = fightModeField.getSelectedIndex();
            		eatingHealth = Integer.parseInt(eatAtHpField.getText());
            		target = objects.get(targetField.getSelectedIndex());
	            	scriptFrame.setVisible(false);
	            	scriptFrame.dispose();
	            	scriptStarted = true;
	            	
	            	controller.displayMessage("@red@AIOThiever by Dvorak. Let's party like it's 2004!");
            	}
        });
        
        
        
    	scriptFrame = new JFrame("Script Options");
    	
    	scriptFrame.setLayout(new GridLayout(0,1));
    	scriptFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	scriptFrame.add(fightModeLabel);
    	scriptFrame.add(fightModeField);
    	scriptFrame.add(eatAtHpLabel);
    	scriptFrame.add(eatAtHpField);
    	scriptFrame.add(targetField);
    	scriptFrame.add(startScriptButton);
    		
    	centerWindow(scriptFrame);
    	scriptFrame.setVisible(true);
    	scriptFrame.pack();
    }
    	
}
