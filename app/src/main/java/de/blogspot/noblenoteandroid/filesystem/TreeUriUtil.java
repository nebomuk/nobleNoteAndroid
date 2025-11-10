package de.blogspot.noblenoteandroid.filesystem;

import android.content.Context;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;


/**
 * this class is used to get the path from a tree Uri returned from OPEN_DOCUMENT_TREE via the storage access framework.
 * this is needed on Android 5 & 6 because they do not support moving documents via Storage Access Framework
 */
public final class TreeUriUtil {

    private static final String PRIMARY_VOLUME_NAME = "primary";

    @NonNull // method requires at least Android 5
    public static String treeUriToFilePath(@Nullable final Uri treeUri, Context con) {
        if (treeUri == null) throw new IllegalArgumentException("treeUriToFilePath failed: treeUri must not be null");
        String volumePath = getVolumePathViaReflection(treeUriToVolumeId(treeUri),con);
        if (volumePath == null) return File.separator;
        if (volumePath.endsWith(File.separator))
            volumePath = volumePath.substring(0, volumePath.length() - 1);

        String documentPath = treeUriToDocumentPath(treeUri);
        if (documentPath.endsWith(File.separator))
            documentPath = documentPath.substring(0, documentPath.length() - 1);

        if (documentPath.length() > 0) {
            if (documentPath.startsWith(File.separator))
                return volumePath + documentPath;
            else
                return volumePath + File.separator + documentPath;
        }
        else return volumePath;
    }


    private static String getVolumePathViaReflection(final String volumeId, Context context) {
        try {
            StorageManager mStorageManager =
                    (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getUuid = storageVolumeClazz.getMethod("getUuid");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
            Object result = getVolumeList.invoke(mStorageManager);

            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String uuid = (String) getUuid.invoke(storageVolumeElement);
                Boolean primary = (Boolean) isPrimary.invoke(storageVolumeElement);

                // primary volume?
                if (primary && PRIMARY_VOLUME_NAME.equals(volumeId))
                    return (String) getPath.invoke(storageVolumeElement);

                // other volumes?
                if (uuid != null && uuid.equals(volumeId))
                    return (String) getPath.invoke(storageVolumeElement);
            }
            // not found.
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private static String treeUriToVolumeId(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if (split.length > 0) return split[0];
        else return null;
    }


    private static String treeUriToDocumentPath(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if ((split.length >= 2) && (split[1] != null)) return split[1];
        else return File.separator;
    }
}