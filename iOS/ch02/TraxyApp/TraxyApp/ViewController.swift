//
//  ViewController.swift
//  TraxyApp
//
//  Created by Jonathan Engelsma on 1/5/17.
//  Copyright © 2017 Jonathan Engelsma. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

    @IBOutlet weak var emailField: UITextField!
    @IBOutlet weak var passwordField: UITextField!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // dismiss keyboard when tapping outside oftext fields
        let detectTouch = UITapGestureRecognizer(target: self, action:
            #selector(self.dismissKeyboard))
        self.view.addGestureRecognizer(detectTouch)
        
        // make this controller the delegate of the text fields.
        self.emailField.delegate = self
        self.passwordField.delegate = self
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    func dismissKeyboard() {
        self.view.endEditing(true)
    }
    
    func validateFields() -> Bool {
        var pwOk = false
        if let pw = self.passwordField.text {
            if pw.lowercased().range(of: "traxy") != nil {
                pwOk = true
            }
        }
        if !pwOk {
            print(NSLocalizedString("Invalid password", comment: ""))
        }
        
        var emailOk = false
        if let email = self.emailField.text {
            let regex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
            let emailPredicate = NSPredicate(format:"SELF MATCHES %@", regex)
            emailOk = emailPredicate.evaluate(with: email)
        }
        if !emailOk {
            print(NSLocalizedString("Invalid email address", comment: ""))
        }
        return emailOk && pwOk
    }

    @IBAction func signupButtonPressed(_ sender: UIButton) {
        if self.validateFields() {
            print(NSLocalizedString("Congratulations! You entered correct values.", comment: ""))
        }
    }
}

extension ViewController : UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        if textField == self.emailField {
            self.passwordField.becomeFirstResponder()
        } else {
            if self.validateFields() {
                print(NSLocalizedString("Congratulations! You entered correct values.", comment: ""))
            }
        }
        return true
    }
}
