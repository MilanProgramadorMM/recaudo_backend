package com.recaudo.api.domain.gateway.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests para {@link MoraConceptCalculator#computeMora(BigDecimal, BigDecimal, long)}.
 *
 * computeMora es un método de cálculo puro (sin dependencias externas), por lo que
 * se prueba instanciando el calculador con dependencias nulas — nunca se tocan.
 *
 * Fórmula bajo prueba:
 *   periodosVencidos = diasMora / 30        (escala 10, HALF_UP)
 *   valorMora        = saldo × tasa × periodosVencidos   (escala 2, HALF_UP)
 */
class MoraConceptCalculatorComputeMoraTest {

    /** Las dependencias no se usan en computeMora, se pasan nulas deliberadamente. */
    private final MoraConceptCalculator calculator =
            new MoraConceptCalculator(null, null, null, null, null, null, null);

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    // ── Casos que deben retornar ZERO (guardas de entrada) ──────────────────────

    @Nested
    @DisplayName("Retorna ZERO cuando algún argumento no es válido")
    class RetornaCero {

        @ParameterizedTest(name = "diasMora = {0} → ZERO")
        @ValueSource(longs = {0L, -1L, -30L, Long.MIN_VALUE})
        @DisplayName("diasMora <= 0")
        void diasMoraNoPositivo(long diasMora) {
            BigDecimal result = calculator.computeMora(bd("1000000"), bd("0.02"), diasMora);
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @ParameterizedTest(name = "saldo = {0} → ZERO")
        @ValueSource(strings = {"0", "0.00", "-0.01", "-500000"})
        @DisplayName("saldoPendiente <= 0")
        void saldoNoPositivo(String saldo) {
            BigDecimal result = calculator.computeMora(bd(saldo), bd("0.02"), 30L);
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @ParameterizedTest(name = "tasa = {0} → ZERO")
        @ValueSource(strings = {"0", "0.00", "-0.0001", "-0.02"})
        @DisplayName("tasaNominal <= 0")
        void tasaNoPositiva(String tasa) {
            BigDecimal result = calculator.computeMora(bd("1000000"), bd(tasa), 30L);
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Todos los argumentos inválidos a la vez")
        void todosInvalidos() {
            BigDecimal result = calculator.computeMora(bd("0"), bd("0"), 0L);
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ── Casos de cálculo real ───────────────────────────────────────────────────

    @Nested
    @DisplayName("Calcula la mora correctamente")
    class CalculoValido {

        @Test
        @DisplayName("30 días = 1 periodo completo → saldo × tasa")
        void unPeriodoCompleto() {
            // periodos = 30/30 = 1 ; mora = 1.000.000 × 0.02 × 1 = 20.000,00
            BigDecimal result = calculator.computeMora(bd("1000000"), bd("0.02"), 30L);
            assertThat(result).isEqualByComparingTo(bd("20000.00"));
        }

        @Test
        @DisplayName("60 días = 2 periodos → saldo × tasa × 2")
        void dosPeriodos() {
            // periodos = 60/30 = 2 ; mora = 1.000.000 × 0.02 × 2 = 40.000,00
            BigDecimal result = calculator.computeMora(bd("1000000"), bd("0.02"), 60L);
            assertThat(result).isEqualByComparingTo(bd("40000.00"));
        }

        @Test
        @DisplayName("1 día (cálculo diario) redondea a 2 decimales")
        void unDiaDiario() {
            // periodos = 1/30 = 0.0333333333
            // mora = 1.000.000 × 0.02 × 0.0333333333 = 666.666666 → 666,67
            BigDecimal result = calculator.computeMora(bd("1000000"), bd("0.02"), 1L);
            assertThat(result).isEqualByComparingTo(bd("666.67"));
        }

        @Test
        @DisplayName("Resultado siempre tiene escala 2")
        void escalaDos() {
            BigDecimal result = calculator.computeMora(bd("1000000"), bd("0.02"), 30L);
            assertThat(result.scale()).isEqualTo(2);
        }

        @ParameterizedTest(name = "saldo={0}, tasa={1}, dias={2} → {3}")
        @CsvSource({
                // saldo,     tasa,     dias, esperado
                "1000000,    0.02,     30,   20000.00",
                "1000000,    0.02,     60,   40000.00",
                "1000000,    0.02,     15,   10000.00",
                "1000000,    0.02,     1,    666.67",
                "500000,     0.025,    30,   12500.00",
                "2500000,    0.018,    45,   67500.00",
                "1234567,    0.02,     7,    5761.31",
        })
        @DisplayName("Casos parametrizados de la fórmula")
        void formulaParametrizada(String saldo, String tasa, long dias, String esperado) {
            BigDecimal result = calculator.computeMora(bd(saldo), bd(tasa), dias);
            assertThat(result).isEqualByComparingTo(bd(esperado));
        }

        @Test
        @DisplayName("Días no múltiplo de 30 usa periodos fraccionarios")
        void periodosFraccionarios() {
            // periodos = 45/30 = 1.5 ; mora = 1.000.000 × 0.02 × 1.5 = 30.000,00
            BigDecimal result = calculator.computeMora(bd("1000000"), bd("0.02"), 45L);
            assertThat(result).isEqualByComparingTo(bd("30000.00"));
        }

        @Test
        @DisplayName("Redondeo HALF_UP en el segundo decimal")
        void redondeoHalfUp() {
            // periodos = 1/30 = 0.0333333333
            // mora = 100.000 × 0.03 × 0.0333333333 = 99.9999999 → 100,00
            BigDecimal result = calculator.computeMora(bd("100000"), bd("0.03"), 1L);
            assertThat(result).isEqualByComparingTo(bd("100.00"));
        }

        @Test
        @DisplayName("Saldo con decimales")
        void saldoConDecimales() {
            // periodos = 30/30 = 1 ; mora = 1.000.000,50 × 0.02 × 1 = 20.000,01
            BigDecimal result = calculator.computeMora(bd("1000000.50"), bd("0.02"), 30L);
            assertThat(result).isEqualByComparingTo(bd("20000.01"));
        }

        @Test
        @DisplayName("Caso real: saldo con centavos, tasa 9.98% y 67 días")
        void casoRealConCentavos() {
            // periodos = 67/30 = 2.2333333333
            // mora = 459.809,58 × 0.0998 × 2.2333333333 = 102.485,424... → 102.485,42
            BigDecimal result = calculator.computeMora(bd("391998.98"), bd("0.0998"), 1L);
            assertThat(result).isEqualByComparingTo(bd("102485.42"));
        }

        @Test
        @DisplayName("Rango largo de mora (catch-up de muchos días)")
        void catchUpLargo() {
            // periodos = 365/30 = 12.1666666667
            // mora = 1.000.000 × 0.02 × 12.1666666667 = 243.333,333334 → 243.333,33
            BigDecimal result = calculator.computeMora(bd("1000000"), bd("0.02"), 365L);
            assertThat(result).isEqualByComparingTo(bd("243333.33"));
        }
    }
}
