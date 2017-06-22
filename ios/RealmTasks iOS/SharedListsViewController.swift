import UIKit
import Realm
import RealmSwift

class SharedListsViewController: ShareBaseTableViewController {
    var invitesRealm: Realm!

    override func awakeFromNib() {
        super.awakeFromNib()
        self.title = "Shared with Me"
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        realm = invitesPrivateRealm()!
        tokens.append(realm.addNotificationBlock { [weak self] _, _ in
            self?.updateList()
        })
        updateList()

        invitesRealm = invitesPublicRealm()
        tokens.append(invitesRealm.addNotificationBlock { [weak self] _, _ in
            self?.processInvitations()
        })
        processInvitations()
    }

    private func updateList() {
        items = realm?.objects(User.self).sorted(byKeyPath: "userName")
        tableView.reloadData()
    }

    private func processInvitations() {
        try! invitesRealm.write {
            let invites = invitesRealm.objects(User.self)

            if invites.count > 0 {
                let new     = invites.filter(NSPredicate(format: "zerokitUserId != nil AND zerokitUserId != ''"))
                let removed = invites.filter(NSPredicate(format: "zerokitUserId == nil OR zerokitUserId == ''"))

                try! realm.write {
                    for user in new {
                        realm.create(User.self, value: user, update: true)
                    }
                    let removedRealmUserIds = Array(removed.map { $0.realmUserId! })
                    if removedRealmUserIds.count > 0 {
                        let result = realm.objects(User.self).filter("realmUserId IN %@", removedRealmUserIds)
                        realm.delete(result)
                    }
                }

                invitesRealm.delete(invites)
            }
        }
    }

    // MARK: - Table view data source

    override func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        return false
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let containerVC = ContainerViewController()
        let user = items![indexPath.row]
        containerVC.tasksRealm = tasksRealm(for: user.realmUserId!)
        containerVC.edgesForExtendedLayout = []
        self.navigationController?.pushViewController(containerVC, animated: true)
    }
}
