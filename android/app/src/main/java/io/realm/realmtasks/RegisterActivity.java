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
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tresorit.zerokit.response.IdentityTokens;

import io.realm.ObjectServerError;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
import io.realm.realmtasks.auth.zerokit.ZerokitAuth;
import io.realm.realmtasks.util.TimingLogger;

import static io.realm.realmtasks.RealmTasksApplication.AUTH_URL;

public class RegisterActivity extends AppCompatActivity implements SyncUser.Callback {

    public static final String TAG = "TIMING";

    private View progressView;
    private View registerFormView;
    private ZerokitAuth zerokitAuth;

    private TimingLogger timingLoginAsync = new TimingLogger(TAG, "Login realm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        AutoCompleteTextView usernameView = (AutoCompleteTextView) findViewById(R.id.username);
        EditText passwordView = (EditText) findViewById(R.id.password);
        EditText passwordConfirmationView = (EditText) findViewById(R.id.password_confirmation);
        final Button mailRegisterButton = (Button) findViewById(R.id.email_register_button);

        registerFormView = findViewById(R.id.register_form);
        progressView = findViewById(R.id.register_progress);

        zerokitAuth = new ZerokitAuth(mailRegisterButton, usernameView, passwordView, passwordConfirmationView) {
            @Override
            public void onRegistrationComplete(IdentityTokens result) {
                UserManager.setAuthMode(UserManager.AUTH_MODE.ZEROKIT);
                SyncCredentials credentials = SyncCredentials.custom(result.getAuthorizationCode(), "custom/zerokit", null);
                timingLoginAsync.reset();
                SyncUser.loginAsync(credentials, AUTH_URL, RegisterActivity.this);
            }

            @Override
            public void onError(String s) {
                super.onError(s);
                showProgress(false);
                Toast.makeText(RegisterActivity.this, s, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onStart() {
                super.onStart();
                showProgress(true);
            }
        };
    }

    private void registrationComplete() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void showProgress(final boolean show) {
        final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        registerFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
        timingLoginAsync.addSplit("finished");
        timingLoginAsync.dumpToLog();
        registrationComplete();
    }

    @Override
    public void onError(ObjectServerError error) {
        String errorMsg;
        switch (error.getErrorCode()) {
            case EXISTING_ACCOUNT: errorMsg = "Account already exists"; break;
            default:
                errorMsg = error.toString();
        }
        Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
    }
}
