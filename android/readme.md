# End-to-End Encrypted RealmTasks Android app
In this tutorial, we'll get you set up with an end-to-end encrypted version of the **[RealmTasks app](https://github.com/realm-demos/realm-tasks/tree/master/RealmTasks%20Android)** with **[ZeroKit-Android-SDK](https://github.com/tresorit/ZeroKit-Android-SDK)**, a basic task management app, designed as a homage to [Realmac Software's Clear app](http://realmacsoftware.com/clear), with their knowledge and permission.

Would you rather start from scratch? [Check out our from-scratch guide](FromScratch.md).
<img src="../.images/android-sample-screenshot.png" alt="Android App screenshot" width="444"/>

## Content
- [Features](#features)
- [Requirements](#requirements)
- [Configuration](#configuration)
- [Application](#application)
- [Best-practices](#best-practices)

## Features
* Login / Registration using ZeroKit authentication
* Create / Delete task list
* Create / Delete task
* Task list and task texts are encrypted by ZeroKit
* Share task lists
* Revoke sharing

## Requirements
  - **Android SDK:** The _Zerokit SDK_ library is compatible from API 21 (Android 5.0 - Lollipop).
  - **IDE:** You will need an installed [Android Studio](https://developer.android.com/studio/index.html) to open and build the project.
  - **Backend:** An installed and working ZeroKit-Realm backend, according to [this description](https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks).
  
## Configuration
#### Step 1: Download app
**Download the sample app** by cloning this repository or downloading as a [zip file](https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks/archive/master.zip).
#### Step 2: Connect app to ZeroKit and Realm
Open up your ZeroKit management portal at [https://manage.tresorit.io](https://manage.tresorit.io) you'll need the following values for your app config:

<img src="/.images/zerokit-basic-settings.png" width="80%">

Scroll down to open up your realm identity provider:

<img src="/.images/zerokit-basic-idpclientedit.png" width="80%">

This is where you can grab the Client ID parameter from:

<img src="/.images/zerokit-idp-copy1.png" width="50%">

#### Step 3: Config file
Before build, you have to configure the application to work together with your backend services.

In the `app/src/main/zerokit.properties` set the values of `baseurl`, `clientid`, `appbackend` and `objectserver`. If this file does not exist, let’s create one with the same name.

```
baseurl=your base url (e.g. https://{tenantid}.api.tresorit.io)
clientid=client id for your openid
appbackend= url of the sample application backend (e.g. http://10.0.2.2:3000)
objectserver=ip with port of your object server (e.g. 10.0.100.10:9080)
```
- `baseurl`: This is your _ZeroKit Service URL_. You can find this URL on the management portal.
- `clientid`: This is the _client ID_ for your _OpenID Connect client_ that you wish to use with your mobile app. You can find this value on the basic configuration page of your _tenant_ at [here](https://manage.tresorit.io)
- `appbackend`: This is the address of the sample _application backend_. The example app requires a _backend_ to function. The previous guide in the root of this repository describes a _backend_ that you can use for this app. You can find the _backend_ and setup instructions [here](https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks).
- `objectserver`: This is the address of your _realm object server_. The previous guide in the root of this repository describes how to configure your _object server_, or you can find some information [here](https://realm.io/docs/realm-object-server/)

_**Note**: the Android emulator doesn't like 127.0.0.1 addresses for servers running on the same box. Use 10.0.2.2 instead, see this [StackOverflow post](https://stackoverflow.com/questions/5806220/how-to-connect-to-my-http-localhost-web-server-from-android-emulator-in-eclips) for more info._

It is **IMPORTANT** to **Clean project** after you finished the modifications of the config file.

Now you are ready to **Build and Run** the example in **Android Studio**.

#### Step 3: Test-drive the app
Once the app started up, **REGISTER** a user with a **test-user-** prefix, such as _test-user-Alice_:

<img src="/.images/android-sample-login.png" width="40%"><img src="/.images/android-sample-reg.png" width="40%">

_**Note**: the test-user- prefix will get your user account automatically approved in your sandbox tenant. Otherwise, your new user registration will stuck without validation and won't be able to log in._

Once logged in with the user, create a new task list with the (plus) sign, than open the list and add task items:

<img src="/.images/android-sample-list.png" width="40%"><img src="/.images/android-sample-tasks.png" width="40%">

**These task items are now all end-to-end encrypted. Check out the tasks' titles using a Realm Object Browser**:

Browse to your Realm server's Realms list: http://localhost:9080/#!/realms and find Alice's *realmtasks* realm. Copy the link's address:

<img src="/.images/realm-dashboard-realms.png" width="50%">

Open Realm Object Browser, paste the Url into the Realm URL box and delete the highlighted part. Enter your Realm admin username & password and hit Open:

<img src="/.images/realm-browser-login.png" width="50%">

Check out the Tasks collection inside the realm, it's end-to-end encrypted!

<img src="/.images/realm-browser-e2ee.png" width="50%">

You can now log out, create another test-user- user and share task lists with each other as in this [video](https://www.youtube.com/watch?v=NF8ZePUwpoM).
The ZeroKit SDK seamlessly handles the encryption keys for the realms.

## Application
Below is how the app works. if you want to build your app from scratch, check out the Start from [Scratch guidance](https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks/blob/master/android/FromScratch.md)

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