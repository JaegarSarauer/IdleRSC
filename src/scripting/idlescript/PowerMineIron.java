package scripting.idlescript;
import java.lang.*;
import java.util.List;
import com.openrsc.client.entityhandling.defs.GameObjectDef;
/**
 * A basic script for following other players. Useful for farms.
 * @author Dvorak
 *
 */

public class PowerMineIron extends IdleScript {
	public class Pref {
		public String place;
		public int x,y,distance,rockID;
		Pref(String place, int rockID, int xx, int yy, int dist) {
			this.place = place;
			this.rockID = rockID;
			this.x = xx;
			this.y = yy;
			this.distance = dist;
		}
	}
 
	 public Pref[] prefs = {
		 new Pref("Yanille Iron Rocks", 103, 570, 708, 4),
		 new Pref("Yanille Iron Rocks Close", 103, 569, 707, 1),
		 new Pref("Falador West Mine", 103, 366, 554, 3),
	 };
	
	public boolean initComplete = false;
	public Pref pref;
	public int[] currentRock = {0,0};
	public long sleepMiningUntil = 0;
	
	public int start(String[] params) {
		if(controller.isRunning()) {
			tryInit(params);
			
			controller.sleepHandler(98, true);
			
			mineRocks();
			
			controller.sleep(100);
		}
		
		return 10; //start() must return a int value now. 
	}
	
	public void mineRocks() {
		int minePosX = pref.x;
		int minePosY = pref.y;
		int mineDistance = pref.distance;
		
		int[] nearestRock = controller.getNearestObjectById(pref.rockID);
		if (nearestRock == null) {
			return;
		}
		int nearestRockID = controller.getObjectAtCoord(nearestRock[0], nearestRock[1]);
		int curRockID = controller.getObjectAtCoord(currentRock[0], currentRock[1]);
		if (curRockID == 98 || curRockID == -1 || nearestRockID == 98 || nearestRockID == -1) {
			boolean end = false;
			controller.setStatus("@gre@This rock is GONE");
			sleepMiningUntil = System.currentTimeMillis();
			List<GameObjectDef> objs = controller.getObjects();
			int[] xxx = controller.getObjectsX();
			int[] zzz = controller.getObjectsZ();
			for (int i = 0; i < objs.size(); ++i) {
				GameObjectDef obj = objs.get(i);
				if (xxx.length <= i || zzz.length <= i) {
					break;
				}
				int objX = xxx[i];
				int objZ = zzz[i];
				if (obj.id == pref.rockID && controller.distance(minePosX, minePosY, objX, objZ) <= mineDistance 
					&& (objX != currentRock[0] || objZ != currentRock[1]) && (objX != nearestRock[0] || objZ != nearestRock[1])) {
					nearestRock[0] = objX;
					nearestRock[1] = objZ;
					break;
				}
			}
			currentRock[0] = 0;
			currentRock[1] = 0;
		}
		//}
		if (/*((nearestRock[0] != currentRock[0] && nearestRock[1] != currentRock[1]) || !controller.isCurrentlyWalking()) */
			 controller.distance(minePosX, minePosY, nearestRock[0], nearestRock[1]) <= mineDistance && sleepMiningUntil <= System.currentTimeMillis()) {
			//if (controller.isCurrentlyWalking()) {
				currentRock = nearestRock;
			//}
			controller.setStatus("@gre@Clicking rock: " + Integer.toString((int)Math.max((System.currentTimeMillis() - 800 - sleepMiningUntil), 0)));
			controller.atObject(nearestRock[0], nearestRock[1]);
			sleepMiningUntil = System.currentTimeMillis() + 800;
		}
	}
	
	public void tryInit(String[] params) {
		if (!initComplete) {
			try {
				pref = prefs[Integer.parseInt(params[0])];
			} catch(Exception e) {
				pref = prefs[0];
			}
			initComplete = true;
		}
	}
}