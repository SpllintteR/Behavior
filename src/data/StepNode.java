package data;

import java.util.ArrayList;
import java.util.List;

public class StepNode {

	private List<StepNode> steps = new ArrayList<StepNode>();
	private boolean sucess = true;
	private StepNode parent;
	private Passo passo;
	private boolean bifurcacao = false;
	private boolean fim = false;

	public StepNode(final Passo passo) {
		this.passo = passo;
	}

	public StepNode() {
		// TODO Auto-generated constructor stub
	}

	public boolean isFim() {
		return fim;
	}

	public void setFim(final boolean fim) {
		this.fim = fim;
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
		lastNode.setParent(this);
	}

	public Passo getPasso() {
		return passo;
	}

	public boolean naoMapeado(final Passo passo) {
		for (StepNode stepNode : steps) {
			if((stepNode.getPasso() != null) && stepNode.getPasso().equals(passo)){
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

	public List<Passo> encontraCaminho(final List<Passo> passos) {
		List<Passo> ret = new ArrayList<Passo>();
		if (isFim()){
			ret.add(getPasso());
			return ret;
		}

		int i = 0;
		while((passos.size() == 0) && (i < steps.size())){
			List<Passo> list = steps.get(i).encontraCaminho(passos);
			if((ret.size() == 0) || (list.size() < ret.size())){
				ret = list;
			}
			i++;
		}
		if(ret.size() > 0){
			ret.add(getPasso());
		}
		return ret;
	}
}
