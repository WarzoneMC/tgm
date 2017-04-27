package com.minehut.teambackend;
import static spark.Spark.*;

/**
 * Created by luke on 4/27/17.
 */
public class TeamBackend {

	public static void main(String[] args) {
		
		port(44556); // http(s)://hostname:44556/*
		
		//TODO setup keystore for HTTPS api calls
		//secure(keystoreFilePath, keystorePassword, truststoreFilePath, truststorePassword);
		
		/*
		 * This method will be called by the spigot server to create
		 * a new user model in the database if there isn't already one.
		 * If a model exists, then that info will be returned and the
		 * user will be authenticated
		 * 
		 * @see UserProfile
		 */
		post("/player/login", (request, response) -> {
			//TODO login logic
			
			
			return "null";
		});
		
	}
	
}
