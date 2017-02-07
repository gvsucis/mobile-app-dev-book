//
//  SignUpViewController.swift
//  TraxyApp
//
//  Created by Jonathan Engelsma on 2/7/17.
//  Copyright © 2017 Jonathan Engelsma. All rights reserved.
//

import UIKit

class SignUpViewController: UIViewController {
    
    @IBOutlet weak var emailField: UITextField!
    @IBOutlet weak var passwordField: UITextField!
    @IBOutlet weak var verifyPasswordField: UITextField!
    

    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    func validateFields() -> Bool {
        
        let pwOk = self.isEmptyOrNil(password: self.passwordField.text)
        if !pwOk {
            print("Invalid password")
        }
        
        let pwMatch = self.passwordField.text == self.verifyPasswordField.text
        if !pwMatch {
            print("Passwords do not match.")
        }
        
        let emailOk = self.isValidEmail(emailStr: self.emailField.text)
        if !emailOk {
            print("Invalid email address")
        }
        
        return emailOk && pwOk && pwMatch
    }
    
    @IBAction func signupButtonPressed(_ sender: UIButton) {
        if self.validateFields() {
            print("Congratulations!  You entered correct values.")
            self.performSegue(withIdentifier: "segueToMainFromSignUp", sender: self)
        }
    }
    
    @IBAction func cancelButtonPressed(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
    }

    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "segueToMainFromSignUp" {
            if let destVC = segue.destination.childViewControllers[0] as? MainViewController {
                destVC.userEmail = self.emailField.text
            }
        }
    }
    
}

extension SignUpViewController : UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        if textField == self.emailField {
            self.passwordField.becomeFirstResponder()
        } else if textField == self.passwordField {
            self.verifyPasswordField.becomeFirstResponder()
        } else {
            if self.validateFields() {
                print("Congratulations!  You entered correct values.")
            }
        }
        return true
    }
}
