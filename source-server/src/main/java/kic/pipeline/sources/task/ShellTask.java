package kic.pipeline.sources.task;

import groovy.text.GStringTemplateEngine;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ShellTask extends Task {
    private static final Logger LOG = LoggerFactory.getLogger(ShellTask.class);
    private static final String SPLIT_REGEX = "(\\r\\n)+|\\n+";

    private final GStringTemplateEngine commandlineTemplateEngine = new GStringTemplateEngine();
    private final Map variableBindings = new HashMap();
    private final String jobId;
    private final String pipeEncoding;
    private final String schedule;
    private final File workingDirectory;
    private final String[] command;
    private final String[] keyExtractCommand;
    private final String[] valueExtractCommand;
    private final BiConsumer<String, String> keyValueConsumer;
    private String scheduleId = null;

    public ShellTask(String jobId,
                     String pipeEncoding,
                     String schedule,
                     File workingDirectory,
                     List<String> command,
                     List<String> keyExtractCommand,
                     List<String> valueExtractCommand,
                     BiConsumer<String, String> keyValueConsumer
    ) {
        variableBindings.put("CLASS_PATH", System.getProperty("java.class.path"));
        this.jobId = jobId;
        this.pipeEncoding = pipeEncoding;
        this.schedule = schedule;
        this.workingDirectory = workingDirectory;
        this.keyValueConsumer = keyValueConsumer;
        this.command = generateProcessCommand(command);
        this.keyExtractCommand = generateProcessCommand(keyExtractCommand);
        this.valueExtractCommand = generateProcessCommand(valueExtractCommand);
    }


    @Override
    public void execute(TaskExecutionContext context) throws RuntimeException {
        ProcessBuilder commandProcessBuilder = new ProcessBuilder(command);
        ProcessBuilder extractKeyProcessBuilder = new ProcessBuilder(keyExtractCommand);
        ProcessBuilder extractValueProcessBuilder = new ProcessBuilder(valueExtractCommand);

        setWorkingDirectory(commandProcessBuilder, extractKeyProcessBuilder, extractValueProcessBuilder);

        try {
            Process process = commandProcessBuilder.start();
            Process keyProcess = extractKeyProcessBuilder.start();
            Process valueProcess = extractValueProcessBuilder.start();

            OutputStream logErrProcess = new LogStream(jobId + Arrays.toString(command));
            OutputStream logErrKey = new LogStream(jobId + "(key)" + Arrays.toString(keyExtractCommand));
            OutputStream logErrValue = new LogStream(jobId + "(value)" + Arrays.toString(valueExtractCommand));
            OutputStream cmdOut = new ByteArrayOutputStream();
            OutputStream tee = new TeeOutputStream(keyProcess.getOutputStream(), new TeeOutputStream(valueProcess.getOutputStream(), cmdOut));

            try {
                IOUtils.copy(process.getInputStream(), tee); // intentionally we want to read stdout of one stream and pass it to the stdin of the other
                IOUtils.copy(process.getErrorStream(), logErrProcess);
                IOUtils.copy(keyProcess.getErrorStream(), logErrKey);
                IOUtils.copy(valueProcess.getErrorStream(), logErrValue);

                List<String> keys = extractLines(keyProcess.getInputStream(), pipeEncoding);
                List<String> values = extractLines(valueProcess.getInputStream(), pipeEncoding);

                awaitProcesses(process);
                tee.flush();
                awaitProcesses(keyProcess, valueProcess);

                LOG.debug("{} out:\n{}\nkeys: {}, values: {}", jobId, cmdOut, keys, values);
                validateKeyValuePairs(keys, values);
                pushKeyValuePairs(keys, values);

                // TODO/FIXME we need to keep state ...
            } finally {
                closeStreams(tee, logErrProcess, logErrKey, logErrValue);
            }
        } catch (IOException e) {
            throw new RuntimeException(toString() + " cannot be started", e);
        }
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    @Override
    public String toString() {
        return jobId;
    }


    private String[] generateProcessCommand(List<String> command) {
        return command.stream()
                      .map(this::substituteVariables)
                      .toArray(i -> new String[i]);
    }

    private String substituteVariables(String command) {
        // FIXME introduce groovy variable substituting here, this is where we need to read some state
        try {
            return commandlineTemplateEngine.createTemplate(command)
                                            .make(variableBindings)
                                            .toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setWorkingDirectory(ProcessBuilder... builders) {
        for (ProcessBuilder builder : builders) {
            builder.directory(workingDirectory);
        }
    }

    private void awaitProcesses(Process... processes) {
        for (int i = 0; i < processes.length; i++) {
            LOG.debug("process {}/{} exited: {}", i + 1, processes.length, awaitProcess(processes[i]));
        }
    }

    private int awaitProcess(Process process) {
        int returnCode = -1;
        try {
            returnCode = process.waitFor();
        } catch (InterruptedException e) {
            LOG.error(toString() + " interupted", e);
        } finally {
            return returnCode;
        }
    }

    private void closeStreams(OutputStream... streams) {
        for (int i = 0; i < streams.length; i++) {
            closeStream(streams[i]);
        }
    }

    private void closeStream(OutputStream stream) {
        try {
            stream.close();
        } catch (IOException e) {
            LOG.trace("unable to close stream " + stream, e);
        }
    }

    private List<String> extractLines(final InputStream input, final String encoding) throws IOException {
        return filterNonEmpty(IOUtils.toString(input, encoding).split(SPLIT_REGEX));
    }

    private List<String> filterNonEmpty(String[] elements) {
        return Arrays.stream(elements)
                     .map(this::trim)
                     .filter(e -> e.length() > 0)
                     .collect(Collectors.toList());
    }

    private String trim(String s) {
        return s.replaceAll("^\\s+|\\s$", "");
    }

    private void validateKeyValuePairs(List<String> keys, List<String> values) {
        if (keys.size() != values.size())
            throw new RuntimeException("keys size different from values size " + keys.size() + "/" + values.size());
    }

    private void pushKeyValuePairs(List<String> keys, List<String> values) {
        if (keyValueConsumer != null) {
            for (int i = 0; i < keys.size(); i++) {
                keyValueConsumer.accept(keys.get(i), values.get(i));
            }
        }
    }

    private class LogStream extends OutputStream {
        final StringBuilder buffer = new StringBuilder();
        final String id;

        public LogStream(String id) {
            this.id = id;
        }

        @Override
        public void write(int b) {
            buffer.append((char) b);
        }

        @Override
        public void close() throws IOException {
            super.close();
            if (buffer.length() > 0) {
                LOG.error("Std-Error of {}\n{}", id, buffer.toString());
            }
        }
    }
}
