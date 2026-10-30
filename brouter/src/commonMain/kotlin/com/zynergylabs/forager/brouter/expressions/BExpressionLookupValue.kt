package com.zynergylabs.forager.brouter.expressions

/**
 * Port of BRouter 1.7.10's `btools.expressions.BExpressionLookupValue`: a lookup value with optional aliases.
 * `toString` gives the primary value; `equals` compares a String or another value against the primary value only;
 * [matches] also compares the aliases.
 */
internal class BExpressionLookupValue(var value: String) {
    var aliases: MutableList<String>? = null

    override fun toString(): String = value

    fun addAlias(alias: String) {
        val a = aliases ?: ArrayList<String>().also { aliases = it }
        a.add(alias)
    }

    // Java's equals accepts a String, which Kotlin's equals contract does not name; kept as Java has it. Java does not
    // override hashCode either.
    override fun equals(other: Any?): Boolean {
        if (other is String) {
            return value == other
        }
        if (other is BExpressionLookupValue) {
            return value == other.value
        }
        return false
    }

    override fun hashCode(): Int = super.hashCode() // Java's identity hash, as Java does not override it

    fun matches(s: String): Boolean {
        if (value == s) return true
        aliases?.let { for (alias in it) if (alias == s) return true }
        return false
    }
}
