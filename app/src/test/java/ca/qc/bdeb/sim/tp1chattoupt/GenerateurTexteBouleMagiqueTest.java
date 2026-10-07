package ca.qc.bdeb.sim.tp1chattoupt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GenerateurTexteBouleMagiqueTest {
    @Test
    void repondreReturnsAnAnswer() {
        String answer = new GenerateurTexteBouleMagique().repondre("Est-ce que ça va marcher?");

        assertNotNull(answer);
        assertFalse(answer.isBlank());
    }
}
