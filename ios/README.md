# RealmTasks iOS with Zerokit
The original [RealmTasks iOS](https://github.com/realm-demos/realm-tasks/tree/master/RealmTasks%20Apple) sample app extended with the functionality of [Zerokit SDK](https://github.com/tresorit/ZeroKit-iOS-SDK).

## Features
* Login / Registration using ZeroKit authentication.
* Create / Delete task list.
* Create / Delete task.
* Task list and task texts are encrypted by ZeroKit.

## Requirements
  - **Xcode 8.1+**
  - **Backend:** An installed and working ZeroKit-Realm backend, according to [this description](https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks).

## Configuration
First we will walk you through the backend setup, then we will configure ZeroKit and Realm for the application.

### Backend
The example app requires a backend to function. The previous guide in the root of this repository describes a backend that you can use for this app. You can find the backend and setup instructions [here](https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks).

If your backend is up and running, you can move on with the configuration of the mobile app.

### Zerokit

In the `RealmTasks Shared/ZeroKit/Config.plist` file set the values for `ZeroKitAPIBaseURL`, `ZeroKitClientId` and `ZeroKitAppBackend` keys. If this file does not exist then copy the sample `Config.sample.plist` file in the same directory to create one:

```xml
<key>ZeroKitAPIBaseURL</key>
<string>{TenantBaseUrl}</string>
<key>ZeroKitClientId</key>
<string>{ClientId}</string>
<key>ZeroKitAppBackend</key>
<string>{AppBackendUrl}</string>
```

- `ZeroKitAPIBaseURL`: This is your tenant's service URL. You can find this URL on the management portal.
- `ZeroKitClientId`: This is the client ID for your OpenID Connect client that you wish to use with your mobile.
- `ZeroKitAppBackend`: This is the URL of the sample application backend.

_**Note:** You can find these values on the basic configuration page of your tenant at [https://manage.tresorit.io](https://manage.tresorit.io)_

### Realm
In the `RealmTasks Shared/Constants.swift` file configure the `IP address` and `port` for your **Realm Object Server**.

```swift
// Specify your Realm object server IP address, eg. "10.0.100.43". The default `localIPAddress` is the IP address of your device.
static let syncHost = localIPAddress
// Specify your Realm object server port. Default is 9080.
static let port = 9080
```

Now you are ready to **Build and Run** (**⌘R**) the example in Xcode.

## Application

The app contains the source code of the original sample app of the [RealmTasks iOS](https://github.com/realm-demos/realm-tasks/tree/master/RealmTasks%20Apple) extended with the **Zerokit SDK**

* The text content of the `Task` and `TaskList` objects are encrypted by ZeroKit, so unauthorized users cannot access this data.
* `ZeroKit`: The `ZeroKitManager` singleton provides access to a `ZeroKit` instance to handle encryption and decryption of data, and user authentication with the ZeroKit service.
* `Backend`: Most of the cryptographic operations (including invites and sharing) must be done client side by the SDK library. To provide control over these operations, and to prevent possible abuse by tampering the client, we introduced the admin API. All client initiated changes which has a permanent effect on the server has to be approved through the Admin API (typically by the server backend of the integrated app). The communication with the backend is implemented in the `Backend.swift` file.
    
### Third Party Dependencies
- [ZeroKit](https://github.com/tresorit/ZeroKit-iOS-SDK): ZeroKit is a simple, breach-proof user authentication and end-to-end encryption library.
- [RealmSwift](https://github.com/realm/realm-cocoa/tree/master/RealmSwift): Realm Swift enables you to efficiently write your app’s model layer in a safe, persisted and fast way.
- [RealmLoginKit](https://github.com/realm-demos/realm-loginkit): A generic interface for logging in to Realm Mobile Platform apps.
- [Cartography](https://github.com/robb/Cartography): A declarative Auto Layout DSL for Swift.
- [SwiftLint](https://github.com/realm/SwiftLint): A tool to enforce Swift style and conventions.