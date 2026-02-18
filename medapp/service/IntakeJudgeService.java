package jp.suzuki.medapp.service;

public class IntakeJudgeService {

    private final double toleranceGram = 0.3;     // 誤差許容
    private final double zeroThresholdGram = 0.1; // ほぼゼロ判定

    public boolean judge(boolean opened, double before, double after, double dose) {

        if (!opened) return false;

        // ゼロになったら確定
        if (after <= zeroThresholdGram) return true;

        double diff = before - after; // 減った量
        return diff >= (dose - toleranceGram);
    }
}


