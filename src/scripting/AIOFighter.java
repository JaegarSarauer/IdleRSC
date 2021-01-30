package scripting;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import orsc.ORSCharacter;

/**
 * This is AIOFighter written for IdleRSC.
 * 
 * It is your standard melee/range/mage fighter script.
 * 
 * It has the following features:
 * 
 * 	* GUI
 *  * Multiple NPCs
 *  * Food eating -- logs out when out of food.
 *  * Looting
 *  * Bone burying (supports all bone types)
 *  * Maging (even when in melee combat)
 *  * Ranging (will switch to melee weapon if in combat. Can also pick up arrows.)
 *  * Anti-wander (will walk back if out of bounds)
 *  
 * 
 * @author Dvorak
 */

public class AIOFighter extends IdleScript {

	//config
	int fightMode = 2;
	int maxWander = 3;
	int eatingHealth = 5;
	boolean buryBones = true;
	
	boolean maging = true;
	int spellId = 0;	
	
	boolean ranging = true;
	int arrowId = -1; //leave -1 to not pickup arrows. 
	int switchId = 81; //weapon to switch to when in combat if ranging.
	
	
	
	int[] npcIds = {};
	int[] loot = {}; //feathers
	int[] bones = {20, 413, 604, 814};
	int[] bowIds = {188, 189, 648, 649, 650, 651, 652, 653, 654, 655, 656, 657, 59, 60};
	
	
	//do not modify these
	int currentAttackingNpc = -1;
	int[] lootTable = null;
	int[] startTile = {-1, -1};
	
	private JFrame scriptFrame;
	boolean guiSetup = false;
	boolean scriptStarted = false;
	
    public void start(String parameters[])
    {
    	if(!guiSetup) {
    		setupGUI();
    		guiSetup = true;
    	}
    	
    	if(scriptStarted) {
    		scriptStart();
    	}
    }
    
	public void npcMessageInterrupt(String message) {
		System.out.println(message);
		if(message.equals("I can't get a clear shot from here")) {
			System.out.println("Walking to npc");
			controller.walktoNPC(currentAttackingNpc, 1);
		}
	}
    
    public void scriptStart() {
    	lootTable = Arrays.copyOf(loot, loot.length);
    	if(buryBones == true) {
    		lootTable = Arrays.copyOf(lootTable, loot.length + bones.length);
    		for(int i = loot.length, k = 0; i < loot.length + bones.length; i++, k++) {
    			lootTable[i] = bones[k];
    		}
    	}
    	
    	startTile[0] = controller.currentX();
    	startTile[1] = controller.currentZ();
    	
    	while(controller.isRunning()) {
    		
    		//0th priority: walking back to starting zone if out of zone
    		//1st priority: setting fightmode
    		//2nd priority: eating
    		//3rd priority: bury any bones in inv
    		//4th priority: pickup loot
    		//5th priority: pickup bones
    		//6th priority: starting a fight via melee or ranging
    		//7th priority: maging
    		
    		controller.sleep(618); //wait 1 tick
    		
    		if(!isWithinWander(controller.currentX(), controller.currentZ())) {
    			controller.displayMessage("@red@AIOFighter: out of range! Walking back.");
    			controller.walkTo(startTile[0], startTile[1], 0, true);
    		}
    		
    		if(controller.getFightMode() != fightMode) {
    			controller.displayMessage("@red@AIOFighter: Changing fightmode");
    			controller.setFightMode(fightMode);
    		}
    		
    		if(controller.getCurrentStat(controller.getStatId("Hits")) <= eatingHealth) {
    			controller.displayMessage("@red@AIOFighter: Eating food");
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
    				controller.displayMessage("@red@AIOFighter: We ran out of food! Logging out.");
    				controller.setAutoLogin(false);
    				controller.logout();
    			}
    			
    			continue;
    		}
    		
    		if(buryBones())
    			continue;
    		
    		int[] lootCoord = controller.getNearestItemByIds(lootTable);
    		if(lootCoord != null) {
    			controller.displayMessage("@red@AIOFighter: Picking up loot");
    			controller.pickupItem(lootCoord[0], lootCoord[1], lootCoord[2], true, false);
    			continue;
    		}
    		
    		if(!controller.isInCombat() ) {
	    		ORSCharacter npc = controller.getNearestNpcByIds(npcIds);
	    		
	    		if(ranging) {
	    			
	    			int[] arrowCoord = controller.getNearestItemById(arrowId);
	    			if(arrowCoord != null) {
	    				controller.displayMessage("@red@AIOFighter: Picking up arrows");
	    				controller.pickupItem(arrowCoord[0], arrowCoord[1], arrowId, false, true);
	    				continue;
	    			}
		    		for(int id : bowIds) {
		    			if(controller.getInventoryItemCount(id) > 0) {
			    			if(!controller.isEquipped(controller.getInventoryItemIdSlot(id))) {
			    				controller.displayMessage("@red@AIOFighter: Equipping bow");
			    				controller.equipItem(controller.getInventoryItemIdSlot(id));
			    				controller.sleep(1000);
			    				break;
			    			}
		    			}
		    		}
	    		}
	    		
	    		//maybe wrap this in a 'while not in combat' loop?
	    		if(npc != null) {
	    			if(maging && !ranging) {
	    				currentAttackingNpc = npc.serverIndex;
	    				controller.castSpellOnNpc(npc.serverIndex, spellId);
	    			} else {
		    			controller.displayMessage("@red@AIOFighter: Attacking NPC");
		    			controller.attackNpc(npc.serverIndex);
		    			controller.sleep(1000);
	    			}
	    			
	    			continue;
	    		}
    		} else {
    			if(ranging == true) {
    				if(!controller.isEquipped(controller.getInventoryItemIdSlot(switchId))) {
    					controller.displayMessage("@red@AIOFighter: Switching to melee weapon");
    					controller.equipItem(controller.getInventoryItemIdSlot(switchId));
    				}
    			}
    			if(maging == true) {
    				controller.displayMessage("@red@AIOFighter: Maging...");
    				ORSCharacter victimNpc = controller.getNearestNpcByIds(npcIds);
    				controller.castSpellOnNpc(victimNpc.serverIndex, spellId);
    			}
    			
    		}
    		

    	}
    }
    
    public boolean buryBones() {
		for(int id : bones) {
			if(controller.getInventoryItemCount(id) > 0) {
				controller.displayMessage("@red@AIOFighter: Burying bones");
				controller.itemCommand(id);
				return true;
			}
		}
		
		return false;
    }
    
    public boolean isWithinWander(int x, int y) { 
    	if(maxWander < 0)
    		return true;
    	
    	return controller.distance(startTile[0], startTile[1], x, y) <= maxWander;
    }
    
    public void popup(String title, String text) {
    	JFrame parent = new JFrame(title);
    	JLabel textLabel = new JLabel(text);
    	JButton okButton = new JButton("OK");
    	
    	parent.setLayout(new GridLayout(0,1));
    	
    	okButton.addActionListener(new ActionListener() {
    		@Override
            public void actionPerformed(ActionEvent e) {
    			parent.setVisible(false);
    			parent.dispose();
            }
    	});
    	
    	parent.add(textLabel);
    	parent.add(okButton);
    	parent.setVisible(true);
    	parent.pack();
    }
    
    public boolean validateFields(JTextField npcIds, JTextField maxWanderField, JTextField eatAtHpField, JTextField lootTableField, JTextField spellNameField, JTextField arrowIdField, JTextField switchIdField) {
    	
    	try {
    		String content = npcIds.getText().replace(" ", "");
    		String[] values = null;
    		
    		if(!content.contains(",")) {
    			values = new String[] { content };
    		} else {
    			values = content.split(",");
    		}
    		
    		for(String value : values) {
    			Integer.valueOf(value);
    		}
    		
    	} catch(Exception e) {
    		popup("Error", "Invalid loot table value(s).");
    		return false;
    	}
    	
    	try {
    		Integer.valueOf(maxWanderField.getText());
    	} catch(Exception e) {
    		popup("Error", "Invalid max wander value.");
    		return false;
    	}
    	
    	try {
    		Integer.valueOf(eatAtHpField.getText());
    	} catch(Exception e) {
    		popup("Error", "Invalid eat at HP value.");
    		return false;
    	}
    	
    	try {
    		String content = lootTableField.getText().replace(" ", "");
    		String[] values = null;
    		
    		if(!content.contains(",")) {
    			values = new String[] { content };
    		} else {
    			values = content.split(",");
    		}
    		
    		for(String value : values) {
    			Integer.valueOf(value);
    		}
    		
    	} catch(Exception e) {
    		popup("Error", "Invalid loot table value(s).");
    		return false;
    	}
    	
    	if(controller.getSpellIdFromName(spellNameField.getText()) < 0) {
    		popup("Error", "Spell name does not exist.");
    		return false;
    	}
    	
    	try {
    		Integer.valueOf(arrowIdField.getText());
    	} catch(Exception e) {
    		popup("Error", "Invalid arrow ID value.");
    		return false;
    	}
    	
    	try {
    		Integer.valueOf(switchIdField.getText());
    	} catch(Exception e) {
    		popup("Error", "Invalid switch ID value.");
    		return false;
    	}
    	
    	
    	return true;
    }
    
    public void setValuesFromGUI(JComboBox<String> fightModeField, JTextField npcIdsField, JTextField maxWanderField, JTextField eatAtHpField, JTextField lootTableField, JCheckBox buryBonesCheckbox, JCheckBox magingCheckbox, JTextField spellNameField, JCheckBox rangingCheckbox, JTextField arrowIdField, JTextField switchIdField) {
    	this.fightMode = fightModeField.getSelectedIndex();
    	
    	if(npcIdsField.getText().contains(",")) {
    		for(String value : npcIdsField.getText().replace(" ",  "").split(",")) {
    			this.npcIds = Arrays.copyOf(npcIds, npcIds.length + 1);
    			this.npcIds[npcIds.length - 1] = Integer.parseInt(value);
    		}
    	} else {
    		this.npcIds = new int[] { Integer.parseInt(npcIdsField.getText()) } ;
    	}
    	
    	this.maxWander = Integer.valueOf(maxWanderField.getText());
    	this.eatingHealth = Integer.valueOf(eatAtHpField.getText());
    	
    	if(lootTableField.getText().contains(",")) {
    		for(String value : lootTableField.getText().replace(" ", "").split(",")) {
    			this.loot = Arrays.copyOf(loot, loot.length + 1);
    			this.loot[loot.length - 1] = Integer.parseInt(value);
    		}
    	} else {
    		this.loot = new int[] { Integer.parseInt(lootTableField.getText()) };
    	}
    	
    	this.buryBones = buryBonesCheckbox.isSelected();
    	this.maging = magingCheckbox.isSelected();
    	this.spellId = controller.getSpellIdFromName(spellNameField.getText());
    	this.ranging = rangingCheckbox.isSelected();
    	this.arrowId = Integer.parseInt(arrowIdField.getText());
    	this.switchId = Integer.parseInt(switchIdField.getText());

    }
    
    public void setupGUI() { 	
    	JLabel fightModeLabel = new JLabel("Fight Mode:");
    	JComboBox<String> fightModeField = new JComboBox<String>(new String[] {"Controlled", "Aggressive", "Accurate", "Defensive"});
    	JLabel npcIdsLabel = new JLabel("NPC IDs:");
    	JTextField npcIdsField = new JTextField("65,114");
    	JLabel maxWanderLabel = new JLabel("Max Wander Distance: (-1 disable)");
    	JTextField maxWanderField = new JTextField("-1");
    	JLabel eatAtHpLabel = new JLabel("Eat at HP: (food is automatically detected)");
    	JTextField eatAtHpField = new JTextField(String.valueOf(controller.getCurrentStat(controller.getStatId("Hits")) / 2));
    	JLabel lootTableLabel = new JLabel("Loot Table: (comma separated)");
    	JTextField lootTableField = new JTextField("381");
    	JCheckBox buryBonesCheckbox = new JCheckBox("Bury Bones?");
    	JCheckBox magingCheckbox = new JCheckBox("Magic?");
    	JLabel spellNameLabel = new JLabel("Spell Name: (exactly as it appears in spellbook)");
    	JTextField spellNameField = new JTextField("Wind Bolt");
    	JCheckBox rangingCheckbox = new JCheckBox("Ranging?");
    	JLabel arrowIdLabel = new JLabel("Pickup Arrow ID: (-1 to disable)");
    	JTextField arrowIdField = new JTextField("-1");
    	JLabel switchLabel = new JLabel("Switch ID (weapon to switch to if in melee combat while ranging)");
    	JTextField switchIdField = new JTextField("81");
        JButton startScriptButton = new JButton("Start");
        
        startScriptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if(validateFields(npcIdsField, maxWanderField, eatAtHpField, lootTableField, spellNameField, arrowIdField, switchIdField)) {
            		setValuesFromGUI(fightModeField, npcIdsField, maxWanderField, eatAtHpField, lootTableField, buryBonesCheckbox, magingCheckbox, spellNameField, rangingCheckbox, arrowIdField, switchIdField);
            		
            		controller.displayMessage("@red@AIOFighter by Dvorak. Let's party like it's 2004!");
            		
	            	scriptFrame.setVisible(false);
	            	scriptFrame.dispose();
	            	scriptStarted = true;
	            	//scriptStart();
            	}
            }
        });
        
        magingCheckbox.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
       			spellNameField.setEnabled(magingCheckbox.isSelected());
        	}
        });
        
        rangingCheckbox.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		arrowIdField.setEnabled(rangingCheckbox.isSelected());
        		switchIdField.setEnabled(rangingCheckbox.isSelected());
        	}
        });
        
        
    	scriptFrame = new JFrame("Script Options");
    	
    	//scriptFrame.setLayout(new BoxLayout(scriptFrame.getContentPane(), BoxLayout.Y_AXIS));
    	scriptFrame.setLayout(new GridLayout(0,2));
    	scriptFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	scriptFrame.add(fightModeLabel);
    	scriptFrame.add(fightModeField);
    	scriptFrame.add(npcIdsLabel);
    	scriptFrame.add(npcIdsField);
    	scriptFrame.add(maxWanderLabel);
    	scriptFrame.add(maxWanderField);
    	scriptFrame.add(eatAtHpLabel);
    	scriptFrame.add(eatAtHpField);
    	scriptFrame.add(lootTableLabel);
    	scriptFrame.add(lootTableField);
    	scriptFrame.add(buryBonesCheckbox);
    	scriptFrame.add(new JLabel());
    	scriptFrame.add(magingCheckbox);
    	scriptFrame.add(new JLabel());
    	scriptFrame.add(spellNameLabel);
    	scriptFrame.add(spellNameField);
    	scriptFrame.add(rangingCheckbox);
    	scriptFrame.add(new JLabel());
    	scriptFrame.add(arrowIdLabel);
    	scriptFrame.add(arrowIdField);
    	scriptFrame.add(switchLabel);
    	scriptFrame.add(switchIdField);
    	scriptFrame.add(startScriptButton);
    	
    	spellNameField.setEnabled(false);
    	arrowIdField.setEnabled(false);
    	switchIdField.setEnabled(false);
    	
    	centerWindow(scriptFrame);
    	scriptFrame.setVisible(true);
    	scriptFrame.pack();
    }
    
	public static void centerWindow(Window frame) {
	    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
	    int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
	    frame.setLocation(x, y);
	}
    
}
