package com.sanghee.glowuprizzcrm.admin.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ConversionRateCalculator 단위 테스트")
class ConversionRateCalculatorTest {

    @Test
    @DisplayName("순 방문자 수로 신청 수를 나눈 값을 반환한다")
    void calculate_returnsSubmissionDividedByVisitor() {
        double rate = ConversionRateCalculator.calculate(2, 3);

        assertThat(rate).isCloseTo(0.6667, within(0.0001));
    }

    @Test
    @DisplayName("순 방문자 수가 0이면 0을 반환한다 (0으로 나누기 방지)")
    void calculate_returnsZero_whenVisitorCountIsZero() {
        double rate = ConversionRateCalculator.calculate(0, 0);

        assertThat(rate).isZero();
    }

    @Test
    @DisplayName("소수 넷째 자리까지 반올림한다")
    void calculate_roundsToFourDecimalPlaces() {
        double rate = ConversionRateCalculator.calculate(1, 3);

        assertThat(rate).isEqualTo(0.3333);
    }

    @Test
    @DisplayName("신청 수가 방문자 수를 초과해도(중복 신청) 100%를 넘는 값을 그대로 반환한다")
    void calculate_allowsRateAboveOne() {
        double rate = ConversionRateCalculator.calculate(4, 2);

        assertThat(rate).isEqualTo(2.0);
    }
}
