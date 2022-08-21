import opennlp.tools.stemmer.PorterStemmer;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class SearchEngineJava {
    private final HashMap<String, HashSet<String>> invertedIndex = new HashMap<>();

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

        HashSet<String> wordsWithoutPrefix = filterSet(allWords, word -> !word.startsWith("+") && !word.startsWith("-"));
        includeAllTogether(finalFoundedFiles, wordsWithoutPrefix);

        HashSet<String> wordsWithPlusPrefix = filterSet(allWords, word -> word.startsWith("+"));
        includeAll(finalFoundedFiles, wordsWithPlusPrefix);

        HashSet<String> wordsWithMinusPrefix = filterSet(allWords, word -> word.startsWith("-"));
        excludeAll(finalFoundedFiles, wordsWithMinusPrefix);

        return finalFoundedFiles;
    }

    private HashSet<String> filterSet(String[] set, Predicate<String> predicate) {
        HashSet<String> resultSet = new HashSet<>();
        for (String word : set) {
            if (predicate.test(word))
                resultSet.add(word);
        }
        return resultSet;
    }

    private void includeAllTogether(HashSet<String> resultFile, HashSet<String> words) {
        for (String word : words) {
            HashSet<String> foundedFiles = searchSingleWord(word);
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
            HashSet<String> foundedFiles = searchSingleWord(word);
            resultFile.addAll(foundedFiles);
        }
    }

    private void excludeAll(HashSet<String> resultFile, HashSet<String> words) {
        for (String word : words) {
            HashSet<String> foundedFiles = searchSingleWord(word);
            foundedFiles.forEach(resultFile::remove);
        }
    }

    private HashSet<String> searchSingleWord(String query) {
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
                HashSet<String> documents = invertedIndex.get(cleanedWord);
                if (documents != null)
                    documents.add(documentName);
                else {
                    documents = new HashSet<>();
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

    private boolean isNotLetter(char c) {
        return !((c >= 65 && c <= 90) || (c >= 97 && c <= 122));
    }

    private String dropLsatNoise(String word) {
        int index = word.length() - 1;
        while (index != -1 && isNotLetter(word.charAt(index)))
            index--;
        return word.substring(0, index + 1);
    }

    private String dropFirstNoise(String word){
        int index = 0;
        while (index != word.length() && isNotLetter(word.charAt(index)))
            index++;
        return word.substring(index);
    }
}