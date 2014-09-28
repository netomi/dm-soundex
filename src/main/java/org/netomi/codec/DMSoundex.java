/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netomi.codec;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;

public class DMSoundex implements StringEncoder {

    private static final String RESOURCE_FILE = "org/netomi/codec/dmrules.txt";
    private static final String COMMENT = "//";
    private static final String DOUBLE_QUOTE = "\"";

    private static final Map<Character, List<Rule>> RULES = new HashMap<Character, List<Rule>>();
    private static final Map<Character, Character> TRANSLATION = new HashMap<Character, Character>();

    static {
        final InputStream rulesIS =
                DMSoundex.class.getClassLoader().getResourceAsStream(RESOURCE_FILE);

        if (rulesIS == null) {
            throw new IllegalArgumentException("Unable to load resource: " + RESOURCE_FILE);
        }

        final Scanner scanner = new Scanner(rulesIS, CharEncoding.UTF_8);
        RULES.putAll(parseRules(scanner, RESOURCE_FILE));
        scanner.close();

        // sort RULES by pattern length in descending order
        for (Map.Entry<Character, List<Rule>> rule : RULES.entrySet()) {
            List<Rule> ruleList = rule.getValue();
            Collections.sort(ruleList, new Comparator<Rule>() {
                @Override
                public int compare(Rule rule1, Rule rule2) {
                    return rule2.getPatternLength() - rule1.getPatternLength();
                }
            });
        }

        TRANSLATION.put('ß', 's');
        TRANSLATION.put('à', 'a');
        TRANSLATION.put('á', 'a');
        TRANSLATION.put('â', 'a');
        TRANSLATION.put('ã', 'a');
        TRANSLATION.put('ä', 'a');
        TRANSLATION.put('å', 'a');
        TRANSLATION.put('æ', 'a');
        TRANSLATION.put('ç', 'c');
        TRANSLATION.put('è', 'e');
        TRANSLATION.put('é', 'e');
        TRANSLATION.put('ê', 'e');
        TRANSLATION.put('ë', 'e');
        TRANSLATION.put('ì', 'i');
        TRANSLATION.put('í', 'i');
        TRANSLATION.put('î', 'i');
        TRANSLATION.put('ï', 'i');
        TRANSLATION.put('ð', 'd');
        TRANSLATION.put('ñ', 'n');
        TRANSLATION.put('ò', 'o');
        TRANSLATION.put('ó', 'o');
        TRANSLATION.put('ô', 'o');
        TRANSLATION.put('õ', 'o');
        TRANSLATION.put('ö', 'o');
        TRANSLATION.put('ø', 'o');
        TRANSLATION.put('ù', 'u');
        TRANSLATION.put('ú', 'u');
        TRANSLATION.put('û', 'u');
        TRANSLATION.put('ý', 'y');
        TRANSLATION.put('ý', 'y');
        TRANSLATION.put('þ', 'b');
        TRANSLATION.put('ÿ', 'y');
        TRANSLATION.put('ć', 'c');
        TRANSLATION.put('ł', 'l');
        TRANSLATION.put('ś', 's');
        TRANSLATION.put('ż', 'z');
        TRANSLATION.put('ź', 'z');
    }

    private static Map<Character, List<Rule>> parseRules(final Scanner scanner, final String location) {
        final Map<Character, List<Rule>> lines = new HashMap<Character, List<Rule>>();
        int currentLine = 0;

        while (scanner.hasNextLine()) {
            currentLine++;
            final String rawLine = scanner.nextLine();
            String line = rawLine;

            // discard comments
            final int cmtI = line.indexOf(COMMENT);
            if (cmtI >= 0) {
                line = line.substring(0, cmtI);
            }

            // trim whitespace
            line = line.trim();

            if (line.length() == 0) {
                continue; // empty lines can be safely skipped
            }

            // rule
            final String[] parts = line.split("\\s+");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Malformed rule statement split into " + parts.length +
                        " parts: " + rawLine + " in " + location);
            } else {
                try {
                    final String pattern = stripQuotes(parts[0]);
                    final String replacement1 = stripQuotes(parts[1]);
                    final String replacement2 = stripQuotes(parts[2]);
                    final String replacement3 = stripQuotes(parts[3]);

                    final Rule r = new Rule(pattern, replacement1, replacement2, replacement3);
                    final char patternKey = r.pattern.charAt(0);
                    List<Rule> rules = lines.get(patternKey);
                    if (rules == null) {
                        rules = new ArrayList<Rule>();
                        lines.put(patternKey, rules);
                    }
                    rules.add(r);
                } catch (final IllegalArgumentException e) {
                    throw new IllegalStateException("Problem parsing line '" + currentLine + "' in " + location, e);
                }
            }
        }

        return lines;
    }

    private static String stripQuotes(String str) {
        if (str.startsWith(DOUBLE_QUOTE)) {
            str = str.substring(1);
        }

        if (str.endsWith(DOUBLE_QUOTE)) {
            str = str.substring(0, str.length() - 1);
        }

        return str;
    }

    @Override
    public Object encode(final Object obj) throws EncoderException {
        if (!(obj instanceof String)) {
            throw new EncoderException(
                    "Parameter supplied to DaitchMokotoffSoundex encode is not of type java.lang.String");
        }
        return encode((String) obj);
    }

    @Override
    public String encode(String source) {
        if (source == null) {
            return null;
        }
        return soundex(source).split("\\|")[0];
    }

    private String cleanup(String input) {
        StringBuilder sb = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (Character.isWhitespace(ch)) {
                continue;
            }

            ch = Character.toLowerCase(ch);
            if (TRANSLATION.containsKey(ch)) {
                ch = TRANSLATION.get(ch);
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    public String soundex(String source) {
        if (source == null) {
            return null;
        }

        final String input = cleanup(source);

        Set<SoundexContext> branches = new LinkedHashSet<SoundexContext>();
        branches.add(new SoundexContext());

        char lastChar = '\0';
        for (int index = 0; index < input.length(); index++) {
            final char ch = input.charAt(index);

            // ignore whitespace inside a name
            if (Character.isWhitespace(ch)) {
                continue;
            }

            final String inputContext = input.substring(index);

            List<Rule> rules = RULES.get(ch);
            if (rules == null) {
                continue;
            }
            for (Rule rule : rules) {
                if (rule.matches(inputContext)) {
                    String replacements = rule.getReplacement(inputContext, lastChar == '\0');
                    String[] replacementsArray = replacements.split("\\|");

                    Set<SoundexContext> newSet = new LinkedHashSet<SoundexContext>();
                    for (SoundexContext branch : branches) {
                        SoundexContext originalBranch = replacementsArray.length > 1 ? branch.createBranch() : branch;
                        int replacementIndex = 0;
                        for (String nextReplacement : replacementsArray) {
                            if (replacementIndex > 0) {
                                branch = originalBranch.createBranch();
                            }

                            boolean addReplacement =
                                    branch.lastReplacement == null ||
                                    !branch.lastReplacement.endsWith(nextReplacement) ||
                                    //(!nextReplacement.equals(branch.lastReplacement)) ||
                                    (lastChar == 'm' && ch == 'n') ||
                                    (lastChar == 'n' && ch == 'm');

                            if (addReplacement) {
                                branch.sb.append(nextReplacement);
                            }

                            branch.lastReplacement = nextReplacement;
                            if (branch.sb.length() > 6) {
                                branch.sb.delete(6, branch.sb.length());
                            }
                            newSet.add(branch);
                            replacementIndex++;
                        }
                    }

                    branches = newSet;
                    index += rule.getPatternLength() - 1;
                    break;
                }
            }

            lastChar = ch;
        }

        StringBuilder result = new StringBuilder();
        for (SoundexContext branch : branches) {
            while (branch.sb.length() < 6) {
                branch.sb.append('0');
            }
            if (result.length() > 0) {
                result.append('|');
            }
            result.append(branch.sb.substring(0, 6).toString());
        }

        return result.toString();
    }

    private static final class SoundexContext {
        private StringBuilder sb;
        private String lastReplacement;

        protected SoundexContext() {
            sb = new StringBuilder();
            lastReplacement = null;
        }

        public SoundexContext createBranch() {
            SoundexContext context = new SoundexContext();
            context.sb.append(sb.toString());
            context.lastReplacement = this.lastReplacement;
            return context;
        }

        public int hashCode() {
            return sb.toString().hashCode();
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (! (other instanceof SoundexContext)) {
                return false;
            }

            return sb.toString().equals(((SoundexContext) other).sb.toString());
        }

        public String toString() {
            return sb.toString();
        }
    }

    public String toString() {
        return RULES.toString();
    }

    private static final class Rule {
        private final String pattern;
        private final String replacementAtStart;
        private final String replacementBeforeVowel;
        private final String replacementDefault;

        protected Rule(final String pattern, final String replacementAtStart,
                       final String replacementBeforeVowel, final String replacementDefault) {
            this.pattern = pattern;
            this.replacementAtStart = replacementAtStart;
            this.replacementBeforeVowel = replacementBeforeVowel;
            this.replacementDefault = replacementDefault;
        }

        public int getPatternLength() {
            return pattern.length();
        }

        public boolean matches(final String context) {
            return context.startsWith(pattern);
        }

        public String getReplacement(final String context, final boolean atStart) {
            if (atStart) {
                return replacementAtStart;
            }

            int nextIndex = getPatternLength();
            boolean nextCharIsVowel = nextIndex < context.length() ? isVowel(context.charAt(nextIndex)) : false;
            if (nextCharIsVowel) {
                return replacementBeforeVowel;
            }

            return replacementDefault;
        }

        private boolean isVowel(final char ch) {
            return ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u';
        }

        public String toString() {
            return String.format("%s=(%s,%s,%s)", pattern, replacementAtStart,
                                 replacementBeforeVowel, replacementDefault);
        }
    }
}
