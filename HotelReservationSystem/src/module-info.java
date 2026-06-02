module HotelReservationSystem {
	
    requires java.desktop;
    requires java.net.http;
    
    exports view; 
    
    exports model;
    exports controller;
    exports service;
    exports persistence;
}
