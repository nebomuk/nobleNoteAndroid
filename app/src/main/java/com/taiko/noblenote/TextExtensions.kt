package com.taiko.noblenote

/**
 * Created by Taiko
 */


    public fun CharSequence.countMatches(substring : String): Int {
        var n = 0;
        var count = 0;
        if(substring != "")
        {
            while (true)
            {
                n = this.indexOf(substring, n, true);
                if(n == -1)
                {
                    break;
                }

                n += substring.length;
                ++count;
            }
        }
        return count;
    }
