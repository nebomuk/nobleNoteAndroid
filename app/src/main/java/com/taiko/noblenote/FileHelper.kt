package com.taiko.noblenote

import android.content.Context
import rx.Observable
import java.io.*

/**
 * Created by taiko000
 */
object FileHelper {


    @JvmStatic
    fun readFile(filePath : String, ctx : Context, isHtml : Boolean ) : Observable<CharSequence>
    {
        return Observable.create({ subscriber ->
            val htmlText = StringBuilder()
            try {
                BufferedReader(FileReader(filePath)).use { br ->

                    while (true) {
                        val line = br.readLine()
                        if(line == null)
                        {
                            break;
                        }
                        htmlText.append(line)
                        htmlText.append('\n')
                    }
                }
            } catch (exception: IOException) {
                KLog.e("Could not load file",exception)
            }

            // do slow html parsing
            val span: CharSequence
            if (isHtml) {
                span = Html.fromHtml(htmlText.toString(), ctx.resources.displayMetrics.density) // time consuming
            } else {
                span = htmlText.toString()
            }

            subscriber.onNext(span)
            subscriber.onCompleted()
        })
    }

    @JvmStatic
    fun writeFile(filePath : String, text : CharSequence) : Observable<Long>
    {

        return Observable.create<Long> {
            val file = File(filePath)
            try {
                val writer = FileWriter(file)
                writer.append(text)
                writer.flush()
                writer.close()
                val lastModified = file.lastModified()
                it.onNext(lastModified)
                it.onCompleted()
            } catch (e: IOException) {
                KLog.e("Could not save file",e)
                it.onError(e)
            }

        }

    }
}