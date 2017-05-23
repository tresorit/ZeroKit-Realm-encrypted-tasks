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

package io.realm.realmtasks.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {



    public static final String FIELD_REALMUSERID = "realmUserId";
    public static final String FIELD_ZEROKITUSERID = "zerokitUserId";
    public static final String FIELD_USERNAME = "userName";

    @PrimaryKey
    private String realmUserId;
    private String zerokitUserId;
    private String userName;

    public User() {
    }

    public User(String realmUserId, String zerokitUserId, String userName) {
        this.realmUserId = realmUserId;
        this.zerokitUserId = zerokitUserId;
        this.userName = userName;
    }

    public String getRealmUserId() {
        return realmUserId;
    }

    public String getZerokitUserId() {
        return zerokitUserId;
    }

    public String getUserName() {
        return userName;
    }

    public void setRealmUserId(String realmUserId) {
        this.realmUserId = realmUserId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setZerokitUserId(String zerokitUserId) {
        this.zerokitUserId = zerokitUserId;
    }
}
