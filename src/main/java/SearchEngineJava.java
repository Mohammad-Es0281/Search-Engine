import opennlp.tools.stemmer.PorterStemmer;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class SearchEngineJava {
    private final HashMap<String, LinkedList<String>> invertedIndex = new HashMap<>();

    private final PorterStemmer stemmer = new PorterStemmer();

    public SearchEngineJava(File[] sourceFiles) {
        addSourceFiles(sourceFiles);
    }

    public SearchEngineJava(File sourceFile) {
        addSourceFile(sourceFile);
    }

    // \\s+ regular expression will remove all whitespaces
    public HashSet<String> search(String query) {
        String[] allWords = query.split("\\s+");
        HashSet<String> finalFoundedFiles = new HashSet<>();

        HashSet<String> wordsWithoutPrefix = new HashSet<>(Arrays.stream(allWords).filter(word -> !word.startsWith("+") && !word.startsWith("-")).toList());
        includeAllTogether(finalFoundedFiles, wordsWithoutPrefix);

        HashSet<String> wordsWithPlusPrefix = new HashSet<>(Arrays.stream(allWords).filter(word -> word.startsWith("+")).toList());
        includeAll(finalFoundedFiles, wordsWithPlusPrefix);

        HashSet<String> wordsWithMinusPrefix = new HashSet<>(Arrays.stream(allWords).filter(word -> word.startsWith("-")).toList());
        excludeAll(finalFoundedFiles, wordsWithMinusPrefix);

        return finalFoundedFiles;
    }

    private void includeAllTogether(HashSet<String> resultFile, HashSet<String> words) {
        for (String word : words) {
            LinkedList<String> foundedFiles = searchSingleWord(word);
            if (foundedFiles != null) {
                if (resultFile.isEmpty())
                    resultFile.addAll(foundedFiles);
                else
                    resultFile.retainAll(foundedFiles);
            }
        }
    }

    private void includeAll(HashSet<String> resultFile, HashSet<String> words) {
        for (String word : words) {
            LinkedList<String> foundedFiles = searchSingleWord(word);
            resultFile.addAll(foundedFiles);
        }
    }

    private void excludeAll(HashSet<String> resultFile, HashSet<String> words) {
        for (String word : words) {
            LinkedList<String> foundedFiles = searchSingleWord(word);
            foundedFiles.forEach(resultFile::remove);
        }
    }

    private LinkedList<String> searchSingleWord(String query) {
        return invertedIndex.getOrDefault(cleanWord(query), null);
    }

    public void addSourceFiles(File[] sourceFiles) {
        for (File sourceFile : sourceFiles)
            addSourceFile(sourceFile);
    }

    public void addSourceFile(File sourceFile) {
        try (Stream<String> stream = Files.lines(Paths.get((sourceFile.getPath())))) {
            stream.forEach(line -> record(line, sourceFile.getName()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void record(String text, String documentName) {
        for (String word : text.split("\\s+")) {
            String cleanedWord = cleanWord(word);
            if (cleanedWord != null) {
                LinkedList<String> documents = invertedIndex.get(cleanedWord);
                if (documents != null)
                    documents.add(documentName);
                else {
                    documents = new LinkedList<>();
                    documents.add(documentName);
                    invertedIndex.put(cleanedWord, documents);
                }
            }
        }
    }

    private String cleanWord(String word) {
        return stemmer.stem(removeWordNoise(word.toLowerCase()));
    }

    // Noise = nonLetter characters like parenthesis, dots or anything else.
    private String removeWordNoise(String word) {
        return dropFirstNoise(dropLsatNoise(word));
    }

    private final Predicate<Character> isNotLetter = character -> !((character >= 65 && character <= 90) || (character >= 97 && character <= 122));

    private String dropLsatNoise(String word) {
        int index = word.length() - 1;
        while (index != -1 && isNotLetter.test(word.charAt(index)))
            index--;
        return word.substring(0, index + 1);
    }

    private String dropFirstNoise(String word){
        int index = 0;
        while (index != word.length() && isNotLetter.test(word.charAt(index)))
            index++;
        return word.substring(index);
    }
}