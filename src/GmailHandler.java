import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;

import org.unbescape.html.HtmlEscape;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
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

        Preferences preferences = DataStore.getPreferencesforCurrentUser();
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
                ListMessagesResponse messagesResponse = gmailService.users().messages().list(me).setQ(folderQuery).execute();
                List<Message> receivedMessages = messagesResponse.getMessages();
                saveMessages(new ArrayList<Message>(receivedMessages), folder);
                //folder.setFolderSaveDate();



            } catch (Exception e) {e.printStackTrace();}
        }
    }

    private void saveMessages(ArrayList<Message> messages, Folder currentFolder) {
        try {
            for (Message message : messages) {
                String path = "";
                StringBuilder mailName = new StringBuilder();

                Message realMessage = gmailService.users().messages().get("me", message.getId()).setFormat("full").execute();
                Message rawMessage = gmailService.users().messages().get("me", message.getId()).setFormat("raw").execute();

                byte[] emailBytes = rawMessage.decodeRaw();

                Properties props = new Properties();
                Session session = Session.getDefaultInstance(props, null);
                MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));

                String messageTime = getMessageTime(realMessage);
                SimpleDateFormat receivedFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss X");
                SimpleDateFormat desiredFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
                Date date = receivedFormat.parse(messageTime);
                String desiredDate = desiredFormat.format(date);
                mailName.append(desiredDate);
                mailName.append(" ");

                mailName.append(currentFolder.getName());
                mailName.append(" ");

                String userEmail = Main.getInstance().getCurrentUser().getEmailAddress();
                Boolean sent = false;
                String from = getMessageFrom(realMessage).replace("<", "").replace(">", "").replace("\"", "");

                if (from.contains(userEmail)){
                    sent = true;
                    String to = Collections.singletonList(getMessageTo(realMessage)).toString().replace("<", "").replace(">", "").replace("\"", "");
                    mailName.append(to);
                    mailName.append(" ");
                } else {
                    //TODO fix
                    mailName.append(from);
                    mailName.append(" ");
                }

                String snippet = realMessage.getSnippet();
                snippet = HtmlEscape.unescapeHtml(snippet);
                snippet = snippet.replace("\\", "");
                snippet = snippet.replace("/", "");
                snippet = snippet.replace(":", "");
                snippet = snippet.replace("*", "");
                snippet = snippet.replace("?", "");
                snippet = snippet.replace("\"", "");
                snippet = snippet.replace("<", "");
                snippet = snippet.replace(">", "");
                snippet = snippet.replace("|", "");
                mailName.append(snippet);

                Boolean subfolderFound = false;
                for (Subfolder subfolder : currentFolder.getSubfolders()) {
                    if (getMessageSubject(realMessage).contains(subfolder.getName())) {
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
                    String savePath = path + "/" + mailName.toString();
                    if (savePath.length() > 249){
                        int pathSize = path.length() + 1;
                        String mailString = mailName.toString();
                        mailString = mailString.substring(0, 249-pathSize);
                        savePath = path + "/" + mailString;
                    }
                    email.writeTo(new FileOutputStream(new File(savePath + ".eml")));
                }
            }

        } catch (Exception e) {e.printStackTrace();}
    }

    private String getMessageSubject(Message message) {
//		System.out.println(message.toPrettyString());
        MessagePart msgpart = message.getPayload();

        List<MessagePartHeader> headers = message.getPayload().getHeaders();
        for(MessagePartHeader header : headers){
            if(header.getName().equals("Subject")){
                return header.getValue();
            }
        }
        return "";
    }

    //Parses given message to return plain text sender name/address
    private String getMessageFrom(Message message) {
        List<MessagePartHeader> headers = message.getPayload().getHeaders();
        for(MessagePartHeader header : headers){
            if(header.getName().equals("From")){
                return header.getValue();
            }
        }
        return "";
    }

    //Parses given message to return plain text recipient name/address
    private String getMessageTo(Message message) {
        MessagePart msgpart = message.getPayload();
        List<MessagePartHeader> headers = message.getPayload().getHeaders();
        for(MessagePartHeader header : headers){
            if(header.getName().equals("To")){
                return header.getValue();
            }
        }
        return "";
    }

    //Parses given message to return plain text date
    private String getMessageTime(Message message) {
        List<MessagePartHeader> headers = message.getPayload().getHeaders();
        for(MessagePartHeader header : headers){
            if(header.getName().equals("Date")){
                return header.getValue();
            }
        }
        return "";
    }

}
