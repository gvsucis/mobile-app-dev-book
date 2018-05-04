//
//  JournalEditorViewController.swift
//  TraxyApp
//
//  Created by Jonathan Engelsma on 10/17/17.
//  Copyright © 2017 Jonathan Engelsma. All rights reserved.
//

import UIKit
import Eureka

protocol JournalEditorDelegate : class {
    func save(journal: Journal)
}

class JournalEditorViewController: UIViewController {
    
    var journalForm : AddJournalViewController!
    var coverSelect : CoverPhotoCollectionViewController!
    
    weak var delegate : JournalEditorDelegate?
    var journal : Journal?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        if self.journal == nil {
            self.navigationItem.title = "Add Journal"
            self.journal = Journal()
        } else {
            self.navigationItem.title = "Edit Journal"
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    
    @IBAction func cancelPressed(_ sender: UIBarButtonItem)
    {
        _ = self.navigationController?.popViewController(animated: true)
    }
    
    @IBAction func savePressed(_ sender: UIBarButtonItem)
    {
        let form = self.journalForm.form
        let errors = form.validate()
        if errors.count > 0 {
            print("fix ur errors!")
        } else {
            let titleRow: TextRow! = form.rowBy(tag: "TitleTag")
            let locRow: LabelRow! = form.rowBy(tag: "LocTag")
            let startDateRow : DateRow! = form.rowBy(tag: "StartDateTag")
            let endDateRow : DateRow! = form.rowBy(tag: "EndDateTag")
            
            let title = titleRow.value! as String
            let location = locRow.value! as String
            let startDate = startDateRow.value! as Date
            let endDate = endDateRow.value! as Date
            
            self.journal?.name = title
            self.journal?.location = location
            self.journal?.startDate = startDate
            self.journal?.endDate = endDate
            self.journal?.coverPhotoUrl = self.coverSelect.journal?.coverPhotoUrl
            self.journal?.lat = self.journalForm.journal?.lat
            self.journal?.lng = self.journalForm.journal?.lng
            self.journal?.placeId = self.journalForm.journal?.placeId
            
            self.delegate?.save(journal: self.journal!)
            _ = self.navigationController?.popViewController(animated: true)
        }
    }
    
    // MARK: - Navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "formSegue" {
            self.journalForm = segue.destination as! AddJournalViewController
            self.journalForm.journal = self.journal
        } else if segue.identifier == "coverSegue" {
            self.coverSelect = segue.destination as! CoverPhotoCollectionViewController
            self.coverSelect.journal = journal
        }
    }
}
