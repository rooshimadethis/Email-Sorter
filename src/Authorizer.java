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
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class Authorizer {

    private static final String APPLICATION_NAME = "EmailSorter";
    private static final File DATA_STORE_DIR = new File(System.getProperty("user.dir"), ".store/email_sorter");
    private static final String CLIENT_SECRET_DIR = System.getProperty("user.dir");
    private static FileDataStoreFactory storeFactory;
    private static HttpTransport httpTransport;
    private static JsonFactory jsonFactory;
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/gmail.readonly", "https://www.googleapis.com/auth/gmail.compose", "https://www.googleapis.com/auth/gmail.send", "https://www.googleapis.com/auth/gmail.insert",
            "https://www.googleapis.com/auth/gmail.labels", "https://www.googleapis.com/auth/gmail.modify", "https://www.googleapis.com/auth/gmail.settings.basic",
            "https://www.googleapis.com/auth/gmail.settings.sharing", "https://mail.google.com/");
    private static GoogleClientSecrets clientSecrets;
    private GoogleAuthorizationCodeFlow flow;
    private Gmail gmailService;
    private Credential currentUserCredential;


    public Authorizer() {
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            jsonFactory = JacksonFactory.getDefaultInstance();
            storeFactory = new FileDataStoreFactory(DATA_STORE_DIR);

            //Load Client Secrets
            InputStream in = new FileInputStream(System.getProperty("user.dir") + "/client_id.json");
            clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));

            // Set up authorization code flow


        } catch (Exception e){e.printStackTrace();}
    }

    public Credential authorizeUser(String userID){
        Credential credential = null;
        try {
            flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, jsonFactory, clientSecrets,
                    SCOPES).setDataStoreFactory(storeFactory)
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

    /*public Message getMessage() {

    }*/
}
