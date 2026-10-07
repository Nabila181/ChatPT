package ca.qc.bdeb.sim.tp1chattoupt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

public class GenerateurTexteAleatoireTest {

    @Test
    public void testCorpusCharge() throws IOException {
        GenerateurTexteAleatoire gen = new GenerateurTexteAleatoire(
                cheminRessource("corpus-un-mot.txt"));
        assertFalse(gen.getMots().isEmpty(), "Le corpus ne doit pas être vide");
    }

    @Test
    public void testRepondreDonneMotDuCorpus() throws IOException {
        GenerateurTexteAleatoire gen = new GenerateurTexteAleatoire("corpus-mini.txt");
        String rep = gen.repondre("test");

        List<String> mots = gen.getMots();
        assertTrue(mots.contains(rep), "La réponse doit être un mot du corpus");
    }

    @Test
    public void testRepondreJamaisVide() throws IOException {
        GenerateurTexteAleatoire gen = new GenerateurTexteAleatoire("corpus-mini.txt");
        String rep = gen.repondre("question");
        assertFalse(rep.isEmpty(), "La réponse ne doit jamais être vide");
    }

    @Test
    public void testIOExceptionSiFichierIntrouvable() {
        assertThrows(IOException.class, () -> {
            new GenerateurTexteAleatoire("fichier-inexistant.txt");
        });
    }

    @Test
    public void testQuestionVideDonneMotDuCorpus() throws IOException {
        GenerateurTexteAleatoire gen = new GenerateurTexteAleatoire("corpus-mini.txt");
        String rep = gen.repondre("");
        assertFalse(rep.isEmpty(), "Même une question vide doit retourner un mot du corpus");
    }

    @Test
    public void testCorpusUnSeulMot() throws IOException {
        GenerateurTexteAleatoire gen = new GenerateurTexteAleatoire(
                cheminRessource("corpus-un-mot.txt"));

        String rep = gen.repondre("test");
        assertEquals("unique", rep);
    }

    @Test
    public void testCorpusUneSeuleLigne() throws IOException {
        GenerateurTexteAleatoire gen = new GenerateurTexteAleatoire(
                cheminRessource("corpus-une-ligne.txt"));
        String rep = gen.repondre("test");
        assertFalse(rep.isEmpty(), "Le générateur doit fonctionner même avec une seule ligne");
    }

    private String cheminRessource(String nom) throws IOException {
        var ressource = getClass().getResource("/" + nom);
        assertNotNull(ressource, "Ressource introuvable : " + nom);
        try {
            return Path.of(ressource.toURI()).toString();
        } catch (URISyntaxException e) {
            throw new IOException("URL de ressource invalide : " + nom, e);
        }
    }
}
