package scripting.idlescript;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * SmithingVarrock by Searos
 * @author Searos
 */
public class SmithingVarrock extends IdleScript {
	JFrame scriptFrame = null;
	boolean guiSetup = false;
	boolean scriptStarted = false;
	int barId = -1;
	int ans1 = -1;
	int ans2 = -1;
	int ans3 = -1;
	int ans4 = -1;
	int barsLeft = -1;
	int totalSmithed =0;
	int[] barIds = { 169, 170, 171, 173, 174, 408 };
	int[] bankerIds = { 95, 224, 268, 485, 540, 617 };

	public int start(String parameters[]) {
		if (!guiSetup) {
			setupGUI();
			guiSetup = true;
		}

		if (scriptStarted) {
			scriptStart();
		}
		
		return 1000; //start() must return a int value now. 
	}

	public void scriptStart() {
		while (controller.isRunning()) {
			if (controller.getInventoryItemCount(barId) <= 5 && !controller.isInBank()) {
				controller.setStatus("@red@Banking");
				controller.openBank();
				controller.sleep(1000);
				if (controller.getInventoryItemCount() > 1 && controller.isInBank()) {
					for (int itemId : controller.getInventoryItemIds()) {
						if (itemId != 168 && itemId != 1263) {
							totalSmithed = totalSmithed + controller.getInventoryItemCount(itemId);
							controller.depositItem(itemId, controller.getInventoryItemCount(itemId));
						}
					}
					controller.sleep(429);
				}
				if (controller.getInventoryItemCount(168) < 1) {
					controller.withdrawItem(168, 1);
				}
				if (controller.getInventoryItemCount(barId) < 1) {
					controller.withdrawItem(barId, 29);
					controller.sleep(1000);
				}
				barsLeft = controller.getBankItemCount(barId);
				controller.closeBank();
				controller.sleep(1280);
				if (barsLeft < 1) {
					controller.sleepHandler(98, true);
					controller.useItemIdOnObject(controller.getNearestObjectById(50)[0],
							controller.getNearestObjectById(50)[1], barId);
					controller.sleep(8000);
					if (controller.isInOptionMenu()) {
						controller.optionAnswer(ans1);
						controller.sleep(640);
						controller.optionAnswer(ans2);
						controller.sleep(640);
						controller.optionAnswer(ans3);
						if (controller.isInOptionMenu()) {
							controller.sleep(640);
							controller.optionAnswer(ans4);
						}
					}
					controller.sleep(640);
					while (controller.isBatching()) {
						controller.sleep(100);
					}
					scriptStarted = false;
					guiSetup = false;
					return;
				}
			}
			while (controller.getInventoryItemCount(barId) > 5 && !controller.isInBank()) {
				controller.setStatus("Smithing");
				controller.sleepHandler(98, true);
				controller.useItemIdOnObject(controller.getNearestObjectById(50)[0],
						controller.getNearestObjectById(50)[1], barId);
				controller.sleep(640);
				if (controller.isInOptionMenu()) {
					controller.optionAnswer(ans1);
					controller.sleep(640);
					controller.optionAnswer(ans2);
					controller.sleep(640);
					controller.optionAnswer(ans3);
					if (controller.isInOptionMenu()) {
						controller.sleep(640);
						controller.optionAnswer(ans4);
					}
				}
				controller.sleep(640);
				while (controller.isBatching()) {
					controller.sleep(100);
				}
			}
		}
		scriptStarted = false;
		guiSetup = false;
	}

	public static void centerWindow(Window frame) {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);
	}

	public void setupGUI() {
		JLabel header = new JLabel("Smithing");
		JLabel barLabel = new JLabel("Bar Type:");
		JComboBox<String> barField = new JComboBox<String>(
				new String[] { "Bronze", "Iron", "Steel", "Mithril", "Adamantite", "Runite" });
		JLabel ans1Label = new JLabel("Item Type:");
		JComboBox<String> ans1Field = new JComboBox<String>(new String[] { "Weapon", "Armour", "Missile Heads" });
		JLabel ans2Label = new JLabel("Weapon Type");
		JComboBox<String> ans2Field = new JComboBox<String>(
				new String[] { "Dagger", "Throwing Knife", "Sword", "Axe", "Mace" });
		JLabel ans3Label = new JLabel("How many");
		JComboBox<String> ans3Field = new JComboBox<String>(new String[] { "1", "5", "10", "all" });
		JLabel ans4Label = new JLabel("Null");
		JComboBox<String> ans4Field = new JComboBox<String>(new String[] { "Null" });
		JButton startScriptButton = new JButton("Start");

		startScriptButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ans1 = ans1Field.getSelectedIndex();
				ans2 = ans2Field.getSelectedIndex();
				ans3 = ans3Field.getSelectedIndex();
				ans4 = ans4Field.getSelectedIndex();
				barId = barIds[barField.getSelectedIndex()];
				scriptFrame.setVisible(false);
				scriptFrame.dispose();
				scriptStarted = true;
				controller.displayMessage("@gre@" + '"' + "heh" + '"' + " - Searos");
				controller.displayMessage("@red@Smithing started @ran@ It's HAMMERTIME");
			}
		});

		ans1Field.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (ans1Field.getSelectedIndex() == 0) {
					ans2Label.setText("Weapon Type");
					ans2Field.setModel(
							new JComboBox<>(new String[] { "Dagger", "Throwing Knife", "Sword", "Axe", "Mace" })
									.getModel());
					scriptFrame.setVisible(false);
					scriptFrame.setVisible(true);
				}
				if (ans1Field.getSelectedIndex() == 1) {
					ans2Label.setText("Armour Type");
					ans2Field.setModel(new JComboBox<>(new String[] { "Helmet", "Shield", "Armour" }).getModel());
					scriptFrame.setVisible(false);
					scriptFrame.setVisible(true);
				}
				if (ans1Field.getSelectedIndex() == 2) {
					ans2Label.setText("Missile Type");
					ans2Field.setModel(new JComboBox<>(new String[] { "Arrowheads" }).getModel());
					scriptFrame.setVisible(false);
					scriptFrame.setVisible(true);
				}
				ans1 = ans1Field.getSelectedIndex();
			}
		});
		ans2Field.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				if (ans2Field.getSelectedIndex() == 0 && ans1Field.getSelectedIndex() == 0) {
					ans3Label.setText("How many");
					ans3Field.setModel(new JComboBox<>(new String[] { "1", "5", "10", "all" }).getModel());
					ans4Label.setText("Null");
					ans4Field.setModel(new JComboBox<>(new String[] { "Null" }).getModel());
					scriptFrame.setVisible(false);
					scriptFrame.setVisible(true);
				}
				if (ans2Field.getSelectedIndex() == 1 && ans1Field.getSelectedIndex() == 0) {
					ans3Label.setText("How many");
					ans3Field.setModel(new JComboBox<>(new String[] { "1", "5", "10", "all" }).getModel());
					ans4Label.setText("Null");
					ans4Field.setModel(new JComboBox<>(new String[] { "Null" }).getModel());
					scriptFrame.setVisible(false);
					scriptFrame.setVisible(true);
				}
				if (ans2Field.getSelectedIndex() == 2 && ans1Field.getSelectedIndex() == 0) {
					ans3Label.setText("Sword Type");
					ans3Field.setModel(new JComboBox<>(new String[] { "Short", "Long", "Scimitar", "2h" }).getModel());
					ans4Label.setText("How many");
					ans4Field.setModel(new JComboBox<>(new String[] { "1", "5", "10", "all" }).getModel());
					scriptFrame.setVisible(false);
					scriptFrame.setVisible(true);
				}
				if (ans2Field.getSelectedIndex() == 3 && ans1Field.getSelectedIndex() == 0) {
					ans3Label.setText("Axe Type");
					ans3Field.setModel(new JComboBox<>(new String[] { "Hatchet", "Battle" }).getModel());
					ans4Label.setText("How many");
					ans4Field.setModel(new JComboBox<>(new String[] { "1", "5", "10", "all" }).getModel());
					scriptFrame.setVisible(false);
					scriptFrame.setVisible(true);
				}
				if (ans2Field.getSelectedIndex() == 4 && ans1Field.getSelectedIndex() == 0) {
					ans3Label.setText("How many");
					ans3Field.setModel(new JComboBox<>(new String[] { "1", "5", "10", "all" }).getModel());
					ans4Label.setText("Null");
					ans4Field.setModel(new JComboBox<>(new String[] { "Null" }).getModel());
					scriptFrame.setVisible(false);
					scriptFrame.setVisible(true);
				}
				if (ans2Field.getSelectedIndex() == 0 && ans1Field.getSelectedIndex() == 1) {
					ans3Label.setText("Helmet Type");
					ans3Field.setModel(new JComboBox<>(new String[] { "Medium", "Large" }).getModel());
					ans4Label.setText("How many");
					ans4Field.setModel(new JComboBox<>(new String[] { "1", "5", "10", "all" }).getModel());
					scriptFrame.setVisible(false);
					scriptFrame.setVisible(true);
				}
				if (ans2Field.getSelectedIndex() == 1 && ans1Field.getSelectedIndex() == 1) {
					ans3Label.setText("Shield Type");
					ans3Field.setModel(new JComboBox<>(new String[] { "Square", "Kite" }).getModel());
					ans4Label.setText("How many");
					ans4Field.setModel(new JComboBox<>(new String[] { "1", "5", "10", "all" }).getModel());
					scriptFrame.setVisible(false);
					scriptFrame.setVisible(true);
				}
				if (ans2Field.getSelectedIndex() == 2 && ans1Field.getSelectedIndex() == 1) {
					ans3Label.setText("Armour Type");
					if(controller.isAuthentic()) {
						ans3Field.setModel(new JComboBox<>(
								new String[] { "Chain Body", "Plate Body", "Plate Legs", "Plate Skirt" })
										.getModel());
					} else {
						ans3Field.setModel(new JComboBox<>(
								new String[] { "Chain Legs", "Chain Body", "Plate Body", "Plate Legs", "Plate Skirt" })
										.getModel());
					}
					ans4Label.setText("How many");
					ans4Field.setModel(new JComboBox<>(new String[] { "1", "5", "10", "all" }).getModel());
					scriptFrame.setVisible(false);
					scriptFrame.setVisible(true);
				}
				if (ans2Field.getSelectedIndex() == 0 && ans1Field.getSelectedIndex() == 2) {
					ans3Label.setText("How many");
					ans3Field.setModel(new JComboBox<>(new String[] { "1", "5", "10", "all" }).getModel());
					ans4Label.setText("Null");
					ans4Field.setModel(new JComboBox<>(new String[] { "Null" }).getModel());
					scriptFrame.setVisible(false);
					scriptFrame.setVisible(true);
				}
				ans2 = ans2Field.getSelectedIndex();
			}
		});

		scriptFrame = new JFrame("Script Options");

		scriptFrame.setLayout(new GridLayout(0, 1));
		scriptFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		scriptFrame.add(header);
		scriptFrame.add(barLabel);
		scriptFrame.add(barField);
		scriptFrame.add(ans1Label);
		scriptFrame.add(ans1Field);
		scriptFrame.add(ans2Label);
		scriptFrame.add(ans2Field);
		scriptFrame.add(ans3Label);
		scriptFrame.add(ans3Field);
		scriptFrame.add(ans4Label);
		scriptFrame.add(ans4Field);
		scriptFrame.add(startScriptButton);
		centerWindow(scriptFrame);
		scriptFrame.setVisible(true);
		scriptFrame.pack();
		scriptFrame.requestFocus();

	}
	@Override
	public void paintInterrupt() {
		if(controller != null) {
			controller.drawBoxAlpha(7, 7, 128, 21+14+14, 0xFF0000, 64);
			controller.drawString("@red@Smithing Varrock @gre@by Searos", 10, 21, 0xFFFFFF, 1);
			controller.drawString("@red@Items Smithed: @yel@" + String.valueOf(this.totalSmithed), 10, 21+14, 0xFFFFFF, 1);
		}
	}
}