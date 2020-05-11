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


class Measures extends Sentences {

    float wrd_over[][], wrd_order[][];
    double wrd_sem[][], overall[][];
    int count = 0,global_k=0;
    RiWordnet wordnet;
    JWS ws;
    JiangAndConrath jcn;
    int cluster[][], center[], change[],maximum=0,perc=0;
   String nam[]={"pollution","in_planets","student","in_sugarcane","in_drug","in_globalFinal1"};
   int siz[][]={{18,15,10},{15,13,10},{6,5,4},{4,3,2},{12,9,7},{18,15,12}};

    public Measures(String file_name) {
        super(file_name);
        System.out.println("Hello");

        wrd_over = new float[sentBoundaries.length][sentBoundaries.length];
        wrd_order = new float[sentBoundaries.length][sentBoundaries.length];
        wrd_sem = new double[sentBoundaries.length][sentBoundaries.length];
        overall = new double[sentBoundaries.length][sentBoundaries.length];
        for (int i = 0; i < sentBoundaries.length; i++) {
            for (int j = 0; j < sentBoundaries.length; j++) {
                wrd_over[i][j] = -1;
                wrd_order[i][j] = -1;
                wrd_sem[i][j] = -1;
                overall[i][j] = 0;
            }
        }
        ws = new JWS("C:/Program Files/WordNet", "2.1");
        jcn = ws.getJiangAndConrath();
        wordnet = new RiWordnet(null);
        this.word_overlap();
        this.word_order();
        this.word_semantics();
        this.overall();
    //    this.clustering();
    }

    public Measures() {
    }

    public boolean spcl_char(String s) {
        String spcl[] = {".", ",", "\"", "\'", "&", "!", "@", "#", "$", "%", "^", "*", "(", ")", "?", "/", ";", ":", "<", ">", "{", "}", "[", "]", "|", "\\", "-"};
        for (int i = 0; i < spcl.length; i++) {
            if (s.contains(spcl[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean in_stpWord_list(String s) {
        String lower = s.toLowerCase();
        if (stpSet.contains(lower)) {           //Using the stop words Set formed before
            return true;
        }
        return false;
    }

    public boolean cases(String a[], String s, int start, int end) {
        if (in_stpWord_list(s) || spcl_char(s) || dup_word(a, s, start, end)) {
            return true;
        }
        return false;

    }

    public boolean dup_word(String a[], String s, int start, int end) {
        for (int i = start; i < end; i++) {
            if (s.equals(a[i])) {
                return true;
            }
        }
        return false;
    }

    public int no_of_same_words(int a[], String b[], int sent_i, int sent_j, int choice) {
        int iStart, iEnd;
        iStart = (sent_i == 0 ? 0 : a[sent_i - 1] + 1);
        iEnd = a[sent_i];

        int jStart, jEnd;
        jStart = (sent_j == 0 ? 0 : a[sent_j - 1] + 1);
        jEnd = a[sent_j];
        int common = 0;
        for (int k_out = iStart; k_out <= iEnd; k_out++) {
            if (cases(b, b[k_out], iStart, k_out)) {
                count++;
            }
        }
        for (int k_in = jStart; k_in <= jEnd; k_in++) {
            if (cases(b, b[k_in], jStart, k_in)) {
                count++;
            }
        }
        for (int k_out = iStart; k_out <= iEnd; k_out++) {
            if (choice == 1) {
                for (int k_in = jStart; k_in <= jEnd; k_in++) {
                    if (b[k_out].equalsIgnoreCase(b[k_in]) && !cases(b, b[k_out], iStart, k_out)) {
                        common++;
                        break;     // to avoid considering the same word twice
                    }
                }
            } else if (choice == 2) {

                for (int k_in = jStart; k_in <= jEnd; k_in++) {
                    if (b[k_out].equalsIgnoreCase(b[k_in])) {
                        common++;
                        break;     // to avoid considering the same word twice
                    }
                }
            }
        }
        return common;
    }//fn

    public int word_overlap() {
        DecimalFormat d = new DecimalFormat("0.0000");
        int iStart, iEnd;
        int jStart, jEnd;
        int ilen, jlen;
        for (int i = 0; i < stpBoundaries.length; i++) {
            iStart = (i == 0 ? 0 : stpBoundaries[i - 1] + 1);
            iEnd = stpBoundaries[i];
            ilen = iEnd - iStart + 1;
            for (int j = 0; j < stpBoundaries.length; j++) {
                jStart = (j == 0 ? 0 : stpBoundaries[j - 1] + 1);
                jEnd = stpBoundaries[j];
                jlen = jEnd - jStart + 1;
                if (wrd_over[j][i] != -1) {
                    wrd_over[i][j] = wrd_over[j][i];
                } else {
                    wrd_over[i][j] = 2 * ((float) no_of_same_words(stpBoundaries, tokenStp, i, j, 1) / (ilen + jlen - count));
                }
                count = 0;
            }
        }
        System.out.println("       \n    \t \t*Common Words Similarity Matrix  *\n");
        for (int i = 0; i < stpBoundaries.length; i++) {
            System.out.print("     \tS" + (i));
        }
        System.out.println(" ");
        for (int i = 0; i < sentBoundaries.length * 20; i++) {
            System.out.print("_");
        }
        for (int i = 0; i < stpBoundaries.length; i++) {
            System.out.print("\nS" + (i) + " |");
            for (int j = 0; j < stpBoundaries.length; j++) {
                System.out.print("\t " + d.format(wrd_over[i][j]));
            }
        }
        count = 0;
/*        try {
            br.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Measures.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        return 0;
    }//fn

    public int find(int[] a, int key) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == key) {
                return i;
            }
        }
        return -1;
    }

    public boolean dup(int[] a, int key) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == key) {
                return true;
            }
        }
        return false;
    }

    public float compute_order_sim(int sent_i, int sent_j) {
        int iStart, iEnd;
        float order = 0;
        iStart = (sent_i == 0 ? 0 : sentBoundaries[sent_i - 1] + 1);
        iEnd = sentBoundaries[sent_i];
        int jStart, jEnd;
        jStart = (sent_j == 0 ? 0 : sentBoundaries[sent_j - 1] + 1);
        jEnd = sentBoundaries[sent_j];
        int out[], in[] = null, i;
        in = new int[no_of_same_words(sentBoundaries, tokens, sent_i, sent_j, 2)];          //   increasing the time. But inevitable!!!
        i = 0;
        for (int k_out = iStart; k_out <= iEnd; k_out++) {
            for (int k_in = jStart; k_in <= jEnd; k_in++) {
                if (tokens[k_out].equalsIgnoreCase(tokens[k_in])) {
                    if (dup(in, k_in) && i != 0) {
                        continue;
                    }
                    in[i++] = k_in;
                    break;
                }
            }
        }
        int[] sort1 = new int[in.length];
        sort1 = Arrays.copyOf(in, in.length);
        Arrays.sort(sort1);
        int diff = 0;
        for (i = 0; i < in.length; i++) {
            diff += Math.abs(i - find(sort1, in[i]));
        }
        diff *= 2;
        int len = in.length * in.length;
        if (in.length % 2 == 0) {
            order = (float) (diff) / (len);
        } else if (in.length != 1) {
            order = (float) (diff) / (len - 1);
        } else {
            order = 0;
        }
        order = 1 - order;
        if (order < 0) {
            order = 0;
        }
        return order;
    }//fn

    public void word_order() {
        DecimalFormat d = new DecimalFormat("0.0000");
        for (int i = 0; i < sentBoundaries.length; i++) {
            for (int j = 0; j < sentBoundaries.length; j++) {
                if (wrd_order[i][j] != -1) {                    //avoiding re-computation of the same value
                    wrd_order[i][j] = wrd_order[j][i];
                } else if (wrd_over[i][j] == 0) {               // If wrd_over is 0, no common words
                    wrd_order[i][j] = 0;
                } else {
                    wrd_order[i][j] = compute_order_sim(i, j);
                }
            }
        }
        System.out.print("\n\n\n      \t  \t *Word Order Similarity * \n");
        for (int i = 0; i < stpBoundaries.length; i++) {
            System.out.print("\t     S" + (i));
        }
        System.out.println(" ");
        for (int i = 0; i < sentBoundaries.length * 20; i++) {
            System.out.print("_");
        }

        for (int i = 0; i < stpBoundaries.length; i++) {
            System.out.print("\nS" + (i) + " |");
            for (int j = 0; j < stpBoundaries.length; j++) {
                System.out.print("\t  " + d.format(wrd_order[i][j]));
            }
        }
       /* try {
            br.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Measures.class.getName()).log(Level.SEVERE, null, ex);
        }
        */

    }//fn


    public boolean compare_tokens_synset(String wrd1, String wrd2, String pos) {
        System.setProperty("wordnet.database.dir", "C:\\Program Files\\WordNet\\2.1\\dict");
        if (jcn.max(wrd1, wrd2, pos) > 1) {
            return true;
        }
        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Synset[] synsets = database.getSynsets(wrd1);
        if (synsets.length > 0) {
            for (int i = 0; i < synsets.length; i++) {
                String[] wordForms = synsets[i].getWordForms();
                for (int j = 0; j < wordForms.length; j++) {
                    if (wordForms[j].equalsIgnoreCase(wrd2)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public double stemmed_similarity(String wrd1, String wrd2, String pos) {

        if (wordnet.getBestPos(wrd1.toLowerCase()) == null || wordnet.getBestPos(wrd2.toLowerCase()) == null) {
            return 0.0;
        }
        System.out.println("\n\t\tOriginal Words   :" + wrd1 + "  -   " + wrd2);
        String s1[] = wordnet.getStems(wrd1.toLowerCase(), wordnet.getBestPos(wrd1.toLowerCase()));
        String s2[] = wordnet.getStems(wrd2.toLowerCase(), wordnet.getBestPos(wrd2.toLowerCase()));
        if (s1[0].equals("be") && s2[0].equals("be")) {
            return 1.0;
        }
        if (s1.length == 0 || s2.length == 0) {
            return 0.0;
        }
        System.out.println("\t\tStems   :" + s1[0] + "  -   " + s2[0]);
        if (compare_tokens_synset(s1[0], s2[0], pos)) {
            return 1.0;
        }
        double temp = jcn.max(s1[0], s2[0], pos);
        System.out.println("\t\tSim  :" + temp);
        return temp;
    }

    public double compute_sem_sim(int sent_i, int sent_j) {
       System.out.println(" *********************************Sentence " + sent_i + " & " + sent_j +"(Round 1\n");
        int iStart, iEnd;
        double score1[][] = new double[sentBoundaries.length][1000];
        double score2[][] = new double[sentBoundaries.length][1000];
        double score_sen_i = 0.0, score_sen_j = 0.0, sim_score = 0.0;
        double temp1 = 0.0, temp2 = 0.0;
        int sen_i_len = 0, sen_j_len = 0, illegal = 0;
        iStart = (sent_i == 0 ? 0 : sentBoundaries[sent_i - 1] + 1);
        iEnd = sentBoundaries[sent_i];
        int jStart, jEnd;
        jStart = (sent_j == 0 ? 0 : sentBoundaries[sent_j - 1] + 1);
        jEnd = sentBoundaries[sent_j];
        for (int k_out = iStart; k_out <= iEnd; k_out++) {
            score1[sent_i][k_out] = 0.0;
            System.out.println("\n  Out  :" + tokens[k_out] + " (" + pos1[k_out] +")");
            if (!pos1[k_out].equals("n") && !pos1[k_out].equals("v") || in_stpWord_list(tokens[k_out])) {
                System.out.println(" \n\t\t\t\tIllegal        ----- " + tokens[k_out]);
                illegal++;
                continue;
            }
            for (int k_in = jStart; k_in <= jEnd; k_in++) {

                System.out.print("\n\t   In  :" + tokens[k_in] + " (" + pos1[k_in] +")");
                if (in_stpWord_list(tokens[k_in])) {
                    System.out.print(" Skipping this in ");
                    continue;
                }
                if (pos1[k_out].equals(pos1[k_in]) && (pos1[k_out].equals("n") || pos1[k_out].equals("v"))) {
                    if ((tokens[k_out].equalsIgnoreCase(tokens[k_in])) || (compare_tokens_synset(tokens[k_out], tokens[k_in], pos1[k_out]))) {
                        score1[sent_i][k_out] = 1.0;
                    } else {
                        temp1 = stemmed_similarity(tokens[k_out], tokens[k_in], pos1[k_out]);
                        if (temp1 > score1[sent_i][k_out]) {
                            score1[sent_i][k_out] = temp1;
                        }
                    }
                    System.out.print("\tScore  " + score1[sent_i][k_out]);
                }
            }//inner for
            System.out.println("\n  Out Over  ... FINAL SCORE for " + tokens[k_out] + " =" + score1[sent_i][k_out]);

            //System.out.println("INSIDE SEM_SIM11111111--> for--->"+k_out+"--->"+tokens[k_out] + "score--->"+score1[sent_i][k_out]);
        }//for
        System.out.print(" \n  *********************************Sentence " + sent_i + " & " + sent_j + "(Round 2\n");
        for (int k_out = jStart; k_out <= jEnd; k_out++) {
            score2[sent_j][k_out] = 0.0;
            System.out.println("\n   Out :" + tokens[k_out] + " (" + pos1[k_out]+")");
            if (!pos1[k_out].equals("n") && !pos1[k_out].equals("v") || in_stpWord_list(tokens[k_out])) {
                System.out.println(" \n\t\t\t\tIllegal        ----- " + tokens[k_out]);
                illegal++;
                continue;
            }
            for (int k_in = iStart; k_in <= iEnd; k_in++) {
                System.out.print("\n\t   In  :" + tokens[k_in] + " (" + pos1[k_in]+")");
                if (in_stpWord_list(tokens[k_in])) {
                    System.out.print(" Skipping this in ");
                    continue;
                }
                if (pos1[k_out].equals(pos1[k_in]) && (pos1[k_out].equals("n") || pos1[k_out].equals("v"))) {
                    if ((tokens[k_out].equalsIgnoreCase(tokens[k_in])) || (compare_tokens_synset(tokens[k_out], tokens[k_in], pos1[k_out]))) {
                        score2[sent_j][k_out] = 1.0;
                    } else {
                        temp1 = stemmed_similarity(tokens[k_out], tokens[k_in], pos1[k_out]);
                        if (temp1 > score2[sent_j][k_out]) {
                            score2[sent_j][k_out] = temp1;
                        }

                    }
                    System.out.print("\tScore  " + score2[sent_j][k_out]);
                }

            }//inner for
            System.out.println(" \n   Out Over  ... Final Score for " + tokens[k_out] + " =" + score2[sent_j][k_out]);

        }//for
        for (int i = iStart; i <= iEnd; i++) {
            score_sen_i += score1[sent_i][i];
        }
        for (int i = jStart; i <= jEnd; i++) {
            score_sen_j += score2[sent_j][i];
        }
        sim_score = 0.0;
        sen_i_len = iEnd - iStart + 1;
        sen_j_len = jEnd - jStart + 1;
        temp1 = score_sen_i + score_sen_j;
        temp2 = sen_i_len + sen_j_len - illegal;
        sim_score = temp1 / temp2;
        System.out.println(" \nTotal Value  =  " + temp1 + "  **  Illegals   =" + illegal + "  **  Final lenght taken  =" + temp2 + "  **  Sim Between Sentence" + sent_i + " & Sentence" + sent_j + " =  :" + sim_score);
        return sim_score;
    }//fn

    public void word_semantics() {
        stan_pos();
        DecimalFormat d = new DecimalFormat("0.0000");
        for (int i = 0; i < sentBoundaries.length; i++) {
            for (int j = 0; j < sentBoundaries.length; j++) {
                if (wrd_sem[i][j] != -1) {                    //avoiding re-computation of the same value
                    wrd_sem[i][j] = wrd_sem[j][i];
                } else {
                    wrd_sem[i][j] = compute_sem_sim(i, j);
                }
            }
        }
        System.out.print("\n\n\n        \t *Word Semantic Similarity Matrix* \n\n");
        for (int i = 0; i < sentBoundaries.length; i++) {
            System.out.print("\t     S" + (i));
        }
        System.out.println("");
        for (int i = 0; i < sentBoundaries.length * 20; i++) {
            System.out.print("_");
        }
        for (int i = 0; i < sentBoundaries.length; i++) {
            System.out.print("\nS" + (i) + " |");
            for (int j = 0; j < sentBoundaries.length; j++) {
                System.out.print("\t" + d.format(wrd_sem[i][j]));
            }
        }
      /*  try {
            br.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Measures.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }//fn

    public void overall() {
        for (int i = 0; i < sentBoundaries.length; i++) {
            for (int j = 0; j < sentBoundaries.length; j++) {
                overall[i][j] = 0.15 * wrd_over[i][j] + 0.1 * wrd_order[i][j] + 0.75 * wrd_sem[i][j];
            }
        }
        DecimalFormat d = new DecimalFormat("0.0000");
        System.out.print("\n\n\n        \t *Overall Similarity* \n\n");
        for (int i = 0; i < sentBoundaries.length; i++) {
            System.out.print("\t     S" + (i));
        }
        System.out.println("");
       for (int i = 0; i < sentBoundaries.length * 20; i++) {
            System.out.print("_");
        }
        for (int i = 0; i < sentBoundaries.length; i++) {
            System.out.print("\nS" + (i) + " |");
            for (int j = 0; j < sentBoundaries.length; j++) {
                System.out.print("\t" + d.format(overall[i][j]));
            }
        }
    }

    @SuppressWarnings("empty-statement")
    public void insert(int key, int index) {
        int j;
        for (j = 0; cluster[index][j] != -1 && cluster[index][j] != -2; j++);
        if (cluster[index][j] == -2) {
            cluster[index][j] = key;
            return;
        }
        cluster[index][j++] = key;
        cluster[index][j] = -1;
    }

    int distinct_terms(int key, ArrayList ar) {
        if (key == -1) // required to return the no. of distinct terms in the whole document
        {
            System.out.println(" List of Distinct Tokens   :");
            for (int i = 0; i < sentBoundaries[sentBoundaries.length - 1]; i++) {
                if (spcl_char(tokens[i]) || in_stpWord_list(tokens[i]) || ar.contains(tokens[i].toLowerCase())) {
                    continue;
                } else {
                    ar.add(tokens[i].toLowerCase());
                }
                System.out.println(tokens[i]);
            }
                 System.out.println(" No. of Distinct Elements in the Document is " + ar.size());
        } else // no. of distinct terms in the particular sentence
        {
            int start;
            if (key == 0) {
                start = 0;
            } else {
                start = sentBoundaries[key - 1] + 1;
            }
            int end = sentBoundaries[key];
            for (int i = start; i <= end; i++) {
                if (spcl_char(tokens[i]) || in_stpWord_list(tokens[i]) || ar.contains(tokens[i].toLowerCase())) {
                    continue;
                } else {
                    ar.add(tokens[i].toLowerCase());
                }
            //    System.out.println(" Distinct Elements " + ar.size());
            }
             System.out.println("*No. of Distinct Elements in Sentence  " + key + "is " + ar.size());
        }

        return ar.size();
    }

    int compute_k() {
        int j = 0;
        int val = 0;
        ArrayList ar = new ArrayList();
        int doc = distinct_terms(-1, ar);
        ar.clear();
        for (int i = 0; i < sentBoundaries.length; i++) {
            val += distinct_terms(i, ar);
            ar.clear();
        }
        j = (doc * sentBoundaries.length) / val;
        int i;
        for( i=0;i<nam.length ;i++) {
            if(file_name.contains(nam[i])) break;
        }
        if(i==nam.length) maximum=0;
        else maximum=siz[i][perc];
        System.out.println(" Maximum " + maximum);
        if(maximum!=0) global_k=maximum;
        else{
            System.out.println(" Entering here");
           if(perc==0) global_k= 50*sentBoundaries.length/100;
           else if(perc==1) global_k= 40*sentBoundaries.length/100;
           else if(perc==2) global_k= 30*sentBoundaries.length/100;
        }
        if(global_k==0) global_k=1;
        System.out.println(" Final K" + global_k);
        return global_k;
        //return j;
    }

    @SuppressWarnings("empty-statement")
    public void clustering() {
    /*   while (true) {
            System.out.println("\n Enter a K-value : (To use system generated, Press 0   :  To exit, Press -1)");
            global_k = 0;
            try {
                global_k = Integer.parseInt(br.readLine());
            } catch (IOException ex) {
                Logger.getLogger(Measures.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (global_k == -1) {
                break;
            }

            global_k = (global_k == 0) ? compute_k() : global_k;*/
            int iterations = 0;
            global_k=compute_k();
            System.out.print("\n $Value of global_k is  " + global_k +" *\n \n   *Initial Clusters *");
            cluster = new int[global_k][sentBoundaries.length + 1];
            center = new int[global_k];
            change = new int[sentBoundaries.length + 1];
            int cnt = 0;
            int initial = sentBoundaries.length / global_k;
            int add = sentBoundaries.length % global_k;
            for (int i = 0; i < global_k; i++) {
                int j = 0;
                for (j = 0; j < initial; j++) {
                    if (cnt < sentBoundaries.length) {
                        cluster[i][j] = cnt++;
                    }
                }
                if (i < add) {
                    cluster[i][j++] = cnt++;
                }
                cluster[i][j] = -1;
            }
            for (int i = 0; i < global_k; i++) {
                System.out.print("\n\tCluster" + (i+1)  +" ");
                for (int j = 0; cluster[i][j] != -1; j++) {
                    if(cluster[i][j]==-2) continue;
                    System.out.print(" "+ cluster[i][j]);
                }
            }
            while (true) {
                cnt = 0;
                iterations++;
                System.out.println(" \n \tIteration  :" + iterations );
                for (int i = 0; i < sentBoundaries.length; i++) {
                    change[i] = 0;
                }
                for (int i = 0; i < global_k; i++) {
                    int j;
                    for (j = 0; cluster[i][j] < 0; j++);
                    center[i] = cluster[i][j];
                    double temp = 0, end_val = 0;
                    for (j = 0; cluster[i][j] != -1; j++) {
                        temp = 0;
                        for (int l = 0; cluster[i][l] != -1; l++) {
                            if (l == j || cluster[i][j] < 0 || cluster[i][l] < 0) {
                                continue;
                            }
                            temp += overall[cluster[i][j]][cluster[i][l]];
                        }
                        if (temp > end_val) {
                            center[i] = cluster[i][j];
                            end_val = temp;
                        }
                    }
                }
                for (int out = 0; out < global_k; out++) {
                    int j;
                    for (j = 0; cluster[out][j] != -1; j++) {
                        if (center[out] == cluster[out][j] || cluster[out][j] == -2) {
                            continue;
                        }
                        int end_cluster = out;
                        double end_val = overall[cluster[out][j]][center[out]];

                        for (int in = 0; in < global_k; in++) {
                            if (in == out) {
                                continue;
                            }
                            if (overall[cluster[out][j]][center[in]] > end_val) {
                                end_val = overall[cluster[out][j]][center[in]];
                                end_cluster = in;
                            }
                        }
                        if (end_cluster != out) {
                            insert(cluster[out][j], end_cluster);
                            change[cluster[out][j]] = 1;
                            cnt++;
                            cluster[out][j] = -2;
                       }
                    }
                    cluster[out][j] = -1;
                }
                System.out.print(" \nAt the end of Iteration " + iterations +", Sentence(s) Replaced from Original Clusters to new Clusters is/are  :" );
                for (int i = 0; i < sentBoundaries.length; i++) {
                    if (change[i] == 1) {
                        System.out.print(" "+i);
                    }
                }
                System.out.print("\n *Clusters *   :");
                for (int i = 0; i < global_k; i++) {
                    System.out.print("\nCluster" + (i+1)  +" :");
                    for (int j = 0; cluster[i][j] != -1; j++) {
                     if(cluster[i][j]==-2) continue;
                    System.out.print(" "+ cluster[i][j]);
                    }
                }
                if (cnt < 2) {
                    break;
                }
            } //while
            System.out.println("\n END OF CLUSTERING....\n\n\t\t *Final Cluster *");
            for (int i = 0; i < global_k; i++) {
                int j;
                for (j = 0; cluster[i][j] < 0; j++);
                center[i] = cluster[i][j];
                double temp = 0, end_val = 0;
                for (j = 0; cluster[i][j] != -1; j++) {
                    temp = 0;
                    for (int l = 0; cluster[i][l] != -1; l++) {
                        if (l == j || cluster[i][j] < 0 || cluster[i][l] < 0) {
                            continue;
                        }
                        temp += overall[cluster[i][j]][cluster[i][l]];
                    }
                    if (temp > end_val) {
                        center[i] = cluster[i][j];
                        end_val = temp;
                    }
                }
                System.out.print("\n Center for Cluster  " + i + " - " + center[i]);
            }
            Arrays.sort(center);
            int sentStart, sentEndToglobal_k;
            System.out.println("\n\n \t\t  *Summary*    \n");
            for (int i = 0; i < center.length; i++) {
                if (center[i] == 0) {
                    sentStart = 0;
                } else {
                    sentStart = sentBoundaries[center[i] - 1] + 1;
                }
                sentEndToglobal_k = sentBoundaries[center[i]];
                System.out.println("SENTENCE " + (center[i]) +":");

                for (int j = sentStart; j <= sentEndToglobal_k; j++) {
                    System.out.print(tokens[j] + whites[j + 1]);
                }
                System.out.println();
            }
          System.out.println("\n The Value of K being Used is  :" +global_k);
  //      }//while
    }//fn
}//class measures
