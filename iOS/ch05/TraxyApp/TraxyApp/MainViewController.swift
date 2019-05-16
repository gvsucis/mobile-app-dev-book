//
//  MainViewController.swift
//  TraxyApp
//
//  Created by Jonathan Engelsma on 2/7/17.
//  Copyright © 2017 Jonathan Engelsma. All rights reserved.
//

import UIKit

class MainViewController: UIViewController {
    
    @IBOutlet weak var loginLabel: UILabel!
    var userEmail : String?
    
    override func viewDidLoad() {
        super.viewDidLoad()

        if let email = self.userEmail {
            self.loginLabel.text = email
        }
        self.setNeedsStatusBarAppearanceUpdate()
    }

    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
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

extension UINavigationController {
    override open var preferredStatusBarStyle : UIStatusBarStyle {
        return topViewController?.preferredStatusBarStyle ?? .default
    }
}

