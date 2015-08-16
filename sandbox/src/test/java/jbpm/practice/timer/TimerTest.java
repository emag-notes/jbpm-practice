package jbpm.practice.timer;

import jbpm.practice.workitemhandler.MyWorkItemHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author Yoshimasa Tanabe
 */
public class TimerTest {

  @Rule
  public TestName testName = new TestName();

  private KieSession kSession;
  private KieRuntimeLogger auditLogger;

  @Before
  public void setUp() throws Exception {
    KieServices kieServices = KieServices.Factory.get();
    kSession = createKieSession("jbpm/practice/timer/" + testName.getMethodName() + ".bpmn2", kieServices);

    auditLogger = kieServices.getLoggers().newFileLogger(
      kSession, "target/" + this.getClass().getName() + "#" + testName.getMethodName());
  }

  @After
  public void tearDown() throws Exception {
    auditLogger.close();
  }

  @Test
  public void repeatEverySecond() throws Exception {
    // Start Timer event はプロセスがデプロイされるとすぐに登録され、
    // KieSession#startProcessInstance を呼ぶ必要はない
    List<Long> list = new ArrayList<>();
    kSession.addEventListener(new DefaultProcessEventListener() {
      public void afterProcessStarted(ProcessStartedEvent event) {
        list.add(event.getProcessInstance().getId());
      }
    });

    int intervalSecond = 1;
    int numberOfExecution = 5;
    // JUnit が先に終わってしまうので、実行感覚 * 確認したい回数分スリープしておく
    TimeUnit.SECONDS.sleep(intervalSecond * numberOfExecution);

    assertThat(list.size(), is(numberOfExecution));
  }

  @Ignore
  @Test
  public void repeat1stEveryMonth10AM() throws Exception {
    TimeUnit.MINUTES.sleep(5);
  }

  @Test
  public void timeoutIn5s() throws Exception {
    MyWorkItemHandler handler = new MyWorkItemHandler();
    kSession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

    ProcessInstance processInstance = kSession.startProcess("Timeout_Test");
    assertThat(processInstance.getState(), is(ProcessInstance.STATE_ACTIVE));

    WorkItem workItem = handler.getWorkItem();
    kSession.getWorkItemManager().completeWorkItem(workItem.getId(), null);

    // 5 秒たつとタイムアウトするので、Slow Task を完了せずに 10 秒待つ
    TimeUnit.SECONDS.sleep(10);
    System.out.println("### Firing Timer");

    assertThat(processInstance.getState(), is(ProcessInstance.STATE_COMPLETED));
  }

  @Ignore
  @Test
  public void timeoutAt10AM() throws Exception {
    MyWorkItemHandler handler = new MyWorkItemHandler();
    kSession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

    ProcessInstance processInstance = kSession.startProcess("Timeout_Test");
    assertThat(processInstance.getState(), is(ProcessInstance.STATE_ACTIVE));

    WorkItem workItem = handler.getWorkItem();
    kSession.getWorkItemManager().completeWorkItem(workItem.getId(), null);

    // 10:00 am にタイマーを設定しているため、 9:56 - 9:59 am に実行するとタイムアウト
    TimeUnit.MINUTES.sleep(5);
    System.out.println("### Firing Timer");

    assertThat(processInstance.getState(), is(ProcessInstance.STATE_COMPLETED));
  }

  @Test
  public void delaying() throws Exception {
    kSession.startProcess("Delaying_Test");
    // 5 秒後に Script Task 2 へ遷移する
    TimeUnit.SECONDS.sleep(5 + 1); // 5 秒でも問題ないが、先にテストが終わってしまわないように念のため +1 秒待っておく
  }

  private KieSession createKieSession(String bpmnName, KieServices kieServices) {
    KieRepository kRepository = kieServices.getRepository();
    KieFileSystem kfs = kieServices.newKieFileSystem();
    kfs.write("src/main/resources/" + bpmnName, getBPMN(bpmnName));

    KieBuilder kBuilder = kieServices.newKieBuilder(kfs);
    kBuilder.buildAll();
    KieContainer kContainer = kieServices.newKieContainer(kRepository.getDefaultReleaseId());

    return kContainer.newKieSession();
  }

  private String getBPMN(String bpmnName) {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader reader =
           new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/" + bpmnName)))) {
      while (reader.ready()) {
        sb.append(reader.readLine()).append('\n');
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return sb.toString();
  }

}
