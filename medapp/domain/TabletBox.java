package jp.suzuki.medapp.domain;


import java.util.EnumMap;
import java.util.Map;

public class TabletBox {

    private Map<DoseSlot, Double> remainingBySlot;

    // ① 既存のコンストラクタ
    public TabletBox(Medicine med, double initialGram) {
        remainingBySlot = new EnumMap<>(DoseSlot.class);
        for (DoseSlot s : DoseSlot.values()) {
            remainingBySlot.put(s, initialGram);
        }
    }

    // ② ★ここに追加する
    public TabletBox(Map<DoseSlot, Double> initialBySlot) {
        remainingBySlot = new EnumMap<>(DoseSlot.class);
        for (DoseSlot s : DoseSlot.values()) {
            remainingBySlot.put(s, initialBySlot.getOrDefault(s, 0.0));
        }
    }

    public double getRemaining(DoseSlot slot) {
        return remainingBySlot.get(slot);
    }

    public void updateRemaining(DoseSlot slot, double after) {
        remainingBySlot.put(slot, after);
    }
}
