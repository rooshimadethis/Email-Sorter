import java.util.ArrayList;

public class Folder {
    private String name;
    private Type type;
    private ArrayList<String> keywords;
    private ArrayList<Folder> subfolder;
    private String path;
    private boolean custom;

    public Folder(String name, ArrayList<String> keywords, Type type) {
        this.name = name;
        this.keywords = keywords;
        this.type = type;
        //TODO check for custom
        ArrayList<String> originalTypes = type.getSubcategories();

    }

    public String getName() {
        return name;
    }
}
