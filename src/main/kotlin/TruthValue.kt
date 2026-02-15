/**
 * Represents a truth value in Gödel Logic.
 * Valid range: [0.0, 1.0] where 0.0 = completely false, 1.0 = completely true
 */
  data class TruthValue(val value: Double) {
    init {
        require(value in 0.0..1.0) { "Truth value must be in range [0.0, 1.0], got $value" }
    }

    override fun toString(): String = "%.3f".format(value)

    companion object {
        val FALSE = TruthValue(0.0)
        val TRUE = TruthValue(1.0)

        fun of(value: Double) = TruthValue(value.coerceIn(0.0, 1.0))
    }
}