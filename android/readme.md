# End-to-End Encrypted RealmTasks Android app

In this tutorial, we'll get you set up with an end-to-end encrypted version of the [RealmTasks app](https://github.com/realm-demos/realm-tasks/tree/master/RealmTasks%20Android), a basic task management app, designed as a homage to [Realmac Software's Clear app](http://realmacsoftware.com/clear), with their knowledge and permission.

Would you rather start from scratch? [Check out our from-scratch guide](https://github.com/davidszabo26/ZeroKit-Realm-encrypted-tasks/blob/master/android/FromScratch.md).

<img src="https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks/blob/master/.images/android-sample-screenshot.png" width="40%">

## Features
* Login / Registration using ZeroKit authentication.
* Create / Delete task list.
* Create / Delete task.
* Task list and task texts are encrypted by ZeroKit.
* Share task lists.
* Revoke sharing.

## Prerequisites

* E2EE Realm backend installed: if you haven't done it yet, do this first: https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks/blob/master/README.md
* IDE: You will need [Android Studio](https://developer.android.com/studio/index.html) to open and build the project.

## Step 1: Download app
**Download the sample app** from https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks/tree/master/android to your computer

Open it up in Android Studio

## Step 2: Connect app to ZeroKit and Realm
Open up your ZeroKit management portal at https://manage.tresorit.io; you'll need the following values for your app config:

<img src="/.images/zerokit-basic-settings.png">

Scroll down to open up your realm identity provider:

<img src="/.images/zerokit-basic-idpclientedit.png">

This is where you can grab the Client ID parameter from:

<img src="/.images/zerokit-idp-copy1.png" width="60%">

**Go back to Android Studio and open the zerokit.properties** config file under *app/src/main*:
**Replace all 4 values**: 

```yml
baseurl=Your ZeroKit Service URL (see above)
clientid=The Client ID from the Realm client (see above)
appbackend=URL of your ZeroKit Node backend, by default it's http://10.0.2.2:3000 - see Note below
objectserver=IP of your ROS with port, by default it's 10.0.2.2:9080 - see Note below
```

* **Note**: the Android emulator doesn't like 127.0.0.1 addresses for servers running on the same box. Use 10.0.2.2 instead; see this StackOverflow post for more info.

## Step 3: Test-drive the app
Choose one of the latest emulator devices, for example **Google's Pixel XL**, and run the app:

<img src="/.images/android-sample-login.png" width="40%">

Once the app started up, **REGISTER a user with a test-user-** prefix, such as **test-user-Alice**:

<img src="/.images/android-sample-reg.png" width="40%">

**Note**: the test-user- prefix will get your user account automatically approved in your sandbox tenant. Otherwise, your new user registration will stuck without validation and won't be able to log in.

Once logged in with the user, create a new task list with the (plus) sign:

<img src="/.images/android-sample-list.png" width="40%">

Open the list and add task items:

<img src="/.images/android-sample-tasks.png" width="40%">

**These task items are now all end-to-end encrypted. Check out the tasks' titles using a Realm Object Browser**:

Browse to your Realm server's Realms list: http://localhost:9080/#!/realms and find Alice's *realmtasks* realm. Copy the link's address:

<img src="/.images/realm-dashboard-realms.png" width="80%">

Open Realm Object Browser, paste the Url into the Realm URL box and delete the highlighted part. Enter your Realm admin username & password and hit Open:

<img src="/.images/realm-browser-login.png" width="40%">

Check out the Tasks collection inside the realm, it's end-to-end encrypted!

<img src="/.images/realm-browser-e2ee.png" width="50%">

You can now log out with the → sign, create another test-user- user and share task lists with each other as in this video.
The ZeroKit SDK seamlessly handles the encryption keys for the realms.

## How the app works?
Below is how the app works. if you want to build your app from scratch, check out the Start from Scratch guidance: [link]

**Modules**

* adminapi: Most of the cryptographic operations (including invites and sharing) are done client side via the ZeroKit SDK library. To provide control over these operations and to prevent possible abuse by tampering with the client app, we introduced the ZeroKit admin API. All client-initiated changes with a permanent effect (such as registering users or sharing data) have to be approved through the Admin API (typically by your ZeroKit backend). These changes are automatically approved in this demo setup. For more information on making your ZeroKit deployment production-read, check out the bottom of your E2EE Realm backend deployment guide.
* **app**: Contains the source code of the original sample app of the [Realm Task Android](https://github.com/realm-demos/realm-tasks/tree/master/RealmTasks%20Android) extended with the **Zerokit SDK**
    * The _`TaskListList`_ is shareable, so it is now possible to provide access rights for different users to handle the same task list.
    * The text fields of _`Task.java`_ and _`TaskList.java`_ are handled in encrypted way, so unauthorized users can no access to these data.
    * The logged in user can share his/her list with an other user. The revocation of these shares are also possible.
    * The user can see two member lists, in the first can be found those members who have shared their task list with the current user, in the second one are located those users who have access to the current user's task list.

## 3rd-party libraries used
* Retrofit: Type-safe HTTP client for Android and Java
* okhttp: An HTTP+HTTP/2 client for Android and Java applications
* Retrolambda: A gradle plugin for getting java lambda support in java 6, 7 and android
