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

public class Dummy_agent1 extends Agent {

    boolean game = false;

    int N = 0;
    int R = 0;
    int S = 0;
    int I = 0;
    int P = 0;
    int id = 0;

    protected void setup() {
        // Printout a welcome message
        System.out.println("Hello! Dummy_agent " + getAID().getName() + " is ready.");
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Player");
        sd.setName("JADE-book-trading");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new MyOneShotBehaviour2());

    }

    public class MyOneShotBehaviour2 extends Behaviour {

        int step = 0;
        int rounds = 1;
        int game = 1;

        public void action() {
            
            
            switch (step) {
                case 0:
                    game = 1;
                    ACLMessage msg1 = receive();
                    if (msg1 != null) {
                        // Process the message
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
                        }
                        step++;
                    } else {
                        block();
                    }
                    break;
                case 1://new game
                    rounds = 1;
                    msg1 = receive();
                    if (msg1 != null) {
                        // Process the message
                       // System.out.println(msg1);
                        step++;
                    } else {
                        block();
                    }
                    break;
                case 2:
                    Random rand = new Random();
                    int pos = rand.nextInt(((S - 1) - 0) + 1) + 0;

                    msg1 = receive();
                    if (msg1 != null) {
                        // Process the message
                       // System.out.println(msg1);
                        //String agent = msg1.getSender().getLocalName();
                        //String message = "Position#1";
                        ACLMessage reply = msg1.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("Position#" + pos);
                        send(reply);
                        //System.out.println("Mandamos respuesta");
                        //addBehaviour(new send_message(agent, message, INFORM));
                        //send_message(agent, message, INFORM);

                        step++;
                    } else {
                        block();
                    }
                    break;
                case 3://resultados
                    msg1 = receive();
                    if (msg1 != null) {
                        // Process the message
                        //System.out.println(msg1);
                        if (rounds < R) {
                            rounds++;
                            step = 2;
                        } else {
                            step++;
                        }
                    } else {
                        block();
                    }
                    break;
                case 4://endgame
                    msg1 = receive();
                    if (msg1 != null) {
                        // Process the message
                        //System.out.println(msg1);
                        if (game < N) {
                            step = 1;
                        } else {
                            step = 0;
                        }
                    } else {
                        block();
                    }
                    break;
            }
        }

        public boolean done() {
            return false;
        }
    }

    /*public void send_message(String agent, String message, int performative) {
        // perform operation X
        System.out.println("mandamos respuesta");
        ACLMessage msg = new ACLMessage(performative);
        msg.addReceiver(new AID(agent, AID.ISLOCALNAME));
        msg.setPerformative(performative);
        msg.setContent(message);
        send(msg);
    }*/

 /*public class send_message extends OneShotBehaviour {

        String agent = null;
        String message = null;
        int performative = 0;

        private send_message(String agent, String message, int performative) {
            this.agent = agent;
            this.message = message;
            this.performative = performative;
        }

        public void action() {
            // perform operation X
            System.out.println("mandamos mensaje");
            ACLMessage msg = new ACLMessage(performative);
            msg.addReceiver(new AID(agent, AID.ISLOCALNAME));
            msg.setPerformative(performative);
            msg.setContent(message);
            send(msg);
        }
    }*/

    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Printout a dismissal message
        System.out.println("Seller-agent " + getAID().getName() + " terminating.");
    }
}
