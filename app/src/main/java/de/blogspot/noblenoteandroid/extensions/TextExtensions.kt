package de.blogspot.noblenoteandroid.extensions


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
