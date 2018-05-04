//
//  MapViewController.swift
//  TraxyApp
//
//  Created by Jonathan Engelsma on 5/4/18.
//  Copyright © 2018 Jonathan Engelsma. All rights reserved.
//

import UIKit
import GoogleMaps

class MapViewController: TraxyTopLevelViewController {
    
    var tappedJournal : Journal? = nil
    var locationManager:CLLocationManager!
    var currentLocation:CLLocation?
    
    var markers:[GMSMarker] = []
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // zoom map to center of USA by default
        let camera = GMSCameraPosition.camera(withLatitude: 39.844546,  longitude:
            -101.304128, zoom: 3.0)
        let mapView = GMSMapView.map(withFrame: CGRect.zero, camera: camera)
        mapView.isMyLocationEnabled = true
        mapView.settings.myLocationButton = true
        mapView.delegate = self
        view = mapView
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.getCurrentLocation()
        if let mapView = self.view as? GMSMapView {
            mapView.clear()
            self.markers.removeAll()
            if let journs = self.journals {
                for j in journs {
                    let marker = GMSMarker()
                    marker.position = CLLocationCoordinate2D(latitude: j.lat!, longitude: j.lng!)
                    marker.title = j.name
                    marker.snippet =  j.location
                    marker.userData = j
                    marker.map = mapView
                    markers.append(marker)
                }
            }
            
            self.focusMapToShowMarkers(markers: markers)
        }
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "showJournalSegue" {
            if let destVC = segue.destination as? JournalTableViewController {
                destVC.journal  = self.tappedJournal
                destVC.userId = self.userId
            }
        }
    }
    
    func focusMapToShowMarkers(markers: [GMSMarker]) {
        
        if let mapView = self.view as? GMSMapView {
            
            var bounds : GMSCoordinateBounds!
            if let current = self.currentLocation {
                bounds = GMSCoordinateBounds(coordinate: current.coordinate,
                                             coordinate: current.coordinate)
            } else {
                bounds = GMSCoordinateBounds()
            }
            
            
            _ = markers.map {
                bounds = bounds.includingCoordinate($0.position)
            }
            
            mapView.animate(with: GMSCameraUpdate.fit(bounds, withPadding: 15.0))
            
        }
    }
    
    func getCurrentLocation() {
        self.locationManager = CLLocationManager()
        self.locationManager.delegate = self
        self.locationManager.desiredAccuracy = kCLLocationAccuracyBest
        
        if CLLocationManager.locationServicesEnabled() {
            switch(CLLocationManager.authorizationStatus()) {
            case .notDetermined:
                print("Not Determined")
                self.locationManager.requestWhenInUseAuthorization()
            case .restricted, .denied:
                print("No access")
                self.askForLocationPermits()
            case .authorizedAlways, .authorizedWhenInUse:
                print("Access")
                self.locationManager.startUpdatingLocation()
            }
        } else {
            self.askForLocationPermits()
        }
    }
    
    func askForLocationPermits()
    {
        // only display once a day!
        if let lastUpdate = UserDefaults.standard.object(forKey:
            "LastAskedForLocationPermits") as? Date {
            
            if lastUpdate.days(from: Date()) >= 1  {
                displayNoLocationAccessAlert()
            }
        } else {
            displayNoLocationAccessAlert()
        }
    }
    
    func displayNoLocationAccessAlert()
    {
        // remember when we last asked
        UserDefaults.standard.set(Date(), forKey: "LastAskedForLocationPermits")
        let alertController = UIAlertController(
            title: "Location Services disabled",
            message: "The app would like your current location but Location Services on your device is currently disabled for this app.",
            preferredStyle: .alert)
        
        let cancelAction = UIAlertAction(title: "Cancel", style: .cancel, handler: nil)
        alertController.addAction(cancelAction)
        
        let openAction = UIAlertAction(title: "Location Settings", style: .default)
        { (action) in
            if let url = URL(string:UIApplicationOpenSettingsURLString) {
                //UIApplication.shared.openURL(url)
                UIApplication.shared.open(url, options: [:], completionHandler: nil)
            }
        }
        alertController.addAction(openAction)
        
        self.present(alertController, animated: true, completion: nil)
    }
    
}

extension MapViewController : GMSMapViewDelegate {
    func mapView(_ mapView: GMSMapView, didTapInfoWindowOf marker: GMSMarker) {
        if let data = marker.userData as? Journal  {
            self.tappedJournal = data
            self.performSegue(withIdentifier: "showJournalSegue", sender: self)
        } else {
            self.tappedJournal = nil
        }
    }
}

extension MapViewController : CLLocationManagerDelegate {
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations:
        [CLLocation]) {
        self.currentLocation = locations[0] as CLLocation
        manager.stopUpdatingLocation()
        self.focusMapToShowMarkers(markers: self.markers)
    }
}

