package com.hez.kim;

import com.google.gson.Gson;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

public class App {
    private static PrivateKeys privateKeys = new PrivateKeys();
    private static Params  params = new Params("",  "","");
    public App(){
    }
    public static void main( String[] args ) throws Exception {
        String payload = generatePayload();
        String derKeySignedUrl =  ""+""  +
                getSign(payload,privateKeys.getDerStringPrivateKey()) ;
        String pemKeySignedUrl = ""+""
        +getSign(payload, privateKeys.getPemStringPrivateKey());
        try{
            System.out.print( "derKeySignedUrl :" + derKeySignedUrl+"\n"+"pemKeySignedUrl :" + pemKeySignedUrl+"\n"+"");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static String  getSign(String payload,String DEROrPEMStringPrivateKey) throws Exception {
        byte[] base64Signature = signSHA256RSA(payload,DEROrPEMStringPrivateKey);
        return payload + "." + Base64.getUrlEncoder().encodeToString(base64Signature);
    }

    private static byte[] signSHA256RSA(String input, String strPk) throws Exception {
        String realPK = strPk.replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("-----BEGIN RSA PRIVATE KEY-----", "")
                .replaceAll("\n", "");
        byte[] data = Base64.getDecoder().decode(realPK);
        byte [] realPKcs8EncodedBytes ;
        PKCS8EncodedKeySpec keySpec;
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        KeyFactory kf = KeyFactory.getInstance("RSA");

        try{
            // DEAL WITH PEM STRING
            System.out.println("DEAL WITH PEM STRING");
            ASN1EncodableVector v = new ASN1EncodableVector();
            v.add(new ASN1Integer(0));
            ASN1EncodableVector v2 = new ASN1EncodableVector();
            v2.add(new ASN1ObjectIdentifier(PKCSObjectIdentifiers.rsaEncryption.getId()));
            v2.add(DERNull.INSTANCE);
            v.add(new DERSequence(v2));
            v.add(new DEROctetString(data));
            ASN1Sequence seq = new DERSequence(v);
            realPKcs8EncodedBytes = seq.getEncoded("DER");
            keySpec = new PKCS8EncodedKeySpec(realPKcs8EncodedBytes);
            privateSignature.initSign(kf.generatePrivate(keySpec));
        } catch (Exception exception){
            // DEAL WITH DER STRING
            System.out.println("DEAL WITH DER STRING");
            realPKcs8EncodedBytes = Base64.getDecoder().decode(realPK);
            keySpec = new PKCS8EncodedKeySpec(realPKcs8EncodedBytes);
            privateSignature.initSign(kf.generatePrivate(keySpec));
        }
        privateSignature.update(input.getBytes("UTF-8"));
        return privateSignature.sign();
    }
    public static String generatePayload() {
        Date tomorrow = new Date();
        int date = (int) (Math.floor((tomorrow.getTime()/1000))+(60 * 5));
        ClaimSet claimSet = new ClaimSet(params.getIss(),params.getSub(),params.getAud(), date);
        Gson gson = new Gson();
        Header header = new Header("RS256");
        String encoded_JWT_Header = Base64.getUrlEncoder().encodeToString(gson.toJson(header).getBytes());
        String encoded_JWT_Claims_Set = Base64.getUrlEncoder().encodeToString(gson.toJson(claimSet).getBytes());
        String existing_string = encoded_JWT_Header + "." + encoded_JWT_Claims_Set;
        return existing_string.replace("=","");
    }
}