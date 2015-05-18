package robot;

public class NXRobot implements Robo{

	private final UltrasonicSensor distancia = new UltrasonicSensor(SensorPort.S1);
	private final int CURVA = 270;
	private final int FRENTE = -150;
	private final int CABECA = 90;

	@Override
	public void frente() {
		Motor.A.rotate(FRENTE, true);
		Motor.C.rotate(FRENTE);
	}

	@Override
	public void esquerda() {
		Motor.A.rotate(CURVA, true);
		Motor.C.rotate(-CURVA);
	}

	@Override
	public void tras() {
		Motor.A.rotate(-FRENTE, true);
		Motor.C.rotate(-FRENTE + 30);
	}

	@Override
	public void direita() {
		Motor.A.rotate(-CURVA, true);
		Motor.C.rotate(CURVA);
	}

	@Override
	public boolean caminhoBloqueado(final boolean frente) {
		int d = getDistancia();
		return d < (frente ? 18 : 30);
	}

	private int getDistancia() {
		return distancia.getDistance();
	}

	@Override
	public void parar() {
		Motor.A.stop();
		Motor.C.stop();
	}

	@Override
	public void olharEsquerda() {
		Motor.B.rotate(-CABECA);
	}

	@Override
	public void olharDireita() {
		Motor.B.rotate(CABECA);
	}
}
