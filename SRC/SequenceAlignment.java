import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

class ScoreNode {
    double i; char c; boolean isOpened;
    public ScoreNode(double i, char c, boolean isOpened) {
        this.i = i; this.c = c; this.isOpened = isOpened;
    }
}

public class SequenceAlignment {
    static final double MATCH_SCORE = 3;
    private static final double MISMATCH_SCORE = -1;
    private static final double GAP_OPENING_PENALTY = -1;
    private static final double GAP_EXTENSION_PENALTY = -0.5;
    public static ScoreNode[][] align(char[] sequence1, char[] sequence2) {

        int m = sequence1.length;
        int n = sequence2.length;

        // Initialize the score matrix (start node and first GAP_OPENING_PENALTY)
        ScoreNode[][] score = new ScoreNode[m + 1][n + 1];
        score[0][0] = new ScoreNode(0.0, '-', false);
        score[0][1] = new ScoreNode(-1.0,'l', true);
        score[1][0] = new ScoreNode(-1.0, 'u', true);


        // Initialize first column (from up to down)
        for (int i = 1; i < m; i++) {
            score[i+1][0] = new ScoreNode(score[i][0].i + GAP_EXTENSION_PENALTY, 'u', true); //  => 'u' means up
        }

        // Initialize first row (from left to right)
        for (int j = 1; j < n; j++) {
            score[0][j+1] = new ScoreNode(score[0][j].i + GAP_EXTENSION_PENALTY, 'l', true); //  => 'l' means left
        }

        // Fill in the score matrix
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {

                // Calculate score for match or mismatch
                double match = (score[i - 1][j - 1].i + (sequence1[i - 1] == sequence2[j - 1] ? MATCH_SCORE : MISMATCH_SCORE));

                // Calculate score for deletion. Select GAP_EXTENSION_PENALTY if there was an GAP_OPENING_PENALTY before.
                double delete = (!score[i - 1][j].isOpened) ? score[i-1][j].i + GAP_OPENING_PENALTY : score[i-1][j].i + GAP_EXTENSION_PENALTY;

                // Calculate score for insertion. Select GAP_EXTENSION_PENALTY if there was an GAP_OPENING_PENALTY before.
                double insert = (!score[i][j - 1].isOpened) ? score[i][j-1].i + GAP_OPENING_PENALTY : score[i][j-1].i + GAP_EXTENSION_PENALTY;

                // Choose the maximum score to be placed on the next node.
                double nextScore = Math.max(Math.max(match, delete), insert);

                // Find if there is GAP_OPENING_PENALTY before.
                boolean isOpened = match != nextScore || score[i - 1][j - 1].isOpened;

                // Movement letter for next node.
                char nextMovement = (match == nextScore) ? 'x' : (delete == nextScore) ? 'u' : 'l';

                // Create new node and place on the score matrix.
                score[i][j] = new ScoreNode(nextScore, nextMovement, isOpened);
            }
        }

        return score;
    }

    public static void main(String[] args) {

        String directoryPath = "src/Test_Inputs/";
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        assert files != null;
        Arrays.sort(files, Comparator.comparing(File::getName));

        for (File file : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

                // First line is sequence1.
                char[] sequence1 = reader.readLine().toCharArray();

                // Second line is sequence1.
                char[] sequence2 = reader.readLine().toCharArray();

                // Align two sequences and find optimum scores.
                ScoreNode[][] score = align(sequence1, sequence2);
                String[] alignment = findAlignment(sequence1, sequence2, score);

                // Alignment Result
                System.out.println("\n## "+file.getName()+":");
                System.out.println(alignment[0]);
                System.out.println(alignment[1]);

                // Bottom-right element of the score matrix is the optimal alignment score.
                System.out.println("Optimal alignment score: " + score[sequence1.length][sequence2.length].i);
                System.out.println("\n-------------");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static String[] findAlignment(char[] sequence1, char[] sequence2, ScoreNode[][] score) {

        int x = sequence1.length;
        int y = sequence2.length;

        // Root node
        ScoreNode node = score[x][y];

        StringBuilder align1 = new StringBuilder();
        StringBuilder align2 = new StringBuilder();

        while (node.c != '-'){
            switch (node.c) {
                case 'l' -> {
                    y--;
                    align1.insert(0, "-");
                    align2.insert(0, sequence2[y]);
                    node = score[x][y];
                }
                case 'u' -> {
                    x--;
                    align2.insert(0, "-");
                    align1.insert(0, sequence1[x]);
                    node = score[x][y];
                }
                case 'x' -> {
                    x--;
                    y--;
                    align1.insert(0, sequence1[x]);
                    align2.insert(0, sequence2[y]);
                    node = score[x][y];
                }
                default -> {
                }
            }
        }

        return new String[]{align1.toString(), align2.toString()};
    }
}