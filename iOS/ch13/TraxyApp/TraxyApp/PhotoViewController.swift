//
//  PhotoViewController.swift
//  TraxyApp
//
//  Created by Jonathan Engelsma on 5/4/17.
//  Copyright © 2017 Jonathan Engelsma. All rights reserved.
//

import UIKit

class PhotoViewController: UIViewController, UIScrollViewDelegate {
    @IBOutlet weak var caption: UITextView!
    @IBOutlet weak var imgTrailingConstraint: NSLayoutConstraint!
    @IBOutlet weak var captionContainer: UIView!
    @IBOutlet weak var imgLeadingConstraint: NSLayoutConstraint!
    @IBOutlet weak var imgTopConstraint: NSLayoutConstraint!
    @IBOutlet weak var imgBottomConstraint: NSLayoutConstraint!
    @IBOutlet weak var imageView: UIImageView!
    @IBOutlet weak var scrollView: UIScrollView!
    
    var imageToView : UIImage?
    var captionToView : String?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.imageView.image = self.imageToView
        self.imageView.sizeToFit()
        self.captionContainer.layer.cornerRadius = 10.0
        self.caption.text = self.captionToView
        
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        updateMinZoomScaleForSize(size: self.view.bounds.size)
        
        // make sure caption positions text at top.
        self.caption.setContentOffset(CGPoint.zero, animated: false)
    }
    
    
    // MARK: - UIScrollViewDelegate
    func viewForZooming(in scrollView: UIScrollView) -> UIView? {
        return self.imageView
    }
    
    private func updateMinZoomScaleForSize(size: CGSize) {
        let widthScale = size.width / self.imageView.bounds.width
        let heightScale = size.height / self.imageView.bounds.height
        let minScale = min(widthScale, heightScale)
        
        self.scrollView.minimumZoomScale = minScale
        
        self.scrollView.zoomScale = minScale
    }
    
    func scrollViewDidZoom(_ scrollView: UIScrollView) {
        updateConstraintsForSize(size: view.bounds.size)
    }
    
    private func updateConstraintsForSize(size: CGSize) {
        
        let yOffset = max(0, (size.height - imageView.frame.height) / 2)
        imgTopConstraint.constant = yOffset
        imgBottomConstraint.constant = yOffset
        
        let xOffset = max(0, (size.width - imageView.frame.width) / 2)
        imgLeadingConstraint.constant = xOffset
        imgTrailingConstraint.constant = xOffset
        
        view.layoutIfNeeded()
    }
    
    /*
     // MARK: - Navigation
     
     // In a storyboard-based application, you will often want to do a little preparation before navigation
     override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
     // Get the new view controller using segue.destinationViewController.
     // Pass the selected object to the new view controller.
     }
     */
    
}
