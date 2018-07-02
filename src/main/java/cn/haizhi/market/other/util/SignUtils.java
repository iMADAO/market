package cn.haizhi.market.other.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

public class SignUtils {
	//TODO
	private static final String ALGORITHM = "RSA";

	private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

	private static final String DEFAULT_CHARSET = "UTF-8";

	private static final String RSA_PRIVATE = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCe/FlEDxk6xekpw5I6Dkb/b/XR2C56mOLkouVMiIoCucDdDko6LhraX/cO6eGjfbXrJIzwdp9Xwdyb41xN4g7tNm7JSx3iFG5xm57zYGAw0w2BNVyDa2b1eR8OMKcvFwl9TFbkdSQNcoY/EAq7H03vn+7YtSVk8lA7bFmfYAN23DoNP7eDPArrpq/JwGtpzoVziX2U/kWbrIIyshWfoY8F8cjsNSsReSvuQrL7Mj2j3zrZprigKWvooS4AVM3RM5sf3yNiZJ4q148mr7utlm4NkEHtfVkMteS2qdqK16TfNACD1gI4Up43pzIZoLXjpfGBEfNEgyYUjXmmQjPFsHOTAgMBAAECggEAR1I9WWLMlqkiKDIr7w9omj7HsBOjBix/3MGWy/aUsxRRiNlYQzFbqotSccZKCuXiLlJ/R4BvixbUuLoONoIq4A+DM5oXFczEnwKJE/UwMbb8X/JoRixwS77CR8NbvKxA/Yy5jlNekQnHX5L6vWcVt2Gv7FeXJuntRDsfGXghFbiPinDPqPY4cc+GlhZn3247b+A1IBnsSlT2PT8Gx/nCpwichKptVv/nEOph68U6oRRc8g2QiixZZ4DMK6Ncn2vyD9rONPfQt/nFW7iZLxk4DcBiuAXZ8cFujItaVMpfYe+nexwhx7GHW/MGKDPyBrGprjYyw6FWShutG4K/ptrcQQKBgQDkbUYmq0x0X5/jdQHRIGSKCrFEY24eIqkWLY53e9xPA4JbTXkwe5Vjljqi7vFfMkpnSL0rrXzjZl6YWIdtLFQm9LaF4aYYKVHDUVJxj1RX+Mdi+2TgTnnBXCXT4o9OT/CIu+2k6AKUSIqBp9WJtSGdHzoFJ62YWOoPof8LibQD1wKBgQCyLT30F+bwIRlTARv29AcKQ3mmxBDqww0HsM4KVIcXLpI6KkWtztNt6xIjf8HA7DEFYvwOuf93jEWeHYgQTU4zNmay9co/GBIdSaT/GN+YNvvbmTRjXGcvbB+EQLogQc9m+ALcwgnUdQwXg1RiE1FEA2LA9QXPq70gHVdUu6aWpQKBgFwlLIhKz6OeM3xo5du7inMZDxs8VN55MuZMaE0QvPVuv/Ye0YVaRRAQOQUjCGFTgyoY8J6e9GMzF3OzOqYynJu+Waa2OO9EM8RrmKtQm0CFjcEDElVdssXqayQHH7ICk1BFJv8/zFKO1LiqwHw1giLExv3vXJq5O2ok+iJoYSKvAoGBAK0qh0HKUSokIQao11LlzEnPVlYbzA090FYfIcCscja3jmp1Vw8bKiV0B0b1DYvAgcwobayqXH0FmUuYi/q8BzqCrsFTXGKBtGk+f9tlgznehlzgwyemUKqoPOE+PybGjxoWBEIlW4Re1YesncBL4flmCP5bpHA3CzJBEfVKAxf5AoGBAKKuLwsCUjwSsc+lvGitRBF3tb6Y7GhS7/c+tUNcAs/pdI1t3J4Dp4p5Bq+OUsutJsrWV2lFI7ZDRXOvBrhLFz7+Z04cdUwieH0QZYImypM0NgZekNq/aCdOyiXUtS2uDyji8E/g8xjcBzPJOIPzgl+cDcJs38hSjeIJjByY6SiG";

	public static String sign(String content, String privateKey) {
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
					Base64.decode(privateKey));
			KeyFactory keyf = KeyFactory.getInstance(ALGORITHM);
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);

			java.security.Signature signature = java.security.Signature
					.getInstance(SIGN_ALGORITHMS);

			signature.initSign(priKey);
			signature.update(content.getBytes(DEFAULT_CHARSET));

			byte[] signed = signature.sign();

			return Base64.encode(signed);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String  getSign(String content){
		String sign = sign(content, RSA_PRIVATE);
		try {
			sign = URLEncoder.encode(sign, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return sign;
	}

}
