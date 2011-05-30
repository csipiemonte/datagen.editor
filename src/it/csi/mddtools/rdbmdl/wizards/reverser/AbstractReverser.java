package it.csi.mddtools.rdbmdl.wizards.reverser;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

//import oracle.jdbc.OracleDatabaseMetaData;
import it.csi.mddtools.rdbmdl.RdbmdlFactory;
import it.csi.mddtools.rdbmdl.Schema;
import it.csi.mddtools.rdbmdl.Table;
import it.csi.mddtools.rdbmdl.TableColumn;
import it.csi.mddtools.rdbmdl.constraints.ConstraintsFactory;
import it.csi.mddtools.rdbmdl.constraints.PrimaryKey;
import it.csi.mddtools.rdbmdl.datatypes.DatatypesFactory;
import it.csi.mddtools.rdbmdl.datatypes.PrimitiveDataType;
import it.csi.mddtools.rdbmdl.datatypes.PrimitiveTypeCodes;

public abstract class AbstractReverser {


	public abstract Connection getConnection(String jdbcUrl, String username, String password) throws SQLException;
	
	
	/**
	 * Create a schema db from oracle metadata
	 * @param fact
	 * @param jdbcUrl
	 * @param schemaName
	 * @param username
	 * @param password
	 * @return Schema
	 */
	public Schema createSchema(RdbmdlFactory fact, String jdbcUrl,
			String schemaName, String username, String password) {

		Schema schema = fact.createSchema();
		schema.setName(schemaName);
		// Connection reference
		Connection conn = null;

		try {

			// Make connection
			conn = getConnection(jdbcUrl, username, password);

			// Get the database meta data
			DatabaseMetaData dmd = (DatabaseMetaData) conn
					.getMetaData();

			if (dmd == null) {
				throw new IllegalArgumentException(
						"Database meta data not available");
			} else {

				// get all schemas from db
				ResultSet rs = dmd.getSchemas();
				ResultSet rs1 = null;
				int i = 0;
				while (rs.next()) {
					// load only referenced schema
					if (rs.getString(1).equalsIgnoreCase(schemaName)) {

						rs1 = dmd.getTables(null, rs.getString(1), "%", null);
						while (rs1.next()) {
							// add table or view to schema passing it tablename
							String tname = rs1.getString(3);
							String ttype = rs1.getString(4);
							if ("TABLE".equalsIgnoreCase(ttype)){
								addTableOrViewToSchema(tname, schema,
										fact, dmd);
							}
						}
						try{
							rs1.close();
							rs1.getStatement().close();
						}
						catch(Exception ce){
							// NOPc
							System.out.println("errore nella chiusura del cursore:"+ce);
						}
					}
				}
				try{
					rs.close();
				}
				catch(Exception ce){
					// NOP
				}
			}
		} catch (SQLException e) {
			throw new IllegalArgumentException();
		} finally {
			// Close connection
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {
					throw new IllegalArgumentException();
				}
			}
		}

		return schema;
	}

	/**
	 * Add a table or a view to schema
	 * @param tableName
	 * @param schema
	 * @param fact
	 * @param dmd
	 * @throws SQLException
	 */
	private void addTableOrViewToSchema(String tableName, Schema schema,
			RdbmdlFactory fact, DatabaseMetaData dmd) throws SQLException {

		Table tab = fact.createTable();
		tab.setName(tableName);
		tab.setUid("tb_"+tableName);
		
		ResultSet rsColumns = dmd.getColumns(null, null, tableName, null);
		while (rsColumns.next()) {
			TableColumn col = fact.createTableColumn();
			col.setName(rsColumns.getString("COLUMN_NAME"));
		    col.setType(setPrimitiveDataType(rsColumns));
		    col.setUid("col_"+tableName+"_"+rsColumns.getString("COLUMN_NAME"));

			tab.getColumns().add(col);
		}
		try{
			rsColumns.close();
			rsColumns.getStatement().close();
		}
		catch(Exception e){
			System.out.println("errore in chiusura statement");
		}
		PrimaryKey pk = createPrimaryKey(dmd, tableName, tab);
		if(pk != null){
			tab.setPrimaryKey(pk);
		}

		schema.getElements().add(tab);

	}

	/**
	 * get the primitive data type for current column
	 * @param rsColumns
	 * @return PrimitiveDataType
	 * @throws SQLException
	 */
	private PrimitiveDataType setPrimitiveDataType(ResultSet rsColumns) throws SQLException {
		PrimitiveDataType dataType = DatatypesFactory.eINSTANCE.createPrimitiveDataType();
		dataType.setDecimalDigits(rsColumns.getInt("DECIMAL_DIGITS"));
		dataType.setName(rsColumns.getString("TYPE_NAME"));
		dataType.setNullable(rsColumns.getInt("NULLABLE") == 0 ? false:true);
		dataType.setSize(rsColumns.getInt("COLUMN_SIZE"));
		dataType.setType(PrimitiveTypeCodes.get(rsColumns.getShort("DATA_TYPE")));
		return dataType;
	}

	/**
	 * return a primary key for current table
	 * @param dmd
	 * @param tableName
	 * @param tab
	 * @return PrimaryKey
	 * @throws SQLException
	 */
	private PrimaryKey createPrimaryKey(DatabaseMetaData dmd,
			String tableName, Table tab) throws SQLException {
		
		PrimaryKey primaryKey = ConstraintsFactory.eINSTANCE.createPrimaryKey();
		primaryKey.setUid("pk_"+tableName);
		primaryKey.setName("pk_"+tableName);
		
		boolean flag = false;
		ResultSet pk = dmd.getPrimaryKeys(null, null, tableName);
		while (pk.next()) {
			addColumnToPk(tab, pk.getString("COLUMN_NAME"), primaryKey);
			flag = true;
		}
		try {
			pk.close();
			pk.getStatement().close();
		}
		catch(Exception e){
			System.out.println("errore in chiusura cursore:"+e);
		}
		if(flag){
			return primaryKey;
		}
		
		return null;
	}

	/**
	 * add column to PrimaryKey object
	 * @param tab
	 * @param columnName
	 * @param primaryKey
	 */
	private void addColumnToPk(Table tab, String columnName, PrimaryKey primaryKey) {
		
		List<TableColumn> listaColonne = tab.getColumns();
		for (TableColumn tableColumn : listaColonne) {
			if(tableColumn.getName().equalsIgnoreCase(columnName)){
				tableColumn.setIsPrimaryKey(true);
				primaryKey.getIncludedColumns().add(tableColumn);
				return;
			}
		}
		
	}
	
}
