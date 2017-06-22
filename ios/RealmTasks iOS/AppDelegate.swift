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

import UIKit
import RealmSwift
import RealmLoginKit

@UIApplicationMain
final class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey : Any]? = nil) -> Bool {
        window = UIWindow(frame: UIScreen.main.bounds)
        window!.tintColor = Color.listColors()[1]

        setAuthenticationFailureCallback {
            self.logOut()
        }

        if canConfigureDefaultRealm() {
            window?.rootViewController = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "LoggingInViewController")
            window?.makeKeyAndVisible()
            configureDefaultRealm { success in
                if let user = SyncUser.current, success {
                    self.afterLogin(with: user)
                } else {
                    self.logOut()
                }
            }
        } else {
            window?.rootViewController = UIViewController()
            window?.makeKeyAndVisible()
            logIn(animated: false)
        }
        return true
    }

    func logIn(animated: Bool = true) {
        let loginController = LoginViewController(style: .darkTranslucent)
        loginController.isServerURLFieldHidden = true
        loginController.isRememberAccountDetailsFieldHidden = true
        loginController.serverURL = Config.shared.syncAuthURL.absoluteString
        loginController.authenticationProvider = ZeroKitAuth(zeroKit: ZeroKitManager.shared.zeroKit)
        loginController.loginSuccessfulHandler = { user in
            self.afterLogin(with: user)
        }

        window?.rootViewController?.present(loginController, animated: false, completion: nil)
    }

    private func afterLogin(with user: SyncUser) {
        ZeroKitManager.shared.zeroKit.whoAmI { userId, error in
            if let userId = userId {
                self.setUpProfile(userId: userId, realmUserId: user.identity!) { error in
                    if error == nil {
                        updateLoggedInUser(zeroKitUserId: userId, realmUser: user)
                        setDefaultRealmConfiguration(with: user) { error in
                            if let error = error {
                                self.window?.rootViewController?.dismiss(animated: false) {
                                    self.present(error: error as NSError)
                                }
                            } else {
                                self.window?.rootViewController = self.loggedInViewController()
                                self.window?.rootViewController?.dismiss(animated: true, completion: nil)
                            }
                        }
                    } else {
                        self.logOut()
                    }
                }
            } else {
                self.logOut()
            }
        }
    }

    private func setUpProfile(userId: String, realmUserId: String, completion: @escaping (Error?) -> Void) {
        ZeroKitManager.shared.backend.getPublicProfile(for: userId) { profileJson, error in
            guard error == nil else {
                completion(error)
                return
            }

            var profile = [String: Any]()

            if let profileJson = profileJson,
                let data = profileJson.data(using: .utf8),
                let jsonObj = (try? JSONSerialization.jsonObject(with: data, options: [])) as? [String : Any] {
                profile = jsonObj
            }

            if let storedId = profile[PublicProfileField.realmUserId.rawValue] as? String, storedId == realmUserId {
                completion(nil)
            } else {
                profile[PublicProfileField.realmUserId.rawValue] = realmUserId
                let data = try! JSONSerialization.data(withJSONObject: profile, options: [])
                ZeroKitManager.shared.backend.storePublicProfile(data: String(data: data, encoding: .utf8)!) { error in
                    completion(error)
                }
            }
        }
    }

    func logOut() {
        self.window?.rootViewController = UIViewController()
        let alert = UIAlertController(title: "Logging out...", message: nil, preferredStyle: .alert)
        self.window?.rootViewController?.present(alert, animated: true, completion: nil)
        logoutServices { _ in
            alert.dismiss(animated: true) {
                self.logIn()
            }
        }
    }

    private func loggedInViewController() -> UIViewController {
        return UIStoryboard(name: "Main", bundle: nil).instantiateInitialViewController()!
    }

    func present(error: NSError) {
        let alertController = UIAlertController(title: error.localizedDescription,
                                                message: error.localizedFailureReason ?? error.description,
                                                preferredStyle: .alert)
        alertController.addAction(UIAlertAction(title: "Try Again", style: .default) { _ in
            self.logOut()
        })
        window?.rootViewController?.present(alertController, animated: true, completion: nil)
    }
}
