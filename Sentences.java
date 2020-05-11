package lingpipe;

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.ModifiedTokenizerFactory;
import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.StopTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;

import com.aliasi.util.Files;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.util.regex.Pattern;
import java.util.Arrays;

import java.io.*;

import java.util.TreeMap;
import edu.sussex.nlp.jws.*;
import java.text.DecimalFormat;

import edu.smu.tspell.wordnet.*;
import rita.wordnet.RiWordnet;


class Sentences {

    static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
    static final SentenceModel SENTENCE_MODEL = new MedlineSentenceModel();
    StopTokenizerFactory st;
    File file;
    String text, filename;
    List<String> tokenList, whiteList, tokenListStp, whiteListStp, tokenListStm, whiteListStm;
    String[] tokens, whites, tokenStp, whiteStp, tokenStem, whiteStem;
    String[] sen1;//mod
    //mod
    String[] words, pos1;
    Tokenizer tokenizer, tokenizerStp, tokenizerStem;
    int sentBoundaries[], stpBoundaries[], stemBoundaries[];
    Set<String> stpSet;
    String file_name;
    BufferedReader br;

    //  Method Init- Reads Input document for summarization
    public Sentences() {
    }

    public Sentences(String file_name) {
        System.out.println("HI");
        br = new BufferedReader(new InputStreamReader(System.in));
        this.file_name = file_name;
        file = new File(file_name);
        try {
            text = Files.readFromFile(file, "ISO-8859-1");
        } catch (IOException ex) {
            Logger.getLogger(SentenceBoundaryDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("INPUT TEXT: ");
        System.out.println(text);
        try {
            this.segmentation();
            this.stopWordsRemoval();
        } catch (IOException ex) {
            Logger.getLogger(Sentences.class.getName()).log(Level.SEVERE, null, ex);
        }

        //this.stemming();
        // this.stan_pos();
    }

    // Segmentation - Segments the input text by finding sentence boundaries
    public void segmentation() throws IOException {

        tokenList = new ArrayList<String>();
        whiteList = new ArrayList<String>();
        tokenizer = TOKENIZER_FACTORY.tokenizer(text.toCharArray(), 0, text.length());
        tokenizer.tokenize(tokenList, whiteList);

        System.out.println(tokenList.size() + " TOKENS");
        System.out.println(whiteList.size() + " WHITESPACES");

        tokens = new String[tokenList.size()];
        whites = new String[whiteList.size()];
        tokenList.toArray(tokens);
        whiteList.toArray(whites);
        sentBoundaries = SENTENCE_MODEL.boundaryIndices(tokens, whites);

        System.out.println(sentBoundaries.length + " SENTENCE ");
        if (sentBoundaries.length < 1) {
            System.out.println("No sentence boundaries found.");
            return;
        }
        int sentStartTok = 0;
        int sentEndTok = 0;
        System.out.println("     \t *Segmentation*");

        for (int i = 0; i < sentBoundaries.length; ++i) {
            sentEndTok = sentBoundaries[i];
            System.out.println("SENTENCE " + (i) + ": " + sentStartTok + ": " + sentEndTok + ": ");
            for (int j = sentStartTok; j <= sentEndTok; j++) {
                System.out.print(tokens[j] + whites[j + 1]);
            }
            System.out.println();
            sentStartTok = sentEndTok + 1;
        }
    }

    // Stop Words are removed by specifying them in a set.
    public void stopWordsRemoval() throws IOException {
        stpSet = new HashSet<String>();
        String stp_word;
        try {
            RandomAccessFile stp_file = new RandomAccessFile("E:\\Project\\APIs\\APIs Used\\lingpipe\\StopWords.txt", "rw");
            while ((stp_word = stp_file.readLine()) != null) {
                stpSet.add(stp_word);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Sentences.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Sentences.class.getName()).log(Level.SEVERE, null, ex);
        }

        st = new StopTokenizerFactory(TOKENIZER_FACTORY, stpSet);
        tokenizerStp = st.tokenizer(text.toCharArray(), 0, text.length());

        tokenListStp = new ArrayList<String>();
        whiteListStp = new ArrayList<String>();
        tokenizerStp.tokenize(tokenListStp, whiteListStp);

        System.out.println("    \t *Stop Words Removal *");

        System.out.println(tokenListStp.size() + " TOKENS***");
        System.out.println(whiteListStp.size() + " WHITESPACES***");

        tokenStp = new String[tokenListStp.size()];
        whiteStp = new String[whiteListStp.size()];
        tokenListStp.toArray(tokenStp);
        whiteListStp.toArray(whiteStp);

        stpBoundaries = SENTENCE_MODEL.boundaryIndices(tokenStp, whiteStp);
        System.out.println(stpBoundaries.length + "Sentences");
        int sentStartTok = 0;
        int sentEndTok = 0;
        for (int i = 0; i < stpBoundaries.length; ++i) {
            sentEndTok = stpBoundaries[i];
            System.out.println("SENTENCE " + (i) + ": " + sentStartTok + ": " + sentEndTok + ": ");
            for (int j = sentStartTok; j <= sentEndTok; j++) {
                System.out.print(tokenStp[j] + whiteStp[j + 1]);
            }
            System.out.println();
            sentStartTok = sentEndTok + 1;
        }
    }

    public void stan_pos() {
        try {
            int cnt = 0, count = 0;
            words = new String[sentBoundaries[sentBoundaries.length-1]+2];
            pos1 = new String[sentBoundaries[sentBoundaries.length-1]+2];
            System.out.println("\n\n\n        \t  *POS Tags* \n\n");

            MaxentTagger tagger = new MaxentTagger("E:\\Project\\APIs\\APIs Used\\Stanford pos\\stanford-postagger-2010-05-26\\models\\left3words-wsj-0-18.tagger");
            @SuppressWarnings({"unchecked", "static-access"})
            List<ArrayList<? extends HasWord>> sentences = tagger.tokenizeText(new BufferedReader(new FileReader(file_name)));

            for (ArrayList<? extends HasWord> sentence : sentences) {
                System.out.println("\n");
                ArrayList<TaggedWord> tSentence = tagger.tagSentence(sentence);
                String s = new String(Sentence.listToString(tSentence, false));
                String s1[] = s.split(" ");
                for (int i = 0; i < s1.length; i++, cnt++) {
                    String s2[] = s1[i].split("/");
                    words[cnt] = s2[0];
                    if (s2[1].startsWith("N")) //pos.add("noun");
                    {
                        pos1[cnt] = "n";
                    } else if (s2[1].startsWith("V")) //pos.add("verb");
                    {
                        pos1[cnt] = "v";
                    } else if (s2[1].startsWith("J")) //pos.add("");
                    {
                        pos1[cnt] = "a";
                    } else if (s2[1].startsWith("R")) //pos.add("");
                    {
                        pos1[cnt] = "r";
                    } else {
                        pos1[cnt] = "XX";
                    }
                    System.out.print(words[cnt] + "(" + pos1[cnt] + ")" + "   ");
                }//for s1
               // System.out.print("Count" + cnt);
            }//for sen
            System.out.println("\n No. of tokens by POS tagger    :" + (cnt-1) );
        }//try
        catch (Exception e) {
        }
        try {
            br.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Sentences.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//pos
}
