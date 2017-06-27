import ZeroKit

enum ProfileField: String {
    case alias
}

enum PublicProfileField: String {
    case realmUserId
}

class ZeroKitManager: NSObject {
    static let shared = ZeroKitManager()

    let zeroKit: ZeroKit
    let backend: Backend
    let clientId: String

    override init() {
        let configFile = Bundle.main.url(forResource: "Config", withExtension: "plist")!
        let configDict = NSDictionary(contentsOf: configFile)!

        let serviceUrl = URL(string: configDict["ZeroKitServiceURL"] as! String)!
        let config = ZeroKitConfig(apiBaseUrl: serviceUrl)
        let zeroKit = try! ZeroKit(config: config)
        self.zeroKit = zeroKit

        let clientId = configDict["ZeroKitClientId"] as! String
        let backendUrl = URL(string: configDict["ZeroKitAppBackend"] as! String)!
        self.backend = Backend(withBackendBaseUrl: backendUrl, authorizationCallback: { credentialsCallback in
            zeroKit.getIdentityTokens(clientId: clientId) { tokens, error in
                credentialsCallback(tokens?.authorizationCode, clientId, error)
            }
        })

        self.clientId = clientId
    }

    private let zeroKitUserIdKey = "zeroKitUserId"
    private let realmUserIdKey = "realmUserId"

    func set(zeroKitUserId: String, realmUserId: String) {
        UserDefaults.standard.set(zeroKitUserId, forKey: zeroKitUserIdKey)
        UserDefaults.standard.set(realmUserId, forKey: realmUserIdKey)
    }

    func zeroKitAndRealmUserId() -> (zeroKitUserId: String?, realmUserId: String?) {
        return (
            UserDefaults.standard.string(forKey: zeroKitUserIdKey),
            UserDefaults.standard.string(forKey: realmUserIdKey)
        )
    }

    func removeZeroKitUserIdAndRealmId() {
        UserDefaults.standard.removeObject(forKey: zeroKitUserIdKey)
        UserDefaults.standard.removeObject(forKey: realmUserIdKey)
    }
}
