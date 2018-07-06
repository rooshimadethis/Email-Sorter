import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.Preferences;

public class DataStore implements Serializable{

    /**
     * The data of this class is stored locally as a few extension-less files to be easily removable/saveable.
     *  and also easier to debug with
     */

    private static final String BASE_DIR = System.getProperty("user.dir") + "\\data";
    private static final String DELETE_PREFERENCE_KEY = "delete_key";
    private static final String READ_PREFERENCE_KEY = "read_key";
    private static final String RECEIVED_PREFERENCE_KEY = "received_key";
    private static final String SENT_PREFERENCE_KEY = "sent_key";
    private static final String SEPARATE_PREFERENCE_KEY = "separate_key";
    private static final String SAVE_DELAY_PREFERENCE_KEY = "save_delay_key";
    private static final String USER_FILE_DIR = "\\UserData";
    private static final String TYPE_FILE_DIR = "TypeData";
    private static final String FOLDER_FILE_DIR = "FolderData";
    private static final String DISABLED_FOLDER_FILE_DIR = "DisabledFolderData";
    private static final String HD_FILE_DIR = "HardDriveData";

    /**
     * This method saves the data about the hard drive as saveable data to be reloaded on start
     * @param HDName The name of the hard drive that is in use for the emails
     * @param folderPath the folder path in the drive where the emails are stored
     */
    public static void saveHardDriveInfo(String HDName, String folderPath){
        String[] HDData = {HDName, folderPath};
        try {
            FileOutputStream outputStream = new FileOutputStream(BASE_DIR + "\\" + HD_FILE_DIR);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(HDData);
            outputStream.close();
            objectOutputStream.close();
        }catch (Exception e) {e.printStackTrace();}
    }

    /**
     * The hard drive info is loaded from the saved file
     * @return a string array that contains the HDName as [0] and folderPath as [1]
     * Could have used a special serializable class like HDData to store the info
     */
    public static String[] loadHardDriveInfo() {
        String[] data = null;
        try {
            File userFile = new File(BASE_DIR + "\\" + HD_FILE_DIR);
            Boolean filemade = userFile.getParentFile().mkdir();

            FileInputStream fileInputStream = new FileInputStream(userFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            data = (String[])objectInputStream.readObject();

        } catch (Exception e) {e.printStackTrace();}
        if (data != null) {
            return data;
        } else return new String[]{};
    }

    /**
     * This method loads the users that have emails saved through the program
     * @return a list of the Users that have been entered in the program
     */
    public static ArrayList<User> loadUsers() {
        ArrayList<User> users = new ArrayList<User>();
        try {
            File userFile = new File(BASE_DIR + "\\" + USER_FILE_DIR);
            Boolean filemade = userFile.getParentFile().mkdir();

            FileInputStream fileInputStream = new FileInputStream(userFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            users = (ArrayList<User>)objectInputStream.readObject();

        } catch (FileNotFoundException ignored) {}
          catch (Exception e){e.printStackTrace();}
        if (users != null) {
            return users;
        } else return new ArrayList<User>();
    }

    /**
     * This method takes an Arraylist of Users and outputs them to a saved file
     * @param users an ArrayList of Users entered in the program
     */
    public static void saveUsers(ArrayList<User> users){
        try {
            FileOutputStream outputStream = new FileOutputStream(BASE_DIR + "\\" + USER_FILE_DIR);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(users);
            outputStream.close();
            objectOutputStream.close();
        }catch (Exception e) {e.printStackTrace();}
    }

    /**
     * This method loads the list of available Types that can be used to created sorted directories
     * @param currentUser the data is stored with the user
     * @return
     */
    public static ArrayList<Type> loadTypes(User currentUser) {
        ArrayList<Type> types = new ArrayList<Type>();
        try {
            File typeFile = new File(BASE_DIR + currentUser.getUserPath() + "\\" + TYPE_FILE_DIR);
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

        } catch (FileNotFoundException ignored) {}
        catch (Exception e){e.printStackTrace();}
        if (types != null) {
            return types;
        } else return new ArrayList<Type>();
    }

    /**
     * This method saves all of the available types given with the User
     * @param types the types that are available
     * @param currentUser the User that uses those types
     */
    public static void saveTypes(ArrayList<Type> types, User currentUser){
        try {
            FileOutputStream outputStream = new FileOutputStream(BASE_DIR + currentUser.getUserPath() + "\\" + TYPE_FILE_DIR);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(types);
            outputStream.close();
            objectOutputStream.close();
        }catch (Exception e) {e.printStackTrace();}
    }

    /**
     * This method loads all of the folders created for the User
     * @param currentUser the User whose folders you're saving
     * @return the folders that the User creates and uses
     */
    public static ArrayList<Folder> loadFolders(User currentUser) {
        ArrayList<Folder> folders = new ArrayList<Folder>();
        try {
            File folderFile = new File(BASE_DIR + currentUser.getUserPath() + "\\" + FOLDER_FILE_DIR);
            Boolean directoryMade = folderFile.getParentFile().mkdir();

            FileInputStream fileInputStream = new FileInputStream(folderFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            folders = (ArrayList<Folder>)objectInputStream.readObject();

        } catch (FileNotFoundException ignored) {}
        catch (Exception e){e.printStackTrace();}
        if (folders != null) {
            return folders;
        } else return new ArrayList<Folder>();
    }

    /**
     * This method saves all of the folders that were created and uses by a specific User
     * @param folders the folders that the User has
     * @param currentUser the User that the folders get saved with
     */
    public static void saveFolders(ArrayList<Folder> folders, User currentUser){
        try {
            FileOutputStream outputStream = new FileOutputStream(BASE_DIR + currentUser.getUserPath() + "\\" + FOLDER_FILE_DIR);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(folders);
            outputStream.close();
            objectOutputStream.close();
        }catch (Exception e) {e.printStackTrace();}
    }

    /**
     * This loads the list of disabled folders of a User so they they can be reloaded if they are needed again
     * @param currentUser the User that has disabled folders that need loading
     * @return
     */
    public static ArrayList<Folder> loadDisabledFolders(User currentUser) {
        ArrayList<Folder> folders = new ArrayList<Folder>();
        try {
            File folderFile = new File(BASE_DIR + currentUser.getUserPath() + "\\" + DISABLED_FOLDER_FILE_DIR);
            Boolean directoryMade = folderFile.getParentFile().mkdir();

            FileInputStream fileInputStream = new FileInputStream(folderFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            folders = (ArrayList<Folder>)objectInputStream.readObject();

        } catch (FileNotFoundException ignored) {}
        catch (Exception e){e.printStackTrace();}
        if (folders != null) {
            return folders;
        } else return new ArrayList<Folder>();
    }

    /**
     * This method saves the disabled folders of the specified user
     * @param folders the list of disabled folders that need saving
     * @param currentUser the User whose disabled folders will be saved with their other info
     */
    public static void saveDisabledFolders(ArrayList<Folder> folders, User currentUser){
        try {
            FileOutputStream outputStream = new FileOutputStream(BASE_DIR + currentUser.getUserPath() + "\\" + DISABLED_FOLDER_FILE_DIR);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(folders);
            outputStream.close();
            objectOutputStream.close();
        }catch (Exception e) {e.printStackTrace();}
    }

    public static void createNewPreferences(String userShortAddress){
        Preferences preferences = Preferences.userRoot().node(userShortAddress);
    }

    public static Preferences getPreferencesForCurrentUser() {
        return Preferences.userRoot().node(Main.getInstance().getCurrentUser().getShortAddress());
    }

    public static String getDeleteKey() {
        return DELETE_PREFERENCE_KEY;
    }

    public static String getReadKey() {
        return READ_PREFERENCE_KEY;
    }

    public static String getIncomingKey() {
        return RECEIVED_PREFERENCE_KEY;
    }

    public static String getOutgoingKey() {
        return SENT_PREFERENCE_KEY;
    }

    public static String getSeparateKey() { return SEPARATE_PREFERENCE_KEY; }

    public static String getSaveDelayKey() {
        return SAVE_DELAY_PREFERENCE_KEY;
    }
}
