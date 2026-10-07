package ca.qc.bdeb.sim.tp1chattoupt;

import java.io.File;
import java.io.IOException;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import javafx.stage.Stage;
import javafx.stage.FileChooser;

public class MainJavaFX extends Application {
    private GenerateurTexte generateur;
    @Override
    public void start(Stage stage) {
        generateur = new GenerateurTexteBouleMagique();

        BorderPane root = new BorderPane();
        root.setTop(creerBarreHaut(stage));
        root.setLeft(creerBarreGauche());
        root.setCenter(creerZoneConversation());
        root.setBottom(creerZoneSaisie());
        root.setStyle("-fx-background-color: #f7f3ef;");


        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("ChatTouPT");
        stage.setScene(scene);
        stage.show();
    }

    // private Node creerZoneConversation() {
    //     TextArea conversation = new TextArea();
    //     conversation.setEditable(false);
    //     conversation.setWrapText(true);
    //     return conversation;
    // }
    private VBox historiqueMessages;

    private ScrollPane scrollPaneConversation;
    
    private ScrollPane creerZoneConversation() {
        historiqueMessages = new VBox(10); // 10 px d’espace entre les messages
        historiqueMessages.setStyle("-fx-padding: 10;");
        scrollPaneConversation = new ScrollPane(historiqueMessages);//permet de scroller si la liste devient longue
        scrollPaneConversation.setFitToWidth(true);//les messages s’adaptent à la largeur disponible
        // 🔥 Listener qui scroll automatiquement quand la hauteur change
        historiqueMessages.heightProperty().addListener((obs, oldVal, newVal) -> {
        scrollPaneConversation.setVvalue(1.0);
        });
        return scrollPaneConversation;
        }
    private TextField champQuestion;

    private HBox creerZoneSaisie() {
        champQuestion = new TextField();
        champQuestion.setPromptText("Posez une question...");
        HBox.setHgrow(champQuestion, Priority.ALWAYS);//Le TextField doit prendre tout l’espace disponible dans le HBox.
        champQuestion.setMaxWidth(Double.MAX_VALUE);//Le TextField doit pouvoir s’étendre à la largeur maximale disponible.

        Button btnEnvoyer = new Button("Envoyer");

        // Quand on clique sur Envoyer
        btnEnvoyer.setOnAction(e -> envoyerQuestion());
        btnEnvoyer.setStyle(
            "-fx-background-color: #e07a5f;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 5;"
        );

        // Quand on appuie sur Enter dans le champ
        champQuestion.setOnAction(e -> envoyerQuestion());
        champQuestion.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #8d6e63;" +
            "-fx-border-radius: 5;" +
            "-fx-padding: 5;"
        );

        HBox bas = new HBox(10);
        bas.setStyle("-fx-padding: 10;");
        bas.getChildren().addAll(champQuestion, btnEnvoyer);
        return bas;
    }
    private void ajouterMessage(String question, String reponse) {
        Label lblQuestion = new Label("Vous: " + question);
        lblQuestion.setStyle("-fx-font-weight: bold; -fx-text-fill: #3d3d3d;");


        Label lblReponse = new Label("Agent: " + reponse);
        lblReponse.setStyle("-fx-text-fill: #5a5a5a;");
        historiqueMessages.getChildren().addAll(lblQuestion, lblReponse);
        //scrollPaneConversation.layout();// Forcer le scroll vers le bas après ajout,force JavaFX à recalculer la hauteur du contenu.
        // Scroll automatique vers le bas APRÈS mise à jour de l'UI
        //scrollPaneConversation.setVvalue(1.0);//positionne le scroll tout en bas (0 = haut, 1 = bas).
        // Platform.runLater(() -> {scrollPaneConversation.setVvalue(1.0);    });

        
    }
    private void envoyerQuestion() {
        String question = champQuestion.getText().trim();
        if (question.isEmpty()) {// On ignore les questions vides
            return;
        }

        String reponse;
        try {
            reponse = generateur.repondre(question);// Générer la réponse via le modèle
        } catch (IOException ex) {
            if (lblErreur != null) {
                lblErreur.setText("Impossible de générer une réponse");
            }
            return;
        }

        ajouterMessage(question, reponse);
        ajouterDerniereQuestion(question);

        champQuestion.clear();
        }
    private VBox vboxDernieresQuestions;

    private VBox creerBarreGauche() {
        VBox gauche = new VBox(15);
        gauche.setStyle(
            "-fx-background-color: #f2cc8f;" +
            "-fx-padding: 10;" +
            "-fx-border-color: #e07a5f;" +
            "-fx-border-width: 0 2 0 0;"
        );

        // Section "Dernières questions"
        Label lblDernieres = new Label("Dernières questions");
        vboxDernieresQuestions = new VBox(5);

        // Section "Questions fréquentes"
        Label lblFrequentes = new Label("Questions fréquentes");
        VBox vboxFrequentes = new VBox(5);

        // Ajouter quelques questions fréquentes
        ajouterQuestionsFrequentes(vboxFrequentes);

        gauche.getChildren().addAll(lblDernieres, vboxDernieresQuestions,
                                    lblFrequentes, vboxFrequentes);
        return gauche;
    }
    private void ajouterQuestionsFrequentes(VBox vbox) {
        String[] frequentes = {
            "Qui est Astérix le Gaulois ?",
            "Est-ce que j'aime les tacos ?",
            "Qu'est-ce que les gens mangent ?",
            "Qui est Pie-IX ?",
            "Qui est Magritte ?"
        };

        for (String q : frequentes) {
            Button btn = new Button(tronquerTexte(q));
            btn.setOnAction(e -> {
                champQuestion.setText(q);
                envoyerQuestion();
            });
            vbox.getChildren().add(btn);
        }
    }
    private void ajouterDerniereQuestion(String question) {
        Button btn = new Button(tronquerTexte(question));
        btn.setOnAction(e -> {
            champQuestion.setText(question);
            envoyerQuestion();
        });

        vboxDernieresQuestions.getChildren().add(0, btn); // ajouter en haut

        // Si plus de 5, supprimer la plus vieille (en bas)
        if (vboxDernieresQuestions.getChildren().size() > 5) {
            int lastIndex = vboxDernieresQuestions.getChildren().size() - 1;
            vboxDernieresQuestions.getChildren().remove(lastIndex);
        }
    }
    private String tronquerTexte(String texte) {
        int max = 40;
        if (texte.length() <= max) {
            return texte;
        }
        return texte.substring(0, max) + "...";
    }
     

    private Label lblErreur; // pour afficher les erreurs en rouge

    private HBox creerBarreHaut(Stage stage) {
        // 1. Conteneur horizontal
        HBox haut = new HBox(15);// 15px d'espace entre les éléments
        // 2. Logo / titre
        // Label logo = new Label("ChatTouPT");
        // logo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Image img = new Image(getClass().getResourceAsStream("/images/logo.png"));
        ImageView logoView = new ImageView(img);
        logoView.setFitHeight(40);      // hauteur du logo
        logoView.setPreserveRatio(true); // garder les proportions

        // 3. Groupe de RadioButtons
        ToggleGroup groupe = new ToggleGroup();

        RadioButton rbBoule = new RadioButton("Boule Magique");
        rbBoule.setToggleGroup(groupe);
        rbBoule.setSelected(true);// modèle par défaut

        RadioButton rbAleatoire = new RadioButton("Aléatoire");
        rbAleatoire.setToggleGroup(groupe);

        RadioButton rbMarkov = new RadioButton("Markov");
        rbMarkov.setToggleGroup(groupe);

        // 4. Bouton pour charger un fichier
        Button btnCharger = new Button("Charger un nouveau modèle");

        // 5. Label d’erreur
        lblErreur = new Label();
        lblErreur.setStyle("-fx-text-fill: red;");

        // 6. Action du bouton Charger
        // btnCharger.setOnAction(e -> {
        //     if (rbBoule.isSelected()) {
        //         // Boule magique n'a pas besoin de fichier
        //         generateur = new GenerateurTexteBouleMagique();
        //         lblErreur.setText("");
        //     } else {
        //         // Aléatoire ou Markov → il faut choisir un fichier
        //         String chemin = choisirFichier(stage);
        //         if (chemin != null) {
        //             try {
        //                 if (rbAleatoire.isSelected()) {
        //                     generateur = new GenerateurTexteAleatoire(chemin);
        //                 } else if (rbMarkov.isSelected()) {
        //                     generateur = new GenerateurTexteMarkov(chemin);
        //                 }
        //                 lblErreur.setText("");// succès → pas d’erreur
        //             } catch (IOException ex) {
        //                 lblErreur.setText("Impossible de charger le corpus");
        //             }
        //         }else {
        //             lblErreur.setText("Aucun fichier sélectionné");
        //             return ;// on efface les erreurs précédentes
        //         }
        //     }
        // });

        Runnable chargerModele = () -> {
            if (rbBoule.isSelected()) {
                generateur = new GenerateurTexteBouleMagique();
                lblErreur.setText("");
                return;
            }

            String chemin = choisirFichier(stage);
            if (chemin == null) {
                lblErreur.setText("");
                return;
            }

            try {
                if (rbAleatoire.isSelected()) {
                    generateur = new GenerateurTexteAleatoire(chemin);
                } else if (rbMarkov.isSelected()) {
                    generateur = new GenerateurTexteMarkov(chemin);
                }
                lblErreur.setText("");
            } catch (IOException ex) {
                lblErreur.setText("Impossible de charger le corpus");
            }
        };
        btnCharger.setOnAction(e -> chargerModele.run());

        // 7. Ajouter tous les éléments dans la barre
        Label titre = new Label("ChatTouPT");
        titre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        haut.getChildren().addAll(logoView, titre, rbBoule, rbAleatoire, rbMarkov, btnCharger, lblErreur);
        haut.setAlignment(Pos.CENTER_LEFT);
        haut.setSpacing(15);

        return haut;
    }
    //l'interface doit permettre à l’utilisateur de :
        // 1- Cliquer sur Charger un nouveau modèle
        // 2- Ouvrir une fenêtre de sélection de fichier (FileChooser)
        // 3- Choisir un fichier .txt
        // 4- Récupérer le chemin du fichier pour l’envoyer au modèle
    private String choisirFichier(Stage stage) {
        FileChooser selecteur = new FileChooser();//Création du FileChooser
        selecteur.setTitle("Sélectionnez un corpus à analyser");//Définir le titre de la fenêtre
        selecteur.setInitialDirectory(new File("."));//Définir le dossier de départ Ça évite que la fenêtre s’ouvre dans un endroit bizarre (Documents, Images, etc.).
        File fichierChoisi = selecteur.showOpenDialog(stage);//Afficher la fenêtre de sélection

        if (fichierChoisi != null) {//Vérifier si un fichier a été choisi
            return fichierChoisi.getAbsolutePath();//on retourne son chemin complet
        }
        // Sélection annulée
        return null;//on ne plante pas si l’utilisateur annule.On retourne juste null, et c’est à toi de gérer ça dans ton bouton “Charger un nouveau modèle”
    }

     
    public static void main(String[] args) {
        launch(args);
    }
}
