package ca.qc.bdeb.sim.tp1chattoupt;

import java.io.IOException;
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
    
}
