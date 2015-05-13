package dev;

public class Step {

	private Passo passo;
	private boolean sucess;
	
	public Step(Passo passo) {
		this.passo = passo;
		this.sucess = true;
	}
	public Passo getPasso() {
		return passo;
	}
	public void setPasso(Passo passo) {
		this.passo = passo;
	}
	public boolean isSucess() {
		return sucess;
	}
	public void setSucess(boolean sucess) {
		this.sucess = sucess;
	}	
	
}
