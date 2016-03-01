package de.uni_koeln.spinfo.ang.util;

import java.io.File;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
import org.tmatesoft.sqljet.core.table.SqlJetDb;


public class SqliteWrapper {
    
    private static final String DB_NAME = "db.sqlite";
    private static final String TABLE_1_NAME = "data_todo";
    private static final String TABLE_2_NAME = "data_done";

    private static final String FIELD_ID = "id";
    private static final String FIELD_TEXT = "text";
    private static final String FIELD_LANG_DE = "lang_de_manual";
    
    
    public SqlJetDb createAndOpenNewDB() throws SqlJetException{
    	File dbFile = new File(DB_NAME);
        dbFile.delete();
        
        SqlJetDb db = SqlJetDb.open(dbFile, true);
        db.getOptions().setAutovacuum(true);
        
        db.runTransaction(new ISqlJetTransaction() {
            public Object run(SqlJetDb db) throws SqlJetException {
                db.getOptions().setUserVersion(1);
                return true;
            }
        }, SqlJetTransactionMode.WRITE);
       
            
        db.beginTransaction(SqlJetTransactionMode.WRITE);
        try {            
            db.createTable("CREATE TABLE " + TABLE_1_NAME + " (" + FIELD_ID + " INTEGER NOT NULL, " + FIELD_TEXT + " TEXT NOT NULL)");
            db.createTable("CREATE TABLE " + TABLE_2_NAME + " (" + FIELD_ID + " INTEGER NOT NULL, " + FIELD_TEXT + " TEXT NOT NULL, "  + FIELD_LANG_DE + " BOOLEAN NOT NULL)");
        } finally {
            db.commit();
        }
        
        System.out.println("[CREATED DB]:");
        System.out.println(db.getSchema());
        System.out.println(db.getOptions());  
        
        return db;
    }
    
    public void preTransaction(SqlJetDb db) throws SqlJetException{
    	db.beginTransaction(SqlJetTransactionMode.WRITE);
    }
    
    public void postTransaction(SqlJetDb db) throws SqlJetException{
    	db.commit();
    }
    
    public ISqlJetTable getTable(SqlJetDb db, String tableName) throws SqlJetException{
    	return db.getTable(tableName);
    }
    
    public void insertRowTodo(ISqlJetTable table, int id, String text) throws SqlJetException{
        table.insert(id, text);
    }
    
    public void insertRowDone(ISqlJetTable table, int id, String text, boolean langDe) throws SqlJetException{
        table.insert(id, text, langDe);
    }

}
