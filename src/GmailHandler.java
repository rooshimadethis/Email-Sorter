import ch.astorm.jotlmsg.OutlookMessage;
import ch.astorm.jotlmsg.OutlookMessageAttachment;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.util.Base64;
import com.google.api.client.util.Data;
import com.google.api.client.util.StringUtils;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;

import java.io.ByteArrayInputStream;
import java.io.File;
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
                String mailName = "";

                Message realMessage = gmailService.users().messages().get("me", message.getId()).setFormat("full").execute();
                OutlookMessage outlookMessage = new OutlookMessage();
                outlookMessage.setFrom(getMessageFrom(realMessage));
                outlookMessage.setReplyTo(Collections.singletonList(getMessageTo(realMessage)));
                outlookMessage.setSubject(getMessageSubject(realMessage));
                outlookMessage.setPlainTextBody(getMessageBody(realMessage));
                ArrayList<OutlookMessageAttachment> attachments = getMessageAttachments(realMessage);
                for (OutlookMessageAttachment attachment : attachments) {
                    outlookMessage.addAttachment(attachment);
                }

                String messageTime = getMessageTime(realMessage);
                SimpleDateFormat receivedFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss X");
                SimpleDateFormat desiredFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
                Date date = receivedFormat.parse(messageTime);
                String desiredDate = desiredFormat.format(date);
                mailName += desiredDate + " ";

                mailName += currentFolder.getName() + " ";

                String userEmail = Main.getInstance().getCurrentUser().getEmailAddress();
                Boolean sent = false;
                Boolean received = false;
                String from = outlookMessage.getFrom().replace("<", "").replace(">", "").replace("\"", "");

                if (from.contains(userEmail)){
                    sent = true;
                    String to = outlookMessage.getReplyTo().toString().replace("<", "").replace(">", "").replace("\"", "");
                    mailName += to + " ";
                } else if (outlookMessage.getReplyTo().contains(userEmail)){
                    received = true;
                    mailName += from + " ";
                }

                String snippet = realMessage.getSnippet();
                snippet = snippet.replaceAll("[^a-zA-Z0-9 ]", "");
                snippet = snippet.replace("quot", "");
                mailName += snippet;

                Boolean subfolderFound = false;
                for (Subfolder subfolder : currentFolder.getSubfolders()) {
                    if (outlookMessage.getSubject().contains(subfolder.getName())) {
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
                    //mailName = mailName.replaceAll("[^a-zA-Z0-9_[ ][-]]", "");
                    String savePath = path + "/" + mailName;
                    if (savePath.length() > 249){
                        int pathSize = path.length() + 1;
                        mailName = mailName.substring(0, 249-pathSize);
                        savePath = path + "/" + mailName;
                    }
                    outlookMessage.writeTo(new File(savePath + ".msg"));
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

    private String getMessageFrom(Message message) {
        MessagePart msgpart = message.getPayload();
        List<MessagePartHeader> headers = message.getPayload().getHeaders();
        for(MessagePartHeader header : headers){
            if(header.getName().equals("From")){
                return header.getValue();
            }
        }
        return "";
    }

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

    private String getMessageTime(Message message) {
        MessagePart msgpart = message.getPayload();
        List<MessagePartHeader> headers = message.getPayload().getHeaders();
        for(MessagePartHeader header : headers){
            if(header.getName().equals("Date")){
                return header.getValue();
            }
        }
        return "";
    }

    private String getMessageBody(Message message) {
        String MailBody = "";
        String MailBodyDecoded = "";
//        message = Gmailservice.users().messages().get(USER, "14d4d4bbd160b7a9").setFormat("raw").execute();
        try{
            MessagePart msgpart = message.getPayload();
            List<MessagePart> bodyParts = msgpart.getParts();
            for(MessagePart part : bodyParts){
                MailBody = StringUtils.newStringUtf8(Base64.decodeBase64(part.getBody().getData().getBytes()));
                if(MailBody == null){
                    MailBody = StringUtils.newStringUtf8(Base64.decodeBase64(part.getParts().get(1).getBody().getData().getBytes()));
                }
//                MailBodyDecoded = StringUtils.newStringUtf8(Base64.decodeBase64(MailBody));
//				System.out.println(MailBody);
                break;
            }
            return MailBody;
        }catch(NullPointerException e){
            return null;
        }
    }

    private ArrayList<OutlookMessageAttachment> getMessageAttachments(Message message) {
        ArrayList<OutlookMessageAttachment> attachments = new ArrayList<>();
        try {
            List<MessagePart> messageParts = message.getPayload().getParts();
            for (MessagePart part : messageParts) {
                if (part.getFilename().length() > 0) {
                    String filename = part.getFilename();
                    MessagePartBody attachmentParts = part.getBody();
                    String attId = attachmentParts.getAttachmentId();
                    MessagePartBody attachPart = gmailService.users().messages().attachments().
                            get("me", message.getId(), attId).execute();
                    byte[] fileByteArray = com.google.api.client.util.Base64.decodeBase64(attachPart.getData());
                    attachments.add(new OutlookMessageAttachment(filename, part.getMimeType(), new ByteArrayInputStream(fileByteArray)));
                }
            }
        } catch (Exception e) {e.printStackTrace();}
        return attachments;
    }
}
