
public class Main {
    public static void main(String[] args) {
        Protein hemoglobin = new Hemoglobin();
        hemoglobin.showStructure(); // Polymorphism is demonstrated here by calling the showStructure method on a Protein reference that points to a Hemoglobin object
    }
}
