package kic.pipeline.sources.task;

import groovy.text.GStringTemplateEngine;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ShellTask extends Task {
    private static final Logger LOG = LoggerFactory.getLogger(ShellTask.class);
    private static final String SPLIT_REGEX = "(\\r\\n)+|\\n+";

    private final GStringTemplateEngine commandlineTemplateEngine = new GStringTemplateEngine();
    private final String jobId;
    private final String pipeEncoding;
    private final String schedule;
    private final File workingDirectory;
    private final List<String> command;
    private final List<String> keyExtractCommand;
    private final List<String> valueExtractCommand;
    private final BiConsumer<String, String> keyValueConsumer;
    private final Supplier<Map> getJobState;
    private final Consumer<Exception> exceptionHandler;
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
        this(jobId, pipeEncoding, schedule, workingDirectory, command, keyExtractCommand, valueExtractCommand, keyValueConsumer, null, null);
    }

    public ShellTask(String jobId,
                     String pipeEncoding,
                     String schedule,
                     File workingDirectory,
                     List<String> command,
                     List<String> keyExtractCommand,
                     List<String> valueExtractCommand,
                     BiConsumer<String, String> keyValueConsumer,
                     Supplier<Map> getJobState,
                     Consumer<Exception> exceptionHandler
    ) {
        this.jobId = jobId;
        this.pipeEncoding = pipeEncoding;
        this.schedule = schedule;
        this.workingDirectory = workingDirectory;
        this.keyValueConsumer = keyValueConsumer;
        this.command = command;
        this.keyExtractCommand = keyExtractCommand;
        this.valueExtractCommand = valueExtractCommand;
        this.getJobState = getJobState != null ? getJobState : HashMap::new;
        this.exceptionHandler = exceptionHandler != null ? exceptionHandler : this::throwAsRuntimeException;
    }


    @Override
    public void execute(TaskExecutionContext context) throws RuntimeException {
        Map jobState = getJobState.get();
        SimpleProcess dataCommand = new SimpleProcess(generateProcessCommand(command, jobState));
        SimpleProcess getKeyCommand = new SimpleProcess(generateProcessCommand(keyExtractCommand, jobState));
        SimpleProcess getValueCommand = new SimpleProcess(generateProcessCommand(valueExtractCommand, jobState));

        try {
            SimpleProcess.ProcessResult dataResult = dataCommand.execute(workingDirectory, new byte[0]);
            dataResult.retrowExceptionIfCaughtWithMsg(jobId);

            SimpleProcess.ProcessResult keysResult = getKeyCommand.execute(workingDirectory, dataResult.stdOut);
            keysResult.retrowExceptionIfCaughtWithMsg(jobId);

            SimpleProcess.ProcessResult valuesResult = getValueCommand.execute(workingDirectory, dataResult.stdOut);
            valuesResult.retrowExceptionIfCaughtWithMsg(jobId);

            LOG.debug("{} out:\n{}\nkeys: {}, values: {}", jobId, dataResult, keysResult, valuesResult);
            List<String> keys = extractLines(keysResult.stdOut, pipeEncoding);
            List<String> values = extractLines(valuesResult.stdOut, pipeEncoding);

            validateKeyValuePairs(keys, values);
            pushKeyValuePairs(keys, values);
        } catch (Exception e) {
            exceptionHandler.accept(e);
        } finally {
            LOG.debug("{} set last job state to {}", jobId, jobState);
        }
    }

    private String[] generateProcessCommand(List<String> command, Map variables) {
        return command.stream()
                      .map(c -> substituteVariables(c, variables))
                      .toArray(i -> new String[i]);
    }

    private String substituteVariables(String command, Map variables) {
        try {
            return commandlineTemplateEngine.createTemplate(command)
                                            .make(variables)
                                            .toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> extractLines(final byte[] input, final String encoding) {
        try {
            return filterNonEmpty(new String(input, encoding).split(SPLIT_REGEX));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> filterNonEmpty(String[] elements) {
        return Arrays.stream(elements)
                     .map(this::trim)
                     .filter(e -> e.length() > 0)
                     .collect(Collectors.toList());
    }

    private String trim(String s) {
        return s.replaceAll("^\\s+|\\s+$", "");
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

    private void throwAsRuntimeException(Exception e) {
        throw new RuntimeException(e);
    }


    public String getJobId() {
        return jobId;
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

}
