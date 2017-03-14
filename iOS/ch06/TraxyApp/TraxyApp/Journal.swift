//
//  Journal.swift
//  TraxyApp
//
//  Created by Jonathan Engelsma on 3/14/17.
//  Copyright © 2017 Jonathan Engelsma. All rights reserved.
//

import Foundation
struct Journal {
    var key : String?
    var name : String?
    var location : String?
    var startDate : Date?
    var endDate : Date?
    var lat : Double?
    var lng : Double?
    var placeId : String?
    init(key: String?, name: String?, location: String?, startDate: Date?,
         endDate : Date?, lat: Double?, lng: Double?, placeId : String?)
    {
        self.key = key
        self.name = name
        self.location = location
        self.startDate = startDate
        self.endDate = endDate
        self.lat = lat
        self.lng = lng
        self.placeId = placeId
    }
    init(name: String?, location: String?, startDate: Date?, endDate : Date?,
         lat: Double?, lng: Double?, placeId : String?)
    {
        self.init(key: nil, name: name, location: location, startDate: startDate,
                  endDate: endDate, lat: lat, lng: lng, placeId: placeId)
    }
    
    
}
