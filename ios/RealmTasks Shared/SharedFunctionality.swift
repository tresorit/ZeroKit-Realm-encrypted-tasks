////////////////////////////////////////////////////////////////////////////
//
// Copyright 2016 Realm Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////

import Foundation
import RealmSwift
import ZeroKit

// Private Helpers

private var deduplicationNotificationToken: NotificationToken! // FIXME: Remove once core supports ordered sets: https://github.com/realm/realm-core/issues/1206

private var authenticationFailureCallback: (() -> Void)?

public func setDefaultRealmConfiguration(with user: SyncUser, completion: @escaping (Error?) -> Void) {
    SyncManager.shared.errorHandler = { error, session in
        if let authError = error as? SyncAuthError, authError.code == .invalidCredential {
            authenticationFailureCallback?()
        }
    }

    Realm.Configuration.defaultConfiguration = Realm.Configuration(
        syncConfiguration: SyncConfiguration(user: user, realmURL: Constants.syncServerURL!),
        objectTypes: [TaskListList.self, TaskList.self, Task.self]
    )
    let realm = try! Realm()

    if realm.isEmpty {
        let zeroKit = ZeroKitManager.shared.zeroKit
        let backend = ZeroKitManager.shared.backend

        zeroKit.createTresor { tresorId, error in
            if let error = error {
                print("Create tresor error:", error)
                completion(error)
                return
            }

            backend.createdTresor(tresorId: tresorId!) { error in
                if let error = error {
                    print("Approve tresor error:", error)
                    completion(error)
                    return
                }

                zeroKit.encrypt(plainText: Constants.defaultListName, inTresor: tresorId!) { cipherText, error in
                    if let error = error {
                        print("Error encrypting default list name:", error)
                        completion(error)
                        return
                    }

                    try! realm.write {
                        let list = TaskList()
                        list.id = Constants.defaultListID
                        list.tresorId = tresorId!
                        list.text = cipherText!
                        let listLists = TaskListList()
                        listLists.tresorId = tresorId!
                        listLists.items.append(list)
                        realm.add(listLists)
                    }

                    registerDeduplication(realm)
                    completion(nil)
                }
            }
        }
    } else {
        registerDeduplication(realm)
        completion(nil)
    }
}

private func registerDeduplication(_ realm: Realm) {
    // FIXME: Remove once core supports ordered sets: https://github.com/realm/realm-core/issues/1206
    deduplicationNotificationToken = realm.addNotificationBlock { _, realm in
        let items = realm.objects(TaskListList.self).first!.items
        guard items.count > 1 && !realm.isInWriteTransaction else { return }
        let itemsReference = ThreadSafeReference(to: items)
        // Deduplicate
        DispatchQueue(label: "io.realm.RealmTasks.bg").async {
            let realm = try! Realm(configuration: realm.configuration)
            guard let items = realm.resolve(itemsReference), items.count > 1 else {
                return
            }
            realm.beginWrite()
            let listReferenceIDs = NSCountedSet(array: items.map { $0.id })
            for id in listReferenceIDs where listReferenceIDs.count(for: id) > 1 {
                let id = id as! String
                let indexesToRemove = items.enumerated().flatMap { index, element in
                    return element.id == id ? index : nil
                }
                indexesToRemove.dropFirst().reversed().forEach(items.remove(objectAtIndex:))
            }
            try! realm.commitWrite()
        }
    }
}

// Internal Functions

func isDefaultRealmConfigured() -> Bool {
    return try! !Realm().isEmpty
}

// returns true on success
func canConfigureDefaultRealm() -> Bool {
    let userIds = ZeroKitManager.shared.zeroKitAndRealmUserId()

    if let user = SyncUser.current,
        let currentRealmUserId = user.identity,
        let savedRealmUserId = userIds.realmUserId,
        let zeroKitUserId = userIds.zeroKitUserId,
        currentRealmUserId == savedRealmUserId,
        ZeroKitManager.shared.zeroKit.canLoginByRememberMe(with: zeroKitUserId) {

        return true
    }

    return false
}

func configureDefaultRealm(completion: @escaping (Bool) -> Void) {
    let userIds = ZeroKitManager.shared.zeroKitAndRealmUserId()

    if let user = SyncUser.current,
        let zeroKitUserId = userIds.zeroKitUserId,
        canConfigureDefaultRealm() {

        ZeroKitManager.shared.zeroKit.loginByRememberMe(with: zeroKitUserId) { error in
            if let error = error {
                completion(false)
                print("ZeroKit loginByRememberMe failed:", error)
            } else {
                updateLoggedInUser(zeroKitUserId: zeroKitUserId, realmUser: user)
                setDefaultRealmConfiguration(with: user) { error in
                    completion(error == nil)
                }
            }
        }
    } else {
        completion(false)
    }
}

func logout(completion: @escaping (NSError?) -> Void) {
    ZeroKitManager.shared.zeroKit.logout { error in
        SyncUser.current?.logOut()
        completion(error)
    }
}

func updateLoggedInUser(zeroKitUserId: String?, realmUser: SyncUser?) {
    if let zeroKitUserId = zeroKitUserId,
        let realmUserId = realmUser?.identity {
        ZeroKitManager.shared.set(zeroKitUserId: zeroKitUserId, realmUserId: realmUserId)
    } else {
        ZeroKitManager.shared.removeZeroKitUserIdAndRealmId()
    }
}

func resetDefaultRealm() {
    guard let user = SyncUser.current else {
        return
    }

    deduplicationNotificationToken.stop()

    user.logOut()
}

func setAuthenticationFailureCallback(callback: (() -> Void)?) {
    authenticationFailureCallback = callback
}

func authenticate(username: String, password: String, register: Bool, callback: @escaping (NSError?) -> Void) {
    let credentials = SyncCredentials.usernamePassword(username: username, password: password, register: register)
    SyncUser.logIn(with: credentials, server: Constants.syncAuthURL) { user, error in
        DispatchQueue.main.async {
            if let user = user, error == nil {
                setDefaultRealmConfiguration(with: user) { error in
                    callback(error as NSError?)
                }
            } else {
                callback(error as NSError?)
            }
        }
    }
}

private extension NSError {

    convenience init(error: NSError, description: String?, recoverySuggestion: String?) {
        var userInfo = error.userInfo

        userInfo[NSLocalizedDescriptionKey] = description
        userInfo[NSLocalizedRecoverySuggestionErrorKey] = recoverySuggestion

        self.init(domain: error.domain, code: error.code, userInfo: userInfo)
    }

}
