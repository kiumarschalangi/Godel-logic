# Gödel Logic API Reference

## Core Types

### TruthValue
```kotlin
// Creation
val v1 = TruthValue(0.7)           // Direct construction
val v2 = TruthValue.TRUE            // 1.0
val v3 = TruthValue.FALSE           // 0.0
val v4 = TruthValue.of(1.5)         // Coerces to 1.0
val v5 = percent(75)                // 0.75

// Properties
val value: Double = v1.value        // Get underlying value
```

## GodelLogic Operations

### Conjunction (AND)
Returns minimum of values.

```kotlin
// Two values
val result1 = GodelLogic.and(a, b)

// Multiple values (3+ using vararg)
val result2 = GodelLogic.and(a, b, c)
val result3 = GodelLogic.and(a, b, c, d, e)

// Multiple values (using List)
val values = listOf(a, b, c, d)
val result4 = GodelLogic.and(values)
```

### Disjunction (OR)
Returns maximum of values.

```kotlin
// Two values
val result1 = GodelLogic.or(a, b)

// Multiple values (3+ using vararg)
val result2 = GodelLogic.or(a, b, c)
val result3 = GodelLogic.or(a, b, c, d, e)

// Multiple values (using List)
val values = listOf(a, b, c, d)
val result4 = GodelLogic.or(values)
```

### Negation (NOT)
Returns 1.0 if input is 0.0, otherwise 0.0.

```kotlin
val result = GodelLogic.not(a)

// Examples
GodelLogic.not(TruthValue(0.0))   // = 1.0 ✓
GodelLogic.not(TruthValue(0.001)) // = 0.0
GodelLogic.not(TruthValue(0.5))   // = 0.0
GodelLogic.not(TruthValue(1.0))   // = 0.0
```

### Implication
Returns 1.0 if premise ≤ conclusion, otherwise returns conclusion.

```kotlin
val result = GodelLogic.implies(premise, conclusion)

// Examples
GodelLogic.implies(TruthValue(0.7), TruthValue(0.9)) // = 1.0
GodelLogic.implies(TruthValue(0.9), TruthValue(0.7)) // = 0.7
```

### Bi-implication (IFF)
Defined as (a → b) ∧ (b → a).

```kotlin
val result = GodelLogic.iff(a, b)
```

## Formula System

### Creating Formulas

```kotlin
// Variables
val a = Formula.Variable("A")
val b = Formula.Variable("B")

// Constants
val c1 = Formula.Constant(TruthValue(0.5))
val c2 = Formula.Constant(TruthValue.TRUE)

// Compound formulas
val f1 = Formula.And(a, b)              // Vararg constructor
val f2 = Formula.And(listOf(a, b, c))  // List constructor
val f3 = Formula.Or(a, b, c)
val f4 = Formula.Not(a)
val f5 = Formula.Implies(a, b)
val f6 = Formula.Iff(a, b)
```

### Using the DSL

```kotlin
// Import extension functions
import variable
import constant
import and
import or
import not
import implies
import iff

// Create formulas naturally
val a = variable("A")
val b = variable("B")
val c = variable("C")

val formula1 = a and b
val formula2 = a or b
val formula3 = !a
val formula4 = a implies b
val formula5 = a iff b

// Complex formulas
val formula6 = (a and b) or c
val formula7 = (a implies b) and (b implies c)
val formula8 = !a or (b and c)
```

### Evaluating Formulas

```kotlin
val formula = (a and b) or c

// Create context
val context = mapOf(
    "A" to TruthValue(0.8),
    "B" to TruthValue(0.6),
    "C" to TruthValue(0.5)
)

// Evaluate
val result = formula.evaluate(context)
// = (0.8 ∧ 0.6) ∨ 0.5
// = 0.6 ∨ 0.5
// = 0.6
```

## Context Building

### Manual Context
```kotlin
val context = mapOf(
    "temperature" to TruthValue(0.8),
    "humidity" to TruthValue(0.75)
)
```

### Using Context Builder DSL
```kotlin
val context = context {
    "temperature" means 0.8
    "humidity" means 75              // Percentage notation
    "pressure" means TruthValue(0.9)
}
```

## Decision Systems

### Creating a Decision System

```kotlin
class MyDecisionSystem : GodelDecisionSystem(threshold = TruthValue(0.75)) {
    
    // Define your formulas
    private val input1 = Formula.Variable("input1")
    private val input2 = Formula.Variable("input2")
    private val myFormula = Formula.And(input1, input2)
    
    override fun evaluate(inputs: Map<String, TruthValue>): Decision {
        val confidence = myFormula.evaluate(inputs)
        
        return makeDecision(
            confidence,
            "Your reasoning here"
        )
    }
}
```

### Using a Decision System

```kotlin
val system = MyDecisionSystem()

val inputs = mapOf(
    "input1" to TruthValue(0.8),
    "input2" to TruthValue(0.9)
)

val decision = system.evaluate(inputs)

println(decision.shouldAct)     // Boolean: true/false
println(decision.confidence)    // TruthValue
println(decision.reasoning)     // String explanation
```

## Truth Tables

### Generating Truth Tables

```kotlin
val generator = TruthTableGenerator()

// Binary (classical) truth table
val binaryTable = generator.generateBinaryTable(
    formula = myFormula,
    variables = listOf("A", "B")
)

// Multi-valued truth table
val godelTable = generator.generateTable(
    formula = myFormula,
    variables = listOf("A", "B"),
    truthValues = listOf(
        TruthValue(0.0),
        TruthValue(0.5),
        TruthValue(1.0)
    )
)

// Print table
generator.printTable(
    rows = godelTable,
    variables = listOf("A", "B"),
    formulaStr = "A ∧ B"
)
```

## Utility Functions

### Percentage Conversion
```kotlin
val v1 = percent(75)       // TruthValue(0.75)
val v2 = percent(100)      // TruthValue(1.0)
val v3 = percent(50.5)     // TruthValue(0.505)
```

### String Representations
```kotlin
val v = TruthValue(0.756)
println(v.toString())      // "0.756" (formatted to 3 decimals)
```

## Common Patterns

### Pattern 1: All Conditions Required
```kotlin
// Using operations
val allMet = GodelLogic.and(condition1, condition2, condition3)

// Using formulas
val formula = Formula.And(
    Formula.Variable("condition1"),
    Formula.Variable("condition2"),
    Formula.Variable("condition3")
)
```

### Pattern 2: Any Condition Sufficient
```kotlin
// Using operations
val anyMet = GodelLogic.or(option1, option2, option3)

// Using formulas
val formula = Formula.Or(
    Formula.Variable("option1"),
    Formula.Variable("option2"),
    Formula.Variable("option3")
)
```

### Pattern 3: Conditional Logic
```kotlin
// If A then B
val rule1 = GodelLogic.implies(a, b)

// A if and only if B
val rule2 = GodelLogic.iff(a, b)
```

### Pattern 4: Nested Conditions
```kotlin
// (A AND B) OR (C AND D)
val primary = GodelLogic.and(a, b)
val secondary = GodelLogic.and(c, d)
val result = GodelLogic.or(primary, secondary)

// Using DSL
val formula = (a and b) or (c and d)
```

### Pattern 5: Threshold-Based Decision
```kotlin
val confidence = myFormula.evaluate(context)

val action = when {
    confidence.value >= 0.9 -> "HIGH_PRIORITY"
    confidence.value >= 0.7 -> "MEDIUM_PRIORITY"
    confidence.value >= 0.5 -> "LOW_PRIORITY"
    else -> "NO_ACTION"
}
```

## Type Safety

### Inline Value Class Benefits
```kotlin
// TruthValue is an inline value class
// No runtime overhead - compiles to Double
// But provides compile-time type safety

val truth = TruthValue(0.7)
val plain = 0.7

// truth != plain at compile time
// But identical at runtime (both are Double)
```

### Input Validation
```kotlin
// Automatic validation on construction
val valid = TruthValue(0.7)        // ✓ OK

val invalid1 = TruthValue(-0.1)    // ✗ throws IllegalArgumentException
val invalid2 = TruthValue(1.5)     // ✗ throws IllegalArgumentException

// Safe construction with coercion
val coerced = TruthValue.of(1.5)   // ✓ OK, returns TruthValue(1.0)
```

## Examples Quick Reference

All examples are in `GodelLogic.kt`:

1. **MedicalDiagnosisSystem** - Threshold: 0.7
   - Inputs: fever, cough, fatigue, breathShortness, lossOfSmell
   - Outputs: COVID/Flu/Pneumonia diagnosis

2. **LaneChangeDecisionSystem** - Threshold: 0.85
   - Inputs: leftLaneClear, safeFollowingDistance, turnSignalOn, noBlindSpot, roadConditionGood
   - Output: Safe to change lanes (boolean)

3. **CreditRiskAssessment** - Threshold: 0.75
   - Inputs: goodCreditScore, stableEmployment, sufficientIncome, lowDebtRatio, hasCollateral
   - Output: Approve/Deny loan

4. **SmartHeatingSystem** - Threshold: 0.6
   - Inputs: tempLow, someoneHome, nightTime, energyCostAcceptable, weatherCold
   - Output: Turn heating on/off

5. **ContentModerationSystem** - Threshold: 0.75
   - Inputs: offensiveLanguage, userViolationHistory, multipleReports, harassmentContext, threatOfViolence
   - Output: Remove/Review/Keep content

6. **ProductRecommendationSystem** - Threshold: 0.7
   - Inputs: viewedSimilar, priceRangeMatch, highRating, inStock, trendingProduct
   - Output: Recommend/Skip product

## Testing

### Running Unit Tests
```kotlin
// All tests
runTests()

// Specific test categories
val test = GodelLogicTest()
test.testConjunction()
test.testDisjunction()
test.testNegation()
test.testImplication()
test.testDoubleNegationNotTautology()
test.testExcludedMiddleNotTautology()
test.testPrelinearityIsTautology()
```

### Comparisons with Classical Logic
```kotlin
val comparison = ClassicalVsGodelComparison()
comparison.compareExcludedMiddle()
comparison.compareDoubleNegation()
comparison.comparePrelinearity()
```

## Performance Tips

1. **Use inline value classes** - Already done with TruthValue
2. **Prefer binary operations** for simple cases (and(a, b) vs and(listOf(a, b)))
3. **Reuse formulas** - Create once, evaluate many times
4. **Cache contexts** if evaluating same formula with same inputs
5. **Use DSL for readability** - No performance penalty

## Compilation

```bash
# Main implementation only
kotlinc GodelLogic.kt -include-runtime -d GodelLogic.jar

# With tests
kotlinc GodelLogic.kt GodelLogicTests.kt -include-runtime -d GodelLogicFull.jar
```

## Execution

```bash
# Run examples
kotlin -classpath GodelLogic.jar GodelLogicKt

# Run tests and comparisons
kotlin -classpath GodelLogicFull.jar GodelLogicTestsKt

# Run extended demonstrations
kotlin -classpath GodelLogicFull.jar GodelLogicTestsKt
```
