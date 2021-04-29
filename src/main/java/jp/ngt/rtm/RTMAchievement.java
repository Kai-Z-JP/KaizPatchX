package jp.ngt.rtm;

import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

import java.util.ArrayList;
import java.util.List;

/**
 * 実績処理はServerのみでOK
 */
public class RTMAchievement {
    public static final String ACHIEVEMENT_PAGE_NAME = "Real Train Mod";
    public static List<Achievement> achievementList = new ArrayList<>();

    /**
     * 耐火レンガの設置
     */
    public static Achievement startIronMaking;
    /**
     * 金鋸使用
     */
    public static Achievement getSteel;
    /**
     * 熱風炉煉瓦の設置
     */
    public static Achievement startIronMaking2;
    /**
     * 転炉を建造
     */
    public static Achievement buildConverter;
    /**
     * 線路を敷設
     */
    public static Achievement layRail;
    /**
     * 列車を運転
     */
    public static Achievement rideTrain;
    /**
     * モブを撥ねるor車止め衝突
     */
    public static Achievement accidentsWillHappen;

    /**
     * 突放
     */
    //public static Achievement kickOffSwitching;//突放入換, kick-off switching
    public static void init() {
        startIronMaking = achievement("startIronMaking", 0, 0, new ItemStack(RTMBlock.fireBrick, 1, 0), null, true);
        getSteel = achievement("getSteel", 2, 0, new ItemStack(RTMItem.steel_ingot, 1, 0), startIronMaking, false);
        startIronMaking2 = achievement("startIronMaking2", 4, 0, new ItemStack(RTMBlock.hotStoveBrick, 1, 0), getSteel, false);
        buildConverter = achievement("buildConverter", 6, 0, new ItemStack(RTMBlock.steelMaterial, 1, 0), startIronMaking2, false);

        layRail = achievement("layRail", 0, 2, new ItemStack(RTMItem.itemLargeRail, 1, 10), null, true);
        rideTrain = achievement("rideTrain", 2, 2, new ItemStack(RTMItem.itemtrain, 1, 0), layRail, false);
        accidentsWillHappen = achievement("accidentsWillHappen", 2, 4, new ItemStack(RTMItem.itemtrain, 1, 0), rideTrain, false);
        //kickOffSwitching = achievement("kickOffSwitching", 2, 2, new ItemStack(RTMItem.itemtrain, 1, 0), rideTrain, false);

        AchievementPage.registerAchievementPage(new AchievementPage(ACHIEVEMENT_PAGE_NAME, achievementList.toArray(new Achievement[0])));
    }

    /**
     * @param name
     * @param column      左-右+
     * @param row         上-下+
     * @param iconItem
     * @param parent      親実績
     * @param independent 最初の実績のみtrue
     */
    private static Achievement achievement(String name, int column, int row, ItemStack iconItem, Achievement parent, boolean independent) {
        Achievement achievement = new Achievement("achievement." + name, name, column, row, iconItem, parent);
        if (independent) {
            achievement.initIndependentStat();
        }
        achievement.registerStat();
        achievementList.add(achievement);
        return achievement;
    }
}