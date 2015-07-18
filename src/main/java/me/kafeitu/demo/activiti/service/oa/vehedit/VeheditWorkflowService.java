package me.kafeitu.demo.activiti.service.oa.vehedit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import me.kafeitu.demo.activiti.entity.oa.Leave;
import me.kafeitu.demo.activiti.service.oa.leave.LeaveManager;
import me.kafeitu.demo.activiti.service.oa.leave.LeaveWorkflowService;
import me.kafeitu.demo.activiti.util.Page;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 请假流程Service
 *
 * @author HenryYan
 */
@Component
@Transactional
public class VeheditWorkflowService {

    private static Logger logger = LoggerFactory.getLogger(LeaveWorkflowService.class);

    private LeaveManager leaveManager;

    private RuntimeService runtimeService;

    protected TaskService taskService;

    protected HistoryService historyService;

    protected RepositoryService repositoryService;

    @Autowired
    private IdentityService identityService;
    
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 启动流程
     *
     * @param entity
     */
    public ProcessInstance startWorkflow(Leave entity, Map<String, Object> variables) {
    	
//    	List query = entityManager.createNativeQuery("select seq_leave.nextval from dual ").getResultList();
//    	String id = query.get(0).toString();
//		entity.setId(Long.valueOf(id));
//		entityManager.
        leaveManager.saveLeave(entity);
        logger.debug("save entity: {}", entity);
        String businessKey = entity.getId().toString();

        ProcessInstance processInstance = null;
        try {
            // 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
            identityService.setAuthenticatedUserId(entity.getUserId());

            processInstance = runtimeService.startProcessInstanceByKey("vehEditProcess", businessKey, variables);
            String processInstanceId = processInstance.getId();
            entity.setProcessInstanceId(processInstanceId);
            logger.debug("start process of {key={}, bkey={}, pid={}, variables={}}", new Object[]{"vehEditProcess", businessKey, processInstanceId, variables});
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
        return processInstance;
    }

    /**
     * 查询待办任务
     *
     * @param userId 用户ID
     * @return
     */
    @Transactional(readOnly = true)
    public List<Leave> findTodoTasks(String userId, Page<Leave> page, int[] pageParams) {
        List<Leave> results = new ArrayList<Leave>();

        // 根据当前人的ID查询
        TaskQuery taskQuery = taskService.createTaskQuery().taskCandidateOrAssigned(userId);
        List<Task> tasks = taskQuery.list();

        // 根据流程的业务ID查询实体并关联
        for (Task task : tasks) {
            String processInstanceId = task.getProcessInstanceId();
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).active().singleResult();
            String businessKey = processInstance.getBusinessKey();
            if (businessKey == null) {
                continue;
            }
            Leave leave = leaveManager.getLeave(new Long(businessKey));
            leave.setTask(task);
            leave.setProcessInstance(processInstance);
            leave.setProcessDefinition(getProcessDefinition(processInstance.getProcessDefinitionId()));
            results.add(leave);
        }

        page.setTotalCount(taskQuery.count());
        page.setResult(results);
        return results;
    }

    /**
     * 读取运行中的流程
     *
     * @return
     */
    @Transactional(readOnly = true)
    public List<Leave> findRunningProcessInstaces(Page<Leave> page, int[] pageParams) {
        List<Leave> results = new ArrayList<Leave>();
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey("vehEditProcess").active().orderByProcessInstanceId().desc();
        List<ProcessInstance> list = query.listPage(pageParams[0], pageParams[1]);

        // 关联业务实体
        for (ProcessInstance processInstance : list) {
            String businessKey = processInstance.getBusinessKey();
            if (businessKey == null) {
                continue;
            }
            Leave leave = leaveManager.getLeave(new Long(businessKey));
            leave.setProcessInstance(processInstance);
            leave.setProcessDefinition(getProcessDefinition(processInstance.getProcessDefinitionId()));
            results.add(leave);

            // 设置当前任务信息
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().orderByTaskCreateTime().desc().listPage(0, 1);
            leave.setTask(tasks.get(0));
        }

        page.setTotalCount(query.count());
        page.setResult(results);
        return results;
    }

    /**
     * 读取已结束中的流程
     *
     * @return
     */
    @Transactional(readOnly = true)
    public List<Leave> findFinishedProcessInstaces(Page<Leave> page, int[] pageParams) {
        List<Leave> results = new ArrayList<Leave>();
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("vehEditProcess").finished().orderByProcessInstanceEndTime().desc();
        List<HistoricProcessInstance> list = query.listPage(pageParams[0], pageParams[1]);

        // 关联业务实体
        for (HistoricProcessInstance historicProcessInstance : list) {
            String businessKey = historicProcessInstance.getBusinessKey();
            Leave leave = leaveManager.getLeave(new Long(businessKey));
            leave.setProcessDefinition(getProcessDefinition(historicProcessInstance.getProcessDefinitionId()));
            leave.setHistoricProcessInstance(historicProcessInstance);
            results.add(leave);
        }
        page.setTotalCount(query.count());
        page.setResult(results);
        return results;
    }

    /**
     * 查询流程定义对象
     *
     * @param processDefinitionId 流程定义ID
     * @return
     */
    protected ProcessDefinition getProcessDefinition(String processDefinitionId) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
        return processDefinition;
    }

    @Autowired
    public void setLeaveManager(LeaveManager leaveManager) {
        this.leaveManager = leaveManager;
    }

    @Autowired
    public void setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Autowired
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Autowired
    public void setHistoryService(HistoryService historyService) {
        this.historyService = historyService;
    }

    @Autowired
    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

}
