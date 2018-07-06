import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import org.unbescape.html.HtmlEscape;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;

public class GmailHandler {

    private String baseQuery;
    private Boolean deletePreference;
    private Boolean readPreference;
    private Boolean receivedPreference;
    private Boolean sentPreference;
    private Boolean separatePreference;
    private String agePreference;
    private Gmail gmailService;

    public GmailHandler(){
        createBaseQuery();
    }

    /**
     * This method creates a base query depending on the preferences selected including read/unread, received/sent etc.
     */
    private void createBaseQuery() {
        String query = "";

        Preferences preferences = DataStore.getPreferencesForCurrentUser();
        deletePreference = preferences.getBoolean(DataStore.getDeleteKey(), false);
        readPreference = preferences.getBoolean(DataStore.getReadKey(), true);
        receivedPreference = preferences.getBoolean(DataStore.getIncomingKey(), false);
        sentPreference = preferences.getBoolean(DataStore.getOutgoingKey(), false);
        separatePreference = preferences.getBoolean(DataStore.getSeparateKey(), true);
        agePreference = preferences.get(DataStore.getSaveDelayKey(), "1 Month");

        if (readPreference){
            query += "is:read ";
        }

        String userEmailAddress = Main.getInstance().getCurrentUser().getEmailAddress();
        if (receivedPreference && sentPreference){
        } else if (receivedPreference){
            query += ("to:" + userEmailAddress + " ");
        } else if (sentPreference){
            query += ("from:" + userEmailAddress + " ");
        }

        switch (agePreference) {
            case "Any age":
                break;
            case "1 Week":
                query += "older_than:7d ";
                break;
            case "2 Weeks":
                query += "older_than:14d ";
                break;
            case "1 Month":
                query += "older_than:1m ";
                break;
        }

        baseQuery = query;

    }

    /**
     * This is the main processing method for the email downloading and saving process
     * 1. It get credentials for the User and creates a Gmail object
     * 2. For each of the folders that need processing:
     *  a. It builds the full query for each of the folder in a loop
     *  b. It gets a list of message responses
     *  c. It loops through all of the given messages and sends them to the saveMessages() method
     */
    public void processEmailsForCurrentFolders() {

        //This chunk authorizes and gets Credentials based on the User/Email
        Authorizer authorizer = new Authorizer();
        String userId = Main.getInstance().getCurrentUser().getUserID();
        Credential credential = authorizer.authorizeUser(userId);
        gmailService = authorizer.getGmailService(credential);
        String me = "me";

        ArrayList<Folder> folders = Main.getInstance().getPrimaryScreenController().getFolders();
        for (Folder folder : folders) {
            try {
                String folderQuery = baseQuery + "subject:" + "(" + folder.getName() + ") ";
                System.out.println("Query: " + folderQuery);
                ListMessagesResponse messagesResponse = gmailService.users().messages().list(me).setQ(folderQuery).setMaxResults((long)10000).execute();
                List<Message> receivedMessages = new ArrayList<Message>();
                while (messagesResponse.getMessages() != null) {
                    receivedMessages.addAll(messagesResponse.getMessages());
                    saveMessages(new ArrayList<Message>(receivedMessages), folder);
                    if (messagesResponse.getNextPageToken() != null) {
                        String pageToken = messagesResponse.getNextPageToken();
                        messagesResponse = gmailService.users().messages().list(me).setQ(folderQuery).setPageToken(pageToken).setMaxResults((long)10000).execute();
                    } else {
                        break;
                    }
                }

            } catch (Exception e) {e.printStackTrace();}
        }
    }

    /**
     * This method takes an Arraylist of Gmail messages and downloads them into real files
     * @param messages
     * @param currentFolder
     */
    private void saveMessages(ArrayList<Message> messages, Folder currentFolder) {
        //ArrayList<String> emailsToDelete = new ArrayList<>();
        long totalStart = System.currentTimeMillis();
        int numEmails = 0;
        for (Message message : messages) {
            try {
                String path = "";
                StringBuilder mailName = new StringBuilder();

                //The message is saved by taking the raw Email from Gmail and saving the bytes into an .eml format
                Message rawMessage = gmailService.users().messages().get("me", message.getId()).setFormat("raw").execute();
                byte[] emailBytes = rawMessage.decodeRaw();
                Properties props = new Properties();
                Session session = Session.getDefaultInstance(props, null);
                MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));

                //The time is the first thing in the file name for best sorting
                String messageTime = getMessageTimeAsString(email);
                mailName.append(messageTime);
                mailName.append(" ");

                //The name of the folder is then added for general knowledge
                mailName.append(currentFolder.getName());
                mailName.append(" ");

                //Either the recipient is saved if it is a sent email or the sender if it is received
                String userEmail = Main.getInstance().getCurrentUser().getEmailAddress();
                Boolean sent = false;
                String from = getMessageFrom(email).replace("<", "").replace(">", "").replace("\"", "");

                if (from.contains(userEmail)){

                    sent = true;
                    String[] toArr = getMessageTo(email);
                    String first = toArr[0];
                    if (first.contains("<")) {
                        int index1 = first.indexOf("<")+1;
                        int index2 = first.indexOf(">");
                        mailName.append(first.substring(index1, index2));
                    } else {
                        mailName.append(first);
                    }
                    if (toArr.length > 1){
                        mailName.append(" (and more)");
                    }

                } else {
                    mailName.append(from);
                }
                mailName.append(" ");

                //A Gmail snippet of the email is added to the end of the file name so opening the email is not required
                //  in some cases when trying to find the right email
                String snippet = rawMessage.getSnippet();
                mailName.append(snippet);

                //This chunk looks for a subfolder of the Type so that it can be saved in the right area
                Boolean subfolderFound = false;
                for (Subfolder subfolder : currentFolder.getSubfolders()) {
                    if (getMessageSubject(email).contains(subfolder.getName())) {
                        if (subfolder.isSeparate()) {
                            if (sent) {
                                path = subfolder.getSentPath();
                            } else {
                                path = subfolder.getReceivedPath();
                            }
                            subfolderFound = true;
                            break;
                        } else {
                            path = subfolder.getPath();
                            subfolderFound = true;
                            break;
                        }
                    }
                }

                //This chunk will either put it in the received or sent folder depending on if the option is checked
                if (!subfolderFound){
                    if (currentFolder.isSeparateInOut()){
                        if (sent) {
                            path = currentFolder.getMiscFolder().getSentPath();
                        } else {
                            path = currentFolder.getMiscFolder().getReceivedPath();
                        }
                    } else {
                        path = currentFolder.getMiscFolder().getPath();
                    }
                }
                if (!path.equals("")) {
                    String fileName = mailName.toString();

                    //The email file name is cleaned up for reading/Windows purposes
                    fileName = HtmlEscape.unescapeHtml(fileName);
                    fileName = fileName.replace("\\", "");
                    fileName = fileName.replaceAll("[\"/:*?<>|\n\r\t]", "");
                    fileName = fileName.replaceAll("[^\\x00-\\x7F]", "");

                    String savePath = path + "/" + fileName;

                    //The name is trimmed for windows length purposes
                    if (savePath.length() > 249){
                        int pathSize = path.length() + 1;
                        String mailString = fileName;
                        mailString = mailString.substring(0, 249-pathSize);
                        savePath = path + "/" + mailString;
                    }

                    //The email is finally written to the Hard disk
                    FileOutputStream fout = new FileOutputStream(new File(savePath + ".eml"));
                    email.writeTo(fout);
                    fout.close();
                    numEmails++;

                    //If the email is to be deleted in Gmail, it is
                    if (deletePreference) {
                        try {
                            gmailService.users().messages().trash("me", message.getId()).execute();
                        } catch (Exception e) {e.printStackTrace();}
                    }
                }

            } catch (Exception e) {e.printStackTrace();}
        }
        long totalEnd = System.currentTimeMillis();
        System.out.println(((totalEnd-totalStart)/1000.0) + " seconds for " + numEmails + " emails.");


    }

    private String getMessageSubject(MimeMessage message) {
        String subject = "";
        try{
            subject = message.getSubject();
        } catch (Exception e) {e.printStackTrace();}
        return subject;
    }

    //Parses given message to return plain text sender name/address
    private String getMessageFrom(MimeMessage message) {
        String from = "";
        try{
            from = message.getHeader("From")[0];
        } catch (Exception e) {e.printStackTrace();}
        return from;
    }

    //Parses given message to return plain text recipient name/address
    private String[] getMessageTo(MimeMessage message) {
        String[] deliveredTo = {};
        try {
            deliveredTo = message.getHeader("To");

        } catch (Exception e) {e.printStackTrace();}
        return deliveredTo;
    }

    //Parses given message to return plain text date
    private String getMessageTimeAsString(MimeMessage message) {
        String time = "UnknownTime";
        try{

            String dateHeader = message.getHeader("Date")[0];

            SimpleDateFormat origFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
            Date date = origFormat.parse(dateHeader);

            SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
            time = newFormat.format(date);

        } catch (Exception e) {
            try {
                String dateHeader = message.getHeader("Date")[0];

                SimpleDateFormat origFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
                Date date = origFormat.parse(dateHeader);

                SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
                time = newFormat.format(date);
            } catch (Exception e2) {
                try {
                    String dateHeader = message.getHeader("Date")[0];

                    SimpleDateFormat origFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                    Date date = origFormat.parse(dateHeader);

                    SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
                    time = newFormat.format(date);
                } catch (Exception e3) {
                    e.printStackTrace();
                }
            }
        }
        return time;
    }


}
