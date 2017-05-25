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

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Required;

public class Task extends RealmObject implements Completable {

    public static final String FIELD_TEXT = "text";
    public static final String FIELD_COMPLETED = "completed";
    public static final String FIELD_DATE = "date";

    @Required
    private String text;
    private boolean completed;
    private String id;
    private Date date;

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        Response<String, ResponseZerokitError> execute = Zerokit.getInstance().decrypt(text).execute();
        return execute.isError() ? text : execute.getResult();
    }

    public void setText(String text) {
        Response<String, ResponseZerokitError> execute = Zerokit.getInstance().encrypt(id, text).execute();
        this.text = execute.isError() ? text : execute.getResult();
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public boolean isCompletable() {
        return true;
    }
}
