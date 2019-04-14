package test.com.jd.blockchain.tools.initializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.jd.blockchain.crypto.CryptoServiceProviders;
import com.jd.blockchain.crypto.HashDigest;
import com.jd.blockchain.crypto.HashFunction;
import com.jd.blockchain.crypto.RandomFunction;
import com.jd.blockchain.crypto.service.classic.ClassicAlgorithm;
import com.jd.blockchain.tools.initializer.LedgerBindingConfig;
import com.jd.blockchain.tools.initializer.LedgerBindingConfig.BindingConfig;
import com.jd.blockchain.utils.codec.Base58Utils;
import com.jd.blockchain.utils.io.BytesUtils;

public class LedgerBindingConfigTest {

	public static void main(String[] args) {
		//生成测试
		HashFunction hashFunc = CryptoServiceProviders.getHashFunction(ClassicAlgorithm.SHA256);
		HashDigest hash1 = hashFunc.hash(UUID.randomUUID().toString().getBytes());
		HashDigest hash2 = hashFunc.hash(UUID.randomUUID().toString().getBytes());
		System.out.println("Hash1=[" + hash1.toBase58() + "]");
		System.out.println("Hash1=[" + hash2.toBase58() + "]");
	}

	@Test
	public void testResolveAndStore() throws IOException {
		ClassPathResource ledgerBindingConfigFile = new ClassPathResource("ledger-binding.conf");
		InputStream in = ledgerBindingConfigFile.getInputStream();
		try {
			LedgerBindingConfig conf = LedgerBindingConfig.resolve(in);
			assertLedgerBindingConfig(conf);

			conf.store(System.out);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			conf.store(out);

			ByteArrayInputStream newIn = new ByteArrayInputStream(out.toByteArray());
			LedgerBindingConfig newConf = LedgerBindingConfig.resolve(newIn);

			assertLedgerBindingConfig(newConf);
		} finally {
			in.close();
		}

	}

	/**
	 * 判断指定的对象跟测试模板是否一致；
	 * 
	 * @param conf
	 */
	private void assertLedgerBindingConfig(LedgerBindingConfig conf) {
		String[] expectedHashs = { "j5ptBmn67B2p3yki3ji1j2ZMjnJhrUvP4kFpGmcXgvrhmk",
				"j5kLUENMvcUooZjKfz2bEYU6zoK9DAqbdDDU8aZEZFR4qf" };
		HashDigest[] hashs = conf.getLedgerHashs();
		for (int i = 0; i < hashs.length; i++) {
			assertEquals(expectedHashs[i], hashs[i].toBase58());
		}

		BindingConfig bindingConf_0 = conf.getLedger(hashs[0]);
		assertEquals("1", bindingConf_0.getParticipant().getAddress());
		assertEquals("keys/jd-com.priv", bindingConf_0.getParticipant().getPkPath());
		assertEquals("AdSXsf5QJpy", bindingConf_0.getParticipant().getPk());
		assertNull(bindingConf_0.getParticipant().getPassword());

		assertEquals("redis://ip:port/1", bindingConf_0.getDbConnection().getUri());
		assertEquals("kksfweffj", bindingConf_0.getDbConnection().getPassword());

		BindingConfig bindingConf_1 = conf.getLedger(hashs[1]);
		assertEquals("2", bindingConf_1.getParticipant().getAddress());
		assertEquals("keys/jd-com-1.priv", bindingConf_1.getParticipant().getPkPath());
		assertNull(bindingConf_1.getParticipant().getPk());
		assertEquals("kksafe", bindingConf_1.getParticipant().getPassword());

		assertEquals("redis://ip:port/2", bindingConf_1.getDbConnection().getUri());
		assertNull(bindingConf_1.getDbConnection().getPassword());
	}

}
