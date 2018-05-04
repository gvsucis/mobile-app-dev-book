//
//  Date+ISO8601.swift
//  TripApp
//
//  Created by Jonathan Engelsma on 10/12/16.
//  Copyright © 2016 Jonathan Engelsma. All rights reserved.
//

import Foundation

extension Date {
    struct Formatter {
        static let iso8601: DateFormatter = {
            let formatter = DateFormatter()
            formatter.calendar = Calendar(identifier: .iso8601)
            formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSxxx"
            return formatter
        }()
        
        static let monthYear: DateFormatter = {
            let formatter = DateFormatter()
            formatter.dateFormat = "yyyy-MM"
            return formatter
        }()
        
        static let short: DateFormatter = {
            let formatter = DateFormatter()
            formatter.dateFormat = "MM-dd-yyyy"
            return formatter
        }()
        
        static let shortWithTime: DateFormatter = {
            let formatter = DateFormatter()
            formatter.dateFormat = "MM-dd-yyyy, H:mm"
            return formatter
        }()
        
    }
    
    var short: String {
        return Formatter.short.string(from: self)
    }
    
    var shortWithTime: String {
        return Formatter.shortWithTime.string(from: self)
    }
    
    var monthYear: String {
        return Formatter.monthYear.string(from: self)
    }
    
    var iso8601: String {
        return Formatter.iso8601.string(from: self)
    }
    
    func days(from date: Date) -> Int {
        return Calendar.current.dateComponents([.day], from: date, to: self).day!
    }
}


extension String {
    var dateFromISO8601: Date? {
        return Date.Formatter.iso8601.date(from: self)
    }
    
    var dateFromShort: Date? {
        return Date.Formatter.short.date(from: self)
    }
    
}

