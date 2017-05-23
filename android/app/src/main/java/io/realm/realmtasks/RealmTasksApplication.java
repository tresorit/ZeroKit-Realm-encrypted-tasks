/*
 * Copyright 2016 Realm Inc.
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

package io.realm.realmtasks;

import android.app.Application;

import com.tresorit.zerokit.AdminApi;

import java.io.IOException;
import java.util.Properties;

import io.realm.Realm;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;

public class RealmTasksApplication extends Application {

    public static final String HTTP = "http";
    public static final String REALM = "realm";

    public static final String FORMAT1 = "%s://%s:%s/%s";
    public static final String FORMAT2 = "%s://%s:%s/%s/%s";

    public static final String AUTH_URL = String.format(FORMAT1, HTTP, BuildConfig.OBJECT_SERVER_IP, BuildConfig.OBJECT_SERVER_PORT, "auth");
    public static final String REALM_URL = String.format(FORMAT2, REALM, BuildConfig.OBJECT_SERVER_IP, BuildConfig.OBJECT_SERVER_PORT, "%s", "realmtasks");
    public static final String REALM_URL_INVITES_PUBLIC = String.format(FORMAT2, REALM, BuildConfig.OBJECT_SERVER_IP, BuildConfig.OBJECT_SERVER_PORT, "%s","invites_public");
    public static final String REALM_URL_INVITES_PRIVATE = String.format(FORMAT2, REALM, BuildConfig.OBJECT_SERVER_IP, BuildConfig.OBJECT_SERVER_PORT, "%s","invites_private");
    public static final String REALM_URL_SHARE_PRIVATE = String.format(FORMAT2, REALM, BuildConfig.OBJECT_SERVER_IP, BuildConfig.OBJECT_SERVER_PORT, "%s","shares_private");
    public static final String REALM_URL_PERMISSION = String.format(FORMAT2, REALM, BuildConfig.OBJECT_SERVER_IP, BuildConfig.OBJECT_SERVER_PORT, "%s","__permission");

    public static final String REALM_URL_MY = String.format(REALM_URL, "~");
    public static final String REALM_URL_INVITES_PUBLIC_MY = String.format(REALM_URL_INVITES_PUBLIC, "~");
    public static final String REALM_URL_INVITES_PRIVATE_MY = String.format(REALM_URL_INVITES_PRIVATE, "~");
    public static final String REALM_URL_SHARE_PRIVATE_MY = String.format(REALM_URL_SHARE_PRIVATE, "~");
    public static final String REALM_URL_PERMISSION_MY = String.format(REALM_URL_PERMISSION, "~");

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Properties properties = new Properties();
            properties.load(getAssets().open("zerokit.properties"));
            AdminApi.init(properties.getProperty("appbackend", ""), properties.getProperty("clientid", ""));
        } catch (IOException e) {
            throw new RuntimeException("Invalid config file");
        }

        Realm.init(this);
        RealmLog.setLevel(LogLevel.DEBUG);
    }
}
