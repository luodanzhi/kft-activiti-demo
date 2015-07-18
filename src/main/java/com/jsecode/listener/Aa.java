package  com.jsecode.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

public class Aa  implements ExecutionListener {

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		System.out.println("=======================fffffffffffff");
		
	}

}
