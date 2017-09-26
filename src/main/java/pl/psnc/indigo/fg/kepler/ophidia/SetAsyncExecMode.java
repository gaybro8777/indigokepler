package pl.psnc.indigo.fg.kepler.ophidia;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import pl.psnc.indigo.fg.kepler.helper.Messages;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An actor which reads Ophidia workflow in JSON, changes exec mode to
 * asynchronous and save JSON in another file.
 */
public class SetAsyncExecMode extends LimitedFiringSource {
    private static final Pattern PATTERN =
            Pattern.compile("\\s+\"exec_mode\":\\s*\"sync\"");

    private final TypedIOPort tempPathPort;
    private final TypedIOPort jsonFilePort;

    public SetAsyncExecMode(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        tempPathPort = new TypedIOPort(this, "tempPath", true, false);
        tempPathPort.setTypeEquals(BaseType.STRING);
        jsonFilePort = new TypedIOPort(this, "jsonFile", true, false);
        jsonFilePort.setTypeEquals(BaseType.STRING);
        output.setTypeEquals(BaseType.STRING);

        PortHelper.makePortNameVisible(tempPathPort, jsonFilePort, output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        final String tempPath = PortHelper.readStringOptional(tempPathPort);
        final File jsonFile =
                new File(PortHelper.readStringMandatory(jsonFilePort));
        try {
            /* read & transform the JSON */
            final String json = FileUtils
                    .readFileToString(jsonFile, Charset.defaultCharset());
            final Matcher matcher = SetAsyncExecMode.PATTERN.matcher(json);
            final String jsonAsync =
                    matcher.replaceFirst("\"exec_mode\" :\"async\"");

            /* write it in a temporary file */
            final File file;
            if (StringUtils.isBlank(tempPath)) {
                file = File.createTempFile("SetAsyncExecMode", ".json");
            } else {
                file = File.createTempFile("SetAsyncExecMode", ".json",
                                           new File(tempPath));
            }
            FileUtils.write(file, jsonAsync, Charset.defaultCharset());

            output.broadcast(new StringToken(file.getAbsolutePath()));
        } catch (final IOException e) {
            throw new IllegalActionException(this, e, Messages.format(
                    "failed.to.set.async.mode.for.ophidia.workflow.0",
                    jsonFile));
        }
    }
}
