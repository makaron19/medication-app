package jp.suzuki.medapp.domain;



public enum DoseSlot {
    MORNING("朝"),
    NOON("昼"),
    EVENING("夕"),
    NIGHT("夜");
	

    private final String label;

    DoseSlot(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
