package Client;

public enum State {
	
	/* Costum type variable */
	END(-1), CONNECT(0), LOGIN(1), WAITING(2),PLAYING(3);
	
	/* Default type variable */
	protected int value;
	
	/* Constructor */
	private State(int value){
		this.value = value;
	}
}