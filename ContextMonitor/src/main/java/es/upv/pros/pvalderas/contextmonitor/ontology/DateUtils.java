package es.upv.pros.pvalderas.contextmonitor.ontology;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {


	// Convierte un String que contiene una fecha en un Date
	public static Date String2Date (String date){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		Date dat = null; 
		try {
			dat = df.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		return dat;
	}

	// Convierte un Date en un String en el formato en el que est� la fecha en la ontolog�a
	public static String Date2String (Date date){
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");	
		String dateString = df.format(date);	
		return dateString;	
	}

	
	// Convierte un Calendar en un String en el formato en el que est� la fecha en la ontolog�a
	public static String Calendar2String(Calendar c){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String date = sdf.format(c.getTime());
		return date;
	}


	// Convierte un String que contiene una fecha en un Calendar
	public static Calendar String2Calendar(String s) throws ParseException {
		Calendar cal=Calendar.getInstance();
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date d1=df.parse(s);
		cal.setTime(d1);   
		return cal;
	}
	
	// Convierte un Calendar en un Date
	public static Date Calendar2Date(Calendar c){
		Date date;
		String dateS=Calendar2String(c);
		date=String2Date(dateS);		
		return date;
	}

	/**
	 * Devuelve la diferencia entre dos fechas dadas
	 * @param fechainicial
	 * @param fechafinal
	 * @param resultForm, indica si el resultado se muestra en ms, seg, min
	 * @return
	 */
	public static int getDiferenciaFechas(Date fechainicial, Date fechafinal, int resultForm) {

		int factor=1;

		switch(resultForm){

		case 1:  { factor = 1000; break; } 		     //segundos
		case 2:  { factor = 1000*60; break; }        //minutos
		case 3:  { factor = 1000*60*60; break; }     //horas
		case 4:  { factor = 1000*60*60*24; break; }  //dias
		default:  factor =1; break;
		}

		DateFormat df = DateFormat.getDateTimeInstance();
		String fechainiciostring = df.format(fechainicial);
		try {
			fechainicial = df.parse(fechainiciostring);
		}
		catch (ParseException ex) {	}

		String fechafinalstring = df.format(fechafinal);
		try {
			fechafinal = df.parse(fechafinalstring);
		}
		catch (ParseException ex) {}

		long fechainicialms = fechainicial.getTime();
		long fechafinalms = fechafinal.getTime();
		long diferencia = fechafinalms - fechainicialms;

		double min= Math.floor(diferencia / factor);
		return ( (int) min);	
	}
	
}
