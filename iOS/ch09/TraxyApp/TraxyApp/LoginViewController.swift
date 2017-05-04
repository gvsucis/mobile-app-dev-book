//
//  ViewController.swift
//  TraxyApp
//
//  Created by Jonathan Engelsma on 1/5/17.
//  Copyright © 2017 Jonathan Engelsma. All rights reserved.
//

import UIKit
import FirebaseAuth

class LoginViewController: TraxyLoginViewController {

    @IBOutlet weak var emailField: UITextField!
    @IBOutlet weak var passwordField: UITextField!
    var validationErrors = ""
    
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
        let pwOk = self.isEmptyOrNil(password: self.passwordField.text)
        if !pwOk {
            self.validationErrors += "Password cannot be blank. "
        }
        
        let emailOk = self.isValidEmail(emailStr: self.emailField.text)
        if !emailOk {
            self.validationErrors += "Invalid email address."
        }
        
        return emailOk && pwOk
    }

    @IBAction func signupButtonPressed(_ sender: UIButton) {
        if self.validateFields() {
            print("Congratulations!  You entered correct values.")
            FIRAuth.auth()?.signIn(withEmail: self.emailField.text!, password:
            self.passwordField.text!) { (user, error) in
                if let _ = user {
                    self.performSegue(withIdentifier: "segueToMain", sender: self)
                } else {
                    self.reportError(msg: (error?.localizedDescription)!)
                    self.passwordField.text = ""
                    self.passwordField.becomeFirstResponder()
                }
            }
        } else {
            self.reportError(msg: self.validationErrors)
        }
    }
    
    @IBAction func logout(segue : UIStoryboardSegue) {
        do {
            try FIRAuth.auth()?.signOut()
            print("Logged out")
        } catch let signOutError as NSError {
            print ("Error signing out: %@", signOutError)
        }
        
        self.passwordField.text = ""
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "segueToMain" {
            if let destVC = segue.destination.childViewControllers[0] as? MainViewController {
                destVC.userEmail = self.emailField.text
            }
        }
    }
    
}

extension LoginViewController : UITextFieldDelegate {
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
