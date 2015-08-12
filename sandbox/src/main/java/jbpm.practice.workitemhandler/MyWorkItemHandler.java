package jbpm.practice.workitemhandler;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class MyWorkItemHandler implements WorkItemHandler {

  private WorkItem workItem;

  @Override
  public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
    System.out.println("### executeWorkItem");
    if (workItem.getParameters().isEmpty()) {

    } else {
      workItem.getParameters().forEach((k, v) -> {
        System.out.println(" - " + k + " => " + v);
      });
    }

    this.workItem = workItem;
  }

  @Override
  public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
    System.out.println("### abortWorkItem");
  }

  public WorkItem getWorkItem() {
    return workItem;
  }

}
