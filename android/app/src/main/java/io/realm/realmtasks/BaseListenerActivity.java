package io.realm.realmtasks;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.realmtasks.model.User;

import static io.realm.realmtasks.model.User.FIELD_REALMUSERID;

public class BaseListenerActivity extends AppCompatActivity {

    private Realm realmInvitationPublic;

    private RealmResults<User> listInvitation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setSubtitle(UserManager.getCurrentUser().getUserName());
    }

    @Override
    protected void onStart() {
        super.onStart();

        realmInvitationPublic = Realm.getInstance(UserManager.getSyncConfigurationForInvitationPublic());
        listInvitation = realmInvitationPublic.where(User.class).findAll();
        listInvitation.addChangeListener(this::updateInvitationList);
        updateInvitationList(listInvitation);
    }

    private void updateInvitationList(RealmResults<User> invitations) {
        try (Realm realmInvitationPrivate = Realm.getInstance(UserManager.getSyncConfigurationForInvitationPrivate())) {
            realmInvitationPrivate.executeTransaction(realm -> {
                for (User invitation : invitations) {
                    RealmResults<User> users = realm.where(User.class).equalTo(FIELD_REALMUSERID, invitation.getRealmUserId()).findAll();
                    if (users.isEmpty()) {
                        if (!TextUtils.isEmpty(invitation.getZerokitUserId()))
                            realm.insert(new User(invitation.getRealmUserId(), invitation.getZerokitUserId(), invitation.getUserName()));
                    }else {
                        if (TextUtils.isEmpty(invitation.getZerokitUserId()))
                            users.deleteAllFromRealm();
                    }
                }
            });
        }
        if (!invitations.isEmpty())
            realmInvitationPublic.executeTransaction(realm ->
                    invitations.deleteAllFromRealm());
    }

    @Override
    protected void onStop() {
        super.onStop();
        listInvitation.removeChangeListeners();
        realmInvitationPublic.removeAllChangeListeners();
        realmInvitationPublic.close();
        realmInvitationPublic = null;
    }

    private ProgressDialog progressDialog;

    protected void showProgress(boolean show) {
        if (!show && progressDialog != null) {
            progressDialog.dismiss();
        } else if (show && progressDialog == null) {
            progressDialog = ProgressDialog.show(this, null, "Loading... Please wait!", true, false);
            progressDialog.setOnDismissListener(dialog -> progressDialog = null);
        }
    }
}
