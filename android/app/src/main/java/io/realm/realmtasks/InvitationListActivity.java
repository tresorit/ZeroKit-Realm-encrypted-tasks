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

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.realmtasks.list.InvitationListAdapter;
import io.realm.realmtasks.model.User;
import io.realm.realmtasks.view.RecyclerViewWithEmptyViewSupport;

public class InvitationListActivity extends BaseListenerActivity {

    private Realm realm;
    private RecyclerViewWithEmptyViewSupport recyclerView;
    private InvitationListAdapter adapter;
    private RealmResults<User> list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_list);
        recyclerView = (RecyclerViewWithEmptyViewSupport) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setEmptyView(findViewById(R.id.empty_view));
        getSupportActionBar().setTitle("Shared with me");
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter = null;
        realm = Realm.getInstance(UserManager.getSyncConfigurationForInvitationPrivate());

        list = realm.where(User.class).findAll();
        list.addChangeListener(this::updateList);
        updateList(list);
    }

    private void updateList(RealmResults<User> results) {
        if (results.size() > 0 && adapter == null) {
            adapter = new InvitationListAdapter(InvitationListActivity.this, results);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    protected void onStop() {
        list.removeChangeListeners();
        adapter = null;
        realm.removeAllChangeListeners();
        realm.close();
        realm = null;
        super.onStop();
    }

}
