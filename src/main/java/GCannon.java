import net.runelite.api.Point;
import rsb.event.listener.PaintListener;
import rsb.internal.input.VirtualMouse;
import rsb.methods.GameGUI;
import rsb.methods.Inventory;
import rsb.methods.NPCs;
import rsb.methods.Skills;
import rsb.script.Script;
import rsb.script.ScriptManifest;
import rsb.wrappers.RSArea;
import rsb.wrappers.RSNPC;
import rsb.wrappers.RSTile;

import java.awt.*;


@ScriptManifest(authors = { "dginovker" }, name = "Goblin Killer", version = 1.00, description = "<html><head>"
        + "</head><body>"
        + "<center><strong><h2>dginovker's Goblin Killer</h2></strong></center>"
        + "<center><strong>Start the script near Goblins<br />"
        + "</body></html>")
public class GCannon extends Script implements PaintListener {
    private long startTime;
    private State lastState;

    private void l(String toLog) {
        log("Bracket: " + toLog);
    }

    private enum State {
        equip, walkTo, fighting, attack, unknown
    }

    final ScriptManifest properties = getClass().getAnnotation(
            ScriptManifest.class);

    // OTHER VARIABLES
    private RSArea goblinArea = new RSArea(new RSTile(3264, 3222, 0), new RSTile(3256, 3230, 0));
    private int startXP = 0;
    private int startLvl = 0;

    private State getState() {
        if (inventory.contains("Bronze Dagger")) {
            return State.equip;
        }
        if (players.getMyPlayer().isIdle() && getGoblin() != null) {
            return State.attack;
        }
        if (players.getMyPlayer().isInCombat()) {
            return State.fighting;
        }
        if (!goblinArea.contains(players.getMyPlayer().getPosition())) {
            return State.walkTo;
        }
        return State.unknown;
    }

    private RSNPC getGoblin() {
        return NPCs.methods.npcs.getNearest(n -> n != null
                && n.getName() != null
                && n.getName().equals("Goblin")
                && !n.isInCombat()
                && n.getHPPercent() > 0
        );
    }

    // *******************************************************//
    // MAIN LOOP
    // *******************************************************//
    @Override
    public int loop() {
        if (!game.isLoggedIn()) {
            return 2000;
        }

        lastState = getState();
        switch (lastState) {
            case equip:
                inventory.getItem("Bronze Dagger").doAction("Wield");
            case walkTo:
                l("Need to walk to goblins");
                walking.walkTileMM(goblinArea.getNearestTile(players.getMyPlayer().getLocation()));
                break;
            case attack:
                l("Attacking goblin");
                RSNPC goblin = getGoblin();
                if (!goblin.isOnScreen()) {
                    l("Rotated camera so I can see " + goblin);
                    camera.turnTo(goblin);
                }
                if (goblin.doAction("Attack")) {
                    l("Successfully attacked goblin");
                } else {
                    l("Failed to attack goblin");
                }
                break;
            case fighting:
                l("I'm fighting a goblin");
                break;
            default:
                l("Unknown state");
        }

        return 3000;
    }

    // *******************************************************//
    // ON FINISH
    // *******************************************************//
    @Override
    public void onFinish() {
        getBot().getEventManager().removeListener(this);
    }

    @Override
    public boolean onStart() {
        startTime = System.currentTimeMillis();
        startLvl = skills.getCurrentLevel(Skills.getIndex("strength"));
        startXP = skills.getCurrentExp(Skills.getIndex("strength"));
        mouse.setSpeed(mouse.getSpeed() * 2);
        return true;
    }

    // *******************************************************//
    // PAINT SCREEN
    // *******************************************************//
    public void onRepaint(final Graphics g) {
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
                .getIndex("strength"));
        currentXP = skills.getCurrentExp(Skills.getIndex("strength"));
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
            g.drawString("XP To Next Level: "
                            + skills.getExpToNextLevel(Skills.getIndex("strength")),
                    561, coords[8]);
            g.drawString("% To Next Level: "
                    + skills.getPercentToNextLevel(Skills
                    .getIndex("strength")), 561, coords[10]);

            g.drawString("State: " + lastState, 561, coords[12]);
            g.drawString("Animation: " + players.getMyPlayer().getAnimation(), 561, coords[13]);
        }
    }
}
