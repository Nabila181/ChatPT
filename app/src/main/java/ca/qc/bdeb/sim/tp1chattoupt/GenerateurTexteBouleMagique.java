package ca.qc.bdeb.sim.tp1chattoupt;

import java.util.concurrent.ThreadLocalRandom;

public class GenerateurTexteBouleMagique extends GenerateurTexte {
    private static final String[] REPONSES = {
            "Essaye plus tard",
            "Essaye encore",
            "Pas d'avis",
            "C'est ton destin",
            "Le sort en est jeté",
            "Une chance sur deux",
            "Repose ta question",
            "D'après moi oui",
            "C'est certain",
            "Oui absolument",
            "Tu peux compter dessus",
            "Sans aucun doute",
            "Très probable",
            "Oui",
            "C'est bien parti",
            "C'est non",
            "Peu probable",
            "Faut pas rêver",
            "N'y compte pas",
            "Impossible"
    };

    @Override
    public String repondre(String question) {
        int index = ThreadLocalRandom.current().nextInt(REPONSES.length);
        return REPONSES[index];
    }
}
