import java.io.Serializable;
import java.util.ArrayList;

public class Type implements Serializable {
    private String name;
    private ArrayList<String> subcategories;

    public ArrayList<String> getSubcategories() {
        return subcategories;
    }
}
