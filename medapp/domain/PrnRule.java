package jp.suzuki.medapp.domain;

public class PrnRule {
    private final int maxTimesPerDay;     // 1日最大回数
    private final double maxGramPerDay;   // 1日最大g（不要なら大きい値でOK）

    public PrnRule(int maxTimesPerDay, double maxGramPerDay) {
        this.maxTimesPerDay = maxTimesPerDay;
        this.maxGramPerDay = maxGramPerDay;
    }

    public int getMaxTimesPerDay() { return maxTimesPerDay; }
    public double getMaxGramPerDay() { return maxGramPerDay; }
}

