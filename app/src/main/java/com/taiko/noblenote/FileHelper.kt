package com.taiko.noblenote

import android.content.Context
import android.net.Uri
import com.taiko.noblenote.document.SFile
import com.taiko.noblenote.editor.Html
import com.taiko.noblenote.editor.TextConverter
import rx.Observable
import java.io.*


/**
 * contains utilites related to file and directory writing, moving and removing
 */
object FileHelper {

    private val log = loggerFor()


    @JvmStatic
    fun readFile(filePath : Uri, ctx : Context, parseHtml: Boolean ) : Observable<CharSequence>
    {
        return Observable.create { subscriber ->
            val htmlText = StringBuilder()
            try {
                SFile(filePath).openInputStream().bufferedReader().use { br ->

                    while (true) {
                        val line = br.readLine()
                        @Suppress("FoldInitializerAndIfToElvis")
                        if(line == null)
                        {
                            break;
                        }
                        htmlText.append(line)
                        htmlText.append('\n')
                    }
                }
            } catch (exception: IOException) {
                log.e("Could not load file",exception)
            }

            // do slow html parsing
            val span: CharSequence
            if (parseHtml) {
                var htmlString = htmlText.toString();
                if(!TextConverter.mightBeRichText(htmlString))
                {
                    htmlString =  TextConverter.convertFromPlainText(htmlString);
                }
                span = Html.fromHtml(htmlString, ctx.resources.displayMetrics.density)// time consuming
            } else {
                span = htmlText.toString()
            }

            subscriber.onNext(span)
            subscriber.onCompleted()
        }
    }

    @JvmStatic
    fun writeFile(filePath : Uri, text : CharSequence) : Observable<Long>
    {

        return Observable.create<Long> {
            val file = SFile(filePath)
            try {
                val writer = file.openOuptutStream().bufferedWriter();
                writer.append(text)
                writer.flush()
                writer.close()
                val lastModified = file.lastModified()
                it.onNext(lastModified)
                it.onCompleted()
            } catch (e: IOException) {
                log.e("Could not save file",e)
                it.onError(e)
            }

        }

    }

    @Deprecated("this has been superseeded by SFile.rename, which works with directories that contain files")
    @JvmStatic
    fun directoryMove(oldRootDir: File, newRootDir: File): Boolean {
        var result = true
        if (!newRootDir.exists()) {
            result = result && newRootDir.mkdirs()
        }
        if (result) {
            for (f in oldRootDir.listFiles()) {
                if (f.isDirectory) {
                    val newDir = File(newRootDir, f.name)
                    result = result && directoryMove(f, newDir)
                }
                else // move all contained files
                {
                    val newFile = File(newRootDir, f.name)
                    if (newFile.exists()) {
                        result = result && newFile.delete()
                    }
                    result = result && f.renameTo(newFile)
                }
            }
            oldRootDir.delete(); // delete empty directory
        }
        return result
    }

    @Deprecated("Use SFile api directly")
    @JvmStatic
    fun fileMoveToFolder(src : File, destFolder : File) : Boolean
    {
        var result = src.isFile && destFolder.isDirectory;
        if(!result)
        {
            log.w("fileMoveToFolder failed: ${src.absolutePath} is not a file or ${destFolder.absolutePath} is not a directory");
        }
        result = result && src.renameTo(File(destFolder,src.name))
        return result;
    }

    /**
     * moves the file including it's immediate parent directory
     */
    @JvmStatic
    @Deprecated("use IDocumentFile implementations")
    fun fileMoveWithParent(oldFile : File, newRoot : File): Boolean {

        if(oldFile.parentFile == null)
        {
            log.d("fileMoveWithParent failed: parent file missing: $oldFile");
        }

        val newDir = File(newRoot,oldFile.parentFile.name);
        var res = false;
        if(!newDir.exists())
        {
            res = newDir.mkdirs();
        }
        val newFile = File(newDir,oldFile.name);
        res = res && oldFile.renameTo(newFile);
        return res
    }

}