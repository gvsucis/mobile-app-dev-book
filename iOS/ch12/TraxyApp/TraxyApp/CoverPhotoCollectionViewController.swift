//
//  CoverPhotoCollectionViewController.swift
//  TraxyApp
//
//  Created by Jonathan Engelsma on 10/17/17.
//  Copyright © 2017 Jonathan Engelsma. All rights reserved.
//

import UIKit

class CoverPhotoCollectionViewController: UIViewController {
    fileprivate let insets = UIEdgeInsets(top: 18.0, left: 18.0, bottom: 18.0,
                                          right: 18.0)
    fileprivate let ROW_SIZE : CGFloat  = 4.0
    fileprivate let cellId = "PhotoCell"
    @IBOutlet weak var collectionView: UICollectionView!
    
    var entries : [Dictionary<String,AnyObject>]?
    var journal : Journal? {
        didSet {
            self.entries = []
            if let e = journal?.entries {
                for (_,val) in e.enumerated() {
                    let entry = val.1 as! Dictionary<String,AnyObject>
                    let typeRaw = entry["type"] as! Int?
                    let type = EntryType(rawValue: typeRaw!)
                    if type == .photo {
                        self.entries?.append(entry)
                    }
                }
            }
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.collectionView.allowsMultipleSelection = false
        if self.journal == nil {
            self.journal = Journal()
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
}

extension CoverPhotoCollectionViewController : UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        sizeForItemAt indexPath: IndexPath) -> CGSize
    {
        
        let paddingSpace = self.insets.left * (ROW_SIZE + 1.0)
        let availableWidth = view.frame.width - paddingSpace
        let widthPerItem = availableWidth / ROW_SIZE
        
        return CGSize(width: widthPerItem, height: widthPerItem)
    }
    
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        insetForSectionAt section: Int) -> UIEdgeInsets
    {
        return self.insets
    }
    
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        minimumLineSpacingForSectionAt section: Int) -> CGFloat 
    {
        return self.insets.left
    }
}


extension CoverPhotoCollectionViewController : UICollectionViewDataSource {
    func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
    }
    
    func collectionView(_ collectionView: UICollectionView,
                        numberOfItemsInSection section: Int) -> Int
    {
        if let entries = self.entries {
            return entries.count
        }
        return 0
    }
    
    func collectionView(_ collectionView: UICollectionView,
                        cellForItemAt indexPath: IndexPath) -> UICollectionViewCell
    {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: cellId,
                                                      for: indexPath) as! CoverPhotoCollectionViewCell
        cell.photo.image = UIImage(named: "landscape")
        if let entry = self.entries?[indexPath.row] {
            if let url = entry["url"] as? String {
                cell.photo?.kf.indicatorType = .activity
                cell.photo?.kf.setImage(with: URL(string: url))
                if self.journal?.coverPhotoUrl == url {
                    cell.isSelected = true
                    self.collectionView.selectItem(at: indexPath, animated: true, 
                                                   scrollPosition: .top)
                } 
            }
        }
        return cell
    }
}

extension CoverPhotoCollectionViewController : UICollectionViewDelegate {
    func collectionView(_ collectionView: UICollectionView,
                        didSelectItemAt indexPath: IndexPath)
    {
        if let entry = self.entries?[indexPath.row] {
            self.journal?.coverPhotoUrl = entry["url"] as? String
        }
    }
}

