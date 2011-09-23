/*
 * Copyright (c) 2010-2011, University of Sussex
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
package uk.ac.susx.mlcl.dttools;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IStringConverterFactory;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.BooleanConverter;
import com.beust.jcommander.converters.FileConverter;
import com.beust.jcommander.converters.IntegerConverter;
import com.beust.jcommander.converters.LongConverter;
import com.beust.jcommander.converters.StringConverter;
import com.beust.jcommander.internal.Maps;
import java.io.File;
import uk.ac.susx.mlcl.lib.tasks.Task;
import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 *
 * @author hiam20
 */
public class DTTools {

    private static final Logger LOG = Logger.getLogger(CopyTask.class.getName());

    @Parameter(names = {"-h", "--help"},
               description = "USAGE_HELP")
    private boolean usageRequested = false;

    public boolean isUsageRequested() {
        return usageRequested;
    }

    enum Command {

        sort(ExtSortTask.class),
        merge(MergeTask.class),
        knn(ExtKnnTask.class),
        allpairs(AllPairsCommand.class),
        count(ExtCountTask.class),
        filter(FilterTask.class);

        private Class<? extends Task> taskClass;

        private Command(Class<? extends Task> taskClass) {
            this.taskClass = taskClass;
        }

        public Class<? extends Task> getTaskClass() {
            return taskClass;
        }
    }

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, Exception {
////
//        args = new String[]{"count"
////                    "allpairs",
////                    "--input", "sampledata/bnc-gramrels-fruit.features",
////                    "--input-contexts", "sampledata/bnc-gramrels-fruit.contexts",
////                    "--input-heads", "sampledata/bnc-gramrels-fruit.heads",
////                    "--output", "sampledata/out/bnc-gramrels-fruit.lin",
////                    "--measure", "lin",
////                    "--charset", "UTF-8",
////                    "--similarity-min", "0.01",
////                    "--chunk-size", "10"
//                };

        if (args == null)
            throw new NullPointerException();

        DTTools dttools = new DTTools();

        JCommander jc = new JCommander();
        jc.setProgramName("dttools");
        jc.addConverterFactory(new ConverterFactory());

        jc.addObject(dttools);
        jc.setDescriptionsBundle(
                ResourceBundle.getBundle("uk.ac.susx.mlcl.dttools.strings"));

        EnumMap<Command, Task> tasks =
                new EnumMap<Command, Task>(Command.class);

        for (Command c : Command.values()) {
            Task t = c.getTaskClass().newInstance();
            jc.addCommand(c.name(), t);
            tasks.put(c, t);
        }

        try {

            jc.parse(args);
            if (dttools.isUsageRequested() || jc.getParsedCommand() == null) {

                jc.usage();

                if (jc.getParsedCommand() != null) {
                    jc.usage(jc.getParsedCommand());
                }

                System.exit(0);
            }


            Task t = tasks.get(Command.valueOf(jc.getParsedCommand()));
            t.run();

            while (t.isExceptionThrown()) {
                t.throwException();
            }

        } catch (ParameterException ex) {
            System.err.println(ex.getMessage());
            StringBuilder sb = new StringBuilder();
            jc.usage(sb);
            if (jc.getParsedCommand() != null) {
                jc.usage(jc.getParsedCommand(), sb);
            }
            System.err.println(sb);
            System.exit(-1);
        }

        // XXX: The following is required when the software is run from the
        // builddt.sh script. Otherwise it hangs, presumably because a thread is
        // still running.
        System.exit(0);
    }

    public static class ConverterFactory implements IStringConverterFactory {

        private final Map<Class<?>, Class<? extends IStringConverter<?>>> conv;

        public ConverterFactory() {
            conv = new HashMap<Class<?>, Class<? extends IStringConverter<?>>>();
            conv.put(Charset.class, CharsetConverter.class);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Class<? extends IStringConverter<T>> getConverter(
                Class<T> forType) {
            return (Class<? extends IStringConverter<T>>) conv.get(forType);
        }
    }

    public static class CharsetConverter implements IStringConverter<Charset> {

        @Override
        public Charset convert(String string) {
            return Charset.forName(string);
        }
    }
}
