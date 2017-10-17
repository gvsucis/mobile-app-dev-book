//
//  AudioViewController.swift
//  TraxyApp
//
//  Created by Jonathan Engelsma on 9/11/17.
//  Copyright © 2017 Jonathan Engelsma. All rights reserved.
//

import UIKit
import AVFoundation

class AudioViewController: UIViewController {
    
    @IBOutlet weak var saveButton: UIBarButtonItem!
    @IBOutlet weak var timeLabel: UILabel!
    @IBOutlet weak var recordButton: UIButton!
    @IBOutlet weak var playButton: UIButton!
    var recording = false
    var playing = false
    var entry : JournalEntry?
    var journal : Journal!
    
    let playImage =  UIImage(named: "play")
    let stopImage = UIImage(named: "stop")
    let recordImage = UIImage(named: "record")
    
    var captionEntryCtrl : JournalEntryConfirmationViewController?
    var recorder: AVAudioRecorder!
    var player: AVAudioPlayer!
    var audioTimer:Timer!
    var audioFileUrl:URL!
    weak var delegate : AddJournalEntryDelegate?

    override func viewDidLoad() {
        super.viewDidLoad()

        self.playButton.isEnabled = false
        self.setSessionPlayback()
        if self.entry == nil  {
            self.entry = JournalEntry(key: nil, type: .audio, caption: "", url: "", thumbnailUrl: "", date: Date(), lat: 0.0, lng: 0.0)
        }
        
        self.saveButton.isEnabled = false
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func saveButtonPressed(_ sender: UIBarButtonItem) {
        if let del = self.delegate {
            if let (caption,date,_) = (self.captionEntryCtrl?.extractFormValues()) {
                if var e = self.entry {
                    e.url = self.recorder.url.absoluteString
                    e.caption = caption
                    e.date = date
                    del.save(entry: e)
                }
            }
        }
        _ = self.navigationController?.popViewController(animated: true)
    }
    
    @IBAction func cancelButtonPressed(_ sender: UIBarButtonItem) {
        _ = self.navigationController?.popViewController(animated: true)
    }
    
    @IBAction func playButtonPressed(_ sender: UIButton) {
        if self.playing {
            self.playButton.setImage(self.playImage, for: .normal)
            self.recordButton.isEnabled = true
            self.playing = false
            self.player.stop()
        } else {
            self.playButton.setImage(self.stopImage, for: .normal)
            self.recordButton.isEnabled = false
            self.playing = true
            self.setSessionPlayback()
            self.play()
        }
    }
    
    func play() {
        var url:URL?
        if self.recorder != nil {
            url = self.recorder.url
        } else {
            url = self.audioFileUrl!
        }
        do {
            self.player = try AVAudioPlayer(contentsOf: url!)
            player.delegate = self
            player.prepareToPlay()
            player.volume = 1.0
            player.play()
        } catch let error as NSError {
            self.player = nil
            print(error.localizedDescription)
        }
    }
    
    @IBAction func recordButtonPressed(_ sender: UIButton) {
        if self.recording {
            self.recordButton.setImage(self.recordImage, for: .normal)
            self.playButton.isEnabled = true
            self.recording = false
            self.recorder.stop()
        } else {
            // Note we adjust the UI in recordIfPermitted, as we don't know
            // at this point if permissions have been granted.
            if recorder == nil {
                self.recordIfPermitted(setup: true)
            } else {
                self.recordIfPermitted(setup: false)
            }
        }
    }
    
    func updateTimeLabel(timer:Timer) {
        
        if self.recorder.isRecording {
            let min = Int(recorder.currentTime / 60)
            let sec = Int(recorder.currentTime.truncatingRemainder(dividingBy: 60))
            let s = String(format: "%02d:%02d", min, sec)
            self.timeLabel.text = s
            recorder.updateMeters()
        }
    }
    
    func recordIfPermitted(setup:Bool)  {
        let session:AVAudioSession = AVAudioSession.sharedInstance()
        if (session.responds(to:
            #selector(AVAudioSession.requestRecordPermission(_:))))
        {
            AVAudioSession.sharedInstance().requestRecordPermission({(granted: Bool)->
                Void in
                DispatchQueue.main.async {
                    if granted {
                        self.setSessionPlayAndRecord()
                        if setup {
                            self.setupRecorder()
                        }
                        self.recorder.record()
                        self.audioTimer = Timer.scheduledTimer(timeInterval: 0.1, target:self,
                                                               selector:#selector(AudioViewController.updateTimeLabel(timer:)),
                                                               userInfo:nil,repeats:true)
                        
                        // we're rolling!  update the UI
                        self.recordButton.setImage(self.stopImage, for: .normal)
                        self.playButton.isEnabled = false
                        self.recording = true
                    } else {
                        self.displaySettingsAppAlert()
                    }
                }
            })
        } else {
            // technically, this happens when the device doesn't have a mic.
            self.displaySettingsAppAlert()
        }
    }
    
    func displaySettingsAppAlert()
    {
        let avc = UIAlertController(title: "Mic Permission Required", message: "You need to provide this app permissions to use your microphone for this feature. You can do this by going to your Settings app and going to Privacy -> Microphone", preferredStyle: .alert)
        
        let settingsAction = UIAlertAction(title: "Settings", style: .default ) {action in
            UIApplication.shared.open(NSURL(string: UIApplicationOpenSettingsURLString)! as URL, options:
                [:], completionHandler: nil)
            
        }
        
        let cancelAction = UIAlertAction(title: "Cancel", style: .cancel, handler: nil)
        avc.addAction(settingsAction)
        avc.addAction(cancelAction)
        
        self.present(avc, animated: true, completion: nil)
    }
    
    func setupRecorder() {
        let format = DateFormatter()
        format.dateFormat="yyyy-MM-dd-HH-mm-ss"
        let currentFileName = "audio-\\(format.string(from: Date())).m4a"
        let documentsDirectory = FileManager.default.urls(for: .documentDirectory,
                                                          in: .userDomainMask)[0]
        self.audioFileUrl =
            documentsDirectory.appendingPathComponent(currentFileName)
        if FileManager.default.fileExists(atPath: audioFileUrl.absoluteString) {
            print("File \\(audioFileUrl.absoluteString) already exists!")
        }
        let recordSettings:[String : AnyObject] = [
            AVFormatIDKey:             NSNumber(value: kAudioFormatMPEG4AAC),
            AVEncoderAudioQualityKey : NSNumber(value:AVAudioQuality.max.rawValue),
            AVEncoderBitRateKey :      NSNumber(value:320000),
            AVNumberOfChannelsKey:     NSNumber(value:2),
            AVSampleRateKey :          NSNumber(value:44100.0)
        ]
        do {
            recorder = try AVAudioRecorder(url: audioFileUrl, settings: recordSettings)
            recorder.delegate = self
            recorder.isMeteringEnabled = true
            recorder.prepareToRecord()
        } catch let error as NSError {
            recorder = nil
            print(error.localizedDescription)
        }
    }

    func setSessionPlayback() {
        let session:AVAudioSession = AVAudioSession.sharedInstance()
        
        do {
            try session.setCategory(AVAudioSessionCategoryPlayback)
        } catch let error as NSError {
            print(error.localizedDescription)
        }
        do {
            try session.setActive(true)
        } catch let error as NSError {
            print(error.localizedDescription)
        }
    }
    
    func setSessionPlayAndRecord() {
        let session = AVAudioSession.sharedInstance()
        do {
            try session.setCategory(AVAudioSessionCategoryPlayAndRecord)
        } catch let error as NSError {
            print(error.localizedDescription)
        }
        do {
            try session.setActive(true)
        } catch let error as NSError {
            print(error.localizedDescription)
        }
    }
    


    // MARK: - Navigation

    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "setupAudioCaptionForm" {
            if let destCtrl = segue.destination as? JournalEntryConfirmationViewController {
                destCtrl.type = .audio
                destCtrl.entry = self.entry
                destCtrl.journal = self.journal
                self.captionEntryCtrl = destCtrl
            }
        }
    }

}

extension AudioViewController : AVAudioRecorderDelegate {
    
    func audioRecorderDidFinishRecording(_ recorder: AVAudioRecorder,
                                         successfully flag: Bool) {
        self.saveButton.isEnabled = true
        self.recordButton.isEnabled = true
        self.playButton.isEnabled = true
        self.playButton.setImage(self.playImage, for: .normal)
        self.recordButton.setImage(self.recordImage, for: .normal)
    }
    
    func audioRecorderEncodeErrorDidOccur(_ recorder: AVAudioRecorder,
                                          error: Error?) {
        
        if let e = error {
            print("\\(e.localizedDescription)")
        }
    }
}

extension AudioViewController : AVAudioPlayerDelegate {
    func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        self.recordButton.isEnabled = true
        self.playButton.isEnabled = true
        self.playButton.setImage(self.playImage, for: .normal)
        self.recordButton.setImage(self.recordImage, for: .normal)
    }
    
    func audioPlayerDecodeErrorDidOccur(_ player: AVAudioPlayer, error: Error?) {
        if let e = error {
            print("\\(e.localizedDescription)")
        }
    }
}

