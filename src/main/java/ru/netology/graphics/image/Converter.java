package ru.netology.graphics.image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;

public class Converter implements TextGraphicsConverter {
    TextColorSchema schema = new Schema(new char[]{'#', '$', '@', '%', '*', '+', '-', '\''});
    private int maxWidth = Integer.MAX_VALUE;
    private int maxHeight = Integer.MAX_VALUE;
    private double maxRatio = Double.MAX_VALUE;

    @Override
    public String convert(String url) throws IOException, BadImageSizeException {
        //скачиваем изображение
        BufferedImage img = ImageIO.read(new URL(url));

        int width = img.getWidth();
        int height = img.getHeight();

        double ratio = (double) width / height;

        // если текущее соотношение больше максимального, то выбрасываем исключение
        if (ratio > maxRatio) {
            throw new BadImageSizeException(ratio, maxRatio);
        }

        //высчитываем коэффициент отношения текущих размеров к максимально допустимым
        double ratioFactor = Math.max((double) width / maxWidth, (double) height / maxHeight);

        //если картинка больше допустимых размеров, то уменьшаем её в ratioFactor раз
        int newWidth = ratioFactor > 1 ? (int) (width / ratioFactor) : width;
        int newHeight = ratioFactor > 1 ? (int) (height / ratioFactor) : height;

        // Получаем изображение с новыми размерами
        Image scaledImage = img.getScaledInstance(newWidth, newHeight, BufferedImage.SCALE_SMOOTH);

        // Создаем пустую картинку нужных размеров и сделаем ее черно-белой
        BufferedImage bwImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);

        // Инструмент для рисования на картинке:
        Graphics2D graphics = bwImg.createGraphics();

        // Копируем содержимое суженой картинки на новую:
        graphics.drawImage(scaledImage, 0, 0, null);

        // ImageIO.write(bwImg, "png", new File("out.png"));


        // Теперь давайте пройдёмся по пикселям нашего изображения.
        // Если для рисования мы просили у картинки .createGraphics(),
        // то для прохода по пикселям нам нужен будет этот инструмент:
        WritableRaster bwRaster = bwImg.getRaster();

        int[] rgb = new int[3];
        char[][] charByPixel = new char[newHeight][newWidth];
        for (int h = 0; h < newHeight; h++) {
            for (int w = 0; w < newWidth; w++) {
                int color = bwRaster.getPixel(w, h, rgb)[0];
                char c = schema.convert(color);
                charByPixel[h][w] = c;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int h = 0; h < newHeight; h++) {
            for (int w = 0; w < newWidth; w++) {
                char c = charByPixel[h][w];
                sb.append(c);
                sb.append(c);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    @Override
    public void setMaxWidth(int width) {
        maxWidth = width;
    }

    @Override
    public void setMaxHeight(int height) {
        maxHeight = height;
    }

    @Override
    public void setMaxRatio(double maxRatio) {
        this.maxRatio = maxRatio;
    }

    @Override
    public void setTextColorSchema(TextColorSchema schema) {
        this.schema = schema;
    }
}
