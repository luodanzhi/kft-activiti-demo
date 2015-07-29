package me.kafeitu.demo.activiti.lazy.query;

import java.util.List;

import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.cmd.CustomSqlExecution;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import me.kafeitu.demo.activiti.lazy.entity.RunningTask;
import me.kafeitu.demo.activiti.lazy.mapper.TaskQueryMapper;
import me.kafeitu.demo.activiti.service.AbstractTest;

public class CustomQueryTest extends AbstractTest {
    
    @Test
    public void testCustomSql() {
        
        final String processKey = null;
        CustomSqlExecution<TaskQueryMapper, List<RunningTask>> customSqlExecution = new AbstractCustomSqlExecution<TaskQueryMapper, List<RunningTask>>(
                TaskQueryMapper.class){

            public List<RunningTask> execute(TaskQueryMapper customMapper){

                // 使用内置实体对象查询
                // List<TaskEntity> taskByVariable = customMapper.findTasks("applyUserId");

                List<RunningTask> tasks;
                if(StringUtils.isBlank(processKey)) {
                    tasks = customMapper.selectRunningTasks();
                }else{
                    tasks = customMapper.selectRunningTasksByProcessKey(processKey);
                }
                return tasks;
            }
        };

        List<RunningTask> tasks = managementService.executeCustomSql(customSqlExecution);
        for (RunningTask task:tasks) {
            System.out.println("task=" + task);
        }
    }
}
