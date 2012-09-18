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

import java.util.List;

public class ForeignKeyStruct {

	
	//nome della tabella su cui vi e' la FK
	String fkTableName;
	//lista dei nomi delle colonne FK
	List<String> fkColumnNames;
	//lista dei nomi delle colonne pk referenziate dalla FK
	List<String> pksColumNames;
	//nome della tabella PK cui e' relazionata la FK
	String pkTableName;
	
	//costruttore vuoto
	public ForeignKeyStruct(){}







	public String getFkTableName() {
		return fkTableName;
	}







	public void setFkTableName(String fkTableName) {
		this.fkTableName = fkTableName;
	}







	public List<String> getFkColumnNames() {
		return fkColumnNames;
	}







	public void setFkColumnNames(List<String> fkColumnNames) {
		this.fkColumnNames = fkColumnNames;
	}







	public List<String> getPksColumNames() {
		return pksColumNames;
	}







	public void setPksColumNames(List<String> pksColumNames) {
		this.pksColumNames = pksColumNames;
	}







	public String getPkTableName() {
		return pkTableName;
	}







	public void setPkTableName(String pkTableName) {
		this.pkTableName = pkTableName;
	};
	
	
}
