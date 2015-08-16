package jbpm.practice.workitemhandler;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWorkItemHandler implements WorkItemHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(MyWorkItemHandler.class);

  private WorkItem workItem;

  @Override
  public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
    LOGGER.info("### executeWorkItem");

    if (! workItem.getParameters().isEmpty()) {
      workItem.getParameters().forEach((k, v) -> {
        LOGGER.info(" - " + k + " => " + v);
      });
    }

    this.workItem = workItem;
  }

  @Override
  public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
    LOGGER.error("### abortWorkItem");
  }

  public WorkItem getWorkItem() {
    return workItem;
  }

}
