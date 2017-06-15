import ZeroKit
import Realm
import RealmSwift
import RealmLoginKit

enum ZeroKitAuthError: Error {
    case missingUsernameOrPassword
}

class ZeroKitAuth: NSObject, AuthenticationProvider {
    private let zeroKit: ZeroKit
    private var completion: ((RLMSyncCredentials?, Error?) -> Void)?

    init(zeroKit: ZeroKit) {
        self.zeroKit = zeroKit
    }

    private func register() {
        guard let username = self.username, let password = self.password else {
            complete(error: ZeroKitAuthError.missingUsernameOrPassword)
            return
        }

        let backend = ZeroKitManager.shared.backend
        let zeroKit = ZeroKitManager.shared.zeroKit

        backend.initRegistration(username: testUsername(from: username), profileData: "{ \"autoValidate\": true }") { userId, regSessionId, error in

            if let error = error {
                self.complete(error: error)
                return
            }

            zeroKit.register(withUserId: userId!, registrationId: regSessionId!, password: password) { regValidationVerifier, error in

                if let error = error {
                    self.complete(error: error)
                    return
                }

                backend.finishRegistration(userId: userId!, validationVerifier: regValidationVerifier!) { error in

                    if let error = error {
                        self.complete(error: error)
                        return
                    }

                    self.logIn()
                }
            }
        }
    }

    private func logIn() {
        guard let username = self.username, let password = self.password else {
            complete(error: ZeroKitAuthError.missingUsernameOrPassword)
            return
        }

        let backend = ZeroKitManager.shared.backend
        let zeroKit = ZeroKitManager.shared.zeroKit

        backend.getUserId(forUsername: testUsername(from: username)) { userId, error in

            if let error = error {
                self.complete(error: error)
                return
            }

            zeroKit.login(withUserId: userId!, password: password, rememberMe: true) { error in

                if let error = error {
                    self.complete(error: error)
                    return
                }

                zeroKit.getIdentityTokens(clientId: ZeroKitManager.shared.clientId) { tokens, error in

                    if let error = error {
                        self.complete(error: error)
                        return
                    }

                    if let completion = self.completion {
                        let provider = RLMIdentityProvider(rawValue: "custom/zerokit")
                        let creds = RLMSyncCredentials(customToken: tokens!.authorizationCode, provider: provider, userInfo: nil)
                        completion(creds, nil)
                    }
                }
            }
        }
    }

    func testUsername(from username: String) -> String {
        // This modification is for the sample zerokit backend to automatically validate users.
        if !username.hasPrefix("test-user-") {
            return "test-user-\(username)"
        }
        return username
    }

    func complete(error: Error) {
        if let completion = completion {
            completion(nil, error)
        }
    }

    // MARK: AuthenticationProvider

    public var username: String?
    public var password: String?
    public var isRegistering = false

    public func authenticate(onCompletion: ((RLMSyncCredentials?, Error?) -> Void)?) {
        self.completion = onCompletion
        if isRegistering {
            register()
        } else {
            logIn()
        }
    }

    public func cancelAuthentication() {
    }
}
