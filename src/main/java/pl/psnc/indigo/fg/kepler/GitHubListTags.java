package pl.psnc.indigo.fg.kepler;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.GitHub;
import pl.psnc.indigo.fg.kepler.helper.Messages;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

/**
 * Actor which lists tags for a given GitHub repository.
 */
@Slf4j
public class GitHubListTags extends LimitedFiringSource {
    private final TypedIOPort repositoryPort;

    public GitHubListTags(
            final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        repositoryPort =
                new TypedIOPort(this, "repository", true, false); //NON-NLS
        repositoryPort.setTypeEquals(BaseType.STRING);
        output.setTypeEquals(new ArrayType(BaseType.STRING));

        PortHelper.makePortNameVisible(repositoryPort, output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String repositoryName = PortHelper.readStringMandatory(repositoryPort);
        GitHubListTags.log.debug("Attempting to list tags for repository {}",
                                 repositoryName);

        GHRepository repository = getRepository(repositoryName);
        List<GHTag> tags = getTags(repositoryName, repository);

        if (tags.isEmpty()) {
            output.broadcast(new ArrayToken(BaseType.STRING));
        }

        Token[] tokens = new Token[tags.size()];
        for (int i = 0; i < tags.size(); i++) {
            GHTag tag = tags.get(i);
            String name = tag.getName();
            tokens[i] = new StringToken(name);
        }
        output.broadcast(new ArrayToken(BaseType.STRING, tokens));
    }

    private List<GHTag> getTags(
            final String repositoryName, final GHRepository repository)
            throws IllegalActionException {

        try {
            return repository.listTags().asList();
        } catch (final IOException e) {
            throw new IllegalActionException(this, e, MessageFormat
                    .format(Messages.getString(
                            "failed.to.list.tags.in.github.repository.0"),
                            repositoryName));
        }
    }

    private GHRepository getRepository(final String repository)
            throws IllegalActionException {
        try {
            GitHub gitHub = GitHub.connectAnonymously();
            return gitHub.getRepository(repository);
        } catch (final IOException e) {
            throw new IllegalActionException(this, e, MessageFormat
                    .format(Messages.getString(
                            "failed.to.get.github.repository.0"), repository));
        }
    }
}
