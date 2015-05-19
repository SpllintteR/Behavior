package data;

import java.util.List;

public class StepNode {

	private List<StepNode> steps;
	private boolean sucess = true;
	private StepNode parent;
	private final Passo passo;
	private boolean bifurcacao = false;

	public StepNode(final Passo passo) {
		this.passo = passo;
	}

	public boolean isBifurcacao() {
		if (bifurcacao){
			return true;
		}else{
			for (StepNode stepNode : steps) {
				if(stepNode.isBifurcacao()){
					return true;
				}
			}
		}
		return false;
	}


	public void setBifurcacao(final boolean bifurcacao) {
		this.bifurcacao = bifurcacao;
	}

	public StepNode getStep(final int i){
		return steps.get(i);
	}

	public int getStepCount(){
		return steps.size();
	}

	public boolean isSucess() {
		return sucess;
	}

	public void setSucess(final boolean sucess) {
		this.sucess = sucess;
	}

	public StepNode getParent() {
		return parent;
	}

	public void setParent(final StepNode parent) {
		this.parent = parent;
	}

	public void addNode(final StepNode lastNode) {
		steps.add(lastNode);
	}

	public Passo getPasso() {
		return passo;
	}

	public boolean naoMapeado(final Passo passo) {
		for (StepNode stepNode : steps) {
			if(stepNode.getPasso().equals(passo)){
				return stepNode.isBifurcacao();
			}
		}
		return true;
	}

	public StepNode getChild(final Passo passo) {
		for (StepNode stepNode : steps) {
			if(stepNode.getPasso().equals(passo)){
				return stepNode;
			}
		}
		return null;
	}

}
