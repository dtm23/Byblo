/*
 * Copyright (c) 2010-2012, University of Sussex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of the University of Sussex nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.susx.mlcl.byblo.commands;

import com.google.common.io.Files;
import java.io.File;
import java.nio.charset.Charset;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.byblo.Tools;
import uk.ac.susx.mlcl.lib.test.ExitTrapper;
import static uk.ac.susx.mlcl.lib.test.ExitTrapper.*;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class FilterCommandTest {

    private static final String SUBJECT = FilterCommand.class.getName();

    private final static File OUTPUT_ENTRIES = new File(TEST_OUTPUT_DIR,
                                                        TEST_FRUIT_ENTRIES_FILTERED.getName());

    private final static File OUTPUT_FEATURES = new File(TEST_OUTPUT_DIR,
                                                         TEST_FRUIT_FEATURES_FILTERED.getName());

    private final static File OUTPUT_ENTRY_FEATURES = new File(TEST_OUTPUT_DIR,
                                                               TEST_FRUIT_EVENTS_FILTERED.getName());

    @Before
    public void setUp() {
        OUTPUT_ENTRIES.delete();
        OUTPUT_FEATURES.delete();
        OUTPUT_ENTRY_FEATURES.delete();
    }

    @After
    public void tearDown() {
    }

    private void runWithCLI(String[] runArgs) throws Exception {

        String[] commonArgs = {
            "filter",
            "--charset", "UTF-8",
            "--input-entries", TEST_FRUIT_ENTRIES.toString(),
            "--input-features", TEST_FRUIT_FEATURES.toString(),
            "--input-events", TEST_FRUIT_EVENTS.toString(),
            "--output-entries", OUTPUT_ENTRIES.toString(),
            "--output-features", OUTPUT_FEATURES.toString(),
            "--output-events", OUTPUT_ENTRY_FEATURES.toString(),};

        String[] args = new String[commonArgs.length + runArgs.length];
        System.arraycopy(commonArgs, 0, args, 0, commonArgs.length);
        System.arraycopy(runArgs, 0, args, commonArgs.length, runArgs.length);


        try {
            enableExistTrapping();
            Tools.main(args);
        } finally {
            disableExitTrapping();
        }

        assertTrue("Output file " + OUTPUT_ENTRIES + " does not exist.",
                   OUTPUT_ENTRIES.exists());
        assertTrue("Output file " + OUTPUT_FEATURES + " does not exist.",
                   OUTPUT_FEATURES.exists());
        assertTrue("Output file " + OUTPUT_ENTRY_FEATURES + " does not exist.",
                   OUTPUT_ENTRY_FEATURES.exists());
    }

    @Test
    public void testMainMethodRun_fruit_entryFreqFilter() throws Exception {
        System.out.println(
                "Testing " + SUBJECT + ": on fruit, from main method, filter by Entry freqency.");

        runWithCLI(new String[]{"--filter-entry-freq", "50"});
    }

    @Test
    public void testMainMethodRun_fruit_featureFreqFilter() throws Exception {
        System.out.println(
                "Testing " + SUBJECT + " on fruit from main method, filter by context freqency.");

        runWithCLI(new String[]{"--filter-feature-freq", "50"});
    }

    @Test
    public void testMainMethodRun_fruit_eventFreqFilter() throws Exception {
        System.out.println(
                "Testing FilterTask on fruit from main method, filter by feature freqency.");

        runWithCLI(new String[]{"--filter-event-freq", "5"});
    }

    @Test
    public void testMainMethodRun_fruit_EntryWhitelistFilter() throws Exception {
        System.out.println(
                "Testing FilterTask: on fruit, from main method, filter by entry word list.");

        File entryWorldList = new File(TEST_OUTPUT_DIR,
                                       TEST_FRUIT_INPUT.getName() + ".entry-whitelist");

        Files.write("apple\norange\npear\nbanana", entryWorldList, Charset.forName("UTF-8"));

        runWithCLI(new String[]{"--filter-entry-whitelist",
                    entryWorldList.toString()});
    }

    @Test
    public void testMainMethodRun_fruit_featureWhitelistFilter() throws Exception {
        System.out.println(
                "Testing FilterTask: on fruit, from main method, filter by context word list.");

        File contextWorldList = new File(TEST_OUTPUT_DIR, TEST_FRUIT_INPUT.getName() + ".contextWordList");

        Files.write("det:the\ndet:a\niobj:of\nncmod:back\nncmod:for\npassive",
                    contextWorldList, Charset.forName("UTF-8"));

        runWithCLI(new String[]{"--filter-feature-whitelist", contextWorldList.toString()});
    }

    @Test
    public void testMainMethodRun_fruit_entryPatternFilter() throws Exception {
        System.out.println(
                "Testing FilterTask: on fruit, from main method, filter by entry pattern.");

        runWithCLI(new String[]{"--filter-entry-pattern", "^.{0,5}$"});
    }

    @Test
    public void testMainMethodRun_fruit_contextPatternFilter() throws Exception {
        System.out.println(
                "Testing FilterTask: on fruit, from main method, filter by context pattern.");

        runWithCLI(new String[]{"--filter-feature-pattern", "det:.*"});
    }

    @Test
    public void testMainMethodRun_fruit_allFilters() throws Exception {
        System.out.println(
                "Testing FilterTask: on fruit, from main method, using all filters.");

        File entryWorldList = new File(TEST_OUTPUT_DIR,
                                       FRUIT_NAME + ".allFilters-EntryList");
        File contextWorldList = new File(TEST_OUTPUT_DIR,
                                         FRUIT_NAME + ".allFilters-contextWordList");

        Files.write("apple\napricot\navocado\nbanana\nbilberry\nblackberry\n"
                + "blackcap\nblackcurrant\nblueberry\ncantaloupe\ncherry\n"
                + "clementine\ncurrant\ndamson\ndate\ndurian\neggplant\n"
                + "elderberry\ngooseberry\ngrape\ngrapefruit\nhuckleberry\n"
                + "kumquat", entryWorldList, Charset.forName("UTF-8"));

        Files.write("det:the\ndet:a\niobj:of\nncmod:back\nncmod:for\npassive",
                    contextWorldList, Charset.forName("UTF-8"));

        runWithCLI(new String[]{
                    "--filter-entry-whitelist", entryWorldList.toString(), // a-k
                    "--filter-entry-freq", "10",
                    "--filter-feature-freq", "5",
                    "--filter-event-freq", "2",
                    "--filter-feature-pattern", ":",
                    "--filter-feature-whitelist", contextWorldList.toString(),
                    "--filter-entry-pattern", "a"
                });
    }

    @Test
    public void testExitStatus() throws Exception {
        try {
            ExitTrapper.enableExistTrapping();
            Tools.main(new String[]{"filter"});
        } catch (ExitTrapper.ExitException ex) {
            assertTrue("Expecting non-zero exit status.", ex.getStatus() != 0);
        } finally {
            ExitTrapper.disableExitTrapping();
        }
    }

}
