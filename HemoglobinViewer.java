import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;

import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.scene.AmbientLight;
import javafx.scene.PointLight;
import javafx.geometry.Point3D;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class HemoglobinViewer extends Application {

    private Group root;
    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Translate translate = new Translate(0, 0, 0);
    private PerspectiveCamera camera;

    // Van der Waals radii (in angstroms)
    private static final Map<String, Double> vanDerWaalsRadii = new HashMap<>();

    static {
        vanDerWaalsRadii.put("H", 1.20);
        vanDerWaalsRadii.put("C", 1.70);
        vanDerWaalsRadii.put("N", 1.55);
        vanDerWaalsRadii.put("O", 1.52);
        vanDerWaalsRadii.put("S", 1.80);
        vanDerWaalsRadii.put("P", 1.80);
        vanDerWaalsRadii.put("FE", 1.90);
        vanDerWaalsRadii.put("CA", 2.00);
       // other common elements
        
    }


   // CPK Color Mapping
  private static final Map<String, Color> cpkColors = new HashMap<>();
    static {
        cpkColors.put("H", Color.WHITE);
        cpkColors.put("C", Color.GRAY);
        cpkColors.put("N", Color.BLUE);
        cpkColors.put("O", Color.RED);
        cpkColors.put("S", Color.YELLOW);
        cpkColors.put("P", Color.ORANGE);
        cpkColors.put("FE", Color.color(0.8,0.0,0.0)); //Dark Red
        cpkColors.put("CA", Color.color(0.45,0.75,0.45)); // Light green
         
         // Add more elements as needed
    }



    public static void show3DStructure(String pdbId) {
        Application.launch(HemoglobinViewer.class, pdbId);
    }

    @Override
    public void start(Stage stage) {
        root = new Group();
        String pdbId = getParameters().getRaw().get(0);
        String pdbData = PDBFetcher.fetchPDBData(pdbId);

        try (Scanner scanner = new Scanner(pdbData)) {
              Map<Integer, AtomData> atomMap = new HashMap<>();
                while (scanner.hasNextLine()) {
                   String line = scanner.nextLine();
                   if (line.startsWith("ATOM") || line.startsWith("HETATM")) {
                      AtomData atomData = parseAtomLine(line);
                      if (atomData != null){
                           atomMap.put(atomData.serialNumber, atomData);

                        }
                   }
                }
              
              addAtomsAndBonds(atomMap);


           
        }

        Scene scene = new Scene(root, 800, 600, true);
        scene.setFill(Color.BLACK);
           // Camera setup
          camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
         camera.setTranslateZ(-1000);  // Adjust initial camera distance
         camera.setTranslateY(0); //Adjust initial Y
         camera.setTranslateX(0); //Adjust Initial X
      
        scene.setCamera(camera);
         camera.getTransforms().addAll(rotateX, rotateY, translate);
        
         initMouseControl(scene, camera);
        // Lighting Setup
          addLighting();



        stage.setTitle("Hemoglobin 3D Viewer");
        stage.setScene(scene);
        stage.show();
    }
  private void initMouseControl(Scene scene, PerspectiveCamera camera) {
     scene.setOnMousePressed(e -> {
          mousePosX = e.getSceneX();
          mousePosY = e.getSceneY();
          mouseOldX = e.getSceneX();
          mouseOldY = e.getSceneY();
      });


      scene.setOnMouseDragged(e -> {
          mouseOldX = mousePosX;
          mouseOldY = mousePosY;
          mousePosX = e.getSceneX();
          mousePosY = e.getSceneY();

          double mouseDeltaX = mousePosX - mouseOldX;
          double mouseDeltaY = mousePosY - mouseOldY;

          double rotateSpeed = 0.2; // Adjust as needed
           
           rotateX.setAngle(rotateX.getAngle() - mouseDeltaY * rotateSpeed);
           rotateY.setAngle(rotateY.getAngle() + mouseDeltaX * rotateSpeed);
           
            // Apply limits on rotation
            double maxXAngle = 90; // Adjust as needed
            double minXAngle = -90; // Adjust as needed
            
            
           if(rotateX.getAngle() > maxXAngle) rotateX.setAngle(maxXAngle);
            if (rotateX.getAngle() < minXAngle) rotateX.setAngle(minXAngle);
       });
   scene.setOnScroll(e -> {
          double zoomSpeed = 20.0; // Adjust as needed
          double zOffset = e.getDeltaY();
             translate.setZ(translate.getZ() + zOffset * zoomSpeed);

    });

  }
   private void addLighting(){
        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(500);
        pointLight.setTranslateY(500);
        pointLight.setTranslateZ(-1000);


       root.getChildren().addAll(ambientLight, pointLight);
  }
   private static AtomData parseAtomLine(String line) {
          // PDB formatına göre atom verilerini çek
           if(line.length()<54) return null;

           int serialNumber = Integer.parseInt(line.substring(6, 11).trim());

            String atomName = line.substring(12, 16).trim();
           String elementSymbol;

           if (line.length() > 76){
                  elementSymbol = line.substring(76, 78).trim();
           } else {
                 elementSymbol = atomName.replaceAll("[^A-Za-z]", "");  //Extract letters
            }


             double x = Double.parseDouble(line.substring(30, 38).trim());
             double y = Double.parseDouble(line.substring(38, 46).trim());
             double z = Double.parseDouble(line.substring(46, 54).trim());

           return new AtomData(serialNumber, atomName, elementSymbol, x, y, z);
     }



   private void addAtomsAndBonds(Map<Integer, AtomData> atomMap) {


        atomMap.forEach((serialNumber, atomData) -> {
          
            // Adjust radius by element

             double radius = vanDerWaalsRadii.getOrDefault(atomData.elementSymbol, 1.0);

             Sphere atom = new Sphere(radius);
               // Set CPK Color by element
            Color atomColor = cpkColors.getOrDefault(atomData.elementSymbol, Color.GRAY);
            atom.setMaterial(new PhongMaterial(atomColor));



            atom.setTranslateX(atomData.x);
            atom.setTranslateY(atomData.y);
            atom.setTranslateZ(atomData.z);

             root.getChildren().add(atom);
       });


       // Add Bonds
       atomMap.forEach((serialNumber1, atomData1) -> {
          atomMap.forEach((serialNumber2, atomData2) -> {
               if (serialNumber1 < serialNumber2) { // Avoid duplicate bonds
                   double dx = atomData2.x - atomData1.x;
                   double dy = atomData2.y - atomData1.y;
                   double dz = atomData2.z - atomData1.z;

                   double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                  double bondThreshold = 2.0; //Adjust this value to set bond length threshold
                   if (distance > 0.5 && distance < bondThreshold) { // Adjust bond distance criteria as needed
                        createBond(atomData1, atomData2);
                   }


              }
          });

       });


    }

     private void createBond(AtomData atom1, AtomData atom2) {
         double dx = atom2.x - atom1.x;
         double dy = atom2.y - atom1.y;
         double dz = atom2.z - atom1.z;

          double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

         Cylinder bond = new Cylinder(0.2, length); // Adjust bond radius as needed
          bond.setMaterial(new PhongMaterial(Color.LIGHTGRAY));

           bond.setTranslateX((atom1.x + atom2.x) / 2);
           bond.setTranslateY((atom1.y + atom2.y) / 2);
           bond.setTranslateZ((atom1.z + atom2.z) / 2);

          //  Rotate the cylinder to point from atom1 to atom2
         Point3D diff = new Point3D(dx, dy, dz);

          Point3D Y_AXIS = new Point3D(0, 1, 0);

           Point3D axisOfRotation = diff.crossProduct(Y_AXIS).normalize();

           double angle = Math.toDegrees(Math.acos(diff.normalize().dotProduct(Y_AXIS)));

           bond.getTransforms().addAll(new Rotate(angle, axisOfRotation));



          root.getChildren().add(bond);
     }

      private static class AtomData {
        int serialNumber;
        String atomName;
        String elementSymbol;
         double x;
         double y;
         double z;

        public AtomData(int serialNumber, String atomName, String elementSymbol, double x, double y, double z){
            this.serialNumber = serialNumber;
            this.atomName = atomName;
            this.elementSymbol = elementSymbol;
            this.x = x;
            this.y= y;
            this.z= z;
         }

    }

}