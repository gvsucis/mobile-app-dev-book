//
//  JournalTableViewController.swift
//  TraxyApp
//
//  Created by Jonathan Engelsma on 4/10/17.
//  Copyright © 2017 Jonathan Engelsma. All rights reserved.
//

import UIKit
import AVFoundation
import AVKit
import MobileCoreServices
import FirebaseDatabase
import Firebase
import FirebaseStorage
import Kingfisher

class JournalTableViewController: UITableViewController {

    var capturedImage : UIImage?
    var captureVideoUrl : URL?
    var captureType : EntryType = .photo
    
    var journal: Journal!
    var userId : String!
    var entries : [JournalEntry] = []
    var entryToEdit : JournalEntry?
    var tableViewData: [(sectionHeader: String, entries: [JournalEntry])]?
    
    fileprivate var ref : DatabaseReference?
    fileprivate var storageRef : StorageReference?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.tableView.rowHeight = UITableViewAutomaticDimension
        self.tableView.estimatedRowHeight = 200
        self.clearsSelectionOnViewWillAppear = true
        self.navigationItem.title = self.journal.name
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.ref = Database.database().reference().child(self.userId).child(self.journal.key!)
        self.registerForFireBaseUpdates()
        self.configureStorage()
    }
    
    func registerForFireBaseUpdates()
    {
        self.ref!.child("entries").observe(.value, with: { [weak self] snapshot in
            guard let strongSelf = self else { return }
            if let postDict = snapshot.value as? [String : AnyObject] {
                var tmpItems = [JournalEntry]()
                for (key,val) in postDict.enumerated() {
                    print("key = \(key) and val = \\(val)")
                    let entry = val.1 as! Dictionary<String,AnyObject>
                    print ("entry=\(entry)")
                    let key = val.0
                    let caption : String? = entry["caption"] as! String?
                    var url : String? = entry["url"] as? String
                    if url == nil {
                        url = ""
                    }
                    var thumbnailUrl : String? = entry["thumbnailUrl"] as? String
                    if thumbnailUrl == nil {
                        thumbnailUrl = ""
                    }
                    let dateStr  = entry["date"] as! String?
                    let lat = entry["lat"] as! Double?
                    let lng = entry["lng"] as! Double?
                    let typeRaw = entry["type"] as! Int?
                    let type = EntryType(rawValue: typeRaw!)
                    
                    tmpItems.append(JournalEntry(key: key, type: type, caption: caption, url:
                        url!, thumbnailUrl: thumbnailUrl!, date: dateStr?.dateFromISO8601, lat: lat, 
                              lng: lng))
                }
                strongSelf.entries = tmpItems
                strongSelf.entries.sort {$0.date! > $1.date! }
                strongSelf.tableView.reloadData()
            }
        })
    }
    
    func configureStorage() {
        let storageUrl = FirebaseApp.app()?.options.storageBucket
        self.storageRef = Storage.storage().reference(forURL: "gs://" + storageUrl!)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        // unregister from listeners here.
        if let r = self.ref {
            r.removeAllObservers()
        }
        
    }
    
    @IBAction func imageButtonPressed(_ sender: UIButton) {
        let row = Int(sender.tag)
        let indexPath = IndexPath(row: row, section: 0)
        let cell = self.tableView.cellForRow(at: indexPath) as! JournalEntryTableViewCell
        if let tnImg = cell.thumbnailImage {
            self.capturedImage = tnImg.image
        }
        self.entryToEdit = cell.entry
        if let entry = cell.entry {
            switch(entry.type!) {
            case .photo:
                self.performSegue(withIdentifier: "viewPhoto", sender: self)
            case.video:
                self.showContentOfUrlWithAVPlayer(url: entry.url)
            case .audio:
                self.showContentOfUrlWithAVPlayer(url: entry.url)
            default: break
            }
        }
    }
    
    func showContentOfUrlWithAVPlayer(url : String) {
        if url == "" { return}
        let mediaUrl = URL(string: url)
        let player = AVPlayer(url: mediaUrl!)
        let playerViewController = AVPlayerViewController()
        playerViewController.player = player
        self.present(playerViewController, animated: true) {
            playerViewController.player!.play()
        }
    }
    
    @IBAction func editButtonPressed(_ sender: UIButton) {
        let row = Int(sender.tag)
        print("Row \(row) edit button pressed.")
        let indexPath = IndexPath(row: row, section: 0)
        let cell = self.tableView.cellForRow(at: indexPath) as! JournalEntryTableViewCell
        if let tnImg = cell.thumbnailImage {
            self.capturedImage = tnImg.image
        }
        self.entryToEdit = cell.entry
        self.captureType = (self.entryToEdit?.type)!
        self.performSegue(withIdentifier: "confirmSegue", sender: self)
    }
    
    // MARK: - Navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "confirmSegue" {
            if let destCtrl = segue.destination as? JournalEntryConfirmationViewController {
                destCtrl.imageToConfirm = self.capturedImage
                destCtrl.delegate = self
                destCtrl.type = self.captureType
                destCtrl.entry = self.entryToEdit  // will be nil on new item
                destCtrl.journal = self.journal
            }
        } else if segue.identifier == "viewPhoto" {
            if let destCtrl = segue.destination as? PhotoViewController {
                destCtrl.imageToView = self.capturedImage
                destCtrl.captionToView = self.entryToEdit?.caption
            }
        }
    }
    
    func displayCameraIfPermitted() {
        let cameraMediaType = AVMediaType.video
        let cameraAuthorizationStatus =
            AVCaptureDevice.authorizationStatus(for: cameraMediaType)
        
        switch cameraAuthorizationStatus {
        case .denied:
            self.displaySettingsAppAlert()
        case .authorized:
            self.displayImagePicker(type: .camera)
        case .restricted: break
        case .notDetermined:
            // Prompting user for the permission to use the camera.
            AVCaptureDevice.requestAccess(for: cameraMediaType) { granted in
                if granted {
                    DispatchQueue.main.async {
                        self.displayImagePicker(type: .camera)
                    }
                } else {
                    print("Denied access to \\(cameraMediaType)")
                }
            }
        }
    }
    
    func displaySettingsAppAlert()
    {
        let avc = UIAlertController(title: "Camera Permission Required",
                message: "You need to provide this app permission use your camera for this feature. You can do this by going to your Settings app and going to Privacy -> Camera", preferredStyle: .alert)
        
        let settingsAction = UIAlertAction(title: "Settings", style: .default ) {
            action in
            UIApplication.shared.open(NSURL(string: UIApplicationOpenSettingsURLString)!
                as URL, options: [:], completionHandler: nil)
            
        }
        
        let cancelAction = UIAlertAction(title: "Cancel", style: .cancel, handler: nil)
        avc.addAction(settingsAction)
        avc.addAction(cancelAction)
        
        self.present(avc, animated: true, completion: nil)
    }
    
    func displayImagePicker(type: UIImagePickerControllerSourceType)
    {
        let picker = UIImagePickerController()
        picker.allowsEditing = false
        picker.sourceType = type
        picker.mediaTypes = [kUTTypeImage as String, kUTTypeMovie as String]
        picker.modalPresentationStyle = .fullScreen
        picker.delegate = self
        self.present(picker, animated: true, completion: nil)
    }
    
    @IBAction func addEntryButtonPressed(_ sender: UIBarButtonItem) {
        let alertController = UIAlertController(title: nil, message:
            "What kind of entry would you like to add to your journal?",
                                                preferredStyle: .actionSheet)
        
        let cancelAction = UIAlertAction(title: "Cancel", style: .cancel) { (action) in
            // ...
        }
        alertController.addAction(cancelAction)
        
        let addTextAction = UIAlertAction(title: "Text Entry", style: .default) {
            (action) in
            self.captureType = .text
            self.capturedImage = nil
            self.performSegue(withIdentifier: "confirmSegue", sender: self)
        }
        alertController.addAction(addTextAction)
        
        let addCameraAction = UIAlertAction(title: "Photo or Video Entry", style: .default) {
            (action) in
            self.displayCameraIfPermitted()
            
        }
        alertController.addAction(addCameraAction)
        
        let selectCameraRollAction = UIAlertAction(title: "Select from Camera Roll",
                                                   style: .default) { (action) in
            self.displayImagePicker(type: .photoLibrary)
        }
        alertController.addAction(selectCameraRollAction)
        
        
        let addAudioAction = UIAlertAction(title: "Audio Entry", style: .default) {
            (action) in
            // TBD
        }
        alertController.addAction(addAudioAction)
        
        self.present(alertController, animated: true) {
            self.entryToEdit = nil // always creating a new entry when this alert is displayed.
        }
        
    }

}

// MARK: - UITableViewDelegate and UITableViewDataSource

extension JournalTableViewController  {
    
//    override func tableView(_ tableView: UITableView, titleForHeaderInSection
//        section: Int) -> String? {
//        return self.journal.name
//    }
//    
//    override func tableView(_ tableView: UITableView, willDisplayHeaderView
//        view: UIView, forSection section: Int) {
//        let header = view as! UITableViewHeaderFooterView
//        header.textLabel?.textColor = THEME_COLOR2
//        header.contentView.backgroundColor = THEME_COLOR3
//    }
    
    
    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection
        section: Int) -> Int {
        return self.entries.count
    }
    
    
    override func tableView(_ tableView: UITableView,
                            cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let entry = self.entries[indexPath.row]
        var cellIds = ["NA", "TextCell", "PhotoCell", "AudioCell", "PhotoCell"]
        
        let cell = tableView.dequeueReusableCell(withIdentifier:
            cellIds[entry.type!.rawValue],
                                                 for: indexPath) as! JournalEntryTableViewCell
        
        cell.editButton?.tag = indexPath.row
        if let imgButton = cell.imageButton {
            imgButton.tag = cell.editButton!.tag
        }
        
        cell.setValues(entry: entry)
        if let iv = cell.thumbnailImage {
            iv.image = UIImage(named: "landscape")
        }
        switch(entry.type!) {
        case .photo:
            let url = URL(string: entry.url)
            cell.thumbnailImage.kf.indicatorType = .activity
            cell.thumbnailImage.kf.setImage(with: url)
            cell.playButton.isHidden = true
        case .video:
            let url = URL(string: entry.thumbnailUrl)
            cell.thumbnailImage.kf.indicatorType = .activity
            cell.thumbnailImage.kf.setImage(with: url)
            cell.playButton.isHidden = false
            cell.playButton.tag = indexPath.row
        default: break
        }
        
        return cell
    }
}

extension JournalTableViewController : UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        print("canceled")
        self.dismiss(animated: true, completion: nil)
    }
    
    func imagePickerController(_ picker: UIImagePickerController,
                               didFinishPickingMediaWithInfo info: [String : Any])
    {
        self.dismiss(animated: true, completion: nil)
        if let mediaType = info[UIImagePickerControllerMediaType] as? String {
            if mediaType == kUTTypeMovie as String {
                print("got video")
                self.capturedImage = self.thumbnailForVideoAtURL(url:
                    info[UIImagePickerControllerMediaURL] as! URL)
                self.captureVideoUrl = info[UIImagePickerControllerMediaURL] as? URL
                self.captureType = .video
            } else {
                print("got image")
                self.capturedImage = info[UIImagePickerControllerOriginalImage] as? UIImage
                self.captureType = .photo
            }
        }
        self.performSegue(withIdentifier: "confirmSegue", sender: self)
    }
    
    
    func thumbnailForVideoAtURL(url: URL) -> UIImage? {
        let asset = AVURLAsset(url: url)
        let generator = AVAssetImageGenerator(asset: asset)
        generator.appliesPreferredTrackTransform = true
        var time = asset.duration
        time.value = min(time.value, 2)
        
        do {
            let imageRef = try generator.copyCGImage(at: time, actualTime: nil)
            return UIImage(cgImage: imageRef)
        } catch {
            print("error")
            return nil
        }
    }
}

extension JournalTableViewController : AddJournalEntryDelegate {
    
    func save(entry: JournalEntry) {
        switch(entry.type!) {
        case .photo:
            self.savePhoto(entry: entry)
        case .video:
            self.saveVideo(entry: entry)
        case .text:
            let vals = self.toDictionary(vals: entry)
            vals["url"]  = ""
            _ = self.saveEntryToFireBase(key: entry.key, ref: self.ref, vals: vals)
        case .audio:
            self.saveAudio(entry: entry)
        }
    }
    
    func saveEntryToFireBase(key: String?, ref : DatabaseReference?, vals:
        NSMutableDictionary) -> DatabaseReference?
    {
        var child : DatabaseReference?
        if let k = key {
            child = ref?.child("entries").child(k)
            child?.setValue(vals)
        } else {
            child = ref?.child("entries").childByAutoId()
            child?.setValue(vals)
        }
        return child
    }
    
    func toDictionary(vals: JournalEntry) -> NSMutableDictionary {
        return [
            "caption": vals.caption! as NSString,
            "lat": vals.lat! as NSNumber,
            "lng": vals.lng! as NSNumber,
            "date" : NSString(string: (vals.date?.iso8601)!) ,
            "type" : NSNumber(value: vals.type!.rawValue),
            "url" : vals.url as NSString,
            "thumbnailUrl" : vals.thumbnailUrl as NSString
        ]
    }
    
    func savePhoto(entry: JournalEntry) {
        let vals = self.toDictionary(vals: entry)
        let entryRef = self.saveEntryToFireBase(key: entry.key, ref: self.ref, vals: vals)
        if entry.key == nil {
            self.saveImageToFirebase(imageToSave: self.capturedImage, saveRefClosure: {
                (downloadUrl) in
                // store the image URL
                let vals = [
                    "url" : downloadUrl as NSString
                ]
                entryRef?.updateChildValues(vals)
            })
        }
    }
    
    func saveImageToFirebase(imageToSave : UIImage?, saveRefClosure: @escaping
        (String) -> ())
    {
        if let image = imageToSave {
            let imageData = UIImageJPEGRepresentation(image, 0.8)
            let imagePath = "\(self.userId!)/photos/\(Int(Date.timeIntervalSinceReferenceDate * 1000)).jpg"
            let metadata = StorageMetadata()
            metadata.contentType = "image/jpeg"
            if let sr = self.storageRef {
                sr.child(imagePath)
                    .putData(imageData!, metadata: metadata) { (metadata, error) in
                        if let error = error {
                            print("Error uploading: \(error)")
                            return
                        }
                        
                        let downloadUrl = metadata?.downloadURL()!.absoluteString
                        saveRefClosure(downloadUrl!)
                }
            }
        }
    }
    
    func saveVideo(entry: JournalEntry) {
        
        let vals = self.toDictionary(vals: entry)
        let entryRef = self.saveEntryToFireBase(key: entry.key, ref: self.ref, vals: vals)
        
        // if we have a nil key, then this is a new entry and we have an video file to save.
        // as well as a thmbnai image.
        if entry.key == nil {
            
            // save captured video
            if let url = self.captureVideoUrl {
                var newEntry = entry
                newEntry.url = url.absoluteString
                self.saveMediaFileToFirebase(entry: newEntry, saveRefClosure: { (downloadUrl)
                    in
                    
                    // record the URL of the video
                    let vals = [
                        "url" : downloadUrl as NSString
                    ]
                    print("Updating URL video: \\(downloadUrl)")
                    entryRef?.updateChildValues(vals)
                    
                })
            }
            
            // save video's thumbnail image
            self.saveImageToFirebase(imageToSave: self.capturedImage, saveRefClosure: {
                (downloadUrl) in
                
                // record the URL of the thumbnail
                let vals = [
                    "thumbnailUrl" : downloadUrl as NSString
                ]
                print("Updating thumbnail URL : \(downloadUrl)")
                entryRef?.updateChildValues(vals)
            })
        }
    }
    
    func saveMediaFileToFirebase(entry: JournalEntry, saveRefClosure: @escaping (String)
        -> () ) {
        
        let type : String = entry.type! == .audio ? "audio" : "video"
        let ext : String = entry.type! == .audio ? "m4a" : "mp4"
        let mime : String = entry.type! == .audio ? "audio/mp4" : "video/mp4"
        
        do {
            
            if  entry.url != ""  {
                let url = URL(string: entry.url)
                let media = try Data(contentsOf: url!)
                print("got data")
                let mediaPath = "\(self.userId!)/\(type)/\(Int(Date.timeIntervalSinceReferenceDate * 1000)).\(ext)"
                let metadata = StorageMetadata()
                metadata.contentType = mime
                if let sr = self.storageRef {
                    sr.child(mediaPath)
                        .putData(media, metadata: metadata) {(metadata, error) in
                            if let error = error {
                                print("Error uploading: \(error)")
                                return
                            }
                            
                            saveRefClosure((metadata?.downloadURL()!.absoluteString)!)
                    }
                }
            }
        } catch {
            print("oops that wasn't good now")
        }
    }
    
    func saveAudio(entry: JournalEntry) {
        // TODO: stub method, to be completed in next chapter.
    }
    
    
}

