package com.sanghee.glowuprizzcrm.admin.stats;

// 전환율 = 신청 수 / 순 방문자 수 (docs/adr/0006 참고). 순 방문자가 0이면 0으로 나누기를
// 피하기 위해 0을 반환한다. 소수 넷째 자리로 반올림해 응답을 안정적으로 만든다.
public final class ConversionRateCalculator {

    private ConversionRateCalculator() {
    }

    public static double calculate(long submissionCount, long visitorCount) {
        if (visitorCount == 0) {
            return 0.0;
        }
        double rate = (double) submissionCount / visitorCount;
        return Math.round(rate * 10000) / 10000.0;
    }
}
