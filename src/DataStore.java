import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.Preferences;

public class DataStore implements Serializable{

    private static final String DELETE_PREFERENCE_KEY = "delete_key";
    private static final String READ_PREFERENCE_KEY = "read_key";
    private static final String INCOMING_PREFERENCE_KEY = "incoming_key";
    private static final String OUTGOING_PREFERENCE_KEY = "outgoing_key";
    private static final String SEPARATE_PREFERENCE_KEY = "separate_key";
    private static final String SAVE_DELAY_PREFERENCE_KEY = "save_delay_key";
    private static final String USER_FILE_DIR = System.getProperty("user.dir") + "\\data\\UserData";
    private static final String TYPE_FILE_DIR = System.getProperty("user.dir") + "\\data\\TypeData";
    private static final String FOLDER_FILE_DIR = System.getProperty("user.dir") + "\\data\\FolderData";
    private static final String DISABLED_FOLDER_FILE_DIR = System.getProperty("user.dir") + "\\data\\DisabledFolderData";
    private static final String HD_FILE_DIR = System.getProperty("user.dir") + "\\data\\HardDriveData";

    public static void saveHardDriveInfo(String HDName, String folderPath){
        String[] HDData = {HDName, folderPath};
        try {
            FileOutputStream outputStream = new FileOutputStream(HD_FILE_DIR);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(HDData);
            outputStream.close();
            objectOutputStream.close();
        }catch (Exception e) {e.printStackTrace();}
    }

    public static String[] loadHardDriveInfo() {
        String[] data = null;
        try {
            File userFile = new File(HD_FILE_DIR);
            Boolean filemade = userFile.getParentFile().mkdir();

            FileInputStream fileInputStream = new FileInputStream(userFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            data = (String[])objectInputStream.readObject();

        } catch (Exception e) {e.printStackTrace();}
        if (data != null) {
            return data;
        } else return new String[]{};
    }

    public static ArrayList<User> loadUsers() {
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

    public static void saveUsers(ArrayList<User> users){
        try {
            FileOutputStream outputStream = new FileOutputStream(USER_FILE_DIR);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(users);
            outputStream.close();
            objectOutputStream.close();
        }catch (Exception e) {e.printStackTrace();}
    }

    public static ArrayList<Type> loadTypes() {
        ArrayList<Type> types = new ArrayList<Type>();
        try {
            File typeFile = new File(TYPE_FILE_DIR);
            Boolean fileExists = typeFile.exists();
            Boolean directoryMade = typeFile.getParentFile().mkdir();


            if (!fileExists) {

                Type architectural = new Type("Architectural", new ArrayList<String>(Arrays.asList("General", "Structural", "Mechanical", "Electrical", "Plumbing", "Canopy", "Rendering", "Contractor", "Invoice", "Fee Proposal", "Contracts", "Prototype", "Permit", "Franchise")));
                types.add(architectural);

            } else {
                FileInputStream fileInputStream = new FileInputStream(typeFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

                types = (ArrayList<Type>) objectInputStream.readObject();
            }

        } catch (Exception e) {e.printStackTrace();}
        if (types != null) {
            return types;
        } else return new ArrayList<Type>();
    }

    public static void saveTypes(ArrayList<Type> types){
        try {
            FileOutputStream outputStream = new FileOutputStream(TYPE_FILE_DIR);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(types);
            outputStream.close();
            objectOutputStream.close();
        }catch (Exception e) {e.printStackTrace();}
    }

    public static ArrayList<Folder> loadFolders() {
        ArrayList<Folder> folders = new ArrayList<Folder>();
        try {
            File folderFile = new File(FOLDER_FILE_DIR);
            Boolean directoryMade = folderFile.getParentFile().mkdir();

            FileInputStream fileInputStream = new FileInputStream(folderFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            folders = (ArrayList<Folder>)objectInputStream.readObject();

        } catch (Exception e) {e.printStackTrace();}
        if (folders != null) {
            return folders;
        } else return new ArrayList<Folder>();
    }

    public static void saveFolders(ArrayList<Folder> folders){
        try {
            FileOutputStream outputStream = new FileOutputStream(FOLDER_FILE_DIR);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(folders);
            outputStream.close();
            objectOutputStream.close();
        }catch (Exception e) {e.printStackTrace();}
    }

    public static ArrayList<Folder> loadDisabledFolders() {
        ArrayList<Folder> folders = new ArrayList<Folder>();
        try {
            File folderFile = new File(DISABLED_FOLDER_FILE_DIR);
            Boolean directoryMade = folderFile.getParentFile().mkdir();

            FileInputStream fileInputStream = new FileInputStream(folderFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            folders = (ArrayList<Folder>)objectInputStream.readObject();

        } catch (Exception e) {e.printStackTrace();}
        if (folders != null) {
            return folders;
        } else return new ArrayList<Folder>();
    }

    public static void saveDisabledFolders(ArrayList<Folder> folders){
        try {
            FileOutputStream outputStream = new FileOutputStream(DISABLED_FOLDER_FILE_DIR);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(folders);
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

    public static String getSeparateKey() { return SEPARATE_PREFERENCE_KEY; }

    public static String getSaveDelayKey() {
        return SAVE_DELAY_PREFERENCE_KEY;
    }
}
