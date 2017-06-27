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

import com.tresorit.zerokit.Zerokit;
import com.tresorit.zerokit.call.Response;
import com.tresorit.zerokit.response.ResponseZerokitError;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class TaskList extends RealmObject implements Completable {

    public static final String FIELD_ID = "id";
    public static final String FIELD_TEXT = "text";
    public static final String FIELD_COMPLETED = "completed";

    @PrimaryKey @Required
    private String id;
    private String tresorId;
    @Required
    private String text;
    private boolean completed;
    private RealmList<Task> items;

    public String getText() {
        Response<String, ResponseZerokitError> execute = Zerokit.getInstance().decrypt(text).execute();
        return execute.isError() ? text : execute.getResult();
    }

    public void setText(String text) {
        Response<String, ResponseZerokitError> execute = Zerokit.getInstance().encrypt(tresorId, text).execute();
        this.text = execute.isError() ? "" : execute.getResult();
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTresorId() {
        return tresorId;
    }

    public void setTresorId(String tresorId) {
        this.tresorId = tresorId;
    }

    public RealmList<Task> getItems() {
        return items;
    }

    public void setItems(RealmList<Task> items) {
        this.items = items;
    }

    @Override
    public boolean isCompletable() {
        return !getItems().isEmpty();
    }
}
