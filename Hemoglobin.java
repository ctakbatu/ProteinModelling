public class Hemoglobin extends Protein {

    private String pdbId;

    public Hemoglobin() {
        super("Hemoglobin", "1HHO");
        this.pdbId = "1HHO";
    }

        // 3D visualization code will be integrated here
    public void showStructure() {
        System.out.println("Showing 3D structure of Hemoglobin...");
        // 3D görselleştirme kodu buraya entegre edilecek
        HemoglobinViewer.show3DStructure(pdbId);
    }
}
