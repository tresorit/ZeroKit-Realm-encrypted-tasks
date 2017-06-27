import RealmSwift

class User: Object {
    dynamic var realmUserId: String?
    dynamic var zerokitUserId: String?
    dynamic var userName: String?

    override static func primaryKey() -> String? {
        return "realmUserId"
    }
}
