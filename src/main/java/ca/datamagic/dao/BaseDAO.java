/**
 * 
 */
package ca.datamagic.dao;

/**
 * @author Greg
 *
 */
public abstract class BaseDAO {
	private static String _dataPath = null;
	
	public static String getDataPath() {
		return _dataPath;
	}
	
	public static void setDataPath(String newVal) {
		_dataPath = newVal;
	}
}
