import java.io.Serializable;
import java.util.ArrayList;

public class Subfolder implements Serializable{
    private String name;
    //private ArrayList<String> keywords;
    private String path;

    public Subfolder(String name) {
        this.name = name;

    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

}
