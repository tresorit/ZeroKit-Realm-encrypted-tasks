# Start your E2EE Realm iOS app from Scratch
This tutorial walks you through how to integrate ZeroKit into your existing Realm iOS app.

## Prerequisite

* E2EE Realm backend installed: if you haven't done it yet, do this first: https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks/blob/master/README.md

## Step 1: Registration
Once you completed the installation of your E2EE Realm backend components, you can start to use ZeroKit's authentication to register and log in your users.

User registration is a 3-step process that you initiate on the client device by calling _`initRegistration`_, then submit user data to the server by calling _`register`_ and finally call _`finishRegistration`_ that calls the server to review the registration request and process it. Typically in this last step, the server verifies your new user's email address or phone number and marks the user as valid, allowing her to log in.

```swift
backend.initRegistration(username: username, profileData: "") { userId, regSessionId, error in
    zeroKit.register(withUserId: userId!, registrationId: regSessionId!, password: password) { regValidationVerifier, error in
        backend.finishRegistration(userId: userId!, validationVerifier: regValidationVerifier!) { error in
            // If finished successfully you can now log in. See the next step.
        }
    }
}
```

Check out the [Registration flow in the ZeroKit SDK's documentation](https://tresorit.com/zerokit/docs/Common_flows.html) for more information

## Step 2: Login
First you have to log in with the ZeroKit user, then you can request credentials to authenticate with Realm using the custom token method.

Here is an example of setting credentials with the ZeroKit auth provider:

```swift
zeroKit.login(withUserId: userId!, password: password, rememberMe: true) { error in
     zeroKit.getIdentityTokens(clientId: ZeroKitManager.shared.clientId) { tokens, error in
        let provider = RLMIdentityProvider(rawValue: "custom/zerokit")
        let credentials = SyncCredentials(customToken: tokens!.authorizationCode, provider: provider)
        SyncUser.logIn(with: credentials, server: SERVER_URL) { user, error in
            // If finished successfully the user is logged in.
        }
    }
}
```

If you are using the the RealmLoginKit framework on iOS then you should implement the AuthenticationProvider protocol provided by the framework. After the ZeroKit login you get the identity tokens, create the credentials and call the completion closure.

```swift
zeroKit.login(withUserId: userId!, password: password, rememberMe: true) { error in
    zeroKit.getIdentityTokens(clientId: ZeroKitManager.shared.clientId) { tokens, error in
        let provider = RLMIdentityProvider(rawValue: "custom/zerokit")
        let credentials = RLMSyncCredentials(customToken: tokens!.authorizationCode, provider: provider, userInfo: nil)
        // Call the AuthenticationProvider completion callback. RealmLoginKit will perform the authentication using the credentials.
        completion(credentials, nil)
    }
}
```

## Step 3: Tresors and realms

ZeroKit manages access to encrypted content using tresors. A tresor is a virtual lockbox that holds keys for users: if you're member of a tresor, you can share it with other users or you can revoke their access to it. The best practice is to create a new tresor for each new realm and when sharing a realm, share the tresor with the same users. This way, all users of an E2EE realm will have access to the encryption keys used to encrypt data in the realm, so they can decrypt data and encrypt updates that go into the realm.

When you create a new E2EE realm, create a new tresor and connect the two by adding a tresorId field to the realm's schema. This way, you'll see which tresor belongs to which realm:

```swift
zeroKit.createTresor { tresorId, error in
    backend.createdTresor(tresorId: tresorId!) { error in
        let realm = try! Realm()
        try! realm.write {
            let item = TaskListList()
            item.tresorId = tresorId!
            realm.add(item)
        }
    }
}
```

## Step 4: Share a realm

To share an E2EE realm with another user, you have to also share the tresor with her. This will enable her to access the keys that the realm was encrypted with. Note: in order to share a tresor, you have to be a member of it.

To share a tresor, retrieve her ID from your realm's schema and share it using the other user's ZeroKit user ID and Realm user ID:

```swift
zeroKit.share(tresorWithId: tresorId, withUser: zeroKitUserId) { operationId, error in
    backend.sharedTresor(operationId: operationId!) { error in
        let permission = RLMSyncPermissionValue(realmPath: REALM_PATH, userID: realmUserId, accessLevel: .write)
        SyncUser.current?.applyPermission(permission) { error in
            // Tresor and realm are shared with the other user.
        }
    }
}
```

Be sure the handle errors when any of the sharing steps fail.

## Step 5: Encrypt and decrypt

If you're a member of a tresor (either you created it or somebody else shared it with you), you can use encrypt and decrypt the realm's data using the keys stored in the tresor. Thankfully, you don't have to deal with the keys: the ZeroKit SDK will take care of that for you. You just encrypt and decrypt:

  * Encrypt data before you put it into the realm,
  * Decrypt data when you read it from the realm:

```swift
// Example Chat message Realm object with encrypted message content.
class ChatMsg: Object {
    private dynamic var msg = ""
    dynamic var sender = ""
    dynamic var tresorId = "" // Unique ID for keychain that holds keys to this chat.
    func getMsg(completion: @escaping (String?, Error?) -> Void) {
        let zeroKit = ... // Aquire your ZeroKit instance.
        zeroKit.decrypt(cipherText: msg) { plainText, error in
            // Either plainText or error holds a value. Error if user doesn’t have access to the keychain.
            completion(plainText, error)
        }
    }
    func setMsg(_ value: String, completion: @escaping (Error?) -> Void) {
        let zeroKit = ... // Aquire your ZeroKit instance.
        zeroKit.encrypt(plainText: value, inTresor: tresorId) { [weak self] cipherText, error in
            if let cipherText = cipherText, let sself = self {
                // Set value in object after encryption.
                try! sself.realm?.write {
                    sself.msg = cipherText
                }
                completion(nil)
            } else {
                // Error if user doesn’t have access to the keychain.
                completion(error)
            }
        }
    }
}
```
