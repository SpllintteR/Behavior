package data;

import java.util.List;

public class ArvorePassos {

	private List<StepNode> steps;

	public StepNode getRootNode(final int i){
		return steps.get(i);
	}

	public int getRootNodeCount(){
		return steps.size();
	}

	public void addNode(final StepNode lastNode) {
		steps.add(lastNode);
	}

}
