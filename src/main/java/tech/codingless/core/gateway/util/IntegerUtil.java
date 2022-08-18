package tech.codingless.core.gateway.util;

import java.util.Random;

public class IntegerUtil {
	private static Random random  = new Random();
	/**
	 * 
	 * @param min min
	 * @param max max
	 * @return 获得一个随机数N , 满足  min &lt;= N &lt;= max
	 */
	public static int random(int min, int max) { 
		return random.nextInt(max+1-min)+min; 
	}

}
