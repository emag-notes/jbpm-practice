package jbpm.practice.workitemhandler;

import org.junit.After;
import org.junit.Before;
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

  private KieSession kSession;
  private KieRuntimeLogger auditLogger;

  @Before
  public void setUp() throws Exception {
    KieServices kieServices = KieServices.Factory.get();
    KieContainer kContainer = kieServices.getKieClasspathContainer();
    kSession = kContainer.newKieSession("WorkItemHandlerTestKS");

    auditLogger = kieServices.getLoggers().newFileLogger(
      kSession, this.getClass().getName() + "#" + testName.getMethodName());
  }

  @After
  public void tearDown() throws Exception {
    auditLogger.close();
  }

  @Test
  public void test1() throws Exception {
    MyWorkItemHandler handler = new MyWorkItemHandler();
    kSession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

    ProcessInstance processInstance = kSession.startProcess("WorkItemHandler_Test");

    assertThat(processInstance, is(notNullValue()));
    assertThat(processInstance.getState(), is(ProcessInstance.STATE_ACTIVE));

    WorkItem workItem = handler.getWorkItem();
    kSession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
    assertThat(processInstance.getState(), is(ProcessInstance.STATE_COMPLETED));
  }

  @Test
  public void test2() throws Exception {
    MyWorkItemHandler handler = new MyWorkItemHandler();
    kSession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

    ProcessInstance processInstance = kSession.startProcess("WorkItemHandler_Test2");

    assertThat(processInstance, is(notNullValue()));
    assertThat(processInstance.getState(), is(ProcessInstance.STATE_ACTIVE));

    WorkItem workItem1 = handler.getWorkItem();
    kSession.getWorkItemManager().completeWorkItem(workItem1.getId(), null);
    assertThat(processInstance.getState(), is(ProcessInstance.STATE_ACTIVE));

    WorkItem workItem2 = handler.getWorkItem();
    kSession.getWorkItemManager().completeWorkItem(workItem2.getId(), null);
    assertThat(processInstance.getState(), is(ProcessInstance.STATE_COMPLETED));
  }

}
