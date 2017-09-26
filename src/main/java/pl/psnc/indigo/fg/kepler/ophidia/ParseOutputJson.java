package pl.psnc.indigo.fg.kepler.ophidia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.psnc.indigo.fg.kepler.helper.Messages;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Actor to parse JSON produced by Ophidia workflow and generate URI of results.
 */
public class ParseOutputJson extends LimitedFiringSource {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ParseOutputJson.class);
    private static final String JOB_ID = "JobID";
    private static final String SESSION_CODE = "Session Code";
    private static final String WORKFLOW = "Workflow";
    private static final String[] EMPTY = new String[0];

    /**
     * Port to receive contents of JSON file from Ophidia.
     */
    private final TypedIOPort jsonPort;

    public ParseOutputJson(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        jsonPort = new TypedIOPort(this, "json", true, false); //NON-NLS
        jsonPort.setTypeEquals(BaseType.STRING);

        output.setName("resultsUri"); //NON-NLS
        output.setTypeEquals(BaseType.STRING);

        PortHelper.makePortNameVisible(jsonPort, output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        final String json = PortHelper.readStringMandatory(jsonPort);

        String[] keys = ParseOutputJson.EMPTY;
        String[] values = ParseOutputJson.EMPTY;
        try {
            final ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(json);
            jsonNode = Optional.ofNullable(jsonNode.get("response"))
                               .orElse(NullNode.getInstance()); //NON-NLS
            jsonNode = Optional.ofNullable(jsonNode.get("source"))
                               .orElse(NullNode.getInstance()); //NON-NLS

            if (jsonNode.hasNonNull("keys") && jsonNode.hasNonNull("values")) {
                keys = mapper.treeToValue(jsonNode.get("keys"),
                                          String[].class); //NON-NLS
                values = mapper.treeToValue(jsonNode.get("values"),
                                            String[].class); //NON-NLS
            }
        } catch (final IOException e) {
            final String message = Messages.getString("failed.to.parse.json");
            ParseOutputJson.LOGGER.error(message, e);
            output.broadcast(new StringToken(""));
            return;
        }

        if (keys.length != values.length) {
            final String message = Messages.getString(
                    "invalid.json.keys.and.values.do.not.match");
            throw new IllegalActionException(this, message);
        }

        final Map<String, String> map = new HashMap<>(keys.length);
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }

        if (!map.containsKey(ParseOutputJson.JOB_ID) ||
            !map.containsKey(ParseOutputJson.SESSION_CODE) ||
            !map.containsKey(ParseOutputJson.WORKFLOW)) {

            final String message =
                    Messages.getString("invalid.json.lack.of.expected.keys");
            ParseOutputJson.LOGGER.error(message);
            output.broadcast(new StringToken(""));
            return;
        }

        final String sessionCode = map.get(ParseOutputJson.SESSION_CODE);
        final String workflowId = map.get(ParseOutputJson.WORKFLOW);
        final String jobId = map.get(ParseOutputJson.JOB_ID);
        final URI uri =
                UriBuilder.fromUri(jobId).replacePath("").replaceQuery("")
                          .path("/thredds/dodsC/indigo/precip_trend_input/")
                          .path(sessionCode).path(workflowId)
                          .path("precip_trend_analysis.nc").fragment(null)
                          .build();
        final String uriString = uri.toString();
        output.broadcast(new StringToken(uriString));
    }
}
