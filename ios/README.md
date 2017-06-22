# RealmTasks iOS with Zerokit
The original [RealmTasks iOS](https://github.com/realm-demos/realm-tasks/tree/master/RealmTasks%20Apple) sample app extended with the functionality of [Zerokit SDK](https://github.com/tresorit/ZeroKit-iOS-SDK).

## Features
* Login / Registration using ZeroKit authentication.
* Create / Delete task list.
* Create / Delete task.
* Task list and task texts are encrypted by ZeroKit.
* Share task lists.
* Revoke sharing.

## Requirements
  - **Xcode 8.1+**
  - **Backend:** An installed and working ZeroKit-Realm backend, according to [this description](https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks).

## Configuration
First we will walk you through the backend setup, then we will configure ZeroKit and Realm for the application.

### Backend
The example app requires a backend to function. The previous guide in the root of this repository describes a backend that you can use for this app. You can find the backend and setup instructions [here](https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks).

If your backend is up and running, you can move on with the configuration of the mobile app.

### Zerokit & Realm

In the `RealmTasks Shared/Config.plist` file set the values for `ZeroKitAPIBaseURL`, `ZeroKitClientId`, `ZeroKitAppBackend`, `RealmPort` and `RealmHost` keys:

```xml
<key>RealmPort</key>
<integer>9080</integer>
<key>RealmHost</key>
<string>{Your realm host, eg. IP address}</string>
<key>ZeroKitServiceURL</key>
<string>{Your ZeroKit Service URL}</string>
<key>ZeroKitClientId</key>
<string>{Your ZeroKit Client ID}</string>
<key>ZeroKitAppBackend</key>
<string>{Your ZeroKit Sample Backend URL}</string>
```

- `RealmPort`: Your Realm Object Server port. Default is 9080.
- `RealmHost`: Your Realm Object Server host, eg. the IP address such as "10.0.100.43".
- `ZeroKitAPIBaseURL`: This is your tenant's service URL. You can find this URL on the [management portal](https://manage.tresorit.io).
- `ZeroKitClientId`: This is the client ID for your OpenID Connect client that you wish to use with your mobile. You can manage OpenID connect clients on the [management portal](https://manage.tresorit.io).
- `ZeroKitAppBackend`: This is the URL of the sample application backend.

Now you are ready to **Build and Run** (**⌘R**) the example in Xcode.

## Application

The app contains the source code of the original sample app of the [RealmTasks iOS](https://github.com/realm-demos/realm-tasks/tree/master/RealmTasks%20Apple) extended with the **Zerokit SDK**.

* The text content of the `Task` and `TaskList` objects are encrypted by ZeroKit, so unauthorized users cannot access this data.
* The `TaskListList` is shareable, so it is now possible to provide access rights for different users to handle the same task list.
* The logged in user can share his/her list with an other user. The revocation of these shares are also possible.
* The user can see two sharing lists:
  * *My Shares*: This contains the users who have access to the current user's task lists. Here sharing can be initiated by tapping the *+* button.
  * *Shared with Me*: This contains the users who shared their task lists with the current user.
* `ZeroKit`: The `ZeroKitManager` singleton provides access to a `ZeroKit` instance to handle encryption and decryption of data, and user authentication with the ZeroKit service.
* `Backend`: Most of the cryptographic operations (including invites and sharing) must be done client side by the SDK library. To provide control over these operations, and to prevent possible abuse by tampering the client, we introduced the admin API. All client initiated changes which has a permanent effect on the server has to be approved through the Admin API (typically by the server backend of the integrated app). The communication with the backend is implemented in the `Backend.swift` file.

### Third Party Dependencies
- [ZeroKit](https://github.com/tresorit/ZeroKit-iOS-SDK): ZeroKit is a simple, breach-proof user authentication and end-to-end encryption library.
- [RealmSwift](https://github.com/realm/realm-cocoa/tree/master/RealmSwift): Realm Swift enables you to efficiently write your app’s model layer in a safe, persisted and fast way.
- [RealmLoginKit](https://github.com/realm-demos/realm-loginkit): A generic interface for logging in to Realm Mobile Platform apps.
- [Cartography](https://github.com/robb/Cartography): A declarative Auto Layout DSL for Swift.
- [SwiftLint](https://github.com/realm/SwiftLint): A tool to enforce Swift style and conventions.

## Contact

Contact us at [zerokit@tresorit.com](mailto:zerokit@tresorit.com).
