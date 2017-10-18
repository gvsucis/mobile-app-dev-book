//
//  CalendarViewController.swift
//  TraxyApp
//
//  Created by Jonathan Engelsma on 10/17/17.
//  Copyright © 2017 Jonathan Engelsma. All rights reserved.
//

import UIKit
import FSCalendar

class CalendarViewController: TraxyTopLevelViewController {
    
    var journalView : MainViewController?

    @IBOutlet weak var calendar: FSCalendar!
    override func viewDidLoad() {
        super.viewDidLoad()
        self.calendar.backgroundColor = THEME_COLOR3
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func journalsDidLoad() {
        if let _ = self.journals, let jv = self.journalView{
            let fj = filterJournalsByDisplayDate()
            jv.sortIntoSections(journals: fj)
        } else if let jv = self.journalView {
            jv.tableViewData?.removeAll()
        }
    }
    
    func filterJournalsByDisplayDate() -> [Journal]
    {
        var filteredJournals : [Journal] = []
        if let journals = self.journals {
            let date = self.calendar.currentPage
            for j in journals {
                if date.monthYear >= j.startDate!.monthYear
                    && date.monthYear <= j.endDate!.monthYear
                {
                    filteredJournals.append(j)
                }
            }
        }
        return filteredJournals
    }
    
    // MARK: - Navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "embeddedJournalViewSegue" {
            self.journalView = segue.destination as? MainViewController
            self.journalView?.shouldLoad = false
        } else  if segue.identifier == "addJournalSegue" {
            if let destVC = segue.destination as? JournalEditorViewController {
                destVC.delegate = self.journalView
            }
        }
    }
}

extension CalendarViewController : FSCalendarDelegate {
    
    func calendar(_ calendar: FSCalendar, boundingRectWillChange bounds: CGRect,
                  animated: Bool) {
    }
    
    func calendar(_ calendar: FSCalendar, didSelect date: Date) {
    }
    
    func calendarCurrentPageDidChange(_ calendar: FSCalendar) {
        self.journalsDidLoad()
    }
    
}

extension CalendarViewController : FSCalendarDataSource {
    
    func numberOfJournalsOnDate(date: Date) -> Int {
        var cnt = 0
        if let items = self.journals {
            for j in items {
                guard let startDate = j.startDate, let endDate = j.endDate else {
                    continue
                }
                if date >= startDate && date <= endDate {
                    cnt+=1
                }
            }
        }
        return cnt
    }
    
    func calendar(_ calendar: FSCalendar, numberOfEventsFor date: Date) -> Int {
        return self.numberOfJournalsOnDate(date: date)
    }
    
    
    @objc(calendar:shouldSelectDate:) func calendar(_ calendar: FSCalendar, shouldSelect 
        date: Date) -> Bool {
        return false
    }
}
