package com.taiko.noblenote.document

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import androidx.core.content.ContextCompat
import com.taiko.noblenote.loggerFor
import rx.Observable
import rx.subscriptions.Subscriptions
import java.io.File


/**
 * storage access framework helpers, checks if the volumes (sd card, usb stick, primary external storage etc. ) are accessible
 */
object VolumeUtil {

    private val log = loggerFor();

    private const val AUTHORITY = "com.android.externalstorage.documents"


    fun volumeAccessibleObservable(context: Context, persistedOpenDocumentTreeIntentUri : Observable<String>) : Observable<Boolean>
    {
        val obs : Observable<Unit> =  Observable.create<Unit> { subscriber ->

            val uri = DocumentsContract.buildRootsUri(AUTHORITY)

            val contentObserver: ContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {

                    subscriber.onNext(Unit);
                }
            }

            context.contentResolver.registerContentObserver(uri, false, contentObserver)

            subscriber.add(Subscriptions.create { context.contentResolver.unregisterContentObserver(contentObserver) })
        }
        return Observable.combineLatest(obs.startWith(Unit),persistedOpenDocumentTreeIntentUri
                ) { _ : Unit, uri : String ->  volumeAccessible(context,uri)} // maybe requires BiFunction in RxJava2 or later
                .distinctUntilChanged();
    }



    fun volumeAccessible(context: Context, persistedOpenDocumentTreeIntentUri : String): Boolean {

        val uri = Uri.parse(persistedOpenDocumentTreeIntentUri);

        if(uri.scheme == "file")
        {
            val f = File(uri.path).absolutePath;
            if(f.startsWith(context.getExternalFilesDir(null)?.absolutePath.toString(),true) ||
                    f.startsWith(context.filesDir.absolutePath) ||

                    (f.startsWith(Environment.getExternalStorageDirectory().absolutePath)
                            && ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
            {
                return true;
            }
        }

        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        // reflection required for Android < 7
        val storageVolumes : List<Any> = storageManager.invokeDynamic("getStorageVolumes") as List<Any>

        val storageUuids: List<String> = storageVolumes
                .map {
                    if (it.invokeDynamic("isPrimary") as Boolean) {
                        // Primary storage doesn't get a UUID here.
                        "primary"
                    } else {
                        it.invokeDynamic("getUuid") as? String
                    }
                }
                .filterNotNull()


        val volumeUris: List<String> =  storageUuids.map { buildVolumeUriFromUuid(it) }

        val b = volumeUris.any { persistedOpenDocumentTreeIntentUri.startsWith(it,true) }
        return b;
    }

    private fun buildVolumeUriFromUuid(uuid: String): String {
        return DocumentsContract.buildTreeDocumentUri(
                AUTHORITY,
                "$uuid:"
        ).toString()
    }

    private fun Any.invokeDynamic(name : String) : Any?
    {
        val method = this.javaClass.getMethod(name);
        return method.invoke(this);
    }

}