package edu.stanford.nlp.international.spanish;

import edu.stanford.nlp.international.spanish.process.SpanishTokenizer;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import junit.framework.TestCase;

import java.io.StringReader;
import java.util.List;

/**
 * Needs to be an "itest" because the VerbStripper loads data from the models jar.
 *
 * @author Ishita Prasad
 */
public class SpanishTokenizerITest extends TestCase {

  private final String[] spanishInputs = {
      "Esta es una oración.",
      "¡Dímelo!",
      "Hazlo.",
      "Este es un címbalo.",
      "Metelo.",
      "Sentémonos.",
      "Escribámosela.",
      "No comamos allí.",
      "Comamosla.",
      "sub-20",
      "un teléfono (902.400.345).",
      "Port-au-Prince",
      "McLaren/Mercedes",
      "10/12",
      "4X4",
      "3G",
      "3g",
      "sp3",
      "12km",
      "12km/h",
      "Los hombres sentados están muy guapos.",
      "Hizo abrirlos.",
      "salos ) ( 1 de",
  };

  private final String[][] spanishGold = {
      { "Esta", "es", "una", "oración", "." },
      { "¡", "Di", "me", "lo", "!" },
      { "Haz", "lo", "." },
      { "Este", "es", "un", "címbalo", "." },
      { "Mete", "lo", "." },
      { "Sentemos", "nos", "." },
      { "Escribamos", "se", "la", "." },
      { "No", "comamos", "allí", "." },
      { "Comamos", "la", "." },
      { "sub-20" },
      { "un", "teléfono", "=LRB=", "902.400.345", "=RRB=", "." },
      { "Port", "-", "au", "-", "Prince" },
      { "McLaren", "/", "Mercedes" },
      { "10/12" },
      { "4X4" },
      { "3G" },
      { "3g" },
      { "sp3" },
      { "12", "km" },
      { "12", "km", "/", "h" },
      { "Los", "hombres", "sentados", "están", "muy", "guapos", "." },
      { "Hizo", "abrir", "los", "." },
      { "salos", "=RRB=", "=LRB=", "1", "de" },
  };


  private static void runSpanish(TokenizerFactory<CoreLabel> tf, String[] inputs, String[][] gold) {
    for (int sent = 0; sent < inputs.length; sent++) {
      Tokenizer<CoreLabel> spanishTokenizer = tf.getTokenizer(new StringReader(inputs[sent]));
      int i = 0;
      while (spanishTokenizer.hasNext()) {
        String w = spanishTokenizer.next().word();
        try {
          assertEquals("SpanishTokenizer problem", gold[sent][i], w);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
          // the assertion below outside the loop will fail
        }
        i++;
      }
      assertEquals("SpanishTokenizer num tokens problem", i, gold[sent].length);
    }
  }

  public void testSpanishTokenizerWord() {
     assert spanishInputs.length == spanishGold.length;
     final TokenizerFactory<CoreLabel> tf = SpanishTokenizer.ancoraFactory();
     tf.setOptions("");
     tf.setOptions("tokenizeNLs");

     runSpanish(tf, spanishInputs, spanishGold);
   }

  /** Makes a Spanish tokenizer with the options that CoreNLP uses. Results actually no different.... */
  public void testSpanishTokenizerCoreNLP() {
    assert spanishInputs.length == spanishGold.length;
    final TokenizerFactory<CoreLabel> tf = SpanishTokenizer.coreLabelFactory();
    tf.setOptions("");
    tf.setOptions("invertible,ptb3Escaping=true,splitAll=true");

    runSpanish(tf, spanishInputs, spanishGold);
  }

  public void testOffsetsSpacing() {
    // guide                 1         2         3         4          5         6         7           8         9         0         1         2         3
    // guide       0123456789012345678901234567890123456789012345678 90123456789012345678901234567 8 901234567890123456789012345678901234567890123456789012345
    String text = "  La   combinación consonántica ss es ajena a la\tortografía    castellana:   \n\n traigámosela, mandémoselos, escribámosela, comprémoselo.";
    final TokenizerFactory<CoreLabel> tf = SpanishTokenizer.coreLabelFactory();
    tf.setOptions("");
    tf.setOptions("splitAll=true");
    Tokenizer<CoreLabel> spanishTokenizer = tf.getTokenizer(new StringReader(text));
    List<CoreLabel> tokens = spanishTokenizer.tokenize();
    System.err.println(tokens);
    assertEquals(27, tokens.size());
    // assertEquals("  ", tokens.get(0).get(CoreAnnotations.BeforeAnnotation.class));
    // assertEquals("\t", tokens.get(8).get(CoreAnnotations.AfterAnnotation.class));
    assertEquals("Wrong begin char offset", 2, (int) tokens.get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class));
    assertEquals("Wrong end char offset", 4, (int) tokens.get(0).get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
    assertEquals("La", tokens.get(0).get(CoreAnnotations.OriginalTextAnnotation.class));
    // note: after(x) and before(x+1) are the same
    // assertEquals("   ", tokens.get(0).get(CoreAnnotations.AfterAnnotation.class));
    // assertEquals("   ", tokens.get(1).get(CoreAnnotations.BeforeAnnotation.class));

    assertEquals("escribamos", tokens.get(19).get(CoreAnnotations.OriginalTextAnnotation.class));  // todo [cdm 2016]: wrong? should preserve accent
    assertEquals("escribamos", tokens.get(19).get(CoreAnnotations.TextAnnotation.class));
    assertEquals("Wrong begin char offset", 108, (int) tokens.get(19).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class));
    assertEquals("Wrong end char offset", 118, (int) tokens.get(19).get(CoreAnnotations.CharacterOffsetEndAnnotation.class));

    assertEquals("se", tokens.get(20).get(CoreAnnotations.OriginalTextAnnotation.class));
    assertEquals("se", tokens.get(20).get(CoreAnnotations.TextAnnotation.class));
    assertEquals("Wrong begin char offset", 118, (int) tokens.get(20).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class));
    assertEquals("Wrong end char offset", 120, (int) tokens.get(20).get(CoreAnnotations.CharacterOffsetEndAnnotation.class)); // todo [cdm 2016]: Wrong! Has to be 119! Unclear what start is but end must be 119.

    assertEquals("la", tokens.get(21).get(CoreAnnotations.OriginalTextAnnotation.class));
    assertEquals("la", tokens.get(21).get(CoreAnnotations.TextAnnotation.class));
    assertEquals("Wrong begin char offset", 120, (int) tokens.get(21).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class)); // todo [cdm 2016]: Wrong! Has to be 119!
    assertEquals("Wrong end char offset", 122, (int) tokens.get(21).get(CoreAnnotations.CharacterOffsetEndAnnotation.class));

    assertEquals(",", tokens.get(22).get(CoreAnnotations.OriginalTextAnnotation.class));
    assertEquals(",", tokens.get(22).get(CoreAnnotations.TextAnnotation.class));
    assertEquals("Wrong begin char offset", 121, (int) tokens.get(22).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class));
    assertEquals("Wrong end char offset", 122, (int) tokens.get(22).get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
  }

  private void testOffset(String input, int[] beginOffsets, int[] endOffsets) {
    TokenizerFactory<CoreLabel> tf = SpanishTokenizer.ancoraFactory();
    Tokenizer<CoreLabel> tokenizer = tf.getTokenizer(new StringReader(input));
    List<CoreLabel> tokens = tokenizer.tokenize();

    assertEquals("Number of tokens doesn't match reference '" + input + "'", beginOffsets.length, tokens.size());
    for (int i = 0; i < beginOffsets.length; i++) {
      assertEquals("Char begin offset of word " + i + " deviates from reference '" + input + "'",
              beginOffsets[i], tokens.get(i).beginPosition());
      assertEquals("Char end offset of word " + i + " deviates from reference '" + input + "'",
              endOffsets[i], tokens.get(i).endPosition());
    }
  }

  public void testCliticPronounOffset() {
    // will be tokenized into "tengo que decir te algo"
    testOffset("tengo que decirte algo", new int[]{0, 6, 10, 15, 18}, new int[]{5, 9, 15, 17, 22});
  }

  public void testIr() {
    // "ir" is a special case -- it is a verb ending without a stem!
    testOffset("tengo que irme ahora", new int[] {0, 6, 10, 12, 15}, new int[] {5, 9, 12, 14, 20});
  }

  public void testContractionOffsets() {
    // y de el y
    testOffset("y del y", new int[] {0, 2, 3, 6}, new int[] {1, 3, 5, 7});

    // y a el y
    testOffset("y al y", new int[] {0, 2, 3, 5}, new int[] {1, 3, 4, 6});

    // y con mí y
    testOffset("y conmigo y", new int[] {0, 2, 5, 10}, new int[] {1, 5, 9, 11});
  }

  public void testCompoundOffset() {
    testOffset("y abc-def y", new int[] {0, 2, 5, 6, 10}, new int[] {1, 5, 6, 9, 11});
    testOffset("y abc - def y", new int[] {0, 2, 6, 8, 12}, new int[] {1, 5, 7, 11, 13});
  }

}
