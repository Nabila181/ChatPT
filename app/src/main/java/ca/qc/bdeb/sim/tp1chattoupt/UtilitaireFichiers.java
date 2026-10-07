package ca.qc.bdeb.sim.tp1chattoupt;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UtilitaireFichiers {

    // public static BufferedReader ouvrirFichierTextuel(String chemin) throws IOException {
    //     return Files.newBufferedReader(Path.of(chemin));
    // }
    public static BufferedReader ouvrirFichierTextuel(String chemin) throws IOException {
    Path p = Path.of(chemin);

    if (!Files.exists(p)) {
        throw new IOException("Fichier introuvable : " + chemin);
    }

    if (!Files.isReadable(p)) {
        throw new IOException("Fichier illisible : " + chemin);
    }

    return Files.newBufferedReader(p);
}

}
// toute la logique d’ouverture est dans UNE seule classe

// si tu veux changer quelque chose (encodage, logs, vérifications, exceptions personnalisées)
// → tu modifies une seule classe

// tous les générateurs utilisent la même méthode

// C’est ça, la centralisation.