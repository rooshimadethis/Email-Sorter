import java.io.Serializable;
import java.util.ArrayList;

public class Type implements Serializable {
    private String name;
    private ArrayList<String> subcategories;

    public Type(String name, ArrayList<String> subcategories) {
        this.name = name;
        this.subcategories = subcategories;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getSubcategories() {
        return subcategories;
    }
}
