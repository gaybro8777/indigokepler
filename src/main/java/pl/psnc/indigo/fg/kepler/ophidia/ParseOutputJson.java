package pl.psnc.indigo.fg.kepler.ophidia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.psnc.indigo.fg.kepler.helper.AllowedPublicField;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;

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

        jsonPort = new TypedIOPort(this, "json", true, false);
        jsonPort.setTypeEquals(BaseType.STRING);
        new SingletonAttribute(jsonPort, "_showName");

        output.setName("resultsUri");
        output.setTypeEquals(BaseType.STRING);
        new SingletonAttribute(output, "_showName");
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
            jsonNode = jsonNode.get("response");
            jsonNode = jsonNode.get("source");
            keys = mapper.treeToValue(jsonNode.get("keys"), String[].class);
            values = mapper.treeToValue(jsonNode.get("values"), String[].class);
        } catch (IOException e) {
            throw new IllegalActionException(this, e, "Failed to parse JSON");
        }

        if (keys.length != values.length) {
            throw new IllegalActionException(this,
                                             "Invalid JSON: keys and values "
                                             + "do not match");
        }

        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }

        if (!map.containsKey(ParseOutputJson.JOB_ID) || !map
                .containsKey(ParseOutputJson.SESSION_CODE) || !map
                .containsKey(ParseOutputJson.WORKFLOW)) {
            throw new IllegalActionException(this,
                                             "Invalid JSON: lack of expected "
                                             + "keys");
        }

        String sessionCode = map.get(ParseOutputJson.SESSION_CODE);
        String workflowId = map.get(ParseOutputJson.WORKFLOW);
        URI uri = UriBuilder.fromUri(map.get(ParseOutputJson.JOB_ID))
                            .replacePath("").replaceQuery("")
                            .port(ParseOutputJson.DEFAULT_PORT)
                            .path("/thredds/dodsC/indigo/precip_trend_input/")
                            .path(sessionCode).path(workflowId)
                            .path("precip_trend_analysis.nc").build();
        output.broadcast(new StringToken(uri.toString()));
    }
}
