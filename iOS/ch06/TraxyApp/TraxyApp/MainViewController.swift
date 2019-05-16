//
//  MainViewController.swift
//  TraxyApp
//
//  Created by Jonathan Engelsma on 2/7/17.
//  Copyright © 2017 Jonathan Engelsma. All rights reserved.
//

import UIKit

class MainViewController: UIViewController, UITableViewDataSource, UITableViewDelegate
 {
    
    var userEmail : String?
    
    @IBOutlet weak var tableView: UITableView!
    var journals : [Journal]?
    
    var tableViewData: [(sectionHeader: String, journals: [Journal])]? {
        didSet {
            DispatchQueue.main.async {
                self.tableView.reloadData()
            }
        }
    }
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let model = JournalModel()
        self.sortIntoSections(journals: model.getJournals())
        self.setNeedsStatusBarAppearanceUpdate()
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
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
            cell.coverImage?.image = UIImage(named: "landscape")
            
            
            return cell
            
            
    }

    
    
    // MARK: - UITableViewDelegate
    func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) ->
        String? {
            return self.tableViewData?[section].sectionHeader
    }

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
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}

extension Date {
    struct Formatter {
        static let short: DateFormatter = {
            let formatter = DateFormatter()
            formatter.dateFormat = "MM-dd-yyyy"
            return formatter
        }()
    }
    
    var short: String {
        return Formatter.short.string(from: self)
    }
}


extension String {
    
    
    var dateFromShort: Date? {
        return Date.Formatter.short.date(from: self)
    }
    
    
}

extension UINavigationController {
    override open var preferredStatusBarStyle : UIStatusBarStyle {
        return topViewController?.preferredStatusBarStyle ?? .default
    }
}

