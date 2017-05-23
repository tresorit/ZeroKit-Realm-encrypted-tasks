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

import android.text.TextUtils;

import com.tresorit.zerokit.AdminApi;
import com.tresorit.zerokit.Zerokit;

import java.util.HashMap;
import java.util.Map;

import io.realm.SyncConfiguration;
import io.realm.SyncUser;
import io.realm.realmtasks.model.User;

import static io.realm.realmtasks.RealmTasksApplication.REALM_URL;
import static io.realm.realmtasks.RealmTasksApplication.REALM_URL_INVITES_PRIVATE_MY;
import static io.realm.realmtasks.RealmTasksApplication.REALM_URL_INVITES_PUBLIC;
import static io.realm.realmtasks.RealmTasksApplication.REALM_URL_INVITES_PUBLIC_MY;
import static io.realm.realmtasks.RealmTasksApplication.REALM_URL_MY;
import static io.realm.realmtasks.RealmTasksApplication.REALM_URL_PERMISSION_MY;
import static io.realm.realmtasks.RealmTasksApplication.REALM_URL_SHARE_PRIVATE_MY;

public class UserManager {

    public static final String FIELD_USERID = "userId";
    public static final String FIELD_REALMURL = "realmUrl";
    private static final int SCHEMA_VERSION = 7;

    private static SyncConfiguration syncConfigurationInvitationPublic;
    private static SyncConfiguration syncConfigurationInvitationPrivate;
    private static SyncConfiguration syncConfigurationShare;
    private static SyncConfiguration syncConfigurationPermission;
    private static User currentUser;

    private static Map<String, SyncConfiguration> syncConfigurationMap = new HashMap<>();

    // Supported authentication mode
    public enum AUTH_MODE {
        ZEROKIT
    }

    private static AUTH_MODE mode = AUTH_MODE.ZEROKIT; // default

    public static void setAuthMode(AUTH_MODE m) {
        mode = m;
    }

    public static void logoutActiveUser() {
        switch (mode) {
            case ZEROKIT: {
                Zerokit.getInstance().logout();
                AdminApi.getInstance().clearToken();
                setCurrentUser(null);
                syncConfigurationInvitationPublic = null;
                syncConfigurationInvitationPrivate = null;
                syncConfigurationShare = null;
                syncConfigurationMap.clear();
                break;
            }
        }
        SyncUser.currentUser().logout();
    }

    public static SyncConfiguration getSyncConfigurationForTasks() {
        return getSyncConfigurationForTasks("");
    }

    public static SyncConfiguration getSyncConfigurationForTasks(String userId) {
        if (userId == null) userId = "";
        if (!syncConfigurationMap.containsKey(userId)) {
            syncConfigurationMap.put(userId, new SyncConfiguration.Builder(SyncUser.currentUser(), TextUtils.isEmpty(userId) ? REALM_URL_MY : String.format(REALM_URL, userId)).schemaVersion(SCHEMA_VERSION).build());
        }
        return syncConfigurationMap.get(userId);
    }

    public static SyncConfiguration getSyncConfigurationForPermission() {
        if (syncConfigurationPermission == null)
            syncConfigurationPermission = new SyncConfiguration.Builder(SyncUser.currentUser(), REALM_URL_PERMISSION_MY).build();
        return syncConfigurationPermission;
    }

    public static SyncConfiguration getSyncConfigurationForInvitationPublic() {
        if (syncConfigurationInvitationPublic == null)
            syncConfigurationInvitationPublic = getSyncConfigurationForInvitationPublic(null);
        return syncConfigurationInvitationPublic;
    }

    public static SyncConfiguration getSyncConfigurationForInvitationPublic(String userId) {
        return new SyncConfiguration.Builder(SyncUser.currentUser(), TextUtils.isEmpty(userId) ? REALM_URL_INVITES_PUBLIC_MY : String.format(REALM_URL_INVITES_PUBLIC, userId)).schemaVersion(SCHEMA_VERSION).build();
    }

    public static SyncConfiguration getSyncConfigurationForShare() {
        if (syncConfigurationShare == null)
            syncConfigurationShare = new SyncConfiguration.Builder(SyncUser.currentUser(), REALM_URL_SHARE_PRIVATE_MY).build();
        return syncConfigurationShare;
    }

    public static SyncConfiguration getSyncConfigurationForInvitationPrivate() {
        if (syncConfigurationInvitationPrivate == null)
            syncConfigurationInvitationPrivate = new SyncConfiguration.Builder(SyncUser.currentUser(), REALM_URL_INVITES_PRIVATE_MY).schemaVersion(SCHEMA_VERSION).build();
        return syncConfigurationInvitationPrivate;
    }

    public static void setCurrentUser(User currentUser) {
        UserManager.currentUser = currentUser;
    }

    public static User getCurrentUser() {
        return currentUser;
    }
}
