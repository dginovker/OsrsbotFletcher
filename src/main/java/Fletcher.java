import rsb.event.listener.PaintListener;
import rsb.methods.GameGUI;
import rsb.methods.Skills;
import rsb.script.Script;
import rsb.script.ScriptManifest;

import java.awt.*;

import static rsb.methods.MethodProvider.methods;


@ScriptManifest(authors = { "dginovker" }, name = "dginovker's Fletcher", version = 1.08, description = "<html><head>"
        + "</head><body>"
        + "<center><strong><h2>A Fletcher that makes Yew Longbows</h2></strong></center>"
        + "<center><strong>Start the script by a bank with a knife in your inventory<br />"
        + "</body></html>")
public class Fletcher extends Script implements PaintListener {
    private long startTime;
    private State lastState;
    private boolean canHitSpace;

    private void l(String toLog) {
        log("Bracket: " + toLog);
    }

    private enum State {
        startingLogFletch,
        startingStringing,
        fletching,
        depositing,
        withdrawingLogs,
        withdrawingStringAndUs,
        unknown
    }

    final ScriptManifest properties = getClass().getAnnotation(
            ScriptManifest.class);

    private final int KNIFE_ID = 946;
    private final int YEW_LOG_ID = 1515;
    private final int YEW_LONGBOW_U_ID = 66;
    private final int BOWSTRING_ID = 1777;
    private final int YEW_LONGBOW_ID = 855;
    private int startXP = 0;
    private int startLvl = 0;
    private long lastClickTime = 0; // To prevent 5 minute log

    private State getState() {
        if (getMyPlayer().isAnimating()) {
            return State.fletching;
        }
        if (bank.isOpen() && (inventory.getCount(KNIFE_ID) == 1 && inventory.getCount(YEW_LONGBOW_U_ID) > 0 || inventory.getCount(YEW_LONGBOW_ID) > 0)
                || !bank.isOpen() && inventory.getCount(YEW_LOG_ID) == 0 && !bank.isOpen() && inventory.getCount(BOWSTRING_ID) == 0) {
            return State.depositing;
        }
        if (bank.isOpen() && bank.getCount(YEW_LOG_ID) > 0) {
            return State.withdrawingLogs;
        }
        if (inventory.contains(YEW_LONGBOW_U_ID) && inventory.contains(BOWSTRING_ID)) {
            return State.startingStringing;
        }
        if (inventory.contains(YEW_LOG_ID)) {
            return State.startingLogFletch;
        }
        return State.unknown;
    }

    @Override
    public int loop() {
        if (!game.isLoggedIn()) {
            return 2000;
        }

        try {
            lastState = getState();
        } catch (Exception e) {
            l("Exception while getting state: " + e.getMessage());
            e.printStackTrace();
            lastState = State.unknown;
        }
        try  {
            switch (lastState) {
                case startingLogFletch:
                    if (random(0, 1) == 0) {
                        inventory.useItem(YEW_LOG_ID, KNIFE_ID);
                    } else {
                        inventory.useItem(KNIFE_ID, YEW_LOG_ID);
                    }
                    long starttime = System.currentTimeMillis();
                    while (!methods.interfaces.getComponent(270, 16).isValid() && starttime + 5000 > System.currentTimeMillis()) {
                        sleep(200);
                    }
                    if (!canHitSpace || random(0, 100) > 90) {
                        // click yew longbow (u) widget - 270, 16
                        methods.interfaces.getComponent(270, 16).doClick();
                    } else {
                        keyboard.pressKey(' ');
                    }
                    canHitSpace = true;
                    break;
                case startingStringing:
                    break;
                case fletching:
                    if (random(0, 100) > 95) {
                        sleep(random(10000, 20000));
                    }
                    break;
                case depositing:
                    if (inventory.isItemSelected()) {
                        inventory.clickSelectedItem();
                    }
                    bank.open();
                    bank.deposit(YEW_LONGBOW_U_ID, 0);
                    bank.deposit(YEW_LONGBOW_ID, 0);
                    break;
                case withdrawingLogs:
                    bank.withdraw(YEW_LOG_ID, 0);
                    bank.close();
                    break;
                case withdrawingStringAndUs:
                    bank.withdraw(BOWSTRING_ID, 13);
                    bank.withdraw(YEW_LONGBOW_U_ID, 13);
                    break;
                case unknown:
                    return 300;
                default:
                    l("Unknown state");
                    break;
            }
        }
        catch (Exception e) {
            l("Exception while doing state: " + e.getMessage());
        }

        return 300;

    }

    @Override
    public void onFinish() {
        getBot().getEventManager().removeListener(this);
    }

    @Override
    public boolean onStart() {
        startTime = System.currentTimeMillis();
        startLvl = skills.getCurrentLevel(Skills.getIndex("fletching"));
        startXP = skills.getCurrentExp(Skills.getIndex("fletching"));
        mouse.setSpeed(mouse.getSpeed() * 8);
        l("Starting script with mouse speed of " + mouse.getSpeed());
        return true;
    }

    public void onRepaint(final Graphics g) {
        // Draw bot's mouse
        if (methods.mouse.isPressed()) {
            g.setColor(new Color(255, 255, 0, 175));
            g.fillOval(methods.mouse.getLocation().getX(), methods.mouse.getLocation().getY(), 7, 7);
            g.setColor(new Color(0, 255, 255, 175));
            g.drawOval(methods.mouse.getLocation().getX(), methods.mouse.getLocation().getY(), 7, 7);
        } else {
            g.setColor(new Color(0, 255, 255, 175));
            g.fillOval(methods.mouse.getLocation().getX(), methods.mouse.getLocation().getY(), 7, 7);
            g.setColor(new Color(255, 255, 0, 175));
            g.drawOval(methods.mouse.getLocation().getX(), methods.mouse.getLocation().getY(), 7, 7);
        }
        long runTime = 0;
        long seconds = 0;
        long minutes = 0;
        long hours = 0;
        int currentXP = 0;
        int currentLVL = 0;
        int gainedXP = 0;

        runTime = System.currentTimeMillis() - startTime;
        seconds = runTime / 1000;
        if (seconds >= 60) {
            minutes = seconds / 60;
            seconds -= minutes * 60;
        }
        if (minutes >= 60) {
            hours = minutes / 60;
            minutes -= hours * 60;
        }

        g.setColor(new Color(0, 255, 255, 175));
        if (mouse.isPressed()) {
            g.setColor(new Color(255, 255, 0, 175));
        }
        g.fillOval(mouse.getLocation().getX(), mouse.getLocation().getY(), 7, 7);

        currentLVL = skills.getCurrentLevel(Skills
                .getIndex("fletching"));
        currentXP = skills.getCurrentExp(Skills.getIndex("fletching"));
        gainedXP = currentXP - startXP;

        if (game.getCurrentTab() == GameGUI.Tab.INVENTORY) {
            g.setColor(new Color(0, 0, 0, 175));
            g.fillRoundRect(555, 210, 175, 250, 10, 10);
            g.setColor(Color.WHITE);
            final int[] coords = new int[]{225, 240, 255, 270, 285, 300, 315,
                    330, 345, 360, 375, 390, 405, 420, 435, 450};
            g.drawString(properties.name(), 561, coords[0]);
            g.drawString("Version: " + properties.version(), 561, coords[1]);
            g.drawString("Run Time: " + hours + ":" + minutes + ":" + seconds,
                    561, coords[2]);
            g.drawString("Current Lvl: " + currentLVL, 561, coords[4]);
            g.drawString("Lvls Gained: " + (currentLVL - startLvl), 561,
                    coords[5]);
            g.drawString("XP Gained: " + gainedXP, 561, coords[6]);
            g.drawString("% To Next Level: "
                    + skills.getPercentToNextLevel(Skills
                    .getIndex("fletching")), 561, coords[8]);

            g.drawString("State: " + lastState, 561, coords[10]);
            g.drawString("Animation: " + players.getMyPlayer().getAnimation(), 561, coords[11]);
        }
    }
}
