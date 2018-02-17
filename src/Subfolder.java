import java.io.File;
import java.io.Serializable;

public class Subfolder implements Serializable{
    private String name;
    //private ArrayList<String> keywords;
    private String root;
    private boolean separate;

    public Subfolder(String name, boolean separate) {
        this.name = name;
        this.separate = separate;
    }

    public void setRoot(String root) {
        this.root = root;
        createFolders();
    }

    private void createFolders() {
        if (separate){
            File in = new File(root + "/Received/" + name);
            File out = new File(root + "/Sent/" + name);
            in.mkdirs();
            out.mkdirs();
        } else {
            File oneFolder = new File(root + "/" + name);
            oneFolder.mkdirs();
        }
    }

    public String getReceivedPath() {
        return (root + "/Received/" + name);
    }
    public String getSentPath() {
        return (root + "/Sent/" + name);
    }
    public String getPath() {
        return (root + "/" + name);
    }

    public String getName() {
        return name;
    }

    public boolean isSeparate() {
        return separate;
    }
}
