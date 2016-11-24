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
public class Dummy_agent extends Agent {
 protected void setup() {
 // Printout a welcome message
 System.out.println("Hello! Dummy_agent "+getAID().getName()+" is ready.");
 }
}