package dit.hua.gr;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;
import javax.swing.ImageIcon;
import javax.swing.DefaultListModel;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import gr.hua.dit.oop2.musicplayer.*;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;

public class JukeFrame extends JFrame implements ActionListener {

    static int playlistIndex = 0;
    static int randomIndex;
    static long minutes;
    static long seconds;
    static boolean playNextSongFlag = false; //flag if the next song should start playing
    static boolean loopFlag = false; //flag for loop
    static boolean randomFlag = false; //flag for random

    int[] duration; //array with songs duration in seconds;

    String[][] data = new String[0][0]; //data array for JTable

    String categories[] = {"Name", "Artist", "Album","Year", "Genre", "Duration"};

    DefaultTableModel tableModel;
    JTable table;

    JButton playButton;
    JButton importButton;
    JButton stopButton;
    JButton forwardButton;
    JButton backwardButton;
    JToggleButton randomButton;
    JToggleButton loopButton;

    JFileChooser importFileChooser;

    JProgressBar songProgress;

    DefaultListModel<String> playlist;
    ArrayList<Integer> randomIndexes;
    JScrollPane listScroll;

    InputStream song;

    Player p = PlayerFactory.getPlayer();

    JukeFrame() {

        playlist = new DefaultListModel<>();

        seconds = 0;

        ImageIcon logoIcon = new ImageIcon("classes/logo.png");
        ImageIcon playIcon = new ImageIcon(new ImageIcon("classes/play.png").getImage().getScaledInstance(35, 35, Image.SCALE_DEFAULT)); //resizes icon
        ImageIcon stopIcon = new ImageIcon(new ImageIcon("classes/stop.png").getImage().getScaledInstance(28, 28, Image.SCALE_DEFAULT));
        ImageIcon forwardIcon = new ImageIcon(new ImageIcon("classes/for.png").getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT));
        ImageIcon backwardIcon = new ImageIcon(new ImageIcon("classes/back.png").getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT));
        ImageIcon randomIcon = new ImageIcon(new ImageIcon("classes/shuffle.png").getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
        ImageIcon loopIcon = new ImageIcon(new ImageIcon("classes/loop.png").getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
        ImageIcon pauseIcon = new ImageIcon(new ImageIcon("classes/pause.png").getImage().getScaledInstance(35, 35, Image.SCALE_DEFAULT));
        Image backgroundImage = Toolkit.getDefaultToolkit().getImage("classes/background.png");

        JPanel importPanel = new JPanel();
        JPanel listPanel = new JPanel();
        JPanel functionsPanel = new JPanel();

        tableModel = new DefaultTableModel(data, categories);

        table = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.getTableHeader().setReorderingAllowed(false);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 ) {
                    playNextSongFlag = false;
                    if (p.getStatus().equals(Player.Status.PLAYING) || p.getStatus().equals(Player.Status.PAUSED)) {
                        p.stop();
                    }
                    playlistIndex = table.getSelectedRow();
                    songPlay(playlistIndex);
                    playNextSongFlag = true;
            }
        } });

        listScroll = new JScrollPane();
        listScroll.setBounds(0, 0, 700, 300);
        listScroll.setViewportView(table);

        importButton = new JButton();
        playButton = new JButton();
        stopButton = new JButton();
        forwardButton = new JButton();
        backwardButton = new JButton();
        randomButton = new JToggleButton();
        loopButton = new JToggleButton();

        songProgress = new JProgressBar();
        songProgress.setMinimum(0);
        songProgress.setBounds(173, 120, 614, 30);
        songProgress.setForeground(new Color(0x086a7f));
        songProgress.setStringPainted(true);
        songProgress.setString("0:00/0:00");



        importButton.setText("Import List");
        importButton.setFont(new Font("Calibri", Font.PLAIN, 25));
        importButton.setFocusPainted(false);
        importButton.setBounds(0, 0, 300, 100);
        importButton.addActionListener(e -> importButtonPressed());

        randomButton.setIcon(randomIcon);
        randomButton.setFocusPainted(false);
        randomButton.setBounds(334, 55, 35, 35);
        randomButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (randomButton.isSelected()) {
                    randomFlag = true;
                    randomIndexes = new ArrayList();
                    randomIndexes.add(playlistIndex);
                } else {
                    randomFlag = false;
                    playlistIndex = randomIndex;
                    randomIndexes.clear();
                }
            }
        });

        loopButton.setIcon(loopIcon);
        loopButton.setFocusPainted(false);
        loopButton.setBounds(650, 55, 35, 35);
        loopButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (loopButton.isSelected()) {
                    loopFlag = true;
                } else {
                    loopFlag = false;
                }
            }
        });

        playButton.setFocusPainted(false);
        playButton.setBounds(452, 40,60, 60);
        playButton.addActionListener(e -> playButtonPressed());
        playButton.setIcon(playIcon);
        p.addPlayerListener(new PlayerListener() {
            @Override
            public void statusUpdated(PlayerEvent playerEvent) {


                if (playerEvent.getStatus().equals(Player.Status.IDLE) || playerEvent.getStatus().equals(Player.Status.PAUSED)){
                    playButton.setIcon(playIcon);

                } else {
                    playButton.setIcon(pauseIcon);
                }

                if (playerEvent.getStatus().equals(Player.Status.IDLE)) {
                    if (playNextSongFlag) {
                        if (loopFlag) {
                            songPlay(playlistIndex);
                        } else {
                            playNext();
                        }
                    }
                }

                System.out.println("Status changed to " + playerEvent.getStatus());
            }

        });

        p.addProgressListener(new ProgressListener() {
            @Override
            public void progress(ProgressEvent progressEvent) {
                long microseconds = progressEvent.getMicroseconds();

                minutes = (microseconds / 1000000) / 60;
                seconds = (microseconds / 1000000) % 60;
                String secondsString = String.format("%02d", seconds);

                songProgress.setValue((int)(microseconds / 1000000));

                if (randomFlag) {
                    songProgress.setString(minutes + ":" + secondsString + "/" + data[randomIndex][5]);
                } else {
                    songProgress.setString(minutes + ":" + secondsString + "/" + data[playlistIndex][5]);
                }
            }
        });


        backwardButton.setIcon(backwardIcon);
        backwardButton.setFocusPainted(false);
        backwardButton.setBounds(382,50,60, 45);
        backwardButton.addActionListener(e -> backwardButtonPressed());

        forwardButton.setIcon(forwardIcon);
        forwardButton.setFocusPainted(false);
        forwardButton.setBounds(522, 50, 60, 45);
        forwardButton.addActionListener(e -> forwardButtonPressed());

        stopButton.setIcon(stopIcon);
        stopButton.setFocusPainted(false);
        stopButton.setBounds(592, 50, 45, 45);
        stopButton.addActionListener(e -> stopButtonPressed());


        importPanel.setBounds(0, 0 , 300, 300);
        importPanel.setLayout(null);
        importPanel.setOpaque(false);
        importPanel.add(importButton);



        listPanel.setBounds(300, 0, 700, 300);
        listPanel.setLayout(null);
        listPanel.setOpaque(false);
        listPanel.add(listScroll);

        functionsPanel.setBounds(0, 300, 1000, 200);
        functionsPanel.setLayout(null);
        functionsPanel.setOpaque(false);
        functionsPanel.add(songProgress);
        functionsPanel.add(backwardButton);
        functionsPanel.add(playButton);
        functionsPanel.add(forwardButton);
        functionsPanel.add(stopButton);
        functionsPanel.add(randomButton);
        functionsPanel.add(loopButton);



        this.setSize(1000, 500);
        this.setTitle("Jukebox");
        this.setIconImage(logoIcon.getImage());
        this.setContentPane(new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, null);
            }
        });
        this.add(importPanel);
        this.add(listPanel);
        this.add(functionsPanel);


        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        importButtonPressed();
        playButtonPressed();
        stopButtonPressed();
        forwardButtonPressed();
        backwardButtonPressed();

    }

    public void importButtonPressed() {
        importFileChooser = new JFileChooser();
        importFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        importFileChooser.setDialogTitle("Import songs list");
        importFileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                } else {
                    String filename = f.getName().toLowerCase();
                    return filename.endsWith(".m3u");
                }
            }

            @Override
            public String getDescription() {
                return "M3U Files (*.m3u)";
            }
        });

        if (importFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File songfile = new File(importFileChooser.getSelectedFile().getAbsolutePath());
            playlist.clear();
            if (songfile.isDirectory()) {
                File[] directoryContent = songfile.listFiles();
                for (int i = 0; i < directoryContent.length; i++) {
                    if (directoryContent[i].getAbsolutePath().endsWith(".mp3")) {
                        playlist.addElement(directoryContent[i].getAbsolutePath());

                    }
                }
            } else if(songfile.getName().endsWith(".m3u")){
                try {
                    M3UToList(songfile);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("This file is not supported. Please select a directory or an m3u file");
            }
                data = new String[playlist.size()][6];
                duration = new int[playlist.size()];
                for (int i = 0; i < playlist.size(); i++) {
                        try {

                            InputStream input = new FileInputStream(new File(playlist.get(i)));
                            File tempfile = new File(playlist.get(i));
                            ContentHandler handler = new DefaultHandler();
                            Metadata metadata = new Metadata();
                            Parser parser = new Mp3Parser();
                            ParseContext parseContext = new ParseContext();
                            parser.parse(input, handler, metadata, parseContext);

                            data[i][0] = tempfile.getName().substring(0, tempfile.getName().lastIndexOf('.')); //saves filename without the path or the extension
                            data[i][1] = metadata.get("xmpDM:artist");
                            data[i][2] = metadata.get("xmpDM:album");
                            data[i][3] = metadata.get("xmpDM:releaseDate");
                            data[i][4] = metadata.get("xmpDM:genre");

                            duration[i] = (int)Float.parseFloat(metadata.get("xmpDM:duration"));
                            int metaseconds = duration[i] % 60;
                            int metaminutes = duration[i] / 60;

                            data[i][5] =  metaminutes + ":" + String.format("%02d", metaseconds);


                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (SAXException e) {
                            throw new RuntimeException(e);
                        } catch (TikaException e) {
                            throw new RuntimeException(e);
                        }
                }

                tableModel = new DefaultTableModel(data, categories);
                table.setModel(tableModel);
                listScroll.setViewportView(table);



            }

        importFileChooser.setAcceptAllFileFilterUsed(false);
    }

    public void playButtonPressed() {
        if (p.getStatus().equals(Player.Status.PLAYING)) {
            p.pause();
        } else if (p.getStatus().equals(Player.Status.PAUSED)) {
            p.resume();
        }
    }

    public void stopButtonPressed() {
        playNextSongFlag = false;
        p.stop();
    }

    public void forwardButtonPressed() {
        stopButtonPressed();
        playNext();
        playNextSongFlag = true;
    }

    public void backwardButtonPressed() {
        stopButtonPressed();
        if (randomFlag) {
            songPlay(randomIndex);
        } else {
            songPlay(playlistIndex);
        }
        playNextSongFlag = true;


    }

    public void M3UToList(File M3UFile) throws FileNotFoundException {
        Scanner s = new Scanner(M3UFile);
        while (s.hasNextLine()) {
            String data = s.nextLine();

            if (!(data.isBlank())) {
                if (!(data.startsWith("#"))) { // For Extended M3U
                    if (data.endsWith(".mp3")) {
                        File f = new File(data);
                        if (f.exists()) {
                            playlist.addElement(data);
                        }
                    }
                }
            }

        }
        s.close();
    }

    public void playNext() {
        if (randomFlag) {
            songPlay(randomizer());
        } else {
            if (playlistIndex < (playlist.size() - 1)) {
                playlistIndex++;

            } else {
                playlistIndex = 0;
            }

            songPlay(playlistIndex);

        }

    }

    public void songPlay(int index) {
        songProgress.setMaximum(duration[index]);
        try {
            song = new FileInputStream(playlist.get(index));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            p.startPlaying(song);
        } catch (PlayerException e) {
            throw new RuntimeException(e);
        }
    }

    public int randomizer() {
        Random r = new Random();
        do {
            if (playlist.size() == randomIndexes.size()) {
                randomIndexes.clear();
            }
            randomIndex = r.nextInt(playlist.size());
        } while (randomIndexes.contains(randomIndex));

        randomIndexes.add(randomIndex);

        return randomIndex;
    }
}


