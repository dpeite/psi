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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;

public class psi35_Intel1old extends Agent {

    boolean game = false;
    ArrayList<ArrayList> matrix;

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
                    break;
                case 1://new game
                    System.out.println("Juego: " + game);
                    matrix = generate_matrix(S);
                    //print_matrix();
                    for (int i = 0; i <= 1; i++) {
                        if (Integer.parseInt(ids[i]) > id) {
                            playerA = true;
                        }
                    }
                    rounds = 1;
                    break;
                case 2:
                    //Random rand = new Random();
                    //int pos = S - 2;
                    System.out.println("rounds" + rounds);
                    int payoff;
                    if (playerA == true) {
                        payoff = 0;
                    } else {
                        payoff = 1;
                    }
                    best_fila(payoff);
                    //int pos = (int) ((ArrayList) bestfilas.iterator().next()).get(payoff);
                    System.out.println(bestfilas);
                    System.out.println(ordered2);
                    //System.out.println(pos);
                    System.out.println(filcol);
                    System.out.println(filcolcon);
                    //System.out.println(filas);
                    // System.out.println(columnas);

                    int pos = select_option();
                    step = 15;

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
                        //step = 2;
                    } else {
                        //step++;
                    }
                    step = 15;
                    break;
                case 4://endgame

                    if (game < N) {
                        step = 85;
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
        ArrayList ordered2;
        HashSet bestfilas = new HashSet();
        //ArrayList filas = new ArrayList();
        // ArrayList columnas = new ArrayList();
        ArrayList filcol = new ArrayList();
        ArrayList filcolcon = new ArrayList();

        public void best_fila(int payoff) {
            HashSet ordered = new HashSet();
            best_payoff = 0;
            //int payoff;
            //filas.clear();
            //columnas.clear();
            filcol.clear();
            filcolcon.clear();
            /*if (playerA == true) {
                payoff = 0;
            } else {
                payoff = 1;
            }*/
            for (int i = 0; i < S; i++) {
                //ArrayList fila = new ArrayList();
                //ArrayList columna = new ArrayList();
                ArrayList filcol2 = new ArrayList();
                ArrayList filcolcon2 = new ArrayList();
                for (int j = 0; j < S; j++) {
                    ArrayList a = (ArrayList) matrix.get(i).get(j);
                    ArrayList b = (ArrayList) matrix.get(j).get(i);
                    //fila.add(a.get(payoff));
                    // columna.add(b.get(payoff));
                    if (payoff == 0) {
                        filcol2.add(a.get(payoff));
                        filcolcon2.add(b.get(payoff));
                    } else {
                        filcol2.add(b.get(payoff));
                        filcolcon2.add(a.get(payoff));
                    }

                    ArrayList<Integer> best = new ArrayList<>();
                    best.add(i);
                    best.add(j);
                    best.add((Integer) a.get(payoff));
                    if (((Integer) a.get(payoff)) >= best_payoff) {
                        //System.out.println(("Actual payoff: "+(Integer) a.get(payoff)));
                        //System.out.println("Best Payoff: "+best_payoff);
                        if (((Integer) a.get(payoff)) > best_payoff) {
                            bestfilas.clear();
                            best_payoff = (Integer) a.get(payoff);
                        }
                        bestfilas.add(best);
                    }
                    ordered.add(best);
                }
                //filas.add(fila);
                //columnas.add(columna);
                filcol.add(filcol2);
                filcolcon.add(filcolcon2);
            }
            ordered2 = new ArrayList(ordered);
            Collections.sort(ordered2, new Comparator<ArrayList<Integer>>() {
                @Override
                public int compare(ArrayList<Integer> o1, ArrayList<Integer> o2) {
                    return (Integer) (o2.get(2).compareTo(o1.get(2)));
                }

            });
            ordered.clear();
        }

        public int select_option() {
            ArrayList medias_mias = new ArrayList<>();
            ArrayList medias_contricante = new ArrayList<>();

            int result = 0;
            double media_mia = 0;
            double media_contrincante = 0;
            double best_media = 0;

            for (int i = 0; i < filcol.size(); i++) {
                int sum = 0;
                ArrayList<Integer> a = (ArrayList) filcol.get(i);
                for (Integer as : a) {
                    sum += as;
                }
                media_mia = sum / (double) a.size();
                medias_mias.add(media_mia);
                sum = 0;
                ArrayList<Integer> ass = (ArrayList) filcolcon.get(i);
                for (Integer as : ass) {
                    sum += as;
                }
                media_contrincante = sum / (double) ass.size();
                medias_contricante.add(media_contrincante);
                if(best_media < media_mia-media_contrincante){
                    best_media = media_mia-media_contrincante;
                    result = i;
                }
            }
            System.out.println(medias_mias);
            System.out.println(medias_contricante);
            System.out.println(result);

            Random rand = new Random();
            int rnd = rand.nextInt(((20 - 1) - 0) + 1) + 0;
            if (rnd < 2) {
                result = rand.nextInt(((S - 1) - 0) + 1) + 0;
            }
            return result;
        }

        /*public int select_option() {
            int result = 0;
            double media = 0;
            for (int i = 0; i < filcol.size(); i++) {
                int sum = 0;
                ArrayList<Integer> a = (ArrayList) filcol.get(i);
                for (Integer as : a) {
                    sum += as;
                }
                if (media < sum / (double) a.size()) {
                    media = sum / (double) a.size();
                    result = i;
                }
                System.out.println(media);
                System.out.println(result);
            }
            Random rand = new Random();
            int rnd = rand.nextInt(((20 - 1) - 0) + 1) + 0;
            if (rnd < 2) {
                result = rand.nextInt(((S - 1) - 0) + 1) + 0;
            }
            return result;
        }*/

 /*public ArrayList get_filas(int payoff) {
            System.out.println("get_filas");
            ArrayList filas = new ArrayList();
            for (int i = 0; i < S; i++) {
                ArrayList fila = new ArrayList();
                for (int j = 0; j < S; j++) {
                    ArrayList a = (ArrayList) matrix.get(i).get(j);
                    fila.add(a.get(payoff));
                }
                filas.add(fila);
            }
            return filas;
        }

        public ArrayList get_columnas(int payoff) {
            System.out.println("get_columnas");
            ArrayList columnas = new ArrayList();
            for (int i = 0; i < S; i++) {
                ArrayList columna = new ArrayList();
                for (int j = 0; j < S; j++) {
                    ArrayList a = (ArrayList) matrix.get(j).get(i);
                    columna.add(a.get(payoff));
                }
                columnas.add(columna);
            }
            return columnas;
        }*/
        public ArrayList update_matrix(int S, int nfila, int ncolumna, int payoff1, int payoff2) {
            ArrayList asd = (ArrayList) matrix.get(nfila).get(ncolumna);
            asd.set(0, payoff1);
            asd.set(1, payoff2);

            ArrayList asd1 = (ArrayList) matrix.get(ncolumna).get(nfila);
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
            ArrayList matrix = new ArrayList<>();
            for (int i = 0; i < S; i++) {//Generamos fila a fila la matriz
                ArrayList fila = new ArrayList<>();
                for (int j = 0; j < S; j++) {//Para cada fila generamos cada elemento de la matriz
                    ArrayList payoffs = new ArrayList<>();
                    payoffs.add(0);
                    payoffs.add(0);
                    fila.add(payoffs);
                }
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
