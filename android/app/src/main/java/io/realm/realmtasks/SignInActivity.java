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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tresorit.zerokit.AdminApi;
import com.tresorit.zerokit.Zerokit;
import com.tresorit.zerokit.call.Action;
import com.tresorit.zerokit.call.Response;
import com.tresorit.zerokit.response.IdentityTokens;
import com.tresorit.zerokit.response.ResponseAdminApiError;
import com.tresorit.zerokit.response.ResponseZerokitError;

import java.util.Objects;

import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
import io.realm.permissions.PermissionChange;
import io.realm.realmtasks.auth.zerokit.ZerokitAuth;
import io.realm.realmtasks.model.TaskListList;
import io.realm.realmtasks.model.User;
import io.realm.realmtasks.util.JSONObject;

import static io.realm.realmtasks.RealmTasksApplication.AUTH_URL;
import static io.realm.realmtasks.RealmTasksApplication.REALM_URL_INVITES_PUBLIC_MY;
import static io.realm.realmtasks.UserManager.FIELD_REALMURL;
import static io.realm.realmtasks.UserManager.getSyncConfigurationForTasks;
import static io.realm.realmtasks.model.User.FIELD_REALMUSERID;

public class SignInActivity extends AppCompatActivity implements SyncUser.Callback {

    public static final String ACTION_IGNORE_CURRENT_USER = "action.ignoreCurrentUser";

    private View progressView;
    private View loginFormView;
    private ZerokitAuth zerokitAuth;
    private MenuItem itemRegister;

    private Action<ResponseZerokitError> onFailZerokit;
    private Action<ResponseAdminApiError> onFailAdminApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.activity_sign_in_label);

        AutoCompleteTextView usernameView = (AutoCompleteTextView) findViewById(R.id.username);
        EditText passwordView = (EditText) findViewById(R.id.password);
        final Button signInButton = (Button) findViewById(R.id.sign_in_button);

        loginFormView = findViewById(R.id.sign_in_form);
        progressView = findViewById(R.id.sign_in_progress);

        onFailZerokit = responseZerokitError -> {
            showProgress(false);
            Toast.makeText(SignInActivity.this, responseZerokitError.toString(), Toast.LENGTH_LONG).show();
        };
        onFailAdminApi = responseAdminApiError -> {
            showProgress(false);
            Toast.makeText(SignInActivity.this, responseAdminApiError.toString(), Toast.LENGTH_LONG).show();
        };

        // Check if we already got a user, if yes, just continue automatically
        if (savedInstanceState == null) {
            if (!ACTION_IGNORE_CURRENT_USER.equals(getIntent().getAction())) {
                showProgress(true);
                final SyncUser user = SyncUser.currentUser();
                Response<String, ResponseZerokitError> response = Zerokit.getInstance().whoAmI().execute();
                if (user != null && !Objects.equals(response.getResult(), "null")) {
                    loginComplete();
                } else {
                    showProgress(false);
                    Zerokit.getInstance().logout().execute();
                    if (SyncUser.currentUser() != null)
                        SyncUser.currentUser().logout();
                }
            }
        }

        zerokitAuth = new ZerokitAuth(signInButton, usernameView, passwordView, null) {
            @Override
            public void onRegistrationComplete(IdentityTokens result) {
                UserManager.setAuthMode(UserManager.AUTH_MODE.ZEROKIT);
                SyncCredentials credentials = SyncCredentials.custom(result.getAuthorizationCode(), "custom/zerokit", null);
                SyncUser.loginAsync(credentials, AUTH_URL, SignInActivity.this);
            }

            @Override
            public void onError(String s) {
                showProgress(false);
                Toast.makeText(SignInActivity.this, s, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onStart() {
                showProgress(true);
            }
        };
    }

    private void loginComplete() {
        createInitialDataIfNeeded(() -> {
            Intent listActivity = new Intent(SignInActivity.this, TaskListActivity.class);
            startActivities(new Intent[]{listActivity});
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_signin, menu);
        itemRegister = menu.findItem(R.id.register);
        itemRegister.setEnabled(progressView.getVisibility() == View.GONE);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.register) {
            startActivity(new Intent(this, RegisterActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void showProgress(final boolean show) {
        if (itemRegister != null)
            itemRegister.setEnabled(!show);
        final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        loginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onSuccess(SyncUser user) {
        loginComplete();
    }

    @Override
    public void onError(ObjectServerError error) {
        showProgress(false);
        String errorMsg;
        switch (error.getErrorCode()) {
            case UNKNOWN_ACCOUNT:
                errorMsg = "Account does not exists.";
                break;
            case INVALID_CREDENTIALS:
                errorMsg = "The provided credentials are invalid!"; // This message covers also expired account token
                break;
            default:
                errorMsg = error.toString();
        }
        Toast.makeText(SignInActivity.this, errorMsg, Toast.LENGTH_LONG).show();
    }

    private void createInitialDataIfNeeded(final Callback actionSuccess) {
        Realm.getInstance(UserManager.getSyncConfigurationForInvitationPublic()).close();
        try (Realm realmManagement = SyncUser.currentUser().getManagementRealm()) {
            realmManagement.executeTransaction(realm -> {
                if (realm.where(PermissionChange.class).equalTo(FIELD_REALMURL, REALM_URL_INVITES_PUBLIC_MY).count() == 0)
                    realm.insert(new PermissionChange(REALM_URL_INVITES_PUBLIC_MY, "*", true, true, false));
            });
        }

        setUpZerokit(() -> {
            try (Realm realmDefault = Realm.getInstance(getSyncConfigurationForTasks())) {
                if (realmDefault.where(TaskListList.class).count() == 0) {
                    Zerokit.getInstance().createTresor().enqueue(tresorId ->
                            AdminApi.getInstance().createdTresor(tresorId).enqueue(res -> {
                                try (Realm realm = Realm.getInstance(getSyncConfigurationForTasks())) {
                                    realm.executeTransaction(realm1 -> realm1.createObject(TaskListList.class, tresorId));
                                }
                                actionSuccess.call();
                            }, onFailAdminApi), onFailZerokit);
                } else actionSuccess.call();
            }
        });
    }

    private void setUpZerokit(Callback callback){
        final AdminApi adminApi = AdminApi.getInstance();
        final Zerokit zerokit = Zerokit.getInstance();
        if (adminApi.getToken() == null || UserManager.getCurrentUser() == null)
        zerokit.getIdentityTokens(adminApi.getClientId()).enqueue(identityTokens ->
                adminApi.login(identityTokens.getAuthorizationCode()).enqueue(responseAdminLogin -> {
                    adminApi.setToken(responseAdminLogin.getId());
                    zerokit.whoAmI().enqueue(zerokitUserId ->
                            adminApi.getPublicProfile(zerokitUserId).enqueue(publicProfile ->
                                    adminApi.getProfile().enqueue(profile -> {
                                        String realmUserId = SyncUser.currentUser().getIdentity();
                                        UserManager.setCurrentUser(new User(realmUserId, zerokitUserId, new JSONObject(profile).getString("alias")));
                                        JSONObject jsonObject = new JSONObject(publicProfile);
                                        if (TextUtils.isEmpty(jsonObject.getString(FIELD_REALMUSERID))) {
                                            jsonObject.put(FIELD_REALMUSERID, realmUserId);
                                            adminApi.storePublicProfile(jsonObject.toString()).enqueue(res ->
                                                    callback.call(), onFailAdminApi);
                                        } else
                                            callback.call();
                                    }, onFailAdminApi), onFailAdminApi), onFailZerokit);
                }, onFailAdminApi), onFailZerokit);
        else callback.call();
    }

    private interface Callback{
        void call();
    }

}

