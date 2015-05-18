package business;

import robot.Robo;
import data.ArvorePassos;
import data.Passo;
import data.StepNode;


public class BehaviorRobot {

	private final ArvorePassos passos = new ArvorePassos();
	private StepNode lastNode = null;
	private boolean ultimoPassoErro = false;
	private Robo robo;
	private Arbitrator arb;

	public void execute() throws Exception {
		Behavior andarParaFrente = new Behavior() {
			public boolean takeControl() {
				return !robo.caminhoBloqueado(true) && !ultimoPassoErro;
			}

			public void suppress() {
				//
			}

			public void action() {
				frente();
			}
		};

		Behavior mudarDirecao = new Behavior() {
			public boolean takeControl() {
				return robo.caminhoBloqueado(true) || ultimoPassoErro;
			}

			public void suppress() {
				//

			}

			public void action() {
				Passo p = null;
				p = encontraNovoCaminho();
				System.out.println("nova direcao " + p);
				if (p == Passo.FRENTE) {
					frente();
				} else if (p == Passo.DIREITA) {
					direita();
				} else {
					if (p == Passo.ESQUERDA) {
						esquerda();
					} else {
						if(robo.acabou()){
							acabou();
						}else{
							tras();
						}
					}
				}
			}

			private Passo encontraNovoCaminho() {
				if (caminhoLivre(Passo.FRENTE)
						&& (lastNode.stepSucess(Passo.FRENTE))) {
					return Passo.FRENTE;
				} else {
					if (caminhoLivre(Passo.DIREITA)
							&& (lastNode.stepSucess(Passo.DIREITA))) {
						return Passo.DIREITA;
					} else {
						if (caminhoLivre(Passo.ESQUERDA)
								&& (lastNode.stepSucess(Passo.ESQUERDA))) {
							return Passo.ESQUERDA;
						} else {
							return Passo.TRAS;
						}
					}
				}
			}

			private boolean caminhoLivre(final Passo p) {
				boolean result = false;
				if (p == Passo.FRENTE) {
					result = robo.caminhoBloqueado(true);
				} else {
					if (p == Passo.DIREITA) {
						result = cabecaDireita();
					} else {
						result = cabecaEsquerda();
					}
				}
				return !result;
			}
		};

		Behavior stop = new Behavior() {
			public boolean takeControl() {
				//								return Button.ESCAPE.isDown();
				return false;
			}

			public void suppress() {
				robo.parar();
			}

			public void action() {
				robo.parar();
				throw new RuntimeException("a");
			}
		};

		Behavior[] bArray = { andarParaFrente, mudarDirecao, stop };
		arb = (new Arbitrator(bArray)).start();
	}

	private boolean cabecaEsquerda() {
		boolean result;
		robo.olharEsquerda();
		result = robo.caminhoBloqueado(false);
		robo.olharDireita();
		return result;
	}

	private boolean cabecaDireita() {
		boolean result;
		robo.olharDireita();
		result = robo.caminhoBloqueado(false);
		robo.olharEsquerda();
		return result;
	}

	private void updateTree(final Passo passo) {
		StepNode node = new StepNode(passo);
		if(lastNode == null){
			passos.addNode(node);
		}else{
			lastNode.addNode(node);
		}
		lastNode = node;
	}

	private void frente() {
		robo.frente();
		updateTree(Passo.FRENTE);
		ultimoPassoErro = false;
	}

	private void esquerda() {
		robo.esquerda();
		updateTree(Passo.ESQUERDA);
		ultimoPassoErro = false;
	}

	private void direita() {
		robo.direita();
		updateTree(Passo.DIREITA);
		ultimoPassoErro = false;
	}

	private void tras() {
		robo.tras();
		lastNode.setSucess(false);
		lastNode = lastNode.getParent();
		ultimoPassoErro = true;
	}

	private void acabou() {
		arb.stop();
		Passo[] passos = djistra();
		sendByBluetooth(passos);
	}
}
