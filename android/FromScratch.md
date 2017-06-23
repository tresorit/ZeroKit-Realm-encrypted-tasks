# Start your E2EE Android app from Scratch
This tutorial walks you through how to integrate ZeroKit into your existing Realm Android app.

## Prerequisite

* E2EE Realm backend installed: if you haven't done it yet, do this first: https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks/blob/master/README.md

## Step 1: Registration
Once you completed the installation of your E2EE Realm backend components, you can start to use ZeroKit's authentication to register and log in your users.

User registration is a 3-step process that you initiate on the client device by calling _`initRegistration`_, then submit user data to the server by calling _`register`_ and finally call _`finishRegistration`_ that calls the server to review the registration request and process it. Typically in this last step, the server verifies your new user's email address or phone number and marks the user as valid, allowing her to log in.

```java
adminApi.initReg(username, profileData).enqueue(respInitReg -> {
    zerokit.register(respInitReg.getUserId(), respInitReg.getRegSessionId(), password.getBytes()).enqueue(respReg -> {
        adminApi.finishReg(respInitReg.getUserId(), respReg.getRegValidationVerifier()).enqueue(aVoid -> {
            // Registration finished successfully you can now log in. See the next step.
            // Log in...
        }, onFailAdminApi);
    }, onFailZerokit);
}, onFailAdminApi);
```

Check out the [Registration flow in the ZeroKit SDK's documentation](https://tresorit.com/zerokit/docs/Common_flows.html) for more information

## Step 2: Login
First you have to log in with the ZeroKit user, then you can request credentials to authenticate with Realm using the custom token method.

Here is an example of setting credentials with the ZeroKit auth provider:

```java
zerokit.getIdentityTokens(clientId).enqueue(identityTokens -> {
    SyncCredentials credentials = SyncCredentials.custom(identityTokens.getAuthorizationCode(), "custom/zerokit", null);
    SyncUser.loginAsync(credentials, AUTH_URL, callback);
}, ...);
```

Once you acquired the _`identityToken`_ from the ZeroKit SDK, you can get the **authorization code** from it, which is a string representation of a token obtained from the authentication server.

As the _`identityProvider`_ parameter, you must pass **"custom/zerokit"** string as shown above.

## Step 3: Tresors and realms

ZeroKit manages access to encrypted content using tresors. A tresor is a virtual lockbox that holds keys for users: if you're member of a tresor, you can share it with other users or you can revoke their access to it. The best practice is to create a new tresor for each new realm and when sharing a realm, share the tresor with the same users. This way, all users of an E2EE realm will have access to the encryption keys used to encrypt data in the realm, so they can decrypt data and encrypt updates that go into the realm.

When you create a new realm, create a new tresor and connect the two by adding a tresorId field to the realm's schema. This way, you'll see which tresor belongs to which realm:

```java
Zerokit.getInstance().createTresor().enqueue(tresorId -> {
    AdminApi.getInstance().createdTresor(tresorId).enqueue(res -> {
        try (Realm realmTasks = Realm.getDefaultInstance()) {
            realmTasks.executeTransaction(realm -> realm.createObject(TaskListList.class, tresorId));
        }
    }, ...);
}, ...);
```

## Step 4: Share a realm

To share an E2EE realm with another user, you have to also share the tresor with her. This will enable her to access the keys that the realm was encrypted with. Note: in order to share a tresor, you have to be a member of it.

To share a tresor, retrieve her ID from your realm's schema and share it using the other user's ZeroKit user ID and Realm user ID:

```java
Zerokit.getInstance().shareTresor(tresorId, zerokitUserIdInvited).enqueue(operationId ->
    AdminApi.getInstance().sharedTresor(operationId).enqueue(v -> {
        try (Realm realmManagement = SyncUser.currentUser().getManagementRealm()) {
            realmManagement.executeTransaction(realm ->
                    realm.insert(new PermissionChange(REALM_URL_MY, realmUserIdInvited, true, true, false)));
        }
    }, ...), ...);
```

## Step 5: Encrypt and decrypt

If you're a member of a tresor (either you created it or somebody else shared it with you), you can use encrypt and decrypt the realm's data using the keys stored in the tresor. Thankfully, you don't have to deal with the keys: the ZeroKit SDK will take care of that for you. You just encrypt and decrypt:

  * Encrypt data before you put it into the realm,
  * Decrypt data when you read it from the realm:


```java
public class chatMsg extends RealmObject {

  private String msg;
  private String sender; 
  private String tresorId; //Unique ID for keychain that holds keys to this chat

  public void setMsg(String msg) {

    //Encrypt message
    Response<String, ResponseZerokitError> execute = Zerokit.getInstance().encrypt(tresorId, msg).execute();

    //Error if user doesn’t have access to the keychain
    this.msg = execute.isError() ? msg : execute.getResult();
  }

  public String getMsg() {
    
    //Decrypt message
    Response<String, ResponseZerokitError> execute = Zerokit.getInstance().decrypt(msg).execute();

    //Error if user doesn’t have access to the keychain
    return execute.isError() ? msg : execute.getResult();
  }

  //...
}
```
