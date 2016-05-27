package server;

import java.util.logging.LogManager;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import com.restfb.exception.FacebookException;
import com.restfb.types.User;

public class FacebookServer {

	private FacebookClient facebookClient;

	/**
	 * @param String token
	 * @return String with UID or null, if the token doesn't match any user.
	 */
	public static String tokenValidation(String token) {
		try {
	      LogManager.getLogManager().readConfiguration(FacebookServer.class.getResourceAsStream("/server/settings/properties"));
	    } catch (Exception e) {
	      throw new IllegalStateException("Could not read in settings configuration", e);
	    }
		
		if (token == null)
			throw new IllegalArgumentException(
					"You must provide an OAuth access token parameter. " + "See README for more information.");

		return (new FacebookServer(token)).getID();
	}

	FacebookServer(String accessToken) {
		facebookClient = new DefaultFacebookClient(accessToken, Version.VERSION_2_6);
	}


	private String getID() {
		try{
			User u =  facebookClient.fetchObject("me", User.class);
			System.out.println("Name: " + u.getName());
			return u.getId();
		} catch (FacebookException e) {
			System.err.println("FATAL ERROR: Invalid TOKEN.");
			e.printStackTrace();
		}
		
		return null;
	}

	
}