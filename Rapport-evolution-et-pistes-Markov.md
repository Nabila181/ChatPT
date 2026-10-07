# Rapport d'evolution du projet ChatTouPT

## Objet du rapport

Ce document conserve les principales etapes suivies pour mettre en place le projet ChatTouPT, les choix techniques faits en cours de route, l'etat fonctionnel observe et des pistes pour faire evoluer le generateur de Markov.

Le projet a d'abord ete construit pour repondre aux exigences du TP : une version console, une interface JavaFX et trois generateurs de texte. L'objectif d'une evolution future est different : produire des reponses mieux adaptees a un sujet, sans confondre une chaine de mots plausible avec une comprehension du sens.

## 1. Démarche suivie

### 1.1 Création et vérification du projet Gradle

Le projet a ete genere avec Gradle dans VS Code. L'environnement utilise Java 21 et le Gradle Wrapper du projet, en version 9.2.0. Le wrapper permet de lancer Gradle avec la version prevue par le projet, plutot que de dependre uniquement d'une installation globale.

Le projet contient un module `app`. La configuration de la racine se trouve dans `settings.gradle`, et celle de l'application dans `app/build.gradle`.

La premiere verification a consiste a confirmer que le JDK et Gradle etaient accessibles et que le projet d'exemple compile et execute ses tests.

### 1.2 Alignement du nom de projet, du groupe et du package

Trois notions distinctes ont ete alignees sur les consignes :

- Le nom Gradle du projet est `TP1-ChatTouPT`.
- Le groupe Gradle est `ca.qc.bdeb.sim`.
- Le package Java des classes est `ca.qc.bdeb.sim.tp1chattoupt`.

Le groupe Gradle est une metadonnee du projet; il ne cree pas automatiquement le package Java. Les fichiers sources ont donc ete places sous :

```text
app/src/main/java/ca/qc/bdeb/sim/tp1chattoupt/
```

Les classes Java de ce dossier declarent le package avec une ligne `package ca.qc.bdeb.sim.tp1chattoupt;`.

### 1.3 Ajout des deux points d'entrée et configuration JavaFX

Le projet possede deux classes principales :

- `MainCmd` produit un rapport console avec une question fixe et six reponses par generateur.
- `MainJavaFX` etend `Application` et construit l'interface graphique.

Gradle est configure pour utiliser `MainJavaFX` comme classe principale de la tache `run`. Une tache `runCmd` de type `JavaExec` lance `MainCmd`. Le plugin OpenJFX et JavaFX Controls ont ete ajoutes, avec JavaFX 21.0.8 et un toolchain Java 21.

L'execution JavaFX a echoue dans VS Code parce que le cache de configuration Gradle etait active alors que le plugin JavaFX employe accedait a des proprietes de tache qui ne sont pas compatibles avec ce cache dans cette configuration. La propriete `org.gradle.configuration-cache` a ete desactivee dans `gradle.properties`. Apres ce changement, `run` a ouvert la fenetre JavaFX et `runCmd` a lance la console.

### 1.4 Création de la hiérarchie des générateurs

La classe abstraite `GenerateurTexte` fournit l'interface commune `repondre(String question)`. Trois sous-classes permettent de selectionner l'algorithme sans faire porter toute sa logique a l'interface :

- `GenerateurTexteBouleMagique` choisit une reponse parmi vingt reponses predefinies.
- `GenerateurTexteAleatoire` charge les mots distincts d'un corpus et en choisit aleatoirement. La reponse contient trois fois le nombre de mots de la question.
- `GenerateurTexteMarkov` compte les transitions entre mots dans un corpus, puis choisit les mots suivants selon leurs frequences.

Cette separation permet a `MainJavaFX` de conserver une reference de type `GenerateurTexte` et de remplacer le generateur choisi.

### 1.5 Chargement des corpus et rapport console

Un corpus miniature a ete place pour les essais sous `corpus-mini.txt`. `MainCmd` l'utilise pour les generateurs aleatoire et Markov.

Une premiere execution de `runCmd` avait affiche une erreur parce que le programme ne trouvait pas `corpus-mini.txt` au chemin relatif attendu. Le fichier a ensuite ete place au bon endroit et le rapport a pu afficher les six reponses des trois generateurs.

Le rapport de debogage Markov a ensuite ete ajoute. Il parcourt les transitions du modele et affiche le nombre d'occurrences d'une suite divise par le nombre total de suites observees pour le mot courant. Le marqueur vide est presente sous les formes explicites `<début de phrase>` et `<fin de phrase>`.

Exemple : si le mot `manger` est suivi deux fois par une fin de phrase et une fois par `des`, le rapport doit montrer :

```text
manger ==> {<fin de phrase>: 2/3} {des: 1/3}
```

L'ordre des entrees peut varier car les transitions sont conservees dans des `HashMap`; cela ne change pas leurs frequences.

### 1.6 Interface JavaFX

L'interface comprend actuellement :

- une zone centrale d'historique de conversation avec defilement;
- un champ de saisie, le bouton Envoyer et l'envoi par la touche Entree;
- cinq questions frequentes;
- un historique des cinq dernieres questions, avec texte tronque pour les boutons;
- des boutons radio pour choisir Boule Magique, Aleatoire ou Markov;
- un FileChooser pour selectionner un corpus pour les deux derniers generateurs;
- un logo charge depuis les ressources JavaFX et des styles visuels.

Les erreurs d'entree et de chargement sont interceptees par l'interface et affichees dans un label rouge. La classe `UtilitaireFichiers` centralise l'ouverture des corpus et est appelee par les generateurs.

### 1.7 Encodage de la console

Les sources et le corpus ont ete verifies comme contenant de vrais caracteres UTF-8. Le probleme d'accents venait de l'encodage de sortie du terminal Windows et de la JVM. Les options `-Dstdout.encoding=UTF-8` et `-Dstderr.encoding=UTF-8` ont ete ajoutees a la tache `runCmd` et le terminal PowerShell a ete configure en code de page 65001 pour les essais. Les accents du rapport se sont alors affiches correctement.

### 1.8 Correction des tests du générateur aléatoire

Deux tests attendaient un seul mot alors que le contrat de l'algorithme demande trois mots pour une question d'un mot. Les tests ont ete corriges pour :

- verifier que la reponse contient trois mots et que chacun appartient au corpus;
- attendre `unique unique unique` si le corpus de test ne contient qu'un seul mot distinct.

## 2. Architecture actuelle

| Composant | Responsabilité |
|---|---|
| `MainCmd` | Lance la version console et affiche les resultats des trois generateurs ainsi que le rapport Markov. |
| `MainJavaFX` | Construit l'interface, gere les evenements utilisateur, choisit et appelle le generateur actif. |
| `GenerateurTexte` | Contrat commun des classes de generation. |
| `GenerateurTexteBouleMagique` | Selection aleatoire d'une reponse predefinie. |
| `GenerateurTexteAleatoire` | Lecture des mots distincts et generation aleatoire de mots. |
| `GenerateurTexteMarkov` | Construction du modele de transitions et generation probabiliste. |
| `UtilitaireFichiers` | Centralise l'ouverture d'un fichier de corpus et les erreurs de lecture. |

Les sources se trouvent dans `app/src/main/java/ca/qc/bdeb/sim/tp1chattoupt/`. Les tests se trouvent sous `app/src/test/java/ca/qc/bdeb/sim/tp1chattoupt/`, et le logo est dans les ressources de l'application.

## 3. État vérifié et réserves

### Fonctionnalités en place

- Les taches Gradle `run` et `runCmd` sont configurees.
- La console peut charger `corpus-mini.txt`, afficher six resultats par algorithme et les transitions Markov avec leurs frequences.
- Les trois generateurs et l'interface JavaFX sont presents.
- Le modele Markov choisit un mot de depart d'au moins quatre caracteres qui apparait dans le corpus, choisit les transitions en fonction de leurs frequences et traite l'absence de candidat par `Je n'ai pas compris`.
- La lecture des corpus est centralisee dans `UtilitaireFichiers`.
- Des tests unitaires existent pour les trois generateurs.

### Points à ne pas considérer comme définitivement validés

- Le modele Markov actuel est un modele d'ordre 1 : le mot suivant depend seulement du mot courant. Il ne comprend pas la question et ne garantit pas que la reponse soit factuellement correcte.
- La generation a une limite de securite de 200 mots. Elle s'arrete normalement au marqueur de fin, mais peut etre tronquee si cette limite est atteinte.
- `UtilitaireFichiers` verifie l'existence et la lisibilite, mais sa version actuelle ne detecte pas explicitement tous les fichiers binaires ou les contenus mal formes comme fichiers non textuels.
- L'annulation du FileChooser, le chargement d'un corpus incorrect et les interactions visuelles doivent etre testes manuellement dans JavaFX. Un test de compilation ne prouve pas le comportement de chaque controle.
- L'interface appelle le generateur directement depuis le fil JavaFX. Un grand corpus pourrait donc retarder ou figer l'interface pendant son analyse.
- Le rapport HTML des tests et la derniere execution des tests n'ont pas toujours affiche le meme total. Le rapport HTML consulte affichait 15 tests reussis; un lancement ulterieur par le lanceur de tests a indique 18 reussites. Il faut relancer `.\gradlew.bat :app:test` et verifier le nouveau rapport Gradle avant de conserver un total definitif.

## 4. Que signifie « Markov renforcé »?

Renforcer Markov peut ameliorer la coherence locale des phrases et leur adaptation au vocabulaire d'un sujet. Cela ne lui donne pas une comprehension generale du langage. Un modele Markov choisit des suites selon des statistiques apprises dans un texte; il peut donc produire une phrase plausible mais hors sujet ou fausse.

Pour ce projet, l'objectif realiste pourrait etre formule ainsi :

> À partir d'un corpus sélectionné sur un sujet, générer une réponse courte qui reprend un terme pertinent de la question, suit des transitions observées dans ce corpus et s'arrête proprement, tout en indiquant quand le corpus ne permet pas de continuer.

Cette formulation est testable et ne promet pas une intelligence que le modele n'a pas.

## 5. Feuille de route proposée pour renforcer le modèle Markov

### Étape 1 — Stabiliser le comportement de référence

Avant toute modification, conserver un petit corpus de test avec des transitions connues et noter les reponses attendues en termes de regles :

- frequences exactes des transitions;
- mot de depart admissible ou absence de mot de depart;
- presence d'une fin de phrase;
- longueur maximale;
- comportement sur corpus vide, ligne vide ou mot inconnu.

Ajouter une graine aleatoire injectable aux tests rendrait les generations reproductibles sans supprimer l'aleatoire en usage normal.

### Étape 2 — Améliorer le nettoyage et la tokenisation

Le code actuel separe les mots sur les espaces; la ponctuation reste donc collee aux mots (`chat` et `chat,` sont deux tokens). Definir une politique explicite :

1. lire le corpus en UTF-8;
2. normaliser les espaces et les caracteres Unicode;
3. separer les mots et la ponctuation de maniere coherente;
4. normaliser la casse pour les statistiques;
5. conserver les marqueurs de debut, de fin de ligne et, si utile, les signes de ponctuation.

Les questions doivent passer par la meme normalisation pour pouvoir etre comparees aux tokens du corpus.

### Étape 3 — Passer à un Markov d'ordre supérieur

Un modele d'ordre 1 regarde un seul mot precedent. Un modele d'ordre 2 regarde les deux derniers mots; un modele d'ordre 3 en regarde trois. Par exemple, le contexte `c'est bon` peut avoir des continuations differentes de `tres bon`, meme si le dernier mot est le meme.

Representer une cle comme une suite de `n` tokens et associer a cette cle une table des tokens suivants et de leurs comptes. Commencer par l'ordre 2 ou 3, puis mesurer l'effet : un ordre tres eleve produit souvent des contextes rares et des reponses qui s'arretent faute de continuations observees.

### Étape 4 — Ajouter un repli (backoff)

Quand un contexte de trois mots n'existe pas dans le corpus, essayer le contexte des deux derniers mots, puis du dernier mot. Ce repli evite d'echouer simplement parce que la question contient une combinaison rare, tout en profitant des contextes plus precis quand ils existent.

### Étape 5 — Rendre le démarrage et le sujet plus pertinents

Le code actuel choisit au hasard parmi les mots de quatre caracteres ou plus de la question qui existent dans le corpus. Une amelioration pourrait :

- supprimer une liste configurable de mots courants sans valeur thematique;
- normaliser les mots avant la comparaison;
- classer les candidats selon leur frequence ou leur rarete dans le corpus;
- preferer les candidats qui ont des continuations suffisantes;
- ne pas faire semblant d'avoir une reponse si aucun terme utile n'est connu.

Pour garder les sujets separes, charger un corpus par sujet ou conserver une cle de sujet dans l'index. Eviter de melanger des documents de sujets differents sans etiquettes.

### Étape 6 — Améliorer la génération et la fin des phrases

Conserver les frequences de transition pour l'echantillonnage pondere, mais :

- traiter la fin de phrase comme un resultat explicite, et non comme une chaine vide ambiguë;
- imposer une limite de securite configurable et signaler si elle a ete atteinte;
- empecher les boucles repetitives, par exemple en detectant les cycles longs ou en limitant une repetition excessive;
- ne pas ajouter de mot apres le marqueur de fin;
- choisir une longueur raisonnable et expliquer a l'utilisateur si le modele n'a pas pu produire une reponse complete.

### Étape 7 — Ajouter un apprentissage incrémental prudent

Une forme d'adaptation peut etre ajoutee en enregistrant les transitions observees dans de nouveaux textes ou en tenant compte d'evaluations utilisateur. Garder ces donnees distinctes du corpus original permet de les reinitialiser ou de les comparer.

Un vote positif ne prouve pas qu'une reponse est vraie. Les retours devraient ajuster des preferences (par exemple, des corpus ou formulations privilegies), pas transformer automatiquement une phrase generee en fait fiable. Prevoir une sauvegarde versionnee des comptes et la possibilite de reconstruire le modele depuis les sources.

### Étape 8 — Mesurer les améliorations

Comparer l'ordre 1, l'ordre 2 et l'ordre 3 sur les memes questions et corpus. Mesurer au minimum :

- le taux de reponses vides ou « je ne sais pas »;
- le taux de fin naturelle avant la limite;
- les repetitions excessives;
- la presence de termes du sujet dans la reponse;
- la qualite evaluee par une petite liste de questions et une grille humaine;
- le temps de chargement et de generation avec un corpus volumineux.

Les tests automatiques doivent verifier des proprietes et des frequences exactes sur de petits corpus controles; ils ne doivent pas exiger une phrase aleatoire particuliere quand plusieurs tirages sont valides.

### Étape 9 — Protéger la réactivité de JavaFX

Deplacer le chargement et l'analyse de corpus volumineux hors du fil graphique avec `Task` ou `Service` JavaFX. Afficher un indicateur de chargement, desactiver le bouton pendant le traitement et montrer les erreurs de facon explicite. Cela devient important avant d'utiliser de gros documents ou une mise a jour incrementale.

## 6. Ordre de réalisation recommandé

1. Completer et fiabiliser les tests de fichiers invalides, corpus vides et transitions Markov exactes.
2. Definir et tester une tokenisation coherente pour le corpus et les questions.
3. Implementer un modele Markov d'ordre 2 avec repli vers l'ordre 1.
4. Rendre les marqueurs de debut et fin explicites dans le modele et borner proprement la generation.
5. Ajouter la selection de corpus par sujet et une evaluation reproductible.
6. Ajouter l'apprentissage incremental et les retours utilisateur, en conservant des donnees separees.
7. Deplacer les traitements longs vers des taches JavaFX en arriere-plan.

## Conclusion

Le projet est passe d'un squelette Gradle a une application Java avec deux points d'entree, une interface JavaFX, trois generateurs, un chargement de corpus partage et un rapport de transitions Markov. Les tests ont aussi permis de corriger des attentes qui contredisaient la regle des trois mots par mot de question.

La prochaine evolution coherente est un modele Markov d'ordre superieur avec tokenisation, backoff, gestion explicite de la fin de phrase et evaluation sur des corpus thematiques. Cette progression peut rendre les reponses plus coherentes et plus adaptees au sujet, mais Markov seul restera un generateur statistique de texte, pas un systeme qui comprend ou verifie les faits.
