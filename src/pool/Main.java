package pool;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		Pool p = new Pool();
		p.start();
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i=0; i<20; i++){
			Usuario us = new Usuario(p);
			us.start();
			try {
				Thread.sleep((int) Math.random()*300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

	}

}
