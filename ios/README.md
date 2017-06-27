# End-to-End Encrypted RealmTasks iOS app

In this tutorial, we'll get you set up with an end-to-end encrypted version of the [RealmTasks app](https://github.com/realm-demos/realm-tasks/tree/master/RealmTasks%20Apple), a basic task management app, designed as a homage to [Realmac Software's Clear app](http://realmacsoftware.com/clear), with their knowledge and permission.

Would you rather start from scratch? [Check out our from-scratch guide](FromScratch.md).

<img src="/.images/ios-sample-tasks.png" width="30%">

## Features
* Login / Registration using ZeroKit authentication.
* Create / Delete task list.
* Create / Delete task.
* Task list and task texts are encrypted by ZeroKit.
* Share task lists.
* Revoke sharing.

## Prerequisites

* E2EE Realm backend installed: if you haven't done it yet, do this first: https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks/blob/master/README.md
* Xcode 8.1+

## Step 1: Download app
**Download the sample app** by cloning this repository or downloading as a [zip file](https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks/archive/master.zip).

Open up `ios/RealmTasks.xcworkspace` in Xcode.

## Step 2: Connect app to ZeroKit and Realm
Open up your ZeroKit management portal at https://manage.tresorit.io; you'll need the following values for your app config:

<img src="/.images/zerokit-basic-settings.png">

Scroll down to open up your realm identity provider:

<img src="/.images/zerokit-basic-idpclientedit.png">

This is where you can grab the Client ID parameter from:

<img src="/.images/zerokit-idp-copy1.png" width="60%">

In the `RealmTasks Shared/Config.plist` file set the values for `ZeroKitServiceURL`, `ZeroKitClientId`, `ZeroKitAppBackend`, `RealmPort` and `RealmHost`:

```xml
<key>RealmPort</key>
<integer>9080</integer>
<key>RealmHost</key>
<string>Your realm host, eg. 10.0.2.2</string>
<key>ZeroKitServiceURL</key>
<string>This is your Service URL https://abcde12345.api.tresorit.io</string>
<key>ZeroKitClientId</key>
<string>This is your Client ID abcde12345_fghij67890</string>
<key>ZeroKitAppBackend</key>
<string>This is your ZeroKit node backend http://10.0.2.2:3000</string>
```

## Step 3: Test-drive the app
You are now ready to **Build and Run** (**⌘R**) the app in Xcode:

<img src="/.images/ios-sample-login.png" width="40%">

Once the app started up, **REGISTER a user with a test-user-** prefix, such as **test-user-Alice**:

<img src="/.images/ios-sample-reg.png" width="40%">

**Note**: the test-user- prefix will get your user account automatically approved in your sandbox tenant. Otherwise, your new user registration will stuck without validation and won't be able to log in.

Once logged in with the user a default empty task list, called **My Tasks**, is automatically created for you:

<img src="/.images/ios-sample-tasks-empty.png" width="40%">

Tap on the empty list or pull down to add new task items to your list:

<img src="/.images/ios-sample-tasks.png" width="40%">

**These task items are now all end-to-end encrypted. Check out the tasks' titles using a Realm Object Browser**:

Browse to your Realm server's Realms list: http://localhost:9080/#!/realms and find Alice's *realmtasks* realm. Copy the link's address:

<img src="/.images/realm-dashboard-realms.png" width="80%">

Open Realm Object Browser, paste the URL into the Realm URL box and delete the highlighted part. Enter your Realm admin username & password and hit Open:

<img src="/.images/realm-browser-login.png" width="40%">

Check out the Tasks collection inside the realm, it's end-to-end encrypted!

<img src="/.images/realm-browser-e2ee.png" width="50%">

You can now log out on the **Account** tab, create another test-user- user and share task lists with each other.
The ZeroKit SDK seamlessly handles the encryption keys for the realms.

## How the app works?

The app contains the source code of the original [RealmTasks iOS app](https://github.com/realm-demos/realm-tasks/tree/master/RealmTasks%20Apple) extended with the **Zerokit SDK**.

* The text content of the `Task` and `TaskList` objects are encrypted by ZeroKit, so unauthorized users cannot access this data.
* The `TaskListList` is shareable, so it is now possible to provide access rights for different users to handle the same task list.
* The logged in user can share his/her list with an other user. The revocation of these shares are also possible.
* The user can see two sharing lists:
  * *My Shares*: This contains the users who have access to the current user's task lists. Here sharing can be initiated by tapping the *+* button.
  * *Shared with Me*: This contains the users who shared their task lists with the current user.
* `ZeroKit`: The `ZeroKitManager` singleton provides access to a `ZeroKit` instance to handle encryption and decryption of data, and user authentication with the ZeroKit service.
* `Backend`: Most of the cryptographic operations (including invites and sharing) must be done client side by the SDK library. To provide control over these operations, and to prevent possible abuse by tampering the client, we introduced the admin API. All client initiated changes which has a permanent effect on the server has to be approved through the Admin API (typically by the server backend of the integrated app). The communication with the backend is implemented in the `Backend.swift` file.

### 3rd-party libraries used

- [ZeroKit](https://github.com/tresorit/ZeroKit-iOS-SDK): ZeroKit is a simple, breach-proof user authentication and end-to-end encryption library.
- [RealmSwift](https://github.com/realm/realm-cocoa/tree/master/RealmSwift): Realm Swift enables you to efficiently write your app’s model layer in a safe, persisted and fast way.
- [RealmLoginKit](https://github.com/realm-demos/realm-loginkit): A generic interface for logging in to Realm Mobile Platform apps.
- [Cartography](https://github.com/robb/Cartography): A declarative Auto Layout DSL for Swift.
- [SwiftLint](https://github.com/realm/SwiftLint): A tool to enforce Swift style and conventions.
