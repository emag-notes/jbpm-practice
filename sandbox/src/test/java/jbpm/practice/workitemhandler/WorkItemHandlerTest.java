package jbpm.practice.workitemhandler;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.kie.api.KieServices;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author tanabe
 */
public class WorkItemHandlerTest {

  @Rule
  public TestName testName = new TestName();

  @Test
  public void test1() throws Exception {
    KieServices kieServices = KieServices.Factory.get();
    KieContainer kContainer = kieServices.getKieClasspathContainer();
    KieSession kSession = kContainer.newKieSession("WorkItemHandlerTestKS");

    MyWorkItemHandler handler = new MyWorkItemHandler();
    kSession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

    KieRuntimeLogger auditLogger = kieServices.getLoggers().newFileLogger(
      kSession, this.getClass().getName() + "#" + testName.getMethodName());

    ProcessInstance processInstance = kSession.startProcess("WorkItemHandler_Test");

    assertThat(processInstance, is(notNullValue()));
    assertThat(processInstance.getState(), is(ProcessInstance.STATE_ACTIVE));

    WorkItem workItem = handler.getWorkItem();
    kSession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
    assertThat(processInstance.getState(), is(ProcessInstance.STATE_COMPLETED));

    auditLogger.close();
  }
}
