import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class Authorizer {

    private static final String APPLICATION_NAME = "EmailSorter";
    private static final String DATA_STORE_DIR = System.getProperty("user.dir") + "\\credentials\\StoredCredential";
    private static final String CLIENT_SECRET_DIR = "/credentials/client_id.json";
    private static FileDataStoreFactory storeFactory;
    private static HttpTransport httpTransport;
    private static JsonFactory jsonFactory;
    private static final List<String> SCOPES = Collections.singletonList(
            "https://www.googleapis.com/auth/gmail.modify");
    private static GoogleClientSecrets clientSecrets;
    private Credential currentUserCredential;


    public Authorizer() {
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            jsonFactory = JacksonFactory.getDefaultInstance();

            storeFactory = new FileDataStoreFactory(new File(DATA_STORE_DIR));

            InputStream in = getClass().getResourceAsStream(CLIENT_SECRET_DIR);
            clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));

        } catch (Exception e){e.printStackTrace();}
    }

    public Credential authorizeUser(String userID){
        Credential credential = null;
        try {
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, jsonFactory, clientSecrets,
                    SCOPES)
                    .setDataStoreFactory(storeFactory)
                    .setAccessType("offline")
                    .build();


            // Authorize
            AuthorizationCodeInstalledApp AuthCodeInstalledApp = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver());
            credential = AuthCodeInstalledApp.authorize(userID);

        } catch (Exception e){e.printStackTrace();}
        currentUserCredential = credential;
        return credential;
    }

    public Gmail getGmailService(Credential credential) {
        return new Gmail.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

}
