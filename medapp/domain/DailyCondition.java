package jp.suzuki.medapp.domain;

public class DailyCondition {

    // 1〜5（1=とても悪い、5=とても良い）
    private final int moodLevel;

    // 自由記述（空でもOK）
    private final String memo;

    public DailyCondition(int moodLevel, String memo) {
        this.moodLevel = moodLevel;
        this.memo = memo;
    }

    public int getMoodLevel() {
        return moodLevel;
    }

    public String getMemo() {
        return memo;
    }
}
