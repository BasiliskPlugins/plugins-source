package net.autobarbfisher.overlay;

import java.util.concurrent.TimeUnit;

public class AutoBarbFisherOverlayHelper {
    public static int expstarted = 0;
    public static int startinglevel;
    public static long timeBegan;
    public static long xpGained;
    public static double xpTillNextLevel;
    public static long timeTNL;
    public static double nextLevelXp;
    public static String currentState;

    public static final int[] XP_TABLE =
            {
                    0, 0, 83, 174, 276, 388, 512, 650, 801, 969, 1154,
                    1358, 1584, 1833, 2107, 2411, 2746, 3115, 3523, 3973, 4470, 5018,
                    5624, 6291, 7028, 7842, 8740, 9730, 10824, 12031, 13363, 14833,
                    16456, 18247, 20224, 22406, 24815, 27473, 30408, 33648, 37224,
                    41171, 45529, 50339, 55649, 61512, 67983, 75127, 83014, 91721,
                    101333, 111945, 123660, 136594, 150872, 166636, 184040, 203254,
                    224466, 247886, 273742, 302288, 333804, 368599, 407015, 449428,
                    496254, 547953, 605032, 668051, 737627, 814445, 899257, 992895,
                    1096278, 1210421, 1336443, 1475581, 1629200, 1798808, 1986068,
                    2192818, 2421087, 2673114, 2951373, 3258594, 3597792, 3972294,
                    4385776, 4842295, 5346332, 5902831, 6517253, 7195629, 7944614,
                    8771558, 9684577, 10692629, 11805606, 13034431, 200000000
            };

    public static String ft(long duration)
    {
        String res = "";
        long days = TimeUnit.MILLISECONDS.toDays(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration)
                - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                .toHours(duration));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                .toMinutes(duration));
        if (days == 0) {
            res = (hours + ":" + minutes + ":" + seconds);
        } else {
            res = (days + ":" + hours + ":" + minutes + ":" + seconds);
        }
        return res;
    }
}
