import hudson.model.*;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.net.URL;

public class MavenSnapshotCheckTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testPipelineSuccess() throws Exception {
        URL zipFile = getClass().getResource("test-success.zip");
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-pipeline-success");
        String pipelineScript =
                "node {\n" +
                "  unzip '" + zipFile.getPath() + "'\n" +
                "  mavenSnapshotCheck check: 'true' \n" +
                "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun run = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkins.waitForCompletion(run);
        jenkins.assertLogContains("SNAPSHOT", run);
    }

    @Test
    public void testPipelineFailure() throws Exception {
        URL zipFile = getClass().getResource("test-failure.zip");

        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-pipeline-failure");
        String pipelineScript =
                "node {\n" +
                        "  unzip '" + zipFile.getPath() + "'\n" +
                        "  mavenSnapshotCheck check: 'true' \n" +
                        "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun run = job.scheduleBuild2(0).waitForStart();
        Assert.assertNotNull(run);
        jenkins.waitForCompletion(run);
        jenkins.assertBuildStatus(Result.FAILURE, run);
        jenkins.assertLogContains("SNAPSHOT", run);
    }

}