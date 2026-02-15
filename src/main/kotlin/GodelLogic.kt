/**
 * Gödel Logic Implementation in Kotlin
 *
 * This implementation provides a complete framework for working with Gödel's many-valued logic,
 * where truth values are in the interval [0, 1] instead of just {0, 1}.
 *
 * Key Features:
 * - Truth values represented as Double in [0.0, 1.0]
 * - All logical connectives (AND, OR, NOT, IMPLIES)
 * - Formula evaluation system
 * - Multiple real-world use case examples
 */

import kotlin.math.max
import kotlin.math.min


/**
 * Core Gödel Logic operations
 */
object GodelLogic {

    /**
     * Conjunction (AND): Returns the minimum of two truth values
     * α ∧ β = min(α, β)
     */
    fun and(a: TruthValue, b: TruthValue): TruthValue =
        TruthValue(min(a.value, b.value))

    /**
     * Multiple conjunction: AND all values together
     */
    fun and(values: List<TruthValue>): TruthValue {
        require(values.isNotEmpty()) { "Cannot AND zero values" }
        return TruthValue(values.minOf { it.value })
    }

    /**
     * Multiple conjunction: AND all values together (convenience overload)
     */
    fun and(first: TruthValue, second: TruthValue, vararg rest: TruthValue): TruthValue {
        return and(listOf(first, second) + rest.toList())
    }

    /**
     * Disjunction (OR): Returns the maximum of two truth values
     * α ∨ β = max(α, β)
     */
    fun or(a: TruthValue, b: TruthValue): TruthValue =
        TruthValue(max(a.value, b.value))

    /**
     * Multiple disjunction: OR all values together
     */
    fun or(values: List<TruthValue>): TruthValue {
        require(values.isNotEmpty()) { "Cannot OR zero values" }
        return TruthValue(values.maxOf { it.value })
    }

    /**
     * Multiple disjunction: OR all values together (convenience overload)
     */
    fun or(first: TruthValue, second: TruthValue, vararg rest: TruthValue): TruthValue {
        return or(listOf(first, second) + rest.toList())
    }

    /**
     * Negation (NOT): Returns 1.0 if input is 0.0, otherwise returns 0.0
     * ¬α = 1 if α = 0, else 0
     *
     * Note: This is NOT continuous! It's a sharp cutoff.
     */
    fun not(a: TruthValue): TruthValue =
        if (a.value == 0.0) TruthValue.TRUE else TruthValue.FALSE

    /**
     * Implication: α → β
     * Returns 1.0 if α ≤ β, otherwise returns β
     *
     * Intuition: "If premise is weaker than conclusion, implication is perfect.
     * Otherwise, implication is only as good as the conclusion."
     */
    fun implies(premise: TruthValue, conclusion: TruthValue): TruthValue =
        if (premise.value <= conclusion.value) TruthValue.TRUE
        else conclusion

    /**
     * Bi-implication (equivalence): α ↔ β
     * Defined as (α → β) ∧ (β → α)
     */
    fun iff(a: TruthValue, b: TruthValue): TruthValue =
        and(implies(a, b), implies(b, a))
}

// ==================== FORMULA REPRESENTATION ====================

/**
 * Represents a logical formula that can be evaluated
 */
sealed class Formula {
    abstract fun evaluate(context: Map<String, TruthValue>): TruthValue
    abstract override fun toString(): String

    // Constant value
    data class Constant(val value: TruthValue) : Formula() {
        override fun evaluate(context: Map<String, TruthValue>) = value
        override fun toString() = value.toString()
    }

    // Variable (proposition)
    data class Variable(val name: String) : Formula() {
        override fun evaluate(context: Map<String, TruthValue>): TruthValue {
            return context[name] ?: throw IllegalArgumentException("Variable $name not found in context")
        }
        override fun toString() = name
    }

    // Negation
    data class Not(val operand: Formula) : Formula() {
        override fun evaluate(context: Map<String, TruthValue>) =
            GodelLogic.not(operand.evaluate(context))
        override fun toString() = "¬($operand)"
    }

    // Conjunction
    data class And(val operands: List<Formula>) : Formula() {
        constructor(vararg formulas: Formula) : this(formulas.toList())

        override fun evaluate(context: Map<String, TruthValue>): TruthValue {
            val values = operands.map { it.evaluate(context) }
            return GodelLogic.and(values)
        }
        override fun toString() = operands.joinToString(" ∧ ", "(", ")") { it.toString() }
    }

    // Disjunction
    data class Or(val operands: List<Formula>) : Formula() {
        constructor(vararg formulas: Formula) : this(formulas.toList())

        override fun evaluate(context: Map<String, TruthValue>): TruthValue {
            val values = operands.map { it.evaluate(context) }
            return GodelLogic.or(values)
        }
        override fun toString() = operands.joinToString(" ∨ ", "(", ")") { it.toString() }
    }

    // Implication
    data class Implies(val premise: Formula, val conclusion: Formula) : Formula() {
        override fun evaluate(context: Map<String, TruthValue>) =
            GodelLogic.implies(premise.evaluate(context), conclusion.evaluate(context))
        override fun toString() = "($premise → $conclusion)"
    }

    // Bi-implication
    data class Iff(val left: Formula, val right: Formula) : Formula() {
        override fun evaluate(context: Map<String, TruthValue>) =
            GodelLogic.iff(left.evaluate(context), right.evaluate(context))
        override fun toString() = "($left ↔ $right)"
    }
}

// ==================== DECISION SYSTEM FRAMEWORK ====================

/**
 * A decision system that uses Gödel logic with a threshold
 */
abstract class GodelDecisionSystem(val threshold: TruthValue) {

    data class Decision(
        val shouldAct: Boolean,
        val confidence: TruthValue,
        val reasoning: String
    ) {
        override fun toString(): String = buildString {
            append("Decision: ${if (shouldAct) "ACT" else "DON'T ACT"}\n")
            append("Confidence: $confidence\n")
            append("Reasoning: $reasoning")
        }
    }

    /**
     * Evaluate the decision based on inputs
     */
    abstract fun evaluate(inputs: Map<String, TruthValue>): Decision

    /**
     * Helper to create a decision based on confidence and threshold
     */
    protected fun makeDecision(confidence: TruthValue, reasoning: String): Decision {
        return Decision(
            shouldAct = confidence.value >= threshold.value,
            confidence = confidence,
            reasoning = reasoning
        )
    }
}

// ==================== EXAMPLE 1: MEDICAL DIAGNOSIS SYSTEM ====================

class MedicalDiagnosisSystem : GodelDecisionSystem(TruthValue(0.7)) {

    // Input variables
    private val fever = Formula.Variable("fever")
    private val cough = Formula.Variable("cough")
    private val fatigue = Formula.Variable("fatigue")
    private val breathShortness = Formula.Variable("breathShortness")
    private val lossOfSmell = Formula.Variable("lossOfSmell")

    // Disease hypothesis formulas
    private val covidFormula = Formula.And(fever, cough, lossOfSmell)
    private val fluFormula = Formula.And(fever, cough, fatigue)
    private val pneumoniaFormula = Formula.And(fever, cough, breathShortness)

    override fun evaluate(inputs: Map<String, TruthValue>): Decision {
        val covidScore = covidFormula.evaluate(inputs)
        val fluScore = fluFormula.evaluate(inputs)
        val pneumoniaScore = pneumoniaFormula.evaluate(inputs)

        val diagnosis = when {
            fluScore.value >= covidScore.value && fluScore.value >= pneumoniaScore.value ->
                "Flu (confidence: $fluScore)"
            covidScore.value >= pneumoniaScore.value ->
                "COVID-19 (confidence: $covidScore)"
            else ->
                "Pneumonia (confidence: $pneumoniaScore)"
        }

        val maxScore = maxOf(covidScore.value, fluScore.value, pneumoniaScore.value)

        return makeDecision(
            TruthValue(maxScore),
            """
            Primary diagnosis: $diagnosis
            COVID-19 score: $covidScore
            Flu score: $fluScore
            Pneumonia score: $pneumoniaScore
            
            ${if (maxScore >= threshold.value)
                "Confidence exceeds threshold. Recommend treatment."
            else
                "Confidence below threshold. Recommend additional tests."}
            """.trimIndent()
        )
    }
}

// ==================== EXAMPLE 2: AUTONOMOUS VEHICLE LANE CHANGE ====================

class LaneChangeDecisionSystem : GodelDecisionSystem(TruthValue(0.85)) {

    // Safety factors
    private val leftLaneClear = Formula.Variable("leftLaneClear")
    private val safeFollowingDistance = Formula.Variable("safeFollowingDistance")
    private val turnSignalOn = Formula.Variable("turnSignalOn")
    private val noBlindSpot = Formula.Variable("noBlindSpot")
    private val roadConditionGood = Formula.Variable("roadConditionGood")

    // Overall safety formula: ALL conditions must be met
    private val safetyFormula = Formula.And(
        leftLaneClear,
        safeFollowingDistance,
        turnSignalOn,
        noBlindSpot,
        roadConditionGood
    )

    override fun evaluate(inputs: Map<String, TruthValue>): Decision {
        val safetyScore = safetyFormula.evaluate(inputs)

        return makeDecision(
            safetyScore,
            """
            Safety analysis:
            - Left lane clear: ${inputs["leftLaneClear"]}
            - Safe following distance: ${inputs["safeFollowingDistance"]}
            - Turn signal on: ${inputs["turnSignalOn"]}
            - No blind spot vehicle: ${inputs["noBlindSpot"]}
            - Road condition good: ${inputs["roadConditionGood"]}
            
            Overall safety score: $safetyScore (minimum of all factors)
            Required threshold: $threshold
            
            ${if (safetyScore.value >= threshold.value)
                "✓ SAFE TO CHANGE LANES"
            else
                "✗ NOT SAFE - Maintain current lane"}
            """.trimIndent()
        )
    }
}

// ==================== EXAMPLE 3: CREDIT RISK ASSESSMENT ====================

class CreditRiskAssessment : GodelDecisionSystem(TruthValue(0.75)) {

    private val goodCreditScore = Formula.Variable("goodCreditScore")
    private val stableEmployment = Formula.Variable("stableEmployment")
    private val sufficientIncome = Formula.Variable("sufficientIncome")
    private val lowDebtRatio = Formula.Variable("lowDebtRatio")
    private val hasCollateral = Formula.Variable("hasCollateral")

    // Primary qualification: credit AND employment AND income
    private val primaryQualification = Formula.And(
        goodCreditScore,
        stableEmployment,
        sufficientIncome
    )

    // Alternative: strong collateral can compensate
    private val overallQualification = Formula.Or(
        primaryQualification,
        hasCollateral
    )

    // Must also have acceptable debt ratio
    private val finalScore = Formula.And(
        overallQualification,
        lowDebtRatio
    )

    override fun evaluate(inputs: Map<String, TruthValue>): Decision {
        val primaryScore = primaryQualification.evaluate(inputs)
        val overallScore = overallQualification.evaluate(inputs)
        val finalConfidence = finalScore.evaluate(inputs)

        return makeDecision(
            finalConfidence,
            """
            Credit Assessment:
            - Credit score quality: ${inputs["goodCreditScore"]}
            - Employment stability: ${inputs["stableEmployment"]}
            - Income sufficiency: ${inputs["sufficientIncome"]}
            - Debt ratio: ${inputs["lowDebtRatio"]}
            - Collateral: ${inputs["hasCollateral"]}
            
            Primary qualification score: $primaryScore
            With collateral consideration: $overallScore
            Final score (including debt ratio): $finalConfidence
            
            ${when {
                finalConfidence.value >= 0.85 -> "✓ APPROVE - Strong candidate"
                finalConfidence.value >= threshold.value -> "✓ APPROVE - Acceptable risk"
                finalConfidence.value >= 0.6 -> "⚠ CONDITIONAL - Request additional documentation"
                else -> "✗ DENY - Risk too high"
            }}
            """.trimIndent()
        )
    }
}

// ==================== EXAMPLE 4: SMART HOME HEATING SYSTEM ====================

class SmartHeatingSystem : GodelDecisionSystem(TruthValue(0.6)) {

    private val tempLow = Formula.Variable("tempLow")
    private val someoneHome = Formula.Variable("someoneHome")
    private val nightTime = Formula.Variable("nightTime")
    private val energyCostAcceptable = Formula.Variable("energyCostAcceptable")
    private val weatherCold = Formula.Variable("weatherCold")

    // Basic need: cold AND occupied
    private val basicNeed = Formula.And(tempLow, someoneHome)

    // Enhanced need: basic need AND (night OR very cold weather)
    private val enhancedNeed = Formula.And(
        basicNeed,
        Formula.Or(nightTime, weatherCold)
    )

    // Final decision: consider energy cost
    private val heatingDecision = Formula.And(enhancedNeed, energyCostAcceptable)

    override fun evaluate(inputs: Map<String, TruthValue>): Decision {
        val basicScore = basicNeed.evaluate(inputs)
        val enhancedScore = enhancedNeed.evaluate(inputs)
        val finalScore = heatingDecision.evaluate(inputs)

        val priority = when {
            finalScore.value >= 0.8 -> "HIGH"
            finalScore.value >= 0.6 -> "MEDIUM"
            finalScore.value >= 0.4 -> "LOW"
            else -> "NONE"
        }

        return makeDecision(
            finalScore,
            """
            Heating Analysis:
            - Temperature low: ${inputs["tempLow"]}
            - Someone home: ${inputs["someoneHome"]}
            - Night time: ${inputs["nightTime"]}
            - Weather cold: ${inputs["weatherCold"]}
            - Energy cost acceptable: ${inputs["energyCostAcceptable"]}
            
            Basic heating need: $basicScore
            Enhanced need (time/weather): $enhancedScore
            Final decision (with cost): $finalScore
            Priority: $priority
            
            ${if (finalScore.value >= threshold.value)
                "🔥 TURN ON HEATING"
            else
                "❄️  Keep heating off or reduce temperature"}
            """.trimIndent()
        )
    }
}

// ==================== EXAMPLE 5: CONTENT MODERATION SYSTEM ====================

class ContentModerationSystem : GodelDecisionSystem(TruthValue(0.75)) {

    private val offensiveLanguage = Formula.Variable("offensiveLanguage")
    private val userViolationHistory = Formula.Variable("userViolationHistory")
    private val multipleReports = Formula.Variable("multipleReports")
    private val harassmentContext = Formula.Variable("harassmentContext")
    private val threatOfViolence = Formula.Variable("threatOfViolence")

    // Direct violation: offensive AND harassment
    private val directViolation = Formula.And(offensiveLanguage, harassmentContext)

    // Severe violation: threats (automatic high score)
    private val severeViolation = threatOfViolence

    // Community concern: multiple reports
    private val communityConcern = multipleReports

    // Overall action score
    private val actionScore = Formula.Or(
        directViolation,
        severeViolation,
        communityConcern,
        userViolationHistory
    )

    override fun evaluate(inputs: Map<String, TruthValue>): Decision {
        val directScore = directViolation.evaluate(inputs)
        val severeScore = severeViolation.evaluate(inputs)
        val communityScore = communityConcern.evaluate(inputs)
        val finalScore = actionScore.evaluate(inputs)

        val action = when {
            finalScore.value >= 0.9 -> "REMOVE + BAN USER"
            finalScore.value >= 0.75 -> "REMOVE CONTENT"
            finalScore.value >= 0.5 -> "REVIEW BY HUMAN MODERATOR"
            else -> "NO ACTION NEEDED"
        }

        return makeDecision(
            finalScore,
            """
            Content Moderation Analysis:
            - Offensive language: ${inputs["offensiveLanguage"]}
            - Harassment context: ${inputs["harassmentContext"]}
            - Threat of violence: ${inputs["threatOfViolence"]}
            - User violation history: ${inputs["userViolationHistory"]}
            - Multiple reports: ${inputs["multipleReports"]}
            
            Direct violation score: $directScore
            Severe violation score: $severeScore
            Community concern score: $communityScore
            Final action score: $finalScore
            
            Recommended action: $action
            """.trimIndent()
        )
    }
}

// ==================== EXAMPLE 6: E-COMMERCE RECOMMENDATION ====================

class ProductRecommendationSystem : GodelDecisionSystem(TruthValue(0.7)) {

    private val viewedSimilar = Formula.Variable("viewedSimilar")
    private val priceRangeMatch = Formula.Variable("priceRangeMatch")
    private val highRating = Formula.Variable("highRating")
    private val inStock = Formula.Variable("inStock")
    private val trendingProduct = Formula.Variable("trendingProduct")

    // User interest: viewed similar AND price matches
    private val userInterest = Formula.And(viewedSimilar, priceRangeMatch)

    // Product quality: high rating OR trending
    private val productQuality = Formula.Or(highRating, trendingProduct)

    // Recommendation score: (interest OR quality) AND available
    private val recommendationScore = Formula.And(
        Formula.Or(userInterest, productQuality),
        inStock
    )

    override fun evaluate(inputs: Map<String, TruthValue>): Decision {
        val interestScore = userInterest.evaluate(inputs)
        val qualityScore = productQuality.evaluate(inputs)
        val finalScore = recommendationScore.evaluate(inputs)

        val strength = when {
            finalScore.value >= 0.9 -> "VERY STRONG"
            finalScore.value >= 0.7 -> "STRONG"
            finalScore.value >= 0.5 -> "MODERATE"
            else -> "WEAK"
        }

        return makeDecision(
            finalScore,
            """
            Recommendation Analysis:
            - Viewed similar items: ${inputs["viewedSimilar"]}
            - Price range matches: ${inputs["priceRangeMatch"]}
            - High product rating: ${inputs["highRating"]}
            - Trending product: ${inputs["trendingProduct"]}
            - In stock: ${inputs["inStock"]}
            
            User interest score: $interestScore
            Product quality score: $qualityScore
            Final recommendation score: $finalScore
            Recommendation strength: $strength
            
            ${if (finalScore.value >= threshold.value)
                "👍 RECOMMEND THIS PRODUCT"
            else
                "👎 Skip this product"}
            """.trimIndent()
        )
    }
}

// ==================== TESTING AND DEMONSTRATION ====================

fun main() {
    println("=" .repeat(80))
    println("GÖDEL LOGIC IMPLEMENTATION IN KOTLIN")
    println("=" .repeat(80))
    println()

    // Test basic operations
    demonstrateBasicOperations()
    println("\n" + "=".repeat(80) + "\n")

    // Test all use cases
    demonstrateMedicalDiagnosis()
    println("\n" + "=".repeat(80) + "\n")

    demonstrateAutonomousVehicle()
    println("\n" + "=".repeat(80) + "\n")

    demonstrateCreditRisk()
    println("\n" + "=".repeat(80) + "\n")

    demonstrateSmartHome()
    println("\n" + "=".repeat(80) + "\n")

    demonstrateContentModeration()
    println("\n" + "=".repeat(80) + "\n")

    demonstrateRecommendation()
    println("\n" + "=".repeat(80) + "\n")

    demonstrateFormulaEvaluation()
}

fun demonstrateBasicOperations() {
    println("BASIC GÖDEL LOGIC OPERATIONS")
    println("-".repeat(80))

    val a = TruthValue(0.7)
    val b = TruthValue(0.9)
    val c = TruthValue(0.0)
    val d = TruthValue(0.5)

    println("Values: a=0.7, b=0.9, c=0.0, d=0.5")
    println()
    println("AND (conjunction - minimum):")
    println("  a ∧ b = ${GodelLogic.and(a, b)}")
    println("  a ∧ d = ${GodelLogic.and(a, d)}")
    println("  a ∧ b ∧ d = ${GodelLogic.and(a, b, d)}")
    println()
    println("OR (disjunction - maximum):")
    println("  a ∨ b = ${GodelLogic.or(a, b)}")
    println("  a ∨ d = ${GodelLogic.or(a, d)}")
    println("  a ∨ b ∨ d = ${GodelLogic.or(a, b, d)}")
    println()
    println("NOT (negation - sharp cutoff at 0):")
    println("  ¬c = ${GodelLogic.not(c)}")
    println("  ¬a = ${GodelLogic.not(a)}")
    println("  ¬d = ${GodelLogic.not(d)}")
    println()
    println("IMPLIES (implication):")
    println("  a → b = ${GodelLogic.implies(a, b)} (0.7 ≤ 0.9, so result is 1.0)")
    println("  b → a = ${GodelLogic.implies(b, a)} (0.9 > 0.7, so result is 0.7)")
    println("  a → d = ${GodelLogic.implies(a, d)} (0.7 > 0.5, so result is 0.5)")
}

fun demonstrateMedicalDiagnosis() {
    println("EXAMPLE 1: MEDICAL DIAGNOSIS SYSTEM")
    println("-".repeat(80))

    val system = MedicalDiagnosisSystem()

    println("Scenario 1: Strong Flu indicators")
    val inputs1 = mapOf(
        "fever" to TruthValue(0.7),
        "cough" to TruthValue(0.8),
        "fatigue" to TruthValue(0.9),
        "breathShortness" to TruthValue(0.5),
        "lossOfSmell" to TruthValue(0.3)
    )
    println(system.evaluate(inputs1))

    println("\n" + "-".repeat(80) + "\n")

    println("Scenario 2: Possible COVID-19")
    val inputs2 = mapOf(
        "fever" to TruthValue(0.8),
        "cough" to TruthValue(0.7),
        "fatigue" to TruthValue(0.6),
        "breathShortness" to TruthValue(0.4),
        "lossOfSmell" to TruthValue(0.9)
    )
    println(system.evaluate(inputs2))
}

fun demonstrateAutonomousVehicle() {
    println("EXAMPLE 2: AUTONOMOUS VEHICLE LANE CHANGE")
    println("-".repeat(80))

    val system = LaneChangeDecisionSystem()

    println("Scenario 1: Safe to change lanes")
    val inputs1 = mapOf(
        "leftLaneClear" to TruthValue(0.95),
        "safeFollowingDistance" to TruthValue(0.92),
        "turnSignalOn" to TruthValue(1.0),
        "noBlindSpot" to TruthValue(0.88),
        "roadConditionGood" to TruthValue(0.9)
    )
    println(system.evaluate(inputs1))

    println("\n" + "-".repeat(80) + "\n")

    println("Scenario 2: Unsafe - blind spot vehicle detected")
    val inputs2 = mapOf(
        "leftLaneClear" to TruthValue(0.85),
        "safeFollowingDistance" to TruthValue(0.90),
        "turnSignalOn" to TruthValue(1.0),
        "noBlindSpot" to TruthValue(0.4),  // Low confidence!
        "roadConditionGood" to TruthValue(0.95)
    )
    println(system.evaluate(inputs2))
}

fun demonstrateCreditRisk() {
    println("EXAMPLE 3: CREDIT RISK ASSESSMENT")
    println("-".repeat(80))

    val system = CreditRiskAssessment()

    println("Scenario 1: Strong candidate - approve")
    val inputs1 = mapOf(
        "goodCreditScore" to TruthValue(0.95),
        "stableEmployment" to TruthValue(0.9),
        "sufficientIncome" to TruthValue(0.85),
        "lowDebtRatio" to TruthValue(0.8),
        "hasCollateral" to TruthValue(0.7)
    )
    println(system.evaluate(inputs1))

    println("\n" + "-".repeat(80) + "\n")

    println("Scenario 2: Borderline case - needs review")
    val inputs2 = mapOf(
        "goodCreditScore" to TruthValue(0.75),
        "stableEmployment" to TruthValue(0.6),
        "sufficientIncome" to TruthValue(0.7),
        "lowDebtRatio" to TruthValue(0.65),
        "hasCollateral" to TruthValue(0.5)
    )
    println(system.evaluate(inputs2))
}

fun demonstrateSmartHome() {
    println("EXAMPLE 4: SMART HOME HEATING SYSTEM")
    println("-".repeat(80))

    val system = SmartHeatingSystem()

    println("Scenario 1: Cold night, off-peak energy - turn on heating")
    val inputs1 = mapOf(
        "tempLow" to TruthValue(0.8),
        "someoneHome" to TruthValue(0.9),
        "nightTime" to TruthValue(1.0),
        "energyCostAcceptable" to TruthValue(0.9),
        "weatherCold" to TruthValue(0.7)
    )
    println(system.evaluate(inputs1))

    println("\n" + "-".repeat(80) + "\n")

    println("Scenario 2: Peak pricing - delay heating")
    val inputs2 = mapOf(
        "tempLow" to TruthValue(0.7),
        "someoneHome" to TruthValue(0.8),
        "nightTime" to TruthValue(0.5),
        "energyCostAcceptable" to TruthValue(0.3),  // High cost!
        "weatherCold" to TruthValue(0.6)
    )
    println(system.evaluate(inputs2))
}

fun demonstrateContentModeration() {
    println("EXAMPLE 5: CONTENT MODERATION SYSTEM")
    println("-".repeat(80))

    val system = ContentModerationSystem()

    println("Scenario 1: Clear violation - remove content")
    val inputs1 = mapOf(
        "offensiveLanguage" to TruthValue(0.9),
        "harassmentContext" to TruthValue(0.85),
        "threatOfViolence" to TruthValue(0.3),
        "userViolationHistory" to TruthValue(0.6),
        "multipleReports" to TruthValue(0.8)
    )
    println(system.evaluate(inputs1))

    println("\n" + "-".repeat(80) + "\n")

    println("Scenario 2: Likely satire - keep content")
    val inputs2 = mapOf(
        "offensiveLanguage" to TruthValue(0.5),
        "harassmentContext" to TruthValue(0.2),
        "threatOfViolence" to TruthValue(0.1),
        "userViolationHistory" to TruthValue(0.1),
        "multipleReports" to TruthValue(0.2)
    )
    println(system.evaluate(inputs2))
}

fun demonstrateRecommendation() {
    println("EXAMPLE 6: E-COMMERCE PRODUCT RECOMMENDATION")
    println("-".repeat(80))

    val system = ProductRecommendationSystem()

    println("Scenario 1: Strong match - recommend")
    val inputs1 = mapOf(
        "viewedSimilar" to TruthValue(0.95),
        "priceRangeMatch" to TruthValue(0.85),
        "highRating" to TruthValue(0.9),
        "trendingProduct" to TruthValue(0.7),
        "inStock" to TruthValue(1.0)
    )
    println(system.evaluate(inputs1))

    println("\n" + "-".repeat(80) + "\n")

    println("Scenario 2: Weak match - skip")
    val inputs2 = mapOf(
        "viewedSimilar" to TruthValue(0.3),
        "priceRangeMatch" to TruthValue(0.5),
        "highRating" to TruthValue(0.6),
        "trendingProduct" to TruthValue(0.4),
        "inStock" to TruthValue(1.0)
    )
    println(system.evaluate(inputs2))
}

fun demonstrateFormulaEvaluation() {
    println("ADVANCED: FORMULA EVALUATION SYSTEM")
    println("-".repeat(80))

    // Create complex formula: (A ∧ B) ∨ (C → D)
    val a = Formula.Variable("A")
    val b = Formula.Variable("B")
    val c = Formula.Variable("C")
    val d = Formula.Variable("D")

    val formula = Formula.Or(
        Formula.And(a, b),
        Formula.Implies(c, d)
    )

    println("Formula: $formula")
    println()

    val context = mapOf(
        "A" to TruthValue(0.8),
        "B" to TruthValue(0.6),
        "C" to TruthValue(0.9),
        "D" to TruthValue(0.7)
    )

    println("Context: A=0.8, B=0.6, C=0.9, D=0.7")
    println()
    println("Step-by-step evaluation:")
    println("  A ∧ B = min(0.8, 0.6) = 0.6")
    println("  C → D = (0.9 > 0.7, so result is D) = 0.7")
    println("  (A ∧ B) ∨ (C → D) = max(0.6, 0.7) = 0.7")
    println()
    println("Result: ${formula.evaluate(context)}")
}
