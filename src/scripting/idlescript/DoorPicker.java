package scripting.idlescript;


/**
 * Buys vials or newts in Taverly, banks in Falador
 *  
 * @author Dvorak
 */
public class DoorPicker extends IdleScript {	
	
	
	int doorID = 95;
	int doorXCoord = 557;
	int doorYCoord = 3425;
	
	boolean scriptStarted = false;
	boolean guiSetup = false;
	
	int doorsPicked = 0;
	boolean inside = false;
	
	long startTimestamp = System.currentTimeMillis() / 1000L;
	
	public int start(String parameters[]) {
		scriptStart();
    	return 1000;
	}
	
	public void scriptStart() {
		while(controller.isRunning()) {
			try {
				if (controller.currentX() < 557) {
					controller.openDoor(doorXCoord, doorYCoord, true);
					inside = true;
				} else {
					if (inside) {
						inside = false;
						doorsPicked++;
					}
					controller.openDoor(doorXCoord, doorYCoord);
				}
				controller.sleep(100);
			} catch (Exception e) {
				controller.setStatus("@gre@Hit Exception!!!");
				controller.sleep(3000);
			}
		}
	}
}
