package jenkins.plugins.mvn_snapshot_check;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.remoting.RemoteOutputStream;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.MasterToSlaveFileCallable;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author donghui 2019/4/24.
 */
public class MavenSnapshotCheck extends Builder implements SimpleBuildStep{
    private static final String POM_FILE = "pom.xml,**/pom.xml";
    private static final String SNAPSHOT = "SNAPSHOT";

    private boolean check;

    @DataBoundConstructor
    public MavenSnapshotCheck(boolean check) {
        this.check = check;
    }

    public boolean getCheck() {
        return check;
    }

    @DataBoundSetter
    public void setCheck(boolean check) {
        this.check = check;
    }

    @Override
    public MavenSnapshotCheck.DescriptorImpl getDescriptor() {
        return (MavenSnapshotCheck.DescriptorImpl)super.getDescriptor();
    }

    /**
     * traditional job
     * @param build
     * @param launcher
     * @param listener
     * @return
     */
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        FilePath workspace = build.getWorkspace();
        if (getCheck()) {
            listener.getLogger().println("[Maven SNAPSHOT Check]");
            PrintStream logger = listener.getLogger();
            final RemoteOutputStream ros = new RemoteOutputStream(logger);
            try {
                Boolean foundText = workspace.act(new FileChecker(ros));
                if(null != foundText && foundText){
                    return false;
                }
            } catch (IOException e) {
                logger.println("Jenkins Maven SNAPSHOT Check Plugin:" + e.getMessage());
            } catch (InterruptedException e) {
                logger.println("Jenkins Maven SNAPSHOT Check Plugin:" + e.getMessage());
            }
        }
        return true;
    }

    /**
     * pipeline plugin
     * @param run
     * @param workspace
     * @param launcher
     * @param taskListener
     * @throws InterruptedException
     * @throws IOException
     */
    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        if (getCheck()) {
            taskListener.getLogger().println("[Maven SNAPSHOT Check]");
            PrintStream logger = taskListener.getLogger();
            final RemoteOutputStream ros = new RemoteOutputStream(logger);
            try {
                Boolean foundText = workspace.act(new FileChecker(ros));
                if(null != foundText && foundText){
                    run.setResult(Result.FAILURE);
                }
            } catch (IOException e) {
                logger.println("Jenkins Maven SNAPSHOT Check Plugin:" + e.getMessage());
            } catch (InterruptedException e) {
                logger.println("Jenkins Maven SNAPSHOT Check Plugin:" + e.getMessage());
            }
        }
        run.setResult(Result.SUCCESS);
    }

    /**
     * Descriptor for {@link MavenSnapshotCheck}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Symbol("mavenSnapshotCheck")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "Maven SNAPSHOT Check";
        }
    }

    /**
     * Search the given regexp pattern in the file.
     * from text-finder-plugin
     */
    private static boolean checkFile(
            File f,
            Pattern pattern,
            PrintStream logger,
            Charset charset) {
        boolean logFilename = true;
        boolean foundText = false;
        BufferedReader reader = null;
        try {
            // Assume default encoding and text files
            String line;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), charset));
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    if (logFilename) {
                        logger.println(f + ":");
                        logFilename = false;
                    }
                    logger.println(line);
                    foundText = true;
                }
            }
        } catch (IOException e) {
            logger.println("Jenkins Maven SNAPSHOT Check Plugin: Error reading file '" + f + "' -- ignoring");
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return foundText;
    }


    /**
     * compilePattern
     * from text-finder-plugin
     * @param logger used to println message
     * @param regexp to match content of file
     * @return Pattern
     */
    private static Pattern compilePattern(PrintStream logger, String regexp) {
        Pattern pattern = null;
        try {
            pattern = Pattern.compile(regexp);
        } catch (PatternSyntaxException e) {
            logger.println(
                    "Jenkins Maven SNAPSHOT Check Plugin: Unable to compile regular expression '" + regexp + "'");
        }
        return pattern;
    }


    /**
     * from text-finder-plugin
     */
    private static class FileChecker extends MasterToSlaveFileCallable<Boolean> {
        private final RemoteOutputStream ros;

        FileChecker(RemoteOutputStream ros) {
            this.ros = ros;
        }

        @Override
        public Boolean invoke(File ws, VirtualChannel channel) throws IOException, InterruptedException {
            PrintStream logger = new PrintStream(ros, false, Charset.defaultCharset().toString());

            // Collect list of files for searching
            FileSet fs = new FileSet();
            Project p = new Project();
            fs.setProject(p);
            fs.setDir(ws);
            fs.setIncludes(POM_FILE);
            DirectoryScanner ds = fs.getDirectoryScanner(p);

            // Any files in the final set?
            String[] files = ds.getIncludedFiles();
            if (files.length == 0) {
                return false ;
            }

            Pattern pattern = compilePattern(logger, SNAPSHOT);

            boolean foundText = false;

            for (String file : files) {
                File f = new File(ws, file);

                if (!f.exists()) {
                    logger.println("Jenkins Maven SNAPSHOT Check Plugin: Unable to find file '" + f + "'");
                    continue;
                }

                if (!f.canRead()) {
                    logger.println("Jenkins Maven SNAPSHOT Check Plugin: Unable to read from file '" + f + "'");
                    continue;
                }
                foundText |= checkFile(f, pattern, logger, Charset.defaultCharset());
            }

            return foundText;
        }
    }
}
