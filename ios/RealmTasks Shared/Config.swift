import Foundation

struct Config {
    static let shared = Config()

    let syncServerURL: URL
    let syncServerURLFormat: String
    let syncSharesServerURL: URL
    let syncInvitesPrivateServerURL: URL
    let syncInvitesPublicURL: URL
    let syncInvitesPublicURLFormat: String
    let syncAuthURL: URL

    init() {
        let configUrl = Bundle.main.url(forResource: "Config", withExtension: "plist")!
        let config = NSDictionary(contentsOf: configUrl)!

        let syncHost = config["RealmHost"] as! String
        let port = (config["RealmPort"] as! NSNumber).intValue

        let syncRealmPath = "realmtasks"
        let syncSharesRealmPath = "shares_private"
        let syncInvitesPrivateRealmPath = "invites_private"
        let syncInvitesPublicRealmPath = "invites_public"

        syncServerURL = URL(string: "realm://\(syncHost):\(port)/~/\(syncRealmPath)")!
        syncServerURLFormat = "realm://\(syncHost):\(port)/%@/\(syncRealmPath)"
        syncSharesServerURL = URL(string: "realm://\(syncHost):\(port)/~/\(syncSharesRealmPath)")!
        syncInvitesPrivateServerURL = URL(string: "realm://\(syncHost):\(port)/~/\(syncInvitesPrivateRealmPath)")!
        syncInvitesPublicURL = URL(string: "realm://\(syncHost):\(port)/~/\(syncInvitesPublicRealmPath)")!
        syncInvitesPublicURLFormat = "realm://\(syncHost):\(port)/%@/\(syncInvitesPublicRealmPath)"

        syncAuthURL = URL(string: "http://\(syncHost):\(port)")!
    }
}
