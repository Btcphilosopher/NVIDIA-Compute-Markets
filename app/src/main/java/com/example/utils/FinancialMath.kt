package com.example.utils

import com.example.models.Greeks
import com.example.models.OptionType
import kotlin.math.*

object FinancialMath {

    /**
     * Cumulative standard normal distribution function (Hart's approximation)
     */
    fun stdNormalCdf(x: Double): Double {
        val sign = if (x < 0) -1.0 else 1.0
        val absX = abs(x) / sqrt(2.0)
        // Abramowitz & Stegun approximation
        val t = 1.0 / (1.0 + 0.3275911 * absX)
        val erf = 1.0 - (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t - 0.284496736) * t + 0.254829592) * t * exp(-absX * absX)
        return 0.5 * (1.0 + sign * erf)
    }

    /**
     * Standard normal probability density function
     */
    fun stdNormalPdf(x: Double): Double {
        return (1.0 / sqrt(2.0 * Math.PI)) * exp(-0.5 * x * x)
    }

    /**
     * Compute option price and Greeks using Black-76 (Black's Model for forwards/futures)
     * F: Forward/Spot price ($/GPU-hr)
     * K: Strike price ($/GPU-hr)
     * T: Time to expiry in years
     * r: Risk-free interest rate (e.g. 0.045 = 4.5%)
     * sigma: Implied volatility (e.g. 0.40 = 40%)
     */
    fun calculateBlackOption(
        type: OptionType,
        F: Double,
        K: Double,
        T: Double,
        r: Double,
        sigma: Double
    ): Pair<Double, Greeks> {
        val safeT = max(T, 0.001)
        val safeSigma = max(sigma, 0.01)
        val safeF = max(F, 0.01)
        val safeK = max(K, 0.01)

        val d1 = (ln(safeF / safeK) + (0.5 * safeSigma * safeSigma) * safeT) / (safeSigma * sqrt(safeT))
        val d2 = d1 - safeSigma * sqrt(safeT)

        val discount = exp(-r * safeT)
        val nd1 = stdNormalCdf(d1)
        val nd2 = stdNormalCdf(d2)
        val npdfD1 = stdNormalPdf(d1)

        val price: Double
        val delta: Double
        val gamma: Double = discount * npdfD1 / (safeF * safeSigma * sqrt(safeT))
        val vega: Double = safeF * discount * npdfD1 * sqrt(safeT) / 100.0 // per 1% vol
        val theta: Double
        val rho: Double

        if (type == OptionType.CALL_OPTION) {
            price = discount * (safeF * nd1 - safeK * nd2)
            delta = discount * nd1
            theta = -(safeF * discount * npdfD1 * safeSigma / (2.0 * sqrt(safeT))) - r * price
            rho = -safeT * discount * (safeF * nd1 - safeK * nd2) / 100.0
        } else {
            val nMinusD1 = stdNormalCdf(-d1)
            val nMinusD2 = stdNormalCdf(-d2)
            price = discount * (safeK * nMinusD2 - safeF * nMinusD1)
            delta = -discount * nMinusD1
            theta = -(safeF * discount * npdfD1 * safeSigma / (2.0 * sqrt(safeT))) - r * price
            rho = -safeT * discount * (safeK * nMinusD2 - safeF * nMinusD1) / 100.0
        }

        val greeks = Greeks(
            delta = delta,
            gamma = gamma,
            vega = vega,
            theta = theta / 365.0, // daily decay
            rho = rho
        )

        return Pair(max(price, 0.0), greeks)
    }

    /**
     * Probability of profit for Call / Put
     */
    fun calculateProbabilityOfProfit(
        type: OptionType,
        spot: Double,
        breakeven: Double,
        T: Double,
        sigma: Double
    ): Double {
        val safeT = max(T, 0.001)
        val safeSigma = max(sigma, 0.01)
        val d = (ln(spot / breakeven)) / (safeSigma * sqrt(safeT))
        return if (type == OptionType.CALL_OPTION) {
            stdNormalCdf(d) * 100.0
        } else {
            stdNormalCdf(-d) * 100.0
        }
    }
}
