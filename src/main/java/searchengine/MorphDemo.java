package searchengine;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.util.LemmaFinderUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class MorphDemo {
    public static void main(String[] args) throws IOException {
        String word = "минут(";
        LuceneMorphology luceneMorph =
                new RussianLuceneMorphology();
        List<String> wordBaseForms =
                luceneMorph.getNormalForms(word);
        wordBaseForms.forEach(System.out::println);

        List<String> morphForm = luceneMorph.getMorphInfo(word);
        for (String element : morphForm) {
            if (element.contains("ПРЕДЛ")
                    || element.contains("СОЮЗ")
                    || element.contains("МЕЖД")
                    || element.contains("МС")
                    || element.contains("ЧАСТ")
                    || element.length() <= 3) {
                System.out.println("true");
            }
        }

        LemmaFinderUtil lemmaFinderUtil = new LemmaFinderUtil();
        lemmaFinderUtil.collectLemmas("Повторное появление1 леопарда в Осетии позволяет предположить, что леопард постоянно обитает в некоторых районах Северного Кавказа.")
                .entrySet().stream()
                .forEach(s -> System.out.println(s.getKey().toString()
                        + " - " + s.getValue().toString()));

//        URL url = new URL("https://www.playback.ru");
//        System.out.println("uuu=" + url.getPath());

        URL url = new URL("https://www.playback.ru/");
        String path = url.getFile();//.substring(0, url.getFile().lastIndexOf('/'));
        String base = url.getHost();
        System.out.println(base);
    }
}
