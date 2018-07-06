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
    private boolean separateInOut;
    private Subfolder miscSubfolder;

    public Folder(String name, Type type, ArrayList<String> keywords, String path) {
        this.name = name;
        this.keywords = keywords;
        this.type = type;
        this.path = path;
        subfolders = new ArrayList<Subfolder>();

        if (type != null) {
            if (subfolders.size() != type.getSubcategories().size()) {
                custom = true;
            }
        }
        separateInOut = Main.getInstance().getPrimaryScreenController().getSeparateInOut();
        if (separateInOut){
            createInOut();
        }
        for (String subfolder : keywords){
            if (!subfolder.equals("")) {
                addNewSubfolder(subfolder);
            }
        }
        miscSubfolder = new Subfolder("Misc", separateInOut);
        miscSubfolder.setRoot(path);
        subfolders.add(miscSubfolder);

    }

    public void addNewSubfolder(String subfolderName) {
        Subfolder newSubfolder = new Subfolder(subfolderName, separateInOut);
        newSubfolder.setRoot(path);

        subfolders.add(newSubfolder);
    }

    /**
     * Each folder has a Received and Sent folder inside of it if the option is chosen
     */
    private void createInOut(){
        File in = new File(path + "/Received");
        File out = new File(path + "/Sent");
        in.mkdirs();
        out.mkdirs();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getTypeName() {
        if (type != null) {
            return type.getName();
        } else {
            return "Custom";
        }
    }

    public ArrayList<Subfolder> getSubfolders() {
        return subfolders;
    }

    public Subfolder getMiscFolder() {
        return miscSubfolder;
    }

    public boolean isSeparateInOut() {
        return separateInOut;
    }
}
