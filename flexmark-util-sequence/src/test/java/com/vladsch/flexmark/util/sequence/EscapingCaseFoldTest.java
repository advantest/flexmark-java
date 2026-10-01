package com.vladsch.flexmark.util.sequence;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EscapingCaseFoldTest {
    private static final String RESOURCE = "/com/vladsch/flexmark/util/sequence/CaseFolding.txt";
    private static final String SHA_256 = "CDD49E55EAE3BBF1F0A3F6580C974A0263CB86A6A08DAA10FBF705B4808A56F7";

    private static void assertFolds(String expected, String source) {
        assertEquals(expected, Escaping.caseFold(source));
    }

    private static void assertSameFold(String... labels) {
        String first = Escaping.caseFold(labels[0]);
        for (String label : labels) {
            assertEquals("fold of " + label, first, Escaping.caseFold(label));
        }
    }

    private static byte[] readResource() throws IOException {
        try (InputStream in = EscapingCaseFoldTest.class.getResourceAsStream(RESOURCE)) {
            assertNotNull("missing resource " + RESOURCE, in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
            return out.toByteArray();
        }
    }

    // code point -> folded string, C and F entries only
    private static Map<Integer, String> readEntries() throws IOException {
        Map<Integer, String> entries = new LinkedHashMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new java.io.ByteArrayInputStream(readResource()), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            int hash = line.indexOf('#');
            if (hash >= 0) line = line.substring(0, hash);
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] fields = line.split(";");
            String status = fields[1].trim();
            if (!status.equals("C") && !status.equals("F")) continue;
            StringBuilder sb = new StringBuilder();
            for (String cp : fields[2].trim().split(" ")) sb.appendCodePoint(Integer.parseInt(cp, 16));
            assertEquals("duplicate entry " + fields[0], null,
                    entries.put(Integer.parseInt(fields[0].trim(), 16), sb.toString()));
        }
        return entries;
    }

    @Test
    public void ascii() {
        assertFolds("a", "A");
        assertFolds("hello world", "Hello World");
        assertFolds("foo-bar_1.2 [x]", "FOO-bar_1.2 [X]");
    }

    @Test
    public void emptyAndUnchanged() {
        assertFolds("", "");
        assertFolds("123 !?", "123 !?");
        assertFolds("日本語", "日本語");
    }

    @Test
    public void fullFoldingEntriesChangeLength() {
        assertFolds("ss", "\u00DF");
        assertFolds("ss", "\u1E9E");
        assertFolds("ffi", "\uFB03");
        assertFolds("ff", "\uFB00");
    }

    @Test
    public void equivalentLabels() {
        assertSameFold("\u00DF", "\u1E9E", "SS", "ss", "Ss");
        assertSameFold("Ärger", "ärger", "ÄRGER");
    }

    @Test
    public void finalSigmaIsNotContextDependent() {
        assertSameFold("\u03A3", "\u03C3", "\u03C2");
        assertSameFold("\u0391\u03A3", "\u03B1\u03C3", "\u03B1\u03C2");
    }

    @Test
    public void latinCapitalIWithDotAbove() {
        assertFolds("i\u0307", "\u0130");
    }

    @Test
    public void cherokee() {
        // U+13A0 (capital) folds to itself, the small letters U+AB70.. and U+13F8.. fold to the capitals
        assertFolds("\u13A0", "\u13A0");
        assertFolds("\u13A0", "\uAB70");
        assertFolds("\u13F0", "\u13F8");
        assertSameFold("\u13A0", "\uAB70");
        assertSameFold("\u13F3", "\u13FB");
    }

    @Test
    public void supplementaryPlane() {
        assertFolds("\uD801\uDC28", "\uD801\uDC00");
        assertSameFold("\uD801\uDC00", "\uD801\uDC28");
        assertFolds("a\uD801\uDC28b", "A\uD801\uDC00B");
    }

    @Test
    public void unpairedSurrogateIsKept() {
        assertFolds("a\uD801", "A\uD801");
    }

    @Test
    public void everyCommonAndFullEntry() throws IOException {
        Map<Integer, String> entries = readEntries();
        assertTrue("entries " + entries.size(), entries.size() > 1500);
        for (Map.Entry<Integer, String> entry : entries.entrySet()) {
            String source = new String(Character.toChars(entry.getKey()));
            assertEquals(String.format("U+%04X", entry.getKey()), entry.getValue(), Escaping.caseFold(source));
        }
    }

    @Test
    public void asciiFastPathMatchesTable() throws IOException {
        Map<Integer, String> entries = readEntries();
        for (int cp = 0; cp < 0x80; cp++) {
            String expected = cp >= 'A' && cp <= 'Z' ? String.valueOf((char) (cp + 32)) : null;
            assertEquals(String.format("U+%04X", cp), expected, entries.get(cp));
        }
    }

    @Test
    public void resourceIsUnmodified() throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(readResource());
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02X", b));
        assertEquals(SHA_256, sb.toString());
    }
}
