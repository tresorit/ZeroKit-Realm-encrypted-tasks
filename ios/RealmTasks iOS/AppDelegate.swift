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
import RealmLoginKit

@UIApplicationMain
final class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey : Any]? = nil) -> Bool {
        window = UIWindow(frame: UIScreen.main.bounds)

        setAuthenticationFailureCallback {
            resetDefaultRealm()
            self.window?.rootViewController = UIViewController()
            self.logIn()
        }

        if canConfigureDefaultRealm() {
            window?.rootViewController = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "LoggingInViewController")
            window?.makeKeyAndVisible()
            configureDefaultRealm { success in
                if success {
                    self.window?.rootViewController = ContainerViewController()
                } else {
                    logout { _ in
                        self.logIn(animated: true)
                    }
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
        loginController.serverURL = Constants.syncAuthURL.absoluteString
        loginController.authenticationProvider = ZeroKitAuth(zeroKit: ZeroKitManager.shared.zeroKit)
        loginController.loginSuccessfulHandler = { user in
            ZeroKitManager.shared.zeroKit.whoAmI { userId, error in
                if let userId = userId {
                    updateLoggedInUser(zeroKitUserId: userId, realmUser: user)
                    setDefaultRealmConfiguration(with: user) { error in
                        if let error = error {
                            self.window?.rootViewController?.dismiss(animated: false) {
                                self.present(error: error as NSError)
                            }
                        } else {
                            self.window?.rootViewController = ContainerViewController()
                            self.window?.rootViewController?.dismiss(animated: true, completion: nil)
                        }
                    }
                } else {
                    if let error = error {
                        self.window?.rootViewController?.dismiss(animated: false) {
                            self.present(error: error)
                        }
                    }
                    logout { _ in
                        self.window?.rootViewController = UIViewController()
                        self.logIn(animated: false)
                    }
                }
            }
        }

        window?.rootViewController?.present(loginController, animated: false, completion: nil)
    }

    func present(error: NSError) {
        let alertController = UIAlertController(title: error.localizedDescription,
                                                message: error.localizedFailureReason ?? error.description,
                                                preferredStyle: .alert)
        alertController.addAction(UIAlertAction(title: "Try Again", style: .default) { _ in
            logout { _ in
                self.logIn()
            }
        })
        window?.rootViewController?.present(alertController, animated: true, completion: nil)
    }
}
