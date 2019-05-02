//
//  TraxyTabBarController.swift
//  TraxyApp
//
//  Created by Jonathan Engelsma on 10/17/17.
//  Copyright © 2017 Jonathan Engelsma. All rights reserved.
//

import UIKit
import Firebase
import FirebaseDatabase

class TraxyTabBarViewController: UITabBarController {
    var userId : String? = ""
    
    override func viewDidLoad() {
        super.viewDidLoad()
        UITabBar.appearance().isTranslucent = false
        UITabBar.appearance().barTintColor = THEME_COLOR2
        UITabBar.appearance().tintColor = THEME_COLOR3
        Auth.auth().addStateDidChangeListener { auth, user in
            if let user = user {
                self.userId = user.uid
                for child in self.children {
                    if let nc = child as? UINavigationController {
                        if let c = nc.children[0]
                            as? TraxyTopLevelViewController {
                            c.userId = self.userId
                        }
                    }
                }
            } else {
                // No user is signed in.
                self.performSegue(withIdentifier: "presentLogin", sender: self)
            }
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
    }
    
    @IBAction func unwindFromSignup(segue: UIStoryboardSegue) {
        // we end up here when the user signs up for a new account.
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
}
