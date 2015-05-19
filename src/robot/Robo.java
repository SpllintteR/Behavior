package robot;

public interface Robo {

	void frente();

	void esquerda();

	void tras();

	void direita();

	boolean caminhoBloqueado(boolean b);

	void parar();

	void olharEsquerda();

	void olharDireita();

	int getLightSensorValue();

}
