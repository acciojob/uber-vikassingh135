package com.driver.services.impl;

import com.driver.model.Cab;
import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.CabRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

        @Autowired
        CabRepository cabRepository;
        
	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
                customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
                Customer customer = customerRepository2.findById(customerId).get();
                customerRepository2.delete(customer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
                List<Driver> drivers = driverRepository2.findAll();
                Driver selectedDriver = null;
                for(Driver driver : drivers) {
                    Cab cab = driver.getCab();
                    if(cab.isAvailable()) {
                       if(selectedDriver == null) {
                           selectedDriver = driver;
                       } else if(selectedDriver.getDriverId()>driver.getDriverId()) {
                           selectedDriver = driver;
                       }
                    }
                }
                
                if(selectedDriver == null) {
                    throw new Exception("No cab available!");
                }
                
                Customer customer = customerRepository2.findById(customerId).get();
                
                TripBooking trip = new TripBooking();
                trip.setFromLocation(fromLocation);
                trip.setToLocation(toLocation);
                trip.setBill(selectedDriver.getCab().getPerKmRate()*distanceInKm);
                trip.setCustomer(customer);
                trip.setDistanceInKm(distanceInKm);
                trip.setDriver(selectedDriver);
                trip.setTripStatus(TripStatus.CONFIRMED);
                
                customer.getTripBookingList().add(trip);
                selectedDriver.getTripBookingList().add(trip);
                customerRepository2.save(customer);
                driverRepository2.save(selectedDriver);
                return trip;

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
                if(tripId==null) return;
                
                TripBooking trip  = tripBookingRepository2.findById(tripId).get();
                Cab cab = trip.getDriver().getCab();
                cab.setAvailable(true);
                trip.setBill(0);
                trip.setTripStatus(TripStatus.CANCELED);
                cabRepository.save(cab);
                tripBookingRepository2.save(trip);

	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly

                if(tripId==null) return;
                
                TripBooking trip  = tripBookingRepository2.findById(tripId).get();
                Cab cab = trip.getDriver().getCab();
                cab.setAvailable(true);
                trip.setTripStatus(TripStatus.COMPLETED);
                cabRepository.save(cab);
                tripBookingRepository2.save(trip);
	}
}
