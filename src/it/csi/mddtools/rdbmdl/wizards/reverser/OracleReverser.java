/**
 * <copyright>
 * (C) Copyright 2011 CSI-PIEMONTE;

 * Concesso in licenza a norma dell'EUPL, esclusivamente versione 1.1;
 * Non e' possibile utilizzare l'opera salvo nel rispetto della Licenza.
 * E' possibile ottenere una copia della Licenza al seguente indirizzo:
 *
 * http://www.eupl.it/opensource/eupl-1-1
 *
 * Salvo diversamente indicato dalla legge applicabile o concordato per 
 * iscritto, il software distribuito secondo i termini della Licenza e' 
 * distribuito "TAL QUALE", SENZA GARANZIE O CONDIZIONI DI ALCUN TIPO,
 * esplicite o implicite.
 * Si veda la Licenza per la lingua specifica che disciplina le autorizzazioni
 * e le limitazioni secondo i termini della Licenza.
 * </copyright>
 *
 * $Id$
 */
package it.csi.mddtools.rdbmdl.wizards.reverser;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
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

	@Override
	public ResultSet getImportedKeysFromDMD(DatabaseMetaData dmd,
			String schemaName, String tableName) throws SQLException {
			ResultSet foreignKeySet = dmd.getImportedKeys(null, schemaName, tableName);
			return foreignKeySet;
	}
	
}
