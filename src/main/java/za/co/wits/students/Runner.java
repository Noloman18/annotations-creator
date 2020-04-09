package za.co.wits.students;

import org.w3c.dom.css.Rect;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Runner {

    private BufferedImage getImage(Path baseFolder) throws IOException {

        var list = Files.list(baseFolder).collect(Collectors.toList());
        Collections.shuffle(list);

        BufferedImage image = null;
        try (var input = Files.newInputStream(list.get(0))) {
            image = ImageIO.read(input);
        }

        return image;
    }

    private void boundingBoxAnnotations(Path imageBaseFolder) throws IOException {
        var canvas = new JPanel() {
            private Point clickPoint = null;
            private Point mouseLocation = null;
            private Rectangle lastRectangle = null;
            private List<Rectangle> rectangleList = new ArrayList<>();

            BufferedImage img = null;

            void toggleClick(Point point) {
                if (this.clickPoint == null) {
                    this.clickPoint = point;
                } else {
                    this.clickPoint = null;
                    rectangleList.add(lastRectangle);
                }
            }

            void setImg(BufferedImage img) {
                this.img = img;
                this.setSize(this.img.getWidth(), this.img.getHeight());
                rectangleList.clear();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(img, 0, 0, this);

                rectangleList.forEach(item-> {
                    g.setColor(Color.MAGENTA);
                    g.drawRect(item.x, item.y, item.width, item.height);
                });

                if (clickPoint != null) {
                    int width = Math.abs(clickPoint.x - mouseLocation.x);
                    int height = Math.abs(clickPoint.y - mouseLocation.y);
                    int x = Math.min(clickPoint.x, mouseLocation.x);
                    int y = Math.min(clickPoint.y, mouseLocation.y);

                    g.setColor(Color.MAGENTA);
                    g.drawRect(x, y, width, height);

                    lastRectangle = new Rectangle(x,y,width,height);
                }

            }
        };

        var image = getImage(imageBaseFolder);
        canvas.setImg(image);

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                canvas.mouseLocation = new Point(e.getX(), e.getY());
                canvas.repaint();
            }
        });

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                canvas.toggleClick(new Point(e.getX(), e.getY()));
            }
        });

        var button = new JButton();
        button.setText("Next image");
        button.setSize(image.getWidth(), 30);

        var frame = new JFrame();
        frame.setSize(image.getWidth(), image.getHeight() + 30);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Show random image");
        frame.add(canvas, BorderLayout.CENTER);
        frame.add(button, BorderLayout.SOUTH);
        frame.setVisible(true);

        button.addActionListener(event -> {
            try {
                var img = getImage(imageBaseFolder);
                canvas.setImg(img);
                button.setSize(img.getWidth(), 30);
                frame.setSize(img.getWidth(), img.getHeight() + 30);
                frame.repaint();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    public static void main(String[] args) throws Exception {

        Runner runner = new Runner();
        runner.boundingBoxAnnotations(Paths.get("D:\\university\\datasets\\Kangaroo\\kangaroo\\images"));
    }
}
