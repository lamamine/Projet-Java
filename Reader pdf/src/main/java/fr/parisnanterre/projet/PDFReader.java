package fr.parisnanterre.projet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

//import des classes de la bibliotheque itext pour la gestion d'un pdf
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

public class PDFReader extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private JTextArea textArea;
    private JButton openButton;
    private JFileChooser fileChooser;

    public PDFReader() {
        super("PDF Reader");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Création de la zone de texte pour afficher le contenu du PDF
        textArea = new JTextArea();
        textArea.setEditable(false);

        // Création d'un panneau de défilement pour la zone de texte
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        // Création d'un panneau pour le bouton d'ouverture du PDF
        JPanel buttonPanel = new JPanel();

        // Création du bouton d'ouverture du PDF
        openButton = new JButton("Open");
        openButton.addActionListener(this);

        buttonPanel.add(openButton);

        // Ajout de la zone de texte et du panneau de boutons à la fenêtre principale
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new PDFReader();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == openButton) {
            if (fileChooser == null) {
                // Création de l'instance JFileChooser si elle n'existe pas déjà
                fileChooser = new JFileChooser();
            }
            // Affichage de la boîte de dialogue pour sélectionner un fichier
            int returnVal = fileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                // Récupération du fichier sélectionné
                File file = fileChooser.getSelectedFile();
                try {
                    // Lecture du PDF avec iText
                    PdfReader reader = new PdfReader(file.getAbsolutePath());
                    int numPages = reader.getNumberOfPages();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i <= numPages; i++) {
                        // Extraction du texte de chaque page du PDF
                        String text = PdfTextExtractor.getTextFromPage(reader, i);
                        sb.append(text);
                    }
                    // Affichage du texte dans la zone de texte
                    textArea.setText(sb.toString());
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
