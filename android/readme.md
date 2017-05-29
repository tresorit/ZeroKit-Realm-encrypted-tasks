## Realm Task Android with Zerokit
The original sample app of the [Realm Task Android](https://github.com/realm-demos/realm-tasks/tree/master/RealmTasks%20Android) extended with the functionality of **Zerokit SDK**
<img src="../.images/android-sample-screenshot.png" alt="Android App screenshot" width="444"/>

## Content
- [Features](#features)
- [Requirements](#requirements)
- [Configuration](#configuration)
- [Application](#application)
- [Best-practices](#best-practices)

## Features
* Login / Registration
* Create / Delete task list.
* Create / Delete task.
* Share task list
* Revoke share

## Requirements
  - **Android SDK:** The _Zerokit SDK_ library is compatible from API 21 (Android 5.0 - Lollipop).
  - **IDE:** You will need an installed _Android Studio_ to open and build the project.
  - **Backend:** An installed and working ZeroKit-Realm backend, according to [this description](https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks).

## Configuration
Before build, you have to configure the application to work together with your backend services.

In the `app/src/main/zerokit.properties` set the values of `baseurl`, `clientid`, `appbackend` and `objectserver`. If this file does not exist, let’s create one with the same name.

```
baseurl=your base url (e.g. https://{tenantid}.api.tresorit.io)
clientid=client id for your openid
appbackend= protocol, ip and port of the sample application backend (e.g. http://10.0.2.2:3000)
objectserver=ip and port of your object server without protocol (e.g. 10.0.100.10:9080)
```
- `baseurl`: This is your _tenant_'s _service URL_. You can find this URL on the management portal.
- `clientid`: This is the _client ID_ for your _OpenID Connect client_ that you wish to use with your mobile app. You can find this value on the basic configuration page of your _tenant_ at [here](https://manage.tresorit.io)
- `appbackend`: This is the address of the sample _application backend_. The example app requires a _backend_ to function. The previous guide in the root of this repository describes a _backend_ that you can use for this app. You can find the _backend_ and setup instructions [here](https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks).
- `objectserver`: This is the address of your _realm object server_. The previous guide in the root of this repository describes how to configure your _object server_, or you can find some information [here](https://realm.io/docs/realm-object-server/)

Now you are ready to **Build and Run** the example in **Android Studio**.

## Application

#### Modules
* **adminapi**: Most of the cryptographic operations (including invites and sharing) must be done client side by the _SDK_ library. To provide control over these operations, and to prevent possible abuse by tampering the client, we introduced the _admin API_. All client initiated changes which has a permanent effect on the server has to be approved through the _Admin API_ (typically by the server backend of the integrated app).

* **app**: Contains the source code of the original sample app of the [Realm Task Android](https://github.com/realm-demos/realm-tasks/tree/master/RealmTasks%20Android) extended with the **Zerokit SDK**
    * The _`TaskListList`_ is shareable, so it is now possible to provide access rights for different users to handle the same task list.
    * The text fields of _`Task.java`_ and _`TaskList.java`_ are handled in encrypted way, so unauthorized users can no access to these data.
    * The logged in user can share his/her list with an other user. The revocation of these shares are also possible.
    * The user can see two member lists, in the first can be found those members who have shared their task list with the current user, in the second one are located those users who have access to the current user's task list.
    
#### Registering Test Users
Register test users following the `test-user-{XYZ}` username format. These users will be automatically validated by the sample backend so you can log in right after registration.

#### Used 3rd party libraries
- [Retrofit](https://github.com/square/retrofit): Type-safe HTTP client for Android and Java
- [okhttp](https://github.com/square/okhttp): An HTTP+HTTP/2 client for Android and Java applications
- [Retrolambda](https://github.com/evant/gradle-retrolambda): A gradle plugin for getting java lambda support in java 6, 7 and android

## Best-practices
#### Login and Registration
Authentication is used to establish the identity of users and log them in. The credential information for a given user can be acquired with a token through a our custom authentication provider.
Here is an example of setting credentials with the _zerokit auth provider_
```java
zerokit.getIdentityTokens(clientId).enqueue(identityTokens -> {
    SyncCredentials credentials = SyncCredentials.custom(identityTokens.getAuthorizationCode(), "custom/zerokit", null);
    SyncUser.loginAsync(credentials, AUTH_URL, callback);
}, ...);
```
After you acquire the `identityToken` from the _zerokit SDK_, you can get the _authorization code_ from it which is a string representation of a token obtained from the authentication server. 
As the `identityProvider` parameter you must pass the `"custom/zerokit"` string as you can see above.

#### Tresors and Realms
After you create a new realm, it is a good practice to connect a `tresor` to it and add a `tresorId` field to the realm schema, which will identify the `tresor` which belongs to the mentioned realm.

```java
Zerokit.getInstance().createTresor().enqueue(tresorId -> {
    AdminApi.getInstance().createdTresor(tresorId).enqueue(res -> {
        try (Realm realmTasks = Realm.getDefaultInstance()) {
            realmTasks.executeTransaction(realm -> realm.createObject(TaskListList.class, tresorId));
        }
    }, ...);
}, ...);
```

#### Share
If you would like to share a realm with an other user which is encrypted, you have to also share the `tresor`. Otherwise only the encrypted content will be visible for the users.
If you earlier stored the id of the `tresor`, you can easily share it with the other user.

```java
Zerokit.getInstance().shareTresor(tresorId, zerokitUserIdInvited).enqueue(operationId ->
    AdminApi.getInstance().sharedTresor(operationId).enqueue(v -> {
        try (Realm realmManagement = SyncUser.currentUser().getManagementRealm()) {
            realmManagement.executeTransaction(realm ->
                    realm.insert(new PermissionChange(REALM_URL_MY, realmUserIdInvited, true, true, false)));
        }
    }, ...), ...);
```

#### Encrypt and Decrypt
If you own a `tresor` or somebody shared it with you, you can easily use it to encrypt and decrypt a content.
```java
Zerokit.getInstance().decrypt(text).execute();
Zerokit.getInstance().encrypt(tresorId, text).execute();
```