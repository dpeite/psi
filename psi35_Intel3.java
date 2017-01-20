
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class psi35_Intel3 extends Agent {

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
        int payoff = 0;
        boolean playerA = false;

        String[] ids;
        String[] payoffs;
        ArrayList anterior = new ArrayList<>();
        ArrayList anterior2 = new ArrayList<>();
        ArrayList jugada = new ArrayList<>();

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
                    //playerA = false;
                    break;
                case 1://new game
                    anterior.clear();
                    anterior2.clear();
                    jugada.clear();
                    matrix = generate_matrix(S);
                    if (Integer.parseInt(ids[0]) == id) {
                        playerA = true;
                    } else {
                        playerA = false;
                    }
                    rounds = 1;
                    break;
                case 2:
                    if (playerA == true) {
                        payoff = 0;
                    } else {
                        payoff = 1;
                    }
                    best_fila(payoff);
                    int pos = select_option();
                    jugada.add(pos);
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
                    anterior.add(Integer.parseInt(ids[abs(payoff - 1)]));
                    anterior2.add(Integer.parseInt(ids[abs(payoff - 1)]));

                    if (rounds < R) {
                        rounds++;
                        //step = 2;
                    } else {
                        //step++;
                    }
                    step = 15;
                    break;
                case 4://endgame
                    /*if (game < N) {
                        step = 85;
                    } else {
                        step = 0;
                    }*/

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
        ArrayList filcol = new ArrayList();
        ArrayList filcolcon = new ArrayList();

        public void best_fila(int payoff) {
            HashSet ordered = new HashSet();
            best_payoff = 0;
            filcol.clear();
            filcolcon.clear();
            for (int i = 0; i < S; i++) {
                ArrayList filcol2 = new ArrayList();
                ArrayList filcolcon2 = new ArrayList();
                for (int j = 0; j < S; j++) {
                    ArrayList a = (ArrayList) matrix.get(i).get(j);
                    ArrayList b = (ArrayList) matrix.get(j).get(i);
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
                        if (((Integer) a.get(payoff)) > best_payoff) {
                            bestfilas.clear();
                            best_payoff = (Integer) a.get(payoff);
                        }
                        bestfilas.add(best);
                    }
                    ordered.add(best);
                }
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
            /*
        Selecciona la mejor opcion que el agente debe de escoger en funcion de la matriz que conoce hasta ese momento
        Tiene 3 estrategias:
        1 - Calcula la media de la columna/fila que deba escoger el agente
            Calcula la media de la columna/fila del contricante
            Resta las medias para asi obtener que fila/columna es en la que el mas gana, mientras su contricante menos gana
        
        2 - Si en las 3 jugadas anteriores el contricante escogio siempre la misma jugada, el agente supone que la proxima jugada sera igual a las anteriores
            Sabiendo eso el agente calcula cual es la mejor posicion que puede escoger sabiendo cual va a ser la proxima eleccion del contricante.
            Para saber cual es la posicion mas ventajosa es parecido a la estrategia anterior, restamos ambos valores
            y el que tenga la mayor diferencia sera la mejor posicion posible a escoger sabiendo la proxima eleccion.
        
        3- Si el contricante basa su estrategia o parte de ella en escoger mi ultima jugada como su jugada, puedo suponer cual sera su siguiente jugada
           Sabiendo esto aplico la misma estrategia que en 2.
        
        Tambien existe un random, el cual añade aleatoriedad a mis jugadas y ayuda a la hora de ir descubriendo nuevas posiciones de la matriz
             */
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
                if (best_media < media_mia - media_contrincante) {
                    best_media = media_mia - media_contrincante;
                    result = i;
                }
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(psi35_Intel2.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (anterior.size() == 3) {
                if ((anterior.get(0) == anterior.get(1)) && (anterior.get(0) == anterior.get(2))) {
                    int best_dif = 0;
                    for (int i = 0; i < filcolcon.size(); i++) {
                        ArrayList dif1 = (ArrayList) filcol.get(((int) anterior.get(0)));
                        ArrayList dif2 = (ArrayList) filcolcon.get(((int) anterior.get(0)));
                        int dif = (int) dif2.get(i) - (int) dif1.get(i);
                        if (dif > best_dif) {
                            result = i;
                            best_dif = dif;
                        }
                    }
                    anterior.remove(2);

                } else {
                    anterior.clear();
                }
            }
            if (jugada.size() > 1) {
                if (jugada.get(rounds - 3).equals(anterior2.get(rounds - 2))) {
                    int best_dif = 0;
                    for (int i = 0; i < filcolcon.size(); i++) {
                        ArrayList dif1 = (ArrayList) filcol.get(((int) jugada.get(rounds - 3)));
                        ArrayList dif2 = (ArrayList) filcolcon.get(((int) jugada.get(rounds - 3)));
                        int dif = (int) dif2.get(i) - (int) dif1.get(i);
                        if (dif > best_dif) {
                            result = i;
                            best_dif = dif;
                        }
                    }
                }
            }
            Random rand = new Random();
            int rnd = rand.nextInt(((20 - 1) - 0) + 1) + 0;
            if (rnd < 2 || rounds == 1) {
                result = rand.nextInt(((S - 1) - 0) + 1) + 0;
            }
            return result;
        }

        //Actualizamos la matriz con los resultados obtenidos
        public ArrayList update_matrix(int S, int nfila, int ncolumna, int payoff1, int payoff2) {
            ArrayList asd = (ArrayList) matrix.get(nfila).get(ncolumna);
            asd.set(0, payoff1);
            asd.set(1, payoff2);
            //Tambien actualizamos la posicion simetrica
            ArrayList asd1 = (ArrayList) matrix.get(ncolumna).get(nfila);
            asd1.set(0, payoff2);
            asd1.set(1, payoff1);

            return matrix;
        }

        //Generamos una matriz vacia que luego iremos llenando a medida que conozcamos los resultados
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
