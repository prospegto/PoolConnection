package pool;

import java.sql.Connection;
import java.sql.SQLException;

public class Usuario extends Thread {
	
	private static int num = 0;
	private int numU;
	private Connection conexion;
	private Pool p;
	
	public Usuario(Pool p){
		numU = num;
		num++;
		this.p = p;
	}
	
	public void run(){
		pedirConexion(p);
	}
	
	public void pedirConexion(Pool p){
		try {
			System.out.println("Usuario "+numU+" pide conexion");
			setConexion(p.getConnection());
			System.out.println("Usuario "+numU+" obtiene conexion");
			try {
				Thread.sleep((int) (Math.random()*10000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				System.out.println("Usuario "+numU+" libera conexion");
				p.liberaConexion(conexion);				
			}
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Usuario "+numU+": "+e.getMessage());
		}
	}
	
	public void liberarConexion(Pool p){
		if(getConexion()!=null){
			p.liberaConexion(getConexion());
			System.out.println("Conexion liberada");
		}
	}

	public Connection getConexion() {
		return conexion;
	}

	public void setConexion(Connection conexion) {
		this.conexion = conexion;
	}
	
	
}
