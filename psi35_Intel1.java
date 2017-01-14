/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author peite
 */
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

public class psi35_Intel1 extends Agent {

    boolean game = false;
    ArrayList matrix;

    int N = 0;
    int R = 0;
    int S = 0;
    int I = 0;
    int P = 0;
    int id = 0;

    protected void setup() {
        System.out.println("Hello! Intel1_agent " + getAID().getName() + " is ready.");
        DFAgentDescription dfd = new DFAgentDescription();//Registramos al agente en el DF
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Player");
        sd.setName(getAID().getName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new main_loop());

    }

    public class main_loop extends Behaviour {

        int step = 0;
        int rounds = 1;
        int game = 1;
        boolean playerA = false;

        String[] ids;
        String[] payoffs;

        public void action() {
            ACLMessage msg1 = receive();
            if (msg1 != null) {
                System.out.println(msg1);
                String[] message = msg1.getContent().split("#");
                if (message[0].equals("Id")) {
                    id = Integer.parseInt(message[1]);
                    String[] params = message[2].split(",");
                    N = Integer.parseInt(params[0]);
                    S = Integer.parseInt(params[1]);
                    R = Integer.parseInt(params[2]);
                    I = Integer.parseInt(params[3]);
                    P = Integer.parseInt(params[4]);
                    step = 0;
                } else if (message[0].equals("NewGame")) {
                    ids = message[1].split(",");
                    step = 1;
                } else if (message[0].equals("Position")) {
                    step = 2;
                } else if (message[0].equals("Results")) {
                    ids = message[1].split(",");
                    payoffs = message[2].split(",");
                    step = 3;
                } else if (message[0].equals("EndGame")) {
                    step = 4;
                } else if (message[0].equals("Changed")) {
                    step = 5;
                }
            } else {
                block();
            }
            switch (step) {
                case 0:
                    game = 1;
                    playerA = false;
                    best_payoff = 0;
                    matrix = generate_matrix(S);
                    break;
                case 1://new game
                    for (int i = 0; i < 1; i++) {
                        if (Integer.parseInt(ids[i]) > id) {
                            playerA = true;
                        }
                    }
                    rounds = 1;
                    break;
                case 2:
                    //Random rand = new Random();
                    //int pos = S - 2;
                    //////Llamar a bestfilas, coger el primer valor del array y si escoger fila o columna enfuncion del rol
                    //System.out.println(((ArrayList) best_fila().get(0)).get(0));
                    int payoff;
                    if (playerA == true) {
                        payoff = 0;
                    } else {
                        payoff = 1;
                    }
                    int pos = (int) ((ArrayList) best_fila().iterator().next()).get(payoff);
                    //System.out.println(ids[0] + " " + ids[1]);
                    System.out.println(id);
                    System.out.println(playerA);

                    //best_fila();
                    if (msg1 != null) {
                        ACLMessage reply = msg1.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("Position#" + pos);
                        send(reply);
                    }
                    break;
                case 3://resultados
                    update_matrix(S, Integer.parseInt(ids[0]), Integer.parseInt(ids[1]), Integer.parseInt(payoffs[0]), Integer.parseInt(payoffs[1]));
                    print_matrix();

                    if (rounds < R) {
                        rounds++;
                        step = 2;
                    } else {
                        step++;
                    }

                    break;
                case 4://endgame

                    if (game < N) {
                        step = 1;
                    } else {
                        step = 0;
                    }

                    break;
                case 5://changed
                    break;
            }
        }

        public boolean done() {
            return false;
        }

        int best_payoff = 0;
        //ArrayList bestfilas = new ArrayList();
        HashSet bestfilas = new HashSet();

        public HashSet best_fila() {
            for (int i = 0; i < S; i++) {
                for (int j = 0; j < S; j++) {
                    ArrayList a = (ArrayList) ((ArrayList) matrix.get(i)).get(j);
                    int payoff;
                    if (playerA == true) {
                        payoff = 0;
                    } else {
                        payoff = 1;
                    }
                    ArrayList<Integer> best = new ArrayList<Integer>();

                    if (((Integer) a.get(payoff)) >= best_payoff) {
                        //System.out.println(("Actual payoff: "+(Integer) a.get(payoff)));
                        //System.out.println("Best Payoff: "+best_payoff);
                        if (((Integer) a.get(payoff)) > best_payoff) {
                            bestfilas.clear();
                            best_payoff = (Integer) a.get(payoff);
                        }
                        best.add(i);
                        best.add(j);
                        bestfilas.add(best);

                    }
                }
            }

            System.out.println(bestfilas);
            return bestfilas;
        }

        public ArrayList update_matrix(int S, int nfila, int ncolumna, int payoff1, int payoff2) {
            ArrayList asd = (ArrayList) ((ArrayList) matrix.get(nfila)).get(ncolumna);
            asd.set(0, payoff1);
            asd.set(1, payoff2);

            ArrayList asd1 = (ArrayList) ((ArrayList) matrix.get(ncolumna)).get(nfila);
            asd1.set(0, payoff2);
            asd1.set(1, payoff1);

            return matrix;
        }

        public void print_matrix() {
            System.out.println("////////////");
            for (int i = 0; i < matrix.size(); i++) {
                System.out.println(matrix.get(i));
            }
        }

        public ArrayList generate_matrix(int S) {
            ArrayList matrix = new ArrayList();
            for (int i = 0; i < S; i++) {//Generamos fila a fila la matriz
                ArrayList fila = new ArrayList();
                for (int j = 0; j < S; j++) {//Para cada fila generamos cada elemento de la matriz
                    ArrayList payoffs = new ArrayList();
                    payoffs.add(0);
                    payoffs.add(0);
                    fila.add(payoffs);
                }
                ///System.out.println(fila);
                matrix.add(fila);

            }
            return matrix;
        }

    }

    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Printout a dismissal message
        System.out.println("Player " + getAID().getName() + " terminating.");
    }
}
