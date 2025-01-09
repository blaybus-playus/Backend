package org.example.playus.domain.quest.groupGuset;

import lombok.Builder;
import lombok.Getter;

@Getter
public class QuestDetail {
    private int month; // 월
    private int week; // 주
    private String date; // 날짜
    private String day; // 요일
    private int sales; // 매출
    private int personalFee; // 개인 수수료
    private int designFee; // 디자인 수수료
    private int salary; // 급여
    private int retirementPay; // 퇴직금
    private int ensureFee; // 보험료

    @Builder
    public QuestDetail(int month, int week, String date, String day, int sales, int personalFee, int designFee, int salary, int retirementPay, int ensureFee) {
        this.month = month;
        this.week = week;
        this.date = date;
        this.day = day;
        this.sales = sales;
        this.personalFee = personalFee;
        this.designFee = designFee;
        this.salary = salary;
        this.retirementPay = retirementPay;
        this.ensureFee = ensureFee;
    }
}
