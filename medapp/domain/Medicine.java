package jp.suzuki.medapp.domain;

public class Medicine {
    private final String name;
    private final double doseGram;

    // 追加
    private final boolean prn;     // 頓服？
    private final PrnRule prnRule; // 頓服ルール（頓服じゃないならnull）

    // 既存：通常薬
    public Medicine(String name, double doseGram) {
        this(name, doseGram, false, null);
    }

    // 追加：頓服薬
    public Medicine(String name, double doseGram, PrnRule rule) {
        this(name, doseGram, true, rule);
    }

    private Medicine(String name, double doseGram, boolean prn, PrnRule rule) {
        this.name = name;
        this.doseGram = doseGram;
        this.prn = prn;
        this.prnRule = rule;
    }

    public String getName() { return name; }
    public double getDoseGram() { return doseGram; }
    public boolean isPrn() { return prn; }
    public PrnRule getPrnRule() { return prnRule; }
}
