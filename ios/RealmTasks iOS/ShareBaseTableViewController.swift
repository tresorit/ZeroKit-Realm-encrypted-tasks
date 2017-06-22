import UIKit
import Realm
import RealmSwift

class ShareBaseTableViewController: UITableViewController {
    let cellId = "cellId"
    var realm: Realm!
    var tokens = [NotificationToken]()
    var items: Results<User>?
    let colors = Color.shareColors()

    deinit {
        for token in tokens {
            token.stop()
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        tableView.register(UITableViewCell.self, forCellReuseIdentifier: cellId)
        tableView.separatorStyle = .none
        tableView.backgroundColor = .black
        tableView.rowHeight = 54
    }

    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return items?.count ?? 0
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: cellId, for: indexPath)
        cell.textLabel?.text = items?[indexPath.row].userName
        cell.textLabel?.textColor = .white
        cell.textLabel?.font = .systemFont(ofSize: 18)
        cell.textLabel?.backgroundColor = .clear
        cell.selectionStyle = .none
        return cell
    }

    override func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        cell.contentView.backgroundColor = .color(forRow: indexPath.row, count: items!.count, colors: colors)
    }
}
