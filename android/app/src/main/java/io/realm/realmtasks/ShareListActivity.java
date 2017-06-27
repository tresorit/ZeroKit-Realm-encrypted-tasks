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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tresorit.zerokit.AdminApi;
import com.tresorit.zerokit.Zerokit;
import com.tresorit.zerokit.call.Action;
import com.tresorit.zerokit.response.ResponseAdminApiError;
import com.tresorit.zerokit.response.ResponseZerokitError;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.SyncUser;
import io.realm.permissions.PermissionChange;
import io.realm.realmtasks.list.ShareListAdapter;
import io.realm.realmtasks.model.User;
import io.realm.realmtasks.util.JSONObject;
import io.realm.realmtasks.view.RecyclerViewWithEmptyViewSupport;

import static io.realm.realmtasks.RealmTasksApplication.REALM_URL_MY;
import static io.realm.realmtasks.model.User.FIELD_REALMUSERID;

public class ShareListActivity extends BaseListenerActivity {

    public static final String EXTRA_TRESOR_ID = "extra.tresor_id";

    private Realm realm;
    private RecyclerViewWithEmptyViewSupport recyclerView;
    private ShareListAdapter adapter;
    private RealmResults<User> list;
    private String tresorId;
    private Action<ResponseZerokitError> onFailZerokit;
    private Action<ResponseAdminApiError> onFailAdminApi;
    private Action<User> deleteUserAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_list);
        recyclerView = (RecyclerViewWithEmptyViewSupport) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setEmptyView(findViewById(R.id.empty_view));
        getSupportActionBar().setTitle("Invited by me");

        final Intent intent = getIntent();
        if (!intent.hasExtra(EXTRA_TRESOR_ID)) {
            throw new IllegalArgumentException(EXTRA_TRESOR_ID + " required");
        }
        tresorId = intent.getStringExtra(EXTRA_TRESOR_ID);

        onFailZerokit = responseZerokitError -> {
            showProgress(false);
            Toast.makeText(ShareListActivity.this, responseZerokitError.toString(), Toast.LENGTH_LONG).show();
        };
        onFailAdminApi = responseAdminApiError -> {
            showProgress(false);
            Toast.makeText(ShareListActivity.this, responseAdminApiError.toString(), Toast.LENGTH_LONG).show();
        };

        deleteUserAction = user -> {
            showProgress(true);
            Zerokit.getInstance().kickFromTresor(tresorId, user.getZerokitUserId()).enqueue(operationId ->
                    AdminApi.getInstance().kickedUser(operationId).enqueue(aVoid -> {
                        String realmUserId = user.getRealmUserId();
                        try (Realm realmShare = Realm.getInstance(UserManager.getSyncConfigurationForShare())) {
                            realmShare.executeTransaction(realm ->
                                    realm.where(User.class).equalTo(User.FIELD_REALMUSERID, realmUserId).findAll().deleteAllFromRealm());
                        }
                        try (Realm realmManagement = SyncUser.currentUser().getManagementRealm()) {
                            realmManagement.executeTransaction(realm ->
                                    realm.insert(new PermissionChange(REALM_URL_MY, realmUserId, false, false, false)));
                        }
                        try (Realm realmSharePublicInvited = Realm.getInstance(UserManager.getSyncConfigurationForInvitationPublic(realmUserId))) {
                            User currentUser = UserManager.getCurrentUser();
                            realmSharePublicInvited.executeTransaction(realm -> {
                                RealmResults<User> users = realm.where(User.class).equalTo(FIELD_REALMUSERID, currentUser.getRealmUserId()).findAll();
                                if (users.size() == 0)
                                    realm.insert(new User(currentUser.getRealmUserId(), "", ""));
                                else users.get(0).setZerokitUserId("");
                            });
                        }
                        showProgress(false);
                    }, onFailAdminApi), onFailZerokit);
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter = null;
        realm = Realm.getInstance(UserManager.getSyncConfigurationForShare());
        list = realm.where(User.class).findAll();
        list.addChangeListener(this::updateList);
        updateList(list);
    }

    private void updateList(RealmResults<User> results) {
        if (results.size() > 0 && adapter == null) {
            adapter = new ShareListAdapter(ShareListActivity.this, results, deleteUserAction);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AdminApi adminApi = AdminApi.getInstance();
        Zerokit zerokit = Zerokit.getInstance();
        switch (item.getItemId()) {

            case R.id.action_share:
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);

                final EditText editText = new EditText(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                int margin = this.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
                layoutParams.setMargins(margin, margin, margin, margin);
                linearLayout.addView(editText, layoutParams);
                new AlertDialog.Builder(this)
                        .setPositiveButton("Share", (dialog, which) -> {
                            showProgress(true);
                            String userNameInvited = editText.getText().toString();
                            adminApi.getUserId(userNameInvited).enqueue(zerokitUserIdInvited -> adminApi.getPublicProfile(zerokitUserIdInvited).enqueue(publicProfileInvited ->
                                    zerokit.shareTresor(tresorId, zerokitUserIdInvited).enqueue(operationId ->
                                            adminApi.sharedTresor(operationId).enqueue(aVoid -> {
                                                String realmUserIdInvited = new JSONObject(publicProfileInvited).getString(FIELD_REALMUSERID);
                                                try (Realm realmShare = Realm.getInstance(UserManager.getSyncConfigurationForShare())) {
                                                    realmShare.executeTransaction(realm1 -> {
                                                        if (realm1.where(User.class).equalTo(FIELD_REALMUSERID, realmUserIdInvited).count() == 0)
                                                            realm1.insert(new User(realmUserIdInvited, zerokitUserIdInvited, userNameInvited));
                                                    });
                                                }
                                                try (Realm realmManagement = SyncUser.currentUser().getManagementRealm()) {
                                                    realmManagement.executeTransaction(realm ->
                                                            realm.insert(new PermissionChange(REALM_URL_MY, realmUserIdInvited, true, true, false)));
                                                }
                                                try (Realm realmSharePublicInvited = Realm.getInstance(UserManager.getSyncConfigurationForInvitationPublic(realmUserIdInvited))) {
                                                    User currentUser = UserManager.getCurrentUser();
                                                    realmSharePublicInvited.executeTransaction(realm -> {
                                                        if (realm.where(User.class).equalTo(FIELD_REALMUSERID, currentUser.getRealmUserId()).count() == 0)
                                                            realm.insert(new User(currentUser.getRealmUserId(), currentUser.getZerokitUserId(), currentUser.getUserName()));
                                                    });
                                                }
                                                showProgress(false);
                                            }, onFailAdminApi), onFailZerokit), onFailAdminApi), onFailAdminApi);
                        })
                        .setView(linearLayout)
                        .setNegativeButton("Cancel", null)
                        .setTitle("Share tasks")
                        .show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
