import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import javax.imageio.ImageIO;

/** 実画像やシステムのクリップボードを変更せず、画像保存時のヒープを比較する試験。 */
public class MemoryProbe {
    private static BufferedImage latest;

    public static void main(String[] args) throws Exception {
        int width = Integer.parseInt(args[0]);
        int height = Integer.parseInt(args[1]);
        Path output = Files.createTempFile("instantsnap-memory-probe-", ".png");
        try {
            report("start");
            for (int i = 1; i <= 10; i++) {
                capture(width, height, output);
                report("shot-" + i);
            }
            Thread.sleep(25000);
            report("idle-25s");
            if (latest.getWidth() != width) {
                throw new AssertionError("latest image lost");
            }
        } finally {
            Files.deleteIfExists(output);
        }
    }

    private static void capture(int width, int height, Path output) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        Random random = new Random(42);
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = random.nextInt(0x1000000);
        }
        if (!ImageIO.write(image, "png", output.toFile())) {
            throw new AssertionError("PNG writer missing");
        }
        // クリップボードが最新画像を保持する状況だけを再現。OSの変換処理は含まない。
        latest = image;
    }

    private static void report(String phase) {
        var heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        System.out.printf("%s used=%.1fMiB committed=%.1fMiB max=%.1fMiB%n",
                phase, heap.getUsed() / 1048576.0, heap.getCommitted() / 1048576.0,
                heap.getMax() / 1048576.0);
    }
}
