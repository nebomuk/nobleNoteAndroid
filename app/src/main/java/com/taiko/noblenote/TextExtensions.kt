package com.taiko.noblenote

/**
 *
 * counts the number of occurrences of the substring in the given char sequence
 */
fun CharSequence.countMatches(substring: String): Int {
    var n = 0;
    var count = 0;
    if (substring != "") {
        while (true) {
            n = this.indexOf(substring, n, ignoreCase = true);
            if (n == -1) {
                break;
            }

            n += substring.length;
            ++count;
        }
    }
    return count;
}

/**
 * returns all indices of the positions in the text that match the given substring
 */
fun CharSequence.indicesOf(substring: String): List<Int> {

    val indices = mutableListOf<Int>();
    var n = 0;


    if (substring != "") {
        while (true) {
            n = this.indexOf(substring, n, ignoreCase = true);
            if (n == -1) {
                break;
            }
            indices.add(n);

            n += substring.length;
        }
    }
    return indices;
}
