package jp.suzuki.medapp.domain;

import java.time.LocalDateTime;
/**
 * 服薬ログの1件分データ。
 * - 定期薬/頓服どちらも同じ形式で保存する。
 * - timestamp を自動保存し、頓服の「次回服用可能時刻」の算出に利用する。
 * - taken=false は「未確定ログ」（規定違反・異常値など）として使用する。
 */


public class IntakeRecord {

    private final DoseSlot slot;
    private final String medicineName;
    private final double doseGram;
    private final boolean taken;
    private final double beforeGram;
    private final double afterGram;

    // ★追加：服用（記録）時刻を自動保存
    private final LocalDateTime timestamp;

    public IntakeRecord(DoseSlot slot,
                        String medicineName,
                        double doseGram,
                        boolean taken,
                        double beforeGram,
                        double afterGram) {

        this.slot = slot;
        this.medicineName = medicineName;
        this.doseGram = doseGram;
        this.taken = taken;
        this.beforeGram = beforeGram;
        this.afterGram = afterGram;

        // ★自動で現在時刻を保存
        this.timestamp = LocalDateTime.now();
    }

    public DoseSlot getSlot() { return slot; }
    public String getMedicineName() { return medicineName; }
    public double getDoseGram() { return doseGram; }
    public boolean isTaken() { return taken; }
    public double getBeforeGram() { return beforeGram; }
    public double getAfterGram() { return afterGram; }

    public LocalDateTime getTimestamp() { return timestamp; }
}
