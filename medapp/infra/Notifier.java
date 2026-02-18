package jp.suzuki.medapp.infra;

public interface Notifier {
    void notifyToUser(String message);
    void notifyToCaregiver(String message);
}
