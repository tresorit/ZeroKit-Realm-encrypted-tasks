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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;

public class RealmTasksApplication extends Application {

    private static final Pattern patternHttp = Pattern.compile("^(https?)://(.*?)");

    public static final String OBJECT_SERVER;
    public static final String PROTOCOL;
    public static final String REALM = "realm";
    public static final String HTTP = "http";

    static {
        Matcher matcher = patternHttp.matcher(BuildConfig.OBJECT_SERVER);
        if (matcher.matches()) {
            PROTOCOL = matcher.group(0);
            OBJECT_SERVER = matcher.group(1);
        } else {
            PROTOCOL = HTTP;
            OBJECT_SERVER = BuildConfig.OBJECT_SERVER;
        }
    }

    public static final String FORMAT1 = "%s://%s/%s";
    public static final String FORMAT2 = "%s://%s/%s/%s";

    public static final String AUTH_URL = String.format(FORMAT1, PROTOCOL, OBJECT_SERVER, "auth");
    public static final String REALM_URL = String.format(FORMAT2, REALM, OBJECT_SERVER, "%s", "realmtasks");
    public static final String REALM_URL_INVITES_PUBLIC = String.format(FORMAT2, REALM, OBJECT_SERVER, "%s", "invites_public");
    public static final String REALM_URL_INVITES_PRIVATE = String.format(FORMAT2, REALM, OBJECT_SERVER, "%s", "invites_private");
    public static final String REALM_URL_SHARE_PRIVATE = String.format(FORMAT2, REALM, OBJECT_SERVER, "%s", "shares_private");
    public static final String REALM_URL_PERMISSION = String.format(FORMAT2, REALM, OBJECT_SERVER, "%s", "__permission");

    public static final String REALM_URL_MY = String.format(REALM_URL, "~");
    public static final String REALM_URL_INVITES_PUBLIC_MY = String.format(REALM_URL_INVITES_PUBLIC, "~");
    public static final String REALM_URL_INVITES_PRIVATE_MY = String.format(REALM_URL_INVITES_PRIVATE, "~");
    public static final String REALM_URL_SHARE_PRIVATE_MY = String.format(REALM_URL_SHARE_PRIVATE, "~");
    public static final String REALM_URL_PERMISSION_MY = String.format(REALM_URL_PERMISSION, "~");

    @Override
    public void onCreate() {
        super.onCreate();
        AdminApi.init(BuildConfig.APP_BACKEND, BuildConfig.CLIENT_ID);
        Realm.init(this);
        RealmLog.setLevel(LogLevel.DEBUG);
    }
}
