import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class MergeSortClient {
    private static final int[] SIZES = {
        1000, 5000, 10000, 50000, 100000, 500000, 1000000
    };

    private static final int TRIALS = 5;

    public static void main(String[] args) {
        double[] tdMs = new double[SIZES.length];
        double[] buMs = new double[SIZES.length];

        System.out.printf("%-14s %16s %16s%n", "Input Size", "TopDown (ms)", "BottomUp (ms)");
        System.out.println("-".repeat(48));

        Random rand = new Random(42);

        for (int i = 0; i < SIZES.length; i++) {
            tdMs[i] = measureMs(new TopDownMergeSort(), SIZES[i], rand);
            buMs[i] = measureMs(new BottomUpMergeSort(), SIZES[i], rand);
            System.out.printf("%-14d %16.2f %16.2f%n", SIZES[i], tdMs[i], buMs[i]);
        }

        SwingUtilities.invokeLater(() -> showGraph(SIZES, tdMs, buMs));
    }

    private static double measureMs(Object sorter, int n, Random rand) {
        sortWith(sorter, randomArray(rand, n));

        long total = 0;
        for (int t = 0; t < TRIALS; t++) {
            int[] a = randomArray(rand, n);
            long start = System.nanoTime();
            sortWith(sorter, a);
            total += System.nanoTime() - start;
        }
        return (total / (double) TRIALS) / 1_000_000.0;
    }

    private static void sortWith(Object sorter, int[] a) {
        if (sorter instanceof TopDownMergeSort td) {
            td.sort(a);
        } else if (sorter instanceof BottomUpMergeSort bu) {
            bu.sort(a);
        }
    }

    private static int[] randomArray(Random rand, int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = rand.nextInt();
        }
        return a;
    }

    private static void showGraph(int[] sizes, double[] td, double[] bu) {
        JFrame frame = new JFrame("Merge Sort Running-Time Comparison");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new GraphPanel(sizes, td, bu));
        frame.setSize(900, 580);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static class GraphPanel extends JPanel {

        private static final Color COLOR_TD = new Color(0x1565C0);
        private static final Color COLOR_BU = new Color(0xC62828);
        private static final Color GRID = new Color(220, 220, 220);

        private final int[] sizes;
        private final double[] tdMs, buMs;

        GraphPanel(int[] sizes, double[] tdMs, double[] buMs) {
            this.sizes = sizes;
            this.tdMs  = tdMs;
            this.buMs  = buMs;
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            final int PAD_L = 80, PAD_R = 30, PAD_T = 55, PAD_B = 65;
            int plotW = w - PAD_L - PAD_R;
            int plotH = h - PAD_T - PAD_B;

            double maxY = 0;
            for (double v : tdMs) {
                maxY = Math.max(maxY, v);
            }
            for (double v : buMs) {
                maxY = Math.max(maxY, v);
            }
            maxY = Math.ceil(maxY * 1.1);

            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.setColor(Color.BLACK);
            String title = "Top-Down vs Bottom-Up Merge Sort";
            int tw = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, (w - tw) / 2, PAD_T - 22);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            int yTicks = 8;
            for (int i = 0; i <= yTicks; i++) {
                double val = maxY * i / yTicks;
                int y = PAD_T + plotH - (int) (plotH * i / yTicks);

                g2.setColor(GRID);
                g2.setStroke(new BasicStroke(1));
                g2.drawLine(PAD_L, y, PAD_L + plotW, y);

                g2.setColor(Color.DARK_GRAY);
                String label = String.format("%.0f", val);
                int lw = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, PAD_L - lw - 6, y + 4);
            }

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(PAD_L, PAD_T, PAD_L, PAD_T + plotH);
            g2.drawLine(PAD_L, PAD_T + plotH, PAD_L + plotW, PAD_T + plotH);

            int n = sizes.length;
            int[] xCoords = new int[n];
            for (int i = 0; i < n; i++) {
                xCoords[i] = PAD_L + i * plotW / (n - 1);

                g2.setColor(GRID);
                g2.setStroke(new BasicStroke(1));
                g2.drawLine(xCoords[i], PAD_T, xCoords[i], PAD_T + plotH);

                g2.setColor(Color.DARK_GRAY);
                String label = formatSize(sizes[i]);
                int lw = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, xCoords[i] - lw / 2, PAD_T + plotH + 16);
            }

            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(Color.BLACK);

            String xTitle = "Input Size (n)";
            int xtw = g2.getFontMetrics().stringWidth(xTitle);
            g2.drawString(xTitle, PAD_L + (plotW - xtw) / 2, h - 8);

            g2.rotate(-Math.PI / 2);
            String yTitle = "Time (ms)";
            int ytw = g2.getFontMetrics().stringWidth(yTitle);
            g2.drawString(yTitle, -(PAD_T + (plotH + ytw) / 2), 16);
            g2.rotate(Math.PI / 2);

            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawSeries(g2, xCoords, tdMs, maxY, PAD_T, plotH, COLOR_TD);
            drawSeries(g2, xCoords, buMs, maxY, PAD_T, plotH, COLOR_BU);

            drawLegend(g2, PAD_L + 18, PAD_T + 12);
        }

        private void drawSeries(Graphics2D g2, int[] xCoords, double[] vals,
                                double maxY, int padT, int plotH, Color color) {
            g2.setColor(color);
            int[] yCoords = new int[vals.length];
            for (int i = 0; i < vals.length; i++) {
                yCoords[i] = padT + plotH - (int) (plotH * vals[i] / maxY);
            }
            for (int i = 0; i < vals.length - 1; i++) {
                g2.drawLine(xCoords[i], yCoords[i], xCoords[i + 1], yCoords[i + 1]);
            }
            for (int i = 0; i < vals.length; i++) {
                g2.fillOval(xCoords[i] - 4, yCoords[i] - 4, 9, 9);
            }
        }

        private void drawLegend(Graphics2D g2, int x, int y) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            int lineLen = 28, gap = 22;

            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillRoundRect(x - 6, y - 13, 125, 44, 6, 6);
            g2.setColor(new Color(180, 180, 180));
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(x - 6, y - 13, 125, 44, 6, 6);

            g2.setColor(COLOR_TD);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x, y, x + lineLen, y);
            g2.fillOval(x + lineLen / 2 - 4, y - 4, 9, 9);
            g2.setColor(Color.BLACK);
            g2.drawString("Top-Down", x + lineLen + 6, y + 4);

            g2.setColor(COLOR_BU);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x, y + gap, x + lineLen, y + gap);
            g2.fillOval(x + lineLen / 2 - 4, y + gap - 4, 9, 9);
            g2.setColor(Color.BLACK);
            g2.drawString("Bottom-Up", x + lineLen + 6, y + gap + 4);
        }

        private static String formatSize(int n) {
            if (n >= 1_000_000) return (n / 1_000_000) + "M";
            if (n >= 1_000)     return (n / 1_000) + "K";
            return String.valueOf(n);
        }
    }
}