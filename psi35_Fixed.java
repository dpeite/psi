/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author peite
 */
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import static jade.lang.acl.ACLMessage.INFORM;
import java.util.Random;

public class psi35_Fixed extends Agent {

    boolean game = false;

    int N = 0;
    int R = 0;
    int S = 0;
    int I = 0;
    int P = 0;
    int id = 0;

    protected void setup() {
        System.out.println("Hello! Dummy_agent " + getAID().getName() + " is ready.");
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

        public void action() {
            ACLMessage msg1 = receive();
            if (msg1 != null) {
                //System.out.println(msg1);
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
                    step = 1;
                } else if (message[0].equals("Position")) {
                    step = 2;
                } else if (message[0].equals("Results")) {
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
                    break;
                case 1://new game
                    rounds = 1;
                    break;
                case 2:
                    Random rand = new Random();
                    int pos = S-1;

                    if (msg1 != null) {
                        ACLMessage reply = msg1.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("Position#" + pos);
                        send(reply);
                    }
                    break;
                case 3://resultados

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
