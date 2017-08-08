import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class Folder implements Serializable{
    private String name;
    private Type type;
    private ArrayList<String> keywords;
    private ArrayList<Subfolder> subfolders;
    private String path;
    private boolean custom;

    public Folder(String name, Type type, ArrayList<String> keywords, String path) {
        this.name = name;
        this.keywords = keywords;
        this.type = type;
        this.path = path;
        subfolders = new ArrayList<Subfolder>();
        //TODO check for custom

        for (String subfolder : keywords){
            addNewSubfolder(subfolder);
        }

    }

    public void addNewSubfolder(String subfolderName) {
        Subfolder newSubfolder = new Subfolder(subfolderName);
        newSubfolder.setPath(path + "/" + subfolderName);
        File newFile = new File(newSubfolder.getPath());
        newFile.mkdirs();
        subfolders.add(newSubfolder);
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Subfolder> getSubfolders() {
        return subfolders;
    }
}
