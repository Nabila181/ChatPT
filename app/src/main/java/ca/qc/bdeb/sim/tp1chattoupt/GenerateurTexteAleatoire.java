package ca.qc.bdeb.sim.tp1chattoupt;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

// public class GenerateurTexteAleatoire extends GenerateurTexte {
//     @Override
//     public String repondre(String question) throws IOException {
//         throw new UnsupportedOperationException(
//                 "Le générateur aléatoire sera implémenté dans une prochaine étape.");
//     }
// }
public class GenerateurTexteAleatoire extends GenerateurTexte {

    private final List<String> motsConnus = new ArrayList<>();
    private final Random random = new Random();

    public GenerateurTexteAleatoire(String cheminCorpus) throws IOException {
        chargerCorpus(cheminCorpus);
    }

    private void chargerCorpus(String cheminCorpus) throws IOException {
        try (var reader = UtilitaireFichiers.ouvrirFichierTextuel(cheminCorpus)) {
            String ligne;
            Set<String> set = new HashSet<>();
            while ((ligne = reader.readLine()) != null) {
                String[] mots = ligne.trim()
                                     .toLowerCase(Locale.ROOT)
                                     .split("\\s+");
                for (String mot : mots) {
                    if (!mot.isEmpty()) {
                        set.add(mot);
                    }
                }
            }
            motsConnus.addAll(set);
        }
    }
    public List<String> getMots() {
    return motsConnus;
    }

    @Override
    public String repondre(String question) {
        if (motsConnus.isEmpty()) {
            return "(aucun mot connu)";
        }
        return motsConnus.get(random.nextInt(motsConnus.size()));
    }
}
