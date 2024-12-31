public abstract class Protein {
    protected String name;
    protected String pdbId;

    public Protein(String name, String pdbId) {
        this.name = name;
        this.pdbId = pdbId;
    }

    public abstract void showStructure(); // Example of polymorphism
}
