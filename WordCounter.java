import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WordCounter {

    public static final Set<String> EXCLUSIONS = new HashSet<>();

    static {
        // Prepositions
        String[] prepositions = {"in", "on", "at", "by", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "over", "under", "as", "of"};

        // Pronouns
        String[] pronouns = {"he", "she", "it", "they", "them", "him", "her", "we", "us", "you", "i", "me", "his", "all", "this", "there"};

        // Conjunctions
        String[] conjunctions = {"and", "or", "but", "nor", "so", "yet", "for", "as", "that"};

        // Articles
        String[] articles = {"the", "a", "an"};

        // Modal verbs and their forms
        String[] modals = {"is", "was", "am", "are", "were", "be", "been", "being"};

        // Add all words to the HashSet
        for (String word : prepositions) EXCLUSIONS.add(word);
        for (String word : pronouns) EXCLUSIONS.add(word);
        for (String word : conjunctions) EXCLUSIONS.add(word);
        for (String word : articles) EXCLUSIONS.add(word);
        for (String word : modals) EXCLUSIONS.add(word);
    }


    public static void main(String[] args) {
        // Step 01: Get the urlString to get the text file to analyze
        String urlString = "https://courses.cs.washington.edu/courses/cse390c/22sp/lectures/moby.txt";
        int topXWords = 5;
        int limitSorted = 50;

        try {
            long startTime = System.currentTimeMillis();

            // Step 02: Get the file and return a BufferedReader : reader to process further
            BufferedReader reader = getReaderFromURL(urlString);

            // Step 03: Process the words and prepare result map
            Map<String, Object> result = countValidWords(reader, topXWords, limitSorted);

            //Step 04: Print values from the result map
            printResult(result);

            // Close the reader
            reader.close();

            // Log execution time
            long endTime = System.currentTimeMillis();
            double duration = endTime - startTime;
            System.out.println("Execution time: " + duration + " seconds");
        } catch (IOException e) {
            System.err.println("An error occurred while reading from the URL or processing the data: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Method to return a BufferedReader from a URL string
     * @param urlString
     * @return
     * @throws Exception
     */
    public static BufferedReader getReaderFromURL(String urlString) throws Exception {
        // Create URL object
        URL url = new URL(urlString);
        
        // Open connection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // Return the BufferedReader to read from the URL
        return new BufferedReader(new InputStreamReader(conn.getInputStream()));
    }

    // Method to count total words in BufferedReader excluding words in EXCLUSIONS
    public static Map<String, Object> countValidWords(BufferedReader reader, int topXWords, int limitSorted) throws IOException {
        Map<String, Integer> wordCounts = new HashMap<>();
        int totalCount = 0;
        String line;

        while ((line = reader.readLine()) != null) {
            String[] words = line.toLowerCase().replaceAll("[^a-zA-Z ]", "").split("\\s+");
    
            for (String word : words) {
                if (!word.isEmpty()) {
                    word = removePluralPostfix(word);

                    // Check if word is excluded, if not, add to wordCounts
                    if (!isExcluded(word)) {
                        wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
                        totalCount++;
                    }
                }
            }
        }

        // Get the top x frequent words
        Map<String, Integer> top = getTopXFrequentWords(wordCounts, topXWords);

        // Get the top 50 alphabetically sorted unique words
        List<String> topAlphabeticallySorted = getTopXAlphabeticallySortedWords(wordCounts, limitSorted);


        // Return a map containing both total count and top 5 frequent words
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("top", top);
        result.put("topAlphabeticallySorted", topAlphabeticallySorted);
        return result;
    }

    /**
     * Method to get top X frequent words from the map
     * @param wordCounts
     * @param X
     * @return
     */
    public static Map<String, Integer> getTopXFrequentWords(Map<String, Integer> wordCounts, int X) {
        // Sort the words by frequency in descending order
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(wordCounts.entrySet());
        sortedList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        Map<String, Integer> topX = new LinkedHashMap<>();
        for (int i = 0; i < X && i < sortedList.size(); i++) {
            Map.Entry<String, Integer> entry = sortedList.get(i);
            topX.put(entry.getKey(), entry.getValue());
        }

        return topX;
    }

    /**
     * Method to check if the word
     * is excluded word or not
     * @param word
     * @return
     */
    public static boolean isExcluded(String word) {
        return EXCLUSIONS.contains(word.toLowerCase());
    }

    /**
     * Method to remove
     * plural postfix ('s)
     * @param word
     * @return
     */
    public static String removePluralPostfix(String word) {
        if (word.endsWith("'s")) {
            return word.substring(0, word.length() - 2);
        }
        return word;
    }

    /**
     * Method to get the top X alphabetically sorted unique words
     * @param wordCounts
     * @param X
     * @return
     */
    public static List<String> getTopXAlphabeticallySortedWords(Map<String, Integer> wordCounts, int X) {
        List<String> sortedWords = new ArrayList<>(wordCounts.keySet());
        Collections.sort(sortedWords);

        return sortedWords.subList(0, Math.min(X, sortedWords.size()));
    }

    /**
     * Method to print beautified result
     * @param result
     */
    public static void printResult(Map<String, Object> result) {
        // Print the total valid words in a table-like structure
        System.out.println("+------------------------------------------------------------+");
        System.out.println("| Total valid words (excluding exclusions and plural forms): |");
        System.out.println("+------------------------------------------------------------+");
        System.out.println(String.format("| %-58s |", result.get("totalCount")));
        System.out.println("+------------------------------------------------------------+");
        System.out.println("");

        // Print the top frequent words in a table-like structure
        Map<String, Integer> topWords = (Map<String, Integer>) result.get("top");
        System.out.println("+----------------------------------------------------------------------+");
        System.out.println("| Top 5 most frequent words with counts (excluding the filtered words) |");
        System.out.println("+-------------------------------------------+--------------------------+");
        System.out.println("| Word                                      | Count                    |");
        System.out.println("+-------------------------------------------+--------------------------+");
        topWords.forEach((word, count) -> {
            System.out.println(String.format("| %-41s | %-24d |", word, count));
        });
        System.out.println("+-------------------------------------------+--------------------------+");
        System.out.println("");

        // Print the top 50 alphabetically sorted unique words in a table-like structure
        List<String> top50Words = (List<String>) result.get("topAlphabeticallySorted");
        System.out.println("+--------------------------------------------+");
        System.out.println("| Top 50 Alphabetically Sorted Unique Words: |");
        System.out.println("+--------------------------------------------+");
        top50Words.forEach(word -> {
            System.out.println(String.format("| %-42s |", word));
        });
        System.out.println("+--------------------------------------------+");
    }
    
}