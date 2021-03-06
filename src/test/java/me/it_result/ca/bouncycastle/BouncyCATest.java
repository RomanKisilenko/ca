/**
 * Copyright 2010 Roman Kisilenko
 *
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.it_result.ca.bouncycastle;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Date;

import me.it_result.ca.CA;
import me.it_result.ca.CAException;
import me.it_result.ca.CATest;
import me.it_result.ca.X509Assertions;
import me.it_result.ca.db.Database;
import me.it_result.ca.db.FileDatabase;

import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.util.encoders.Base64;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * @author roman
 *
 */
public class BouncyCATest extends CATest {

	private static final String DATABASE_LOCATION = "target/ca.keystore";
	private static final int VALIDITY_DAYS = 365;
	private static final String KEYSTORE_PASSWORD = "changeme";
	private static final String ISSUER = "CN=CA,O=it-result.me,C=RU";

	private String jdkSignatureAlgorithm;
	
	private BouncyCA ca;

	@BeforeMethod
	@Parameters({"keyAlgorithm", "keyBits", "bouncyCastleProviderSignatureAlgorithm", "jdkSignatureAlgorithm"})
	public void setUp(@Optional(value="RSA") String keyAlgorithm, @Optional(value="1024") int keyBits, @Optional("SHA512WithRSA") String bouncyCastleProviderSignatureAlgorithm, @Optional("SHA512withRSA") String jdkSignatureAlgorithm) throws CAException {
		tearDown();
		this.jdkSignatureAlgorithm = jdkSignatureAlgorithm;
		ca = new BouncyCA(getDatabase(), keyAlgorithm, keyBits, VALIDITY_DAYS, KEYSTORE_PASSWORD, ISSUER, bouncyCastleProviderSignatureAlgorithm, ProfileRegistry.getDefaultInstance());
	}
	
	@AfterMethod
	public void tearDown() throws CAException {
		if (ca != null) {
			ca.destroy();
			ca = null;
		}
	}

	private Database getDatabase() {
		return new FileDatabase(DATABASE_LOCATION);
	}

	@Override
	protected CA ca() {
		return ca;
	}

	@Override
	protected void verifyCACertificates(Date minBeforeDate, Date maxBeforeDate) throws Exception {
		// CA keypair generated and stored in a keystore
		// CA certificate is generated and stored in a keystore
		assertNotNull(ca.getCACertificate());
		// CA certificate contain proper SN and extensions
		verifyCertificate(ca.getCACertificate(), ISSUER, new BigInteger("1"), true, false, minBeforeDate, maxBeforeDate);
	}
	
	private void verifyCertificate(X509Certificate cert, String subjectName, BigInteger serialNumber, boolean ca, boolean server, Date minBeforeDate, Date maxBeforeDate) throws Exception {
		X509Certificate caCert = this.ca.getCACertificate();
		// See http://citrixblogger.org/2010/09/10/certificate-public-key-usage/ for a good assembly of key usage guideline materials
		int expectedKeyUsage;
		if (ca)
			expectedKeyUsage = X509KeyUsage.cRLSign | X509KeyUsage.keyCertSign;
		else 
			expectedKeyUsage = X509KeyUsage.digitalSignature | X509KeyUsage.keyEncipherment;// | X509KeyUsage.dataEncipherment;
		new X509Assertions(cert).
			type("X.509").
			version(3).
			issuedBy(caCert).
			subjectName(subjectName).
			serialNumber(serialNumber).
			validDuring(VALIDITY_DAYS, minBeforeDate, maxBeforeDate).
			caCertificate(ca).
			containsSKI().
			containsAKI().
			eku(ca ? null : new KeyPurposeId[] {server ? KeyPurposeId.id_kp_serverAuth : KeyPurposeId.id_kp_clientAuth}).
			keyUsage(expectedKeyUsage).
			noMoreExtensions().
			signatureAlgrithm(jdkSignatureAlgorithm);
	}

	@DataProvider(name="signCertificate")
	public Object[][] getSignCertificateData() {
		byte[] serverCsr = Base64.decode("MIIBizCB9QIBADAqMQ0wCwYDVQQDDAR0ZXN0MRkwFwYKCZImiZPyLGQBAQwJdGVzdEB0ZXN0MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCwCNcx5PAYS22s1DiHnDbWVR1/k1EiWJp/Wxzys/SN0NyVE6wUUE9D6zBzrfpZoeM/lMqVZy6OD3Q2FZTvqL/fw8lrL0MGruID4iSsHlJllpsg4WW7qFtK6m16tFJRWGVFnIe+OTVm0BI0dxV2/UDoQXEZ780Bxx6X77cbdfVC/QIDAQABoCIwIAYJKoZIhvcNAQkDMRMTEVNlcnZlckNlcnRpZmljYXRlMA0GCSqGSIb3DQEBDQUAA4GBABL5OAANZXFvBcO+9+2yxUIHFlzAo4VlIZKXTo/aWxeez4hLqmxzdFAvIrp/AyG6/GJNKB5jYlYtrjgezkqJuF94IOrmocjcylclmRuqIK6JgMMYUy2Q3dawwk9EB3WOWZsJTnn0Yrix4VKEwgvE1jrImojetRh7noTuZhHnC9/a");
		byte[] clientCsr = Base64.decode("MIIBZzCB0QIBADAqMQ0wCwYDVQQDDAR0ZXN0MRkwFwYKCZImiZPyLGQBAQwJdGVzdEB0ZXN0MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCwCNcx5PAYS22s1DiHnDbWVR1/k1EiWJp/Wxzys/SN0NyVE6wUUE9D6zBzrfpZoeM/lMqVZy6OD3Q2FZTvqL/fw8lrL0MGruID4iSsHlJllpsg4WW7qFtK6m16tFJRWGVFnIe+OTVm0BI0dxV2/UDoQXEZ780Bxx6X77cbdfVC/QIDAQABMA0GCSqGSIb3DQEBDQUAA4GBAAUiRW/jaHFbWR27x95v7n2SlRKJhPMQXV26uSNO++q0N7JAvx5vHfDEHMiMPUJYj0zaDiS9H0Xi5jajJV+mJNNuASZMsPCym9kfyr6Q4gMflweqP75Wgw4x4r8rgY60CLDQTY8UU9ic8EfCRCuImQLPFD3RHCHFCBYLx4NBjHW2");
		byte[] externalCsr = Base64.decode("MIICbTCCAVUCAQAwKjENMAsGA1UEAwwEdGVzdDEZMBcGCgmSJomT8ixkAQEMCXRlc3RAdGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKsExxF9s51tP/Phhybs9t3eVQyksxC1hn/tv9FSQ3N0UrMW5Uuju0cQLFelZwnRLllCES3J4juGQw1sl5TqdFm7wLK1CSwTuEQhx2eLlpBq/0m5DN91xeDZe9jaA5XuH+uGzXm6lWohgbVaqAOvyxX5Y0E1P4XxtWKKCLiODgNQcvdo7XbbWIujlQfau0tNNPg2A/GbiDVzcF1P2iUnlraJt9BKjObNsP78eyPDQgYmnc5MVaBwxYvKnhRDkyZNESvdVGWQumRT8syA5PeL6563ld5dC9a5lBWghK9LJroQLFl+HJCGiGRfOTPd6hXZu5Vh1Vz3dwnDEvyusALdkmECAwEAATANBgkqhkiG9w0BAQ0FAAOCAQEASOZa0icr8rxBVJjwXOlxcxRhlW9ROsdVxqYGPFy8qobquTXvxhcPHRUCpGev311f61yHSqQr6aCDmTyyMlqfiJ6gAWS7EJPxuxNLRecpRaJ7Wnl33PKLe5YvQia5K+fyZT06aFaLRQlioXilHBPMzVANjBbe9banzykoO2VTp1/lEFM3lfTRSH4tE9QX/Y5HHcaic8S/jr8aPt/kMHSmdDVsK5aEoT0tpHHYBg7DF7QBVvZEBtG0cJaEjN91tLlqKtdNz8LILnOmtgOWCgjb4F/giqKy9/Hfpxf8iC3eDscsiPJ7uf8QHWqa603Q6UISvq3Eg8Anls3aX3f8C12YoA==");
		return new Object[][] {new Object[] {serverCsr, Boolean.TRUE}, new Object[] {clientCsr, Boolean.FALSE}, new Object[] {externalCsr, Boolean.FALSE}};
	}
	
	@Test(dataProvider="signCertificate")
	public void testSignCertificate(byte[] csr, boolean server) throws Exception {
		// Given an initialized CA 
		ca().initialize();
		// When certificate is signed
		Date minBeforeDate = new Date();
		X509Certificate cert = ca().signCertificate(csr);
		Date maxBeforeDate = new Date();
		// Then a valid certificate should be generated
		assertNotNull(cert);
		verifyCertificate(cert, getSampleSubjectName(), new BigInteger("2"), false, server, minBeforeDate, maxBeforeDate);
		// And the certificate should be included in a signed certificate list
		assertTrue(ca().listCertificates().contains(X509Assertions.toJdkCertificate(cert)));
	}
	
	private String getSampleSubjectName() {
		return "CN=test,UID=test@test";
	}

}
