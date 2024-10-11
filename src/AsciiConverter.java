import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.Toolkit;
import java.io.File;
import javax.imageio.ImageIO;

public class AsciiConverter extends JFrame
{
    private JTextArea outputArea;
    private JButton loadButton;
    private final int MAX_WIDTH;  // Максимальная ширина изображения
    private final int MAX_HEIGHT; // Максимальная высота изображения

    public AsciiConverter()
    {
        // Получаем разрешение экрана
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        MAX_WIDTH = screenSize.width - 100;  // Рамки окна
        MAX_HEIGHT = screenSize.height - 200;

        setTitle("AsciiConverter");
        setSize(screenSize.width / 2, screenSize.height / 2);  // Размер окна в половину экрана
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // При закрытии приложения завершать работу программы
        setLocationRelativeTo(null); // Расположение окна приложения по центру монитора
        setLayout(new BorderLayout());

        outputArea = new JTextArea();
        // Настраиваем размер шрифта в зависимости от ширины экрана
        int fontSize = calculateFontSize(screenSize.width);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, fontSize));
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);  // Включаем перенос строк
        outputArea.setWrapStyleWord(true);  // Перенос по словам
        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane, BorderLayout.CENTER);

        loadButton = new JButton("Загрузить изображение");
        loadButton.addActionListener(new LoadImageAction());
        add(loadButton, BorderLayout.SOUTH);
    }

    // Метод для вычисления размера шрифта в зависимости от ширины экрана
    private int calculateFontSize(int screenWidth)
    {
        if (screenWidth >= 1920)
        {
            return 5;  // Для экранов с FullHD и выше
        } else if (screenWidth >= 1366)
        {
            return 7;  // Для экранов HD
        } else
        {
            return 9;  // Для маленьких экранов
        }
    }

    private class LoadImageAction implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION)
            {
                File selectedFile = fileChooser.getSelectedFile();
                convertImageToAscii(selectedFile);
            }
        }
    }

    private void convertImageToAscii(File imageFile)
    {
        try
        {
            BufferedImage originalImage = ImageIO.read(imageFile);

            // Масштабируем изображение под экран ноутбука
            BufferedImage scaledImage = scaleImage(originalImage, MAX_WIDTH, MAX_HEIGHT);

            StringBuilder asciiArt = new StringBuilder();

            // Массив символов для использования конвертации
            char[] asciiChars = {'*', '@', '#', '?', '$', '&', '.', ',', '\\', '/'};
            int width = scaledImage.getWidth();
            int height = scaledImage.getHeight();

            // Рассчитаем ширину окна для центрирования строк
            int consoleWidth = outputArea.getColumns() > 0 ? outputArea.getColumns() : MAX_WIDTH / 10;

            // Преобразуем каждый пиксель в соответствующий символ и добавляем выравнивание по центру
            for (int y = 0; y < height; y++)
            {
                StringBuilder line = new StringBuilder();
                for (int x = 0; x < width; x++)
                {
                    Color color = new Color(scaledImage.getRGB(x, y));
                    int grayValue = (int) (0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue());
                    int index = (int) (grayValue / 255.0 * (asciiChars.length - 1));
                    line.append(asciiChars[index]);
                }
                // Центрируем строку
                asciiArt.append(centerLine(line.toString(), consoleWidth)).append("\n");
            }

            outputArea.setText(asciiArt.toString());
        } catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки изображения: " + ex.getMessage());
        }
    }

    // Центрирование строки относительно ширины области вывода
    private String centerLine(String line, int totalWidth)
    {
        int padding = (totalWidth - line.length()) / 2; // Рассчитываем количество пробелов для центрирования
        if (padding > 0)
        {
            return " ".repeat(padding) + line; // Добавляем пробелы перед строкой
        } else
        {
            return line; // Если строка длиннее, чем ширина, выводим как есть
        }
    }

    private BufferedImage scaleImage(BufferedImage originalImage, int maxWidth, int maxHeight)
    {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // Рассчитываем соотношение сторон
        double aspectRatio = (double) width / height;

        // Определяем новые размеры изображения с учетом максимальных значений
        if (width > maxWidth || height > maxHeight)
        {
            if (width > maxWidth)
            {
                width = maxWidth;
                height = (int) (width / aspectRatio);
            }

            if (height > maxHeight)
            {
                height = maxHeight;
                width = (int) (height * aspectRatio);
            }
        }

        // Масштабируем изображение до новых размеров
        Image scaled = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage bufferedScaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedScaledImage.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();

        return bufferedScaledImage;
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            AsciiConverter converter = new AsciiConverter();
            converter.setVisible(true); // Сделать окно видимым при запуске программы
        });
    }
}
