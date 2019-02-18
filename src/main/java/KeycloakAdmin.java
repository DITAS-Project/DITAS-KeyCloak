import com.fasterxml.jackson.databind.ObjectMapper;
import model.KeyCloakModel;
import model.UserModel;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import static java.lang.Thread.sleep;

public class KeycloakAdmin {

	private static String realmName = "vdc_access";

	private static String clientId = "vdc_client";

	private static String redirectUri = "http://localhost:9300";

	public static void main(String... args) {

		KeyCloakModel model = null;
		ObjectMapper om = new ObjectMapper();
		try {//TODO make relative path
			model = om.readValue(new File(
							"/opt/jboss/ditas/Keycloak.json"),
					KeyCloakModel.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (model == null) {
			System.exit(-1);
		}
		waitForKeycloak(model.getUrl());
		Keycloak kc = Keycloak.getInstance(model.getUrl(), "master", "admin",
				model.getPassword(), "admin-cli");

		RealmRepresentation realm = new RealmRepresentation();
		realm.setDefaultRoles(model.getRoles());
		realm.setRealm(realmName);
		realm.setRegistrationAllowed(model.isRegistrationAllowed());

		//build a test client to optain tokens
		ClientRepresentation client = new ClientRepresentation();
		client.setClientId(clientId);
		client.setRedirectUris(Arrays.asList(redirectUri));

		kc.realms().create(realm);
		for (UserModel user : model.getUsers()) {
			kc.realm(realmName).users().create(parseUser(user));
		}

	}

	private static void waitForKeycloak(String url) {
		int status=0;
		/**try {while(status!=200) {
			URL keycloak = new URL(url);
			HttpURLConnection con =
					(HttpURLConnection) keycloak.openConnection();
			con.setRequestMethod("GET");
			//con.connect();
			status=con.getResponseCode();
			sleep(1000);
		}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		try {
			sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static UserRepresentation parseUser(UserModel model) {
		//build Credentials representation
		CredentialRepresentation cred = new CredentialRepresentation();
		cred.setType(CredentialRepresentation.PASSWORD);
		cred.setValue(model.getPassword());

		//build the User Representation
		UserRepresentation user = new UserRepresentation();
		user.setUsername(model.getUsername());
		user.setEnabled(true);
		user.setCredentials(Arrays.asList(cred));

		return user;
	}

}
