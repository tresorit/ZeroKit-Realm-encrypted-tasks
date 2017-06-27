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

import Foundation
import RealmSwift

protocol EncryptionItem {
    var tresorId: String? { get set }
}

protocol ListPresentable: EncryptionItem {
    associatedtype Item: Object, CellPresentable
    var items: List<Item> { get }
}

protocol CellPresentable: class, EncryptionItem {
    var text: String { get set }
    var date: Date? { get set }
    var completed: Bool { get set }
    var isCompletable: Bool { get }

    func plainText(completion: @escaping (String?, Error?) -> Void)
    func setPlainText(_ value: String, completion: @escaping (Error?) -> Void)
}

extension CellPresentable where Self: Object {
    func plainText(completion: @escaping (String?, Error?) -> Void) {
        ZeroKitManager.shared.zeroKit.decrypt(cipherText: self.text) { plainText, error in
            completion(plainText, error)
        }
    }

    func setPlainText(_ value: String, completion: @escaping (Error?) -> Void) {
        let len = value.characters.count
        if let tresorId = self.tresorId, len > 0 {
            self.text = "Encrypting..." // Set tmp text here, so item is not intstantly removed from the list because content is empty
            ZeroKitManager.shared.zeroKit.encrypt(plainText: value, inTresor: tresorId) { [weak self] cipherText, error in
                if let cipherText = cipherText, let sself = self {
                    try! sself.realm?.write {
                        sself.text = cipherText
                    }
                    completion(nil)
                } else {
                    completion(error)
                }
            }
        } else if len == 0 {
            self.text = ""
        } else {
            self.text = "Error: missing tresor."
            completion(NSError(domain: "EncryptedCellPresentable", code: 1, userInfo: [NSLocalizedDescriptionKey: "No tresor id for item."]))
        }
    }
}

final class TaskListList: Object, ListPresentable {
    dynamic var id: String? // swiftlint:disable:this variable_name
    let items = List<TaskList>()

    var tresorId: String? {
        get { return id }
        set { id = newValue }
    }

    override static func primaryKey() -> String? {
        return "id"
    }

    override static func ignoredProperties() -> [String] {
        return ["tresorId"]
    }
}

final class TaskList: Object, CellPresentable, ListPresentable {
    dynamic var id = NSUUID().uuidString // swiftlint:disable:this variable_name
    dynamic var tresorId: String?
    dynamic var text = ""
    var date: Date?
    dynamic var completed = false
    let items = List<Task>()

    var isCompletable: Bool {
        return !items.filter("completed == false").isEmpty
    }

    override static func primaryKey() -> String? {
        return "id"
    }

    override static func ignoredProperties() -> [String] {
        return ["date"]
    }
}

final class Task: Object, CellPresentable {
    dynamic var text = ""
    dynamic var date: Date?
    dynamic var id: String? // swiftlint:disable:this variable_name
    dynamic var completed = false

    var isCompletable: Bool { return true }

    var tresorId: String? {
        get { return id }
        set { id = newValue }
    }

    convenience init(text: String) {
        self.init()
        self.text = text
    }

    override static func ignoredProperties() -> [String] {
        return ["tresorId"]
    }
}
