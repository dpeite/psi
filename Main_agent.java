
import jade.core.Agent;

public class Main_agent extends Agent {

    protected void setup() {
        System.out.println("Hello! Main Agent " + getAID().getName() + " is ready.");
        System.out.println("Lanzando GUI...");
        psi35_GUI GUI = new psi35_GUI();
        GUI.init();
        System.out.println("Siiiiiii");
    }
}
