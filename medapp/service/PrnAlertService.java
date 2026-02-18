package jp.suzuki.medapp.service;

import java.util.List;

import jp.suzuki.medapp.domain.IntakeRecord;

public class PrnAlertService {

    public boolean isOverLimit(String prnName, int maxTimesPerDay, List<IntakeRecord> records) {

        int count = 0;

        for (IntakeRecord r : records) {
            if (r.isTaken() && r.getMedicineName().equals(prnName)) {
                count++;
            }
        }
        return count > maxTimesPerDay;
    }
}
