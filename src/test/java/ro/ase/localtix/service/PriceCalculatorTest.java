package ro.ase.localtix.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.ase.localtix.model.TicketCategory;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PriceCalculatorTest {

    private static final BigDecimal BASE_PRICE = new BigDecimal("100.00");
    private static final long NOT_EARLY = 5;    // bought 5 days before the event
    private static final long EARLY = 20;       // bought 20 days before the event

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    // AC1: normal category pays the full price
    @Test
    void normalPaysFullPrice() {
        BigDecimal result = calculator.calculate(BASE_PRICE, TicketCategory.NORMAL, NOT_EARLY);

        assertEquals(new BigDecimal("100.00"), result);
    }

    // AC1: student gets 30% off
    @Test
    void studentGets30PercentDiscount() {
        BigDecimal result = calculator.calculate(BASE_PRICE, TicketCategory.STUDENT, NOT_EARLY);

        assertEquals(new BigDecimal("70.00"), result);
    }

    // AC1: senior gets 40% off
    @Test
    void seniorGets40PercentDiscount() {
        BigDecimal result = calculator.calculate(BASE_PRICE, TicketCategory.SENIOR, NOT_EARLY);

        assertEquals(new BigDecimal("60.00"), result);
    }

    // AC2: early bird (more than 14 days before) gets 20% off
    @Test
    void earlyBirdGets20PercentDiscount() {
        BigDecimal result = calculator.calculate(BASE_PRICE, TicketCategory.NORMAL, EARLY);

        assertEquals(new BigDecimal("80.00"), result);
    }

    // AC2: exactly 14 days before is NOT early bird ("more than 14 days")
    @Test
    void exactly14DaysBeforeIsNotEarlyBird() {
        BigDecimal result = calculator.calculate(BASE_PRICE, TicketCategory.NORMAL, 14);

        assertEquals(new BigDecimal("100.00"), result);
    }

    // AC2: 15 days before is already early bird
    @Test
    void fifteenDaysBeforeIsEarlyBird() {
        BigDecimal result = calculator.calculate(BASE_PRICE, TicketCategory.NORMAL, 15);

        assertEquals(new BigDecimal("80.00"), result);
    }

    // AC3: student + early bird -> only the biggest discount (30%), not 30% + 20%
    @Test
    void studentEarlyBirdGetsOnlyTheBiggestDiscount() {
        BigDecimal result = calculator.calculate(BASE_PRICE, TicketCategory.STUDENT, EARLY);

        assertEquals(new BigDecimal("70.00"), result);
    }

    // AC3: senior + early bird -> only the biggest discount (40%)
    @Test
    void seniorEarlyBirdGetsOnlyTheBiggestDiscount() {
        BigDecimal result = calculator.calculate(BASE_PRICE, TicketCategory.SENIOR, EARLY);

        assertEquals(new BigDecimal("60.00"), result);
    }

    // AC4: the final price is rounded to 2 decimals (99.99 * 0.70 = 69.993 -> 69.99)
    @Test
    void finalPriceIsRoundedDownToTwoDecimals() {
        BigDecimal result = calculator.calculate(new BigDecimal("99.99"), TicketCategory.STUDENT, NOT_EARLY);

        assertEquals(new BigDecimal("69.99"), result);
    }

    // AC4: the final price is rounded to 2 decimals (33.33 * 0.60 = 19.998 -> 20.00)
    @Test
    void finalPriceIsRoundedUpToTwoDecimals() {
        BigDecimal result = calculator.calculate(new BigDecimal("33.33"), TicketCategory.SENIOR, NOT_EARLY);

        assertEquals(new BigDecimal("20.00"), result);
    }

    // AC4: a negative base price throws an exception
    @Test
    void negativeBasePriceThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> calculator.calculate(new BigDecimal("-10.00"), TicketCategory.NORMAL, NOT_EARLY));

        assertEquals("Base price cannot be negative", ex.getMessage());
    }

    // AC4: a base price of 0 is allowed (free event)
    @Test
    void zeroBasePriceIsAllowed() {
        BigDecimal result = calculator.calculate(BigDecimal.ZERO, TicketCategory.STUDENT, NOT_EARLY);

        assertEquals(new BigDecimal("0.00"), result);
    }

    // AC5: the applied discount is shown - student gets 30%
    @Test
    void discountPercentForStudentIs30() {
        assertEquals(30, calculator.getDiscountPercent(TicketCategory.STUDENT, NOT_EARLY));
    }

    // AC5: the applied discount is shown - early bird gets 20%
    @Test
    void discountPercentForEarlyBirdIs20() {
        assertEquals(20, calculator.getDiscountPercent(TicketCategory.NORMAL, EARLY));
    }

    // AC5 + AC3: the shown discount is the biggest one (senior 40% beats early bird 20%)
    @Test
    void discountPercentShowsOnlyTheBiggestDiscount() {
        assertEquals(40, calculator.getDiscountPercent(TicketCategory.SENIOR, EARLY));
    }
}
