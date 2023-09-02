package net.autowines.loadout;

import lombok.extern.slf4j.Slf4j;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;

@Slf4j
public class ActionDelayHelper {
    public static boolean stopScript = false;

    public static void shortSleep() {
        Time.sleep(150,325);
    }
    public static void fastSleep() {
        sleepClientTick();
        Time.sleep(25,55);
    }
    public static int fastReturn() {
        sleepClientTick();

        return Rand.nextInt(25,55);
    }
    public static boolean waitClientTick = false;
    public static void sleepClientTick() {
        waitClientTick = true;
        Time.sleepUntil(() -> !waitClientTick,10, 600);
    }
    public static int returnTick() {
        Time.sleepTick();

        return fastReturn();
    }
    public static int shortReturn() {
        return Rand.nextInt(200,325);
    }
}