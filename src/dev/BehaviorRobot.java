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

	private static int CURVA = 270;
	private static int FRENTE = -150;
	private static int CABECA = 90;
	private static List<Step> passos = new ArrayList<Step>();
	private static int pos = -1;
	private static int countErrors = 0;

	public static void main(String[] args) throws Exception {
		Behavior andarParaFrente = new Behavior() {
			public boolean takeControl() {
				return !caminhoBloqueado(true) && !ultimoPassoErro();
			}

			public void suppress() {
				//
			}

			public void action() {
				System.out.println("frente");
				frente();
			}
		};

		Behavior mudarDirecao = new Behavior() {
			public boolean takeControl() {
				return caminhoBloqueado(true) || ultimoPassoErro();
			}

			public void suppress() {
				//
				
			}

			public void action() {
				Passo p = null;
				p = encontraNovoCaminho();
				System.out.println("nova direcao " + p);
				// try {
				// Thread.sleep(1000);
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
				if (p == Passo.FRENTE) {
					frente();
				} else if (p == Passo.DIREITA) {
					direita();
				} else {
					if (p == Passo.ESQUERDA) {
						esquerda();
					} else {
						tras();
						// try {
						// Thread.sleep(1000);
						// } catch (InterruptedException e) {
						// e.printStackTrace();
						// }
					}
				}
			}

			private Passo encontraNovoCaminho() {
				if (caminhoLivre(Passo.FRENTE)
						&& (passos.get(pos + countErrors).isSucess())) {
					return Passo.FRENTE;
				} else {
					if (caminhoLivre(Passo.DIREITA)
							&& (passos.get(pos).isSucess())) {
						return Passo.DIREITA;
					} else {
						if (caminhoLivre(Passo.ESQUERDA)
								&& (passos.get(pos).isSucess())) {
							return Passo.ESQUERDA;
						} else {
							return Passo.TRAS;
						}
					}
				}
			}

			private boolean caminhoLivre(Passo p) {
				boolean result = false;
				if (p == Passo.FRENTE) {
					result = caminhoBloqueado(true);
				} else {
					if (p == Passo.DIREITA) {
						Motor.B.rotate(CABECA);
						result = caminhoBloqueado();
						Motor.B.rotate(-CABECA);
					} else {
						Motor.B.rotate(-CABECA);
						result = caminhoBloqueado();
						Motor.B.rotate(CABECA);
					}
				}
				// System.out.println("caminho livre " + p);
				// try {
				// Thread.sleep(1000);
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
				return !result;
			}
		};

		Behavior stop = new Behavior() {
			public boolean takeControl() {
				return Button.ESCAPE.isDown();
			}

			public void suppress() {
				parar();
			}

			public void action() {
				parar();
				throw new RuntimeException("a");
			}
		};

		Behavior[] bArray = { andarParaFrente, mudarDirecao, stop };
		(new Arbitrator(bArray)).start();
	}

	private static boolean ultimoPassoErro() {
		return (passos.size() > 0)
				&& !(passos.get(passos.size() - 1).isSucess());
	}

	private static void parar() {
		Motor.A.stop();
		Motor.C.stop();
	}

	private static void frente() {
		Motor.A.rotate(FRENTE, true);
		Motor.C.rotate(FRENTE);
		passos.add(new Step(Passo.FRENTE));
		pos++;
	}

	private static void tras() {
		Motor.A.rotate(-FRENTE, true);
		Motor.C.rotate(-FRENTE + 30);
		passos.get(pos + countErrors).setSucess(false);
		pos--;
		countErrors++;
	}

	private static void esquerda() {
		Motor.A.rotate(CURVA, true);
		Motor.C.rotate(-CURVA);
		passos.add(new Step(Passo.ESQUERDA));
		pos++;
	}

	private static void direita() {
		Motor.A.rotate(-CURVA, true);
		Motor.C.rotate(CURVA);
		passos.add(new Step(Passo.DIREITA));
		pos++;
	}

	private static boolean caminhoBloqueado() {
		return caminhoBloqueado(false);
	}

	private static boolean caminhoBloqueado(boolean frente) {
		int d = distancia.getDistance();
		// System.out.println(d);
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		return d < (frente ? 18 : 30);
	}
}
