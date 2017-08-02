import java.io.*;
import java.util.ArrayList;
import java.util.prefs.Preferences;

public class DataStore implements Serializable{

    private static final String DELETE_PREFERENCE_KEY = "delete_key";
    private static final String READ_PREFERENCE_KEY = "read_key";
    private static final String INCOMING_PREFERENCE_KEY = "incoming_key";
    private static final String OUTGOING_PREFERENCE_KEY = "outgoing_key";
    private static final String SEPARATE_PREFERENCE_KEY = "separate_key";
    private static final String SAVE_DELAY_PREFERENCE_KEY = "save_delay_key";

    public DataStore() {

    }
    public static ArrayList<User> loadUsers(String USER_FILE_DIR) {
        ArrayList<User> users = new ArrayList<User>();
        try {
            File userFile = new File(USER_FILE_DIR);
            Boolean filemade = userFile.getParentFile().mkdir();

            FileInputStream fileInputStream = new FileInputStream(userFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            users = (ArrayList<User>)objectInputStream.readObject();

        } catch (Exception e) {e.printStackTrace();}
        if (users != null) {
            return users;
        } else return new ArrayList<User>();
    }

    public static void saveUsers(String USER_FILE_DIR, ArrayList<User> users){
        try {
            FileOutputStream outputStream = new FileOutputStream(USER_FILE_DIR);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(users);
            outputStream.close();
            objectOutputStream.close();
        }catch (Exception e) {e.printStackTrace();}
    }

    public static void createNewPreferences(String userID){
        Preferences preferences = Preferences.userRoot().node(userID);
    }

    public static Preferences getPreferencesforCurrentUser() {
        return Preferences.userRoot().node(Main.getInstance().getCurrentUser().getUserID());
    }

    public static String getDeleteKey() {
        return DELETE_PREFERENCE_KEY;
    }

    public static String getReadKey() {
        return READ_PREFERENCE_KEY;
    }

    public static String getIncomingKey() {
        return INCOMING_PREFERENCE_KEY;
    }

    public static String getOutgoingKey() {
        return OUTGOING_PREFERENCE_KEY;
    }

    public static String getSeparateKey() {
        return SEPARATE_PREFERENCE_KEY;
    }

    public static String getSaveDelayKey() {
        return SAVE_DELAY_PREFERENCE_KEY;
    }
}
