package us.patt.mrubart;

import java.lang.reflect.Field;

/**
 * POJO for a bart station
 * @author dave
 */
public class BartStation {
	public String name;
	public String abbr;
	public double gtfsLatitude;
	public double gtfsLongitude;
	public String address;
	public String city;
	public String county;
	public String state;
	public String zipcode;
	
	public String toString() {
		String str = "";
		Field[] fields = this.getClass().getDeclaredFields();
		for (int i=0; i<fields.length; i++)
		{
		    try {
				str += fields[i].getName() + " - " + fields[i].get(this) + " ";
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return str;
	}
}
