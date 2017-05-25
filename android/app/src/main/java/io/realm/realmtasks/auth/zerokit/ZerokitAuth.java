package io.realm.realmtasks.auth.zerokit;

import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.tresorit.zerokit.AdminApi;
import com.tresorit.zerokit.Zerokit;
import com.tresorit.zerokit.call.Action;
import com.tresorit.zerokit.response.IdentityTokens;
import com.tresorit.zerokit.response.ResponseAdminApiError;
import com.tresorit.zerokit.response.ResponseZerokitError;
import com.tresorit.zerokit.util.JSONObject;

import io.realm.realmtasks.R;

import static android.text.TextUtils.isEmpty;

public abstract class ZerokitAuth {

    private final Action<ResponseAdminApiError> onFailAdminApi;
    private final Action<ResponseZerokitError> onFailZerokit;

    public ZerokitAuth(Button button, final AutoCompleteTextView usernameView, final EditText passwordView, final EditText passwordConfirmationView) {
        final boolean newUser = passwordConfirmationView != null;

        onFailZerokit = responseZerokitError -> onError(responseZerokitError.toString());

        onFailAdminApi = responseAdminApiError -> onError(responseAdminApiError.toString());


        button.setOnClickListener(v -> {
            if (!needToCancel(usernameView, passwordView, passwordConfirmationView)) {
                onStart();
                final String username = usernameView.getText().toString();
                final String password = passwordView.getText().toString();

                if (newUser)
                    register(username, password);
                else
                    login(username, password);
            }
        });
    }

    private void register(final String username, final String password) {
        AdminApi adminApi = AdminApi.getInstance();
        Zerokit zerokit = Zerokit.getInstance();
        String profileData = new JSONObject()
                .put("autoValidate", true)
                .put("canCreateTresor", true)
                .put("alias", username)
                .toString();
        adminApi.initReg(username, profileData).enqueue(respInitReg -> {
            zerokit.register(respInitReg.getUserId(), respInitReg.getRegSessionId(), password.getBytes()).enqueue(respReg -> {
                adminApi.finishReg(respInitReg.getUserId(), respReg.getRegValidationVerifier()).enqueue(aVoid -> {
                    login(username, password);
                }, onFailAdminApi);
            }, onFailZerokit);
        }, onFailAdminApi);
    }

    private void login(final String username, final String password) {
        final Zerokit zerokit = Zerokit.getInstance();
        final AdminApi adminApi = AdminApi.getInstance();
        adminApi.getUserId(username).enqueue(s -> {
            zerokit.login(s, password.getBytes()).enqueue(resp -> {
                zerokit.getIdentityTokens(adminApi.getClientId()).enqueue(result -> {
                    onRegistrationComplete(result);
                }, onFailZerokit);
            }, onFailZerokit);
        }, onFailAdminApi);
    }

    private boolean needToCancel(final AutoCompleteTextView usernameView, final EditText passwordView, final EditText passwordConfirmationView) {
        usernameView.setError(null);
        passwordView.setError(null);

        final String email = usernameView.getText().toString();
        final String password = passwordView.getText().toString();


        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            passwordView.setError(passwordView.getResources().getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            usernameView.setError(usernameView.getResources().getString(R.string.error_field_required));
            focusView = usernameView;
            cancel = true;
        }

        if (passwordConfirmationView != null) {
            final String passwordConfirmation = passwordConfirmationView.getText().toString();
            if (isEmpty(passwordConfirmation)) {
                passwordConfirmationView.setError(passwordConfirmationView.getResources().getString(R.string.error_field_required));
                focusView = passwordConfirmationView;
                cancel = true;
            }

            if (!password.equals(passwordConfirmation)) {
                passwordConfirmationView.setError(passwordConfirmationView.getResources().getString(R.string.error_incorrect_password));
                focusView = passwordConfirmationView;
                cancel = true;
            }
        }

        if (cancel) {
            focusView.requestFocus();
        }
        return cancel;
    }

    public abstract void onRegistrationComplete(final IdentityTokens result);


    public void onError(String s) {
    }

    public void onStart() {
    }
}
