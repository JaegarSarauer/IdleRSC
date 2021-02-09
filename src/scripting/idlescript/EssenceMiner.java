package scripting.idlescript;

import java.lang.Math;
import javax.swing.JComboBox;
import javax.swing.JFrame;

public class EssenceMiner extends IdleScript {
	JComboBox<String> destination = new JComboBox<String>(new String[] { "Seers", "Falador" });
	JFrame scriptFrame = null;
	boolean guiSetup = false;
	boolean scriptStarted = false;
	int[] bankerIds = { 95, 224, 268, 485, 540, 617 };
	int[] bankX = { 500 };
	int[] bankY = { 455 };
	int totalStones = 0;
	boolean started = false;
	boolean inEssenceMine = false;
	boolean bankTime = false;

	int[] pickaxes = { 12, 87, 88, 203, 204, 405, 1263 };

	public void start(String parameters[]) {
			controller.displayMessage("@gre@" + '"' + "heh" + '"' + " - Searos");
		while (controller.isRunning()) {
			scriptStart();
		}
	}

	public void startWalking(int x, int y) {
		// shitty autowalk
		int newX = x;
		int newY = y;
		while (controller.currentX() != x || controller.currentZ() != y) {
			if (controller.currentX() - x > 23) {
				newX = controller.currentX() - 20;
			}
			if (controller.currentZ() - y > 23) {
				newY = controller.currentZ() - 20;
			}
			if (controller.currentX() - x < -23) {
				newX = controller.currentX() + 20;
			}
			if (controller.currentZ() - y < -23) {
				newY = controller.currentZ() + 20;
			}
			if (Math.abs(controller.currentX() - x) <= 23) {
				newX = x;
			}
			if (Math.abs(controller.currentZ() - y) <= 23) {
				newY = y;
			}
			if (!controller.isTileEmpty(newX, newY)) {
				controller.walkToAsync(newX, newY, 2);
				controller.sleep(640);
			} else {
				controller.walkToAsync(newX, newY, 0);
				controller.sleep(640);
			}
		}
	}

	public boolean isPickaxe(int id) {
		for (int i = 0; i < pickaxes.length; i++) {
			if (pickaxes[i] == id)
				return true;
		}

		return false;
	}

	public void scriptStart() {
			if (controller.currentZ() <= 95) {
				inEssenceMine = true;
			} else {
				inEssenceMine = false;
			}
			if (controller.getInventoryItemCount() < 30) {
				bankTime = false;
			}
			while (inEssenceMine && !bankTime && controller.isTileEmpty(691, 2)) {
				startWalking(694, 10);
				controller.sleep(640);
			}
			if (!inEssenceMine && !bankTime && controller.getNearestNpcById(54, false) == null) {
				controller.displayMessage("@red@Walking to Aubury");
				while (controller.getNearestNpcById(54, false) == null) {
					startWalking(104, 524);
					controller.sleep(640);
				}
				while (controller.getNearestNpcById(54, false) != null) {
					controller.npcCommand1(controller.getNearestNpcById(54, false).serverIndex);
					controller.sleep(640);
				}
				return;
			}
			if (controller.getInventoryItemCount() == 30) {
				bankTime = true;
			}
			if (inEssenceMine && !bankTime && !controller.isTileEmpty(691, 2)) {
				controller.atObject(691, 2);
				controller.sleep(640);
				while (controller.isBatching() && controller.getInventoryItemCount() < 30) {
					controller.sleep(100);
				}
				return;
			}
			if (inEssenceMine && bankTime) {
				controller.displayMessage("@red@Leaving Mine");
				while (controller.getNearestNpcById(54, true) == null) {
					controller.atObject(685, 14);
					controller.sleep(640);
				}
				inEssenceMine = false;
				return;
			}
			if (!inEssenceMine && bankTime) {
				controller.displayMessage("@red@Walking to bank");
				while (controller.getNearestNpcByIds(bankerIds, false) == null) {
					startWalking(bankX[0], bankY[0]);
				}
				controller.displayMessage("@red@Banking");
				while (!controller.isInBank()) {
					controller.openBank();
				}
				totalStones = totalStones + controller.getInventoryItemCount(1299);
				while (controller.getInventoryItemCount() > 1 && controller.isInBank()) {
					for (int itemId : controller.getInventoryItemIds()) {
						if (itemId != 0 && !isPickaxe(itemId)) {
							controller.depositItem(itemId, controller.getInventoryItemCount(itemId));
							controller.sleep(10);
						}
					}
				}
				controller.sleep(618);
				controller.closeBank();
				controller.displayMessage("@red@Finished Banking");
				controller.displayMessage("@yel@ Banked " + totalStones + " Rune Stones");
			}
	}
}