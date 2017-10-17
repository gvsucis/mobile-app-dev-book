//
//  CustomerJournalSectionHeaderTableViewCell.swift
//  TraxyApp
//
//  Created by Jonathan Engelsma on 10/12/17.
//  Copyright © 2017 Jonathan Engelsma. All rights reserved.
//

import UIKit

class CustomJournalSectionHeaderTableViewCell: UITableViewCell {
    
    @IBOutlet weak var weatherIcon: UIImageView!
    @IBOutlet weak var temperatureLabel: UILabel!
    @IBOutlet weak var headerText: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        self.weatherIcon.layer.cornerRadius = 5.0
        self.weatherIcon.clipsToBounds = true
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
    }
    
}
