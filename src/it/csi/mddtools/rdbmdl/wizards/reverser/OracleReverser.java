package it.csi.mddtools.rdbmdl.wizards.reverser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class OracleReverser extends AbstractReverser{


	@Override
	public Connection getConnection(String jdbcUrl, String username,
			String password) throws SQLException {
		// Load database driver
		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
		// Make connection
		Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
		return conn;
	}
	
}
