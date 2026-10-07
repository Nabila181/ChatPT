package ca.qc.bdeb.sim.tp1chattoupt;

import java.io.IOException;
import java.util.Map;

// public class MainCmd {
//     public static void main(String[] args) {
//         System.out.println("ChatTouPT - version ligne de commande");
//     }
// }
public class MainCmd {
    public static void main(String[] args) {
        String question = "c'est bon les tacos";

        System.out.println("QUESTION: " + question);
        System.out.println();

        try {
            GenerateurTexte boule = new GenerateurTexteBouleMagique();
            GenerateurTexte aleatoire = new GenerateurTexteAleatoire("corpus-mini.txt");
            GenerateurTexte markov = new GenerateurTexteMarkov("corpus-mini.txt");

            System.out.println("# Boule Magique");
            for (int i = 0; i < 6; i++) {
                System.out.println(boule.repondre(question));
            }
            System.out.println();

            System.out.println("# Aléatoire - corpus-mini.txt");
            for (int i = 0; i < 6; i++) {
                System.out.println(aleatoire.repondre(question));
            }
            System.out.println();

            System.out.println("# Markov - corpus-mini.txt");
            for (int i = 0; i < 6; i++) {
                System.out.println(markov.repondre(question));
            }
            System.out.println();

            // Ici: afficher les infos de débogage de la chaîne de Markov
            // en parcourant transitions dans GenerateurTexteMarkov
            System.out.println("=== Infos de débogage pour la chaîne de Markov ===");

            Map<String, Map<String, Integer>> trans = ((GenerateurTexteMarkov) markov).getTransitions();

            for (var entry : trans.entrySet()) {
                String mot = entry.getKey();
                Map<String, Integer> suites = entry.getValue();

                int total = suites.values().stream().mapToInt(Integer::intValue).sum();
                // 🔥 Correction : afficher <début de phrase> au lieu de ""
                String motAffiche = mot.isEmpty() ? "<début de phrase>" : mot;
                System.out.print(motAffiche + " ==> ");

                for (var suite : suites.entrySet()) {
                    String suivant = suite.getKey();
                    int freq = suite.getValue();
                    // 🔥 Correction : afficher <fin de phrase> au lieu de ""
                    String suivantAffiche = suivant.isEmpty() ? "<fin de phrase>" : suivant;
                    System.out.print("{" + suivantAffiche + ": " + freq + "/" + total + "} ");
                }

                System.out.println();
            }

        } catch (IOException e) {
            System.out.println("Erreur lors du chargement du corpus : " + e.getMessage());
        }
    }
}
