package com.taiko.noblenote.filesystem

import android.Manifest
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.taiko.noblenote.util.loggerFor
import rx.Observable
import rx.lang.kotlin.PublishSubject
import rx.lang.kotlin.switchOnNext
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import rx.subscriptions.Subscriptions
import java.io.File


/**
 * storage access framework helpers, checks if the volumes (sd card, usb stick, primary external storage etc. ) are accessible
 */
class VolumeUtil
{

    private val onActivityStartHook : PublishSubject<Unit> = PublishSubject();


    fun volumeAccessible(context: Context, persistedOpenDocumentTreeIntentUri : String) : Observable<Boolean>
    {

        val uriString = persistedOpenDocumentTreeIntentUri
            val uri = Uri.parse(uriString);

            if(uri.scheme == "file")
            {
                return Observable.just(isFileUriAccessible(uri, context))
            }
            else if(context.contentResolver.persistedUriPermissions.isEmpty())
            {
                return Observable.just(false);
            }
            else // uri.scheme == "content"
            {
                // this is only allowed when when the uri is a content uri
                val changed : Observable<Unit> = createExternalStorageChangedObservable(context)
                return changed.map { fileOrContentUriAccessible(context, uri.toString()) }
                        .startWith(fileOrContentUriAccessible(context, persistedOpenDocumentTreeIntentUri))
                        .distinctUntilChanged();
            }
    }


    companion object
    {

    private const val AUTHORITY = "com.android.externalstorage.documents"


    private val log = loggerFor();


    fun fileOrContentUriAccessible(context: Context, persistedOpenDocumentTreeIntentUri : String): Boolean {

        val uri = Uri.parse(persistedOpenDocumentTreeIntentUri);

        if(uri.scheme == "file")
        {
            if (isFileUriAccessible(uri, context)) return true
        }

        // all uris have been revoked via app detail settings (its not possible to revoke a single uri)
        if(context.contentResolver.persistedUriPermissions.isEmpty())
        {
            return false;
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

    private fun isFileUriAccessible(uri: Uri, context: Context): Boolean {
        val f = File(uri.path).absolutePath;
        if (f.startsWith(context.getExternalFilesDir(null)?.absolutePath.toString(), true) ||
                f.startsWith(context.filesDir.absolutePath) ||

                (f.startsWith(Environment.getExternalStorageDirectory().absolutePath)
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            return true;
        }
        return false
    }

    private fun buildVolumeUriFromUuid(uuid: String): String {
        return DocumentsContract.buildTreeDocumentUri(
                AUTHORITY,
                "$uuid:"
        ).toString()
    }

    // used to call hidden API methods on Android 5,6 that later became public
    private fun Any.invokeDynamic(name : String) : Any?
    {
        val method = this.javaClass.getMethod(name);
        return method.invoke(this);
    }

        // creates a content observer, this must never be called when Uri permission is not granted!
        private fun createExternalStorageChangedObservable(context: Context): Observable<Unit> {
            return Observable.create<Unit> { subscriber ->

                val uri = DocumentsContract.buildRootsUri(AUTHORITY)

                val contentObserver: ContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
                    override fun onChange(selfChange: Boolean) {

                        subscriber.onNext(Unit);
                    }
                }

                context.contentResolver.registerContentObserver(uri, false, contentObserver)

                subscriber.add(Subscriptions.create { context.contentResolver.unregisterContentObserver(contentObserver) })
            }
        }

    }

}