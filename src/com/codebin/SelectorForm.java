package com.codebin;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.html.ImageView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class SelectorForm {
    private static JFrame frame;

    private JPanel panel1;
    private JTextArea output_ta;
    private JPanel imgView_jp;
    private JButton loadButton;
    private JButton clearButton;
    private JTextField idIn;
    private JScrollPane scrollPane;
    private JTextField floorIn;

    private String filepath;
    private BufferedImage image;
    private int floor;
    private ArrayList<PixelPanel> bounds = new ArrayList<>();

    //Priority Queue Comparator to get the least ID
    private class PanelPriorityComparator implements Comparator<PixelPanel> {
        @Override
        public int compare(PixelPanel a, PixelPanel b){
            return (int) (a.id - b.id);
        }
    }

    public SelectorForm() {
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (floorIn.getText().isBlank()){
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Make sure floor has a set numeric value first!");
                    return;
                }

                try{
                    floor = Integer.parseInt(floorIn.getText());
                    floor = (2 * floor) - 1;
                } catch (NumberFormatException numberFormatException) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Make sure floor is a numeric value!");
                }

                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("BMP Floor Boundary", "bmp");
                chooser.setFileFilter(filter);

                if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    filepath = chooser.getSelectedFile().getPath();
                }

                try{
                    image = ImageIO.read(new File(filepath));
                    populateImgView();
                } catch (IOException exception){
                    exception.printStackTrace();
                }
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bounds.clear();
                output_ta.setText("");
                image = null;
                imgView_jp.removeAll();
                frame.pack();
            }
        });
    }

    public static void main(String[] args) {
        frame = new JFrame("Engineering Building Bounds Selector");
        frame.setContentPane(new SelectorForm().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void populateImgView(){
        //imgView_jp.setLayout(new GridLayout(20, 20, 0,0));
        //imgView_jp.setMinimumSize(new Dimension(20,20));
        imgView_jp.setLayout(new GridLayout(image.getHeight(), image.getWidth(), 0, 0));

        for (int y = 0; y < image.getHeight(); y++){
            for (int x = 0; x < image.getWidth(); x++){
                PixelPanel panel = new PixelPanel(image.getRGB(x, y), x, y, 2*(Integer.parseInt(floorIn.getText()) - 1));
                panel.addMouseListener(new GroupAddListener(panel));
                imgView_jp.add(panel);
            }
        }

        frame.pack();
    }

    public class PixelPanel extends JPanel{
        Color backgroundColor;
        int x, y, z;
        int id;

        public PixelPanel(int passedColor, int x, int y, int z) {
            backgroundColor = new Color((passedColor & 0xff0000) >> 16, (passedColor & 0xff00) >> 8, passedColor & 0xff);
            this.setBackground(backgroundColor);

            this.x = x;
            this.y = y;

            this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            this.setMinimumSize(new Dimension(1,1));
            this.setPreferredSize(new Dimension(7,7));
            this.setMaximumSize(new Dimension(10,10));
        }

        public Color getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(Color backgroundColor) {
            this.setBackground(backgroundColor);
        }
    }

    public class GroupAddListener extends MouseAdapter{
        private PixelPanel panel;

        public GroupAddListener(PixelPanel panel) {
            this.panel = panel;
        }

        @Override
        public void mousePressed(MouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON1 && panel.getBackgroundColor().equals(Color.white)) {
                if (idIn.getText().isBlank()){
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Make sure room ID has a set value first!");
                    return;
                }

                try {
                    int tempID = Integer.parseInt(idIn.getText());
                    panel.id = tempID;
                } catch  (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Make sure room ID is a numeric value!");
                    return;
                }

                if (!bounds.contains(panel)){
                    bounds.add(panel);
                    updateList();
                    panel.setBackgroundColor(Color.BLUE);
                    panel.repaint();
                }
            } else if (event.getButton() == MouseEvent.BUTTON3 && panel.getBackgroundColor().equals(Color.white)) {

                bounds.remove(panel);
                updateList();

                panel.setBackgroundColor(Color.WHITE);
                panel.repaint();
            }
        }
    }

    private void updateList(){
        bounds.sort(new PanelPriorityComparator());
        output_ta.setText("");
        for (PixelPanel panel : bounds){
            output_ta.append("(" + panel.id + "," + panel.x + "," + panel.y + "," + panel.z + "),\n");
        }
    }
}
