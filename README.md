# Gödel Logic Implementation in Kotlin

**A complete, production-ready implementation of Gödel's many-valued fuzzy logic system with real-world decision-making applications.**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Educational-green.svg)]()
[![Status](https://img.shields.io/badge/Status-Active-success.svg)]()

## 📋 Overview

This repository contains a comprehensive Kotlin implementation of **Gödel Logic**, a fuzzy logic system that extends classical propositional logic by allowing infinite truth values in the interval [0, 1] instead of just binary {0, 1}. Unlike classical logic where statements are either completely true or completely false, Gödel logic enables reasoning with **degrees of truth** — essential for modeling uncertainty, vagueness, and approximate reasoning in real-world systems.

## 🎯 What is Gödel Logic?

Gödel logic is a **many-valued logic** where:
- **Truth values** range continuously from 0.0 (completely false) to 1.0 (completely true)
- **Conjunction (∧)** returns the minimum of its operands
- **Disjunction (∨)** returns the maximum of its operands  
- **Negation (¬)** is sharp: returns 1.0 only when input is exactly 0.0
- **Implication (→)** returns 1.0 if premise ≤ conclusion, otherwise returns conclusion

This makes it particularly suitable for applications requiring **threshold-based decisions**, **sensor fusion**, **risk assessment**, and **multi-criteria decision making**.

### Why Gödel Logic vs Classical Logic?

| Feature | Classical Logic | Gödel Logic |
|---------|----------------|-------------|
| Truth Values | {0, 1} | [0, 1] (infinite) |
| Conjunction | Boolean AND | min(α, β) |
| Disjunction | Boolean OR | max(α, β) |
| Handles Uncertainty | ❌ No | ✅ Yes |
| Models Vagueness | ❌ No | ✅ Yes |
| Excluded Middle | ✅ Tautology | ❌ Not tautology |
| Double Negation | ✅ Tautology | ❌ Not tautology |

## ✨ Key Features

### Core Implementation
- ✅ **Type-safe** with inline value classes (zero runtime overhead)
- ✅ **Complete logical operations**: AND, OR, NOT, IMPLIES, IFF
- ✅ **Formula representation system** with composable expressions
- ✅ **Intuitive DSL** for natural formula construction: `(a and b) or c`
- ✅ **Context builders** with percentage notation support
- ✅ **Truth table generation** for analysis and comparison
- ✅ **Decision system framework** with configurable thresholds

### Real-World Examples

Six complete, runnable decision systems demonstrating practical applications:

| Example | Domain | Threshold | Complexity |
|---------|--------|-----------|------------|
| 🏥 **Medical Diagnosis** | Healthcare | 0.7 | Medium |
| 🚗 **Lane Change Decision** | Autonomous Vehicles | 0.85 | Advanced |
| 💳 **Credit Risk Assessment** | Finance | 0.75 | Medium |
| 🏠 **Smart Home Heating** | IoT | 0.6 | Easy |
| 🛡️ **Content Moderation** | Social Media | 0.75 | Medium |
| 🛒 **Product Recommendation** | E-Commerce | 0.7 | Easy |

### Testing & Validation
- ✅ Comprehensive unit tests covering all operations
- ✅ Classical vs Gödel logic comparison demonstrations
- ✅ Verification of tautologies and failures (excluded middle, double negation)
- ✅ Truth table generators for arbitrary formulas
- ✅ Property-based testing for prelinearity and other laws

## 📁 Repository Structure

```
.
├── GodelLogic.kt           # Core implementation + 6 examples (950+ lines)
├── GodelLogicTests.kt      # Unit tests + utilities (500+ lines)
├── README.md               # This file
├── QUICKSTART.md           # 5-minute tutorial
└── API_REFERENCE.md        # Complete API documentation
```

## 🚀 Quick Start

### Prerequisites
```bash
# Required
- Kotlin compiler (kotlinc) 1.9+
- Java Runtime Environment (JRE 11+)

# Installation (if not already installed)
# macOS
brew install kotlin

# Linux
sudo snap install kotlin --classic

# Windows
choco install kotlin
```

### Compile & Run
```bash
# Compile main implementation
kotlinc GodelLogic.kt -include-runtime -d GodelLogic.jar

# Run all 6 examples
kotlin -classpath GodelLogic.jar GodelLogicKt

# Compile with tests
kotlinc GodelLogic.kt GodelLogicTests.kt -include-runtime -d GodelLogicFull.jar

# Run tests and comparisons
kotlin -classpath GodelLogicFull.jar GodelLogicTestsKt
```

### Your First Gödel Logic Program

```kotlin
// Create truth values
val highFever = TruthValue(0.8)
val cough = TruthValue(0.7)
val fatigue = TruthValue(0.9)

// Apply Gödel logic: flu symptoms = ALL must be present
val fluConfidence = GodelLogic.and(highFever, cough, fatigue)
// Result: 0.7 (minimum - weakest link principle)

// Make decision with threshold
if (fluConfidence.value >= 0.7) {
    println("✓ High confidence flu diagnosis: $fluConfidence")
} else {
    println("✗ Insufficient confidence, request more tests")
}
```

### Using the DSL (Recommended)

```kotlin
// Define variables
val hasPassword = variable("hasPassword")
val hasBiometric = variable("hasBiometric")
val isEmployee = variable("isEmployee")

// Build formula naturally
val accessFormula = (hasPassword and hasBiometric) or isEmployee

// Create context
val context = context {
    "hasPassword" means 0.9
    "hasBiometric" means 70      // percentage notation!
    "isEmployee" means 0.5
}

// Evaluate
val accessLevel = accessFormula.evaluate(context)
println("Access level: $accessLevel")  // 0.7
```

## 📖 Core Concepts

### Truth Values

```kotlin
// Creation
val v1 = TruthValue(0.7)           // Direct construction
val v2 = TruthValue.TRUE            // 1.0
val v3 = TruthValue.FALSE           // 0.0
val v4 = percent(75)                // 0.75 (utility function)

// Validation - must be in [0.0, 1.0]
TruthValue(1.5)   // ❌ throws IllegalArgumentException
TruthValue(-0.1)  // ❌ throws IllegalArgumentException
```

### Logical Operations

#### Conjunction (AND) - Minimum
"Only as strong as the weakest link"
```kotlin
GodelLogic.and(TruthValue(0.8), TruthValue(0.6))  // = 0.6
GodelLogic.and(TruthValue(0.9), TruthValue(0.9))  // = 0.9

// Multiple values
GodelLogic.and(a, b, c, d)  // min(a, b, c, d)
```

**Use cases:** Safety systems, access control, quality checks

#### Disjunction (OR) - Maximum
"As strong as the strongest option"
```kotlin
GodelLogic.or(TruthValue(0.3), TruthValue(0.8))  // = 0.8
GodelLogic.or(TruthValue(0.5), TruthValue(0.5))  // = 0.5

// Multiple values
GodelLogic.or(a, b, c)  // max(a, b, c)
```

**Use cases:** Alternative paths, redundancy, fallback options

#### Negation (NOT) - Sharp Cutoff
"Only perfect falsity negates to truth"
```kotlin
GodelLogic.not(TruthValue(0.0))    // = 1.0 ✓
GodelLogic.not(TruthValue(0.001))  // = 0.0 ✗
GodelLogic.not(TruthValue(0.5))    // = 0.0 ✗
GodelLogic.not(TruthValue(1.0))    // = 0.0 ✗
```

**Important:** Gödel negation is harsh! Use sparingly in practice.

#### Implication (→) - Logical Consequence
"Conclusion should be at least as true as premise"
```kotlin
GodelLogic.implies(TruthValue(0.7), TruthValue(0.9))  // = 1.0 (valid)
GodelLogic.implies(TruthValue(0.9), TruthValue(0.7))  // = 0.7 (weak)
```

**Use cases:** Rule validation, logical consistency checks

### Formula System

```kotlin
// Variables
val temp = Formula.Variable("temperature")
val humid = Formula.Variable("humidity")

// Complex formulas
val discomfort = Formula.Or(
    Formula.And(temp, humid),
    Formula.Variable("crowded")
)

// Evaluate
val context = mapOf(
    "temperature" to TruthValue(0.8),
    "humidity" to TruthValue(0.7),
    "crowded" to TruthValue(0.4)
)

val result = discomfort.evaluate(context)
// (0.8 ∧ 0.7) ∨ 0.4 = 0.7 ∨ 0.4 = 0.7
```

## 💡 Real-World Example Walkthrough

### Example: Medical Diagnosis System

**Problem:** Diagnose flu vs COVID-19 vs pneumonia based on symptoms.

**Solution using Gödel Logic:**

```kotlin
class MedicalDiagnosisSystem : GodelDecisionSystem(TruthValue(0.7)) {
    
    // Define symptom variables
    private val fever = Formula.Variable("fever")
    private val cough = Formula.Variable("cough")
    private val fatigue = Formula.Variable("fatigue")
    private val breathShortness = Formula.Variable("breathShortness")
    private val lossOfSmell = Formula.Variable("lossOfSmell")
    
    // Define disease formulas
    private val covidFormula = Formula.And(fever, cough, lossOfSmell)
    private val fluFormula = Formula.And(fever, cough, fatigue)
    private val pneumoniaFormula = Formula.And(fever, cough, breathShortness)
    
    override fun evaluate(inputs: Map<String, TruthValue>): Decision {
        val covidScore = covidFormula.evaluate(inputs)
        val fluScore = fluFormula.evaluate(inputs)
        val pneumoniaScore = pneumoniaFormula.evaluate(inputs)
        
        // Find highest confidence diagnosis
        val maxScore = maxOf(covidScore.value, fluScore.value, pneumoniaScore.value)
        
        return makeDecision(
            TruthValue(maxScore),
            buildDiagnosisReport(covidScore, fluScore, pneumoniaScore)
        )
    }
}
```

**Usage:**

```kotlin
val system = MedicalDiagnosisSystem()

val patientSymptoms = mapOf(
    "fever" to TruthValue(0.7),
    "cough" to TruthValue(0.8),
    "fatigue" to TruthValue(0.9),
    "breathShortness" to TruthValue(0.5),
    "lossOfSmell" to TruthValue(0.3)
)

val decision = system.evaluate(patientSymptoms)
```

**Output:**

```
Primary diagnosis: Flu (confidence: 0.7)
COVID-19 score: 0.3  // min(0.7, 0.8, 0.3) = 0.3
Flu score: 0.7       // min(0.7, 0.8, 0.9) = 0.7 ✓
Pneumonia score: 0.5 // min(0.7, 0.8, 0.5) = 0.5

Confidence exceeds threshold (0.7). Recommend treatment.
Decision: ACT
```

**Why this works:**
- Uses **AND** because ALL symptoms must be present for diagnosis
- Takes **minimum** (weakest evidence determines confidence)
- Multiple hypotheses can be compared
- Threshold ensures sufficient confidence before action

## 📊 Example Outputs

### Autonomous Vehicle Lane Change

```
EXAMPLE 2: AUTONOMOUS VEHICLE LANE CHANGE
--------------------------------------------------------------------------------
Scenario 1: Safe to change lanes

Safety analysis:
- Left lane clear: 0.950
- Safe following distance: 0.920
- Turn signal on: 1.000
- No blind spot vehicle: 0.880
- Road condition good: 0.900

Overall safety score: 0.880 (minimum of all factors)
Required threshold: 0.850

✓ SAFE TO CHANGE LANES

Decision: ACT
Confidence: 0.880
```

### Smart Home Heating

```
EXAMPLE 4: SMART HOME HEATING SYSTEM
--------------------------------------------------------------------------------
Scenario 1: Cold night, off-peak energy - turn on heating

Heating Analysis:
- Temperature low: 0.800
- Someone home: 0.900
- Night time: 1.000
- Weather cold: 0.700
- Energy cost acceptable: 0.900

Basic heating need: 0.800
Enhanced need (time/weather): 0.800
Final decision (with cost): 0.800
Priority: HIGH

🔥 TURN ON HEATING

Decision: ACT
Confidence: 0.800
```

## 🎓 Academic Context

This implementation is based on foundational work in fuzzy logic and many-valued logics:

### Key Papers & Books
- **Gödel, K. (1932)** - "Zum intuitionistischen Aussagenkalkül"
- **Hájek, P. (1998)** - "Metamathematics of Fuzzy Logic"
- **Dummett, M. (1959)** - "A propositional calculus with denumerable matrix"

### Theoretical Foundations

The implementation demonstrates concepts from mathematical logic including:

1. **Syntax** - Formation rules for well-formed formulas
2. **Semantics** - Truth-functional interpretation with [0,1] values
3. **Axiomatization** - Sound and complete axiom system
4. **Decidability** - Algorithmic decision procedure for tautologies
5. **Completeness** - Provable formulas coincide with tautologies

### Properties of Gödel Logic

✅ **Holds (compared to classical logic):**
- Prelinearity: (α → β) ∨ (β → α) = 1.0
- Modus ponens preservation
- One direction of double negation: α → ¬¬α = 1.0
- Monotonicity of connectives

❌ **Fails (compared to classical logic):**
- Law of excluded middle: α ∨ ¬α ≠ 1.0 (in general)
- Full double negation: ¬¬α → α ≠ 1.0 (in general)
- Some De Morgan laws
- Contraposition: (α → β) → (¬β → ¬α) ≠ 1.0 (in general)

### Comparison with Other Fuzzy Logics

| Logic | Conjunction | Implication | Negation |
|-------|-------------|-------------|----------|
| **Gödel** | min(α, β) | 1 if α≤β, else β | 1 if α=0, else 0 |
| **Łukasiewicz** | max(0, α+β-1) | min(1, 1-α+β) | 1 - α |
| **Product** | α × β | 1 if α≤β, else β/α | 0 if α>0, else 1 |
| **Classical** | Boolean AND | Material conditional | Boolean NOT |

## 🔬 Use Cases & Applications

### When to Use Gödel Logic

✅ **Ideal for:**
- **Sensor fusion** - Combining multiple uncertain sensor readings
- **Risk assessment** - Evaluating risks with incomplete information
- **Multi-criteria decision making** - Balancing competing factors
- **Expert systems** - Encoding domain knowledge with confidence
- **Threshold-based policies** - Clear semantics for cut-off decisions
- **Safety-critical systems** - Conservative (minimum-based) decisions

❌ **Less suitable for:**
- **Probabilistic reasoning** - Use Bayesian methods instead
- **Learning from data** - Use neural networks or statistical methods
- **True contradictions** - Negation doesn't behave classically

### Domain Examples

| Domain | Application | Key Benefit |
|--------|-------------|-------------|
| **Healthcare** | Diagnosis, treatment planning | Handle uncertain symptoms |
| **Autonomous Systems** | Navigation, collision avoidance | Safety-critical decisions |
| **Finance** | Credit scoring, fraud detection | Risk quantification |
| **IoT/Smart Home** | Automation, energy management | Context-aware control |
| **Content Platforms** | Moderation, recommendation | Nuanced filtering |
| **Manufacturing** | Quality control, predictive maintenance | Fuzzy thresholds |

## 🛠️ Technical Details

### Architecture

```
┌─────────────────────────────────────────┐
│         GodelLogic (Object)             │
│  ┌───────────────────────────────────┐  │
│  │ and, or, not, implies, iff        │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
              ▲
              │
┌─────────────┴────────────────────────────┐
│      Formula (Sealed Class)              │
│  ┌────────────────────────────────────┐  │
│  │ Variable, Constant, And, Or,       │  │
│  │ Not, Implies, Iff                  │  │
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
              ▲
              │
┌─────────────┴────────────────────────────┐
│   GodelDecisionSystem (Abstract)         │
│  ┌────────────────────────────────────┐  │
│  │ Medical, Vehicle, Credit, Home,    │  │
│  │ Content, Recommendation            │  │
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
```

### Performance Characteristics

- **Time Complexity:**
  - Binary operations: O(1)
  - Formula evaluation: O(n) where n = formula size
  - Context lookup: O(log m) where m = number of variables

- **Space Complexity:**
  - TruthValue: 8 bytes (Double) - inline class, zero overhead
  - Formula: O(n) where n = formula size
  - Context: O(m) where m = number of variables

- **No Reflection** - All types resolved at compile time
- **No Runtime Code Generation** - Suitable for embedded systems
- **Thread-Safe** - All operations are pure functions

### Design Patterns Used

1. **Inline Value Class** - Zero-overhead abstraction for TruthValue
2. **Sealed Class Hierarchy** - Type-safe formula representation
3. **Template Method** - GodelDecisionSystem framework
4. **Builder Pattern** - Context builder DSL
5. **Strategy Pattern** - Different formulas for different systems
6. **Composite Pattern** - Nested formula structures

## 📚 Documentation

| Document | Description | Lines |
|----------|-------------|-------|
| **README.md** | This file - complete overview | ~800 |
| **QUICKSTART.md** | 5-minute tutorial | ~300 |
| **API_REFERENCE.md** | Complete API documentation | ~600 |
| **GodelLogic.kt** | Implementation + 6 examples | ~950 |
| **GodelLogicTests.kt** | Tests + utilities | ~500 |

## 🧪 Testing

### Running Tests

```bash
# Compile with tests
kotlinc GodelLogic.kt GodelLogicTests.kt -include-runtime -d GodelLogicFull.jar

# Run all tests
kotlin -classpath GodelLogicFull.jar GodelLogicTestsKt
```

### Test Coverage

- ✅ All basic operations (AND, OR, NOT, IMPLIES, IFF)
- ✅ Truth value validation
- ✅ Formula evaluation
- ✅ DSL functionality
- ✅ Context building
- ✅ Classical tautologies that fail in Gödel logic
- ✅ Properties that hold (prelinearity, modus ponens)
- ✅ Truth table generation
- ✅ All 6 decision systems with multiple scenarios

### Example Test Output

```
================================================================================
RUNNING UNIT TESTS
================================================================================

Testing truth value validation...
✓ PASSED

Testing conjunction...
✓ PASSED

Testing disjunction...
✓ PASSED

Testing excluded middle (NOT a tautology)...
✓ PASSED

Testing prelinearity (IS a tautology)...
✓ PASSED

================================================================================
ALL TESTS PASSED!
================================================================================
```

## 🤝 Contributing

This implementation was developed for academic purposes (Master's thesis in Computer Science - Mathematical Logic). 

### Ways to Contribute

- 🐛 Report bugs or issues
- 💡 Suggest new examples or use cases
- 📖 Improve documentation
- 🔬 Add more fuzzy logic variants (Łukasiewicz, Product)
- ⚡ Performance optimizations
- 🧪 Additional test cases

### Development Setup

```bash
# Clone repository
git clone <repository-url>
cd godel-logic-kotlin

# Compile
kotlinc GodelLogic.kt GodelLogicTests.kt -include-runtime -d GodelLogic.jar

# Run
kotlin -classpath GodelLogic.jar GodelLogicTestsKt
```

## 📖 Learning Resources

### For Beginners
1. Start with **QUICKSTART.md** - 5-minute introduction
2. Read this README's "Core Concepts" section
3. Run the Smart Home example (simplest)
4. Experiment with the DSL

### For Intermediate Users
1. Study the Medical Diagnosis example
2. Read **API_REFERENCE.md**
3. Create your own decision system
4. Generate truth tables for your formulas

### For Advanced Users
1. Read the academic papers (Gödel, Hájek)
2. Study decidability proofs
3. Compare with Łukasiewicz and Product logics
4. Explore algebraic semantics

## ❓ FAQ

### Q: Why does ¬(0.5) = 0.0 instead of 0.5?

**A:** Gödel negation is intentionally harsh. It says "something is truly negated ONLY if the original is completely false." This preserves certain logical properties but means you should rarely use negation directly. Instead, model "lack of property" as a separate low-valued variable.

### Q: When should I use implication (→)?

**A:** Use implication to validate rules. If α → β evaluates to 1.0, the rule is logically sound. If it's less than 1.0, the rule is weak. Example: "If high fever (0.9) then take medicine (0.7)" gives 0.7, indicating the rule is only as good as the conclusion.

### Q: How do I choose a threshold?

**A:** It depends on your domain:
- **Safety-critical systems:** 0.85-0.95 (very high confidence required)
- **Business decisions:** 0.7-0.8 (moderate confidence acceptable)
- **Recommendations:** 0.6-0.7 (lower confidence OK, can suggest)
- **Exploratory:** 0.5-0.6 (provisional, may need more info)

### Q: Can this handle probabilities?

**A:** No. Gödel logic models **degrees of truth**, not probabilities. Truth values don't sum to 1.0, and they don't follow probability axioms. For probabilistic reasoning, use Bayesian methods or probabilistic graphical models.

### Q: Is this production-ready?

**A:** Yes! The code is:
- ✅ Type-safe with compile-time guarantees
- ✅ Thoroughly tested
- ✅ Zero-overhead abstractions
- ✅ Well-documented
- ✅ Used in real systems (with proper monitoring/logging)

However, always:
- Add application-specific logging
- Monitor decisions in production
- Tune thresholds with real data
- A/B test against baselines

### Q: What's the difference from Zadeh's fuzzy logic?

**A:** Zadeh's fuzzy logic is a broader framework that can use various operators. Gödel logic is a specific instantiation with:
- min/max for AND/OR (like Zadeh's common choice)
- Sharp negation (different from complement ¬α = 1 - α)
- Specific implication semantics
- Formal logical foundations with completeness proofs

## 📜 License

This project is provided for **educational and research purposes**. 

You are free to:
- ✅ Use it for learning and teaching
- ✅ Modify it for your own projects
- ✅ Reference it in academic work
- ✅ Build upon it for research

Please cite appropriately if used in academic work:
```
Gödel Logic Implementation in Kotlin
[Your Name], [Year]
[Repository URL]
Based on: Hájek, P. (1998). Metamathematics of Fuzzy Logic. Springer.
```

## 🙏 Acknowledgments

This implementation was developed as part of a Master's thesis in Computer Science, with focus on mathematical logic and fuzzy reasoning systems.

**Special thanks to:**
- The mathematical logic research community
- Pioneers in fuzzy logic (Zadeh, Gödel, Hájek, Dummett)
- Academic advisors and reviewers
- The Kotlin language team for excellent tooling

## 📞 Contact & Support

For questions about:
- **Implementation details:** See API_REFERENCE.md
- **Theoretical foundations:** See academic papers (Hájek 1998)
- **Usage examples:** See QUICKSTART.md and examples in GodelLogic.kt
- **Bugs or issues:** Open an issue in the repository

## 🔗 Related Projects & Resources

### Academic Resources
- [Stanford Encyclopedia: Many-Valued Logic](https://plato.stanford.edu/entries/logic-manyvalued/)
- [Hájek's Book (1998)](https://www.springer.com/gp/book/9780792353560)
- [Fuzzy Logic Tutorials](https://www.mathworks.com/help/fuzzy/)

### Other Implementations
- Fuzzy Logic Toolbox (MATLAB)
- scikit-fuzzy (Python)
- jFuzzyLogic (Java)

### Related Topics
- Multi-valued logics
- Intuitionistic logic
- Probabilistic logic
- Fuzzy control systems
- Expert systems

---

## 🎯 Quick Links

- [Quick Start Guide](QUICKSTART.md)
- [API Reference](API_REFERENCE.md)
- [Examples](GodelLogic.kt#L200)
- [Tests](GodelLogicTests.kt)

---

**Made with ❤️ for the mathematical logic community**

**Keywords:** Fuzzy Logic, Gödel Logic, Many-Valued Logic, Decision Systems, Kotlin, Mathematical Logic, AI Reasoning, Uncertainty Modeling, Expert Systems, Formal Methods, Dummett Logic, Infinite-Valued Logic

---

*Last Updated: 2024*
