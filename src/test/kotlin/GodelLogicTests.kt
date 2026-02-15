/**
 * Unit Tests and Additional Utilities for Gödel Logic Implementation
 */

import kotlin.test.*

// ==================== UTILITY: TRUTH VALUE BUILDER ====================

/**
 * Builder for creating truth values with percentage notation
 */
fun percent(value: Int): TruthValue {
    require(value in 0..100) { "Percentage must be in range [0, 100], got $value" }
    return TruthValue(value / 100.0)
}

fun percent(value: Double): TruthValue {
    require(value in 0.0..100.0) { "Percentage must be in range [0.0, 100.0], got $value" }
    return TruthValue(value / 100.0)
}

// ==================== UTILITY: DSL FOR FORMULAS ====================

/**
 * DSL for creating formulas more naturally
 */
infix fun Formula.and(other: Formula): Formula = Formula.And(this, other)
infix fun Formula.or(other: Formula): Formula = Formula.Or(this, other)
infix fun Formula.implies(other: Formula): Formula = Formula.Implies(this, other)
infix fun Formula.iff(other: Formula): Formula = Formula.Iff(this, other)
operator fun Formula.not(): Formula = Formula.Not(this)

fun variable(name: String): Formula = Formula.Variable(name)
fun constant(value: TruthValue): Formula = Formula.Constant(value)
fun constant(value: Double): Formula = Formula.Constant(TruthValue(value))

// ==================== UTILITY: CONTEXT BUILDER ====================

/**
 * Builder for creating evaluation contexts
 */
class ContextBuilder {
    private val map = mutableMapOf<String, TruthValue>()
    
    infix fun String.means(value: TruthValue) {
        map[this] = value
    }
    
    infix fun String.means(value: Double) {
        map[this] = TruthValue(value)
    }
    
    infix fun String.means(value: Int) {
        map[this] = percent(value)
    }
    
    fun build(): Map<String, TruthValue> = map.toMap()
}

fun context(builder: ContextBuilder.() -> Unit): Map<String, TruthValue> {
    return ContextBuilder().apply(builder).build()
}

// ==================== UTILITY: TRUTH TABLE GENERATOR ====================

/**
 * Generates truth tables for formulas
 */
class TruthTableGenerator {
    
    data class TruthTableRow(
        val inputs: Map<String, TruthValue>,
        val output: TruthValue
    )
    
    /**
     * Generate truth table for classical (binary) values
     */
    fun generateBinaryTable(formula: Formula, variables: List<String>): List<TruthTableRow> {
        val rows = mutableListOf<TruthTableRow>()
        val n = variables.size
        val combinations = 1 shl n // 2^n
        
        for (i in 0 until combinations) {
            val inputs = mutableMapOf<String, TruthValue>()
            for (j in variables.indices) {
                val bit = (i shr j) and 1
                inputs[variables[j]] = if (bit == 1) TruthValue.TRUE else TruthValue.FALSE
            }
            val output = formula.evaluate(inputs)
            rows.add(TruthTableRow(inputs, output))
        }
        
        return rows
    }
    
    /**
     * Generate truth table for selected truth values
     */
    fun generateTable(
        formula: Formula, 
        variables: List<String>,
        truthValues: List<TruthValue>
    ): List<TruthTableRow> {
        val rows = mutableListOf<TruthTableRow>()
        
        fun generateCombinations(
            index: Int,
            current: MutableMap<String, TruthValue>
        ) {
            if (index == variables.size) {
                val output = formula.evaluate(current)
                rows.add(TruthTableRow(current.toMap(), output))
                return
            }
            
            for (value in truthValues) {
                current[variables[index]] = value
                generateCombinations(index + 1, current)
            }
        }
        
        generateCombinations(0, mutableMapOf())
        return rows
    }
    
    fun printTable(rows: List<TruthTableRow>, variables: List<String>, formulaStr: String) {
        // Header
        val header = variables.joinToString(" | ") { it.padEnd(6) } + " | Result"
        println(header)
        println("-".repeat(header.length))
        
        // Rows
        for (row in rows) {
            val inputStr = variables.joinToString(" | ") { 
                row.inputs[it].toString().padEnd(6) 
            }
            println("$inputStr | ${row.output}")
        }
        println()
        println("Formula: $formulaStr")
    }
}

// ==================== UNIT TESTS ====================

class GodelLogicTest {
    
    @Test
    fun testTruthValueValidation() {
        // Valid values
        assertNotNull(TruthValue(0.0))
        assertNotNull(TruthValue(0.5))
        assertNotNull(TruthValue(1.0))
        
        // Invalid values should throw
        assertFailsWith<IllegalArgumentException> { TruthValue(-0.1) }
        assertFailsWith<IllegalArgumentException> { TruthValue(1.1) }
    }
    
    @Test
    fun testConjunction() {
        assertEquals(0.3, GodelLogic.and(TruthValue(0.3), TruthValue(0.7)).value, 0.001)
        assertEquals(0.5, GodelLogic.and(TruthValue(0.5), TruthValue(0.5)).value, 0.001)
        assertEquals(0.0, GodelLogic.and(TruthValue(0.0), TruthValue(1.0)).value, 0.001)
        
        // Multiple operands (using vararg overload)
        assertEquals(0.2, GodelLogic.and(
            TruthValue(0.5),
            TruthValue(0.2),
            TruthValue(0.8)
        ).value, 0.001)
        
        // Multiple operands (using List)
        assertEquals(0.2, GodelLogic.and(
            listOf(TruthValue(0.5), TruthValue(0.2), TruthValue(0.8))
        ).value, 0.001)
    }
    
    @Test
    fun testDisjunction() {
        assertEquals(0.7, GodelLogic.or(TruthValue(0.3), TruthValue(0.7)).value, 0.001)
        assertEquals(0.5, GodelLogic.or(TruthValue(0.5), TruthValue(0.5)).value, 0.001)
        assertEquals(1.0, GodelLogic.or(TruthValue(0.0), TruthValue(1.0)).value, 0.001)
        
        // Multiple operands (using vararg overload)
        assertEquals(0.9, GodelLogic.or(
            TruthValue(0.5),
            TruthValue(0.2),
            TruthValue(0.9)
        ).value, 0.001)
        
        // Multiple operands (using List)
        assertEquals(0.9, GodelLogic.or(
            listOf(TruthValue(0.5), TruthValue(0.2), TruthValue(0.9))
        ).value, 0.001)
    }
    
    @Test
    fun testNegation() {
        // Only 0.0 negates to 1.0
        assertEquals(1.0, GodelLogic.not(TruthValue(0.0)).value, 0.001)
        
        // Everything else negates to 0.0
        assertEquals(0.0, GodelLogic.not(TruthValue(0.001)).value, 0.001)
        assertEquals(0.0, GodelLogic.not(TruthValue(0.5)).value, 0.001)
        assertEquals(0.0, GodelLogic.not(TruthValue(0.999)).value, 0.001)
        assertEquals(0.0, GodelLogic.not(TruthValue(1.0)).value, 0.001)
    }
    
    @Test
    fun testImplication() {
        // When premise ≤ conclusion, result is 1.0
        assertEquals(1.0, GodelLogic.implies(TruthValue(0.3), TruthValue(0.7)).value, 0.001)
        assertEquals(1.0, GodelLogic.implies(TruthValue(0.5), TruthValue(0.5)).value, 0.001)
        assertEquals(1.0, GodelLogic.implies(TruthValue(0.0), TruthValue(0.5)).value, 0.001)
        
        // When premise > conclusion, result is conclusion
        assertEquals(0.3, GodelLogic.implies(TruthValue(0.7), TruthValue(0.3)).value, 0.001)
        assertEquals(0.5, GodelLogic.implies(TruthValue(0.9), TruthValue(0.5)).value, 0.001)
        assertEquals(0.0, GodelLogic.implies(TruthValue(1.0), TruthValue(0.0)).value, 0.001)
    }
    
    @Test
    fun testDoubleNegationNotTautology() {
        // In classical logic: ¬¬α ↔ α is a tautology
        // In Gödel logic: it's NOT a tautology
        
        val alpha = TruthValue(0.5)
        val notAlpha = GodelLogic.not(alpha)
        val notNotAlpha = GodelLogic.not(notAlpha)
        
        assertEquals(0.0, notAlpha.value, 0.001)
        assertEquals(1.0, notNotAlpha.value, 0.001)
        
        // α ≠ ¬¬α when α ∈ (0, 1)
        assertNotEquals(alpha.value, notNotAlpha.value)
    }
    
    @Test
    fun testExcludedMiddleNotTautology() {
        // In classical logic: α ∨ ¬α = 1 always (tautology)
        // In Gödel logic: α ∨ ¬α may not be 1
        
        val alpha = TruthValue(0.5)
        val notAlpha = GodelLogic.not(alpha)
        val excludedMiddle = GodelLogic.or(alpha, notAlpha)
        
        assertEquals(0.0, notAlpha.value, 0.001)
        assertEquals(0.5, excludedMiddle.value, 0.001)
        
        // Not a tautology!
        assertNotEquals(1.0, excludedMiddle.value)
    }
    
    @Test
    fun testPrelinearityIsTautology() {
        // (α → β) ∨ (β → α) should always equal 1.0
        
        val testCases = listOf(
            Pair(0.3, 0.7),
            Pair(0.7, 0.3),
            Pair(0.5, 0.5),
            Pair(0.0, 1.0),
            Pair(1.0, 0.0),
            Pair(0.2, 0.8)
        )
        
        for ((a, b) in testCases) {
            val alpha = TruthValue(a)
            val beta = TruthValue(b)
            
            val alphaImpliesBeta = GodelLogic.implies(alpha, beta)
            val betaImpliesAlpha = GodelLogic.implies(beta, alpha)
            val prelinearity = GodelLogic.or(alphaImpliesBeta, betaImpliesAlpha)
            
            assertEquals(1.0, prelinearity.value, 0.001, 
                "Prelinearity failed for α=$a, β=$b")
        }
    }
    
    @Test
    fun testFormulaEvaluation() {
        val a = Formula.Variable("A")
        val b = Formula.Variable("B")
        val formula = Formula.And(a, b)
        
        val ctx = mapOf(
            "A" to TruthValue(0.6),
            "B" to TruthValue(0.8)
        )
        
        val result = formula.evaluate(ctx)
        assertEquals(0.6, result.value, 0.001)
    }
    
    @Test
    fun testComplexFormula() {
        // (A ∧ B) ∨ (C → D)
        val a = Formula.Variable("A")
        val b = Formula.Variable("B")
        val c = Formula.Variable("C")
        val d = Formula.Variable("D")
        
        val formula = Formula.Or(
            Formula.And(a, b),
            Formula.Implies(c, d)
        )
        
        val ctx = mapOf(
            "A" to TruthValue(0.8),
            "B" to TruthValue(0.6),
            "C" to TruthValue(0.9),
            "D" to TruthValue(0.7)
        )
        
        // A ∧ B = min(0.8, 0.6) = 0.6
        // C → D = 0.7 (since 0.9 > 0.7)
        // (A ∧ B) ∨ (C → D) = max(0.6, 0.7) = 0.7
        
        val result = formula.evaluate(ctx)
        assertEquals(0.7, result.value, 0.001)
    }
    
    @Test
    fun testDSL() {
        val a = variable("A")
        val b = variable("B")
        val c = variable("C")
        
        // Using DSL
        val formula1 = a and b or c
        val formula2 = (a implies b) and !c
        
        val ctx = mapOf(
            "A" to TruthValue(0.7),
            "B" to TruthValue(0.5),
            "C" to TruthValue(0.3)
        )
        
        assertNotNull(formula1.evaluate(ctx))
        assertNotNull(formula2.evaluate(ctx))
    }
    
    @Test
    fun testContextBuilder() {
        val ctx = context {
            "temperature" means 0.8
            "humidity" means 75  // percentage
            "pressure" means TruthValue(0.9)
        }
        
        assertEquals(0.8, ctx["temperature"]?.value)
        assertEquals(0.75, ctx["humidity"]?.value)
        assertEquals(0.9, ctx["pressure"]?.value)
    }
}

// ==================== CLASSICAL VS GÖDEL COMPARISON ====================

class ClassicalVsGodelComparison {
    
    fun compareExcludedMiddle() {
        println("COMPARISON: Law of Excluded Middle (α ∨ ¬α)")
        println("-".repeat(80))
        
        val generator = TruthTableGenerator()
        val formula = Formula.Or(
            Formula.Variable("α"),
            Formula.Not(Formula.Variable("α"))
        )
        
        println("Classical logic (binary truth values):")
        val binaryTable = generator.generateBinaryTable(formula, listOf("α"))
        generator.printTable(binaryTable, listOf("α"), "α ∨ ¬α")
        
        println("\n" + "-".repeat(80) + "\n")
        
        println("Gödel logic (multiple truth values):")
        val godelValues = listOf(
            TruthValue(0.0),
            TruthValue(0.25),
            TruthValue(0.5),
            TruthValue(0.75),
            TruthValue(1.0)
        )
        val godelTable = generator.generateTable(formula, listOf("α"), godelValues)
        generator.printTable(godelTable, listOf("α"), "α ∨ ¬α")
        
        println("\nObservation: In Gödel logic, α ∨ ¬α is NOT always 1.0!")
        println("For α ∈ (0, 1): ¬α = 0, so α ∨ ¬α = α < 1.0")
    }
    
    fun compareDoubleNegation() {
        println("\nCOMPARISON: Double Negation (¬¬α → α)")
        println("-".repeat(80))
        
        val generator = TruthTableGenerator()
        val alpha = Formula.Variable("α")
        val notNotAlpha = Formula.Not(Formula.Not(alpha))
        val formula = Formula.Implies(notNotAlpha, alpha)
        
        println("Classical logic (binary truth values):")
        val binaryTable = generator.generateBinaryTable(formula, listOf("α"))
        generator.printTable(binaryTable, listOf("α"), "¬¬α → α")
        
        println("\n" + "-".repeat(80) + "\n")
        
        println("Gödel logic (multiple truth values):")
        val godelValues = listOf(
            TruthValue(0.0),
            TruthValue(0.25),
            TruthValue(0.5),
            TruthValue(0.75),
            TruthValue(1.0)
        )
        val godelTable = generator.generateTable(formula, listOf("α"), godelValues)
        generator.printTable(godelTable, listOf("α"), "¬¬α → α")
        
        println("\nObservation: In Gödel logic, ¬¬α → α is NOT always 1.0!")
        println("For α ∈ (0, 1): ¬¬α = 1, so (¬¬α → α) = α < 1.0")
    }
    
    fun comparePrelinearity() {
        println("\nCOMPARISON: Prelinearity ((α → β) ∨ (β → α))")
        println("-".repeat(80))
        
        val generator = TruthTableGenerator()
        val alpha = Formula.Variable("α")
        val beta = Formula.Variable("β")
        val formula = Formula.Or(
            Formula.Implies(alpha, beta),
            Formula.Implies(beta, alpha)
        )
        
        println("Both classical and Gödel logic (selected values):")
        val values = listOf(
            TruthValue(0.0),
            TruthValue(0.5),
            TruthValue(1.0)
        )
        val table = generator.generateTable(formula, listOf("α", "β"), values)
        generator.printTable(table, listOf("α", "β"), "(α → β) ∨ (β → α)")
        
        println("\nObservation: Prelinearity IS a tautology in both logics!")
        println("This is because one of the implications is always perfect (= 1.0)")
    }
}

// ==================== DEMONSTRATION RUNNER ====================

fun runTests() {
    println("=" .repeat(80))
    println("RUNNING UNIT TESTS")
    println("=" .repeat(80))
    println()
    
    val test = GodelLogicTest()
    
    try {
        println("Testing truth value validation...")
        test.testTruthValueValidation()
        println("✓ PASSED\n")
        
        println("Testing conjunction...")
        test.testConjunction()
        println("✓ PASSED\n")
        
        println("Testing disjunction...")
        test.testDisjunction()
        println("✓ PASSED\n")
        
        println("Testing negation...")
        test.testNegation()
        println("✓ PASSED\n")
        
        println("Testing implication...")
        test.testImplication()
        println("✓ PASSED\n")
        
        println("Testing double negation (NOT a tautology)...")
        test.testDoubleNegationNotTautology()
        println("✓ PASSED\n")
        
        println("Testing excluded middle (NOT a tautology)...")
        test.testExcludedMiddleNotTautology()
        println("✓ PASSED\n")
        
        println("Testing prelinearity (IS a tautology)...")
        test.testPrelinearityIsTautology()
        println("✓ PASSED\n")
        
        println("Testing formula evaluation...")
        test.testFormulaEvaluation()
        println("✓ PASSED\n")
        
        println("Testing complex formula...")
        test.testComplexFormula()
        println("✓ PASSED\n")
        
        println("Testing DSL...")
        test.testDSL()
        println("✓ PASSED\n")
        
        println("Testing context builder...")
        test.testContextBuilder()
        println("✓ PASSED\n")
        
        println("=" .repeat(80))
        println("ALL TESTS PASSED!")
        println("=" .repeat(80))
        
    } catch (e: Exception) {
        println("✗ FAILED: ${e.message}")
        e.printStackTrace()
    }
}

fun runComparisons() {
    println("\n\n")
    println("=" .repeat(80))
    println("CLASSICAL LOGIC VS GÖDEL LOGIC COMPARISONS")
    println("=" .repeat(80))
    println()
    
    val comparison = ClassicalVsGodelComparison()
    
    comparison.compareExcludedMiddle()
    println("\n" + "=" .repeat(80) + "\n")
    
    comparison.compareDoubleNegation()
    println("\n" + "=" .repeat(80) + "\n")
    
    comparison.comparePrelinearity()
}

// ==================== EXTENDED MAIN ====================

fun extendedMain() {
    // Run tests
    runTests()
    
    // Run comparisons
    runComparisons()
    
    // Run original demonstrations
    println("\n\n")
    main()
}
