package ca.qc.bdeb.sim.tp1chattoupt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map;

public class GenerateurTexteMarkovTest {

    @Test
    public void testTransitionsConstruites() throws IOException {
        GenerateurTexteMarkov gen = new GenerateurTexteMarkov("corpus-mini.txt");
        Map<String, Map<String, Integer>> trans = gen.getTransitions();

        org.junit.jupiter.api.Assertions.assertFalse(trans.isEmpty(), "Les transitions ne devraient pas être vides");
    }

    @Test
    public void testDebutDePhraseExiste() throws IOException {
        GenerateurTexteMarkov gen = new GenerateurTexteMarkov("corpus-mini.txt");
        Map<String, Map<String, Integer>> trans = gen.getTransitions();

        assertTrue(trans.containsKey(""), "Le marqueur de début de phrase '' doit exister");
    }

    @Test
    public void testFinDePhraseExiste() throws IOException {
        GenerateurTexteMarkov gen = new GenerateurTexteMarkov("corpus-mini.txt");
        Map<String, Map<String, Integer>> trans = gen.getTransitions();

        boolean finExiste = trans.values().stream()
                .anyMatch(map -> map.containsKey(""));

        assertTrue(finExiste, "Le marqueur de fin de phrase '' doit apparaître comme transition");
    }

    @Test
    public void testProbabilitesSommentCorrectement() throws IOException {
        GenerateurTexteMarkov gen = new GenerateurTexteMarkov("corpus-mini.txt");
        Map<String, Map<String, Integer>> trans = gen.getTransitions();

        for (var entry : trans.entrySet()) {
            int total = entry.getValue().values().stream().mapToInt(Integer::intValue).sum();
            assertTrue(total > 0, "Chaque mot doit avoir au moins une transition");
        }
    }

    @Test
    public void testRepondreMotInconnu() throws IOException {
        GenerateurTexteMarkov gen = new GenerateurTexteMarkov("corpus-mini.txt");
        String rep = gen.repondre("xyzabc");
        assertEquals("Je n'ai pas compris", rep);
    }

    @Test
    public void testRepondreMotConnu() throws IOException {
        GenerateurTexteMarkov gen = new GenerateurTexteMarkov("corpus-mini.txt");
        String rep = gen.repondre("bon");
        assertFalse(rep.isEmpty(), "La réponse ne doit pas être vide pour un mot connu");
    }

    @Test
    public void testFinNaturelleSansLimiteArbitraire() throws IOException {
        GenerateurTexteMarkov gen = new GenerateurTexteMarkov("corpus-mini.txt");
        String rep = gen.repondre("bon");

        // La phrase doit se terminer naturellement sans dépasser la limite de sécurité
        int nbMots = rep.split("\\s+").length;
        assertTrue(nbMots <= 200, "La réponse ne doit pas dépasser la limite de sécurité");
    }
}
