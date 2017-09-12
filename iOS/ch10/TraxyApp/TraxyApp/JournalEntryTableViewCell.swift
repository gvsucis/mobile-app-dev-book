//
//  JournalEntryTableViewCell.swift
//  TraxyApp
//
//  Created by Jonathan Engelsma on 5/4/17.
//  Copyright © 2017 Jonathan Engelsma. All rights reserved.
//

import UIKit
class JournalEntryTableViewCell: UITableViewCell {
    
    @IBOutlet weak var containingView: UIView!
    @IBOutlet weak var playButton: UIButton!
    @IBOutlet weak var date: UILabel!
    @IBOutlet weak var textData : UILabel!
    @IBOutlet weak var imageButton: UIButton!
    @IBOutlet weak var thumbnailImage: UIImageView!
    @IBOutlet weak var editButton: UIButton!
    var entry : JournalEntry?
    
    override func awakeFromNib() {
        super.awakeFromNib()
        self.containingView.layer.cornerRadius = 10
        if let button = self.imageButton {
            button.imageView?.contentMode = .scaleAspectFill
        }
        
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
    func setValues(entry : JournalEntry) {
        self.entry = entry
        self.textData.text = entry.caption
        self.date.text = entry.date?.shortWithTime
    }
    
}

