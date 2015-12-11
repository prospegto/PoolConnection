package pool;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class Pool extends Thread implements DataSource {

	private boolean andando = false;
	private int maxActivas, minLibres, maxLibres, maxTiempoEspera, valorIntermedio;
	private String servidor, usuario, password;
	private ArrayList<Connection> listaConexionesEnUso, listaConexionesLibres;


	public Pool(){
		minLibres = 2;
		maxLibres = 6;
		maxActivas = 10;
		maxTiempoEspera = 4000;
		valorIntermedio = 4;
		servidor = "jdbc:sqlserver://localhost";
		usuario = "prueba";
		password = "123";
		listaConexionesLibres = new ArrayList<Connection>();
		listaConexionesEnUso = new ArrayList<Connection>();
		synchronized (this) {
			for(int i=0; i<maxLibres; i++){
				listaConexionesLibres.add(new DBSQL(servidor, usuario, password).getConnection());
			}
			System.out.println("Arranque - Conexiones disponibles: "+listaConexionesLibres.size());
		}		

	}


	public void run(){
		while(true){
			try {
				andando = true;
				if(minLibres>listaConexionesLibres.size() && maxActivas>(listaConexionesLibres.size()+listaConexionesEnUso.size())){
					System.out.println("Demonio Reanuda");
					while(maxActivas>(listaConexionesLibres.size()+listaConexionesEnUso.size()) && listaConexionesLibres.size()<=valorIntermedio){
						listaConexionesLibres.add(new DBSQL(servidor, usuario, password).getConnection());
						System.out.println("Conexion creada - Conexiones disponibles: "+listaConexionesLibres.size());					
						sleep(15);
					}
					if(maxActivas<=(listaConexionesLibres.size()+listaConexionesEnUso.size())){
						System.out.println("Demonio pausado por maximas activas");
					}
					if(listaConexionesLibres.size()>valorIntermedio){
						System.out.println("Demonio pausado por relleno de Pool");
					}
				}				
				andando = false;
				synchronized (this) {
					this.wait();
				}				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	@Override
	public PrintWriter getLogWriter() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int getLoginTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setLogWriter(PrintWriter arg0) throws SQLException {
		// TODO Auto-generated method stub

	}


	@Override
	public void setLoginTimeout(int arg0) throws SQLException {
		// TODO Auto-generated method stub

	}


	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Connection getConnection() throws SQLException {
		return getConnection(null, null);
	}


	@Override
	public Connection getConnection(String arg0, String arg1)
			throws SQLException {
		// TODO Auto-generated method stub
		if(!andando){
			synchronized (this) {				
				this.notify();
			}
		}		
		Connection conexion = null;
		synchronized (this.listaConexionesLibres) {
			if(this.listaConexionesLibres.size()>0){
				conexion = listaConexionesLibres.get(0);
				this.listaConexionesLibres.remove(conexion);
				this.listaConexionesEnUso.add(conexion);
				System.out.println("Conexion asignada - libres: "+listaConexionesLibres.size());
			}else{
				System.out.println("Sin conexiones, reintentando...");
			}
		}
		if(conexion==null){
			try {
				Thread.sleep((int) (Math.random()*maxTiempoEspera));
				synchronized (this.listaConexionesLibres) {
					if(this.listaConexionesLibres.size()>0){
						conexion = listaConexionesLibres.get(0);
						this.listaConexionesLibres.remove(conexion);
						this.listaConexionesEnUso.add(conexion);
						System.out.println("Conexion asignada - libres: "+listaConexionesLibres.size());
					}else{
						throw new SQLException("No hay conexiones. ERROR.");
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return conexion;		
	}


	public synchronized void liberaConexion(Connection con){
		if(this.listaConexionesEnUso.contains(con)){
			this.listaConexionesEnUso.remove(con);
			this.listaConexionesLibres.add(con);
			System.out.println("Conexion liberada - libres: "+listaConexionesLibres.size());
			if(maxLibres<listaConexionesLibres.size()){			
				while(listaConexionesLibres.size()>valorIntermedio){
					listaConexionesLibres.remove(0);
					System.out.println("Conexion eliminada - Conexiones disponibles: "+listaConexionesLibres.size());
					try {
						sleep(15);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}		
	}


	public synchronized void cierraTodo(){
		andando = false;
		for(Connection c: listaConexionesEnUso){
			try {
				c.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(Connection c: listaConexionesLibres){
			try {
				c.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	private class DBSQL{


		private Connection conexion;

		public DBSQL(String serv, String user, String pass){
			try{
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
				// Create a connection through the DriverManager
				conexion = (DriverManager.getConnection(serv, user, password));
				System.out.println("Conexion establecida");
			}catch(ClassNotFoundException ex){
				ex.printStackTrace();
			}catch(SQLException ex){
				ex.printStackTrace();
			}
		}

		public Connection getConnection(){
			return conexion;
		}


	}



}
