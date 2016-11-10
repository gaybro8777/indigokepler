package pl.psnc.indigo.fg.kepler.ophidia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.psnc.indigo.fg.kepler.helper.AllowedPublicField;
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

/**
 * Actor to parse JSON produced by Ophidia workflow and generate URI of results.
 */
public class ParseOutputJson extends LimitedFiringSource {
    private static final String JOB_ID = "JobID";
    private static final String SESSION_CODE = "Session Code";
    private static final String WORKFLOW = "Workflow";
    private static final int DEFAULT_PORT = 8080;
    /**
     * Port to receive contents of JSON file from Ophidia.
     */
    @AllowedPublicField
    public TypedIOPort jsonPort;

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

        String json = PortHelper.readStringMandatory(jsonPort);

        String[] keys;
        String[] values;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(json);
            jsonNode = jsonNode.get("response"); //NON-NLS
            jsonNode = jsonNode.get("source"); //NON-NLS
            keys = mapper.treeToValue(jsonNode.get("keys"),
                                      String[].class); //NON-NLS
            values = mapper.treeToValue(jsonNode.get("values"),
                                        String[].class); //NON-NLS
        } catch (final IOException e) {
            String message = Messages.getString("failed.to.parse.json");
            throw new IllegalActionException(this, e, message);
        }

        if (keys.length != values.length) {
            String message = Messages.getString(
                    "invalid.json.keys.and.values.do.not.match");
            throw new IllegalActionException(this, message);
        }

        Map<String, String> map = new HashMap<>(keys.length);
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }

        if (!map.containsKey(ParseOutputJson.JOB_ID) || !map
                .containsKey(ParseOutputJson.SESSION_CODE) || !map
                .containsKey(ParseOutputJson.WORKFLOW)) {
            String message =
                    Messages.getString("invalid.json.lack.of.expected.keys");
            throw new IllegalActionException(this, message);
        }

        String sessionCode = map.get(ParseOutputJson.SESSION_CODE);
        String workflowId = map.get(ParseOutputJson.WORKFLOW);
        String jobId = map.get(ParseOutputJson.JOB_ID);
        URI uri = UriBuilder.fromUri(jobId).replacePath("").replaceQuery("")
                            .port(ParseOutputJson.DEFAULT_PORT)
                            .path("/thredds/dodsC/indigo/precip_trend_input/")
                            .path(sessionCode).path(workflowId)
                            .path("precip_trend_analysis.nc").fragment(null)
                            .build();
        String uriString = uri.toString();
        output.broadcast(new StringToken(uriString));
    }
}
