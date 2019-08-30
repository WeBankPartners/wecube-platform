package com.webank.wecube.core.service.workflow.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("srvFailBean")
public class ServiceFailureHandleDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(ServiceFailureHandleDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String parentActivityId = null;
        String parentActivityName = null;

        FlowElement flowElement = execution.getBpmnModelElementInstance();
        ModelElementInstance parentEi = flowElement.getParentElement();
        if (parentEi != null) {
            parentActivityId = parentEi.getAttributeValue("id");
            parentActivityName = parentEi.getAttributeValue("name");
        }
        log.warn("***********************************************************************");
        log.warn("### service FAILED : {}>{}, {}>{}", parentActivityName, execution.getCurrentActivityName(),
                parentActivityId, execution.getCurrentActivityId());
        log.warn("***********************************************************************");
    }

}
