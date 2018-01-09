import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.sun.org.apache.xpath.internal.SourceTree;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.unbescape.html.HtmlEscape;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    //creates the query based on preferences to later send request
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

    public void processEmailsForCurrentFolders() {
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
                List<Message> receivedMessages = new ArrayList<>();
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

    private void saveMessages(ArrayList<Message> messages, Folder currentFolder) {

        long totalStart = System.currentTimeMillis();
        int numEmails = 0;
        for (Message message : messages) {
            try {
                String path = "";
                StringBuilder mailName = new StringBuilder();

                Message rawMessage = gmailService.users().messages().get("me", message.getId()).setFormat("raw").execute();

                byte[] emailBytes = rawMessage.decodeRaw();
                Properties props = new Properties();
                Session session = Session.getDefaultInstance(props, null);
                MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));

                String messageTime = getMessageTimeAsString(email);
                mailName.append(messageTime);
                mailName.append(" ");

                mailName.append(currentFolder.getName());
                mailName.append(" ");

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


                String snippet = rawMessage.getSnippet();
                mailName.append(snippet);

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

                    fileName = HtmlEscape.unescapeHtml(fileName);
                    fileName = fileName.replace("\\", "");
                    fileName = fileName.replaceAll("[\"/:*?<>|\n\r\t]", "");
                    fileName = fileName.replaceAll("[^\\x00-\\x7F]", "");

                    String savePath = path + "/" + fileName;

                    if (savePath.length() > 249){
                        int pathSize = path.length() + 1;
                        String mailString = fileName;
                        mailString = mailString.substring(0, 249-pathSize);
                        savePath = path + "/" + mailString;
                    }
                    FileOutputStream fout = new FileOutputStream(new File(savePath + ".eml"));
                    email.writeTo(fout);
                    fout.close();
                    numEmails++;
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
