import UIKit
import RealmSwift
import ZeroKit

class AccountViewController: UIViewController {
    @IBOutlet weak var usernameLabel: UILabel!
    @IBOutlet weak var zeroKitIdLabel: UILabel!
    @IBOutlet weak var realmIdLabel: UILabel!

    override func awakeFromNib() {
        super.awakeFromNib()
        self.title = "Account"
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

        self.usernameLabel.text = nil
        self.zeroKitIdLabel.text = nil

        ZeroKitManager.shared.backend.getProfile { profile, _ in
            if let profile = profile,
                let profileData = profile.data(using: .utf8),
                let json = (try? JSONSerialization.jsonObject(with: profileData, options: [])) as? [String: Any] {

                self.usernameLabel.text = json[ProfileField.alias.rawValue] as? String
            } else {
                self.usernameLabel.text = nil
            }
        }

        ZeroKitManager.shared.zeroKit.whoAmI { (userId, _) in
            self.zeroKitIdLabel.text = userId
        }

        realmIdLabel.text = SyncUser.current?.identity
    }

    @IBAction func logoutButtonTapped(sender: UIButton) {
        let alert = UIAlertController(title: "Log out?", message: nil, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Log out", style: .default, handler: { _ in
            (UIApplication.shared.delegate as! AppDelegate).logOut()
        }))
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        self.present(alert, animated: true, completion: nil)
    }
}
