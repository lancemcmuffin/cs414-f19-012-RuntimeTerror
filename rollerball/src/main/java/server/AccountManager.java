package server;

import java.util.regex.Pattern;

import server.api.RegistrationRequest;
import server.api.RegistrationResponse;

public class AccountManager {
	public static RegistrationResponse registerUser(RegistrationRequest request){
		RegistrationResponse response = new RegistrationResponse();
		response.errorMessage = "";
		response.success = true;
		
		if(request.password == null || request.password.length() < 8){
			response.success = false;
			response.errorMessage += "Password must be at least 8 characters.\n";
		}
		
		if(!request.email.matches("[a-zA-Z1-9.]+@([a-zA-Z1-9.]+\\.[a-zA-Z0-9]+)+")){
			response.success = false;
			response.errorMessage+="Invalid email.\n";
		}
		
		if(request.username.equals("admin")||!request.username.matches("[a-zA-Z0-9]+")){
			response.success = false;
			response.errorMessage+="Invalid username.\n";
		}
		
		return response;
	}
}