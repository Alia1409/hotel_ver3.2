module HotelReservationSystem {
	
    requires java.desktop;
    requires java.net.http;
    requires org.junit.jupiter.api;
    
    exports view; 
    
    exports model;
    exports controller;
    exports service;
    exports persistence;
    
    opens model to org.junit.jupiter.api;
    opens service to org.junit.jupiter.api;
    opens controller to org.junit.jupiter.api;
    opens persistence to org.junit.jupiter.api;
}
