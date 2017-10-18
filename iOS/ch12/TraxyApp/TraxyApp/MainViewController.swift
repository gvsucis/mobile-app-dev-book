//
//  MainViewController.swift
//  TraxyApp
//
//  Created by Jonathan Engelsma on 2/7/17.
//  Copyright © 2017 Jonathan Engelsma. All rights reserved.
//

import UIKit
import FirebaseAuth
import FirebaseDatabase

class MainViewController: TraxyTopLevelViewController, UITableViewDataSource, UITableViewDelegate, JournalEditorDelegate
 {

    
    @IBOutlet weak var tableView: UITableView!
    var journalToEdit : Journal?
    
    var tableViewData: [(sectionHeader: String, journals: [Journal])]? {
        didSet {
            DispatchQueue.main.async {
                self.tableView.reloadData()
            }
        }
    }
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
//        let model = JournalModel()
//        self.journals = model.getJournals()
//        self.sortIntoSections(journals: self.journals!)
//        
//        FIRAuth.auth()?.addStateDidChangeListener { auth, user in
//            if let user = user {
//                self.userId = user.uid
//                self.ref = FIRDatabase.database().reference()
//                self.registerForFireBaseUpdates()
//            }
//        }
    }
    
    override func journalsDidLoad() {
        if let j = self.journals {
            self.sortIntoSections(journals: j)
        } else {
            self.tableViewData?.removeAll()
        }
    }
    
    func sortIntoSections(journals: [Journal]) {
        
        
        // We assume the model already provides them ascending date order.
        var currentSection  = [Journal]()
        var futureSection = [Journal]()
        var pastSection = [Journal]()
        
        let today = (Date().short.dateFromShort)!
        for j in journals {
            let endDate = (j.endDate?.short.dateFromShort)!
            let startDate = (j.startDate?.short.dateFromShort)!
            if today <=  endDate && today >= startDate {
                currentSection.append(j)
            } else if today < startDate {
                futureSection.append(j)
            } else {
                pastSection.append(j)
            }
        }
        
        var tmpData: [(sectionHeader: String, journals: [Journal])] = []
        if currentSection.count > 0 {
            tmpData.append((sectionHeader: "CURRENT", journals: currentSection))
        }
        if futureSection.count > 0 {
            tmpData.append((sectionHeader: "FUTURE", journals: futureSection))
        }
        if pastSection.count > 0 {
            tmpData.append((sectionHeader: "PAST", journals: pastSection))
        }
        
        self.tableViewData = tmpData
        
    }

    // MARK: - UITableViewDataSource
    func numberOfSections(in tableView: UITableView) -> Int {
        return self.tableViewData?.count ?? 0
    }
    
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.tableViewData?[section].journals.count ?? 0
    }
    
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) ->
        UITableViewCell {
            let cell = self.tableView.dequeueReusableCell(withIdentifier: "FancyCell", for:
                indexPath) as! TraxyMainTableViewCell
            
            
            guard let journal = tableViewData?[indexPath.section].journals[indexPath.row] else {
                return cell
            }
            
            
            cell.name?.text = journal.name
            cell.subName?.text = journal.location
            if let coverUrl = journal.coverPhotoUrl {
                let url = URL(string: coverUrl)
                cell.coverImage?.kf.indicatorType = .activity
                cell.coverImage?.kf.setImage(with: url)
            } else {
                cell.coverImage?.image = UIImage(named: "landscape")
            }

            
            
            return cell
            
            
    }

    
    
    // MARK: - UITableViewDelegate
    func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) ->
        String? {
            return self.tableViewData?[section].sectionHeader
    }

    // MARK: - UITableViewDelegate
    
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) ->
        CGFloat {
            return 200.0
    }

    func tableView(_ tableView: UITableView, willDisplayFooterView view: UIView, forSection
        section: Int) {
        let header = view as! UITableViewHeaderFooterView
        header.textLabel?.textColor = THEME_COLOR2
        header.contentView.backgroundColor = THEME_COLOR3
    }
    
    
    func tableView(_ tableView: UITableView, willDisplayHeaderView view: UIView,
                   forSection section: Int) {
        
        
        let header = view as! UITableViewHeaderFooterView
        header.textLabel?.textColor = THEME_COLOR2
        header.contentView.backgroundColor = THEME_COLOR3
        
        
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        guard let journal = tableViewData?[indexPath.section].journals[indexPath.row] else {
            return
        }
        print("Selected\(String(describing: journal.name))")
    }


    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    // MARK: - Navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "addJournalSegue" {
            if let destVC = segue.destination as? JournalEditorViewController {
                destVC.delegate = self
            }
        } else if segue.identifier == "showJournalSegue" {
            if let destVC = segue.destination as? JournalTableViewController {
                let indexPath = self.tableView.indexPathForSelectedRow
                let values = self.tableViewData?[indexPath!.section]
                destVC.journal  = values?.journals[indexPath!.row]
                destVC.userId = self.userId
                destVC.journalEditorDelegate = self
            }
        }
    }
    
    // MARK: - JournalEditorDelegate
    func save(journal: Journal) {
        if let k = journal.key {
            let child = self.ref?.child(self.userId!).child(k)
            child?.updateChildValues(self.toDictionary(vals: journal))
        } else {
            let newChild = self.ref?.child(self.userId!).childByAutoId()
            newChild?.setValue(self.toDictionary(vals: journal))
        }
    }
    
    func toDictionary(vals: Journal) -> [String : Any] {
        
        return [
            "name": vals.name! as NSString,
            "address": vals.location! as NSString,
            "startDate" : NSString(string: (vals.startDate?.iso8601)!) ,
            "endDate": NSString(string: (vals.endDate?.iso8601)!),
            "lat" : NSNumber(value: vals.lat!),
            "lng" : NSNumber(value: vals.lng!),
            "placeId" : vals.placeId! as NSString,
            "coverPhotoUrl" : vals.coverPhotoUrl! as NSString
        ]
        
    }
    
    /*
    fileprivate func registerForFireBaseUpdates()
    {
        self.ref!.child(self.userId!).observe(.value, with: { snapshot in
            if let postDict = snapshot.value as? [String : AnyObject] {
                var tmpItems = [Journal]()
                for (_,val) in postDict.enumerated() {
                    let entry = val.1 as! Dictionary<String,AnyObject>
                    let key = val.0
                    let name : String? = entry["name"] as! String?
                    let location : String?  = entry["address"] as! String?
                    let startDateStr  = entry["startDate"] as! String?
                    let endDateStr = entry["endDate"] as! String?
                    let lat = entry["lat"] as! Double?
                    let lng = entry["lng"] as! Double?
                    let placeId = entry["placeId"] as! String?
                    tmpItems.append(Journal(key: key, name: name, location: location, startDate:
                        startDateStr?.dateFromISO8601, endDate: endDateStr?.dateFromISO8601,
                                                       lat: lat, lng: lng, placeId: placeId))
                }
                self.journals = tmpItems
                self.sortIntoSections(journals: self.journals!)
            }
        })
        
    }
*/

}


