package jp.suzuki.medapp.app;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import jp.suzuki.medapp.domain.DailyCondition;
import jp.suzuki.medapp.domain.DoseSlot;
import jp.suzuki.medapp.domain.IntakeRecord;
import jp.suzuki.medapp.domain.Medicine;
import jp.suzuki.medapp.domain.TabletBox;
import jp.suzuki.medapp.infra.ConsoleNotifier;
import jp.suzuki.medapp.infra.Notifier;
import jp.suzuki.medapp.service.IntakeJudgeService;

/**
 * 【要件定義（最新版）とクラス対応】
 *
 * 1) 1週間の記録管理
 *   - weekly: Map<Integer, List<IntakeRecord>>（Main）
 *   - IntakeRecord: 1件の服薬ログ（timestamp自動保存）
 *
 * 2) 体調記録（気分1〜5 + 自由記述）
 *   - conditionMap: Map<Integer, DailyCondition>（Main）
 *   - DailyCondition: moodLevel, memo（空でも可）
 *
 * 3) 定期薬（朝昼夕夜）
 *   - DoseSlot: MORNING/NOON/EVENING/NIGHT
 *   - Medicine: 薬名 + 1回量(g)
 *   - TabletBox: 時間帯ごとの残量(g)を保持
 *   - IntakeJudgeService: opened/before/after/dose から服薬判定
 *
 * 4) 頓服の安全設計
 *   - 6時間未満: NG（次回服用可能時刻を表示）
 *   - 1日3回以上: NG（＝maxTimes=2、3回目アウト）
 *   - timestamp（IntakeRecord）を用いて lastTime.plusHours(6) で次回時刻を算出
 *
 * 5) 未確定ログ
 *   - 規定違反や異常時は taken=false の IntakeRecord として履歴に残す（誤記録防止）
 *
 * 6) 通知
 *   - Notifier（interface）→ ConsoleNotifier（実装）
 *   - 本人/介助者へメッセージ出力（将来はLINE等に差し替え可能）
 *
 * 【今回の到達点】
 *   - コンソール表示のみで1週間分を管理し、頓服の回数・間隔を安全に判定できる。
 * 【将来拡張】
 *   - DB永続化（Repository）、Servlet/JSP化、実センサー連携、分析高度化。
 */


public class Main {

    public static void main(String[] args) {

        Notifier notifier = new ConsoleNotifier();
        Scanner sc = new Scanner(System.in);
        IntakeJudgeService judge = new IntakeJudgeService();

        Map<DoseSlot, Medicine> medBySlot = new EnumMap<>(DoseSlot.class);
        medBySlot.put(DoseSlot.MORNING, new Medicine("朝：血圧の薬", 5.0));
        medBySlot.put(DoseSlot.NOON,    new Medicine("昼：胃薬",     2.0));
        medBySlot.put(DoseSlot.EVENING, new Medicine("夕：ビタミン",  1.0));
        medBySlot.put(DoseSlot.NIGHT,   new Medicine("夜：睡眠薬",    0.5));

        Map<Integer, List<IntakeRecord>> weekly = new HashMap<>();
        Map<Integer, DailyCondition> conditionMap = new HashMap<>();

        System.out.println("=== 服薬管理アプリ（timestamp自動保存版）===");

        for (int day = 1; day <= 7; day++) {

            System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("📅 " + day + "日目");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━");

            int mood = readMood(sc);
            System.out.print("今日のメモ（空欄OK）：");
            String memo = sc.nextLine();
            conditionMap.put(day, new DailyCondition(mood, memo));

            List<IntakeRecord> records = new ArrayList<>();
            weekly.put(day, records);

            Map<DoseSlot, Double> init = new EnumMap<>(DoseSlot.class);
            for (DoseSlot slot : DoseSlot.values()) {
                init.put(slot, medBySlot.get(slot).getDoseGram());
            }
            TabletBox box = new TabletBox(init);

            // ===== 定期薬 =====
            for (DoseSlot slot : DoseSlot.values()) {

                Medicine med = medBySlot.get(slot);
                double before = box.getRemaining(slot);

                System.out.println("\n【" + slot.label() + "】 " + med.getName());

                System.out.print("フタ開いた？ true/false：");
                boolean opened = Boolean.parseBoolean(sc.nextLine());

                double after = before;

                if (opened) {
                    System.out.print("取り出し後残量(g)：");
                    after = Double.parseDouble(sc.nextLine());
                }

                boolean taken = judge.judge(opened, before, after, med.getDoseGram());

                if (after > before) {
                    notifier.notifyToUser("センサー異常の可能性。");
                    taken = false;
                    after = before;
                }

                if (!taken) after = before;

                box.updateRemaining(slot, after);

                records.add(new IntakeRecord(
                        slot,
                        med.getName(),
                        med.getDoseGram(),
                        taken,
                        before,
                        after
                ));

                System.out.println(taken ? "✅ 服薬記録（時刻保存）" : "× 未服薬");
            }

            // ===== 頓服 =====
            while (true) {

                System.out.print("\n頓服を試みますか？ yes/no：");
                String ans = sc.nextLine();
                if (!ans.equalsIgnoreCase("yes")) break;

                String prnName = "頓服：痛み止め";
                double prnDose = 1.0;
                int maxTimes = 2;   // 3回目以降アウト
                int minHours = 6;

                List<IntakeRecord> prnList = getPrnTaken(records, prnName);

                // 日3回以上アウト
                if (prnList.size() >= maxTimes) {

                    notifier.notifyToUser("今日は規定回数に達しています。");
                    notifier.notifyToCaregiver("頓服回数超過。");

                    records.add(new IntakeRecord(
                            DoseSlot.MORNING,
                            prnName + "（未確定：回数超過）",
                            prnDose,
                            false,
                            0,
                            0
                    ));

                    System.out.println("⛔ 今日はこれ以上服用できません。次は明日以降です。");
                    continue;
                }

                // 前回服用時刻取得
                LocalDateTime lastTime = null;
                if (!prnList.isEmpty()) {
                    lastTime = prnList.get(prnList.size() - 1).getTimestamp();
                }

                LocalDateTime now = LocalDateTime.now();

                if (lastTime != null) {

                    LocalDateTime nextOk = lastTime.plusHours(minHours);

                    if (now.isBefore(nextOk)) {

                        notifier.notifyToUser("規定間隔に達していません。");
                        notifier.notifyToCaregiver("短間隔の可能性。");

                        records.add(new IntakeRecord(
                                DoseSlot.MORNING,
                                prnName + "（未確定：間隔不足）",
                                prnDose,
                                false,
                                0,
                                0
                        ));

                        System.out.println("⏳ 次に服用可能な時刻："
                                + nextOk.getHour() + "時"
                                + String.format("%02d", nextOk.getMinute()) + "分 以降");

                        continue;
                    }
                }

                System.out.println("✅ 今は服用可能です。");

                System.out.print("実際に飲みましたか？ yes/no：");
                String took = sc.nextLine();
                if (!took.equalsIgnoreCase("yes")) continue;

                records.add(new IntakeRecord(
                        DoseSlot.MORNING,
                        prnName,
                        prnDose,
                        true,
                        0,
                        0
                ));

                LocalDateTime nextOk = LocalDateTime.now().plusHours(minHours);

                System.out.println("🕒 次に服用可能な時刻："
                        + nextOk.getHour() + "時"
                        + String.format("%02d", nextOk.getMinute()) + "分 以降");
            }

            printDaySummary(day, conditionMap.get(day), records);
        }

        printCorrelation(weekly, conditionMap);

        sc.close();
    }

    private static int readMood(Scanner sc) {
        while (true) {
            System.out.print("気分(1〜5、空欄=3)：");
            String s = sc.nextLine();
            if (s.isBlank()) return 3;
            try {
                int v = Integer.parseInt(s);
                if (v >= 1 && v <= 5) return v;
            } catch (Exception ignored) {}
            System.out.println("1〜5で入力してください");
        }
    }

    private static List<IntakeRecord> getPrnTaken(List<IntakeRecord> records, String name) {
        List<IntakeRecord> list = new ArrayList<>();
        for (IntakeRecord r : records) {
            if (r.isTaken() && r.getMedicineName().equals(name)) list.add(r);
        }
        list.sort(Comparator.comparing(IntakeRecord::getTimestamp));
        return list;
    }

    private static void printDaySummary(int day, DailyCondition dc, List<IntakeRecord> records) {
        System.out.println("\n==== " + day + "日目 記録一覧 ====");
        for (IntakeRecord r : records) {
            System.out.println(r.getSlot().label() + " | "
                    + r.getMedicineName() + " | "
                    + (r.isTaken() ? "〇" : "×")
                    + " | 時刻=" + r.getTimestamp().getHour()
                    + ":" + String.format("%02d", r.getTimestamp().getMinute()));
        }
        System.out.println("気分：" + dc.getMoodLevel());
        System.out.println("メモ：" + dc.getMemo());
    }

    private static void printCorrelation(
            Map<Integer, List<IntakeRecord>> weekly,
            Map<Integer, DailyCondition> conditionMap) {

        System.out.println("\n=== 相関（気分×頓服回数）===");
        for (int day : weekly.keySet()) {
            int prn = 0;
            for (IntakeRecord r : weekly.get(day)) {
                if (r.getMedicineName().contains("頓服") && r.isTaken()) prn++;
            }
            System.out.println(day + "日目：気分="
                    + conditionMap.get(day).getMoodLevel()
                    + " 頓服回数=" + prn);
        }
    }
}
