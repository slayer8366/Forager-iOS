package com.zynergylabs.forager.brouter.util

/**
 * BRouter 1.7.10's `btools.util.StringUtils`, whole (step 7, session 24): the formats need it (RECORD -111). Java
 * dereferences a null `s` (a `NullPointerException`); here the parameter is nullable and `s!!` throws Kotlin's.
 */
internal object StringUtils {
    private val xmlChr = charArrayOf('&', '<', '>', '\'', '"', '\t', '\n', '\r')
    private val xmlEsc = arrayOf("&amp;", "&lt;", "&gt;", "&apos;", "&quot;", "&#x9;", "&#xA;", "&#xD;")

    private val jsnChr = charArrayOf('\'', '"', '\\', '/')
    private val jsnEsc = arrayOf("\\'", "\\\"", "\\\\", "\\/")

    /** `StringUtils.java:16-18`. */
    fun escapeJson(s: String?): String = escape(s!!, jsnChr, jsnEsc)

    /** `StringUtils.java:23-25`. */
    fun escapeXml10(s: String?): String = escape(s!!, xmlChr, xmlEsc)

    private fun escape(s: String, chr: CharArray, esc: Array<String>): String {
        var sb: StringBuilder? = null
        for (i in s.indices) {
            val c = s[i]
            var j = 0
            while (j < chr.size) {
                if (c == chr[j]) {
                    if (sb == null) {
                        sb = StringBuilder(s.substring(0, i))
                    }
                    sb.append(esc[j])
                    break
                }
                j++
            }
            if (sb != null && j == chr.size) {
                sb.append(c)
            }
        }
        return sb?.toString() ?: s
    }
}
