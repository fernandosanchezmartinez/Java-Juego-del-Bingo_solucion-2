import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * @author
 * @date 20/11/2015
 */

/**
 * Clase que sirve para crear cada uno de los hilos de los jugadores
 */
class Jugador extends Thread {
	private final int TOTAL_CARTON = 5; // Cantidad de números por cart�n
	private final int TOTAL_BOMBO = 10; // Números posibles del bombo
	private int idJugador; // Identificador del jugador
	private Set<Integer> carton; // Para almacenar los números pendientes de
									// acertar
	private Bombo miBombo;

	/**
	 * @param idJugador
	 */
	public Jugador(int idJugador, Bombo miBombo) {
		this.miBombo = miBombo;
		this.idJugador = idJugador;
		carton = new HashSet<Integer>();
		while (carton.size() < TOTAL_CARTON)
			carton.add((int) Math.floor(Math.random() * TOTAL_BOMBO) + 1);
	}

	/**
	 * Muestra el carton por pantalla con los nomeros pendientes
	 */
	void imprimeCarton() {
		synchronized (System.out) {
			System.out.print("Pendientes jugador " + idJugador + ": ");

			for (Integer integer : carton)
				System.out.print(integer + " ");
			System.out.println();
		}
	}

	/**
	 * Tacha el numero del carton en caso de que exista
	 *
	 * @param numero
	 *            a tachar
	 */
	int tacharNum(Integer numero) {
		carton.remove(numero);
		return carton.size();
	}

	/**
	 * Metodo para conseguir el tamaño del Carto.
	 *
	 * @return Tamaño del carto.
	 */
	synchronized int getCartonSize() {
		return carton.size();
	}

	/**
	 * Metodo run, que revisa si el tamño del carton sigue siendo mayor que 0 y
	 * mientras sea, imprime el carton y tacha. Si es 0, imprime el ganador, y
	 * termina el programa.
	 */
	public void run() {
		while (!miBombo.hayUnGanador()) {
			if (tacharNum(miBombo.getUltimo()) == 0)
				miBombo.setGanador(true, idJugador);
			else
				imprimeCarton();
		}
	}

}

/**
 * Clase que sirve para el hilo del presentador
 */
class Presentador extends Thread {
	private Bombo miBombo;

	/**
	 * Constructor del presentador.
	 * 
	 * @param miBombo
	 *            objeto bombo.
	 */
	public Presentador(Bombo miBombo) {
		this.miBombo = miBombo;
	}

	/**
	 * Metodo run, que mientras no haya un ganador, sacara numeros e imprimira
	 * el Bombo.
	 */
	public void run() {
		while (!miBombo.hayUnGanador()) {
			try {
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			miBombo.sacarNum();
			miBombo.imprimirBombo();
		}
		miBombo.avisarBingo();
		System.err.println("BINGO!!!!  Ganador: " + miBombo.getGanador());	
	}
}


class Bombo {
	private final int TOTAL_BOMBO = 10; // Numeros posibles del bombo
	private Set<Integer> bombo; // Para almacenar los valores que van saliendo
	private Integer ultNumero; // Ultimo numero del bombo
	private boolean hayGanador = false; // Boolean para controlar si ya hay un
										// ganador.
	private int ganador; // Identificador del ganador

	/**
	 * @return the ganador
	 */
	public int getGanador() {
		return ganador;
	}

	/**
	 * Inicializa vacio el bombo
	 */
	public Bombo() {
		bombo = new HashSet<Integer>();
	}

	public synchronized void avisarBingo() {
		notifyAll();
	}

	public synchronized Integer sacarNum() {
		Integer bolita = 0;
		int cantidadBolas = bombo.size();
		if (cantidadBolas < TOTAL_BOMBO) {
			do {
				ultNumero = (int) Math.floor(Math.random() * TOTAL_BOMBO) + 1;
				bombo.add(ultNumero);
				bolita = ultNumero;
			} while (cantidadBolas == bombo.size());
			System.err.println("Ha salido el numero: " + ultNumero);
		} else
			System.out.println("Ya han salido todas las bolas");
		try {
			notifyAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bolita;
	}

	public void imprimirBombo() {
		if (!hayUnGanador()){
			synchronized (System.out) {
				System.out.print("Bolas sacadas hasta el momento: ");
				for (Integer integer : bombo)
					System.out.print(integer + " ");
				System.out.println();
			}
		}
	}

	public synchronized boolean hayUnGanador() {
		return hayGanador;
	}

	public synchronized void setGanador(boolean hayGanador, int id) {
		this.hayGanador = hayGanador;
		this.ganador = id;

	}

	public synchronized int getUltimo() {
		int bolita = 0;
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!hayUnGanador())
			bolita = ultNumero;

		return bolita;
	}
}

public class Bingo {
	public static void main(String[] args) {
		try {
			System.out
					.print("¿Cuántos jugadores tiene el bingo de esta noche? ");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			int cantidad = Integer.parseInt(br.readLine());

			Bombo miBombo = new Bombo();
			Presentador presentador = new Presentador(miBombo);
			Jugador[] jugadores;

			presentador.start();
			jugadores = new Jugador[cantidad];
			for (int i = 0; i < jugadores.length; i++) {
				jugadores[i] = new Jugador(i + 1, miBombo);
				jugadores[i].start();
			}
			for (int i = 0; i < jugadores.length; i++) {
				jugadores[i].join();
			}

			presentador.join();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}