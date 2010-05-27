/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.providers.media;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.media.MtpClient;
import android.media.MtpCursor;
import android.net.Uri;
import android.provider.Mtp;
import android.util.Log;


/**
 * Provides access to content on MTP and PTP devices via USB.
 * At the top level we have a list of MTP/PTP devices,
 * and then a list of storage units for each device.
 * Finally a list of objects (typically files and folders)
 * and their properties can be for each storage unit.
 */
public class MtpProvider extends ContentProvider {

    private static final String TAG = "MtpProvider";

    private MtpClient mClient;

    private static final UriMatcher sUriMatcher;

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate");
        mClient = new MtpClient();
        mClient.start();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        Log.d(TAG, "query projection: " + projection);

        if (projection == null) {
            throw new UnsupportedOperationException("MtpProvider queries require a projection");
        }
        if (selection != null || selectionArgs != null) {
            throw new UnsupportedOperationException("MtpProvider queries do not support selection");
        }
        if (sortOrder != null) {
            throw new UnsupportedOperationException("MtpProvider queries do not support sortOrder");
        }

        int deviceID = 0;
        int storageID = 0;
        int objectID = 0;
        int queryType = sUriMatcher.match(uri);
        try {
            switch (queryType) {
                case MtpCursor.DEVICE:
                    break;

                case MtpCursor.DEVICE_ID:
                case MtpCursor.STORAGE:
                case MtpCursor.OBJECT:
                    deviceID = Integer.parseInt(uri.getPathSegments().get(1));
                    break;

                case MtpCursor.STORAGE_ID:
                case MtpCursor.STORAGE_CHILDREN:
                    deviceID = Integer.parseInt(uri.getPathSegments().get(1));
                    storageID = Integer.parseInt(uri.getPathSegments().get(3));
                    break;

                case MtpCursor.OBJECT_CHILDREN:
                case MtpCursor.OBJECT_ID:
                    deviceID = Integer.parseInt(uri.getPathSegments().get(1));
                    objectID = Integer.parseInt(uri.getPathSegments().get(3));
                    storageID = -1;
                    break;

                default:
                    throw new IllegalStateException("Unknown URL: " + uri.toString());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unknown URL: " + uri.toString());
        }

        MtpCursor cursor = new MtpCursor(mClient, queryType, deviceID,
                storageID, objectID, projection);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        Log.d(TAG, "getType");
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        return 0;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Mtp.AUTHORITY, "device", MtpCursor.DEVICE);
        sUriMatcher.addURI(Mtp.AUTHORITY, "device/#", MtpCursor.DEVICE_ID);
        sUriMatcher.addURI(Mtp.AUTHORITY, "device/#/storage", MtpCursor.STORAGE);
        sUriMatcher.addURI(Mtp.AUTHORITY, "device/#/storage/#", MtpCursor.STORAGE_ID);
        sUriMatcher.addURI(Mtp.AUTHORITY, "device/#/object", MtpCursor.OBJECT);
        sUriMatcher.addURI(Mtp.AUTHORITY, "device/#/object/#", MtpCursor.OBJECT_ID);
        sUriMatcher.addURI(Mtp.AUTHORITY, "device/#/storage/#/child", MtpCursor.STORAGE_CHILDREN);
        sUriMatcher.addURI(Mtp.AUTHORITY, "device/#/object/#/child", MtpCursor.OBJECT_CHILDREN);
    }
}
