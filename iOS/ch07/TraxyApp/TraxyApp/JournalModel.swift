//
//  JournalModel.swift
//  TraxyApp
//
//  Created by Jonathan Engelsma on 3/14/17.
//  Copyright © 2017 Jonathan Engelsma. All rights reserved.
//

import Foundation
class JournalModel {
    fileprivate var items : [Journal] = [Journal]()
    init() {
        createJournals()
    }
    
    func getJournals() -> [Journal]
    {
        return self.items
    }
    
    fileprivate func createJournals()
    {
        items.append(Journal(name: "Out West", location: "Estes Park, Co",
                             startDate: Date.distantPast, endDate: Date.init(timeInterval: 0,
                                                                               since: Date.distantPast), lat: 40.3772059, lng: -105.5216651,
                                                                                                           placeId: "ChIJAxoZu9ZlaYcRDKzKqbeYlts"))
        items.append(Journal(name: "Out West", location: "Estes Park, Co",
                             startDate: Date.distantFuture, endDate: Date.init(timeInterval: 100000,
                                                                               since: Date.distantFuture), lat: 40.3772059, lng: -105.5216651,
                                                                                                           placeId: "ChIJAxoZu9ZlaYcRDKzKqbeYlts"))
        items.append(Journal(name: "Down South", location: "Charlotte, NC",
                             startDate: Date.distantPast, endDate: Date.distantFuture, lat: 40.3772059, lng: -105.5216651,
                                                                                                           placeId: "ChIJAxoZu9ZlaYcRDKzKqbeYlts"))
        items.append(Journal(name: "Out East", location: "New York, NY", 
                             startDate: Date.distantFuture, endDate: Date.init(timeInterval: 100000, 
                                                                               since: Date.distantFuture), lat: 40.3772059, lng: -105.5216651, 
                                                                                                           placeId: "ChIJAxoZu9ZlaYcRDKzKqbeYlts"))
    }
}
