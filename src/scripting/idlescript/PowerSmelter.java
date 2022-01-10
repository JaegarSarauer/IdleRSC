package scripting.idlescript;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * This is AIOSmelter written for IdleRSC.
 * 
 * It is your standard Falador/AK smelter script.
 * 
 * It has the following features:
 * 
 * * GUI 
 * * CLI arg support (example: "AIOSmelter" "Falador" "Gold amulet")
 * * All bars smeltable 
 * * Goldsmithing gauntlets support 
 * * Cannonball support
 * * Silver/gold/gem items craftable
 * 
 * 
 * @author Dvorak (modified by Searos)
 */

public class PowerSmelter extends IdleScript {
	JFrame scriptFrame = null;
	boolean guiSetup = false;
	boolean scriptStarted = false;

	int barId = -1;
	int destinationId = -1;
	int[] barIds = { 169, 170, 384, 171, 1041, 172, 173, 174, 408, 44, 1027, 283, 288, 296, 284, 289, 297, 285, 290, 298, 286, 291, 299, 287, 292, 300, 543, 544, 524 };
	
	String[] traders;

	long startTimestamp = System.currentTimeMillis() / 1000L;
	int success = 0;
	
	String[] options = new String[] {"Bronze Bar", "Iron Bar", "Silver Bar",
			"Steel Bar", "Cannonballs", "Gold Bar", "Mithril Bar", "Adamantite Bar", "Runite Bar", 
			"Holy symbol","Unholy symbol","Gold ring","Gold necklace","Gold amulet","Sapphire ring",
			"Sapphire necklace","Sapphire amulet","Emerald ring","Emerald necklace","Emerald amulet",
			"Ruby ring","Ruby necklace","Ruby amulet","Diamond ring","Diamond necklace","Diamond amulet",
			"Dragonstone ring","Dragonstone necklace","Dragonstone amulet"};
	
	String[] destinations = new String[] { "Falador", "Al-Kharid" };
	

	Map<Integer, Map<Integer, Integer>> ingredientsMapping = new HashMap<Integer, Map<Integer, Integer>>() {
		{
			put(169, new HashMap<Integer, Integer>() {
				{
					put(150, 1);
					put(202, 1);
				}
			}); // bronze needs 1 copper and 1 tin
			put(170, new HashMap<Integer, Integer>() {
				{
					put(151, 1);
				}
			}); // iron needs 1 iron ore
			put(384, new HashMap<Integer, Integer>() {
				{
					put(383, 1);
				}
			}); // silver needs 1 silver ore
			put(171, new HashMap<Integer, Integer>() {
				{
					put(151, 1);
					put(155, 2);
				}
			}); // steel needs 1 iron 2 coal
			put(1041, new HashMap<Integer, Integer>() {
				{
					put(1057, 1);
					put(171, 1);
				}
			});
			put(172, new HashMap<Integer, Integer>() {
				{
					put(152, 1);
					put(699, 1);
				}
			}); // gold needs 1 gold ore
			put(173, new HashMap<Integer, Integer>() {
				{
					put(153, 1);
					put(155, 4);
				}
			});
			put(174, new HashMap<Integer, Integer>() {
				{
					put(154, 1);
					put(155, 6);
				}
			});
			put(408, new HashMap<Integer, Integer>() {
				{
					put(409, 1);
					put(155, 8);
				}
			});
			//Holy symbol
			put(44, new HashMap<Integer, Integer>() {
				{
					put(384, 1);
					put(386, 1);
				}
			});
			//Unholy symbol
			put(1027, new HashMap<Integer, Integer>() {
				{
					put(384, 1);
					put(1026, 1);
				}
			});
			//Gold ring
			put(283, new HashMap<Integer, Integer>() {
				{
					put(293, 1);
					put(172, 1);
				}
			});
			//Gold necklace
			put(288, new HashMap<Integer, Integer>() {
				{
					put(295, 1);
					put(172, 1);
				}
			});
			//Gold amulet
			put(296, new HashMap<Integer, Integer>() {
				{
					put(294, 1);
					put(172, 1);
				}
			});
			//Sapphire ring
			put(284, new HashMap<Integer, Integer>() {
				{
					put(293, 1);
					put(172, 1);
					put(164, 1);
				}
			});
			//Sapphire necklace
			put(289, new HashMap<Integer, Integer>() {
				{
					put(295, 1);
					put(172, 1);
					put(164, 1);
				}
			});
			//Sapphire amulet
			put(297, new HashMap<Integer, Integer>() {
				{
					put(294, 1);
					put(172, 1);
					put(164, 1);
				}
			});
			//Emerald ring
			put(285, new HashMap<Integer, Integer>() {
				{
					put(293, 1);
					put(172, 1);
					put(163, 1);
				}
			});
			//Emerald necklace
			put(290, new HashMap<Integer, Integer>() {
				{
					put(295, 1);
					put(172, 1);
					put(163, 1);
				}
			});
			//Emerald amulet
			put(298, new HashMap<Integer, Integer>() {
				{
					put(294, 1);
					put(172, 1);
					put(163, 1);
				}
			});
			//Ruby ring
			put(286, new HashMap<Integer, Integer>() {
				{
					put(293, 1);
					put(172, 1);
					put(162, 1);
				}
			});
			//Ruby necklace
			put(291, new HashMap<Integer, Integer>() {
				{
					put(295, 1);
					put(172, 1);
					put(162, 1);
				}
			});
			//Ruby amulet
			put(299, new HashMap<Integer, Integer>() {
				{
					put(294, 1);
					put(172, 1);
					put(162, 1);
				}
			});
			//Diamond ring
			put(287, new HashMap<Integer, Integer>() {
				{
					put(293, 1);
					put(172, 1);
					put(161, 1);
				}
			});
			//Diamond necklace
			put(292, new HashMap<Integer, Integer>() {
				{
					put(295, 1);
					put(172, 1);
					put(161, 1);
				}
			});
			//Diamond amulet
			put(300, new HashMap<Integer, Integer>() {
				{
					put(294, 1);
					put(172, 1);
					put(161, 1);
				}
			});
			//Dragonstone ring
			put(543, new HashMap<Integer, Integer>() {
				{
					put(293, 1);
					put(172, 1);
					put(523, 1);
				}
			});
			//Dragonstone necklace
			put(544, new HashMap<Integer, Integer>() {
				{
					put(295, 1);
					put(172, 1);
					put(523, 1);
				}
			});
			//Dragonstone amulet
			put(524, new HashMap<Integer, Integer>() {
				{
					put(294, 1);
					put(172, 1);
					put(523, 1);
				}
			});
		}
	};

	Map<Integer, Integer> ingredients = null;

	public int start(String parameters[]) {
		if(scriptStarted) { 
			scriptStart();
		} else {
			if(parameters.length == 1) {
				if (!guiSetup) {
					setupGUI();
					guiSetup = true;
				}
		
				if (scriptStarted) {
					scriptStart();
				}
			} else {
				try {
					
					for(int i = 0; i < destinations.length; i++) {
						String option = destinations[i];
						
						if(option.toLowerCase().equals(parameters[0].toLowerCase())) {
							destinationId = i;
							break;
						}
					}
					
					for(int i = 0; i < options.length; i++) {
						String option = options[i];
						
						if(option.toLowerCase().equals(parameters[1].toLowerCase())) {
							barId = barIds[i];
							ingredients = ingredientsMapping.get(barId);
							break;
						}
					}
					
					traders = parameters[2].split(",");
					
					if(barId == -1 || ingredients == null)
						throw new Exception("Ingredients not selected!");
					
					scriptStarted = true;
					controller.displayMessage("@red@AIOSmelter by Dvorak. Let's party like it's 2004!");
				}
				catch(Exception e) {
					System.out.println("Could not parse parameters!");
					controller.displayMessage("@red@Could not parse parameters!");
					controller.stop();
				}
				
			}
		}
		
		return 1000; //start() must return a int value now. 
	}

	boolean acceptedTrade = false;
	boolean tradeRecipientAccepted = false;

	public void scriptStart() {
		if (isEnoughOre()) {
			while (controller.getNearestObjectById(118) == null) {
				controller.setStatus("Walking to furnace..");
				if (destinationId == 0) {
					controller.walkTo(318, 551, 0, true);
					controller.walkTo(311, 545, 0, true);
				}
				if (destinationId == 1) {
					controller.walkTo(84, 679, 0, true);
				}
			}
			Iterator<Entry<Integer, Integer>> iterator = ingredients.entrySet().iterator();
			int oreId = iterator.next().getKey();
			int mouldAnswer = -1;
			int gemAnswer = 0;
			
			if (oreId == 1057) { // do not use the cannonball mold on the furnace!
				oreId = 171;
			} else if(controller.getInventoryItemCount(293) > 0) {
				oreId = 172;
				mouldAnswer = 0;
			} else if(controller.getInventoryItemCount(295) > 0) {
				oreId = 172;
				mouldAnswer = 1;
			} else if(controller.getInventoryItemCount(294) > 0) {
				oreId = 172;
				mouldAnswer = 2;
			}

			if(controller.getInventoryItemCount(164) > 0)
				gemAnswer = 1;
			if(controller.getInventoryItemCount(163) > 0)
				gemAnswer = 2;
			if(controller.getInventoryItemCount(162) > 0)
				gemAnswer = 3;
			if(controller.getInventoryItemCount(161) > 0)
				gemAnswer = 4;
			if(controller.getInventoryItemCount(523) > 0) 
				gemAnswer = 5;
				
			controller.setStatus("Smelting..");
			
			while (controller.getInventoryItemCount(oreId) > 0 && controller.getNearestObjectById(118) != null) {

				while (controller.isBatching())
					controller.sleep(10);

				if (controller.getInventoryItemCount(699) > 0) { // wield gauntlets
					controller.setStatus("Wielding gauntlets..");
					controller.equipItem(controller.getInventoryItemSlotIndex(699));
					controller.sleep(618);
				}

				controller.setStatus("Smelting!");
				controller.sleepHandler(98, true);
				controller.useItemIdOnObject(controller.getNearestObjectById(118)[0],
						controller.getNearestObjectById(118)[1], oreId);
				
				if (oreId == 171) {
					controller.sleep(3000); // cannonballs take way longer and can be interrupted by starting another one
				} else if(oreId == 172) {
					controller.sleep(800);
					controller.optionAnswer(mouldAnswer);
					controller.sleep(800);
					controller.optionAnswer(gemAnswer);
					controller.sleep(250);
				} else {
					controller.sleep(618);
				}

				while (controller.isBatching())
					controller.sleep(10);
			}

		} else {
			controller.setStatus("Trading..");
			if (!tradeRecipientAccepted && controller.isTradeRecipientAccepting()) {
				tradeRecipientAccepted = true;
				controller.sleep(700);
			} else if (tradeRecipientAccepted && !controller.isTradeRecipientAccepting()) {
				tradeRecipientAccepted = false;
				acceptedTrade = false;
				controller.sleep(700);
			}
			if (controller.isInTradeConfirmation()) {
				controller.acceptTradeConfirmation();
				acceptedTrade = true;
			} else if (controller.isInTrade() && !controller.isInTradeConfirmation()) {
				int size = Math.min(12, controller.getInventoryItemCount(barId));
				int[] ids = new int[size];
				int[] amounts = new int[size];
				for (int i = 0; i < size; ++i) {
					ids[i] = barId;
					amounts[i] = 1;
				}
				controller.setTradeItems(ids, amounts);
				controller.sleep(700);
				controller.acceptTrade();
				acceptedTrade = true;
			} else if (!controller.isInTrade()) {
				for (int i = 0; i < traders.length; ++i) {
					int playerServerIndex = controller.getPlayerServerIndexByName(traders[i]);
					if (playerServerIndex >= 0) {
						controller.sleep(700);
						if (!controller.isInTrade()) {
							controller.tradePlayer(playerServerIndex);
							controller.sleep(700);
							if (controller.isInTrade()) {
								tradeRecipientAccepted = false;
								acceptedTrade = false;
								break;
							}
						}
					}
				}
			}
		}
		controller.sleep(100);
	}

	public boolean isEnoughOre() {
		for (Map.Entry<Integer, Integer> entry : ingredients.entrySet()) {
			if(entry.getKey() == 699)
				continue;
				
			if (controller.getInventoryItemCount(entry.getKey()) < entry.getValue())
				return false;
		}

		return true;
	}

	public static void centerWindow(Window frame) {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);
	}

	public void setupGUI() {
		JLabel header = new JLabel("Start in Falador or Al-Kharid bank!");
		JComboBox<String> destination = new JComboBox<String>(destinations);
		JLabel barLabel = new JLabel("Bar Type:");
		JComboBox<String> barField = new JComboBox<String>(options);
		JButton startScriptButton = new JButton("Start");

		startScriptButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				barId = barIds[barField.getSelectedIndex()];
				destinationId = destination.getSelectedIndex();
				ingredients = ingredientsMapping.get(barId);
				scriptFrame.setVisible(false);
				scriptFrame.dispose();
				scriptStarted = true;

				controller.displayMessage("@red@AIOSmelter by Dvorak. Let's party like it's 2004!");
				controller.displayMessage("@red@Al-Kharid support added by Searos. Heh.");
			}
		});

		scriptFrame = new JFrame("Script Options");

		scriptFrame.setLayout(new GridLayout(0, 1));
		scriptFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		scriptFrame.add(header);
		scriptFrame.add(destination);
		scriptFrame.add(barLabel);
		scriptFrame.add(barField);
		scriptFrame.add(startScriptButton);

		centerWindow(scriptFrame);
		scriptFrame.setVisible(true);
		scriptFrame.pack();
	}
    
    @Override
    public void serverMessageInterrupt(String message) {
    	if(message.contains("very heavy"))
    		success++;
    }
	
	
    @Override
    public void questMessageInterrupt(String message) {
    	if(message.contains("retrieve a") || message.contains("make a"))
        	success++;
    }
	
    @Override
    public void paintInterrupt() {
        if(controller != null) {
        			
        	int successPerHr = 0;
        	try {
        		float timeRan = (System.currentTimeMillis() / 1000L) - startTimestamp;
        		float scale = (60 * 60) / timeRan;
        		successPerHr = (int)(success * scale);
        	} catch(Exception e) {
        		//divide by zero
        	}
        	
            controller.drawBoxAlpha(7, 7, 160, 21+14, 0xFF0000, 128);
            controller.drawString("@red@AIOSmelter @whi@by @red@Dvorak", 10, 21, 0xFFFFFF, 1);
            controller.drawString("@red@Smelts: @whi@" + String.format("%,d", success) + " @red@(@whi@" + String.format("%,d", successPerHr) + "@red@/@whi@hr@red@)", 10, 21+14, 0xFFFFFF, 1);
        }
    }
	
}