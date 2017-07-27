package java;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Authorizer {
    private static final String APPLICATION_NAME = "EmailSorter";
    private static final File DATA_STORE_DIR = new File(System.getProperty("user.dir"), ".store/email_sorter");
    private static FileDataStoreFactory storeFactory;
    private static HttpTransport httpTransport;
    private static JsonFactory jsonFactory;
    private static final List<String> SCOPES = Arrays.asList("https://mail.google.com/", "https://www.googleapis.com/auth/gmail.compose",
            "https://www.googleapis.com/auth/gmail.labels", "https://www.googleapis.com/auth/gmail.insert", "https://www.googleapis.com/auth/gmail.metadata",
            "https://www.googleapis.com/auth/gmail.modify", "https://www.googleapis.com/auth/gmail.readonly", "https://www.googleapis.com/auth/gmail.send");
}
