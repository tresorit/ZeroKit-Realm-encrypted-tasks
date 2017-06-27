import UIKit
import Realm
import RealmSwift
import ZeroKit

class MySharesViewController: ShareBaseTableViewController {
    var alert: UIAlertController?

    override func awakeFromNib() {
        super.awakeFromNib()
        self.title = "My Shares"
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        realm = sharesRealm()!
        tokens.append(realm.addNotificationBlock { [weak self] _, _ in
            self?.updateList()
        })
        updateList()
    }

    private func updateList() {
        items = realm?.objects(User.self).sorted(byKeyPath: "userName")
        tableView.reloadData()
    }

    @IBAction func addButtonTap(_ sender: Any) {
        let alert = UIAlertController(title: "Share My Tasks", message: "Enter user name to share:", preferredStyle: .alert)
        alert.addTextField(configurationHandler: nil)
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        alert.addAction(UIAlertAction(title: "Share", style: .default, handler: { [weak self, weak alert] _ in
            let username = alert?.textFields?.first?.text ?? ""
            self?.share(with: username)
        }))
        present(alert, animated: true, completion: nil)
    }

    private func share(with username: String) {
        let errorHandler = { [weak self] (message: String) in
            self?.dismissMessage {
                self?.showError(title: "Error sharing with user '\(username)'.", message: message)
            }
        }

        present(message: "Sharing...")

        getUserObjects(for: username) { myUser, otherUser, error in
            guard let myUser = myUser,
                let otherUser = otherUser else {
                    errorHandler("Failed to get user IDs.")
                    return
            }
            guard myUser.realmUserId! != otherUser.realmUserId! else {
                errorHandler("You cannot share with yourself.")
                return
            }

            try! self.realm.write {
                self.realm.create(User.self, value: otherUser, update: true)
            }

            let permission = self.realmPermission(for: otherUser.realmUserId!)
            SyncUser.current?.applyPermission(permission) { error in
                guard error == nil else {
                    errorHandler("Failed to add permission for realmtasks realm.")
                    return
                }

                let tresorId = (try! Realm()).objects(TaskListList.self).first!.tresorId!
                self.shareTresor(tresorId: tresorId, userId: otherUser.zerokitUserId!) { error in
                    guard error == nil else {
                        errorHandler("Failed sharing tresor.")
                        return
                    }

                    let invitesPublic = invitesPublicRealm(for: otherUser.realmUserId!)!
                    try! invitesPublic.write {
                        invitesPublic.create(User.self, value: myUser, update: true)
                    }
                    invitesPublic.refresh()

                    self.dismissMessage()
                }
            }
        }
    }

    private func revokeAccess(from username: String) {
        let errorHandler = { [weak self] (message: String) in
            self?.dismissMessage {
                self?.showError(title: "Error revoking access from user '\(username)'.", message: message)
            }
        }

        present(message: "Revoking access...")

        getUserObjects(for: username) { myUser, otherUser, error in
            guard let myUser = myUser,
                let otherUser = otherUser else {
                    errorHandler("Failed to get users.")
                    return
            }
            guard myUser.realmUserId! != otherUser.realmUserId! else {
                errorHandler("You cannot revoke access from yourself.")
                return
            }

            // Set ID empty when revoking access
            myUser.zerokitUserId = ""

            let invitesPublic = invitesPublicRealm(for: otherUser.realmUserId!)!
            try! invitesPublic.write {
                invitesPublic.create(User.self, value: myUser, update: true)
            }
            invitesPublic.refresh()

            let tresorId = (try! Realm()).objects(TaskListList.self).first!.tresorId!
            self.kickUser(userId: otherUser.zerokitUserId!, from: tresorId) { error in
                guard error == nil else {
                    errorHandler("Failed to kick user from tresor.")
                    return
                }

                let permission = self.realmPermission(for: otherUser.realmUserId!)
                SyncUser.current?.revokePermission(permission) { _ in
//                    guard error == nil else {
//                        errorHandler("Failed to revoke permission from realmtasks realm.")
//                        return
//                    }

                    try! self.realm.write {
                        let result = self.realm.objects(User.self).filter("realmUserId == %@", otherUser.realmUserId!)
                        if result.count > 0 {
                            self.realm.delete(result)
                        }
                    }

                    self.dismissMessage()
                }
            }
        }
    }

    private func getUserObjects(for username: String, completion: @escaping (User?, User?, Error?) -> Void) {
        ZeroKitManager.shared.backend.getProfile { profile, error in
            guard let profile = profile,
                let profileData = profile.data(using: .utf8),
                let json = (try? JSONSerialization.jsonObject(with: profileData, options: [])) as? [String: Any],
                let myUsername = json[ProfileField.alias.rawValue] as? String else {
                    completion(nil, nil, error)
                    return
            }
            ZeroKitManager.shared.zeroKit.whoAmI { myUserId, error in
                guard let myUserId = myUserId,
                    let myRealmId = SyncUser.current?.identity else {
                    completion(nil, nil, error)
                    return
                }
                let myUser = User()
                myUser.userName = myUsername
                myUser.realmUserId = myRealmId
                myUser.zerokitUserId = myUserId
                self.getOtherUser(for: username) { otherUser, error in
                    guard let otherUser = otherUser else {
                        completion(nil, nil, error)
                        return
                    }
                    completion(myUser, otherUser, nil)
                }
            }
        }
    }

    private func getOtherUser(for username: String, completion: @escaping (User?, Error?) -> Void) {
        ZeroKitManager.shared.backend.getUserId(forUsername: ZeroKitAuth.testUsername(from: username)) { userId, error in
            guard let userId = userId else {
                completion(nil, error)
                return
            }
            self.getRealmUserId(for: userId) { realmUserId, error in
                if let realmUserId = realmUserId {
                    let user = User()
                    user.userName = username
                    user.realmUserId = realmUserId
                    user.zerokitUserId = userId
                    completion(user, nil)
                } else {
                    completion(nil, error)
                }
            }
        }
    }

    private func getRealmUserId(for zeroKitUserId: String, completion: @escaping (String?, Error?) -> Void) {
        ZeroKitManager.shared.backend.getPublicProfile(for: zeroKitUserId) { profileStr, error in
            guard let data = profileStr?.data(using: .utf8),
                let json = try? JSONSerialization.jsonObject(with: data, options: []),
                let dict = json as? [String: Any],
                let realmUserId = dict[PublicProfileField.realmUserId.rawValue] as? String else {
                    completion(nil, error)
                    return
            }
            completion(realmUserId, nil)
        }
    }

    private func realmPermission(for realmUserId: String) -> RLMSyncPermissionValue {
        let defaultRealm = try! Realm()
        let path = defaultRealm.configuration.syncConfiguration!.realmURL.path
        let permission = RLMSyncPermissionValue(realmPath: path, userID: realmUserId, accessLevel: .write)
        return permission
    }

    private func shareTresor(tresorId: String, userId: String, completion: @escaping (Error?) -> Void) {
        ZeroKitManager.shared.zeroKit.share(tresorWithId: tresorId, withUser: userId) { operationId, error in
            if let operationId = operationId {
                ZeroKitManager.shared.backend.sharedTresor(operationId: operationId) { error in
                    completion(error)
                }
            } else if error! == ZeroKitError.alreadyMember {
                completion(nil)
            } else {
                completion(error)
            }
        }
    }

    private func kickUser(userId: String, from tresorId: String, completion: @escaping (Error?) -> Void) {
        ZeroKitManager.shared.zeroKit.kick(userWithId: userId, fromTresor: tresorId) { operationId, error in
            if let operationId = operationId {
                ZeroKitManager.shared.backend.kickedUser(operationId: operationId) { error in
                    completion(error)
                }
            } else if error! == ZeroKitError.notMember {
                completion(nil)
            } else {
                completion(error)
            }
        }
    }

    func showError(title: String, message: String?) {
        let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "OK", style: .cancel, handler: nil))
        present(alert, animated: true, completion: nil)
    }

    func present(message: String) {
        func present() {
            self.alert = UIAlertController(title: message, message: nil, preferredStyle: .alert)
            self.present(self.alert!, animated: true, completion: nil)
        }
        if let alert = alert {
            alert.dismiss(animated: false, completion: present)
        } else {
            present()
        }
    }

    func dismissMessage(completion: ((Void) -> Void)? = nil) {
        alert?.dismiss(animated: true, completion: completion)
        alert = nil
    }

    // MARK: - Table view data source

    override func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        return true
    }

    override func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == .delete {
            let user = items![indexPath.row]
            self.revokeAccess(from: user.userName!)
        }
        tableView.reloadData()
    }
}
