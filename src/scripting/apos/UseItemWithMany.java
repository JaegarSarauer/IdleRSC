package scripting.apos;
import compatibility.apos.Script;
import java.text.DecimalFormat;

// cut willow longs: 13,1,633,28,660,1

public class UseItemWithMany extends Script {

    public UseItemWithMany(String ex) {
    }

    private int itemOne, itemOneAmount, itemTwo, itemTwoAmount, itemResult;
    private boolean waitForBank = false;
    private final int CHEST = 942;
    private boolean needToMove = false;
    private long bankTimer;
    private int count = 0;
    private long startTime = -1L;
    private final DecimalFormat iformat = new DecimalFormat("#,##0");
    private int option;


    @Override
    public void init(String params) {
        String[] options = params.split(",");
        itemOne = Integer.parseInt(options[0]);
        itemOneAmount = Integer.parseInt(options[1]);
        itemTwo = Integer.parseInt(options[2]);
        itemTwoAmount = Integer.parseInt(options[3]);
        itemResult = Integer.parseInt(options[4]);
        try {
            option = Integer.parseInt(options[5]);
        } catch (Exception ex) {
            option = 0;
        }
    }

    @Override
    public int main() {
        if (startTime == -1L) startTime = System.currentTimeMillis();

        if (waitForBank && !isBanking()) {
            if (System.currentTimeMillis() - bankTimer > 10000) {
                waitForBank = false;
                bankTimer = -1L;
            }
            return 50;
        }

        if (isQuestMenu()) {
            if (questMenuOptions()[0].toLowerCase().contains("bank")) {
                answer(0);
                return 1000;
            } else {
                answer(option);
                return 600;
            }
        }

        if (getFatigue() > 95) {
            useSleepingBag();
            return 1000;
        }

        if (isBanking()) {
            bankTimer = -1L;
            waitForBank = false;
            if (getInventoryCount(itemResult) > 0) {
                count += getInventoryCount(itemResult);
                deposit(itemResult, getInventoryCount(itemResult));
                return 1800;
            } else if (getInventoryCount(itemOne) < itemOneAmount) {
                withdraw(itemOne, itemOneAmount - getInventoryCount(itemOne));
                return 1800;
            } else if (getInventoryCount(itemTwo) < itemTwoAmount) {
                withdraw(itemTwo, itemTwoAmount - getInventoryCount(itemTwo));
                return 1800;
            } else {
                closeBank();
                return 1200;
            }
        }

        if (needToMove) {
            needToMove = false;
            if (isReachable(getX() + 1, getY())) {
                walkTo(getX()+1,getY());
                return 1500;
            }
            if (isReachable(getX(), getY()+1)) {
                walkTo(getX(),getY()+1);
                return 1500;
            }
            if (isReachable(getX(), getY()-1)) {
                walkTo(getX(),getY()-1);
                return 1500;
            }
            if (isReachable(getX()-1, getY())) {
                walkTo(getX()-1,getY());
                return 1500;
            }
        }

        if (getInventoryCount(itemTwo) == 0 || getInventoryCount(itemOne) == 0) {
            int[] chest = getObjectById(CHEST);
            if (chest[0] != -1) {
                atObject(chest[1], chest[2]);
                return 1000;
            } else {
                int[] banker = getNpcByIdNotTalk(BANKERS);
                if (banker[0] != -1) {
                    talkToNpc(banker[0]);
                    return 2000;
                }
            }
        } else {
            useItemWithItem(getInventoryIndex(itemOne), getInventoryIndex(itemTwo));
            return 600;
        }
        return 1000;
    }

    @Override
    public void onServerMessage(String str) {
        str = str.toLowerCase();
        if (str.contains("this chest")) {
            waitForBank = true;
            bankTimer = System.currentTimeMillis();
        } else if (str.contains("have been standing")) {
            needToMove = true;
        }
    }

    // ripped from Shantay_Trader
    private String per_hour(long count, long time) {
        double amount, secs;

        if (count == 0) return "0";
        amount = count * 60.0 * 60.0;
        secs = (System.currentTimeMillis() - time) / 1000.0;
        return iformat.format(amount / secs);
    }

    @Override
    public void paint() {
        final int white = 0xFFFFFF;
        final int cyan = 0x00FFFF;
        int y = 25;
        int num = 0;
        drawString("Use 1 Item With Many", 25, y, 1, white);
        y += 15;
        drawString("Runtime: " + get_time_since(startTime), 25, y, 1, white);
        y += 15;
        drawString("Count: " + per_hour(count, startTime) + "/h", 25, y, 1, white);
        y += 15;
    }

    // ripped from somewhere.. one of the S_ scripts
    private static String get_time_since(long t) {
        long millis = (System.currentTimeMillis() - t) / 1000;
        long second = millis % 60;
        long minute = (millis / 60) % 60;
        long hour = (millis / (60 * 60)) % 24;
        long day = (millis / (60 * 60 * 24));

        if (day > 0L) {
            return String.format("%02d days, %02d hrs, %02d mins",
                    day, hour, minute);
        }
        if (hour > 0L) {
            return String.format("%02d hours, %02d mins, %02d secs",
                    hour, minute, second);
        }
        if (minute > 0L) {
            return String.format("%02d minutes, %02d seconds",
                    minute, second);
        }
        return String.format("%02d seconds", second);
    }
}
