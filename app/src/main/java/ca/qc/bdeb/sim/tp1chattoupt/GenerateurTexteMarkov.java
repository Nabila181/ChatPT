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
    private static final String CORPUS_APPRIS = "corpus-appris.txt";

    private void chargerCorpusAppris() {
        Path p = Path.of(CORPUS_APPRIS);
        if (!Files.exists(p)) return;

        try (BufferedReader br = Files.newBufferedReader(p)) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                ligne = ligne.trim().toLowerCase(Locale.ROOT);
                if (ligne.isEmpty()) continue;

                String[] mots = ligne.split("\\s+");
                List<String> liste = new ArrayList<>();
                liste.add("");
                Collections.addAll(liste, mots);
                liste.add("");

                for (int i = 0; i < liste.size() - 1; i++) {
                    ajouterTransition(liste.get(i), liste.get(i + 1));
                }
            }
        } catch (IOException e) {
            System.err.println("Impossible de lire corpus-appris.txt : " + e.getMessage());
        }
    }

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

    private Map<String, Map<String, Integer>> scores = new HashMap<>();

    public GenerateurTexteMarkov(String cheminCorpus) throws IOException {
        construireModele(cheminCorpus);
        chargerCorpusAppris();
        appliquerFeedback();
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
    
    if (modeEntrainement) {
        delta *= 3; // renforcement/pénalisation plus fort
    }

    for (int i = 0; i < mots.length - 1; i++) {
        String courant = mots[i];
        String suivant = mots[i + 1];

        Map<String, Integer> map = transitions.get(courant);
        if (map == null) continue;
        Map<String, Integer> mapScores = scores.get(courant);
        if (mapScores != null) {
            int s = mapScores.getOrDefault(suivant, 0);
            mapScores.put(suivant, s + (positif ? 1 : -1));
        }

        int freq = map.getOrDefault(suivant, 0);

        if (positif) {
            map.put(suivant, freq + 1);   // renforcement
        } else {
            if (freq <= 2) {
            map.remove(suivant); // élimination
        } else {
            map.put(suivant, freq - 1); // pénalisation douce
        }
        }
        // Persistance dans feedback.csv
        enregistrerFeedbackCSV(courant, suivant, delta);
    }
    // Ajout au corpus si 👍
    if (positif) {
        enregistrerReponseApprise(reponse);
    }   
}
private boolean reponseDejaPresente(String reponse) {
    Path p = Path.of(CORPUS_APPRIS);
    if (!Files.exists(p)) return false;

    try (BufferedReader br = Files.newBufferedReader(p)) {
        String ligne;
        while ((ligne = br.readLine()) != null) {
            if (ligne.equals(reponse)) {
                return true;
            }
        }
    } catch (IOException e) {
        System.err.println("Impossible de lire corpus-appris.txt : " + e.getMessage());
    }
    return false;
}
public void afficherProbabilites() {
    for (var entry : transitions.entrySet()) {
        String mot = entry.getKey();
        Map<String, Integer> suiv = entry.getValue();

        int total = suiv.values().stream().mapToInt(Integer::intValue).sum();

        System.out.println("Mot : " + mot);
        for (var e : suiv.entrySet()) {
            String s = e.getKey();
            int freq = e.getValue();
            double p = (double) freq / total;
            System.out.printf("  -> %s (freq=%d, p=%.3f)%n", s, freq, p);
        }
    }
}
public void exporterModele(String chemin) {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(chemin))) {
        bw.write("mot,suivant,freq,score");
        bw.newLine();

        for (var entry : transitions.entrySet()) {
            String mot = entry.getKey();
            Map<String, Integer> suiv = entry.getValue();
            Map<String, Integer> mapScores = scores.getOrDefault(mot, Map.of());

            for (var e : suiv.entrySet()) {
                String s = e.getKey();
                int freq = e.getValue();
                int score = mapScores.getOrDefault(s, 0);

                bw.write(mot + "," + s + "," + freq + "," + score);
                bw.newLine();
            }
        }
    } catch (IOException e) {
        System.err.println("Impossible d'exporter le modèle : " + e.getMessage());
    }
}
private boolean modeEntrainement = false;

public void setModeEntrainement(boolean actif) {
    modeEntrainement = actif;
}

private void enregistrerReponseApprise(String reponse) {
    if (reponseDejaPresente(reponse)) {
        return;
    }

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(CORPUS_APPRIS, true))) {
        bw.write(reponse);
        bw.newLine();
    } catch (IOException e) {
        System.err.println("Impossible d'écrire dans corpus-appris.txt : " + e.getMessage());
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
