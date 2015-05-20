package business;

import java.util.ArrayList;
import java.util.List;

import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import robot.NXRobot;
import robot.Robo;
import data.Passo;
import data.StepNode;


public class BehaviorRobot {

	private StepNode root;
	private StepNode lastNode = new StepNode();
	private boolean ultimoPassoErro = false;
	private Robo robo = new NXRobot();
	private Arbitrator arb;
	private final List<Passo> possibilidades = new ArrayList<Passo>();
	private boolean recalcularPossibilidades = true;
	private boolean fim;
	protected boolean mapeandoBifurcacoes;
	private StepNode checkPoint;
	private boolean inAction = false;

	public void execute() throws Exception {
		Behavior caminhoUnico = new Behavior() {
			public boolean takeControl() {
				return !inAction && !fim && getPossibilidades() == 1;
			}

			public void suppress() {
			}

			public void action() {
				inAction = true;
				System.out.println(possibilidades.get(0));
				caminhar(possibilidades.get(0));
				proximoPasso();
				inAction = false;
			}
		};

		Behavior bifurcacao = new Behavior() {
			public boolean takeControl() {
				return !inAction && !fim && getPossibilidades() > 1;
			}

			public void suppress() {
			}

			public void action() {
				inAction = true;
				for(int i = 1; i < possibilidades.size(); i++){
					StepNode node = new StepNode(possibilidades.get(i));
					node.setBifurcacao(true);
					lastNode.addNode(node);
				}
				System.out.println("bif - " + possibilidades.get(0));
				caminhar(possibilidades.get(0));
				proximoPasso();
				inAction = false;
			}
		};

		Behavior voltar = new Behavior() {
			public boolean takeControl() {
				return !inAction && !fim && (getPossibilidades() == 0) && !estaNoFim() && !lastNode.equals(checkPoint);
			}

			public void suppress() {
			}

			public void action() {
				inAction = true;
				caminhar(Passo.TRAS);
				proximoPasso();
				inAction = false;
			}
		};

		Behavior fimFaltaMapear = new Behavior(){
			public boolean takeControl() {
				return !inAction && !fim && (getPossibilidades() == 0) && (estaNoFim() || lastNode.equals(checkPoint)) && temNoComBifurcacao();
			}

			public void suppress() {
			}

			public void action() {
				inAction = true;
				if(estaNoFim() && !lastNode.isFim()){
					lastNode.setFim(true);
				}
				mapeandoBifurcacoes = true;
				voltarAteBifurcacao();
				proximoPasso();
				inAction = false;
			}
		};

		Behavior fimMapeouTudo = new Behavior(){
			public boolean takeControl() {
				return !inAction && !fim && (getPossibilidades() == 0) && (estaNoFim() || lastNode.equals(checkPoint)) && !temNoComBifurcacao();
			}

			public void suppress() {
			}

			public void action() {
				inAction = true;
				if(estaNoFim() && !lastNode.isFim()){
					lastNode.setFim(true);
				}
				fim = true;
				Passo[] menorCaminho = djistra();
				sendByBluetooth(menorCaminho);
				inAction = false;
			}
		};

		Behavior[] bArray = { voltar, bifurcacao, caminhoUnico, fimFaltaMapear, fimMapeouTudo};
		arb = new Arbitrator(bArray);
		arb.start();
	}

	private void preenchePossibilidades() throws InterruptedException {
		if (caminhoLivre(Passo.FRENTE)
				&& ((lastNode == null) || lastNode.naoMapeado(Passo.FRENTE))) {
			possibilidades.add(Passo.FRENTE);
		}
		boolean caminhoLivre = caminhoLivre(Passo.DIREITA);
		boolean naoMapeado = lastNode.naoMapeado(Passo.DIREITA);
		if (caminhoLivre
				&& (naoMapeado
				&& (!Passo.DIREITA.equals(lastNode.getPasso())))) {
			possibilidades.add(Passo.DIREITA);
		}
		
		caminhoLivre = caminhoLivre(Passo.ESQUERDA);
		naoMapeado = lastNode.naoMapeado(Passo.ESQUERDA);
		if (caminhoLivre
				&& (naoMapeado
				&& (!Passo.ESQUERDA.equals(lastNode.getPasso())))) {
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
		frente();
	}

	private void direita(final boolean encontrarBifurcacao) {
		robo.direita();
		if(encontrarBifurcacao){
			lastNode = lastNode.getParent();
		}else{
			updateTree(Passo.DIREITA);
			ultimoPassoErro = false;
		}
		frente();
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
		Passo[] menorCaminho = djistra();
		sendByBluetooth(menorCaminho);
	}

	private void sendByBluetooth(final Passo[] menorCaminho) {
		// TODO
	}

	private Passo[] djistra() {
		List<Passo> passos = new ArrayList<Passo>();
		passos = root.encontraCaminho(passos);
		Passo[] ret = new Passo[passos.size()];
		for(int i = 0; i < passos.size(); i++){
			ret[passos.size() - 1 - i] = passos.get(i);
		}
		return ret;
	}

	private void proximoPasso() {
		possibilidades.clear();
		System.out.println("poss: " + possibilidades.size());
		recalcularPossibilidades = true;
	}

	private synchronized int getPossibilidades() {
		if(recalcularPossibilidades){
			try {
				preenchePossibilidades();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			recalcularPossibilidades = false;
		}
		return possibilidades.size();
	}

	private boolean estaNoFim(){
		int value = robo.getLightSensorValue();
//		return value < 5 && value > 50;
		return false;
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
		default:
			return Passo.FRENTE;//NUNCA DEVERIA PASSAR AQUI
		}
	}
}
