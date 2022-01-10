package scripting.idlescript;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import orsc.ORSCharacter;
import controller.Controller;
import models.entities.GroundItemDef;
import scripting.idlescript.AIOCooker.FoodObject;

/**
 * Buys vials or newts in Taverly, banks in Falador
 *  
 * @author Dvorak
 */
public class PickupSlave extends IdleScript {	
	
	public static int distance(int x1, int y1, int x2, int y2) {
		return (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}

	public static int[] getTrimmedPath(int xPos, int yPos, int[] path, boolean traverse) {
		int lowestDistance = 10000000;
		int lowestIndex = path.length / 2;
		for (int i = 0; i < path.length; i += 2) {
			int distance = distance(xPos, yPos, path[i], path[i + 1]);
			if (distance < lowestDistance) {
				lowestDistance = distance;
				lowestIndex = i;
			}
		}
		return Arrays.copyOfRange(path, (traverse ? 0 : path.length - 1), lowestIndex);
	}

	public abstract class BotLocationOption {
		public String optionName;
		int pickupX;
		int pickupY;
		int pickupDistance;
		
		BotLocationOption(String optionName, int pickupX, int pickupY, int pickupDistance) {
			this.optionName = optionName;
			this.pickupX = pickupX;
			this.pickupY = pickupY;
			this.pickupDistance = pickupDistance;
		}

		void walkIntoSpot(Controller controller) {
			controller.walkTo(this.pickupX, this.pickupY);
		}
		
		abstract void walkToBank(Controller controller);
		abstract void walkToSpot(Controller controller);
	}

	public class FaladorWestMine extends BotLocationOption {
		
		int[] doorToMinePath = {343,580,
								348, 571,
								353, 562,
								362, 554};
		
		int[] bankToDoorPath = {288, 574,
								298, 583,
								308, 582,
								319, 582,
								330, 582,
								343, 582};
								
		FaladorWestMine() {
			super("Falador West Mine", 365, 555, 12);
		}
		
		public void walkToBank(Controller controller) {
			
			controller.walkPathReverse(doorToMinePath);
			controller.sleep(1000);
			
			//open gate
			while(controller.currentY() <= 580) {
				controller.displayMessage("@red@Opening door..");
				if(controller.getObjectAtCoord(343, 581) == 138)
					controller.atObject(343, 581);
				controller.sleep(5000);
			}
			
			controller.walkPathReverse(bankToDoorPath);
		}
		
		public void walkToSpot(Controller controller) {
			controller.setStatus("@gre@Walking back to spot..");
			
			controller.walkPath(bankToDoorPath);
			controller.sleep(1000);
			
			//open door
			while(controller.currentY() > 580) {
				controller.displayMessage("@red@Opening door..");
				if(controller.getObjectAtCoord(343, 581) == 138)
					controller.atObject(343, 581);
				controller.sleep(5000);
			}
			
			controller.walkPath(doorToMinePath);
		}
	}

	public class FightArenaMine extends BotLocationOption {
		
		int[] bankToSpotPath = {584, 752,
								581, 746,
								577, 736,
								571, 723,
								571, 714};
								
		FightArenaMine() {
			super("Fight Arena Mine", 571, 714, 12);
		}
		
		public void walkToBank(Controller controller) {
			controller.walkPathReverse(bankToSpotPath);
			controller.sleep(1000);
		}
		
		public void walkToSpot(Controller controller) {
			controller.walkPath(bankToSpotPath);
			controller.sleep(1000);
		}
	}

	String[] lootLocationOptions = new String[] { "Falador West Mine", "Mine east of Fight Area"};			
	
	String pickIDsString = "151,157,158,159,160";
	int pickupX;
	int pickupY;
	int pickupDistance;
	
	
	BotLocationOption botOption;
	int[] lootIDs;
	
	boolean scriptStarted = false;
	boolean guiSetup = false;
	
	int itemsPickedUp = 0;
	
	long startTimestamp = System.currentTimeMillis() / 1000L;
	
	public int start(String parameters[]) {
		if(!guiSetup) {
			if (parameters.length > 0) {
				loadParameterData(Integer.parseInt(parameters[0]), pickIDsString);
			} else {
    			setupGUI();
			}
    		guiSetup = true;
    	}
    	
    	if(scriptStarted) {
    		scriptStart();
    	}
    	
    	return 1000; //start() must return a int value now. 
	}
	
	public void scriptStart() {
		controller.displayMessage("@red@Item pickup slave");
		controller.displayMessage("@red@Start at falador mine");
		
		while(controller.isRunning()) {
			try {
				if(controller.getInventoryItemCount() < 30 && isInSpot()) {
					controller.setStatus("@gre@Grabbing stuff");
					pickupItems();
				} else if (controller.getInventoryItemCount() > 29) {
					if (isNearBank()) {
						controller.setStatus("@gre@Banking");
						bank();
					} else {
						controller.setStatus("@gre@Walking to bank");
						botOption.walkToBank(controller);
					}
				} else {
					if (isNearSpot()) {
						controller.setStatus("@gre@Walking back into spot");
						botOption.walkIntoSpot(controller);
					} else {
						controller.setStatus("@gre@Walking to spot");
						botOption.walkToSpot(controller);
					}
				}
				
				controller.sleep(100);
			} catch (Exception e) {
				controller.setStatus("@gre@Hit Exception!!!");
				controller.sleep(3000);
			}
		}
	}
	
	public int countLoot() {
		int count = 0;
		for(int i = 0; i < lootIDs.length; i++) {
			count += controller.getInventoryItemCount(lootIDs[i]);
		}
		
		return count;
	}
	
	public void pickupItems() {
		List<GroundItemDef> items = controller.getGroundItemsStacked();
		GroundItemDef shortestItem = null;
		int lowestDist = 999999;
		for (int i = 0; i < items.size(); ++i) {
			GroundItemDef item = items.get(i);
			for (int j = 0; j < lootIDs.length; ++j) {
				int dist = controller.distance(item.getX(), item.getZ(), pickupX, pickupY);
				if (item.getID() == lootIDs[j] && dist < pickupDistance) {
					if (dist < lowestDist) {
						lowestDist = dist;
						shortestItem = item;
					}
				}
			}
		}
		if (shortestItem != null) {
			controller.pickupItem(shortestItem.getX(), shortestItem.getZ(), shortestItem.getID(), true, true);
			controller.sleep(600);
		}
	}
	
	public boolean isNearBank() {
		int[] nearestBankCoords = controller.getNearestBank();
		return controller.getDistanceFromLocalPlayer(nearestBankCoords[0], nearestBankCoords[1]) < 20;
	}
	
	public boolean isNearSpot() {
		int distance = controller.getDistanceFromLocalPlayer(pickupX, pickupY);
		controller.log("Distance: " + Integer.toString(distance));
		return distance < pickupDistance * 2;
	}
	
	public boolean isInSpot() {
		return controller.getDistanceFromLocalPlayer(pickupX, pickupY) < pickupDistance;
	}
	
	public void bank() {
		controller.setStatus("@gre@Banking..");
		
		controller.openBank();
		
		for(int i = 0; i < lootIDs.length; i++) {
			itemsPickedUp += controller.getInventoryItemCount(lootIDs[i]);
		}
		
		while(countLoot() > 0) { 
			for(int i = 0; i < lootIDs.length; i++) {
				if(controller.getInventoryItemCount(lootIDs[i]) > 0) {
					controller.depositItem(lootIDs[i], controller.getInventoryItemCount(lootIDs[i])); ///////////////////////////////
					controller.sleep(250);
				}
			}
		}
	}
    
	public static void centerWindow(Window frame) {
	    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
	    int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
	    frame.setLocation(x, y);
	}
	
	public BotLocationOption getBotOption(int index) {
		switch(index) {
			case 0:
				return new FaladorWestMine();
			case 1:
				return new FightArenaMine();
		}
		controller.setStatus("@gre@error loading location..");
		return new FaladorWestMine();
	}
	
    public void setupGUI() { 	
    	final JFrame scriptFrame = new JFrame("Script Options");
    	JLabel headerLabel = new JLabel("Start at spot!");
    	JComboBox<String> optionField = new JComboBox<String>(lootLocationOptions);
    	JTextField itemIDsText = new JTextField(pickIDsString, 30);
        JButton startScriptButton = new JButton("Start");
        
        startScriptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
					String[] split = itemIDsText.getText().split(",");
					lootIDs = new int[split.length];
					for (int i = 0; i < split.length; ++i) {
						lootIDs[i] = Integer.parseInt(split[i]);
					}
					
					botOption = getBotOption(optionField.getSelectedIndex());
					
					pickupX = botOption.pickupX;
					pickupY = botOption.pickupY;
					pickupDistance = botOption.pickupDistance;
            		
	            	scriptFrame.setVisible(false);
	            	scriptFrame.dispose();

					loadParameterData(optionField.getSelectedIndex(), itemIDsText.getText());
	            	
            	}
        });   	
    	
    	scriptFrame.setLayout(new GridLayout(0,1));
    	scriptFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	scriptFrame.add(headerLabel);
    	scriptFrame.add(itemIDsText);
    	scriptFrame.add(optionField);
    	scriptFrame.add(startScriptButton);
    		
    	centerWindow(scriptFrame);
    	scriptFrame.setVisible(true);
    	scriptFrame.pack();
    }

	public void loadParameterData(int optionIndex, String pickIDs) {
		String[] split = pickIDs.split(",");
		lootIDs = new int[split.length];
		for (int i = 0; i < split.length; ++i) {
			lootIDs[i] = Integer.parseInt(split[i]);
		}
		
		botOption = getBotOption(optionIndex);
		
		pickupX = botOption.pickupX;
		pickupY = botOption.pickupY;
		pickupDistance = botOption.pickupDistance;

		scriptStarted = true;
	    controller.displayMessage("@red@Starting Banking Slave");
	}
    
    @Override
    public void paintInterrupt() {
        if(controller != null) {
        			
        	int itemsPerHour = 0;
        	try {
        		float timeRan = (System.currentTimeMillis() / 1000L) - startTimestamp;
        		float scale = (60 * 60) / timeRan;
        		itemsPerHour = (int)(itemsPickedUp * scale);
        	} catch(Exception e) {
        		//divide by zero
        	}
        	
        	int height = 21 + 14 + 14;
        	
            controller.drawBoxAlpha(7, 7, 160, height, 0xFFFFFF, 128);
            controller.drawString("@gre@TaverlyBuyer @whi@by @gre@Dvorak", 10, 21, 0xFFFFFF, 1);
            
			controller.drawString("@gre@Vials bought: @whi@" + String.format("%,d", itemsPickedUp) + " @gre@(@whi@" + String.format("%,d", itemsPerHour) + "@gre@/@whi@hr@gre@)", 10, 21+14, 0xFFFFFF, 1);

        }
    }

}
