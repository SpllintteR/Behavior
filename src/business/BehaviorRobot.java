package business;

import java.util.ArrayList;
import java.util.List;

import robot.Robo;
import data.Passo;
import data.StepNode;


public class BehaviorRobot {

	private final StepNode root;
	private StepNode lastNode = null;
	private boolean ultimoPassoErro = false;
	private Robo robo;
	private Arbitrator arb;
	private final List<Passo> possibilidades = new ArrayList<>();
	private final boolean recalcularPossibilidades = true;
	private boolean fim;
	protected boolean mapeandoBifurcacoes;
	private StepNode checkPoint;

	public void execute() throws Exception {
		Behavior caminhoUnico = new Behavior() {
			public boolean takeControl() {
				return getPossibilidades() == 1;
			}

			public void suppress() {
				//
			}

			public void action() {
				caminhar(possibilidades.get(0));
				proximoPasso();
			}
		};

		Behavior bifurcacao = new Behavior() {
			public boolean takeControl() {
				return getPossibilidades() > 1;
			}

			public void suppress() {
				//

			}

			public void action() {
				for(int i = 1; i < possibilidades.size(); i++){
					StepNode node = new StepNode(possibilidades.get(i));
					node.setBifurcacao(true);
					lastNode.addNode(node);
				}
				caminhar(possibilidades.get(0));
				proximoPasso();
			}
		};

		Behavior voltar = new Behavior() {
			public boolean takeControl() {
				return (getPossibilidades() == 0) && !estaNoFim() && !lastNode.equals(checkPoint);
			}

			public void suppress() {
			}

			public void action() {

				caminhar(Passo.TRAS);
				proximoPasso();
			}
		};

		Behavior fimFaltaMapear = new Behavior(){
			public boolean takeControl() {
				return (getPossibilidades() == 0) && (estaNoFim() || lastNode.equals(checkPoint)) && temNoComBifurcacao();
			}

			public void suppress() {
			}

			public void action() {
				if(estaNoFim() && !lastNode.isFim()){
					lastNode.setFim(true);
				}
				mapeandoBifurcacoes = true;
				voltarAteBifurcacao();
				proximoPasso();
			}
		};

		Behavior fimMapeouTudo = new Behavior(){
			public boolean takeControl() {
				return (getPossibilidades() == 0) && (estaNoFim() || lastNode.equals(checkPoint)) && !temNoComBifurcacao();
			}

			public void suppress() {
			}

			public void action() {
				if(estaNoFim() && !lastNode.isFim()){
					lastNode.setFim(true);
				}
				arb.stop();
				Passo[] menorCaminho = djistra();
				sendByBluetooth(menorCaminho);
			}
		};

		Behavior[] bArray = { caminhoUnico, bifurcacao, voltar, fimFaltaMapear, fimMapeouTudo};
		arb = (new Arbitrator(bArray)).start();
	}

	private Passo preenchePossibilidades() {
		if (caminhoLivre(Passo.FRENTE)
				&& (lastNode.naoMapeado(Passo.FRENTE))) {
			possibilidades.add(Passo.FRENTE);
		}
		if (caminhoLivre(Passo.DIREITA)
				&& (lastNode.naoMapeado(Passo.DIREITA))
				&& !(lastNode.getParent().equals(Passo.DIREITA))) {
			possibilidades.add(Passo.DIREITA);
		}
		if (caminhoLivre(Passo.ESQUERDA)
				&& (lastNode.naoMapeado(Passo.ESQUERDA))
				&& !(lastNode.getParent().equals(Passo.ESQUERDA))) {
			possibilidades.add(Passo.ESQUERDA);
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
		StepNode node;
		if(lastNode == null){
			node = new StepNode(passo);
			root = node;
		}else{
			node = lastNode.getChild(passo);
			if(node == null){
				node = new StepNode(passo);
				lastNode.addNode(node);
			}else{
				node.setBifurcacao(false);
			}
		}
		lastNode = node;
	}

	private void caminhar(final Passo p) {
		caminhar(p, false);
	}

	private void caminhar(final Passo p, final boolean encontrarBifurcacao){
		switch (p) {
		case FRENTE:
			frente();
			break;
		case DIREITA:
			direita(encontrarBifurcacao);
			break;
		case ESQUERDA:
			esquerda(encontrarBifurcacao);
			break;
		case TRAS:
			tras(encontrarBifurcacao);
		}
	}

	private void frente() {
		robo.frente();
		updateTree(Passo.FRENTE);
		ultimoPassoErro = false;
	}

	private void esquerda(final boolean encontrarBifurcacao) {
		robo.esquerda();
		if(encontrarBifurcacao){
			lastNode = lastNode.getParent();
		}else{
			updateTree(Passo.ESQUERDA);
			ultimoPassoErro = false;
		}
	}

	private void direita(final boolean encontrarBifurcacao) {
		robo.direita();
		if(encontrarBifurcacao){
			lastNode = lastNode.getParent();
		}else{
			updateTree(Passo.DIREITA);
			ultimoPassoErro = false;
		}
	}

	private void tras(final boolean encontrarBifurcacao) {
		robo.tras();
		if(!encontrarBifurcacao){
			lastNode.setSucess(false);
			ultimoPassoErro = true;
		}
		lastNode = lastNode.getParent();
	}

	private void acabou() {
		arb.stop();
		Passo[] menorCaminho = djistra();
		sendByBluetooth(menorCaminho);
	}

	private void sendByBluetooth(final Passo[] menorCaminho) {
		// TODO
	}

	private Passo[] djistra() {
		List<Passo> passos = new ArrayList<>();
		passos = root.encontraCaminho(passos);
		Passo[] ret = new Passo[passos.size()];
		for(int i = 0; i < passos.size(); i++){
			ret[passos.size() - 1 - i] = passos.get(i);
		}
		return ret;
	}

	private void proximoPasso() {
		possibilidades.clear();
		recalcularPossibilidades = true;
	}

	private synchronized int getPossibilidades() {
		if(recalcularPossibilidades){
			preenchePossibilidades();
			recalcularPossibilidades = false;
		}
		return possibilidades.size();
	}

	private boolean estaNoFim(){
		//TODO: ler cor vermelha/verde/sei lá a cor do fim
	}

	private boolean temNoComBifurcacao(){
		return root.isBifurcacao();
	}

	private void voltarAteBifurcacao(){
		do{
			Passo p = encontrarPassoInverso();
			caminhar(p, true);
		} while (lastNode.isBifurcacao());
		checkPoint = lastNode;
	}

	private Passo encontrarPassoInverso() {
		switch (lastNode.getPasso()) {
		case FRENTE:
			return Passo.TRAS;
		case DIREITA:
			return Passo.ESQUERDA;
		case ESQUERDA:
			return Passo.DIREITA;
		case TRAS:
			return Passo.FRENTE;//NUNCA DEVERIA PASSAR AQUI
		}
	}
}
