/**
 * Copyright (c) 2011 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.qe.katello.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.X509ExtendedKeyManager;

import net.oauth.signature.pem.PEMReader;
import net.oauth.signature.pem.PKCS1EncodedKeySpec;

/**
 * PEMx509KeyManager - An X509 Key Manager for SSL Context backed by PEM files.
 *
 * This class is pretty dumb, we just store a single x509 certificate from the pem file,
 * and return it for any request.
 *
 * ** NOTE ** We have to extend X509ExtendedKeyManager (vs implementing X509KeyManager)
 * for the two chooseEngine* methods. These are the *only way* that our ssl connection will
 * select an alias (and thus get a private key/certificate) to use.
 */

public class PEMx509KeyManager extends X509ExtendedKeyManager {
    protected Logger log = Logger.getLogger(PEMx509KeyManager.class.getName());

    private static String [] aliases = {"alias"};

    private PrivateKey privateKey;
    // we're assuming only a single certificate in the pem (so not a chain at all,
    // just the certificate for the subject).
    private X509Certificate [] certificateChain = new X509Certificate[1];
    
    public void addPEM(String certificate, String privateKey)
        throws GeneralSecurityException, IOException {
        certificateChain[0] = getX509CertificateFromPem(certificate);
        this.privateKey = getPrivateKeyFromPem(privateKey);

        log.finer("cert info! " + certificateChain[0].getSubjectDN().getName());
    }

    /* taken from oauth's RSA_SHA1.java */
    private PrivateKey getPrivateKeyFromPem(String pem)
        throws GeneralSecurityException, IOException {

        InputStream stream = new ByteArrayInputStream(
                pem.getBytes("UTF-8"));

        PEMReader reader = new PEMReader(stream);
        byte[] bytes = reader.getDerBytes();
        KeySpec keySpec;

        if (PEMReader.PRIVATE_PKCS1_MARKER.equals(reader.getBeginMarker())) {
            keySpec = (new PKCS1EncodedKeySpec(bytes)).getKeySpec();
        }
        else if (PEMReader.PRIVATE_PKCS8_MARKER.equals(reader.getBeginMarker())) {
            keySpec = new PKCS8EncodedKeySpec(bytes);
        }
        else {
            throw new IOException("Invalid PEM file: Unknown marker " +
                    "for private key " + reader.getBeginMarker());
        }

        KeyFactory fac = KeyFactory.getInstance("RSA");
        return fac.generatePrivate(keySpec);
    }

    private X509Certificate getX509CertificateFromPem(String pem)
        throws GeneralSecurityException, IOException {

        InputStream stream = new ByteArrayInputStream(pem.getBytes("UTF-8"));

        PEMReader reader = new PEMReader(stream);
        byte[] bytes = reader.getDerBytes();

        if (!PEMReader.CERTIFICATE_X509_MARKER.equals(reader.getBeginMarker())) {
            throw new IOException("Invalid PEM file: Unknown marker for " +
                " public key or cert " + reader.getBeginMarker());
        }

        CertificateFactory fac = CertificateFactory.getInstance("X509");
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        X509Certificate cert = (X509Certificate) fac.generateCertificate(in);

        return cert;
    }

    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        if ( privateKey == null ) {
            return null;
        }
        return aliases[0];
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        return null;
        //return aliases[0];
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        log.fine("returning x509 certificate");
        return certificateChain;
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        if ( privateKey == null ) {
            return null;
        }
        return aliases;
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        return privateKey;
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return null;
        //return aliases;
    }

    public String chooseEngineClientAlias(String[] keyType,
        Principal[] issuers, SSLEngine engine) {
        log.fine("chooseEngineClientAlias");
        log.info("Principal name in engine: " + engine.getHandshakeSession().getLocalPrincipal().getName());
        try {
            engine.beginHandshake();
        } catch (SSLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if ( privateKey == null ) {
            return null;
        }
        return aliases[0];
    }

    @Override
    public String chooseEngineServerAlias(String keyType, Principal[] issuers,
        SSLEngine engine) {
        return null;
    }
}