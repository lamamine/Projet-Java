package fr.parisnanterre.projet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.File;
import java.io.IOException;



public class PDFReader extends JFrame {

    private JTabbedPane tabbedPane;
    JTextArea commentArea = new JTextArea();
    File currentFile;
    

    public PDFReader() {
        setTitle("PDF Reader");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        add(tabbedPane);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        openMenuItem.addActionListener(new OpenFiles());
        
        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        saveMenuItem.addActionListener(new SaveFiles());
        
    

        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        KeyStroke prevTabKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
        KeyStroke nextTabKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);

        KeyStroke increasePageKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK);
        KeyStroke decreasePageKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);
        
        InputMap inputMap = tabbedPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(prevTabKeyStroke, "prevTab");
        inputMap.put(nextTabKeyStroke, "nextTab");

        inputMap.put(increasePageKeyStroke, "increasePage");
        inputMap.put(decreasePageKeyStroke, "decreasePage");

        ActionMap actionMap = tabbedPane.getActionMap();
        actionMap.put("prevTab", new TabAction(-1));
        actionMap.put("nextTab", new TabAction(1));


        actionMap.put("increasePage", new PageAction(1));
        actionMap.put("decreasePage", new PageAction(-1));
        
    


    }
    
    

    
    private class OpenFiles implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF Files", "pdf");
            fileChooser.setFileFilter(filter);
            fileChooser.setMultiSelectionEnabled(true);

            int returnVal = fileChooser.showOpenDialog(PDFReader.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                

                try {
                    PDDocument document = PDDocument.load(file);
                    PDFRenderer renderer = new PDFRenderer(document);


                

                    JPanel panel = new JPanel(new GridLayout(0, 1,10,10)); // Utilise GridLayout pour afficher les pages en 2 colonnes

                    
                    
                    
                    Rectangle boundingBox = determineBoundingBox(document,renderer, 5);

          

                    for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                        BufferedImage image = renderer.renderImage(pageIndex);

                        BufferedImage croppedImage = image.getSubimage(
                            boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
                        ImageIcon croppedIcon = new ImageIcon(croppedImage);
                        JLabel croppedLabel = new JLabel(croppedIcon);


                        JPanel pagePanel = new JPanel();
                        pagePanel.setLayout(new BorderLayout());
                        pagePanel.add(Box.createHorizontalGlue(), BorderLayout.LINE_START);
                        pagePanel.add(croppedLabel, BorderLayout.CENTER);
                        pagePanel.add(Box.createHorizontalGlue(), BorderLayout.LINE_END);
                        
                        




                        panel.add(pagePanel);

                    }

                    JScrollPane scrollPane = new JScrollPane(panel);
                    
                    


                    
                    JScrollPane commentScrollPane = new JScrollPane(commentArea);

                    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, commentScrollPane);
                    splitPane.setResizeWeight(1.0); // Permet de conserver la proportion entre les panneaux

                    currentFile = file;
                    document.close();
                    
                    tabbedPane.addTab(file.getName(), splitPane);
                    
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        
        private Rectangle determineBoundingBox(PDDocument document, PDFRenderer renderer, int numPages) throws IOException {
            Rectangle boundingBox = null;
            int pageCount = Math.min(numPages, document.getNumberOfPages());
            for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                BufferedImage image = renderer.renderImage(pageIndex);
                
                Rectangle pageBoundingBox = calculateBoundingBox(image);
                if (boundingBox == null) {
                    boundingBox = pageBoundingBox;
                } else {
                    boundingBox = boundingBox.union(pageBoundingBox);
                }
            }
            return boundingBox;
        }
        
        private Rectangle calculateBoundingBox(BufferedImage image) {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
    
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int rgb = image.getRGB(x, y);
                    if (rgb != Color.WHITE.getRGB()) {
                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        maxX = Math.max(maxX, x);
                        maxY = Math.max(maxY, y);
                    }
                }
            }
    
            if (minX == Integer.MAX_VALUE || minY == Integer.MAX_VALUE || maxX == Integer.MIN_VALUE || maxY == Integer.MIN_VALUE) {

                return new Rectangle(0, 0, image.getWidth(), image.getHeight());
            }
    
            return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
        }
    
    }
    



    private class SaveFiles implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String comments = commentArea.getText();
            String filename = currentFile.getName().replaceAll("\\.pdf$", "") + ".txt";
    
            try {
                FileWriter writer = new FileWriter(filename);
                writer.write(comments);
                writer.close();
                JOptionPane.showMessageDialog(PDFReader.this, "Commentaires enregistrés avec succès !");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(PDFReader.this, "Erreur lors de l'enregistrement des commentaires.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class TabAction extends AbstractAction {
        private int direction;
    
        public TabAction(int direction) {
            this.direction = direction;
        }
    
        @Override
        public void actionPerformed(ActionEvent e) {
            int currentIndex = tabbedPane.getSelectedIndex();
            int newIndex = (currentIndex + direction + tabbedPane.getTabCount()) % tabbedPane.getTabCount();
            tabbedPane.setSelectedIndex(newIndex);
        }
    }

    private class PageAction extends AbstractAction {
        private int direction;

        public PageAction(int direction) {
            this.direction = direction;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int currentIndex = tabbedPane.getSelectedIndex();
            Component selectedComponent = tabbedPane.getComponentAt(currentIndex);
            if (selectedComponent instanceof JSplitPane) {
                JSplitPane splitPane = (JSplitPane) selectedComponent;
                JScrollPane scrollPane = (JScrollPane) splitPane.getLeftComponent();
                Component viewport = scrollPane.getViewport().getView();
                if (viewport instanceof JPanel) {
                    JPanel panel = (JPanel) viewport;
                    int numComponents = panel.getComponentCount();
                    int nbpage = 1 + direction;
                    int nbrows = 0;
                    
                    if (nbpage > 0 ) {
                        if(numComponents%nbpage != 0)
                        {
                            nbrows = 1 ;
                        }
                        int rows = nbrows + numComponents/nbpage;

                        panel.setLayout(new GridLayout(rows, nbpage,10,10));
                        tabbedPane.revalidate();
                        tabbedPane.repaint();
                    }
                }
            }
        }

        
    
        
    }
    

    
    
    
    

    
    




    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PDFReader reader = new PDFReader();
            reader.setVisible(true);
        });
    }
}


