package com.taiko.noblenote

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.tbruyelle.rxpermissions.RxPermissions
import rx.Observable
import java.io.*
import java.text.Collator
import java.util.*


/**
 * Created by taiko000
 */
object FileHelper {


    @JvmStatic
    fun readFile(filePath : String, ctx : Context, parseHtml: Boolean ) : Observable<CharSequence>
    {
        return Observable.create({ subscriber ->
            val htmlText = StringBuilder()
            try {
                BufferedReader(FileReader(filePath)).use { br ->

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
                KLog.e("Could not load file",exception)
            }

            // do slow html parsing
            val span: CharSequence
            if (parseHtml) {
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

    /**
     * moves the file including it's immediate parent directory
     */
    @JvmStatic
    fun fileMoveWithParent(oldFile : File, newRoot : File)
    {
        if(oldFile.parentFile == null)
        {
            Log.d("","parent file missing: $oldFile");
        }

        val newDir = File(newRoot,oldFile.parentFile.name);
        if(!newDir.exists())
        {
            newDir.mkdirs();
        }
        val newFile = File(newDir,oldFile.name);
        oldFile.renameTo(newFile);

    }

    /**
     * creates a mutable list of the contents of the directory
     * and creates the directory if it does not exist
     */
    @JvmStatic
    fun listFilesSorted(dir: File, filter: FileFilter): ArrayList<File> {
        //List<File> fileList = Arrays.asList(); // returns read only list, causes unsupported operation exceptions in adapter
        val fileList = ArrayList<File>()
        if (dir.exists() || dir.mkdirs()) {
            Collections.addAll(fileList, *dir.listFiles(filter))
            Collections.sort(fileList) { lhs, rhs -> Collator.getInstance().compare(lhs.name, rhs.name) }
        }
        return fileList
    }

    /**
     * checks write permission and sd card mount state
     * and invokes the callback when evertything is true
     */
    @JvmStatic
    fun checkMountStateAndPermission(activity: Activity, onSuccess: () -> Unit, onFailure : () -> Unit = {})
    {
        RxPermissions(activity).request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe({
            if(it) {

                if(Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)
                {
                    onSuccess();
                }
                else
                {
                    onFailure();
                    KLog.d("getExternalStorageState() != MEDIA_MOUNTED");
                    Toast.makeText(activity,R.string.msg_external_storage_not_mounted, Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                onFailure();
                KLog.d("Permission WRITE_EXTERNAL_STORAGE not granted");
                Toast.makeText(activity,R.string.msg_external_storage_permission_denied, Toast.LENGTH_LONG).show();
            }
        }, {
            KLog.e("exception in RxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)", it);
            onFailure();
        });
    }

}