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

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoderAbstractTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link DMSoundex}.
 * <p>
 * Keep this file in UTF-8 encoding for proper Javadoc processing.</p>
 */
public class DMSoundexTest extends StringEncoderAbstractTest<DMSoundex> {

    @Override
    protected DMSoundex createStringEncoder() {
        return new DMSoundex();
    }

    /**
     * Examples from http://www.jewishgen.org/infofiles/soundex.html
     */
    @Test
    public void testEncodeBasic() {
        Assert.assertEquals("583600", this.getStringEncoder().soundex("GOLDEN"));
        Assert.assertEquals("087930", this.getStringEncoder().soundex("Alpert"));
        Assert.assertEquals("791900", this.getStringEncoder().soundex("Breuer"));
        Assert.assertEquals("579000", this.getStringEncoder().soundex("Haber"));
        Assert.assertEquals("665600", this.getStringEncoder().soundex("Mannheim"));
        Assert.assertEquals("664000", this.getStringEncoder().soundex("Mintz"));
        Assert.assertEquals("370000", this.getStringEncoder().soundex("Topf"));
        Assert.assertEquals("586660", this.getStringEncoder().soundex("Kleinmann"));
        Assert.assertEquals("769600", this.getStringEncoder().soundex("Ben Aron"));

        Assert.assertEquals("097400|097500", this.getStringEncoder().soundex("AUERBACH"));
        Assert.assertEquals("097400|097500", this.getStringEncoder().soundex("OHRBACH"));
        Assert.assertEquals("874400", this.getStringEncoder().soundex("LIPSHITZ"));
        Assert.assertEquals("874400|874500", this.getStringEncoder().soundex("LIPPSZYC"));
        Assert.assertEquals("876450", this.getStringEncoder().soundex("LEWINSKY"));
        Assert.assertEquals("876450", this.getStringEncoder().soundex("LEVINSKI"));
        Assert.assertEquals("486740", this.getStringEncoder().soundex("SZLAMAWICZ"));
        Assert.assertEquals("486740", this.getStringEncoder().soundex("SHLAMOVITZ"));
    }

    /**
     * Examples from http://www.avotaynu.com/soundex.htm
     */
    @Test
    public void testEncodeBasic2() {
        Assert.assertEquals("467000|567000", this.getStringEncoder().soundex("Ceniow"));
        Assert.assertEquals("467000", this.getStringEncoder().soundex("Tsenyuv"));
        Assert.assertEquals("587400|587500", this.getStringEncoder().soundex("Holubica"));
        Assert.assertEquals("587400", this.getStringEncoder().soundex("Golubitsa"));
        Assert.assertEquals("746480|794648", this.getStringEncoder().soundex("Przemysl"));
        Assert.assertEquals("746480", this.getStringEncoder().soundex("Pshemeshil"));
        Assert.assertEquals("944744|944745|944754|944755|945744|945745|945754|945755",
                            this.getStringEncoder().soundex("Rosochowaciec"));
        Assert.assertEquals("945744", this.getStringEncoder().soundex("Rosokhovatsets"));
    }

    /**
     * Examples from http://en.wikipedia.org/wiki/Daitch%E2%80%93Mokotoff_Soundex
     */
    @Test
    public void testEncodeBasic3() {
        Assert.assertEquals("734000|739400", this.getStringEncoder().soundex("Peters"));
        Assert.assertEquals("734600|739460", this.getStringEncoder().soundex("Peterson"));
        Assert.assertEquals("645740", this.getStringEncoder().soundex("Moskowitz"));
        Assert.assertEquals("645740", this.getStringEncoder().soundex("Moskovitz"));
        Assert.assertEquals("154600|145460|454600|445460", this.getStringEncoder().soundex("Jackson"));
        Assert.assertEquals("154654|154645|154644|145465|145464|454654|454645|454644|445465|445464",
                            this.getStringEncoder().soundex("Jackson-Jackson"));
    }

    @Test
    public void testEncodeIgnoreApostrophes() throws EncoderException {
        this.checkEncodingVariations("079600", new String[]{
            "OBrien",
            "'OBrien",
            "O'Brien",
            "OB'rien",
            "OBr'ien",
            "OBri'en",
            "OBrie'n",
            "OBrien'"});
    }

    /**
     * Test data from http://www.myatt.demon.co.uk/sxalg.htm
     *
     * @throws EncoderException
     */
    @Test
    public void testEncodeIgnoreHyphens() throws EncoderException {
        this.checkEncodingVariations("565463", new String[]{
            "KINGSMITH",
            "-KINGSMITH",
            "K-INGSMITH",
            "KI-NGSMITH",
            "KIN-GSMITH",
            "KING-SMITH",
            "KINGS-MITH",
            "KINGSM-ITH",
            "KINGSMI-TH",
            "KINGSMIT-H",
            "KINGSMITH-"});
    }

    @Test
    public void testEncodeIgnoreTrimmable() {
        Assert.assertEquals("746536", this.getStringEncoder().encode(" \t\n\r Washington \t\n\r "));
        Assert.assertEquals("746536", this.getStringEncoder().encode("Washington"));
    }

    @Test
    public void testAdjacentCodes() {
        // AKSSOL
        // A-KS-S-O-L
        // 0-54-4---8 -> wrong
        // 0-54-----8 -> correct
        Assert.assertEquals("054800", this.getStringEncoder().soundex("AKSSOL"));

        // GERSCHFELD
        // G-E-RS-CH-F-E-L-D
        // 5--4/94-5/4-7-8-3 -> wrong
        // 5--4/94-5/--7-8-3 -> correct
        Assert.assertEquals("547830|545783|594783|594578", this.getStringEncoder().soundex("GERSCHFELD"));
    }

    @Test
    public void testAccentedCharacterFolding() {
        Assert.assertEquals("294795", getStringEncoder().soundex("Straßburg"));
        Assert.assertEquals("294795", getStringEncoder().soundex("Strasburg"));

        Assert.assertEquals("095600", getStringEncoder().soundex("Éregon"));
        Assert.assertEquals("095600", getStringEncoder().soundex("Eregon"));
    }

    @Test
    public void testSpecialRomanianCharacters() {
        Assert.assertEquals("364000|464000", this.getStringEncoder().soundex("ţamas")); // t-cedilla
        Assert.assertEquals("364000|464000", this.getStringEncoder().soundex("țamas")); // t-comma
    }
}
