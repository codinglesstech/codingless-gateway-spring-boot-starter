package tech.codingless.core.gateway.util;

public class LongUtil {

 
	public static long add(Long v1, Long v2) {
		return (v1 == null ? 0 : v1) + (v2 == null ? 0 : v2);
	}

	public static long get(Long v1, long defalutVal) {
		return v1 != null ? v1 : defalutVal;
	}

	public static Long del(Long v1, Long v2) {
		return (v1 == null ? 0 : v1) - (v2 == null ? 0 : v2);
	}

	public static long add(Long... v1) {
		if (v1 == null) {
			return 0L;
		}
		long sum = 0L;
		for (Long v : v1) {
			sum += (v == null ? 0L : v);
		}
		return sum;
	}

}
