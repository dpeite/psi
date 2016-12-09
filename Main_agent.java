
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

public class Main_agent extends Agent {

    boolean game = false;
    psi35_GUI GUI;
    ArrayList matrix;
    ArrayList games = new ArrayList();

    int N = 0;
    int R = 8;
    int S = 3;
    int I = 0;
    int P = 20;

    protected void setup() {
        System.out.println("Hello! Main Agent " + getAID().getName() + " is ready.");
        //System.out.println("Lanzando GUI...");
        GUI = new psi35_GUI();
        GUI.init(this, R, S, I, P);
        addBehaviour(new TickerBehaviour(this, 2000) {
            protected void onTick() {
                // Update the list of seller agents
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("Player");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    AID[] sellerAgents = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        sellerAgents[i] = result[i].getName();
                        if (game == false) {
                            GUI.setTable(result[i].getName().getLocalName());
                            update_labels();

                        }
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
                // Perform the request
            }
        });

        //addBehaviour(new main_loop());
    }

    public void set_params(String R, String S, String I, String P) {
        if (game != false) {
            stop_game();
        }
        try {
            this.R = Integer.parseInt(R);
            this.S = Integer.parseInt(S);
            this.I = Integer.parseInt(I);
            this.P = Integer.parseInt(P);
            update_labels();
            GUI.jTextArea1.append("Parametros guardados\n");
        } catch (Exception exc) {
            GUI.jTextArea1.append("Algun valor no es un caracter valido\n");
        }
    }

    public ArrayList generate_matrix(int S) {
        GUI.jTextArea1.append("Generando matriz...\n");
        ArrayList matrix = new ArrayList();
        Random rand = new Random();
        for (int i = 0; i < S; i++) {
            ArrayList fila = new ArrayList();
            for (int j = 0; j < S; j++) {
                ArrayList payoffs = new ArrayList();
                for (int k = 0; k < 2; k++) {
                    if (i == 0) {
                        if (j == i) {
                            int value = rand.nextInt((9 - 0) + 1) + 0;
                            payoffs.add(value);
                            payoffs.add(value);
                            break;
                        } else {
                            payoffs.add(rand.nextInt((9 - 0) + 1) + 0);
                        }
                    } else if (i >= j) {
                        if (i == j) {
                            int value = rand.nextInt((9 - 0) + 1) + 0;
                            payoffs.add(value);
                            payoffs.add(value);
                            break;
                        } else if (i != 1 && (S - i != S - 1)) {
                            if (j == 0) {
                                payoffs = (ArrayList) ((ArrayList) matrix.get(0)).get(i - j);
                            } else {
                                payoffs = (ArrayList) ((ArrayList) matrix.get(j)).get(i);
                            }
                        } else {
                            payoffs = (ArrayList) ((ArrayList) matrix.get(0)).get(i - j);
                        }
                    } else {
                        payoffs.add(rand.nextInt((9 - 0) + 1) + 0);

                    }
                }
                ArrayList payoffs2 = new ArrayList();
                payoffs2.add(payoffs.get(0));
                payoffs2.add(payoffs.get(1));

                Collections.swap(payoffs2, 0, 1);
                fila.add(payoffs2);
            }
            matrix.add(fila);
        }
        GUI.jTextArea1.append("Matriz generada\n");
        print_matrix(matrix);
        return matrix;
    }

    public void update_matrix(int p) {
        GUI.jTextArea1.append("Actualizando matriz...\n");
        Random rand = new Random();
        int S = ((ArrayList) matrix.get(0)).size();
        double cambios = 0;
        double matriz_cambiada;
        boolean exit = true;
        do {
            int fila = rand.nextInt(((S - 1) - 0) + 1) + 0;
            int columna = rand.nextInt(((S - 1) - 0) + 1) + 0;
            //System.out.println("Fila: " + fila + " Columna: " + columna);
            ArrayList swap = new ArrayList();
            swap.add(rand.nextInt((9 - 0) + 1) + 0);
            swap.add(rand.nextInt((9 - 0) + 1) + 0);
            ((ArrayList) matrix.get(fila)).set(columna, swap);
            cambios++;
            if (fila != columna) {
                ArrayList swap2 = new ArrayList();
                swap2.add(swap.get(0));
                swap2.add(swap.get(1));
                Collections.swap(swap2, 0, 1);
                ((ArrayList) matrix.get(columna)).set(fila, swap2);
                cambios++;
            }

            matriz_cambiada = cambios / (S * S);
            if ((matriz_cambiada * 100) >= p) {
                exit = false;
            }
        } while (exit);
        GUI.jTextArea1.append("Cambios realizados a la matriz: " + cambios + "\n"
                + "De un total de " + (S * S) + " posiciones en la matriz\n");

        print_matrix(matrix);
    }

    public void update_labels() {
        int jugadores = GUI.getTable().size();
        GUI.players_label.setText(Integer.toString(jugadores));
        N = (jugadores * (jugadores - 1)) / 2;
        GUI.totalgames_label.setText(Integer.toString(N));
        GUI.totalrounds_label.setText(Integer.toString(R));
        GUI.actualgame_label.setText("1");
        GUI.actualround_label.setText("1");
    }

    public void print_matrix(ArrayList matrix) {
        GUI.jTextArea3.setText("");
        for (int i = 0; i < matrix.size(); i++) {
            GUI.jTextArea3.append(matrix.get(i).toString() + "\n");
        }
        GUI.jTextArea3.append("\n");
    }

    public void calculate_games() {
        games.removeAll(games);
        for (int i = 0; i < GUI.getTable().size(); i++) {
            for (int j = 0; j < GUI.getTable().size(); j++) {
                ArrayList game = new ArrayList();
                if (i != j) {
                    //game.add((String) ((Vector) GUI.getTable().get(i)).get(0));
                    //game.add((String) ((Vector) GUI.getTable().get(j)).get(0));
                    game.add(i);
                    game.add(j);
                    ArrayList swap = new ArrayList(game);
                    Collections.swap(swap, 0, 1);
                    if (games.contains(swap)) {
                        continue;
                    }
                    games.add(game);
                }
            }
        }
        System.out.println(games);
    }

    public void start_game() {
        if (GUI.getTable().size() != 0) {
            if (game == true) {
                stop_game();
            }
            GUI.jTextArea1.append("Comienza el juego\n");
            matrix = generate_matrix(S);
            update_labels();
            GUI.reset_scoreboard();

            calculate_games();
            game = true;
            stop = false;
            pause = false;
            addBehaviour(new main_loop());
        } else {
            GUI.jTextArea1.append("El juego no ha empezado, no hay jugadores disponibles\n");
        }

    }

    boolean stop = false;
    boolean pause = false;

    public void stop_game() {
        stop = true;
        game = false;
        pause = false;
        System.out.println("stop");
    }

    public void pause_game() {
        pause = true;
    }

    public void resume_game() {
        pause = false;
    }

    public class main_loop extends Behaviour {

        private int step = 0;
        private int j = 0;
        private int rounds = 1;
        private int games_played = 1;
        private int row;
        private int col;
        private int payoff1 = 0;
        private int payoff2 = 0;
        private int total_rounds = 0;

        ACLMessage msg1 = receive();

        public void action() {
            if (pause == false) {
                switch (step) {
                    case 0:
                        for (int i = 0; i < GUI.getTable().size(); i++) {
                            System.out.println(((Vector) GUI.getTable().get(i)).get(0));
                            String agent = (String) ((Vector) GUI.getTable().get(i)).get(0);
                            String message = "Id#" + i + "#" + GUI.getTable().size() + ","
                                    + S + "," + R + "," + I + "," + P;
                            addBehaviour(new send_message(agent, message, INFORM));
                            //send_message(agent, message, INFORM);
                        }
                        step++;
                        break;
                    case 1:
                        rounds = 1;
                        payoff1 = 0;
                        payoff2 = 0;
                        GUI.actualgame_label.setText(Integer.toString(games_played));
                        GUI.jTextArea1.append("Comienza el juego: " + games_played + "\n");
                        for (int i = 0; i < 2; i++) {
                            int id0 = (int) ((ArrayList) games.get(0)).get(0);
                            int id1 = (int) ((ArrayList) games.get(0)).get(1);
                            int id = (int) ((ArrayList) games.get(0)).get(i);

                            String agent = (String) ((Vector) GUI.getTable().get(id)).get(0);
                            String message = "NewGame#" + id0 + "," + id1;
                            addBehaviour(new send_message(agent, message, INFORM));
                            //send_message(agent, message, INFORM);
                        }
                        step++;
                        break;
                    case 2:
                        if (j == 0) {
                            GUI.actualround_label.setText(Integer.toString(rounds));
                            GUI.jTextArea1.append("Comienza la ronda: " + rounds + "\n");
                            for (int i = 0; i < 2; i++) {

                                int id0 = (int) ((ArrayList) games.get(0)).get(0);
                                int id1 = (int) ((ArrayList) games.get(0)).get(1);
                                int id = (int) ((ArrayList) games.get(0)).get(i);

                                String agent = (String) ((Vector) GUI.getTable().get(id)).get(0);
                                String message = "Position";
                                addBehaviour(new send_message(agent, message, REQUEST));
                                //send_message(agent, message, INFORM);
                            }
                            j++;

                        }
                        msg1 = receive();
                        if (msg1 != null) {
                            // Process the message
                            //System.out.println(msg1);
                            String msg = msg1.getContent();
                            if (j == 1) {
                                j++;
                                row = Integer.parseInt(msg.split("#")[1]);

                            } else {
                                col = Integer.parseInt(msg.split("#")[1]);

                                j = 0;
                                step++;
                            }
                        } else {
                            block();
                        }
                        break;
                    case 3://Results

                        for (int i = 0; i < 2; i++) {
                            int id0 = (int) ((ArrayList) games.get(0)).get(0);
                            int id1 = (int) ((ArrayList) games.get(0)).get(1);
                            int id = (int) ((ArrayList) games.get(0)).get(i);
                            int payoff1_now = (int) ((ArrayList) ((ArrayList) matrix.get(row)).get(col)).get(0);
                            int payoff2_now = (int) ((ArrayList) ((ArrayList) matrix.get(row)).get(col)).get(1);
                            payoff1 = payoff1 + payoff1_now;
                            payoff2 = payoff2 + payoff2_now;
                            String agent = (String) ((Vector) GUI.getTable().get(id)).get(0);
                            String message = "Results#" + row + "," + col + "#"
                                    + payoff1_now + "," + payoff2_now;
                            addBehaviour(new send_message(agent, message, INFORM));
                            //send_message(agent, message, INFORM);
                        }
                        print_matrix(matrix);
                        GUI.jTextArea3.append((String) ((Vector) GUI.getTable().get(0)).get(0) + ": " + Integer.toString(payoff1) + "\n");
                        GUI.jTextArea3.append((String) ((Vector) GUI.getTable().get(1)).get(0) + ": " + Integer.toString(payoff2) + "\n");

                        if (rounds < R) {
                            rounds++;
                            total_rounds++;
                            if (total_rounds == I) {
                                step = 5;
                            } else {
                                step = 2;
                            }
                        } else {
                            step++;
                        }
                        break;
                    case 4:
                        for (int i = 0; i < 2; i++) {
                            int id0 = (int) ((ArrayList) games.get(0)).get(0);
                            int id1 = (int) ((ArrayList) games.get(0)).get(1);
                            int id = (int) ((ArrayList) games.get(0)).get(i);

                            String agent = (String) ((Vector) GUI.getTable().get(id)).get(0);
                            String message = "EndGame";
                            addBehaviour(new send_message(agent, message, INFORM));
                            //send_message(agent, message, INFORM);
                        }
                        //step++;
                        int id0 = (int) ((ArrayList) games.get(0)).get(0);
                        int id1 = (int) ((ArrayList) games.get(0)).get(1);

                        if (payoff1 < payoff2) {
                            GUI.jTextArea1.append("Juego ganado por: " + id1 + " " + ((Vector) GUI.getTable().get(id1)).get(0) + "\n");
                            GUI.addResult(id1, 2);
                            GUI.addResult(id0, 3);
                        } else if (payoff1 > payoff2) {
                            GUI.jTextArea1.append("Juego ganado por: " + id0 + " " + ((Vector) GUI.getTable().get(id0)).get(0) + "\n");
                            GUI.addResult(id1, 3);
                            GUI.addResult(id0, 2);
                        }

                        if (games_played < N) {
                            step = 1;
                            games_played++;
                            games.remove(0);

                        } else {
                            stop = true;
                            game = false;
                            pause = false;
                        }
                        break;
                    case 5:
                        update_matrix(P);
                        total_rounds = 0;
                        for (int i = 0; i < 2; i++) {

                            id0 = (int) ((ArrayList) games.get(0)).get(0);
                            id1 = (int) ((ArrayList) games.get(0)).get(1);
                            int id = (int) ((ArrayList) games.get(0)).get(i);

                            String agent = (String) ((Vector) GUI.getTable().get(id)).get(0);
                            String message = "Changed#" + P;
                            addBehaviour(new send_message(agent, message, INFORM));
                        }
                        step = 2;
                        break;

                }
            }
        }

        public boolean done() {
            return stop == true;
        }
    }

    public class send_message extends OneShotBehaviour {

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
            //System.out.println("mandamos mensaje222");
            ACLMessage msg = new ACLMessage(performative);
            msg.addReceiver(new AID(agent, AID.ISLOCALNAME));
            msg.setPerformative(performative);
            msg.setContent(message);
            send(msg);
        }
    }

}
