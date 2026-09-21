package bd.gov.govos.service;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.util.Utils;
import org.springframework.stereotype.Service;

@Service
public class MfaService {

	private final SecretGenerator secretGenerator = new DefaultSecretGenerator(32);
	private final CodeVerifier verifier;
	private final CodeGenerator generator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6);

	public MfaService() {
		DefaultCodeVerifier v = new DefaultCodeVerifier(generator, new SystemTimeProvider());
		v.setAllowedTimePeriodDiscrepancy(1);
		this.verifier = v;
	}

	public String generateSecret() {
		return secretGenerator.generate();
	}

	public boolean verify(String secret, String code) {
		if (secret == null || code == null || code.isBlank()) {
			return false;
		}
		return verifier.isValidCode(secret, code.trim());
	}

	/** Current TOTP for demo/dev when authenticator app is unavailable */
	public String currentCode(String secret) {
		try {
			long counter = Math.floorDiv(System.currentTimeMillis() / 1000, 30);
			return generator.generate(secret, counter);
		} catch (Exception e) {
			return null;
		}
	}

	public String qrDataUri(String username, String secret) {
		QrData data = new QrData.Builder()
				.label(username)
				.secret(secret)
				.issuer("GovOS Bangladesh")
				.algorithm(HashingAlgorithm.SHA1)
				.digits(6)
				.period(30)
				.build();
		try {
			byte[] image = new ZxingPngQrGenerator().generate(data);
			return Utils.getDataUriForImage(image, "image/png");
		} catch (QrGenerationException e) {
			return null;
		}
	}
}
