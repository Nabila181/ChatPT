package ca.qc.bdeb.sim.tp1chattoupt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

// public class GenerateurTexteMarkov extends GenerateurTexte {
//     @Override
//     public String repondre(String question) throws IOException {
//         throw new UnsupportedOperationException(
//                 "Le générateur Markov sera implémenté dans une prochaine étape.");
//     }
// }
public class GenerateurTexteMarkov extends GenerateurTexte {
    private final Map<String, Map<String, Integer>> transitions = new HashMap<>();
    private final Random random = new Random();
    private static final String FICHIER_FEEDBACK = "feedback.csv";

    private void enregistrerFeedbackCSV(String mot, String suivant, int delta) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FICHIER_FEEDBACK, true))) {
            bw.write(mot + "," + suivant + "," + delta);
            bw.newLine();
        } catch (IOException e) {
            // On ne casse pas le programme si le feedback ne peut pas être écrit
            System.err.println("Impossible d'écrire dans feedback.csv : " + e.getMessage());
        }
    }
    
    // @Override
    // public String repondre(String question) throws IOException {
    //     if (transitions.isEmpty()) {
    //         return "";
    //     }

    //     List<String> mots = new ArrayList<>();
    //     String motActuel = "";
    //     mots.add(motActuel);

    //     while (true) {
    //         String prochainMot = choisirSuivant(motActuel);
    //         if (prochainMot == null || "".equals(prochainMot)) {
    //             break;
    //         }

    //         mots.add(prochainMot);
    //         motActuel = prochainMot;

    //         if (mots.size() > 50) {
    //             break;
    //         }
    //     }

    //     return String.join(" ", mots).trim();
    // }
    @Override
    public String repondre(String question) {
        String[] mots = question.trim()
                                .toLowerCase(Locale.ROOT)
                                .split("\\s+");
        List<String> candidats = new ArrayList<>();
        for (String mot : mots) {
            if (mot.length() >= 4 && transitions.containsKey(mot)) {
                candidats.add(mot);
            }
        }

        if (candidats.isEmpty()) {
            return "Je n'ai pas compris";
        }

        String courant = candidats.get(random.nextInt(candidats.size()));
        StringBuilder sb = new StringBuilder();
        sb.append(courant);

        int maxMots = 200; // limite haute pour éviter les boucles infinies
        for (int i = 0; i < maxMots; i++) {
            String suivant = choisirSuivant(courant);
            if (suivant.isEmpty()) {// 🔥 Arrêt naturel : fin de phrase
                break;
            }
            sb.append(' ').append(suivant);
            courant = suivant;
        }

        return sb.toString();
    }


    public GenerateurTexteMarkov(String cheminCorpus) throws IOException {
        construireModele(cheminCorpus);
    }

    private void ajouterTransition(String from, String to) {
        transitions
            .computeIfAbsent(from, k -> new HashMap<>())
            .merge(to, 1, Integer::sum);
    }

    private void construireModele(String cheminCorpus) throws IOException {
        try (var reader = UtilitaireFichiers.ouvrirFichierTextuel(cheminCorpus)) {
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                ligne = ligne.trim().toLowerCase(Locale.ROOT);
                if (ligne.isEmpty()) continue;

                String[] mots = ligne.split("\\s+");
                List<String> liste = new ArrayList<>();
                liste.add(""); // début
                Collections.addAll(liste, mots);
                liste.add(""); // fin

                for (int i = 0; i < liste.size() - 1; i++) {
                    String from = liste.get(i);
                    String to = liste.get(i + 1);
                    ajouterTransition(from, to);
                }
            }
        }
    }
    private String choisirSuivant(String motCourant) {
        Map<String, Integer> freq = transitions.get(motCourant);
        if (freq == null || freq.isEmpty()) {
            return "";
        }
        int total = freq.values().stream().mapToInt(Integer::intValue).sum();
        int r = random.nextInt(total) + 1;
        int cumul = 0;
        for (var entry : freq.entrySet()) {
            cumul += entry.getValue();
            if (cumul >= r) {
                return entry.getKey();
            }
        }
        return "";
    }
public Map<String, Map<String, Integer>> getTransitions() {
    return transitions;
}
    public void enregistrerFeedback(String question, String reponse, boolean positif) {
    if (reponse == null || reponse.isBlank()) {
    return;
    }
    String[] mots = reponse.split("\\s+");
    int delta = positif ? 1 : -1;

    for (int i = 0; i < mots.length - 1; i++) {
        String courant = mots[i];
        String suivant = mots[i + 1];

        Map<String, Integer> map = transitions.get(courant);
        if (map == null) continue;

        int freq = map.getOrDefault(suivant, 0);

        if (positif) {
            map.put(suivant, freq + 1);   // renforcement
        } else {
            map.put(suivant, Math.max(1, freq - 1)); // pénalisation douce
        }
        // Persistance dans feedback.csv
        enregistrerFeedbackCSV(courant, suivant, delta);
    }
    
}
public void appliquerFeedback() {
    Path p = Path.of(FICHIER_FEEDBACK);
    if (!Files.exists(p)) return;

    try (BufferedReader br = Files.newBufferedReader(p)) {
        String ligne;
        while ((ligne = br.readLine()) != null) {
            String[] t = ligne.split(",");
            if (t.length != 3) continue;

            String mot = t[0];
            String suivant = t[1];
            int delta = Integer.parseInt(t[2]);

            Map<String, Integer> map = transitions.get(mot);
            if (map == null) continue;

            int freq = map.getOrDefault(suivant, 1);
            map.put(suivant, Math.max(1, freq + delta));
        }
    } catch (IOException e) {
        System.err.println("Impossible de lire feedback.csv : " + e.getMessage());
    }
}

}
