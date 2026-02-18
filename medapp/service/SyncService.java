package jp.suzuki.medapp.service;



import jp.suzuki.medapp.domain.IntakeRecord;
import jp.suzuki.medapp.domain.Medicine;

public class SyncService {
    public void sync(IntakeRecord record, Medicine med) {
        System.out.println("📲 アプリ連携：薬=" + med.getName()
                + ", 時間帯=" + record.getSlot().label()
                + ", 飲んだ=" + (record.isTaken() ? "YES" : "NO")
                + ", before=" + record.getBeforeGram() + "g"
                + ", after=" + record.getAfterGram() + "g");
    }
}

