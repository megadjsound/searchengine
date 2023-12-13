package searchengine.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Component
public class LemmaFinderUtil {
    private static RussianLuceneMorphology russianLuceneMorphology;

    static {
        try {
            russianLuceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
        }
    }


    public HashMap<String, Integer> collectLemmas(String text) {
        text = prepareText(text);
        HashMap<String, Integer> lemmaList = new HashMap<>();

        String[] elements = text.split("\\s+");
        for (String element : elements) {
            List<String> wordsList = getLemma(element);
            for (String word : wordsList) {
                int count = lemmaList.getOrDefault(word, 0);
                lemmaList.put(word, count + 1);
            }
        }
        return lemmaList;
    }

    public List<String> getLemma(String word) {
        List<String> lemmaList = new ArrayList<>();
        try {
            List<String> lemmaForms = russianLuceneMorphology.getNormalForms(word);
            if (!isServiceWord(word) && !word.isEmpty() && (word.length() > 1)) {
                lemmaList.addAll(lemmaForms);
            }
            //}
        } catch (Exception e) {
        }
        return lemmaList;
    }

    public List<Integer> findLemmaIndexInText(String text, String lemma) {
        List<Integer> lemmaIndexList = new ArrayList<>();
        //String[] elements = arrayContainsRussianWords(text);
        String[] elements = text.toLowerCase(Locale.ROOT).split("\\p{Punct}|\\s");
        int index = 0;
        for (String element : elements) {
            List<String> lemmas = getLemma(element);
            for (String lem : lemmas) {
                if (lem.equals(lemma)) {
                    lemmaIndexList.add(index);
                }
            }
            index += element.length() + 1;
        }
        return lemmaIndexList;
    }

    private String prepareText(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim();
    }

    private boolean isServiceWord(String word) {
        List<String> morphForm = russianLuceneMorphology.getMorphInfo(word);
        for (String element : morphForm) {
            if (element.contains("ПРЕДЛ")
                    || element.contains("СОЮЗ")
                    || element.contains("МЕЖД")
                    || element.contains("МС")
                    || element.contains("ЧАСТ")
                    || element.length() <= 3) {
                return true;
            }
        }
        return false;
    }

    public String[] arrayContainsRussianWords(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }

    public String removeHtmlTags(String html) {
        Pattern pattern = Pattern.compile("<[^>]*>");
        Matcher matcher = pattern.matcher(html);
        String plainText = matcher.replaceAll("");
        return plainText;
    }
}
