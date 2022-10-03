package jenkins.plugins.mvn_snapshot_check;

import hudson.EnvVars;
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
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.*;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author donghui 2019/4/24.
 */
public class MavenSnapshotCheck extends Builder implements SimpleBuildStep{
    private static final String PLUGIN_NAME = "Jenkins Maven SNAPSHOT Check Plugin";
    private static final String DEFAULT_POM_FILES = "pom.xml,**/pom.xml";
    private static final String SNAPSHOT = "SNAPSHOT";
    private String CHECKED = "Yes, it was checked."; // NOSONAR
    private String NOT_CHECKED = "No, it wasn't checked."; // NOSONAR

    private boolean check;
    private String pomFiles;

    @DataBoundConstructor
    public MavenSnapshotCheck(boolean check, String pomFiles) {
        this.check = check;
        this.pomFiles = pomFiles;
    }

    public boolean getCheck() {
        return check;
    }

    @DataBoundSetter
    public void setCheck(boolean check) {
        this.check = check;
    }

    public String getPomFiles() {
        if(StringUtils.isNotEmpty(pomFiles)){
            return pomFiles;
        }
        return DEFAULT_POM_FILES;
    }

    @DataBoundSetter
    public void setPomFiles(String pomFiles) {
        this.pomFiles = pomFiles;
    }

    @Override
    public MavenSnapshotCheck.DescriptorImpl getDescriptor() {
        return (MavenSnapshotCheck.DescriptorImpl)super.getDescriptor();
    }

    /**
     * traditional job
     * @param build a build this is running as a part of
     * @param launcher a way to start processes
     * @param listener a place to send output
     * @return
     */
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        FilePath workspace = build.getWorkspace();
        if (getCheck()) {
            build.addAction(new MavenSnapshotCheckAction(CHECKED));
            String message = "[Maven SNAPSHOT Check], pomFiles: " + getPomFiles();
            listener.getLogger().println(message);
            PrintStream logger = listener.getLogger();
            final RemoteOutputStream ros = new RemoteOutputStream(logger);
            try {
                Boolean foundText = workspace.act(new FileChecker(ros, getPomFiles())); // NOSONAR
                if(null != foundText && foundText){
                    return false;
                }
            } catch (IOException e) {
                message = PLUGIN_NAME + ":" + e.getMessage();
                logger.println(message);
            } catch (InterruptedException e) {
                message = PLUGIN_NAME + ":" + e.getMessage();
                logger.println(message);
                Thread.currentThread().interrupt();
            }
        }else {
            build.addAction(new MavenSnapshotCheckAction(NOT_CHECKED));
        }
        return true;
    }

    /**
     * pipeline plugin
     * @param run a build this is running as a part of
     * @param workspace a workspace to use for any file operations
     * @param env environment variables applicable to this step
     * @param launcher a way to start processes
     * @param listener a place to send output
     */
    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env, @NonNull Launcher launcher,
                        @NonNull TaskListener listener) {
        if (getCheck()) {
            run.addAction(new MavenSnapshotCheckAction(CHECKED));
            String message = "[Maven SNAPSHOT Check], pomFiles: " + getPomFiles();
            listener.getLogger().println(message);
            PrintStream logger = listener.getLogger();
            final RemoteOutputStream ros = new RemoteOutputStream(logger);
            try {
                Boolean foundText = workspace.act(new FileChecker(ros, getPomFiles()));
                if(null != foundText && foundText){
                    run.setResult(Result.FAILURE);
                    throw new MavenSnapshotCheckException("Maven SNAPSHOT Check Failed!", null, false, false);
                }
            } catch (IOException e) {
                message = PLUGIN_NAME + ":" + e.getMessage();
                logger.println(message);
            } catch (InterruptedException e){
                message = PLUGIN_NAME + ":" + e.getMessage();
                logger.println(message);
                Thread.currentThread().interrupt();
            }
        }else {
            run.addAction(new MavenSnapshotCheckAction(NOT_CHECKED));
        }
        run.setResult(Result.SUCCESS);
    }

    /**
     * Descriptor for {@link MavenSnapshotCheck}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <code>src/main/resources/jenkins/plugins/mvn_snapshot_check/MavenSnapshotCheck/config.jelly</code>
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
     * from text-finder-plugin
     */
    private static class FileChecker extends MasterToSlaveFileCallable<Boolean> {
        private final RemoteOutputStream ros;
        private String includePomFiles;

        FileChecker(RemoteOutputStream ros, String includePomFiles) {
            this.ros = ros;
            this.includePomFiles = includePomFiles;
        }

        @Override
        public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
            boolean foundText = false;
            try(PrintStream logger = new PrintStream(ros, false, Charset.defaultCharset().toString())) {

                // Collect list of files for searching
                FileSet fs = new FileSet();
                Project p = new Project();
                fs.setProject(p);
                fs.setDir(ws);
                fs.setIncludes(includePomFiles);
                DirectoryScanner ds = fs.getDirectoryScanner(p);

                // Any files in the final set?
                String[] files = ds.getIncludedFiles();
                if (files.length == 0) {
                    return false;
                }

                Pattern pattern = compilePattern(logger, SNAPSHOT);

                for (String file : files) {
                    File f = new File(ws, file);

                    if (!f.exists()) {
                        String message = "Jenkins Maven SNAPSHOT Check Plugin: Unable to find file '" + f + "'";
                        logger.println(message);
                        continue;
                    }

                    if (!f.canRead()) {
                        String message = "Jenkins Maven SNAPSHOT Check Plugin: Unable to read from file '" + f + "'";
                        logger.println(message);
                        continue;
                    }
                    foundText |= checkFile(f, pattern, logger, Charset.defaultCharset()); //NOSONAR
                }
            }

            return foundText;
        }

        /**
         * Search the given regexp pattern in the file.
         * from text-finder-plugin
         */
        private boolean checkFile(
                File f,
                Pattern pattern,
                PrintStream logger,
                Charset charset) {
            boolean logFilename = true;
            boolean foundText = false;

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), charset));) {
                // Assume default encoding and text files
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        if (logFilename) {
                            String message = f + ":";
                            logger.println(message);
                            logFilename = false;
                        }
                        String message = line;
                        logger.println(message);
                        foundText = true;
                    }
                }
            } catch (IOException e) {
                String message = "Jenkins Maven SNAPSHOT Check Plugin: Error reading file '" + f + "' -- ignoring";
                logger.println(message);
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
        private Pattern compilePattern(PrintStream logger, String regexp) {
            Pattern pattern = null;
            try {
                pattern = Pattern.compile(regexp);
            } catch (PatternSyntaxException e) {
                String message = "Jenkins Maven SNAPSHOT Check Plugin: Unable to compile regular expression '" + regexp + "'";
                logger.println(message);
            }
            return pattern;
        }

    }
}
