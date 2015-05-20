package robot;

import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

public class NXRobot implements Robo{

	private final UltrasonicSensor distancia = new UltrasonicSensor(SensorPort.S1);
	private final LightSensor sensorLuminosidade = new LightSensor(SensorPort.S2);
	private final int CURVA = 200;
	private final int FRENTE = -500;
	private final int RE = 770;
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
		Motor.A.rotate(RE, true);
		Motor.C.rotate(RE + 30);
	}

	@Override
	public void direita() {
		Motor.A.rotate(-CURVA, true);
		Motor.C.rotate(CURVA);
	}

	@Override
	public boolean caminhoBloqueado(final boolean frente) {
		int d = getDistancia();
//		return d < (frente ? 18 : 30);
		return d < 25;
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

	@Override
	public int getLightSensorValue() {
		return sensorLuminosidade.getLightValue();
	}
}
