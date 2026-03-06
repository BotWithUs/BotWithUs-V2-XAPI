package net.botwithus.xapi.util.statistic;

import net.botwithus.xapi.util.BwuMath;
import net.botwithus.xapi.util.StringUtils;
import net.botwithus.xapi.util.collection.PairList;
import net.botwithus.xapi.util.time.DurationStringFormat;
import net.botwithus.xapi.util.time.Stopwatch;
import net.botwithus.xapi.util.time.Timer;

import java.text.NumberFormat;

public class XPInfo {
    private final String skillName;
    private int startLvl;
    private int startXP;
    private int currentLvl;
    private int currentXP;
    private int xpUntilNextLevel;

    public XPInfo(String skillName, int level, int xp, int xpUntilNextLevel) {
        this.skillName = skillName;
        snapshot(level, xp, xpUntilNextLevel);
        reset();
    }

    public void snapshot(int level, int xp, int xpUntilNextLevel) {
        this.currentLvl = level;
        this.currentXP = xp;
        this.xpUntilNextLevel = xpUntilNextLevel;
    }

    public void reset() {
        this.startLvl = currentLvl;
        this.startXP = currentXP;
    }

    public int getLevelsGained() {
        return currentLvl - startLvl;
    }

    public int getGainedXP() {
        return currentXP - startXP;
    }

    public int getXPHour(Stopwatch watch) {
        return BwuMath.getUnitsPerHour(watch, getGainedXP());
    }

    public int getSecondsUntilLevel(Stopwatch watch) {
        int rate = getXPHour(watch);
        if (rate <= 0) {
            return Integer.MAX_VALUE;
        }
        return (int) ((((double) xpUntilNextLevel) / rate) * 3600.0);
    }

    public PairList<String, String> getPairList(Stopwatch stopWatch) {
        PairList<String, String> list = new PairList<>();
        if (currentXP > startXP) {
            String name = StringUtils.toTitleCase(skillName);
            list.add(name + " Level: ", currentLvl + " (" + getLevelsGained() + " Gained)");
            list.add(name + " XP Gained: ", NumberFormat.getIntegerInstance().format(getGainedXP()) + " (" + NumberFormat.getIntegerInstance().format(getXPHour(stopWatch)) + "/Hour)");
            list.add(name + " TTL: ", Timer.secondsToFormattedString(getSecondsUntilLevel(stopWatch), DurationStringFormat.DESCRIPTION));
        }
        return list;
    }

    public String getSkillsType() {
        return skillName;
    }
}
