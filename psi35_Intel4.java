
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.Math.abs;

public class psi35_Intel4 extends Agent {

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

        int rounds = 1;
        int playerA = 0;

        String[] ids;
        String[] payoffs;
        ArrayList anterior = new ArrayList<>();
        ArrayList anteriorBIS = new ArrayList<>();
        ArrayList mi_jugada = new ArrayList<>();

        public void action() {
            ACLMessage msg1 = receive();
            if (msg1 != null) {
                //System.out.println(msg1);
                String[] message = msg1.getContent().split("#");
                if (message[0].equals("Id")) {//Al iniciar una nueva partida guardamos los datos
                    id = Integer.parseInt(message[1]);
                    String[] params = message[2].split(",");
                    N = Integer.parseInt(params[0]);
                    S = Integer.parseInt(params[1]);
                    R = Integer.parseInt(params[2]);
                    I = Integer.parseInt(params[3]);
                    P = Integer.parseInt(params[4]);
                } else if (message[0].equals("NewGame")) {//Al comienzo de un nuevo juego limpiamos todas las variables
                    ids = message[1].split(",");
                    anterior.clear();
                    anteriorBIS.clear();
                    mi_jugada.clear();
                    matrix = generate_matrix(S);//Generamos una matriz de 0
                    if (Integer.parseInt(ids[0]) == id) {//Comprobamos si tenemos que escoger filas o columnas segun el id
                        playerA = 0;
                    } else {
                        playerA = 1;
                    }
                    rounds = 1;
                } else if (message[0].equals("Position")) {
                    read_matrix(playerA);//Obtenemos informacion de la matriz que utilizaremos en la estrategia
                    int pos = select_option();//Con los datos obtenidos antes, escogemos la mejor posicion
                    mi_jugada.add(pos);//Añadimos nuestra jugada a un array, para llevar el control de nuestras jugadas y las del contricante

                    if (msg1 != null) {
                        ACLMessage reply = msg1.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("Position#" + pos);
                        send(reply);
                    }
                } else if (message[0].equals("Results")) {
                    ids = message[1].split(",");
                    payoffs = message[2].split(",");
                    //Actualizamos la matriz con las payoffs obtenidas del resultado
                    update_matrix(S, Integer.parseInt(ids[0]), Integer.parseInt(ids[1]), Integer.parseInt(payoffs[0]), Integer.parseInt(payoffs[1]));
                    //Añadimos la jugada del contricante a dos arrays diferentes, que utilizaremos para detectar
                    //si siempre escoge la misma jugada, o si escoge mi jugada anterior
                    anterior.add(Integer.parseInt(ids[abs(playerA - 1)]));
                    anteriorBIS.add(Integer.parseInt(ids[abs(playerA - 1)]));

                    if (rounds < R) {
                        rounds++;
                    } else if (message[0].equals("EndGame")) {
                    } else if (message[0].equals("Changed")) {
                    }
                } else {
                    block();
                }
            }
        }

        public boolean done() {
            return false;
        }

        //int best_payoff = 0;
        //ArrayList ordered2;
        //HashSet bestfilas = new HashSet();
        ArrayList filcol = new ArrayList();
        ArrayList filcolcon = new ArrayList();

        public void read_matrix(int payoff) {
            /*
            Esta funcion recorre la matriz que vamos aprendido en cada jugada y va obteniendo y guardando los datos necesarios para la estrategia a seguir
            A medida que va recorriendo la matriz añade a dos arraylist las payoffs de los jugadores, en la variable filcol añade las payoffs
            de la fila/columna que me correspondan. Mientras que en la variable filcolcon hace lo mismo pero con las payoffs del contricante
            
            Nota: La parte comentada, son otras dos variables que me dan, la posicion en la que gano mas payoff y la matriz ordenada de mayor payoff a menor
            No son relevantes para mi estrategia, pero si podrian ser utiles en otra
             */
            //HashSet ordered = new HashSet();
            //best_payoff = 0;
            filcol.clear();
            filcolcon.clear();
            for (int i = 0; i < S; i++) {
                ArrayList filcolN = new ArrayList();
                ArrayList filcolconN = new ArrayList();
                for (int j = 0; j < S; j++) {
                    ArrayList a = (ArrayList) matrix.get(i).get(j);
                    ArrayList b = (ArrayList) matrix.get(j).get(i);
                    if (payoff == 0) {
                        filcolN.add(a.get(payoff));
                        filcolconN.add(b.get(payoff));
                    } else {
                        filcolN.add(b.get(payoff));
                        filcolconN.add(a.get(payoff));
                    }

                    /*ArrayList<Integer> best = new ArrayList<>();
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
                    ordered.add(best);*/
                }
                filcol.add(filcolN);
                filcolcon.add(filcolconN);
            }
            /*ordered2 = new ArrayList(ordered);
            Collections.sort(ordered2, new Comparator<ArrayList<Integer>>() {
                @Override
                public int compare(ArrayList<Integer> o1, ArrayList<Integer> o2) {
                    return (Integer) (o2.get(2).compareTo(o1.get(2)));
                }

            });
            ordered.clear();*/
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

            int result = 0;
            double media_mia = 0;
            double media_contrincante = 0;
            double best_media = 0;

            for (int i = 0; i < filcol.size(); i++) {
                int sum = 0;
                ArrayList<Integer> arrayTMP = (ArrayList) filcol.get(i);
                for (Integer payoff : arrayTMP) {
                    sum += payoff;
                }
                media_mia = sum / (double) arrayTMP.size();
                sum = 0;
                ArrayList<Integer> arrayTMP2 = (ArrayList) filcolcon.get(i);
                for (Integer payoff : arrayTMP2) {
                    sum += payoff;
                }
                media_contrincante = sum / (double) arrayTMP2.size();
                if (best_media < media_mia - media_contrincante) {
                    best_media = media_mia - media_contrincante;
                    result = i;
                }
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
            if (mi_jugada.size() > 2) {
                if ((mi_jugada.get(rounds - 3).equals(anteriorBIS.get(rounds - 2))) && (mi_jugada.get(rounds - 4).equals(anteriorBIS.get(rounds - 3)))) {
                    int best_dif = 0;
                    for (int i = 0; i < filcolcon.size(); i++) {
                        ArrayList dif1 = (ArrayList) filcol.get(((int) mi_jugada.get(rounds - 3)));
                        ArrayList dif2 = (ArrayList) filcolcon.get(((int) mi_jugada.get(rounds - 3)));
                        int dif = (int) dif2.get(i) - (int) dif1.get(i);
                        if (dif > best_dif) {
                            result = i;
                            best_dif = dif;
                        }
                    }
                }
            }
            //No es necesario un sleep, pero parece que ayuda a obtener mejores resultados¿?
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(psi35_Intel2.class.getName()).log(Level.SEVERE, null, ex);
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

        //Generamos una matriz de ceros que luego iremos llenando a medida que conozcamos los resultados
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
