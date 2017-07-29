import java.io.*;
import java.util.ArrayList;

public class DataStore implements Serializable{

    public DataStore() {

    }
    public ArrayList<User> loadUsers(String USER_FILE_DIR) {
        ArrayList<User> users = null;
        try {
            File userFile = new File(USER_FILE_DIR);
            Boolean filemade = userFile.getParentFile().mkdir();

            FileInputStream fileInputStream = new FileInputStream(userFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            users = (ArrayList<User>)objectInputStream.readObject();

        } catch (Exception e) {e.printStackTrace();}
        return users;
    }

    public void saveUsers(String USER_FILE_DIR, ArrayList<User> users){
        try {
            FileOutputStream outputStream = new FileOutputStream(USER_FILE_DIR);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(users);
            outputStream.close();
            objectOutputStream.close();
        }catch (Exception e) {e.printStackTrace();}
    }
}
