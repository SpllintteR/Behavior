package dev;

import java.util.ArrayList;
import java.util.List;

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

public class BehaviorRobot {

	private static UltrasonicSensor distancia = new UltrasonicSensor(
			SensorPort.S1);

	private static int CURVA = 195;
	private static int FRENTE = 500;
	private static int CABECA = 50;
	private static List<Passo> passosNaoPodeIr = new ArrayList<Passo>();

	public static void main(String[] args) throws Exception {
		Behavior andarParaFrente = new Behavior() {
			public boolean takeControl() {
				return passosNaoPodeIr.size() == 0;
			}

			public void suppress() {
				Motor.A.stop();
				Motor.C.stop();
			}

			public void action() {
				Motor.A.rotate(FRENTE);
				Motor.C.rotate(FRENTE);
			}
		};

		Behavior andarParaDireita = new Behavior() {
			public boolean takeControl() {
				return caminhoBloqueado() || passosNaoPodeIr.size() > 0;
			}

			public void suppress() {
				Motor.A.stop();
				Motor.C.stop();
			}

			public void action() {
				Passo p = encontraNovoCaminho();
				if (p == Passo.DIREITA) {
					Motor.A.rotate(-CURVA, true);
					Motor.C.rotate(CURVA);
				}else{
					if(p == Passo.ESQUERDA){
						Motor.A.rotate(CURVA, true);
						Motor.C.rotate(-CURVA);
					}else{
						Motor.A.rotate(-CURVA, true);
						Motor.C.rotate(-CURVA);
						passosNaoPodeIr.add(Passo.FRENTE);
					}
				}
			}

			private Passo encontraNovoCaminho() {
				if(caminhoLivre(Passo.FRENTE) && !passosNaoPodeIr.get(passosNaoPodeIr.size() - 1).equals(Passo.FRENTE)){
					return Passo.FRENTE;
				}else{
					if(caminhoLivre(Passo.DIREITA) && !passosNaoPodeIr.get(passosNaoPodeIr.size() - 1).equals(Passo.DIREITA)){
						return Passo.DIREITA;
					}else{
						if(caminhoLivre(Passo.ESQUERDA) && !passosNaoPodeIr.get(passosNaoPodeIr.size() - 1).equals(Passo.ESQUERDA)){
							return Passo.ESQUERDA;
						}else{
							return Passo.TRAS;
						}
					}
				}
			}

			private boolean caminhoLivre(Passo p) {
				boolean result;
				if(p == Passo.FRENTE){
					return caminhoBloqueado();
				}else{
					if(p == Passo.DIREITA){
						Motor.B.rotate(CABECA);
						result = caminhoBloqueado();
						Motor.B.rotate(-CABECA);
						return result;
					}else{
						Motor.B.rotate(-CABECA);
						result = caminhoBloqueado();
						Motor.B.rotate(CABECA);
						return result;
					}
				}
			}
		};

		Behavior[] bArray = { andarParaFrente, andarParaDireita };
		(new Arbitrator(bArray)).start();
	}

	private static boolean caminhoBloqueado() {
		return distancia.getDistance() < 20;
	}
}
