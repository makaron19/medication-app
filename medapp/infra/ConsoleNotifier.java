package jp.suzuki.medapp.infra;

public class ConsoleNotifier implements Notifier {

    @Override
    public void notifyToUser(String message) {
        System.out.println("🔔【本人】" + message);
    }

    @Override
    public void notifyToCaregiver(String message) {
        // 今回はコンソールで代用（将来：LINE/メールに差し替え）
        System.out.println("📩【介助者】" + message);
    }
}
